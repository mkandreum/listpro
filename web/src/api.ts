import type { InvitePreview, Alternatives, BasketCost, Catalog, Cheaper, Comparison, ListData, MealPlan, PlanPrefs, PlannerOptions, PriceHistory, Session, StoreInfo, User } from './types';

const BASE = import.meta.env.VITE_API_BASE ?? '';

async function json<T>(res: Response): Promise<T> {
  if (!res.ok) throw Object.assign(new Error(`HTTP ${res.status}`), { status: res.status, body: await res.json().catch(() => null) });
  return res.json() as Promise<T>;
}

const send = (method: string, path: string, body?: unknown, token?: string | null) =>
  fetch(`${BASE}${path}`, {
    method,
    headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) },
    body: body === undefined ? undefined : JSON.stringify(body),
  });

/** Igual que json(), pero el error lleva el mensaje del servidor listo para enseñarlo. */
async function friendly<T>(res: Response): Promise<T> {
  if (res.ok) return (res.status === 204 ? undefined : await res.json()) as T;
  const body = await res.json().catch(() => null);
  throw Object.assign(new Error(body?.error ?? 'No se pudo conectar. Prueba otra vez.'), { status: res.status });
}

export const api = {
  register: (b: { name: string; email: string; password: string; memberId?: string | null; listCode?: string | null; store?: string; invite?: string; joinCode?: string | null }) =>
    send('POST', '/api/auth/register', b).then((r) => friendly<Session>(r)),
  login: (email: string, password: string) => send('POST', '/api/auth/login', { email, password }).then((r) => friendly<Session>(r)),
  forgot: (email: string) => send('POST', '/api/auth/forgot', { email }).then((r) => json<{ ok: boolean; message: string }>(r)),
  resetPassword: (token: string, password: string) => send('POST', '/api/auth/reset', { token, password }).then((r) => friendly<{ ok: boolean }>(r)),
  me: (token: string) => send('GET', '/api/me', undefined, token).then((r) => friendly<User>(r)),
  updateMe: (token: string, patch: { name?: string; listCode?: string | null; store?: string }) => send('PUT', '/api/me', patch, token).then((r) => friendly<User>(r)),
  changePassword: (token: string, current: string, next: string) =>
    send('PUT', '/api/me/password', { current, next }, token).then((r) => friendly<Session>(r)),
  logoutAll: (token: string) => send('POST', '/api/auth/logout-all', {}, token).then((r) => friendly<Session>(r)),
  deleteAccount: (token: string, password: string) => send('DELETE', '/api/me', { password }, token).then((r) => friendly<void>(r)),
  catalog: () => fetch(`${BASE}/api/catalog`, { cache: 'no-cache' }).then((r) => json<Catalog>(r)),
  catalogMeta: (store = 'mercadona') => fetch(`${BASE}/api/catalog/meta?store=${encodeURIComponent(store)}`, { cache: 'no-store' }).then((r) => json<{ version: string; updatedAt: string }>(r)),
  stores: () => fetch(`${BASE}/api/stores`, { cache: 'no-cache' }).then((r) => json<StoreInfo[]>(r)),
  compare: (id: string) => fetch(`${BASE}/api/products/${encodeURIComponent(id)}/compare`).then((r) => json<Comparison>(r)),
  cheaper: (ids: string[]) => send('POST', '/api/compare', { ids }).then((r) => json<Cheaper>(r)),
  alternatives: (items: { id: string; qty: number }[]) => send('POST', '/api/basket/alternatives', { items }).then((r) => json<Alternatives>(r)),
  basketCost: (items: { id: string; qty: number }[]) => send('POST', '/api/basket/compare', { items }).then((r) => json<BasketCost>(r)),
  history: (id: string) => fetch(`${BASE}/api/products/${encodeURIComponent(id)}/history`).then((r) => json<PriceHistory>(r)),
  plannerOptions: () => fetch(`${BASE}/api/planner/options`).then((r) => json<PlannerOptions>(r)),
  createPlan: (prefs: PlanPrefs, avoid: string[] = []) => send('POST', '/api/plans', { prefs, avoid }).then((r) => friendly<MealPlan>(r)),
  swapRecipe: (id: string, index: number) => send('POST', `/api/plans/${encodeURIComponent(id)}/swap`, { index }).then((r) => friendly<MealPlan>(r)),
  getPlan: (id: string) => fetch(`${BASE}/api/plans/${encodeURIComponent(id)}`).then((r) => json<MealPlan>(r)),
  createList: (data: ListData) =>
    fetch(`${BASE}/api/lists`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ data }) })
      .then((r) => json<{ code: string; updatedAt: string }>(r)),
  invite: (code: string) => fetch(`${BASE}/api/lists/${code}/invite`, { cache: 'no-store' }).then((r) => json<InvitePreview>(r)),
  getList: (code: string) =>
    fetch(`${BASE}/api/lists/${code}`, { cache: 'no-store' }).then((r) => json<{ code: string; data: ListData; updatedAt: string }>(r)),
  putList: (code: string, data: ListData, baseUpdatedAt: string | null, force = false) => {
    const body = JSON.stringify({ data, baseUpdatedAt, force });
    // keepalive: si se está cerrando la pestaña, el navegador termina de enviarlo igual (solo hasta 64 KB)
    return fetch(`${BASE}/api/lists/${code}`, {
      method: 'PUT', headers: { 'Content-Type': 'application/json' }, body,
      keepalive: document.visibilityState === 'hidden' && body.length < 60_000,
    });
  },
};
