import crypto from 'node:crypto';
import zlib from 'node:zlib';
import { db, getMeta } from './db.js';
import { buildTree, reloadSections, sectionIdFor } from './taxonomy.js';
import { DEFAULT_STORE, getStore, metaKey } from './stores.js';

// Un cambio de precio se marca como "sube/baja" en la app durante estos días.
const PRICE_CHANGE_DAYS = 30;

const caches = new Map(); // súper → { version, etag, json, gzip, meta }

/** Catálogo completo de un súper para web y app. Se construye una vez por versión y se sirve ya comprimido. */
export function getCatalog(store = DEFAULT_STORE) {
  const version = getMeta(metaKey(store, 'catalog_version')) ?? '0';
  const cached = caches.get(store);
  if (cached?.version === version) return cached;

  // Si el scrape lo ha hecho otro proceso (CLI, otro contenedor), este proceso
  // necesita releer el pasillo de cada categoría antes de construir el árbol.
  reloadSections();

  const categories = db.prepare('SELECT id, name, position FROM categories WHERE parent_id IS NULL AND store = ? ORDER BY position').all(store);
  const subcategories = db.prepare('SELECT id, parent_id, name, position FROM categories WHERE parent_id IS NOT NULL AND store = ? ORDER BY parent_id, position').all(store);
  const rows = db.prepare('SELECT * FROM products WHERE active = 1 AND store = ? ORDER BY position').all(store);

  const counts = new Map();
  for (const r of rows) counts.set(r.subcategory_id, (counts.get(r.subcategory_id) ?? 0) + 1);

  const since = Date.now() - PRICE_CHANGE_DAYS * 86400_000;
  const products = rows.map((r) => {
    const recent = r.price_changed_at && Date.parse(r.price_changed_at) > since && r.last_price != null;
    return {
      id: r.id,
      store,
      name: r.name,
      size: r.size,
      section: sectionIdFor(r.category_id),
      category: r.category_id,
      subcategory: r.subcategory_id,
      group: r.grp,
      price: r.price,
      refPrice: r.ref_price,
      refFormat: r.ref_format,
      // Precio anterior: el tachado del súper o, si no, el último que registramos nosotros
      previousPrice: r.previous_price ?? (recent ? r.last_price : null),
      priceChangedAt: recent ? r.price_changed_at : null,
      image: r.image,
      url: r.share_url,
      isNew: !!r.is_new,
    };
  });

  const sections = buildTree(categories, subcategories, counts, store);
  attachPreviews(sections, products);

  const body = {
    version,
    store,
    storeName: getStore(store)?.name ?? store,
    updatedAt: getMeta(metaKey(store, 'catalog_updated_at')),
    warehouse: store === DEFAULT_STORE ? getMeta('warehouse') : null,
    count: products.length,
    sections,
    products,
  };
  const json = Buffer.from(JSON.stringify(body));
  const built = {
    version,
    // Del contenido, no de la versión: si cambia el formato de la respuesta, los clientes no reciben un 304 viejo
    etag: `"${crypto.createHash('sha1').update(json).digest('hex').slice(0, 16)}"`,
    json,
    gzip: zlib.gzipSync(json, { level: 9 }),
    meta: { version, store, updatedAt: body.updatedAt, warehouse: body.warehouse, count: body.count },
  };
  caches.set(store, built);
  return built;
}

// Fotos reales para pintar las baldosas: la primera de cada subcategoría y hasta 3 por categoría,
// cada una de una subcategoría distinta para que la baldosa cuente lo que hay dentro.
function attachPreviews(sections, products) {
  const firstBySub = new Map();
  for (const p of products) if (p.image && !firstBySub.has(p.subcategory)) firstBySub.set(p.subcategory, p.image);
  for (const s of sections) {
    for (const c of s.categories) {
      for (const sub of c.subcategories) sub.image = firstBySub.get(sub.id) ?? null;
      c.images = c.subcategories.map((sub) => sub.image).filter(Boolean).slice(0, 3);
    }
    s.images = s.categories.map((c) => c.images[0]).filter(Boolean).slice(0, 4);
  }
}

export function getPriceHistory(id) {
  const product = db.prepare('SELECT id, name, price FROM products WHERE id = ?').get(id);
  if (!product) return null;
  const history = db.prepare('SELECT price, at FROM price_history WHERE product_id = ? ORDER BY at').all(id);
  return { ...product, history };
}
