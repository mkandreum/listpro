import { db, getMeta, setMeta, transaction } from './db.js';

// API pública que usa la propia tienda online (tienda.mercadona.es). El almacén (wh) fija los
// precios de la zona: mad1 = Madrid, bcn1 = Barcelona, vlc1 = Valencia, svq1 = Sevilla...
const BASE = 'https://tienda.mercadona.es/api';
const WAREHOUSE = process.env.MERCADONA_WH ?? 'mad1';
const DELAY_MS = Number(process.env.SCRAPE_DELAY_MS ?? 350);

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

async function getJson(pathname, attempt = 1) {
  const url = `${BASE}${pathname}?lang=es&wh=${WAREHOUSE}`;
  try {
    const res = await fetch(url, {
      headers: { Accept: 'application/json', 'User-Agent': 'ListPro/2.0 (+lista de la compra personal)' },
      signal: AbortSignal.timeout(20_000),
    });
    if (res.status === 429 || res.status >= 500) throw new Error(`HTTP ${res.status}`);
    if (!res.ok) throw Object.assign(new Error(`HTTP ${res.status} en ${url}`), { fatal: true });
    return await res.json();
  } catch (e) {
    if (e.fatal || attempt >= 4) throw e;
    await sleep(1000 * 2 ** attempt);
    return getJson(pathname, attempt + 1);
  }
}

const num = (v) => (v === null || v === undefined || v === '' ? null : Number(v));

function fmtNumber(n) {
  return Number.isInteger(n) ? String(n) : String(Math.round(n * 1000) / 1000).replace('.', ',');
}

function fmtQty(qty, unit) {
  if (qty == null || !unit) return '';
  if (unit === 'kg' && qty < 1) { qty *= 1000; unit = 'g'; }
  if (unit === 'l' && qty < 1) { qty *= 1000; unit = 'ml'; }
  if (unit === 'l') unit = 'L';
  return `${fmtNumber(qty)} ${unit}`;
}

/** "Garrafa 5 L", "6 ud. x 125 g", "Pieza 1,2 kg aprox." — lo mismo que enseña la tienda. */
export function formatSize(p) {
  const pi = p.price_instructions ?? {};
  let size = fmtQty(num(pi.unit_size), pi.size_format);
  if (pi.is_pack && pi.total_units) {
    const per = fmtQty(num(pi.pack_size), pi.size_format);
    size = `${pi.total_units} ${pi.unit_name ?? 'ud.'}${per ? ` x ${per}` : ''}`;
  }
  if (pi.approx_size && size) size += ' aprox.';
  return [p.packaging, size].filter(Boolean).join(' ');
}

function toRow(p, categoryId, subcategoryId, group, position) {
  const pi = p.price_instructions ?? {};
  return {
    id: String(p.id),
    name: p.display_name.trim(),
    size: formatSize(p),
    packaging: p.packaging ?? null,
    category_id: categoryId,
    subcategory_id: subcategoryId,
    grp: group,
    position,
    price: num(pi.unit_price),
    bulk_price: num(pi.bulk_price),
    ref_price: num(pi.reference_price),
    ref_format: pi.reference_format ?? null,
    previous_price: num(pi.previous_unit_price),
    image: p.thumbnail ?? null,
    share_url: p.share_url ?? null,
    is_new: pi.is_new || p.is_new_arrival ? 1 : 0,
  };
}

let running = null;

/** Descarga el catálogo completo. Si ya hay una pasada en marcha, devuelve esa misma promesa. */
export function scrapeCatalog(log = console.log) {
  running ??= doScrape(log).finally(() => { running = null; });
  return running;
}

export const isScraping = () => running !== null;

async function doScrape(log) {
  const started = Date.now();
  log(`[scraper] Empezando (almacén ${WAREHOUSE})`);
  const root = await getJson('/categories/');

  const categories = [];
  const products = new Map();
  let pos = 0;
  for (const [ci, cat] of root.results.entries()) {
    categories.push({ id: cat.id, parent_id: null, name: cat.name, position: ci });
    for (const [si, sub] of cat.categories.entries()) {
      categories.push({ id: sub.id, parent_id: cat.id, name: sub.name, position: si });
      await sleep(DELAY_MS);
      const detail = await getJson(`/categories/${sub.id}/`);
      for (const group of detail.categories ?? []) {
        for (const p of group.products ?? []) {
          if (!p.published || products.has(String(p.id))) continue;
          const row = toRow(p, cat.id, sub.id, group.name, pos++);
          if (row.price != null) products.set(row.id, row);
        }
      }
    }
    log(`[scraper] ${cat.name}: ${products.size} productos acumulados`);
  }

  // Si la tienda devuelve algo anómalo (caída parcial), no machacamos el catálogo bueno.
  const previous = db.prepare('SELECT COUNT(*) AS n FROM products WHERE active = 1').get().n;
  if (products.size < 500 || (previous > 0 && products.size < previous * 0.6)) {
    throw new Error(`Catálogo sospechoso: ${products.size} productos (antes ${previous}). No se guarda.`);
  }

  const now = new Date().toISOString();
  const stats = transaction(() => saveCatalog(categories, [...products.values()], now));
  const version = String(Date.now());
  setMeta('catalog_version', version);
  setMeta('catalog_updated_at', now);
  setMeta('warehouse', WAREHOUSE);
  const secs = Math.round((Date.now() - started) / 1000);
  log(`[scraper] Listo en ${secs}s: ${products.size} productos, ${stats.added} nuevos, ${stats.priceChanges} cambios de precio, ${stats.removed} retirados`);
  return { ...stats, total: products.size, version, updatedAt: now };
}

function saveCatalog(categories, rows, now) {
  db.exec('DELETE FROM categories');
  const insCat = db.prepare('INSERT INTO categories (id, parent_id, name, position) VALUES (?, ?, ?, ?)');
  for (const c of categories) insCat.run(c.id, c.parent_id, c.name, c.position);

  const existing = new Map(db.prepare('SELECT id, price FROM products').all().map((r) => [r.id, r.price]));
  const upsert = db.prepare(`
    INSERT INTO products (id, name, size, packaging, category_id, subcategory_id, grp, position, price, bulk_price,
      ref_price, ref_format, previous_price, image, share_url, is_new, active, first_seen, last_seen)
    VALUES (:id, :name, :size, :packaging, :category_id, :subcategory_id, :grp, :position, :price, :bulk_price,
      :ref_price, :ref_format, :previous_price, :image, :share_url, :is_new, 1, :now, :now)
    ON CONFLICT(id) DO UPDATE SET
      name = excluded.name, size = excluded.size, packaging = excluded.packaging, category_id = excluded.category_id,
      subcategory_id = excluded.subcategory_id, grp = excluded.grp, position = excluded.position,
      price = excluded.price, bulk_price = excluded.bulk_price, ref_price = excluded.ref_price,
      ref_format = excluded.ref_format, previous_price = excluded.previous_price, image = excluded.image,
      share_url = excluded.share_url, is_new = excluded.is_new, active = 1, last_seen = excluded.last_seen
  `);
  const markChange = db.prepare('UPDATE products SET last_price = ?, price_changed_at = ? WHERE id = ?');
  const history = db.prepare('INSERT OR IGNORE INTO price_history (product_id, price, at) VALUES (?, ?, ?)');

  let added = 0;
  let priceChanges = 0;
  for (const r of rows) {
    const before = existing.get(r.id);
    upsert.run({ ...r, now });
    if (before === undefined) {
      added++;
      history.run(r.id, r.price, now);
    } else if (Math.abs(before - r.price) >= 0.005) {
      priceChanges++;
      markChange.run(before, now, r.id);
      history.run(r.id, r.price, now);
    }
  }
  const removed = db.prepare('UPDATE products SET active = 0 WHERE last_seen <> ? AND active = 1').run(now).changes;
  return { added, priceChanges, removed };
}

/** Programa una pasada diaria a la hora indicada (hora de Madrid) y otra al arrancar si el catálogo está viejo. */
export function scheduleScraping({ hour = Number(process.env.SCRAPE_HOUR ?? 6), maxAgeHours = 20 } = {}) {
  const run = () => scrapeCatalog().catch((e) => console.error('[scraper] Error:', e.message));

  const last = getMeta('catalog_updated_at');
  if (!last || Date.now() - Date.parse(last) > maxAgeHours * 3600_000) setTimeout(run, 2000);

  const scheduleNext = () => {
    const now = new Date();
    const madridNow = new Date(now.toLocaleString('en-US', { timeZone: 'Europe/Madrid' }));
    const offset = now.getTime() - madridNow.getTime();
    const next = new Date(madridNow);
    next.setHours(hour, Math.floor(Math.random() * 30), 0, 0);
    if (next <= madridNow) next.setDate(next.getDate() + 1);
    const wait = next.getTime() + offset - now.getTime();
    console.log(`[scraper] Próxima actualización: ${new Date(now.getTime() + wait).toISOString()}`);
    setTimeout(async () => { await run(); scheduleNext(); }, wait);
  };
  scheduleNext();
}
