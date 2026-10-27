import type { Catalog, ListData, PriceHistory } from './types';

const BASE = import.meta.env.VITE_API_BASE ?? '';

async function json<T>(res: Response): Promise<T> {
  if (!res.ok) throw Object.assign(new Error(`HTTP ${res.status}`), { status: res.status, body: await res.json().catch(() => null) });
  return res.json() as Promise<T>;
}

export const api = {
  catalog: () => fetch(`${BASE}/api/catalog`, { cache: 'no-cache' }).then((r) => json<Catalog>(r)),
  catalogMeta: () => fetch(`${BASE}/api/catalog/meta`, { cache: 'no-store' }).then((r) => json<{ version: string; updatedAt: string }>(r)),
  history: (id: string) => fetch(`${BASE}/api/products/${encodeURIComponent(id)}/history`).then((r) => json<PriceHistory>(r)),
  createList: (data: ListData) =>
    fetch(`${BASE}/api/lists`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ data }) })
      .then((r) => json<{ code: string; updatedAt: string }>(r)),
  getList: (code: string) =>
    fetch(`${BASE}/api/lists/${code}`, { cache: 'no-store' }).then((r) => json<{ code: string; data: ListData; updatedAt: string }>(r)),
  putList: (code: string, data: ListData, baseUpdatedAt: string | null, force = false) =>
    fetch(`${BASE}/api/lists/${code}`, {
      method: 'PUT', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ data, baseUpdatedAt, force }),
    }),
};
