import { db, getMeta } from './db.js';
import { metaKey, STORES } from './stores.js';
import { sectionIdFor } from './taxonomy.js';

// Comparador entre súper. Tres niveles de confianza:
//   "same"       — mismo código de barras (EAN): es exactamente el mismo producto.
//   "equivalent" — el mismo producto de otra marca (sobre todo marca blanca contra marca blanca):
//                  mismo tipo, misma variante (entera, sin lactosa, virgen extra...) y formato comparable.
//   "similar"    — del mismo tipo pero con alguna diferencia (otra marca conocida, otra variante suave,
//                  formato muy distinto). Sirve para mirar, no para decir "ahorras X".
//
// Lo que más fallaba antes: los packs ("6 x 1 L" contra "1 L" contaba como el mismo precio), los nombres
// largos de El Corte Inglés ("aceite suave ... contiene 20% de virgen extra"), las variantes que no se
// pueden mezclar y que cada súper pone el mismo producto en un pasillo distinto.

const STOP = new Set(['de', 'del', 'la', 'el', 'los', 'las', 'con', 'y', 'e', 'en', 'para', 'al', 'a', 'o', 'u', 'un', 'una', 'x', 'pack', 'bandeja', 'bolsa', 'brik', 'botella', 'lata', 'tarro', 'bote', 'paquete', 'caja', 'malla', 'pieza', 'granel', 'unidad', 'unidade', 'ud', 'uds', 'g', 'gr', 'kg', 'ml', 'cl', 'l', 'lt', 'pro', 'plus', 'original', 'clasico', 'classic', 'nuevo', 'new', 'edicion', 'limitada', 'aprox', 'aproximado', 'peso', 'formato', 'ahorro', 'familiar', 'neto', 'escurrido', 'rebanada', 'calidad', 'seleccion', 'extra',
  // Lo que ya miran las variantes, y tamaños que cada súper llama a su manera
  'sin', 'mg', 'anadid', 'anadido', 'azucar', 'azucare', 'lactos', 'gluten', 'grande', 'grand', 'median', 'pequen', 'mini', 'maxi', 'gigante', 'ud.', 'corte', 'fino', 'fina', 'docena']);
// Marcas propias: no dicen qué es el producto y nunca coinciden entre súper
const OWN_BRANDS = ['hacendado', 'deliplus', 'bosque verde', 'compy', 'steinburg', 'fuente dehesa', 'la verja', 'consum', 'kyrell', 'casa juncal', 'el corte ingles', 'hipercor', 'aliada', 'mimosin', 'ecocesta'];
const MIN_SIMILAR = 0.5;
const MIN_EQUIVALENT = 0.85;

const fold = (s) => String(s ?? '').toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '');

// Donde empieza el envase o la descripción: "Pan de molde blanco sin corteza 16 rebanadas bolsa 450 g",
// "Aceite de oliva suave 0,4º contiene aceites refinados...". Solo se corta si antes hay al menos dos
// palabras, para no romper productos que empiezan así ("Bolsas de basura", "Caja de galletas").
const CUT = /\s(contiene|elaborad[oa]s?|procedente|ideal para|apto para|perfect[oa] para|peso aproximado|al peso|envase|envases|bandeja|botella|botellas|garrafa|lata|latas|brik|briks|tarro|frasco|bolsa|bolsas|paquete|paquetes|pack|caja|estuche|bote|dosificador|malla|sobres?|tarrina|blister|formato|de cultivo|enriquecid[oa]|fuente de|rico en|sin gluten y|pack-\d+)\b/;

function core(name) {
  const s = ` ${fold(name).replace(/[.,;:()"!¡]+/g, ' ').replace(/\s+/g, ' ').trim()}`;
  const m = CUT.exec(s);
  if (m && s.slice(0, m.index).trim().split(' ').length >= 2) return s.slice(0, m.index).trim();
  return s.trim();
}

const stem = (t) => {
  let w = t.length > 4 && t.endsWith('es') && /[^aeiou]es$/.test(t) ? t.slice(0, -2) : t.length > 4 && t.endsWith('s') ? t.slice(0, -1) : t;
  if (w.length > 5 && /[ao]$/.test(w)) w = w.slice(0, -1);
  return w;
};

/** Palabras que describen el producto, en orden: la primera suele decir qué es ("Desodorante", "Leche"). */
function words(coreName, brand) {
  let s = ` ${coreName} `;
  for (const b of [...OWN_BRANDS, brand].filter(Boolean)) s = s.replaceAll(` ${b} `, ' ');
  return s.replace(/[^a-z0-9ñ ]+/g, ' ').split(/\s+/).filter((t) => t.length > 1 && !STOP.has(t) && !/^\d/.test(t)).map(stem).filter((t) => !STOP.has(t));
}

// Variantes que no se pueden mezclar. Se miran en el nombre recortado (no en la descripción).
// missing: qué pasa si un lado la tiene y el otro no dice nada.
const VARIANTS = [
  { key: 'grasa', re: [['semi', /\bsemi ?desnatad/], ['desnatada', /\bdesnatad/], ['entera', /\bentera\b/]], missing: 0.3 },
  { key: 'lactosa', re: [['sin', /\bsin lactosa\b|\b0% lactosa/]], missing: 'reject' },
  { key: 'aceite', re: [['virgen extra', /\bvirgen extra\b|\bv\.? ?e\.?\b/], ['virgen', /\bvirgen\b/], ['suave', /\bsuave\b|0[,.]4/], ['intenso', /\bintenso\b|\b1º/]], missing: 0.3 },
  { key: 'cafeina', re: [['descafeinado', /\bdescafeinad|\bsin cafeina\b|\bdecaf/]], missing: 'reject' },
  { key: 'integral', re: [['integral', /\bintegral\b/]], missing: 'reject' },
  { key: 'azucar', re: [['sin', /\bsin azucar|\b0% azucar|\bsin azucares|\bzero\b|\bcero\b|\blight\b|\bedulcorad/], ['con', /\bazucarad|\bcon azucar\b/]], missing: 0.25 },
  { key: 'gas', re: [['con', /\bcon gas\b|\bgasificad/]], missing: 'reject' },
  { key: 'eco', re: [['eco', /\becologic|\bbio\b/]], missing: 0.1 },
  // El formato también cambia el producto: un spray de aceite no sustituye a una botella, ni las cápsulas al café molido
  { key: 'formato', re: [['spray', /\bspray\b|\bpulverizador\b/], ['monodosis', /\bmonodosis\b/], ['capsulas', /\bcapsulas?\b/], ['soluble', /\bsoluble\b/], ['grano', /\ben grano\b/], ['polvo', /\ben polvo\b/]], missing: 'reject' },
  // En conservas y fritos el aceite cambia el producto: atún en aceite de oliva no es atún en girasol
  { key: 'aceiteTipo', re: [['oliva', /\boliva\b/], ['girasol', /\bgirasol\b/], ['natural', /\bal natural\b|\ben agua\b/], ['escabeche', /\bescabeche\b/]], missing: 0.25 },
  { key: 'sal', re: [['baja', /\bbajo en sal\b|\bsin sal\b|\breducido en sal\b|\bbaja en sal\b/]], missing: 0.3 },
  { key: 'gluten', re: [['sin', /\bsin gluten\b/]], missing: 0 },
];

function variantsOf(coreName) {
  const out = {};
  for (const g of VARIANTS) {
    for (const [v, re] of g.re) if (re.test(coreName)) { out[g.key] = v; break; }
  }
  return out;
}

/** Diferencia de variantes: null si no se pueden comparar; si no, la penalización. */
function variantPenalty(a, b) {
  let p = 0;
  for (const g of VARIANTS) {
    const va = a[g.key], vb = b[g.key];
    if (va === vb) continue;
    if (va && vb) return null;
    if (g.missing === 'reject') return null;
    p += g.missing;
  }
  return p;
}

// ───────── Cantidad total y precio por unidad ─────────

const COUNT_WORDS = 'unidades|unidad|uds?|latas?|botellas?|briks?|bolsas?|paquetes?|envases?|tarrinas?|sobres?|botes?|tarros?|frascos?|piezas?|rollos?|cajas?';
const num = (s) => Number(String(s).replace(',', '.'));
const toBase = (n, unit) => {
  switch (unit) {
    case 'kg': return { q: n * 1000, u: 'g' };
    case 'g': case 'gr': case 'grs': return { q: n, u: 'g' };
    case 'l': case 'lt': case 'litro': case 'litros': return { q: n * 1000, u: 'ml' };
    case 'cl': return { q: n * 10, u: 'ml' };
    case 'ml': return { q: n, u: 'ml' };
    default: return null;
  }
};

/** Cantidad total en unidades base (g, ml, ud o lavados) leyendo el formato y el nombre: "6 x 33 cl" → 1980 ml. */
export function quantity(size, name = '') {
  const s = fold(`${size ?? ''} | ${name ?? ''}`).replace(/(\d),(\d)/g, '$1.$2');
  // "6 x 125 g", "6 ud. x 125 g", "4 paquetes x 200 g", "pack 2 x 200 g"
  let m = new RegExp(`(\\d+)\\s*(?:(?:${COUNT_WORDS})\\.?\\s*)?x\\s*(\\d+(?:\\.\\d+)?)\\s*(kg|g|gr|l|ml|cl)\\b`).exec(s);
  if (m) return { ...toBase(Number(m[1]) * num(m[2]), m[3]), n: Number(m[1]) };
  const amount = /(\d+(?:\.\d+)?)\s*(kg|grs?|g|litros?|lt|l|ml|cl)\b/.exec(s);
  // "pack 8 unidades 120 g", "pack 6 botellas 2 l", "Pack-6 ..." (El Corte Inglés pone el tamaño de una sola)
  const pack = new RegExp(`\\bpack(?:-|\\s+de\\s+|\\s+)(\\d+)\\b|\\b(\\d+)\\s*(?:${COUNT_WORDS})\\b`).exec(s);
  if (amount) {
    const one = toBase(num(amount[1]), amount[2]);
    const n = pack ? Number(pack[1] ?? pack[2]) : 1;
    // Si el formato ya da el total ("envase 800 g" con "4 paquetes x 200 g") lo de arriba ya lo resolvió
    if (one) return { ...one, q: one.q * (n > 1 && n <= 48 ? n : 1), n: n > 1 && n <= 48 ? n : 1 };
  }
  const lav = /(\d+)\s*(?:lavados|dosis|cacitos|capsulas|pastillas)\b/.exec(s);
  if (lav) return { q: Number(lav[1]), u: 'lv', n: 1 };
  if (pack) return { q: Number(pack[1] ?? pack[2]), u: 'ud', n: 1 };
  if (/\bmedia docena\b|\b1\/2 docena\b/.test(s)) return { q: 6, u: 'ud', n: 1 };
  if (/\bdocena\b/.test(s)) return { q: 12, u: 'ud', n: 1 };
  return null;
}

/** Lo que dice el súper con su precio por unidad ("0,96 €/L"): es lo más fiable para saber cuánto trae. */
function fromRef(price, refPrice, refFormat) {
  if (!price || !refPrice) return null;
  const f = fold(refFormat).replace(/\s+/g, ' ').trim();
  const units = { kg: ['g', 1000], kilo: ['g', 1000], g: ['g', 1], '100 g': ['g', 100], l: ['ml', 1000], lt: ['ml', 1000], litro: ['ml', 1000], ml: ['ml', 1], '100 ml': ['ml', 100], ud: ['ud', 1], uds: ['ud', 1], unidad: ['ud', 1], lv: ['lv', 1], lavado: ['lv', 1], dosis: ['lv', 1] };
  const u = units[f];
  if (!u) return null;
  return { q: (price / refPrice) * u[1], u: u[0] };
}

function measure(r) {
  const parsed = quantity(r.size, r.name);
  const ref = fromRef(r.price, r.ref_price, r.ref_format);
  // Lo que dice el súper manda, salvo que se contradiga mucho con lo que pone el envase (precios por unidad mal puestos)
  if (ref && (!parsed || parsed.u !== ref.u || Math.abs(Math.log(ref.q / parsed.q)) < 0.4)) return { ...ref, n: parsed?.n ?? 1 };
  if (parsed) return parsed;
  // Fruta, carne y pescado "al peso" de El Corte Inglés: el precio es por kilo
  if (/\bal peso\b/.test(fold(r.name)) && fold(r.ref_format) === 'kg') return { q: 1000, u: 'g', n: 1 };
  return null;
}

const PER = { g: ['kg', 1000], ml: ['L', 1000], ud: ['ud', 1], lv: ['lavado', 1] };
/** Precio por kilo/litro/unidad calculado por nosotros, igual para todos los súper. */
const perUnit = (it) => (it.qty ? { value: Math.round((it.price / it.qty.q) * PER[it.qty.u][1] * 1000) / 1000, label: PER[it.qty.u][0] } : null);

// ───────── Índice ─────────

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
let weights = new Map();

function build() {
  const key = STORES.map((s) => getMeta(metaKey(s.id, 'catalog_version')) ?? '0').join('|') + `|${getMeta(metaKey('mercadona', 'enriched_at')) ?? ''}`;
  if (index?.key === key) return index;
  const rows = db.prepare('SELECT id, store, name, size, brand, ean, price, ref_price, ref_format, image, category_id FROM products WHERE active = 1').all();
  const byStore = new Map(STORES.map((s) => [s.id, { items: [], byToken: new Map(), byEan: new Map() }]));
  const byId = new Map();
  // Marcas de más de 2 letras, las largas primero para que "coca-cola zero" gane a "coca-cola"
  const vocabulary = [...new Set(rows.map((r) => fold(r.brand).trim()).filter((b) => b.length > 2 && !OWN_BRANDS.includes(b)))].sort((a, b) => b.length - a.length);
  for (const r of rows) {
    const bucket = byStore.get(r.store);
    if (!bucket) continue;
    const brand = brandOf(r, vocabulary);
    const c = core(r.name);
    const w = words(c, brand === OWN ? null : brand);
    const it = {
      ...r, section: sectionIdFor(r.category_id), brandKey: brand, tokens: new Set(w), head: w[0] ?? null, kind: w[1] ?? null,
      variants: variantsOf(c), qty: measure(r),
    };
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
  cache.clear();
  return index;
}

const weight = (t) => weights.get(t) ?? 1;
const weightOf = (set) => { let w = 0; for (const t of set) w += weight(t); return w || 1; };

/** Cuánto se parece "b" a "a" (el producto de la cesta). 0 = no sirve. */
function similarity(a, b) {
  // Siempre el mismo tipo de producto, mirado desde los dos lados: una crema de manos no es un
  // desodorante en crema, ni unas patatas fritas son cebolla frita
  if (!a.head || !b.head || !b.tokens.has(a.head) || !a.tokens.has(b.head)) return 0;
  // Y de la misma clase: la palabra que sigue al tipo (aceite de COCO, leche de AVENA) tiene que estar en el otro
  if (a.kind && b.kind && !b.tokens.has(a.kind) && !a.tokens.has(b.kind)) return 0;
  const vp = variantPenalty(a.variants, b.variants);
  if (vp === null) return 0;

  let shared = 0;
  for (const t of a.tokens) if (b.tokens.has(t)) shared += weight(t);
  const base = shared / weightOf(a.tokens);
  if (base < 0.45) return 0;
  // Cuánto de lo que describe a "a" está en "b", con algo de castigo por lo que sobra en "b"
  let score = base - 0.15 * ((weightOf(b.tokens) - shared) / weightOf(b.tokens)) - vp;
  // La palabra que más distingue a "a" (sin contar el tipo) tiene que estar: aceite de coco no es aceite de oliva
  let key = null;
  for (const t of a.tokens) if (t !== a.head && (!key || weight(t) > weight(key))) key = t;
  if (key && !b.tokens.has(key)) score -= 0.4;
  // Cada súper reparte los pasillos a su manera (el pan de molde es "frescos" en uno y "despensa" en otro):
  // otro pasillo no descarta, pero resta un poco
  if (a.section !== b.section) score -= 0.05;
  // La marca pesa: Coca-Cola no es "un refresco de cola" cualquiera. Pero la marca blanca
  // del súper sí puede sustituir a una marca conocida si el producto es el mismo.
  if (a.brandKey && b.brandKey) {
    if (a.brandKey === b.brandKey) score += 0.25;
    else if (a.brandKey === OWN && b.brandKey === OWN) score += 0.15;
    else if (a.brandKey === OWN || b.brandKey === OWN) score += 0.0;
    else score -= 0.35;
  }
  if (a.qty && b.qty) {
    if (a.qty.u !== b.qty.u) score -= 0.15;
    else {
      const r = closest(a.qty, b.qty);
      score += r > 0.8 && r < 1.25 ? 0.15 : r > 0.4 && r < 2.5 ? 0 : -0.1;
    }
  }
  return score;
}

/** Proporción de tamaño más favorable entre el total y lo que trae cada envase: un pack de 6 litros y un litro suelto son el mismo producto. */
function closest(a, b) {
  const total = b.q / a.q;
  const each = (b.q / (b.n || 1)) / (a.q / (a.n || 1));
  return Math.abs(Math.log(total)) < Math.abs(Math.log(each)) ? total : each;
}

/** Cuántos envases del otro hacen falta para cubrir lo mismo que `qty` del producto de la cesta. */
function packsFor(self, other, qty) {
  if (!self.qty || !other.qty || self.qty.u !== other.qty.u) return { packs: qty, comparable: false };
  const need = self.qty.q * qty;
  let packs = Math.max(1, Math.round(need / other.qty.q));
  if (packs * other.qty.q < need * 0.75) packs++;
  return { packs, comparable: true };
}

const view = (it) => ({
  id: it.id, store: it.store, name: it.name, size: it.size, price: it.price, refPrice: it.ref_price, refFormat: it.ref_format, image: it.image,
  per: perUnit(it),
});

/** Opciones de "it" en un súper, de más a menos parecida, con su nivel de confianza. */
function optionsIn(it, bucket) {
  const exact = it.ean ? bucket.byEan.get(it.ean) : null;
  if (exact) return [{ item: exact, match: 'same', score: 2 }];
  const seen = new Set();
  const scored = [];
  for (const t of it.tokens) {
    for (const c of bucket.byToken.get(t) ?? []) {
      if (seen.has(c.id)) continue;
      seen.add(c.id);
      const s = similarity(it, c);
      if (s >= MIN_SIMILAR) scored.push({ item: c, score: s });
    }
  }
  for (const o of scored) {
    const r = it.qty && o.item.qty && it.qty.u === o.item.qty.u ? closest(it.qty, o.item.qty) : null;
    o.match = o.score >= MIN_EQUIVALENT && r != null && r > 0.3 && r < 3.5 ? 'equivalent' : 'similar';
  }
  // Primero los equivalentes; entre los que se parecen casi igual, el más barato por kilo o litro
  const rank = (o) => (o.match === 'equivalent' ? 1 : 0);
  scored.sort((x, y) => rank(y) - rank(x) || (Math.abs(y.score - x.score) > 0.08 ? y.score - x.score : unitCost(x.item) - unitCost(y.item)));
  return scored.slice(0, 8);
}
const unitCost = (c) => (c.qty ? c.price / c.qty.q : c.price);

const cache = new Map();

function optionsFor(it) {
  const idx = build();
  if (!cache.has(it.id)) {
    if (cache.size > 5000) cache.clear();
    const out = [];
    for (const [store, bucket] of idx.byStore) if (store !== it.store) out.push([store, optionsIn(it, bucket)]);
    cache.set(it.id, out);
  }
  return cache.get(it.id);
}

/** Equivalentes de un producto en los otros súper (formato que ya usan la web y la app). */
export function compareProduct(id, qty = 1) {
  const idx = build();
  const it = idx.byId.get(String(id));
  if (!it) return null;
  const matches = [];
  for (const [store, opts] of optionsFor(it)) {
    if (!opts.length) continue;
    const decorate = (o) => ({ ...view(o.item), match: o.match, ...packsFor(it, o.item, qty) });
    const [best, ...rest] = opts.map(decorate);
    matches.push({ store, match: best.match, product: best, alternatives: rest });
  }
  return { id: it.id, store: it.store, per: perUnit(it), matches };
}

/**
 * Para una línea de la cesta: cuánto costaría lo mismo en cada opción y cuánto ahorras.
 * El ahorro se calcula para la cantidad que llevas; si los envases no cuadran, por precio por kilo o litro.
 */
function costOption(self, o, qty) {
  const { packs, comparable } = packsFor(self, o.item, qty);
  const mine = self.price * qty;
  let cost = o.item.price * packs;
  let approx = false;
  if (o.match === 'same') cost = o.item.price * qty;
  else if (comparable) {
    const need = self.qty.q * qty;
    const got = o.item.qty.q * packs;
    // Si con los envases enteros te llevas bastante más (o menos), el ahorro justo es por kilo o litro
    if (Math.abs(got - need) / need > 0.15) { cost = unitCost(o.item) * need; approx = true; }
  } else approx = true;
  return { packs, cost: Math.round(cost * 100) / 100, saving: Math.round((mine - cost) * 100) / 100, approx, comparable: comparable || o.match === 'same' };
}

/** Alternativas de cada producto de la cesta en los otros súper, ordenadas por ahorro. */
export function basketAlternatives(items) {
  const idx = build();
  const out = {};
  for (const line of items.slice(0, 300)) {
    const it = idx.byId.get(String(line.id));
    if (!it) continue;
    const qty = Math.max(1, Math.min(99, Number(line.qty) || 1));
    const options = [];
    for (const [, opts] of optionsFor(it)) {
      for (const o of opts.slice(0, 4)) {
        const c = costOption(it, o, qty);
        if (o.match === 'similar' && !c.comparable) continue;
        options.push({ ...view(o.item), match: o.match, ...c });
      }
    }
    options.sort((a, b) => (a.match === 'similar') - (b.match === 'similar') || b.saving - a.saving);
    out[it.id] = { per: perUnit(it), options: options.slice(0, 9) };
  }
  return out;
}

/** Compatibilidad con las apps que ya están instaladas: la mejor alternativa más barata de cada producto. */
export function cheaperElsewhere(ids) {
  const alts = basketAlternatives(ids.map((id) => ({ id, qty: 1 })));
  const out = {};
  for (const [id, a] of Object.entries(alts)) {
    const best = a.options.filter((o) => o.match !== 'similar' && o.saving >= 0.05).sort((x, y) => y.saving - x.saving)[0];
    if (best) out[id] = { store: best.store, match: best.match === 'same' ? 'same' : 'similar', product: best, alternatives: [], saving: best.saving, approx: best.approx };
  }
  return out;
}

/**
 * Coste aproximado de una cesta en cada súper: de cada línea, el mismo producto o el equivalente
 * más parecido, con los envases que hagan falta para la misma cantidad.
 */
export function basketCost(items) {
  const idx = build();
  const rows = new Map(STORES.map((s) => [s.id, { id: s.id, name: s.name, hue: s.hue, total: 0, matched: 0, exact: 0, similar: 0, equivalent: 0, missing: [], comparedTotal: 0 }]));
  const totalItems = items.length;
  const ownCompared = new Map(STORES.map((s) => [s.id, 0]));

  for (const line of items) {
    const it = idx.byId.get(String(line.id));
    if (!it) continue;
    const qty = Math.max(1, Math.min(99, Number(line.qty) || 1));
    const byStore = new Map(optionsFor(it));
    for (const store of STORES) {
      const row = rows.get(store.id);
      if (!idx.byStore.get(store.id)?.items.length) continue;
      if (store.id === it.store) {
        row.total += it.price * qty;
        row.matched++;
        row.exact++;
        continue;
      }
      // Para estimar el coste vale el más parecido, no el más barato: si no, toda cesta "sale" más barata
      const o = (byStore.get(store.id) ?? []).filter((x) => x.match !== 'similar').sort((x, y) => y.score - x.score)[0];
      if (!o) { row.missing.push(it.name); continue; }
      const c = costOption(it, o, qty);
      row.total += c.cost;
      row.matched++;
      // Lo que cuesta esa misma parte de la cesta en el súper de origen, para comparar con justicia
      ownCompared.set(store.id, ownCompared.get(store.id) + it.price * qty);
      if (o.match === 'same') row.exact++;
      else row.equivalent++;
    }
  }

  return {
    totalItems,
    stores: [...rows.values()]
      .filter((r) => idx.byStore.get(r.id)?.items.length && r.matched > 0)
      .map((r) => ({
        ...r,
        similar: r.equivalent,
        total: Math.round(r.total * 100) / 100,
        // Lo que costarían en tu súper solo los productos que sí se han encontrado en este
        comparedTotal: Math.round(ownCompared.get(r.id) * 100) / 100,
        coverage: totalItems ? Math.round((r.matched / totalItems) * 100) : 0,
        missing: r.missing.slice(0, 12),
        missingCount: r.missing.length,
      })),
  };
}

// Para las pruebas del comparador
export const _internals = { core, words, variantsOf, quantity, measure, build };
