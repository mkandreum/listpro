// Peticiones a las tiendas online: reintentos con espera creciente ante 429 o fallos del servidor.
export const sleep = (ms) => new Promise((r) => setTimeout(r, ms));
export const UA = 'ListPro/2.0 (+lista de la compra personal)';

export async function getJson(url, attempt = 1) {
  try {
    const res = await fetch(url, {
      headers: { Accept: 'application/json', 'User-Agent': UA },
      signal: AbortSignal.timeout(20_000),
    });
    if (res.status === 429 || res.status >= 500) throw new Error(`HTTP ${res.status}`);
    if (!res.ok) throw Object.assign(new Error(`HTTP ${res.status} en ${url}`), { fatal: true });
    return await res.json();
  } catch (e) {
    if (e.fatal || attempt >= 4) throw e;
    await sleep(1000 * 2 ** attempt);
    return getJson(url, attempt + 1);
  }
}

export const num = (v) => (v === null || v === undefined || v === '' ? null : Number(v));
