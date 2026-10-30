import { getStore } from '../stores.js';
import { consumSection } from '../taxonomy.js';
import { getJson, num, sleep } from './http.js';

// API pública de la tienda online de Consum (tienda.consum.es). El árbol de categorías tiene
// 3 o 4 niveles: sección ("Despensa") → categoría ("Arroz, pastas, legumbres") → subcategoría → hoja.
// En ListPro: categoría = nivel 2, subcategoría = nivel 3 y grupo = la hoja.
const BASE = 'https://tienda.consum.es/api/rest/V1.0';
const DELAY_MS = Number(process.env.SCRAPE_DELAY_MS ?? 350);
const PAGE = 100; // lo máximo que devuelve por página
const { prefix: PREFIX, categoryOffset: OFFSET } = getStore('consum');
// Una categoría de nivel 2 sin subcategorías hace también de subcategoría con este id
const SELF_SUB = 500_000;
// "Ahora más barato": repite productos de las demás categorías
const OFFERS_ROOT = 99999;

const UNITS = { gr: 'g', g: 'g', grs: 'g', kg: 'kg', kgs: 'kg', ml: 'ml', cl: 'cl', l: 'L', lt: 'L', lts: 'L', litro: 'L', litros: 'L', ud: 'ud.', uds: 'ud.', unid: 'ud.', unidades: 'ud.' };
const SIZE_RE = /(\d+(?:[.,]\d+)?\s*(?:x\s*\d+(?:[.,]\d+)?\s*)?)(grs?|g|kgs?|ml|cl|lts?|l|litros?|uds?|unid(?:ades)?)\.?$/i;

/** "Rabanito Bolsa 250 Gr" → "250 g"; "Pack 6 x 33 Cl" → "6 x 33 cl". Si no hay cantidad, el formato ("Pieza precio aprox."). */
export function sizeOf(name, description, format) {
  const rest = description?.startsWith(name) ? description.slice(name.length).trim() : description ?? '';
  const m = SIZE_RE.exec(rest) ?? SIZE_RE.exec(description ?? '');
  if (m) return `${m[1].replace(/\s+/g, ' ').trim()} ${UNITS[m[2].toLowerCase()] ?? m[2]}`;
  return (format ?? '').trim();
}

// Mismos códigos que Mercadona (lv = lavado, dc = docena, m = metro) para poder comparar €/unidad entre súper
const REF_FORMATS = { '1 kg': 'kg', '1 l': 'L', '1 u': 'ud', '1 ud': 'ud', '1 ud.': 'ud', '100 gr': '100 g', '100 g': '100 g', '100 ml': '100 ml', '1 lv': 'lv', '1 dc': 'dc', '1 do': 'dz', '1 docena': 'dz', '1 m': 'm' };
const refFormat = (v) => (v ? REF_FORMATS[v.trim().toLowerCase()] ?? v.trim().toLowerCase() : null);

/** Árbol: para cada hoja, su categoría (nivel 2), subcategoría (nivel 3) y nombre de grupo. */
function readTree(menu) {
  const categories = [];
  const leaf = new Map(); // id de Consum → { category, subcategory, group }
  menu.filter((top) => top.id !== OFFERS_ROOT).forEach((top, ti) => {
    (top.subcategories ?? []).forEach((cat, ci) => {
      const catId = OFFSET + cat.id;
      // El nivel 1 va en la posición para conservar el orden de la tienda
      const section = consumSection(top.id, cat.id);
      categories.push({ id: catId, parent_id: null, name: cat.name, position: ti * 100 + ci, section });
      const subs = cat.subcategories ?? [];
      if (!subs.length) {
        const subId = OFFSET + SELF_SUB + cat.id;
        categories.push({ id: subId, parent_id: catId, name: cat.name, position: 0, section });
        leaf.set(cat.id, { category: catId, subcategory: subId, group: cat.name });
        return;
      }
      subs.forEach((sub, si) => {
        const subId = OFFSET + sub.id;
        categories.push({ id: subId, parent_id: catId, name: sub.name, position: si, section });
        leaf.set(sub.id, { category: catId, subcategory: subId, group: sub.name });
        const walk = (node) => {
          for (const n of node.subcategories ?? []) {
            leaf.set(n.id, { category: catId, subcategory: subId, group: n.name });
            walk(n);
          }
        };
        walk(sub);
      });
    });
  });
  return { categories, leaf };
}

function toRow(p, place, position) {
  const d = p.productData ?? {};
  const prices = p.priceData?.prices ?? [];
  const regular = prices.find((x) => x.id === 'PRICE')?.value;
  const offer = prices.find((x) => x.id === 'OFFER_PRICE')?.value;
  const now = offer ?? regular;
  return {
    id: `${PREFIX}${p.id}`,
    name: String(d.name ?? '').trim(),
    size: sizeOf(d.name ?? '', d.description, d.format),
    packaging: null,
    category_id: place.category,
    subcategory_id: place.subcategory,
    grp: place.group,
    position,
    price: num(now?.centAmount),
    bulk_price: null,
    ref_price: num(now?.centUnitAmount),
    ref_format: refFormat(p.priceData?.unitPriceUnitType),
    // El precio de siempre mientras dura la oferta
    previous_price: offer && regular && regular.centAmount > offer.centAmount ? num(regular.centAmount) : null,
    image: d.imageURL ?? null,
    share_url: d.url ?? null,
    is_new: d.novelty ? 1 : 0,
    ean: p.ean ? String(p.ean) : '',
    brand: d.brand?.name ?? null,
  };
}

export async function fetchCatalog(log) {
  log('[consum] Empezando');
  const { categories, leaf } = readTree(await getJson(`${BASE}/shopping/category/menu`));
  const products = new Map();
  let offset = 0;
  let total = Infinity;
  let pos = 0;
  while (offset < total) {
    const page = await getJson(`${BASE}/catalog/product?limit=${PAGE}&offset=${offset}&showRecommendations=false`);
    total = page.totalCount ?? 0;
    for (const p of page.products ?? []) {
      if (p.productData?.availability === '0') continue;
      const place = (p.categories ?? []).map((c) => leaf.get(c.id)).find(Boolean);
      if (!place) continue;
      const row = toRow(p, place, pos++);
      if (row.price != null && row.name && !products.has(row.id)) products.set(row.id, row);
    }
    if (!page.hasMore) break;
    offset += PAGE;
    if (offset % 2000 === 0) log(`[consum] ${products.size} productos de ${total}`);
    await sleep(DELAY_MS);
  }
  return { categories, products: [...products.values()], meta: {} };
}
