import { db, getMeta, setMeta, transaction } from './db.js';
import { DEFAULT_STORE, metaKey, STORES } from './stores.js';
import { reloadSections } from './taxonomy.js';
import * as mercadona from './scrapers/mercadona.js';
import * as consum from './scrapers/consum.js';
import * as eci from './scrapers/eci.js';

// Un lector por súper: fetchCatalog() devuelve categorías y productos ya en el formato de la base de
// datos; enrich() (opcional) completa datos que el listado no trae, como los códigos de barras.
const READERS = { mercadona, consum, eci };
// Por debajo de esto, una pasada se considera rota y no machaca el catálogo bueno
const MIN_PRODUCTS = { mercadona: 500, consum: 1000, eci: 1000 };

const running = new Map();

/** Descarga el catálogo de un súper. Si ya hay una pasada en marcha, devuelve esa misma promesa. */
export function scrapeCatalog(store = DEFAULT_STORE, log = console.log) {
  if (!running.has(store)) running.set(store, doScrape(store, log).finally(() => running.delete(store)));
  return running.get(store);
}

/** Todos los súper, uno detrás de otro (para no cargar la red ni la memoria a la vez). */
export async function scrapeAll(log = console.log) {
  const results = {};
  for (const s of STORES) {
    try {
      results[s.id] = await scrapeCatalog(s.id, log);
    } catch (e) {
      log(`[scraper] ${s.name}: ${e.message}`);
      results[s.id] = { error: e.message };
    }
  }
  return results;
}

export const isScraping = (store) => (store ? running.has(store) : running.size > 0);

async function doScrape(store, log) {
  const started = Date.now();
  const reader = READERS[store];
  const { categories, products, meta } = await reader.fetchCatalog(log);

  // Si la tienda devuelve algo anómalo (caída parcial), no machacamos el catálogo bueno.
  const previous = db.prepare('SELECT COUNT(*) AS n FROM products WHERE active = 1 AND store = ?').get(store).n;
  if (products.length < MIN_PRODUCTS[store] || (previous > 0 && products.length < previous * 0.6)) {
    throw new Error(`Catálogo sospechoso de ${store}: ${products.length} productos (antes ${previous}). No se guarda.`);
  }

  const now = new Date().toISOString();
  const stats = transaction(() => saveCatalog(store, categories, products, now));
  reloadSections();
  const version = String(Date.now());
  setMeta(metaKey(store, 'catalog_version'), version);
  setMeta(metaKey(store, 'catalog_updated_at'), now);
  if (meta?.warehouse) setMeta('warehouse', meta.warehouse);
  const secs = Math.round((Date.now() - started) / 1000);
  log(`[scraper] ${store} listo en ${secs}s: ${products.length} productos, ${stats.added} nuevos, ${stats.priceChanges} cambios de precio, ${stats.removed} retirados`);

  // Lo que va después del catálogo no lo retrasa ni lo invalida
  if (reader.enrich) {
    reader.enrich(log).then(() => setMeta(metaKey(store, 'enriched_at'), new Date().toISOString())).catch((e) => log(`[scraper] ${store} enrich: ${e.message}`));
  }
  return { ...stats, total: products.length, version, updatedAt: now };
}

function saveCatalog(store, categories, rows, now) {
  db.prepare('DELETE FROM categories WHERE store = ?').run(store);
  const insCat = db.prepare('INSERT OR REPLACE INTO categories (id, parent_id, name, position, store, section) VALUES (?, ?, ?, ?, ?, ?)');
  for (const c of categories) insCat.run(c.id, c.parent_id, c.name, c.position, store, c.section ?? null);

  const existing = new Map(db.prepare('SELECT id, price FROM products WHERE store = ?').all(store).map((r) => [r.id, r.price]));
  const upsert = db.prepare(`
    INSERT INTO products (id, store, name, size, packaging, category_id, subcategory_id, grp, position, price, bulk_price,
      ref_price, ref_format, previous_price, image, share_url, is_new, ean, brand, active, first_seen, last_seen)
    VALUES (:id, :store, :name, :size, :packaging, :category_id, :subcategory_id, :grp, :position, :price, :bulk_price,
      :ref_price, :ref_format, :previous_price, :image, :share_url, :is_new, :ean, :brand, 1, :now, :now)
    ON CONFLICT(id) DO UPDATE SET
      name = excluded.name, size = excluded.size, packaging = excluded.packaging, category_id = excluded.category_id,
      subcategory_id = excluded.subcategory_id, grp = excluded.grp, position = excluded.position,
      price = excluded.price, bulk_price = excluded.bulk_price, ref_price = excluded.ref_price,
      ref_format = excluded.ref_format, previous_price = excluded.previous_price, image = excluded.image,
      share_url = excluded.share_url, is_new = excluded.is_new, active = 1, last_seen = excluded.last_seen,
      -- Mercadona no trae el código de barras en el listado: se conserva el que ya teníamos
      ean = COALESCE(excluded.ean, products.ean), brand = COALESCE(excluded.brand, products.brand)
  `);
  const markChange = db.prepare('UPDATE products SET last_price = ?, price_changed_at = ? WHERE id = ?');
  const history = db.prepare('INSERT OR IGNORE INTO price_history (product_id, price, at) VALUES (?, ?, ?)');

  let added = 0;
  let priceChanges = 0;
  for (const r of rows) {
    const before = existing.get(r.id);
    upsert.run({ ...r, store, now });
    if (before === undefined) {
      added++;
      history.run(r.id, r.price, now);
    } else if (Math.abs(before - r.price) >= 0.005) {
      priceChanges++;
      markChange.run(before, now, r.id);
      history.run(r.id, r.price, now);
    }
  }
  const removed = db.prepare('UPDATE products SET active = 0 WHERE store = ? AND last_seen <> ? AND active = 1').run(store, now).changes;
  return { added, priceChanges, removed };
}

/**
 * Programa una pasada diaria a la hora indicada (hora de Madrid) y, al arrancar,
 * solo los catálogos nuevos o viejos. Así un redeploy con un súper nuevo (por
 * ejemplo El Corte Inglés) lo descarga sin repetir Mercadona y Consum.
 * FORCE_SCRAPE=1 fuerza todos los súpers en el arranque.
 */
export function scheduleScraping({ hour = Number(process.env.SCRAPE_HOUR ?? 6), maxAgeHours = 20 } = {}) {
  const runAll = () => scrapeAll().catch((e) => console.error('[scraper] Error:', e.message));

  const stale = process.env.FORCE_SCRAPE === '1'
    ? [...STORES]
    : STORES.filter((s) => {
      const last = getMeta(metaKey(s.id, 'catalog_updated_at'));
      return !last || Date.now() - Date.parse(last) > maxAgeHours * 3600_000;
    });
  if (stale.length) {
    console.log(`[scraper] Catálogos pendientes: ${stale.map((s) => s.name).join(', ')}. Se actualizan ahora.`);
    setTimeout(async () => {
      for (const s of stale) {
        try {
          await scrapeCatalog(s.id);
        } catch (e) {
          console.error(`[scraper] ${s.name}: ${e.message}`);
        }
      }
    }, 2000);
  }

  const scheduleNext = () => {
    const now = new Date();
    const madridNow = new Date(now.toLocaleString('en-US', { timeZone: 'Europe/Madrid' }));
    const offset = now.getTime() - madridNow.getTime();
    const next = new Date(madridNow);
    next.setHours(hour, Math.floor(Math.random() * 30), 0, 0);
    if (next <= madridNow) next.setDate(next.getDate() + 1);
    const wait = next.getTime() + offset - now.getTime();
    console.log(`[scraper] Próxima actualización: ${new Date(now.getTime() + wait).toISOString()}`);
    setTimeout(async () => { await runAll(); scheduleNext(); }, wait);
  };
  scheduleNext();
}
