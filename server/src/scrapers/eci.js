import { getStore } from '../stores.js';
import { sleep } from './http.js';

// Lector de El Corte Inglés (supermercado online).
//
// No usa la API interna /api/food-firefly (robots.txt la desaconseja). Las mismas
// rutas de categoría devuelven JSON si se piden con la cabecera `response_type: json`:
//   GET /supermercado/lacteos/  ->  { plp: { products, pagination, categories, ... } }
//
// Cada sección trae su árbol de categorías y todos sus productos paginados. Con eso
// se recorre el súper entero sin pedir una ficha por producto.

const BASE = 'https://www.elcorteingles.es';
const UA = 'ListPro/2.0 (+lista de la compra personal)';
// El Corte Inglés no publica límite, pero son ~1.000 páginas por pasada:
// una petición cada 450 ms, secuencial, con jitter y backoff si responde 429/5xx.
const DELAY_MS = Number(process.env.ECI_DELAY_MS ?? process.env.SCRAPE_DELAY_MS ?? 450);
const MAX_PAGES = Number(process.env.ECI_MAX_PAGES ?? 0); // 0 = todas
const ONLY = (process.env.ECI_SECTIONS ?? '').split(',').map((s) => s.trim()).filter(Boolean);

// Las secciones de primer nivel del súper. El nombre y el id reales se leen de la
// respuesta; aquí solo van la URL, el nombre de reserva y el pasillo de ListPro.
const SECTIONS = [
  { slug: 'alimentacion-general', name: 'Alimentación General', section: 'despensa' },
  { slug: 'desayunos-dulces-y-pan', name: 'Desayunos, dulces y pan', section: 'despensa' },
  { slug: 'lacteos', name: 'Lácteos', section: 'lacteos' },
  { slug: 'congelados', name: 'Congelados', section: 'congelados' },
  { slug: 'nutricion-y-bienestar', name: 'Nutrición y bienestar', section: 'despensa' },
  { slug: 'bebidas', name: 'Bebidas', section: 'bebidas' },
  { slug: 'frescos', name: 'Frescos', section: 'frescos' },
  { slug: 'bebes', name: 'Bebés', section: 'bebe' },
  { slug: 'cuidado-personal-y-belleza', name: 'Cuidado personal y belleza', section: 'cuidado' },
  { slug: 'drogueria-y-limpieza', name: 'Droguería y limpieza', section: 'hogar' },
  { slug: 'mascotas', name: 'Mascotas', section: 'mascotas' },
];

const { prefix: PREFIX, categoryOffset: OFFSET } = getStore('eci');

const REF_FORMATS = {
  litro: 'L', litros: 'L', kilo: 'kg', kilos: 'kg', unidad: 'ud', unidades: 'ud',
  '100 g': '100 g', '100 ml': '100 ml',
};

/** "6807.349119" -> 349119; el desplazamiento lo pone ListPro para no chocar. */
const numericId = (eciId) => Number(String(eciId).split('.').pop());
const categoryId = (eciId) => OFFSET + numericId(eciId);

function parsePrice(value) {
  if (value == null || value === '') return null;
  const s = String(value).trim();
  // "1.234,56" o "0,83"; si solo hay punto, es decimal ("0.83")
  const n = s.includes(',') ? Number(s.replace(/\./g, '').replace(',', '.')) : Number(s);
  return Number.isFinite(n) ? n : null;
}

const refFormat = (pum) => (pum ? REF_FORMATS[String(pum).trim().toLowerCase()] ?? null : null);

function cleanName(description, brand) {
  let name = String(description ?? '').trim();
  const b = String(brand ?? '').trim().toLowerCase();
  if (b && name.toLowerCase().endsWith(` - ${b}`)) name = name.slice(0, -(b.length + 3));
  name = name.replace(/\s+/g, ' ').trim();
  return name ? name.charAt(0).toUpperCase() + name.slice(1) : '';
}

/** Nodo de la sección dentro del árbol que devuelve cada PLP. */
function findNode(nodes, url) {
  for (const n of nodes ?? []) {
    if (n.url === url) return n;
    const found = findNode(n.children, url);
    if (found) return found;
  }
  return null;
}

/** Lidl/ECI repiten el nodo de la sección como hijo de sí mismo; se salta. */
function directChildren(sectionNode) {
  let node = sectionNode;
  while (node?.children?.length === 1 && node.children[0].id === node.id) node = node.children[0];
  return node?.children ?? [];
}

/** El array trae un primer nodo suelto y luego el nodo con toda la cadena de padres. */
function deepestCategory(product) {
  return (product.categories ?? []).find((c) => c.parent_category) ?? product.categories?.[0] ?? null;
}

/** Primer ancestro que está en el árbol de subcategorías de la sección. */
function directChild(product, childIds) {
  let node = deepestCategory(product);
  while (node) {
    if (childIds.has(node.identifier)) return node;
    node = node.parent_category;
  }
  return null;
}

/** "Leche y bebidas vegetales / Leche semidesnatada" sin la sección ni sus duplicados. */
function groupPath(product, sectionEciId, sectionName) {
  const names = [];
  let node = deepestCategory(product);
  while (node) {
    if (node.identifier === sectionEciId) break;
    if (node.name && node.name !== sectionName) names.push(node.name);
    node = node.parent_category;
  }
  return names.reverse().join(' / ');
}

async function getJson(path, attempt = 1) {
  try {
    const res = await fetch(BASE + path, {
      headers: {
        'User-Agent': UA,
        Accept: 'application/json',
        'Accept-Language': 'es-ES,es;q=0.9',
        response_type: 'json',
      },
      signal: AbortSignal.timeout(30_000),
    });
    if (res.status === 429 || res.status >= 500) {
      const retry = Number(res.headers.get('retry-after'));
      if (attempt >= 5) throw new Error(`HTTP ${res.status} en ${path}`);
      await sleep((Number.isFinite(retry) && retry > 0 ? retry : 2 ** attempt) * 1000);
      return getJson(path, attempt + 1);
    }
    if (!res.ok) throw Object.assign(new Error(`HTTP ${res.status} en ${path}`), { fatal: true });
    if (!String(res.headers.get('content-type') ?? '').includes('json')) {
      throw Object.assign(new Error(`Respuesta no JSON en ${path}`), { fatal: true });
    }
    return await res.json();
  } catch (e) {
    if (e.fatal || attempt >= 4) throw e;
    await sleep(1000 * 2 ** attempt + Math.round(Math.random() * 400));
    return getJson(path, attempt + 1);
  }
}

const jitter = () => Math.round(DELAY_MS * (0.8 + Math.random() * 0.4));

export async function fetchCatalog(log = console.log) {
  log('[eci] Empezando');
  const categories = [];
  const products = new Map();
  const sections = ONLY.length ? SECTIONS.filter((s) => ONLY.includes(s.slug)) : SECTIONS;
  let position = 0;

  for (const [si, source] of sections.entries()) {
    const path = `/supermercado/${source.slug}/`;
    let first;
    try {
      first = await getJson(path);
    } catch (e) {
      // Una sección caída no debe tirar el súper entero; el guard de scraper.js
      // impide guardar un catálogo demasiado incompleto.
      log(`[eci] ${source.name}: ${e.message}, se salta`);
      continue;
    }
    const plp = first?.plp;
    if (!plp?.pagination) {
      log(`[eci] ${source.name}: sin listado, se salta`);
      continue;
    }

    const sectionEciId = plp.category?.id;
    const sectionName = plp.category?.name ?? source.name;
    const sectionId = categoryId(sectionEciId);
    const tree = findNode(plp.categories, path);
    const children = directChildren(tree);
    const childIds = new Set();

    categories.push({ id: sectionId, parent_id: null, name: sectionName, position: si, section: source.section });
    children.forEach((child, ci) => {
      childIds.add(child.id);
      categories.push({
        id: categoryId(child.id),
        parent_id: sectionId,
        name: child.name,
        position: ci,
        section: source.section,
      });
    });

    const totalPages = Math.min(plp.pagination.totalPages || 1, MAX_PAGES || Infinity);
    let nextPath = path;
    let count = 0;
    const selfSubId = OFFSET + 900_000 + si;

    for (let page = 0; page < totalPages; page++) {
      const data = page === 0 ? first : await getJson(nextPath);
      const current = data?.plp;
      if (!current) break;

      for (const p of current.products ?? []) {
        if (p.type !== 'item' || !p.id || !p.description) continue;
        const prices = p.priceSpecification ?? {};
        const now = parsePrice(prices.salePrice) ?? parsePrice(prices.price);
        if (now == null) continue;

        const normal = parsePrice(prices.price);
        const child = directChild(p, childIds);
        let subId = child ? categoryId(child.identifier) : null;
        let grp = groupPath(p, sectionEciId, sectionName);
        if (!subId) {
          subId = selfSubId;
          grp = sectionName;
          if (!categories.some((c) => c.id === selfSubId)) {
            categories.push({
              id: selfSubId, parent_id: sectionId, name: `${sectionName} (otros)`, position: 999, section: source.section,
            });
          }
        }

        const brand = p.brand?.name ?? null;
        const id = PREFIX + p.id;
        if (products.has(id)) continue;
        products.set(id, {
          id,
          name: cleanName(p.description, brand),
          size: String(p.presentation_detail_quantity ?? p.presentation_detail_description ?? '').trim(),
          packaging: p.presentation_detail_description ?? null,
          category_id: sectionId,
          subcategory_id: subId,
          grp,
          position: position++,
          price: now,
          bulk_price: null,
          ref_price: parsePrice(prices.measurementUnitPrice),
          ref_format: refFormat(p.pum),
          previous_price: normal != null && normal > now + 0.001 ? normal : null,
          image: p.image ?? null,
          share_url: BASE + (p.url ?? ''),
          is_new: 0,
          ean: '',
          brand,
        });
        count++;
      }

      nextPath = current.pagination?.path_next;
      if (!nextPath || page + 1 >= totalPages) break;
      await sleep(jitter());
      if ((page + 1) % 10 === 0) log(`[eci] ${sectionName}: ${count} productos (${page + 1}/${totalPages})`);
    }

    log(`[eci] ${sectionName}: ${count} productos, ${totalPages} páginas`);
  }

  log(`[eci] Listo: ${products.size} productos, ${categories.length} categorías`);
  return { categories, products: [...products.values()], meta: {} };
}
