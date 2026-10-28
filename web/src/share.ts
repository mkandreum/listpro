import type { Expense, ListData, Member } from './types';

export const MEMBER_HUES = ['#22C55E', '#7C5CFF', '#FF8A2A', '#FF5FA8', '#3BA7FF', '#E5B800', '#14B8A6', '#E5484D'];

export const initials = (name: string) =>
  name.trim().split(/\s+/).slice(0, 2).map((w) => w[0]?.toUpperCase() ?? '').join('') || '?';

const cents = (v: number) => Math.round(v * 100);

/** Reparte `total` según pesos, en céntimos exactos: lo que sobra del redondeo va a los restos más grandes. */
export function splitByWeights(total: number, weights: Record<string, number>): Record<string, number> {
  const ids = Object.keys(weights).filter((k) => weights[k] > 0);
  const sum = ids.reduce((a, k) => a + weights[k], 0);
  if (!ids.length || sum <= 0) return {};
  const t = cents(total);
  const raw = ids.map((k) => ({ k, v: (t * weights[k]) / sum }));
  const out = raw.map(({ k, v }) => ({ k, c: Math.floor(v), r: v - Math.floor(v) }));
  let left = t - out.reduce((a, x) => a + x.c, 0);
  for (const x of [...out].sort((a, b) => b.r - a.r)) { if (left <= 0) break; x.c++; left--; }
  return Object.fromEntries(out.map((x) => [x.k, x.c / 100]));
}

export const splitEqual = (total: number, among: string[]) => splitByWeights(total, Object.fromEntries(among.map((m) => [m, 1])));

/** Saldo de cada miembro: positivo = le deben; negativo = debe. */
export function balances(expenses: Expense[], members: Member[]): Map<string, number> {
  const b = new Map<string, number>(members.map((m) => [m.id, 0]));
  for (const e of expenses) {
    b.set(e.paidBy, (b.get(e.paidBy) ?? 0) + cents(e.amount));
    for (const [m, v] of Object.entries(e.shares)) b.set(m, (b.get(m) ?? 0) - cents(v));
  }
  return new Map([...b].map(([k, v]) => [k, v / 100]));
}

/** Los pagos mínimos para quedar en paz: el que más debe paga al que más le deben, y así. */
export function settlements(bal: Map<string, number>): { from: string; to: string; amount: number }[] {
  const debt = [...bal].filter(([, v]) => v < -0.004).map(([k, v]) => ({ k, c: -cents(v) }));
  const cred = [...bal].filter(([, v]) => v > 0.004).map(([k, v]) => ({ k, c: cents(v) }));
  const out: { from: string; to: string; amount: number }[] = [];
  while (debt.length && cred.length) {
    debt.sort((a, b) => b.c - a.c);
    cred.sort((a, b) => b.c - a.c);
    const d = debt[0], c = cred[0];
    const x = Math.min(d.c, c.c);
    out.push({ from: d.k, to: c.k, amount: x / 100 });
    d.c -= x; c.c -= x;
    if (!d.c) debt.shift();
    if (!c.c) cred.shift();
  }
  return out;
}

// ───────── Fusión a tres bandas ─────────
// Cuando dos personas tocan la lista a la vez, se compara cada una con la última versión común (base):
// lo que cambió aquí se queda; lo que no, se toma del servidor. Así nadie pisa lo que añadió el otro.

const same = (a: unknown, b: unknown) => JSON.stringify(a) === JSON.stringify(b);

function mergeMap<T>(base: Record<string, T> = {}, local: Record<string, T> = {}, remote: Record<string, T> = {}) {
  const out: Record<string, T> = {};
  for (const k of new Set([...Object.keys(local), ...Object.keys(remote), ...Object.keys(base)])) {
    const v = !same(local[k], base[k]) ? local[k] : remote[k];
    if (v !== undefined) out[k] = v;
  }
  return out;
}

function mergeSet(base: string[] = [], local: string[] = [], remote: string[] = []) {
  const b = new Set(base), l = new Set(local), r = new Set(remote);
  return [...new Set([...local, ...remote, ...base])].filter((k) => (l.has(k) !== b.has(k) ? l.has(k) : r.has(k)));
}

function mergeById<T extends { id: string }>(base: T[] = [], local: T[] = [], remote: T[] = []) {
  const byId = (xs: T[]) => Object.fromEntries(xs.map((x) => [x.id, x]));
  const merged = mergeMap(byId(base), byId(local), byId(remote));
  // Conserva el orden: primero el local, luego lo nuevo del servidor
  const order = [...new Set([...local.map((x) => x.id), ...remote.map((x) => x.id)])];
  return order.filter((id) => merged[id]).map((id) => merged[id]);
}

export function merge3(base: ListData | null, local: ListData, remote: ListData): ListData {
  const b: Partial<ListData> = base ?? {};
  const pick = <K extends keyof ListData>(k: K) => (!same(local[k], b[k]) ? local[k] : remote[k]);
  return {
    basket: mergeMap(b.basket, local.basket, remote.basket),
    addedPrices: mergeMap(b.addedPrices, local.addedPrices, remote.addedPrices),
    inCart: mergeSet(b.inCart, local.inCart, remote.inCart),
    savedBaskets: mergeById(b.savedBaskets, local.savedBaskets, remote.savedBaskets).sort((x, y) => x.date.localeCompare(y.date)),
    planId: pick('planId'),
    planPrefs: pick('planPrefs'),
    members: mergeById(b.members, local.members, remote.members),
    addedBy: mergeMap(b.addedBy, local.addedBy, remote.addedBy),
    pickedBy: mergeMap(b.pickedBy, local.pickedBy, remote.pickedBy),
    expenses: mergeById(b.expenses, local.expenses, remote.expenses).sort((x, y) => x.at.localeCompare(y.at)),
  };
}
