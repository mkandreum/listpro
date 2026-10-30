import crypto from 'node:crypto';
import { GoogleGenAI, Type } from '@google/genai';
import { db, getMeta } from './db.js';
import { sectionIdFor } from './taxonomy.js';
import { DEFAULT_STORE, getStore, isStore, metaKey, STORES } from './stores.js';

// Planes de recetas (desayunos, comidas, meriendas y cenas) con Gemini, sobre el catálogo real del súper elegido.
// La IA elige recetas y productos; los precios, la lista de la compra y el coste
// los calcula siempre el servidor con los precios de la base de datos.
//
// Para que no repita platos, el servidor reparte antes un "guion" de la semana: a cada receta le
// asigna una base (pollo, lentejas, merluza...), un formato (guiso, horno, wok...) y un estilo de
// cocina, sin repetir en días seguidos ni entre la comida y la cena del mismo día. Cada tipo de
// comida se pide en paralelo con su parte del presupuesto.

// Mismo patrón que Puchero: cadena de modelos configurable desde Coolify.
const MODELS = [...new Set([
  process.env.GEMINI_MODEL_PLAN || 'gemini-3-flash-preview',
  process.env.GEMINI_MODEL_FAST || 'gemini-3.5-flash-lite',
])];
const apiKey = process.env.GEMINI_API_KEY;
const ai = apiKey ? new GoogleGenAI({ apiKey }) : null;
// Solo para desarrollo: monta un plan con productos reales sin llamar a la IA
const MOCK = process.env.PLANNER_MOCK === '1';
if (!ai && !MOCK) console.warn('[planner] GEMINI_API_KEY no configurada: los planes de recetas no estarán disponibles.');

export const plannerEnabled = () => ai !== null || MOCK;

db.exec(`
  CREATE TABLE IF NOT EXISTS plans (
    id TEXT PRIMARY KEY,
    created_at TEXT NOT NULL,
    prefs TEXT NOT NULL,
    plan TEXT NOT NULL
  );
`);

// ───────────── Preferencias ─────────────

export const DIETS = {
  omnivora: 'Come de todo',
  vegetariana: 'Vegetariana (sin carne ni pescado; huevos y lácteos sí)',
  vegana: 'Vegana (nada de origen animal)',
  pescetariana: 'Pescetariana (pescado sí, carne no)',
  sin_gluten: 'Sin gluten (celiaquía)',
  sin_lactosa: 'Sin lactosa',
  alta_proteina: 'Alta en proteína',
  baja_carbohidratos: 'Baja en hidratos de carbono',
  ligera: 'Ligera, pocas calorías',
  halal: 'Halal (sin cerdo ni alcohol)',
};

export const APPLIANCES = {
  fuego: 'Fuego (vitro, inducción o gas)',
  horno: 'Horno',
  microondas: 'Microondas',
  freidora_aire: 'Freidora de aire',
  olla_rapida: 'Olla rápida u olla exprés',
  robot: 'Robot de cocina (tipo Thermomix)',
  sandwichera: 'Sandwichera o plancha eléctrica',
  batidora: 'Batidora',
};

// Orden del día. `share` reparte el presupuesto; `guide` le dice a la IA qué se espera de cada una.
export const MEALS = {
  desayuno: {
    label: 'Desayuno', plural: 'desayunos', these: 'estos', share: 0.14, kcal: '300-450', maxMinutes: 15,
    guide: 'Desayunos de diario: rápidos (10 minutos o menos entre semana; el fin de semana puede ser algo más elaborado), que se puedan hacer medio dormido. Combina hidrato de calidad (pan, avena, fruta), proteína (huevo, lácteo, embutido magro, legumbre untable) y algo de fruta. Nada de platos de cena.',
  },
  comida: {
    label: 'Comida', plural: 'comidas', these: 'estas', share: 0.42, kcal: '550-800', maxMinutes: null,
    guide: 'La comida principal del día, al estilo español: plato completo y saciante (proteína + verdura + hidrato), o primero y segundo sencillos. Se puede cocinar el doble y llevar en táper al trabajo si encaja.',
  },
  merienda: {
    label: 'Merienda', plural: 'meriendas', these: 'estas', share: 0.09, kcal: '150-300', maxMinutes: 10,
    guide: 'Meriendas pequeñas y rápidas (10 minutos como mucho, mejor 5), dulces o saladas, para media tarde. Raciones pequeñas. Nada de platos completos.',
  },
  cena: {
    label: 'Cena', plural: 'cenas', these: 'estas', share: 0.35, kcal: '400-650', maxMinutes: null,
    guide: 'Cenas más ligeras que la comida: mucha verdura, proteína y poca fritura. Que se hagan en el tiempo indicado un día de diario.',
  },
};
const MEAL_ORDER = Object.keys(MEALS);

const clampInt = (v, lo, hi, def) => (Number.isFinite(Number(v)) ? Math.min(hi, Math.max(lo, Math.round(Number(v)))) : def);
const cleanList = (v, allowed) => (Array.isArray(v) ? [...new Set(v.filter((x) => typeof x === 'string' && (!allowed || x in allowed)))] : []);
const cleanText = (v, max = 300) => (typeof v === 'string' ? v.replace(/\s+/g, ' ').trim().slice(0, max) : '');

/** Normaliza lo que manda el cliente: nunca se fía de él para nada que llegue al prompt. */
export function normalizePrefs(p = {}) {
  const appliances = cleanList(p.appliances, APPLIANCES);
  const meals = cleanList(p.meals, MEALS).sort((a, b) => MEAL_ORDER.indexOf(a) - MEAL_ORDER.indexOf(b));
  // `nights` es el nombre antiguo de `days` (clientes de antes de las recetas)
  const days = clampInt(p.days ?? p.nights, 1, 7, 5);
  return {
    budget: clampInt(p.budget, 10, 600, 50),
    people: clampInt(p.people, 1, 8, 2),
    days,
    nights: days,
    meals: meals.length ? meals : ['cena'],
    maxMinutes: clampInt(p.maxMinutes, 10, 120, 40),
    diets: cleanList(p.diets, DIETS),
    appliances: appliances.length ? appliances : ['fuego'],
    likes: cleanText(p.likes),
    dislikes: cleanText(p.dislikes),
    allergies: cleanText(p.allergies),
    pantry: cleanText(p.pantry, 500),
    store: isStore(p.store) ? p.store : DEFAULT_STORE,
  };
}

const cleanAvoid = (v) => (Array.isArray(v) ? [...new Set(v.map((x) => cleanText(x, 80)).filter(Boolean))].slice(0, 40) : []);

// ───────────── Catálogo para la IA ─────────────

// Subcategorías que no sirven para cocinar una comida o cena (dulces, desayuno, helados, infantil...).
const NOT_FOR_MAINS = new Set([
  59, 64, 65, 66, 68, 69, 71, 78, 79, 80, 81, 83, 84, 86, 88, 89, 90, 92, 95, 97, 833,
  98, 99, 100, 143, 105, 106, 107, 108, 110, 111, 132, 152, 154, 155, 216,
]);
// Para desayunos y meriendas sí valen bollería, cereales, café o zumos; solo se quitan velas, chuches, helados e infantil.
const NOT_FOR_SNACKS = new Set([71, 833, 95, 97, 107, 111]);
// Lo mismo en Consum, por sus categorías: turrones y dulces, desayuno y café, bollería dulce, tartas y helados
const CONSUM_NOT_FOR_MAINS = new Set([1005298, 1001852, 1005021, 1005031, 1001784]);
const CONSUM_NOT_FOR_SNACKS = new Set([1001784]);
const FOOD_SECTIONS = new Set(['frescos', 'lacteos', 'despensa', 'congelados']);
// Marca propia de cada súper, para que la IA la proponga cuando dé igual la marca
const OWN_BRAND = { mercadona: 'Hacendado', consum: 'Consum' };
const LIGHT_MEALS = new Set(['desayuno', 'merienda']);

const catalogCaches = new Map(); // súper → { version, mains, snacks, byId }

function plannerCatalog(store) {
  const version = getMeta(metaKey(store, 'catalog_version')) ?? '0';
  const cached = catalogCaches.get(store);
  if (cached?.version === version) return cached;
  const rows = db.prepare('SELECT id, name, size, price, ref_price, ref_format, category_id, subcategory_id, image FROM products WHERE active = 1 AND store = ? ORDER BY position').all(store);
  const notMains = (r) => (store === 'consum' ? CONSUM_NOT_FOR_MAINS.has(r.category_id) : NOT_FOR_MAINS.has(r.subcategory_id));
  const notSnacks = (r) => (store === 'consum' ? CONSUM_NOT_FOR_SNACKS.has(r.category_id) : NOT_FOR_SNACKS.has(r.subcategory_id));
  const byId = new Map();
  const mains = [];
  const snacks = [];
  for (const r of rows) {
    byId.set(r.id, r);
    if (!FOOD_SECTIONS.has(sectionIdFor(r.category_id))) continue;
    const line = `${r.id}|${r.name}|${r.size}|${r.price.toFixed(2)}`;
    if (!notMains(r)) mains.push(line);
    if (!notSnacks(r)) snacks.push(line);
  }
  const built = { version, mains, snacks, byId };
  catalogCaches.set(store, built);
  return built;
}

// ───────────── Guion de variedad ─────────────

const DAYS = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo'];

// Bases de comidas y cenas. `tags` sirve para quitarlas según la dieta.
const BASES = [
  { id: 'pollo', text: 'pollo', tags: ['carne'] },
  { id: 'pavo', text: 'pavo', tags: ['carne'] },
  { id: 'ternera', text: 'ternera', tags: ['carne'] },
  { id: 'cerdo', text: 'cerdo (lomo, solomillo, costilla o secreto)', tags: ['carne', 'cerdo'] },
  { id: 'picada', text: 'carne picada', tags: ['carne'] },
  { id: 'merluza', text: 'pescado blanco (merluza, bacalao, lenguado o panga)', tags: ['pescado'] },
  { id: 'salmon', text: 'pescado azul (salmón, caballa, sardinas, boquerón o bonito)', tags: ['pescado'] },
  { id: 'atun', text: 'atún (fresco o en conserva)', tags: ['pescado'] },
  { id: 'marisco', text: 'marisco o cefalópodos (gambas, mejillones, calamar o sepia)', tags: ['pescado', 'marisco'] },
  { id: 'huevo', text: 'huevos', tags: ['huevo'] },
  { id: 'lentejas', text: 'lentejas', tags: ['legumbre'] },
  { id: 'garbanzos', text: 'garbanzos', tags: ['legumbre'] },
  { id: 'alubias', text: 'alubias', tags: ['legumbre'] },
  { id: 'tofu', text: 'tofu, tempeh o soja texturizada', tags: ['vegetal'] },
  { id: 'seitan', text: 'seitán o proteína vegetal tipo Heura', tags: ['vegetal', 'gluten'] },
  { id: 'queso', text: 'queso o lácteo protagonista (queso fresco, mozzarella, ricotta, feta)', tags: ['lacteo'] },
];
const DIET_EXCLUDES = {
  vegetariana: ['carne', 'pescado'],
  vegana: ['carne', 'pescado', 'huevo', 'lacteo'],
  pescetariana: ['carne'],
  halal: ['cerdo'],
  sin_lactosa: ['lacteo'],
  sin_gluten: ['gluten'],
};
// En dieta de todo, las bases vegetales salen menos (pero salen).
const OMNI_SKIP = new Set(['seitan', 'tofu']);

// Formatos. `any`: hace falta al menos uno de esos electrodomésticos.
const MAIN_FORMATS = {
  comida: [
    { t: 'guiso o plato de cuchara', any: ['fuego', 'olla_rapida', 'robot'] },
    { t: 'arroz (meloso, seco tipo paella o salteado)', any: ['fuego', 'robot', 'olla_rapida'], styles: ['asiática (arroz frito o salteado)', 'italiana (risotto)'] },
    { t: 'pasta o fideos', any: ['fuego', 'robot'], styles: ['italiana', 'italiana', 'asiática (fideos salteados)'] },
    { t: 'al horno con guarnición', any: ['horno'] },
    { t: 'a la plancha con guarnición de verduras', any: ['fuego', 'sandwichera'] },
    { t: 'salteado al wok', any: ['fuego'], styles: ['china', 'tailandesa', 'japonesa', 'coreana'], always: true },
    { t: 'ensalada completa o bowl templado', any: [], styles: ['griega', 'libanesa', 'poke o bowl japonés', 'mexicana'] },
    { t: 'estofado o curry con salsa', any: ['fuego', 'olla_rapida', 'robot'], styles: ['india suave (curry)', 'tailandesa (curry)', 'marroquí (tajín)'] },
    { t: 'en freidora de aire con acompañamiento', any: ['freidora_aire'] },
    { t: 'al vapor o papillote en el microondas', any: ['microondas'] },
  ],
  cena: [
    { t: 'crema o sopa de verduras con un segundo sencillo', any: ['fuego', 'robot', 'olla_rapida'] },
    { t: 'ensalada templada o bowl', any: [], styles: ['griega', 'libanesa', 'poke o bowl japonés', 'mexicana'] },
    { t: 'a la plancha con verduras', any: ['fuego', 'sandwichera'] },
    { t: 'al horno o en papillote', any: ['horno', 'microondas'] },
    { t: 'salteado rápido', any: ['fuego'], styles: ['china', 'tailandesa', 'japonesa'] },
    { t: 'tortilla, revuelto o frittata con relleno', any: ['fuego'] },
    { t: 'wrap, fajitas o tacos', any: ['fuego', 'sandwichera'], styles: ['mexicana', 'mexicana', 'libanesa (shawarma o falafel)', 'griega (pita)'], always: true },
    { t: 'tosta, coca o pizza casera', any: ['horno', 'freidora_aire', 'sandwichera'], styles: ['italiana'] },
    { t: 'verduras rellenas', any: ['horno', 'freidora_aire', 'microondas'] },
    { t: 'brochetas o pinchos', any: ['fuego', 'horno', 'freidora_aire', 'sandwichera'], styles: ['moruna', 'griega (souvlaki)', 'japonesa (yakitori)'] },
    { t: 'hamburguesa o albóndigas caseras con ensalada', any: ['fuego', 'horno', 'freidora_aire'], styles: ['americana', 'griega (keftedes)', 'sueca o italiana (albóndigas)'] },
    { t: 'en freidora de aire con verduras', any: ['freidora_aire'] },
  ],
};
const LIGHT_FORMATS = {
  desayuno: [
    { t: 'tostada salada (tomate, aguacate, jamón, queso, huevo...)', any: [] },
    { t: 'tostada o pan dulce (fruta, crema de cacahuete, requesón...)', any: [] },
    { t: 'porridge o gachas de avena', any: ['fuego', 'microondas'] },
    { t: 'bol de yogur o skyr con fruta y cereales', any: [] },
    { t: 'huevos (revueltos, a la plancha, tortilla francesa o shakshuka exprés)', any: ['fuego', 'microondas'] },
    { t: 'tortitas o pancakes de avena', any: ['fuego', 'sandwichera'] },
    { t: 'batido o smoothie bowl', any: ['batidora', 'robot'] },
    { t: 'sándwich o bocadillo pequeño', any: [] },
    { t: 'avena remojada de la noche anterior (overnight oats)', any: [] },
    { t: 'bol de fruta con queso fresco y frutos secos', any: [] },
  ],
  merienda: [
    { t: 'fruta con yogur o kéfir', any: [] },
    { t: 'tostada pequeña salada', any: [] },
    { t: 'hummus o crema de legumbre con palitos de verdura', any: [] },
    { t: 'fruta y frutos secos', any: [] },
    { t: 'batido de fruta', any: ['batidora', 'robot'] },
    { t: 'mini sándwich', any: [] },
    { t: 'queso fresco con membrillo, miel o fruta', any: [] },
    { t: 'palomitas caseras o tortitas de maíz con algo', any: ['microondas', 'fuego'] },
    { t: 'bizcocho de taza o galletas de avena exprés', any: ['microondas', 'freidora_aire', 'horno'] },
    { t: 'tortitas de arroz con untable', any: [] },
  ],
};
// Formatos que no casan con una base: nadie hace brochetas de lentejas ni guiso de huevo.
// Se comparan por trozos del texto del formato.
const CLASHES = {
  legumbre: ['plancha', 'brochetas', 'tortilla', 'papillote', 'freidora', 'pasta', 'tosta', 'sueca', 'keftedes', 'risotto', 'wok'],
  huevo: ['brochetas', 'hamburguesa', 'guiso', 'estofado', 'papillote', 'plancha', 'freidora'],
  marisco: ['hamburguesa', 'rellenas', 'freidora', 'estofado', 'shawarma', 'souvlaki', 'yakitori', 'albóndigas', 'keftedes', 'moruna'],
  pescado: ['hamburguesa', 'tortilla', 'shawarma', 'souvlaki', 'yakitori', 'keftedes', 'moruna'],
  lacteo: ['guiso', 'estofado', 'brochetas', 'wok', 'plancha', 'papillote', 'hamburguesa', 'salteado'],
  picada: ['plancha', 'papillote', 'tortilla', 'brochetas', 'wok'],
  vegetal: ['papillote', 'tortilla'],
};
const clashes = (baseText, format) => {
  const b = BASES.find((x) => x.text === baseText);
  if (!b) return false;
  const keys = [b.id, ...b.tags];
  return keys.some((k) => (CLASHES[k] ?? []).some((w) => format.includes(w)));
};

// El estilo depende del formato (el wok es asiático, las fajitas mexicanas...). La cocina de casa pesa más:
// solo cuando el formato tiene estilos propios se usa uno, y aun así la mitad de las veces sale casera.
const HOME_CUISINES = ['casera española', 'mediterránea', 'de mercado, sencilla y de temporada'];
const FORMAT_STYLES = new Map(Object.values(MAIN_FORMATS).flat().map((f) => [f.t, f]));

const rand = () => crypto.randomInt(0, 1_000_000) / 1_000_000;
function shuffle(a) {
  const x = [...a];
  for (let i = x.length - 1; i > 0; i--) { const j = crypto.randomInt(0, i + 1); [x[i], x[j]] = [x[j], x[i]]; }
  return x;
}
const canCook = (f, appliances) => !f.any.length || f.any.some((a) => appliances.includes(a));

function basesFor(prefs) {
  const lower = `${prefs.dislikes} ${prefs.allergies}`.toLowerCase();
  const out = new Set(prefs.diets.flatMap((d) => DIET_EXCLUDES[d] ?? []));
  if (/marisco|crust[aá]ceo|molusco/.test(lower)) out.add('marisco');
  if (/pescado/.test(lower)) out.add('pescado');
  if (/huevo/.test(lower)) out.add('huevo');
  if (/l[aá]cteo|lactosa|leche|queso/.test(lower)) out.add('lacteo');
  if (/gluten|trigo/.test(lower)) out.add('gluten');
  const omni = !prefs.diets.some((d) => ['vegetariana', 'vegana'].includes(d));
  let list = BASES.filter((b) => !b.tags.some((t) => out.has(t)) && !lower.includes(b.id) && !(omni && OMNI_SKIP.has(b.id)));
  if (list.length < 3) list = BASES.filter((b) => !b.tags.some((t) => out.has(t)));
  return list.length ? list : BASES.filter((b) => b.tags.includes('legumbre'));
}

/** Elige el menos usado que no choque; a igualdad, al azar. */
function pick(pool, uses, blocked) {
  const options = pool.filter((x) => !blocked.has(x)).length ? pool.filter((x) => !blocked.has(x)) : pool;
  return options.map((x) => [x, (uses.get(x) ?? 0) + rand() * 0.9]).sort((a, b) => a[1] - b[1])[0][0];
}

/**
 * Guion de la semana: para cada tipo de comida, una ficha por día con base, formato y estilo.
 * Comida y cena del mismo día nunca comparten base, y nada se repite en días seguidos.
 */
export function buildGuide(prefs) {
  const bases = basesFor(prefs).map((b) => b.text);
  const baseUses = new Map();
  const guide = Object.fromEntries(prefs.meals.map((m) => [m, []]));
  const formatPools = Object.fromEntries(prefs.meals.map((m) => {
    const all = (LIGHT_MEALS.has(m) ? LIGHT_FORMATS : MAIN_FORMATS)[m];
    const ok = all.filter((f) => canCook(f, prefs.appliances));
    return [m, (ok.length >= 3 ? ok : all.filter((f) => !f.any.length)).map((f) => f.t)];
  }));
  const formatUses = new Map();
  const cuisineUses = new Map();
  for (let d = 0; d < prefs.days; d++) {
    const today = new Set();
    for (const meal of prefs.meals) {
      const prev = guide[meal][d - 1];
      if (LIGHT_MEALS.has(meal)) {
        const format = pick(formatPools[meal], formatUses, new Set(prev ? [prev.format] : []));
        formatUses.set(format, (formatUses.get(format) ?? 0) + 1);
        guide[meal].push({ day: DAYS[d], format });
        continue;
      }
      // Primero la base y luego un formato que tenga sentido con ella
      const base = pick(bases, baseUses, new Set([...today, ...(prev ? [prev.base] : [])]));
      baseUses.set(base, (baseUses.get(base) ?? 0) + 1);
      today.add(base);
      const fits = formatPools[meal].filter((f) => !clashes(base, f));
      const format = pick(fits.length ? fits : formatPools[meal], formatUses, new Set(prev ? [prev.format] : []));
      formatUses.set(format, (formatUses.get(format) ?? 0) + 1);
      const f = FORMAT_STYLES.get(format);
      const styles = (f?.styles ?? []).filter((s) => !clashes(base, s));
      const world = styles.length && (f.always || rand() < 0.5);
      const cuisine = pick(world ? styles : HOME_CUISINES, cuisineUses, new Set(prev ? [prev.cuisine] : []));
      cuisineUses.set(cuisine, (cuisineUses.get(cuisine) ?? 0) + 1);
      guide[meal].push({ day: DAYS[d], base, format, cuisine });
    }
  }
  return guide;
}

// ───────────── Prompt y esquema ─────────────

function seasonNote(date = new Date()) {
  const m = date.getMonth();
  if (m === 11 || m <= 1) return 'invierno: naranja, mandarina, kiwi, coliflor, brócoli, puerro, alcachofa, acelga, col, calabaza y boniato';
  if (m <= 4) return 'primavera: espárragos, guisantes, habas, fresas, alcachofa, espinacas, zanahoria y nísperos';
  if (m <= 7) return 'verano: tomate, calabacín, pimiento, berenjena, pepino, judía verde, sandía, melón y melocotón';
  return 'otoño: calabaza, setas, boniato, uva, granada, manzana, pera, coliflor y puerro';
}

const euros = (n) => `${n.toFixed(2).replace('.', ',')} €`;

function slotLine(meal, s, i) {
  const weekend = s.day === 'Sábado' || s.day === 'Domingo';
  const parts = LIGHT_MEALS.has(meal)
    ? [`tipo: ${s.format}`]
    : [`base: ${s.base}`, `formato: ${s.format}`, `estilo: ${s.cuisine}`];
  return `${i + 1}. ${s.day}${weekend ? ' (fin de semana)' : ''} · ${parts.join(' · ')}`;
}

function buildPrompt({ meal, slots, prefs, catalog, share, avoid, others, feedback }) {
  const M = MEALS[meal];
  const n = slots.length;
  const diets = prefs.diets.map((d) => DIETS[d]).join('; ') || DIETS.omnivora;
  const appliances = prefs.appliances.map((a) => APPLIANCES[a]).join(', ');
  const minutes = M.maxMinutes ? Math.min(M.maxMinutes, prefs.maxMinutes) : prefs.maxMinutes;
  const who = `${prefs.people} ${prefs.people === 1 ? 'persona' : 'personas'}`;
  const storeName = getStore(prefs.store)?.name ?? 'Mercadona';
  return `Eres el chef y nutricionista de ListPro, una app de lista de la compra con el catálogo real de ${storeName} (España). Cocinas como alguien de casa con mucha mano: platos ricos, variados, realistas y baratos.
Tu tarea: ${n === 1 ? `1 ${M.label.toLowerCase()}` : `${n} ${M.plural}`} para ${who}, siguiendo el guion.

QUÉ ES ESTA COMIDA: ${M.guide} Referencia: ${M.kcal} kcal por ración.

GUION (una receta por línea, en este orden). La base manda; el formato y el estilo son sugerencias para que la semana sea variada:
${slots.map((s, i) => slotLine(meal, s, i)).join('\n')}

RESTRICCIONES (obligatorias):
- Presupuesto para la compra de ${n === 1 ? 'esta receta' : `${M.these} ${M.plural}`}: unos ${euros(share)} como máximo (del total de ${prefs.budget} € de la semana). Cuenta envases enteros, no lo que se gasta en cada plato.
- Dieta: ${diets}.
- Alergias e intolerancias: ${prefs.allergies || 'ninguna'}. No uses NINGÚN producto que las contenga.
- No le gusta: ${prefs.dislikes || 'nada en especial'}.
- Electrodomésticos disponibles: ${appliances}. No uses ningún otro (si no hay horno, nada al horno).
- Tiempo máximo: ${minutes} minutos por receta, incluida la preparación${M.maxMinutes ? '' : ' (el fin de semana puede llegar a 20 minutos más)'}.
- Le gusta: ${prefs.likes || 'comida casera variada'}. Tenlo en cuenta en el estilo, sin repetir.
- Ya tiene en casa: ${prefs.pantry || 'solo lo básico'}. Úsalo primero y NO lo compres.
Se da por hecho que tiene aceite de oliva, sal, pimienta, vinagre y agua: no los compres.

RECETAS CON SENTIDO (lo primero de todo):
- Solo platos que existen de verdad y que cualquiera reconocería: recetas de la cocina española de casa o clásicos conocidos de la cocina del estilo. Nada de inventos, fusiones forzadas ni mezclas raras.
- Si el formato o el estilo de una línea del guion no casa con su base, quédate con la base y cocínala de la forma más natural (y en cocina casera). Mejor un clásico bien hecho que algo original sin sentido.
- Todos los ingredientes de un plato tienen que pegar entre sí y con el estilo (nada de soja en un cocido ni chorizo en un curry).
- Cantidades realistas por persona: 120-150 g de carne o pescado, 70-80 g de pasta o arroz en seco, 60-70 g de legumbre seca (o 200 g cocida), 2 huevos, 150-200 g de verdura.
- Cada receta se tiene que poder hacer solo con sus ingredientes más lo básico: todo lo que aparezca en los pasos está en los ingredientes y todo lo de los ingredientes se usa en los pasos.
- Pasos en orden real de cocina, sin saltos (no sirvas algo que no has cocinado).

VARIEDAD (el usuario se queja de platos repetidos):
- Cada receta debe ser claramente distinta de las demás: otro ingrediente principal, otra técnica, otro sabor dominante.
- No repitas la misma verdura protagonista más de 2 veces, ni la misma guarnición (arroz blanco, patata, ensalada verde, pan) más de 2 veces.
- Alterna salsas y aliños: sofrito, ajo y perejil, pimentón, curry, soja y jengibre, limón y hierbas, pesto, yogur y especias, tomate, mostaza y miel...
- Nombres concretos y apetecibles que digan qué lleva ("Merluza en salsa verde con guisantes", no "Pescado con verduras"). Prohibido usar dos veces la misma palabra principal en los nombres.
${LIGHT_MEALS.has(meal) ? '- Alterna dulce y salado; nada de repetir la misma fruta o el mismo pan dos días seguidos.\n' : ''}- Aprovecha la temporada (${seasonNote()}).
${others.length ? `- Esta semana también hay estas recetas en otras comidas; no las repitas ni hagas versiones parecidas: ${others.join('; ')}.\n` : ''}${avoid.length ? `- El usuario ya ha tenido estas hace poco; NO las repitas ni hagas variantes casi iguales: ${avoid.join('; ')}.\n` : ''}
AHORRO SIN REPETIR:
- Reaprovecha envases entre recetas (un paquete de huevos, una bolsa de espinacas, una malla de cebollas), pero usando el producto de forma distinta en cada plato.
- Marca blanca (${OWN_BRAND[prefs.store] ?? 'la del súper'}) cuando dé igual, legumbre cocida, congelados y verdura de temporada para cuadrar el presupuesto.

CÓMO RESPONDER:
- Usa SOLO productos de la lista, citando su id exacto. Formato: id|nombre|formato|precio en €.
- "fraction": qué parte de UN envase usa la receta para todas las raciones (0.25 = un cuarto del paquete, 2 = dos envases). Sé realista con el tamaño del envase.
- "amount": la cantidad en lenguaje de cocina para todas las raciones ("300 g", "2 dientes", "1/2 bolsa").
- Si un ingrediente es de lo que ya tiene en casa, pon "productId": "" y "fromPantry": true.
- Nutrición POR RACIÓN, estimada a partir de las cantidades reales.
- De 3 a 7 pasos concretos, con tiempos y fuego cuando importe (máximo 25 palabras cada uno), en español de España, tono cercano.
- "tip": un consejo útil de verdad (cómo conservar la sobra, una variante, cómo adelantarlo), nunca obviedades.
- "summary": una frase que resuma ${n === 1 ? 'la receta' : `${M.these} ${M.plural}`} de la semana.
${feedback ? `\nCORRIGE ESTO DE TU INTENTO ANTERIOR: ${feedback}\n` : ''}
CATÁLOGO:
${catalog.join('\n')}`;
}

const NUTRITION = {
  type: Type.OBJECT,
  properties: {
    kcal: { type: Type.NUMBER }, protein: { type: Type.NUMBER }, carbs: { type: Type.NUMBER },
    fat: { type: Type.NUMBER }, fiber: { type: Type.NUMBER }, salt: { type: Type.NUMBER },
  },
  required: ['kcal', 'protein', 'carbs', 'fat', 'fiber', 'salt'],
};

const SCHEMA = {
  type: Type.OBJECT,
  properties: {
    summary: { type: Type.STRING },
    recipes: {
      type: Type.ARRAY,
      items: {
        type: Type.OBJECT,
        properties: {
          name: { type: Type.STRING },
          description: { type: Type.STRING },
          minutes: { type: Type.INTEGER },
          difficulty: { type: Type.STRING, enum: ['fácil', 'media', 'elaborada'] },
          appliances: { type: Type.ARRAY, items: { type: Type.STRING, enum: Object.keys(APPLIANCES) } },
          nutrition: NUTRITION,
          ingredients: {
            type: Type.ARRAY,
            items: {
              type: Type.OBJECT,
              properties: {
                productId: { type: Type.STRING },
                name: { type: Type.STRING },
                amount: { type: Type.STRING },
                fraction: { type: Type.NUMBER },
                fromPantry: { type: Type.BOOLEAN },
              },
              required: ['productId', 'name', 'amount', 'fraction'],
            },
          },
          steps: { type: Type.ARRAY, items: { type: Type.STRING } },
          tip: { type: Type.STRING },
        },
        required: ['name', 'description', 'minutes', 'difficulty', 'appliances', 'nutrition', 'ingredients', 'steps'],
      },
    },
  },
  required: ['summary', 'recipes'],
};

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

async function generate(prompt) {
  let last = null;
  for (const model of MODELS) {
    let config = {
      responseMimeType: 'application/json',
      responseSchema: SCHEMA,
      temperature: 0.95,
      maxOutputTokens: 14000,
      thinkingConfig: { thinkingLevel: 'LOW' },
    };
    for (let attempt = 1; attempt <= 3; attempt++) {
      try {
        const res = await ai.models.generateContent({ model, contents: prompt, config });
        return JSON.parse(res.text ?? '');
      } catch (e) {
        last = e;
        const msg = String(e?.message ?? '');
        if (config.thinkingConfig && /thinking|not supported|unsupported|invalid argument/i.test(msg)) {
          const { thinkingConfig: _drop, ...rest } = config;
          config = rest;
          continue;
        }
        if (/503|unavailable|high demand|429/i.test(msg) && attempt < 3) { await sleep(600 * attempt); continue; }
        console.warn('[planner] modelo falló', model, msg.slice(0, 160));
        break;
      }
    }
  }
  throw last ?? new Error('Sin respuesta de la IA');
}

// ───────────── Validación y costes (con precios reales) ─────────────

const round2 = (n) => Math.round(n * 100) / 100;
const num = (v, lo, hi) => (Number.isFinite(Number(v)) ? Math.min(hi, Math.max(lo, Number(v))) : lo);
const packsFor = (fraction) => Math.max(1, Math.ceil(fraction - 0.05));

/** Convierte una receta de la IA en una con precios reales. */
function finalizeRecipe(r, slot, meal, prefs, byId, warnings) {
  const ingredients = [];
  let cost = 0;
  for (const ing of Array.isArray(r?.ingredients) ? r.ingredients : []) {
    const name = cleanText(ing.name, 80);
    if (!name) continue;
    const product = ing.productId ? byId.get(String(ing.productId)) : null;
    if (ing.productId && !product) { warnings.push(`«${name}» no está en el catálogo y se ha quitado.`); continue; }
    const fraction = product ? num(ing.fraction, 0.02, 12) : 0;
    if (product) cost += fraction * product.price;
    ingredients.push({
      productId: product?.id ?? null,
      name: product?.name ?? name,
      amount: cleanText(ing.amount, 40),
      fraction: round2(fraction),
      fromPantry: !product,
      image: product?.image ?? null,
      price: product?.price ?? null,
      size: product?.size ?? null,
    });
  }
  const n = r?.nutrition ?? {};
  const M = MEALS[meal];
  return {
    day: slot.day,
    meal,
    base: slot.base ?? slot.format,
    name: cleanText(r?.name, 80) || `${M.label} del ${slot.day.toLowerCase()}`,
    description: cleanText(r?.description, 220),
    minutes: clampInt(r?.minutes, 2, 180, M.maxMinutes ?? prefs.maxMinutes),
    difficulty: ['fácil', 'media', 'elaborada'].includes(r?.difficulty) ? r.difficulty : 'fácil',
    appliances: cleanList(r?.appliances, APPLIANCES),
    servings: prefs.people,
    nutrition: {
      kcal: Math.round(num(n.kcal, 0, 3000)), protein: Math.round(num(n.protein, 0, 300)),
      carbs: Math.round(num(n.carbs, 0, 500)), fat: Math.round(num(n.fat, 0, 300)),
      fiber: Math.round(num(n.fiber, 0, 100)), salt: round2(num(n.salt, 0, 20)),
    },
    ingredients,
    steps: (Array.isArray(r?.steps) ? r.steps : []).map((s) => cleanText(s, 240)).filter(Boolean).slice(0, 12),
    tip: cleanText(r?.tip, 220),
    cost: round2(cost),
    costPerServing: round2(cost / prefs.people),
  };
}

const complete = (r) => r.ingredients.length >= 2 && r.steps.length >= 2;

/** Lo que se compra para un conjunto de recetas: envases enteros, sumando lo compartido antes de redondear. */
function shoppingFor(recipes, byId) {
  const use = new Map();
  for (const r of recipes) for (const i of r.ingredients) if (i.productId && byId.has(i.productId)) use.set(i.productId, (use.get(i.productId) ?? 0) + i.fraction);
  return [...use.entries()].map(([id, fraction]) => {
    const p = byId.get(id);
    const packs = packsFor(fraction);
    const users = recipes.filter((r) => r.ingredients.some((x) => x.productId === id));
    return {
      productId: id, name: p.name, size: p.size, image: p.image, price: p.price, packs, cost: round2(packs * p.price),
      usedIn: [...new Set(users.map((r) => r.day))],
      usedFor: users.map((r) => ({ day: r.day, meal: r.meal })),
    };
  }).sort((a, b) => (a.usedFor.length === b.usedFor.length ? b.cost - a.cost : b.usedFor.length - a.usedFor.length));
}
const totalOf = (items) => round2(items.reduce((a, s) => a + s.cost, 0));

/** Junta las recetas en un plan: lista de la compra, totales y resúmenes por comida. */
function assemble(recipes, prefs, byId, summaries, warnings) {
  const ordered = [...recipes].sort((a, b) => (DAYS.indexOf(a.day) - DAYS.indexOf(b.day)) || (MEAL_ORDER.indexOf(a.meal) - MEAL_ORDER.indexOf(b.meal)));
  const shopping = shoppingFor(ordered, byId);
  const shoppingTotal = totalOf(shopping);
  const servings = ordered.length * prefs.people || 1;
  const avg = (xs) => (xs.length ? xs.reduce((a, b) => a + b, 0) / xs.length : 0);
  const sections = Object.fromEntries(prefs.meals.map((m) => {
    const rs = ordered.filter((r) => r.meal === m);
    return [m, {
      summary: summaries[m] ?? '',
      count: rs.length,
      kcalAvg: Math.round(avg(rs.map((r) => r.nutrition.kcal))),
      costPerServing: round2(avg(rs.map((r) => r.costPerServing))),
    }];
  }));
  const days = new Set(ordered.map((r) => r.day)).size || 1;
  const M = MEALS[prefs.meals[0]];
  return {
    title: prefs.meals.length === 1 ? `Tus ${M.plural} de la semana` : 'Tu semana de recetas',
    summary: prefs.meals.map((m) => summaries[m]).filter(Boolean).join(' '),
    sections,
    recipes: ordered,
    shopping,
    totals: {
      shopping: shoppingTotal,
      consumed: round2(ordered.reduce((a, r) => a + r.cost, 0)),
      perServing: round2(shoppingTotal / servings),
      kcalAvg: Math.round(avg(ordered.map((r) => r.nutrition.kcal))),
      kcalDay: Math.round(ordered.reduce((a, r) => a + r.nutrition.kcal, 0) / days),
      proteinAvg: Math.round(avg(ordered.map((r) => r.nutrition.protein))),
      budget: prefs.budget,
      overBudget: shoppingTotal > prefs.budget,
    },
    warnings: [...new Set(warnings)].slice(0, 6),
  };
}

// ───────────── Generación ─────────────

export class PlannerError extends Error {
  constructor(message, status = 500) { super(message); this.status = status; }
}

/** Presupuesto de cada comida: su peso relativo entre las elegidas. */
function shares(prefs) {
  const w = prefs.meals.reduce((a, m) => a + MEALS[m].share, 0);
  return Object.fromEntries(prefs.meals.map((m) => [m, round2((prefs.budget * MEALS[m].share) / w)]));
}

/** Pide a la IA las recetas de una comida y las deja con precios reales. */
async function cookMeal({ meal, slots, prefs, cat, share, avoid, others, feedback, warnings }) {
  const catalog = LIGHT_MEALS.has(meal) ? cat.snacks : cat.mains;
  const raw = MOCK ? mockMeal(meal, slots, catalog) : await generate(buildPrompt({ meal, slots, prefs, catalog, share, avoid, others, feedback }));
  const list = Array.isArray(raw?.recipes) ? raw.recipes : [];
  const recipes = slots.map((slot, i) => (list[i] ? finalizeRecipe(list[i], slot, meal, prefs, cat.byId, warnings) : null)).filter(Boolean);
  return { recipes, summary: cleanText(raw?.summary, 240), cost: totalOf(shoppingFor(recipes, cat.byId)) };
}

export async function createPlan(rawPrefs, rawAvoid = []) {
  if (!ai && !MOCK) throw new PlannerError('El planificador no está disponible ahora mismo.', 503);
  const prefs = normalizePrefs(rawPrefs);
  const avoid = cleanAvoid(rawAvoid);
  const cat = plannerCatalog(prefs.store);
  if (cat.mains.length < 100) throw new PlannerError('El catálogo aún se está cargando. Prueba en un minuto.', 503);

  const guide = buildGuide(prefs);
  const budgets = shares(prefs);
  const warnings = [];
  // Cada comida va en paralelo; a cada una se le cuentan las bases de las otras para que no se pisen.
  const othersFor = (meal) => prefs.meals.filter((m) => m !== meal && !LIGHT_MEALS.has(m))
    .flatMap((m) => guide[m].map((s) => `${s.day.toLowerCase()} en la ${MEALS[m].label.toLowerCase()}: ${s.base}`));
  const results = {};
  const settled = await Promise.allSettled(prefs.meals.map((meal) => cookMeal({
    meal, slots: guide[meal], prefs, cat, share: budgets[meal], avoid, others: othersFor(meal), warnings,
  })));
  settled.forEach((s, i) => { if (s.status === 'fulfilled') results[prefs.meals[i]] = s.value; else console.warn('[planner]', prefs.meals[i], s.reason?.message ?? s.reason); });

  // Segunda vuelta solo para lo que salió cojo o muy caro, explicándole el fallo.
  const total = () => totalOf(shoppingFor(Object.values(results).flatMap((r) => r.recipes), cat.byId));
  const overall = total() - prefs.budget;
  const retry = prefs.meals.filter((m) => {
    const r = results[m];
    if (!r || r.recipes.length < guide[m].length || !r.recipes.every(complete)) return true;
    return overall > prefs.budget * 0.03 && r.cost > budgets[m] * 1.08;
  });
  if (retry.length) {
    const again = await Promise.allSettled(retry.map((meal) => {
      const prev = results[meal];
      const names = Object.entries(results).filter(([m]) => m !== meal).flatMap(([, r]) => r.recipes.map((x) => x.name));
      const feedback = [
        prev && prev.cost > budgets[meal] ? `La compra de estas recetas sale a ${euros(prev.cost)} y el máximo es ${euros(budgets[meal])}: baja ${euros(prev.cost - budgets[meal])} con productos más baratos (${OWN_BRAND[prefs.store] ?? 'marca blanca'}, legumbres, huevos, verdura de temporada, congelados) y reaprovechando más.` : '',
        !prev || prev.recipes.length < guide[meal].length || !prev.recipes.every(complete) ? `Faltan recetas o alguna no tiene ingredientes o pasos suficientes: necesito ${guide[meal].length} recetas completas.` : '',
      ].filter(Boolean).join(' ');
      return cookMeal({ meal, slots: guide[meal], prefs, cat, share: budgets[meal], avoid: [...avoid, ...names].slice(0, 50), others: othersFor(meal), feedback, warnings: [] });
    }));
    again.forEach((s, i) => {
      if (s.status !== 'fulfilled') return;
      const meal = retry[i];
      const prev = results[meal];
      const score = (r) => (r ? r.recipes.filter(complete).length * 1000 - r.cost : -Infinity);
      if (score(s.value) > score(prev)) results[meal] = s.value;
    });
  }

  const recipes = Object.values(results).flatMap((r) => r.recipes);
  if (!recipes.length) throw new PlannerError('No hemos podido crear el plan. Prueba otra vez.', 502);
  const missing = prefs.meals.filter((m) => !results[m]?.recipes.length);
  if (missing.length) warnings.unshift(`No hemos podido preparar ${missing.map((m) => MEALS[m].plural).join(' ni ')} esta vez. Prueba con «Otro plan».`);
  const summaries = Object.fromEntries(Object.entries(results).map(([m, r]) => [m, r.summary]));
  return save(prefs, assemble(recipes, prefs, cat.byId, summaries, warnings));
}

function save(prefs, plan) {
  if (plan.totals.overBudget) {
    plan.warnings.unshift(`La compra se pasa ${round2(plan.totals.shopping - prefs.budget).toFixed(2).replace('.', ',')} € de tu presupuesto. Prueba con menos días o quitando alguna restricción.`);
  }
  const id = crypto.randomBytes(9).toString('base64url');
  const createdAt = new Date().toISOString();
  const full = { id, createdAt, prefs, ...plan };
  db.prepare('INSERT INTO plans (id, created_at, prefs, plan) VALUES (?, ?, ?, ?)').run(id, createdAt, JSON.stringify(prefs), JSON.stringify(full));
  return full;
}

/** Cambia una receta del plan por otra distinta. Devuelve un plan nuevo (con otro id, para que se sincronice). */
export async function swapRecipe(planId, index) {
  if (!ai && !MOCK) throw new PlannerError('El planificador no está disponible ahora mismo.', 503);
  const old = getPlan(planId);
  const target = old?.recipes?.[index];
  if (!target) throw new PlannerError('Esa receta ya no está en el plan.', 404);
  const prefs = normalizePrefs(old.prefs);
  const meal = MEALS[target.meal] ? target.meal : 'cena';
  const cat = plannerCatalog(prefs.store);

  // Una ficha nueva: otra base y otro formato que los de la receta que se cambia y los de ese día.
  const fresh = buildGuide({ ...prefs, meals: [meal], days: 7 })[meal];
  const sameDay = old.recipes.filter((r) => r.day === target.day).map((r) => r.base);
  const slot = { ...(fresh.find((s) => s.base !== target.base && !sameDay.includes(s.base) && s.format !== target.base) ?? fresh[0]), day: target.day };
  const share = Math.max(round2(target.cost * 1.25), round2(shares(prefs)[meal] / Math.max(1, prefs.days)));
  const names = old.recipes.map((r) => r.name);
  const others = old.recipes.filter((r) => r !== target).map((r) => `${r.name} (${r.day.toLowerCase()})`).slice(0, 30);
  const { recipes } = await cookMeal({ meal, slots: [slot], prefs, cat, share, avoid: [target.name, ...names], others, warnings: [] });
  if (!recipes[0] || !complete(recipes[0])) throw new PlannerError('No hemos podido cambiar la receta. Prueba otra vez.', 502);

  const next = old.recipes.map((r, i) => (i === index ? recipes[0] : { ...r, meal: MEALS[r.meal] ? r.meal : 'cena' }));
  const summaries = Object.fromEntries(Object.entries(old.sections ?? {}).map(([m, s]) => [m, s.summary]));
  if (!old.sections) summaries.cena = old.summary;
  return save(prefs, assemble(next, prefs, cat.byId, summaries, []));
}

export function getPlan(id) {
  const row = db.prepare('SELECT plan FROM plans WHERE id = ?').get(id);
  return row ? JSON.parse(row.plan) : null;
}

/** Opciones para pintar el formulario: las mismas en web y Android. */
export const plannerOptions = () => ({
  enabled: plannerEnabled(),
  diets: Object.entries(DIETS).map(([id, label]) => ({ id, label })),
  appliances: Object.entries(APPLIANCES).map(([id, label]) => ({ id, label })),
  meals: Object.entries(MEALS).map(([id, m]) => ({ id, label: m.label })),
  stores: STORES.filter((s) => (getMeta(metaKey(s.id, 'catalog_version')) ?? null) !== null).map((s) => ({ id: s.id, label: s.name })),
});

// ───────────── Plan de prueba (PLANNER_MOCK=1) ─────────────

function mockMeal(meal, slots, lines) {
  const find = (q) => {
    const line = lines.find((l) => l.split('|')[1].toLowerCase().includes(q));
    return line ? line.split('|')[0] : '';
  };
  const dish = (name, description, minutes, appliances, ing, steps, nutrition) => ({
    name, description, minutes, difficulty: 'fácil', appliances, nutrition,
    ingredients: ing.map(([q, amount, fraction]) => ({ productId: find(q), name: q, amount, fraction })),
    steps, tip: 'Receta de prueba generada sin IA.',
  });
  const sets = {
    desayuno: [
      dish('Tostadas con tomate y jamón', 'Pan tostado con tomate rallado y jamón.', 8, [], [['pan', '4 rebanadas', 0.3], ['tomate', '2 uds', 0.3], ['jamón', '60 g', 0.5]], ['Tuesta el pan.', 'Ralla el tomate y úntalo.', 'Pon el jamón por encima.'], { kcal: 340, protein: 16, carbs: 40, fat: 12, fiber: 4, salt: 1.6 }),
      dish('Porridge de avena y plátano', 'Avena cremosa con plátano y canela.', 8, ['microondas'], [['avena', '80 g', 0.16], ['leche', '400 ml', 0.4], ['plátano', '1 ud', 0.2]], ['Mezcla avena y leche.', 'Calienta 3 minutos.', 'Corona con plátano.'], { kcal: 360, protein: 13, carbs: 58, fat: 8, fiber: 6, salt: 0.2 }),
      dish('Yogur con fruta y granola', 'Bol rápido y fresco.', 4, [], [['yogur', '2 uds', 0.5], ['granola', '50 g', 0.15], ['manzana', '1 ud', 0.15]], ['Pon el yogur en un bol.', 'Añade la fruta troceada y la granola.'], { kcal: 310, protein: 11, carbs: 42, fat: 10, fiber: 4, salt: 0.2 }),
    ],
    merienda: [
      dish('Hummus con zanahoria', 'Crema de garbanzo con palitos.', 5, [], [['hummus', '100 g', 0.4], ['zanahoria', '2 uds', 0.2]], ['Pela y corta la zanahoria.', 'Sirve con el hummus.'], { kcal: 190, protein: 6, carbs: 18, fat: 10, fiber: 6, salt: 0.6 }),
      dish('Manzana con queso fresco', 'Dulce y salado en 2 minutos.', 3, [], [['queso fresco', '100 g', 0.4], ['manzana', '1 ud', 0.15]], ['Corta la manzana.', 'Acompaña con el queso.'], { kcal: 170, protein: 9, carbs: 16, fat: 7, fiber: 3, salt: 0.5 }),
    ],
    comida: [
      dish('Lentejas estofadas con verduras', 'Cuchara de toda la vida.', 40, ['fuego'], [['lentejas', '300 g', 0.6], ['zanahoria', '2 uds', 0.2], ['cebolla', '1 ud', 0.2]], ['Sofríe la verdura.', 'Añade las lentejas y agua.', 'Cuece 25 minutos.'], { kcal: 520, protein: 26, carbs: 70, fat: 12, fiber: 14, salt: 1.0 }),
      dish('Arroz con pollo y pimiento', 'Arroz seco de domingo.', 40, ['fuego'], [['arroz', '200 g', 0.2], ['pechuga de pollo', '300 g', 0.5], ['pimiento', '1 ud', 0.5]], ['Dora el pollo.', 'Sofríe el pimiento.', 'Añade arroz y caldo, 18 minutos.'], { kcal: 640, protein: 38, carbs: 78, fat: 16, fiber: 4, salt: 1.2 }),
      dish('Macarrones con atún y tomate', 'Clásico rápido de despensa.', 15, ['fuego'], [['macarrones', '200 g', 0.4], ['tomate frito', '200 g', 0.5], ['atún', '2 latas', 0.67]], ['Cuece la pasta.', 'Calienta el tomate con el atún.', 'Mezcla y sirve.'], { kcal: 610, protein: 30, carbs: 82, fat: 16, fiber: 5, salt: 1.5 }),
    ],
    cena: [
      dish('Tortilla de espinacas', 'Tortilla jugosa con espinacas y queso.', 20, ['fuego'], [['huevos', '4 huevos', 0.35], ['espinacas', '150 g', 0.6], ['queso rallado', '40 g', 0.25]], ['Saltea las espinacas.', 'Bate los huevos con el queso.', 'Cuaja la tortilla a fuego medio.'], { kcal: 360, protein: 24, carbs: 6, fat: 26, fiber: 3, salt: 1.3 }),
      dish('Salmón con patata', 'Salmón al horno sobre patata en láminas.', 35, ['horno'], [['salmón', '2 lomos', 1], ['patata', '400 g', 0.2]], ['Hornea la patata 15 minutos.', 'Añade el salmón y hornea 12 más.'], { kcal: 520, protein: 34, carbs: 30, fat: 28, fiber: 3, salt: 0.8 }),
      dish('Crema de calabacín', 'Crema suave con quesitos.', 25, ['fuego', 'batidora'], [['calabacín', '2 uds', 0.5], ['cebolla', '1 ud', 0.2]], ['Pocha la cebolla.', 'Cuece el calabacín 15 minutos.', 'Tritura y sirve.'], { kcal: 220, protein: 6, carbs: 18, fat: 14, fiber: 4, salt: 0.7 }),
    ],
  };
  const base = sets[meal];
  return { summary: `${MEALS[meal].label}s de prueba sin IA.`, recipes: slots.map((s, i) => ({ ...base[i % base.length], name: `${base[i % base.length].name} (${s.day})` })) };
}
