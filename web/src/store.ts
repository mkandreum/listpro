import { useSyncExternalStore } from 'react';
import { api } from './api';
import { uid } from './lib';
import type { Catalog, Category, ListData, MealPlan, PlanPrefs, Product, SavedBasket, Section, Subcategory } from './types';

export type ThemePref = 'system' | 'light' | 'dark';

interface Indexes {
  products: Map<string, Product>;
  sections: Map<string, Section>;
  categories: Map<number, Category & { section: string }>;
  subcategories: Map<number, Subcategory & { category: number; section: string }>;
  aisleOrder: Map<number, number>; // subcategoría → orden de tienda
}

export interface State {
  catalog: Catalog | null;
  catalogError: boolean;
  idx: Indexes;
  basket: Record<string, number>;
  addedPrices: Record<string, number>;
  inCart: string[];
  savedBaskets: SavedBasket[];
  sync: { code: string | null; updatedAt: string | null; dirty: boolean };
  theme: ThemePref;
  toasts: { id: string; text: string }[];
  plan: MealPlan | null;
  planPrefs: PlanPrefs | null;
}

function load<T>(key: string, fallback: T): T {
  try { const v = localStorage.getItem(key); return v ? (JSON.parse(v) as T) : fallback; } catch { return fallback; }
}
function save(key: string, value: unknown) {
  try { localStorage.setItem(key, JSON.stringify(value)); } catch { /* modo privado o almacenamiento lleno */ }
}

const emptyIdx = (): Indexes => ({ products: new Map(), sections: new Map(), categories: new Map(), subcategories: new Map(), aisleOrder: new Map() });

let state: State = {
  catalog: null,
  catalogError: false,
  idx: emptyIdx(),
  basket: load('listproBasket', {}),
  addedPrices: load('listproAddedPrices', {}),
  inCart: load('listproInCart', []),
  savedBaskets: load<SavedBasket[]>('listproSavedBaskets', []).map((b) => ({ ...b, id: b.id ?? uid() })),
  sync: { code: null, updatedAt: null, dirty: false, ...load('listproSync', {}) },
  theme: load<ThemePref>('listproThemePref', 'system'),
  toasts: [],
  plan: load<MealPlan | null>('listproPlan', null),
  planPrefs: load<PlanPrefs | null>('listproPlanPrefs', null),
};

const listeners = new Set<() => void>();
function set(patch: Partial<State>) {
  state = { ...state, ...patch };
  listeners.forEach((l) => l());
}
const subscribe = (l: () => void) => { listeners.add(l); return () => { listeners.delete(l); }; };

export function useStore<T>(selector: (s: State) => T): T {
  return useSyncExternalStore(subscribe, () => selector(state));
}
export const getState = () => state;

// --- Catálogo ---
function buildIndexes(c: Catalog): Indexes {
  const idx = emptyIdx();
  let order = 0;
  for (const s of c.sections) {
    idx.sections.set(s.id, s);
    for (const cat of s.categories) {
      idx.categories.set(cat.id, { ...cat, section: s.id });
      for (const sub of cat.subcategories) {
        idx.subcategories.set(sub.id, { ...sub, category: cat.id, section: s.id });
        idx.aisleOrder.set(sub.id, order++);
      }
    }
  }
  for (const p of c.products) idx.products.set(p.id, p);
  return idx;
}

const CATALOG_CACHE = 'listpro-data-v1';

/** Pinta al instante el último catálogo guardado; la red lo refresca después. */
async function loadCachedCatalog() {
  try {
    const hit = await (await caches.open(CATALOG_CACHE)).match('/api/catalog');
    if (hit && !state.catalog) {
      const c = (await hit.json()) as Catalog;
      if (!state.catalog) set({ catalog: c, idx: buildIndexes(c) });
    }
  } catch { /* sin Cache API (http sin TLS, modo privado) */ }
}

export async function loadCatalog(): Promise<boolean> {
  if (!state.catalog) await loadCachedCatalog();
  try {
    const res = await fetch('/api/catalog', { cache: 'no-cache' });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const copy = res.clone();
    caches.open(CATALOG_CACHE).then((cache) => cache.put('/api/catalog', copy)).catch(() => {});
    const c = (await res.json()) as Catalog;
    if (state.catalog?.version === c.version && state.catalog.updatedAt === c.updatedAt) return false;
    const changed = state.catalog != null && state.catalog.version !== c.version;
    set({ catalog: c, idx: buildIndexes(c), catalogError: false });
    return changed;
  } catch {
    if (!state.catalog) set({ catalogError: true });
    return false;
  }
}

export async function checkCatalogUpdate() {
  if (!state.catalog) return loadCatalog();
  try {
    const meta = await api.catalogMeta();
    if (meta.version !== state.catalog.version && (await loadCatalog())) toast('Precios actualizados');
  } catch { /* sin red: seguimos con lo que hay */ }
}

// --- Cesta ---
function persistList() {
  save('listproBasket', state.basket);
  save('listproAddedPrices', state.addedPrices);
  save('listproInCart', state.inCart);
  save('listproSavedBaskets', state.savedBaskets);
  markDirty();
}

export function changeQty(id: string, delta: number) {
  const basket = { ...state.basket };
  const addedPrices = { ...state.addedPrices };
  let inCart = state.inCart;
  const next = Math.max(0, (basket[id] ?? 0) + delta);
  if (next === 0) {
    delete basket[id];
    delete addedPrices[id];
    inCart = inCart.filter((x) => x !== id);
  } else {
    basket[id] = next;
    const p = state.idx.products.get(id);
    if (p && addedPrices[id] == null) addedPrices[id] = p.price;
  }
  set({ basket, addedPrices, inCart });
  persistList();
}

export function toggleInCart(id: string) {
  const inCart = state.inCart.includes(id) ? state.inCart.filter((x) => x !== id) : [...state.inCart, id];
  set({ inCart });
  persistList();
}

export function clearBasket() {
  set({ basket: {}, addedPrices: {}, inCart: [] });
  persistList();
}

export function dropMissing() {
  const basket = { ...state.basket };
  for (const id of Object.keys(basket)) if (!state.idx.products.has(id)) delete basket[id];
  set({ basket });
  persistList();
}

export function basketLines() {
  const { basket, idx } = state;
  return Object.entries(basket)
    .flatMap(([id, qty]) => { const p = idx.products.get(id); return p ? [{ p, qty }] : []; })
    .sort((a, b) => (idx.aisleOrder.get(a.p.subcategory) ?? 1e9) - (idx.aisleOrder.get(b.p.subcategory) ?? 1e9) || a.p.name.localeCompare(b.p.name));
}

export function saveBasket(name: string) {
  const lines = basketLines();
  if (!lines.length) return false;
  const snapshot: SavedBasket['snapshot'] = {};
  for (const { p } of lines) snapshot[p.id] = { name: p.name, price: p.price, section: p.section, category: p.category };
  const basket: SavedBasket = {
    id: uid(), name, date: new Date().toISOString(),
    total: Math.round(lines.reduce((a, l) => a + l.p.price * l.qty, 0) * 100) / 100,
    items: Object.fromEntries(lines.map((l) => [l.p.id, l.qty])), snapshot,
  };
  set({ savedBaskets: [...state.savedBaskets, basket], basket: {}, addedPrices: {}, inCart: [] });
  persistList();
  return true;
}

export function reuseBasket(id: string) {
  const b = state.savedBaskets.find((x) => x.id === id);
  if (!b) return 0;
  const basket = { ...state.basket };
  const addedPrices = { ...state.addedPrices };
  let n = 0;
  for (const [pid, q] of Object.entries(b.items)) {
    const p = state.idx.products.get(pid);
    if (!p) continue;
    basket[pid] = (basket[pid] ?? 0) + q;
    addedPrices[pid] ??= p.price;
    n++;
  }
  set({ basket, addedPrices });
  persistList();
  return n;
}

export function deleteBasket(id: string) {
  set({ savedBaskets: state.savedBaskets.filter((x) => x.id !== id) });
  persistList();
}

// --- Tema ---
export function setTheme(theme: ThemePref) {
  set({ theme });
  save('listproThemePref', theme);
}

// --- Avisos ---
export function toast(text: string) {
  const id = uid();
  set({ toasts: [...state.toasts, { id, text }] });
  setTimeout(() => set({ toasts: state.toasts.filter((t) => t.id !== id) }), 2800);
}

// --- Copia de seguridad ---
export function exportBackup() {
  const data = { app: 'listpro', version: 2, savedBaskets: state.savedBaskets, basket: state.basket };
  const a = document.createElement('a');
  a.href = URL.createObjectURL(new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' }));
  a.download = 'listpro_backup.json';
  a.click();
  setTimeout(() => URL.revokeObjectURL(a.href), 1000);
}

export async function importBackup(file: File) {
  const data = JSON.parse(await file.text());
  if (!Array.isArray(data.savedBaskets)) throw new Error('formato');
  set({
    savedBaskets: data.savedBaskets.map((b: SavedBasket) => ({ ...b, id: b.id ?? uid() })),
    basket: data.basket && typeof data.basket === 'object' ? data.basket : state.basket,
  });
  persistList();
}

// --- Sincronización (mismo protocolo que la app Android) ---
const payload = (): ListData => ({
  basket: state.basket, addedPrices: state.addedPrices, inCart: state.inCart, savedBaskets: state.savedBaskets,
  planId: state.plan?.id ?? null, planPrefs: state.planPrefs,
});

function setSync(sync: State['sync']) {
  set({ sync });
  save('listproSync', sync);
}

let pushTimer: ReturnType<typeof setTimeout> | undefined;
function markDirty() {
  if (!state.sync.code) return;
  setSync({ ...state.sync, dirty: true });
  clearTimeout(pushTimer);
  pushTimer = setTimeout(pushSync, 1200);
}

// Si dos dispositivos escriben a la vez: cestas guardadas = unión por id; cesta actual = la de este dispositivo.
function mergeSaved(remote: SavedBasket[] = []) {
  const ids = new Set(state.savedBaskets.map((b) => b.id));
  return [...state.savedBaskets, ...remote.filter((b) => !ids.has(b.id))].sort((a, b) => a.date.localeCompare(b.date));
}

function applyRemote(data: ListData) {
  if (data.planPrefs) { set({ planPrefs: data.planPrefs }); save('listproPlanPrefs', data.planPrefs); }
  if (data.planId && data.planId !== state.plan?.id) {
    api.getPlan(data.planId).then((plan) => { set({ plan }); save('listproPlan', plan); }).catch(() => {});
  }
  set({
    basket: data.basket ?? {},
    addedPrices: data.addedPrices ?? {},
    inCart: data.inCart ?? [],
    savedBaskets: (data.savedBaskets ?? []).map((b) => ({ ...b, id: b.id ?? uid() })),
  });
  save('listproBasket', state.basket);
  save('listproAddedPrices', state.addedPrices);
  save('listproInCart', state.inCart);
  save('listproSavedBaskets', state.savedBaskets);
}

export async function pushSync() {
  const { code, dirty, updatedAt } = state.sync;
  if (!code || !dirty) return;
  try {
    let res = await api.putList(code, payload(), updatedAt);
    if (res.status === 409) {
      const remote = await res.json();
      set({ savedBaskets: mergeSaved(remote.data.savedBaskets) });
      save('listproSavedBaskets', state.savedBaskets);
      res = await api.putList(code, payload(), null, true);
    }
    if (res.status === 404) {
      toast('Ese código de sincronización ya no existe');
      setSync({ code: null, updatedAt: null, dirty: false });
    } else if (res.ok) {
      setSync({ code, updatedAt: (await res.json()).updatedAt, dirty: false });
    }
  } catch { /* sin red: se reintenta en el próximo cambio o sondeo */ }
}

export async function pullSync() {
  const { code, dirty, updatedAt } = state.sync;
  if (!code || document.visibilityState === 'hidden') return;
  if (dirty) return pushSync();
  try {
    const remote = await api.getList(code);
    if (remote.updatedAt !== updatedAt && !state.sync.dirty) {
      applyRemote(remote.data);
      setSync({ code, updatedAt: remote.updatedAt, dirty: false });
    }
  } catch { /* sin red */ }
}

export async function createSync() {
  const r = await api.createList(payload());
  setSync({ code: r.code, updatedAt: r.updatedAt, dirty: false });
}

export async function joinSync(raw: string) {
  const code = raw.trim().toUpperCase();
  if (!/^[A-Z2-9]{8}$/.test(code)) throw new Error('El código tiene 8 letras y números');
  const remote = await api.getList(code).catch((e) => {
    throw new Error(e.status === 404 ? 'No existe ningún código así' : 'No se pudo conectar');
  });
  applyRemote({ ...remote.data, savedBaskets: mergeSaved(remote.data.savedBaskets) });
  setSync({ code, updatedAt: remote.updatedAt, dirty: true });
  pushSync();
}

export function leaveSync() {
  setSync({ code: null, updatedAt: null, dirty: false });
}

// --- Planes de cenas ---
export async function generatePlan(prefs: PlanPrefs) {
  set({ planPrefs: prefs });
  save('listproPlanPrefs', prefs);
  const plan = await api.createPlan(prefs).catch((e) => {
    throw new Error(e?.body?.error ?? 'No se ha podido crear el plan. Prueba otra vez.');
  });
  set({ plan });
  save('listproPlan', plan);
  markDirty();
  return plan;
}

export function clearPlan() {
  set({ plan: null });
  save('listproPlan', null);
  markDirty();
}

/** Mete en la cesta los envases que hacen falta para el plan (o solo para una cena). */
export function addPlanToBasket(items: { productId: string; packs: number }[]) {
  const basket = { ...state.basket };
  const addedPrices = { ...state.addedPrices };
  let n = 0;
  for (const { productId, packs } of items) {
    const p = state.idx.products.get(productId);
    if (!p) continue;
    basket[productId] = Math.max(basket[productId] ?? 0, packs);
    addedPrices[productId] ??= p.price;
    n++;
  }
  set({ basket, addedPrices });
  persistList();
  return n;
}
