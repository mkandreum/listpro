import { useSyncExternalStore } from 'react';
import { api } from './api';
import { uid } from './lib';
import { MEMBER_HUES, merge3, splitByWeights, splitEqual } from './share';
import type { Catalog, Category, Expense, ListData, MealPlan, Member, PlanPrefs, Product, SavedBasket, Section, Session, SplitMode, StoreInfo, Subcategory } from './types';

export type ThemePref = 'system' | 'light' | 'dark';

interface Indexes {
  products: Map<string, Product>;
  sections: Map<string, Section>;
  categories: Map<number, Category & { section: string }>;
  subcategories: Map<number, Subcategory & { category: number; section: string }>;
  aisleOrder: Map<number, number>; // subcategoría → orden de tienda
}

/** Súper con catálogo. La lista real llega de /api/stores; esta es la de partida (y la de sin red). */
export const DEFAULT_STORES: StoreInfo[] = [
  { id: 'mercadona', name: 'Mercadona', hue: '#00A650', count: 0, updatedAt: null },
  { id: 'consum', name: 'Consum', hue: '#F28C00', count: 0, updatedAt: null },
  { id: 'eci', name: 'El Corte Inglés', hue: '#7C3AED', count: 0, updatedAt: null },
];
/** El súper de un producto por su id: "c:" Consum, "e:" El Corte Inglés; Mercadona no lleva prefijo. */
export const storeOf = (productId: string): string => (
  productId.startsWith('e:') ? 'eci' : productId.startsWith('c:') ? 'consum' : 'mercadona'
);

export interface State {
  /** Súper que se está viendo: su catálogo es `catalog`. */
  store: string;
  stores: StoreInfo[];
  /** Catálogo del súper activo. */
  catalog: Catalog | null;
  /** Todos los catálogos descargados (el activo y los de otros súper que hay en la cesta). */
  catalogs: Record<string, Catalog>;
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
  /** Sesión de la cuenta (null = sin cuenta). */
  auth: Session | null;
  /** Ya vio la presentación de la app. */
  onboarded: boolean;
  /** Eligió seguir sin cuenta. */
  guest: boolean;
}

function load<T>(key: string, fallback: T): T {
  try { const v = localStorage.getItem(key); return v ? (JSON.parse(v) as T) : fallback; } catch { return fallback; }
}
function save(key: string, value: unknown) {
  try { localStorage.setItem(key, JSON.stringify(value)); } catch { /* modo privado o almacenamiento lleno */ }
}

const emptyIdx = (): Indexes => ({ products: new Map(), sections: new Map(), categories: new Map(), subcategories: new Map(), aisleOrder: new Map() });

const savedAuth = load<Session | null>('listproAuth', null);
let state: State = {
  store: load<string | null>('listproStore', null) ?? savedAuth?.user.store ?? 'mercadona',
  stores: load<StoreInfo[]>('listproStores', DEFAULT_STORES),
  catalog: null,
  catalogs: {},
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
  auth: savedAuth,
  onboarded: load('listproOnboarded', false),
  guest: load('listproGuest', false),
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
/**
 * Índices sobre todos los catálogos descargados: así la cesta encuentra los productos de cualquier
 * súper. Los pasillos salen del activo; categorías y subcategorías no chocan porque sus ids son de cada súper.
 */
function buildIndexes(active: string, catalogs: Record<string, Catalog>): Indexes {
  const idx = emptyIdx();
  const ordered = [catalogs[active], ...Object.entries(catalogs).filter(([s]) => s !== active).map(([, c]) => c)].filter(Boolean);
  for (const c of ordered) {
    let order = 0;
    for (const s of c.sections) {
      if (!idx.sections.has(s.id)) idx.sections.set(s.id, s);
      for (const cat of s.categories) {
        idx.categories.set(cat.id, { ...cat, section: s.id });
        for (const sub of cat.subcategories) {
          idx.subcategories.set(sub.id, { ...sub, category: cat.id, section: s.id });
          idx.aisleOrder.set(sub.id, order++);
        }
      }
    }
    for (const p of c.products) idx.products.set(p.id, p.store ? p : { ...p, store: storeOf(p.id) });
  }
  return idx;
}

function putCatalog(store: string, c: Catalog) {
  const catalogs = { ...state.catalogs, [store]: c };
  set({ catalogs, catalog: catalogs[state.store] ?? null, idx: buildIndexes(state.store, catalogs) });
}

const CATALOG_CACHE = 'listpro-data-v1';
// Mercadona conserva la clave de siempre para aprovechar lo que ya hay guardado
const catalogPath = (store: string) => (store === 'mercadona' ? '/api/catalog' : `/api/catalog?store=${encodeURIComponent(store)}`);

/** Pinta al instante el último catálogo guardado; la red lo refresca después. */
async function loadCachedCatalog(store: string) {
  try {
    const hit = await (await caches.open(CATALOG_CACHE)).match(catalogPath(store));
    if (hit && !state.catalogs[store]) {
      const c = (await hit.json()) as Catalog;
      if (!state.catalogs[store]) putCatalog(store, c);
    }
  } catch { /* sin Cache API (http sin TLS, modo privado) */ }
}

/** Descarga (o refresca) el catálogo de un súper. Devuelve true si había precios nuevos. */
export async function loadCatalog(store: string = state.store): Promise<boolean> {
  if (!state.catalogs[store]) await loadCachedCatalog(store);
  try {
    const res = await fetch(catalogPath(store), { cache: 'no-cache' });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const copy = res.clone();
    caches.open(CATALOG_CACHE).then((cache) => cache.put(catalogPath(store), copy)).catch(() => {});
    const c = (await res.json()) as Catalog;
    const before = state.catalogs[store];
    if (store === state.store) set({ catalogError: false });
    if (before?.version === c.version && before.updatedAt === c.updatedAt) return false;
    putCatalog(store, c);
    return before != null && before.version !== c.version;
  } catch {
    if (store === state.store && !state.catalogs[store]) set({ catalogError: true });
    return false;
  }
}

/** Los súper que hay en la cesta (además del activo) también necesitan su catálogo. */
export function loadBasketCatalogs() {
  const needed = new Set(Object.keys(state.basket).map(storeOf));
  for (const s of needed) if (!state.catalogs[s]) loadCatalog(s).catch(() => {});
}

export async function loadStores() {
  try {
    const stores = await api.stores();
    if (stores.length) { set({ stores }); save('listproStores', stores); }
  } catch { /* sin red: la lista guardada */ }
}

/** Cambia el súper que se ve. Si hay cuenta, lo recuerda para los otros dispositivos. */
export async function setStore(store: string, remember = true) {
  if (store === state.store) return;
  set({ store, catalog: state.catalogs[store] ?? null, idx: buildIndexes(store, state.catalogs), catalogError: false });
  save('listproStore', store);
  const a = state.auth;
  if (remember && a && a.user.store !== store) api.updateMe(a.token, { store }).then((user) => setAuth({ ...a, user })).catch(() => {});
  await loadCatalog(store);
}

export const storeInfo = (id: string, s: State = state): StoreInfo =>
  s.stores.find((x) => x.id === id) ?? DEFAULT_STORES.find((x) => x.id === id) ?? { id, name: id, hue: '#16A34A', count: 0, updatedAt: null };

export async function checkCatalogUpdate() {
  if (!state.catalog) return loadCatalog();
  for (const store of Object.keys(state.catalogs)) {
    try {
      const meta = await api.catalogMeta(store);
      if (meta.version !== state.catalogs[store]?.version && (await loadCatalog(store)) && store === state.store) toast('Precios actualizados');
    } catch { /* sin red: seguimos con lo que hay */ }
  }
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
  if (next > 0 && !state.catalogs[storeOf(id)]) loadCatalog(storeOf(id)).catch(() => {});
}

/** Cambia un producto de la cesta por su equivalente en otro súper (con los envases que hagan falta para la misma cantidad). */
export function moveToStore(fromId: string, toId: string, price?: number, packs?: number) {
  const qty = state.basket[fromId];
  if (!qty || fromId === toId) return;
  const basket = without(state.basket, [fromId]);
  basket[toId] = (basket[toId] ?? 0) + (packs ?? qty);
  const addedPrices = without(state.addedPrices, [fromId]);
  addedPrices[toId] ??= state.idx.products.get(toId)?.price ?? price ?? 0;
  const who = state.addedBy[fromId];
  set({
    basket, addedPrices,
    inCart: state.inCart.filter((x) => x !== fromId),
    addedBy: who ? { ...without(state.addedBy, [fromId]), [toId]: who } : state.addedBy,
    pickedBy: without(state.pickedBy, [fromId]),
  });
  persistList();
  if (!state.catalogs[storeOf(toId)]) loadCatalog(storeOf(toId)).catch(() => {});
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

/** Productos de la cesta que su súper ya no vende (solo de los súper cuyo catálogo tenemos). */
export const missingIds = (s: State = state) => Object.keys(s.basket).filter((id) => s.catalogs[storeOf(id)] && !s.idx.products.has(id));

export function dropMissing() {
  const basket = { ...state.basket };
  for (const id of missingIds()) delete basket[id];
  set({ basket });
  persistList();
}

/** Líneas de la cesta, en el orden de la tienda. Con `store`, solo las de ese súper. */
export function basketLines(store?: string) {
  const { basket, idx } = state;
  return Object.entries(basket)
    .filter(([id]) => !store || storeOf(id) === store)
    .flatMap(([id, qty]) => { const p = idx.products.get(id); return p ? [{ p, qty }] : []; })
    .sort((a, b) => (idx.aisleOrder.get(a.p.subcategory) ?? 1e9) - (idx.aisleOrder.get(b.p.subcategory) ?? 1e9) || a.p.name.localeCompare(b.p.name));
}

/** Lo que entra en la compra: si has marcado algo, solo lo marcado (lo demás se queda en la cesta); si no, todo. */
export function checkoutLines(store?: string) {
  const lines = basketLines(store);
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
  /** Con varios súper en la cesta, se termina la compra de uno. */
  store?: string;
}

/** Cuánto le toca a cada uno de una compra. Con «cada uno lo suyo», lo que pidió cada uno; lo que no es de nadie, a partes iguales. */
export function purchaseShares(c: Required<Pick<Checkout, 'amount' | 'mode' | 'among'>> & { store?: string }) {
  if (!c.among.length) return {};
  if (c.mode === 'equal') return splitEqual(c.amount, c.among);
  const w: Record<string, number> = Object.fromEntries(c.among.map((m) => [m, 0]));
  for (const { p, qty } of checkoutLines(c.store)) {
    const cost = p.price * qty;
    const who = state.addedBy[p.id];
    if (who && who in w) w[who] += cost;
    else for (const m of c.among) w[m] += cost / c.among.length;
  }
  return splitByWeights(c.amount, w);
}

/** Guarda la compra en el historial, la quita de la cesta y, si la lista es compartida, la apunta en las cuentas. */
export function finishShop(c: Checkout) {
  const lines = checkoutLines(c.store);
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
    const shares = purchaseShares({ amount: c.amount, mode: c.mode ?? 'equal', among: c.among, store: c.store });
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
  // Lo que otro haya añadido de otro súper también necesita su catálogo
  loadBasketCatalogs();
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

/** Al salir (otra app, cerrar la pestaña, bloquear el móvil): sube ya lo pendiente en vez de esperar. */
export function flushSync() {
  if (!state.sync.code || !state.sync.dirty) return;
  clearTimeout(pushTimer);
  pushSync();
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
  linkAccountList(r.code);
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
  linkAccountList(code);
}

export function leaveSync() {
  setSync({ code: null, updatedAt: null, dirty: false });
  setBase(null);
  // Con cuenta, siempre tienes una lista: al salir de la compartida, empiezas una tuya con lo que hay aquí
  if (state.auth) createSync(state.auth.user.name).catch(() => {});
}

// --- Planes de recetas ---
/** `avoid`: nombres de recetas que no queremos que repita (las del plan que se está cambiando). */
export async function generatePlan(prefs: PlanPrefs, avoid: string[] = []) {
  set({ planPrefs: prefs });
  save('listproPlanPrefs', prefs);
  return setPlan(await api.createPlan(prefs, avoid));
}

/** Cambia una receta del plan por otra. El plan nuevo lleva otro id para que se sincronice. */
export async function swapRecipe(index: number) {
  if (!state.plan) throw new Error('No hay plan');
  return setPlan(await api.swapRecipe(state.plan.id, index));
}

function setPlan(plan: MealPlan) {
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

/** Mete en la cesta los envases que hacen falta para el plan (o solo para una receta). */
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

// --- Cuenta ---
function setAuth(auth: Session | null) {
  set({ auth });
  save('listproAuth', auth);
}

/** La cuenta recuerda en qué lista está, para encontrarla al entrar desde otro móvil. */
function linkAccountList(code: string) {
  const a = state.auth;
  if (!a || a.user.listCode === code) return;
  api.updateMe(a.token, { listCode: code })
    .then((user) => setAuth({ ...a, user }))
    .catch(() => {});
}

/** Tras entrar o registrarse: yo soy la cuenta, y la cuenta tiene su lista (y su súper). */
async function adoptSession(s: Session) {
  setAuth(s);
  if (s.user.store && s.user.store !== state.store) setStore(s.user.store, false).catch(() => {});
  setMe(s.user.id);
  set({ guest: false });
  save('listproGuest', false);
  const code = s.user.listCode;
  if (code && code !== state.sync.code) {
    try { await joinSync(code, s.user.name); return; } catch { /* la lista ya no existe: abajo se crea otra */ }
  }
  if (state.sync.code) { upsertMe(s.user.name); linkAccountList(state.sync.code); return; }
  await createSync(s.user.name);
}

export async function register(name: string, email: string, password: string, store?: string, invite?: string) {
  const s = await api.register({ name, email, password, memberId: state.meId, listCode: state.sync.code, store: store ?? state.store, invite });
  await adoptSession(s);
}

export async function login(email: string, password: string) {
  await adoptSession(await api.login(email, password));
}

/** Cierra la sesión en este dispositivo. La lista y los datos se quedan aquí, pero ya sin sincronizar. */
export function logout() {
  setAuth(null);
  setSync({ code: null, updatedAt: null, dirty: false });
  setBase(null);
  set({ guest: false });
  save('listproGuest', false);
}

export async function logoutEverywhere() {
  const a = state.auth;
  if (!a) return;
  setAuth(await api.logoutAll(a.token));
}

export async function changePassword(current: string, next: string) {
  const a = state.auth;
  if (!a) return;
  setAuth(await api.changePassword(a.token, current, next));
}

export async function renameAccount(name: string) {
  const a = state.auth;
  if (!a) return;
  const user = await api.updateMe(a.token, { name });
  setAuth({ ...a, user });
  upsertMe(user.name);
}

export async function deleteAccount(password: string) {
  const a = state.auth;
  if (!a) return;
  await api.deleteAccount(a.token, password);
  logout();
}

/** Comprueba la sesión al abrir: si la revocaron (cambio de contraseña, "cerrar en todos"), sale. */
export async function refreshAccount() {
  const a = state.auth;
  if (!a) return;
  try {
    const user = await api.me(a.token);
    setAuth({ ...a, user });
    // La cuenta manda: si en otro dispositivo se cambió de lista o de súper, este se pone igual
    const code = state.sync.code;
    if (user.listCode && user.listCode !== code) await joinSync(user.listCode, user.name).catch(() => {});
    else if (!user.listCode && !code) await createSync(user.name).catch(() => {});
    else if (!user.listCode && code) linkAccountList(code);
    if (user.store && user.store !== state.store) await setStore(user.store, false).catch(() => {});
  } catch (e) {
    if ((e as { status?: number }).status === 401) { logout(); toast('Tu sesión ha caducado. Vuelve a entrar.'); }
  }
}

export function finishOnboarding() {
  set({ onboarded: true });
  save('listproOnboarded', true);
}

export function continueAsGuest() {
  set({ guest: true });
  save('listproGuest', true);
}

export function wantAccount() {
  set({ guest: false });
  save('listproGuest', false);
}
