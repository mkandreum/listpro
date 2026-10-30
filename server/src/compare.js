import { db, getMeta } from './db.js';
import { metaKey, STORES } from './stores.js';
import { sectionIdFor } from './taxonomy.js';

// Comparador entre súper. Dos niveles:
//   "same"    — mismo código de barras (EAN): es exactamente el mismo producto.
//   "similar" — mismo pasillo, nombre parecido y formato parecido. Hace falta porque las marcas
//               blancas (Hacendado, Consum...) solo existen en su súper; se compara por €/kg o €/l.

const STOP = new Set(['de', 'del', 'la', 'el', 'los', 'las', 'con', 'sin', 'y', 'e', 'en', 'para', 'al', 'a', 'o', 'u', 'un', 'una', 'x', 'pack', 'bandeja', 'bolsa', 'brik', 'botella', 'lata', 'tarro', 'bote', 'paquete', 'caja', 'malla', 'pieza', 'granel', 'unidad', 'ud', 'uds', 'g', 'gr', 'kg', 'ml', 'cl', 'l', 'lt']);
// Marcas propias: no dicen qué es el producto y nunca coinciden entre súper
const OWN_BRANDS = ['hacendado', 'deliplus', 'bosque verde', 'compy', 'consum', 'kyrell', 'casa juncal'];
const MIN_SIMILAR = 0.5;

const fold = (s) => String(s ?? '').toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '');

/** Palabras que describen el producto, en orden: la primera suele decir qué es ("Desodorante", "Leche"). */
function words(name, brand) {
  let s = fold(name);
  for (const b of [...OWN_BRANDS, brand].filter(Boolean)) s = s.replaceAll(b, ' ');
  return s.replace(/[^a-z0-9ñ ]+/g, ' ').split(/\s+/).filter((t) => t.length > 1 && !STOP.has(t) && !/^\d/.test(t))
    // Plurales y género: "huevos"/"huevo" y "canaria"/"canario" cuentan igual
    .map((t) => (t.length > 4 && t.endsWith('s') ? t.slice(0, -1) : t))
    .map((t) => (t.length > 5 && /[ao]$/.test(t) ? t.slice(0, -1) : t));
}

/** Cantidad total en unidades base (g, ml o ud) a partir del formato: "6 x 33 cl" → 1980 ml. */
export function quantity(size) {
  const m = /(?:(\d+(?:[.,]\d+)?)\s*(?:x|ud\.?\s*x)\s*)?(\d+(?:[.,]\d+)?)\s*(kg|g|l|ml|cl|ud)\b/i.exec(fold(size));
  if (!m) return null;
  const n = (m[1] ? Number(m[1].replace(',', '.')) : 1) * Number(m[2].replace(',', '.'));
  const unit = m[3].toLowerCase();
  if (unit === 'kg') return { q: n * 1000, u: 'g' };
  if (unit === 'l') return { q: n * 1000, u: 'ml' };
  if (unit === 'cl') return { q: n * 10, u: 'ml' };
  return { q: n, u: unit };
}

// Marca de un producto: la que trae el súper o, si no (Mercadona la mete en el nombre), la que se reconozca
// en el nombre entre todas las marcas conocidas. Las marcas propias cuentan como "propia".
const OWN = '*propia*';
function brandOf(row, vocabulary) {
  const b = fold(row.brand).trim();
  if (b) return OWN_BRANDS.includes(b) ? OWN : b;
  const n = ` ${fold(row.name).replace(/[^a-z0-9ñ&' -]+/g, ' ')} `;
  if (OWN_BRANDS.some((o) => n.includes(` ${o} `))) return OWN;
  return vocabulary.find((v) => n.includes(` ${v} `)) ?? null;
}

let index = null; // { key, byStore: Map<store, { items, byToken, byEan }>, byId }

function build() {
  const key = STORES.map((s) => getMeta(metaKey(s.id, 'catalog_version')) ?? '0').join('|') + `|${getMeta(metaKey('mercadona', 'enriched_at')) ?? ''}`;
  if (index?.key === key) return index;
  const rows = db.prepare('SELECT id, store, name, size, brand, ean, price, ref_price, ref_format, image, category_id FROM products WHERE active = 1').all();
  const byStore = new Map(STORES.map((s) => [s.id, { items: [], byToken: new Map(), byEan: new Map() }]));
  const byId = new Map();
  // Marcas de más de 2 letras, las largas primero para que "coca-cola zero" gane a "coca-cola"
  const vocabulary = [...new Set(rows.map((r) => fold(r.brand).trim()).filter((b) => b.length > 2 && !OWN_BRANDS.includes(b)))].sort((a, b) => b.length - a.length);
  for (const r of rows) {
    const brand = brandOf(r, vocabulary);
    const w = words(r.name, brand === OWN ? null : brand);
    const it = { ...r, section: sectionIdFor(r.category_id), brandKey: brand, tokens: new Set(w), head: w[0] ?? null, kind: w[1] ?? null, qty: quantity(r.size) };
    const bucket = byStore.get(r.store);
    if (!bucket) continue;
    bucket.items.push(it);
    byId.set(r.id, it);
    if (r.ean) bucket.byEan.set(r.ean, it);
    for (const t of it.tokens) {
      if (!bucket.byToken.has(t)) bucket.byToken.set(t, []);
      bucket.byToken.get(t).push(it);
    }
  }
  // Peso de cada palabra según lo rara que es: "aceite" o "virgen" salen en cientos de productos,
  // "coco" en pocos; perder una palabra rara pesa mucho más que perder una corriente
  const df = new Map();
  for (const it of byId.values()) for (const t of it.tokens) df.set(t, (df.get(t) ?? 0) + 1);
  weights = new Map([...df].map(([t, n]) => [t, Math.log((byId.size + 1) / (n + 1)) + 0.1]));
  index = { key, byStore, byId };
  return index;
}

let weights = new Map();
const weight = (t) => weights.get(t) ?? 1;
const weightOf = (set) => { let w = 0; for (const t of set) w += weight(t); return w || 1; };

function similarity(a, b) {
  if (a.section !== b.section) return 0;
  // Tiene que ser el mismo tipo de producto, mirado desde los dos lados: una crema de manos no es un
  // desodorante en crema, ni un tomate frito con aceite de oliva es aceite de oliva
  if (!a.head || !b.head || !b.tokens.has(a.head) || !a.tokens.has(b.head)) return 0;
  // Y de la misma clase: la palabra que sigue al tipo (aceite de COCO, leche de AVENA) tiene que estar en el otro
  if (a.kind && b.kind && !b.tokens.has(a.kind) && !a.tokens.has(b.kind)) return 0;
  let shared = 0;
  for (const t of a.tokens) if (b.tokens.has(t)) shared += weight(t);
  const base = shared / weightOf(a.tokens);
  // Marca y formato ayudan a desempatar, pero no convierten en parecido lo que solo comparte lo genérico
  if (base < 0.45) return 0;
  // Cuánto de lo que describe a "a" está en "b", con algo de castigo por lo que sobra en "b"
  let score = base - 0.15 * ((weightOf(b.tokens) - shared) / weightOf(b.tokens));
  // La palabra que más distingue a "a" (sin contar el tipo) tiene que estar: aceite de coco no es aceite de oliva
  let key = null;
  for (const t of a.tokens) if (t !== a.head && (!key || weight(t) > weight(key))) key = t;
  if (key && !b.tokens.has(key)) score -= 0.4;
  // La marca pesa: Coca-Cola no es "un refresco de cola" cualquiera
  if (a.brandKey && b.brandKey) {
    if (a.brandKey === b.brandKey) score += 0.25;
    else if (a.brandKey !== OWN && b.brandKey !== OWN) score -= 0.35;
    else score -= 0.1;
  }
  if (a.qty && b.qty && a.qty.u === b.qty.u) {
    const r = a.qty.q / b.qty.q;
    score += r > 0.8 && r < 1.25 ? 0.15 : r > 0.4 && r < 2.5 ? 0 : -0.2;
  }
  return score;
}

const view = (it) => ({ id: it.id, store: it.store, name: it.name, size: it.size, price: it.price, refPrice: it.ref_price, refFormat: it.ref_format, image: it.image });

/** Para un producto, el mejor equivalente en cada uno de los otros súper (más alternativas si son parecidos). */
function matchesFor(it, idx) {
  const out = [];
  for (const [store, bucket] of idx.byStore) {
    if (store === it.store) continue;
    const exact = it.ean ? bucket.byEan.get(it.ean) : null;
    if (exact) { out.push({ store, match: 'same', product: view(exact), alternatives: [] }); continue; }
    const seen = new Set();
    const scored = [];
    for (const t of it.tokens) {
      for (const c of bucket.byToken.get(t) ?? []) {
        if (seen.has(c.id)) continue;
        seen.add(c.id);
        const s = similarity(it, c);
        if (s >= MIN_SIMILAR) scored.push([s, c]);
      }
    }
    if (!scored.length) continue;
    // Entre los que se parecen casi igual, primero el más barato por kilo o litro
    scored.sort((a, b) => (Math.abs(b[0] - a[0]) > 0.08 ? b[0] - a[0] : unitPrice(a[1]) - unitPrice(b[1])));
    const [best, ...rest] = scored.slice(0, 4).map(([, c]) => view(c));
    out.push({ store, match: 'similar', product: best, alternatives: rest });
  }
  return out;
}
const unitPrice = (c) => c.ref_price ?? c.price;

const cache = new Map();

/** Equivalentes de un producto en los otros súper. */
export function compareProduct(id) {
  const idx = build();
  const it = idx.byId.get(String(id));
  if (!it) return null;
  const key = `${idx.key}:${it.id}`;
  if (!cache.has(key)) {
    if (cache.size > 5000) cache.clear();
    cache.set(key, { id: it.id, store: it.store, matches: matchesFor(it, idx) });
  }
  return cache.get(key);
}

/**
 * Para la cesta: de cada producto, la alternativa más barata en otro súper si la hay. Solo cuenta
 * "same" o un "similar" con el mismo formato de precio por unidad, para no comparar peras con kilos.
 */
export function cheaperElsewhere(ids) {
  const out = {};
  for (const id of ids.slice(0, 300)) {
    const c = compareProduct(id);
    if (!c) continue;
    const self = build().byId.get(c.id);
    let best = null;
    for (const m of c.matches) {
      const p = m.product;
      const saving = m.match === 'same'
        ? self.price - p.price
        : p.refFormat && p.refFormat === self.ref_format && self.ref_price && p.refPrice ? (self.ref_price - p.refPrice) / self.ref_price * self.price : 0;
      if (saving >= 0.05 && (!best || saving > best.saving)) best = { ...m, saving: Math.round(saving * 100) / 100 };
    }
    if (best) out[id] = best;
  }
  return out;
}
