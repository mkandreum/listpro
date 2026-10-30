import { db } from '../db.js';
import { getJson, num, sleep } from './http.js';

// API pública que usa la propia tienda online (tienda.mercadona.es). El almacén (wh) fija los
// precios de la zona: mad1 = Madrid, bcn1 = Barcelona, vlc1 = Valencia, svq1 = Sevilla...
const BASE = 'https://tienda.mercadona.es/api';
export const WAREHOUSE = process.env.MERCADONA_WH ?? 'mad1';
const DELAY_MS = Number(process.env.SCRAPE_DELAY_MS ?? 350);
// Códigos de barras por pasada: el listado no los trae y hay que pedir la ficha de cada producto.
// Como casi no cambian, se rellenan poco a poco (y siempre los productos nuevos).
const EAN_PER_RUN = Number(process.env.EAN_PER_RUN ?? 1500);

const api = (pathname) => getJson(`${BASE}${pathname}?lang=es&wh=${WAREHOUSE}`);

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
    ean: null,
    brand: null,
  };
}

/** Catálogo completo: categoría → subcategoría → grupos de productos. */
export async function fetchCatalog(log) {
  log(`[mercadona] Empezando (almacén ${WAREHOUSE})`);
  const root = await api('/categories/');
  const categories = [];
  const products = new Map();
  let pos = 0;
  for (const [ci, cat] of root.results.entries()) {
    categories.push({ id: cat.id, parent_id: null, name: cat.name, position: ci });
    for (const [si, sub] of cat.categories.entries()) {
      categories.push({ id: sub.id, parent_id: cat.id, name: sub.name, position: si });
      await sleep(DELAY_MS);
      const detail = await api(`/categories/${sub.id}/`);
      for (const group of detail.categories ?? []) {
        for (const p of group.products ?? []) {
          if (!p.published || products.has(String(p.id))) continue;
          const row = toRow(p, cat.id, sub.id, group.name, pos++);
          if (row.price != null) products.set(row.id, row);
        }
      }
    }
    log(`[mercadona] ${cat.name}: ${products.size} productos acumulados`);
  }
  return { categories, products: [...products.values()], meta: { warehouse: WAREHOUSE } };
}

/** Rellena códigos de barras y marca de los que aún no los tienen (para comparar con otros súper). */
export async function enrich(log) {
  const missing = db.prepare("SELECT id FROM products WHERE store = 'mercadona' AND active = 1 AND ean IS NULL ORDER BY first_seen DESC LIMIT ?").all(EAN_PER_RUN);
  if (!missing.length) return;
  const save = db.prepare('UPDATE products SET ean = ?, brand = ? WHERE id = ?');
  let done = 0;
  for (const { id } of missing) {
    await sleep(DELAY_MS);
    try {
      const p = await api(`/products/${id}/`);
      // Sin código, se guarda vacío para no volver a pedirlo cada día
      save.run(p.ean ? String(p.ean) : '', p.brand ? String(p.brand) : null, id);
      done++;
    } catch (e) {
      if (e.fatal) save.run('', null, id);
    }
  }
  log(`[mercadona] Códigos de barras: ${done} de ${missing.length}`);
}
