import { useSyncExternalStore } from 'react';
import { api } from './api';
import { uid } from './lib';
import { MEMBER_HUES, merge3, splitByWeights, splitEqual } from './share';
import type { Catalog, Category, Expense, ListData, MealPlan, Member, PlanPrefs, Product, SavedBasket, Section, SplitMode, Subcategory } from './types';

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
  members: Member[];
  addedBy: Record<string, string>;
  pickedBy: Record<string, string>;
  expenses: Expense[];
  /** Quién soy yo en la lista compartida (solo en este dispositivo). */
  meId: string | null;
  /** Última versión de la lista que vimos en el servidor: la base de la fusión a tres bandas. */
  syncBase: ListData | null;
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
  members: load('listproMembers', []),
  addedBy: load('listproAddedBy', {}),
  pickedBy: load('listproPickedBy', {}),
  expenses: load('listproExpenses', []),
  meId: load<string | null>('listproMeId', null),
  syncBase: load<ListData | null>('listproSyncBase', null),
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
  save('listproMembers', state.members);
  save('listproAddedBy', state.addedBy);
  save('listproPickedBy', state.pickedBy);
  save('listproExpenses', state.expenses);
  markDirty();
}

const without = <T,>(m: Record<string, T>, ids: Iterable<string>) => {
  const out = { ...m };
  for (const id of ids) delete out[id];
  return out;
};

export const isShared = (s: State = state) => s.sync.code != null;

export function changeQty(id: string, delta: number) {
  const basket = { ...state.basket };
  const addedPrices = { ...state.addedPrices };
  let { inCart, addedBy, pickedBy } = state;
  const next = Math.max(0, (basket[id] ?? 0) + delta);
  if (next === 0) {
    delete basket[id];
    delete addedPrices[id];
    inCart = inCart.filter((x) => x !== id);
    addedBy = without(addedBy, [id]);
    pickedBy = without(pickedBy, [id]);
  } else {
    if (!basket[id] && state.meId && isShared()) addedBy = { ...addedBy, [id]: state.meId };
    basket[id] = next;
    const p = state.idx.products.get(id);
    if (p && addedPrices[id] == null) addedPrices[id] = p.price;
  }
  set({ basket, addedPrices, inCart, addedBy, pickedBy });
  persistList();
}

export function toggleInCart(id: string) {
  const had = state.inCart.includes(id);
  const inCart = had ? state.inCart.filter((x) => x !== id) : [...state.inCart, id];
  const pickedBy = had || !state.meId ? without(state.pickedBy, [id]) : { ...state.pickedBy, [id]: state.meId };
  set({ inCart, pickedBy });
  persistList();
}

export function clearBasket() {
  set({ basket: {}, addedPrices: {}, inCart: [], addedBy: {}, pickedBy: {} });
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

/** Lo que entra en la compra: si has marcado algo, solo lo marcado (lo demás se queda en la cesta); si no, todo. */
export function checkoutLines() {
  const lines = basketLines();
  const picked = lines.filter((l) => state.inCart.includes(l.p.id));
  return picked.length ? picked : lines;
}

export interface Checkout {
  name: string;
  /** Lo que marca el tique (puede no coincidir con los precios de la web). */
  amount: number;
  paidBy?: string;
  mode?: SplitMode;
  among?: string[];
}

/** Cuánto le toca a cada uno de una compra. Con «cada uno lo suyo», lo que pidió cada uno; lo que no es de nadie, a partes iguales. */
export function purchaseShares(c: Required<Pick<Checkout, 'amount' | 'mode' | 'among'>>) {
  if (!c.among.length) return {};
  if (c.mode === 'equal') return splitEqual(c.amount, c.among);
  const w: Record<string, number> = Object.fromEntries(c.among.map((m) => [m, 0]));
  for (const { p, qty } of checkoutLines()) {
    const cost = p.price * qty;
    const who = state.addedBy[p.id];
    if (who && who in w) w[who] += cost;
    else for (const m of c.among) w[m] += cost / c.among.length;
  }
  return splitByWeights(c.amount, w);
}

/** Guarda la compra en el historial, la quita de la cesta y, si la lista es compartida, la apunta en las cuentas. */
export function finishShop(c: Checkout) {
  const lines = checkoutLines();
  if (!lines.length) return false;
  const snapshot: SavedBasket['snapshot'] = {};
  for (const { p } of lines) snapshot[p.id] = { name: p.name, price: p.price, section: p.section, category: p.category };
  const saved: SavedBasket = {
    id: uid(), name: c.name, date: new Date().toISOString(),
    total: Math.round(lines.reduce((a, l) => a + l.p.price * l.qty, 0) * 100) / 100,
    items: Object.fromEntries(lines.map((l) => [l.p.id, l.qty])), snapshot,
  };
  const ids = lines.map((l) => l.p.id);
  let expenses = state.expenses;
  if (isShared() && c.paidBy && c.among?.length && c.amount > 0) {
    const shares = purchaseShares({ amount: c.amount, mode: c.mode ?? 'equal', among: c.among });
    expenses = [...expenses, {
      id: uid(), at: saved.date, title: c.name, amount: Math.round(c.amount * 100) / 100, paidBy: c.paidBy,
      shares, kind: 'purchase', mode: c.mode ?? 'equal', basketId: saved.id,
    }];
  }
  set({
    savedBaskets: [...state.savedBaskets, saved],
    basket: without(state.basket, ids),
    addedPrices: without(state.addedPrices, ids),
    inCart: state.inCart.filter((id) => !ids.includes(id)),
    addedBy: without(state.addedBy, ids),
    pickedBy: without(state.pickedBy, ids),
    expenses,
  });
  persistList();
  return true;
}

export function reuseBasket(id: string) {
  const b = state.savedBaskets.find((x) => x.id === id);
  if (!b) return 0;
  const basket = { ...state.basket };
  const addedPrices = { ...state.addedPrices };
  const addedBy = { ...state.addedBy };
  let n = 0;
  for (const [pid, q] of Object.entries(b.items)) {
    const p = state.idx.products.get(pid);
    if (!p) continue;
    if (!basket[pid] && state.meId && isShared()) addedBy[pid] = state.meId;
    basket[pid] = (basket[pid] ?? 0) + q;
    addedPrices[pid] ??= p.price;
    n++;
  }
  set({ basket, addedPrices, addedBy });
  persistList();
  return n;
}

export function deleteBasket(id: string) {
  set({ savedBaskets: state.savedBaskets.filter((x) => x.id !== id) });
  persistList();
}

// --- Cuentas ---
export function addExpense(e: { title: string; amount: number; paidBy: string; among: string[] }) {
  const amount = Math.round(e.amount * 100) / 100;
  set({ expenses: [...state.expenses, {
    id: uid(), at: new Date().toISOString(), title: e.title, amount, paidBy: e.paidBy,
    shares: splitEqual(amount, e.among), kind: 'purchase', mode: 'equal',
  }] });
  persistList();
}

/** Apunta que `from` le ha pagado `amount` a `to`. */
export function settleUp(from: string, to: string, amount: number) {
  set({ expenses: [...state.expenses, {
    id: uid(), at: new Date().toISOString(), title: 'Pago', amount, paidBy: from, shares: { [to]: amount }, kind: 'settle',
  }] });
  persistList();
}

export function deleteExpense(id: string) {
  set({ expenses: state.expenses.filter((e) => e.id !== id) });
  persistList();
}

// --- Miembros ---
function setMe(meId: string) {
  set({ meId });
  save('listproMeId', meId);
}

/** Me apunta (o me renombra) en la lista con este nombre. */
export function upsertMe(name: string) {
  const clean = name.trim().slice(0, 24);
  if (!clean) return;
  let meId = state.meId;
  if (!meId) { meId = uid(); setMe(meId); }
  const exists = state.members.some((m) => m.id === meId);
  const used = new Set(state.members.map((m) => m.hue));
  const members = exists
    ? state.members.map((m) => (m.id === meId ? { ...m, name: clean } : m))
    : [...state.members, {
        id: meId, name: clean, joinedAt: new Date().toISOString(),
        hue: MEMBER_HUES.find((h) => !used.has(h)) ?? MEMBER_HUES[state.members.length % MEMBER_HUES.length],
      }];
  set({ members });
  persistList();
}

export const memberName = (id: string | undefined, s: State = state) =>
  id === s.meId ? 'Tú' : s.members.find((m) => m.id === id)?.name ?? 'Alguien';

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
  members: state.members, addedBy: state.addedBy, pickedBy: state.pickedBy, expenses: state.expenses,
});

function setSync(sync: State['sync']) {
  set({ sync });
  save('listproSync', sync);
}

function setBase(syncBase: ListData | null) {
  set({ syncBase });
  save('listproSyncBase', syncBase);
}

// Cada cambio local sube la revisión; así un cambio hecho mientras se sube otro no se pierde.
let rev = 0;
let pushTimer: ReturnType<typeof setTimeout> | undefined;
function markDirty() {
  rev++;
  if (!state.sync.code) return;
  setSync({ ...state.sync, dirty: true });
  clearTimeout(pushTimer);
  pushTimer = setTimeout(pushSync, 700);
}

function applyRemote(data: ListData) {
  if (data.planPrefs) { set({ planPrefs: data.planPrefs }); save('listproPlanPrefs', data.planPrefs); }
  if (data.planId && data.planId !== state.plan?.id) {
    api.getPlan(data.planId).then((plan) => { set({ plan }); save('listproPlan', plan); }).catch(() => {});
  } else if (!data.planId && state.plan) {
    set({ plan: null }); save('listproPlan', null);
  }
  set({
    basket: data.basket ?? {},
    addedPrices: data.addedPrices ?? {},
    inCart: data.inCart ?? [],
    savedBaskets: (data.savedBaskets ?? []).map((b) => ({ ...b, id: b.id ?? uid() })),
    members: data.members ?? [],
    addedBy: data.addedBy ?? {},
    pickedBy: data.pickedBy ?? {},
    expenses: data.expenses ?? [],
  });
  for (const [k, v] of [['listproBasket', state.basket], ['listproAddedPrices', state.addedPrices], ['listproInCart', state.inCart],
    ['listproSavedBaskets', state.savedBaskets], ['listproMembers', state.members], ['listproAddedBy', state.addedBy],
    ['listproPickedBy', state.pickedBy], ['listproExpenses', state.expenses]] as const) save(k, v);
}

let pushing = false;
export async function pushSync() {
  const { code, dirty } = state.sync;
  if (!code || !dirty || pushing) return;
  pushing = true;
  try {
    let updatedAt = state.sync.updatedAt;
    for (let attempt = 0; attempt < 4; attempt++) {
      const sentRev = rev;
      const sent = payload();
      const res = await api.putList(code, sent, updatedAt, attempt === 3);
      if (res.status === 409) {
        // Alguien cambió la lista mientras tanto: juntamos sus cambios con los nuestros y reintentamos
        const remote = await res.json();
        applyRemote(merge3(state.syncBase, payload(), remote.data));
        setBase(remote.data);
        updatedAt = remote.updatedAt;
        continue;
      }
      if (res.status === 404) {
        toast('Esa lista compartida ya no existe');
        setSync({ code: null, updatedAt: null, dirty: false });
        setBase(null);
      } else if (res.ok) {
        setBase(sent);
        setSync({ code, updatedAt: (await res.json()).updatedAt, dirty: rev !== sentRev });
        if (rev !== sentRev) { clearTimeout(pushTimer); pushTimer = setTimeout(pushSync, 300); }
      }
      break;
    }
  } catch { /* sin red: se reintenta en el próximo cambio o sondeo */ } finally {
    pushing = false;
  }
}

export async function pullSync() {
  const { code, dirty, updatedAt } = state.sync;
  if (!code || document.visibilityState === 'hidden') return;
  if (dirty) return pushSync();
  try {
    const remote = await api.getList(code);
    if (remote.updatedAt !== updatedAt && !state.sync.dirty && !pushing) {
      applyRemote(remote.data);
      setBase(remote.data);
      setSync({ code, updatedAt: remote.updatedAt, dirty: false });
    }
  } catch { /* sin red */ }
}

/** Crea una lista compartida con lo que hay ahora en la cesta y me apunta con mi nombre. */
export async function createSync(name: string) {
  upsertMe(name);
  const data = payload();
  const r = await api.createList(data);
  setBase(data);
  setSync({ code: r.code, updatedAt: r.updatedAt, dirty: false });
}

export const validCode = (raw: string) => /^[A-Z2-9]{8}$/.test(raw.trim().toUpperCase());

/** Me uno a la lista de otro: me quedo con su cesta, junto las compras guardadas y me apunto. */
export async function joinSync(raw: string, name: string) {
  const code = raw.trim().toUpperCase();
  if (!validCode(code)) throw new Error('El código tiene 8 letras y números');
  if (code === state.sync.code) return;
  const remote = await api.getList(code).catch((e) => {
    throw new Error(e.status === 404 ? 'No existe ninguna lista con ese código' : 'No se pudo conectar');
  });
  const mine = new Set(remote.data.savedBaskets?.map((b) => b.id));
  applyRemote({
    ...remote.data,
    savedBaskets: [...(remote.data.savedBaskets ?? []), ...state.savedBaskets.filter((b) => !mine.has(b.id))].sort((a, b) => a.date.localeCompare(b.date)),
  });
  setBase(remote.data);
  setSync({ code, updatedAt: remote.updatedAt, dirty: false });
  upsertMe(name);
}

export function leaveSync() {
  setSync({ code: null, updatedAt: null, dirty: false });
  setBase(null);
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
  const addedBy = { ...state.addedBy };
  let n = 0;
  for (const { productId, packs } of items) {
    const p = state.idx.products.get(productId);
    if (!p) continue;
    if (!basket[productId] && state.meId && isShared()) addedBy[productId] = state.meId;
    basket[productId] = Math.max(basket[productId] ?? 0, packs);
    addedPrices[productId] ??= p.price;
    n++;
  }
  set({ basket, addedPrices, addedBy });
  persistList();
  return n;
}
