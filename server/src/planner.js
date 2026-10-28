import crypto from 'node:crypto';
import { GoogleGenAI, Type } from '@google/genai';
import { db } from './db.js';
import { sectionIdFor } from './taxonomy.js';

// Planes de cenas con Gemini, sobre el catálogo real de Mercadona.
// La IA elige recetas y productos; los precios, la lista de la compra y el coste
// los calcula siempre el servidor con los precios de la base de datos.

// Mismo patrón que Puchero: cadena de modelos configurable desde Coolify.
const MODELS = [...new Set([
  process.env.GEMINI_MODEL_PLAN || 'gemini-3-flash-preview',
  process.env.GEMINI_MODEL_FAST || 'gemini-3.5-flash-lite',
])];
const apiKey = process.env.GEMINI_API_KEY;
const ai = apiKey ? new GoogleGenAI({ apiKey }) : null;
// Solo para desarrollo: monta un plan con productos reales sin llamar a la IA
const MOCK = process.env.PLANNER_MOCK === '1';
if (!ai && !MOCK) console.warn('[planner] GEMINI_API_KEY no configurada: los planes de cenas no estarán disponibles.');

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

const clampInt = (v, lo, hi, def) => (Number.isFinite(Number(v)) ? Math.min(hi, Math.max(lo, Math.round(Number(v)))) : def);
const cleanList = (v, allowed) => (Array.isArray(v) ? [...new Set(v.filter((x) => typeof x === 'string' && (!allowed || x in allowed)))] : []);
const cleanText = (v, max = 300) => (typeof v === 'string' ? v.replace(/\s+/g, ' ').trim().slice(0, max) : '');

/** Normaliza lo que manda el cliente: nunca se fía de él para nada que llegue al prompt. */
export function normalizePrefs(p = {}) {
  const appliances = cleanList(p.appliances, APPLIANCES);
  return {
    budget: clampInt(p.budget, 10, 400, 50),
    people: clampInt(p.people, 1, 8, 2),
    nights: clampInt(p.nights, 1, 7, 5),
    maxMinutes: clampInt(p.maxMinutes, 10, 120, 40),
    diets: cleanList(p.diets, DIETS),
    appliances: appliances.length ? appliances : ['fuego'],
    likes: cleanText(p.likes),
    dislikes: cleanText(p.dislikes),
    allergies: cleanText(p.allergies),
    pantry: cleanText(p.pantry, 500),
    store: 'mercadona',
  };
}

// ───────────── Catálogo para la IA ─────────────

// Subcategorías que no sirven para cocinar una cena (dulces, desayuno, helados, infantil...).
const NOT_FOR_DINNER = new Set([
  59, 64, 65, 66, 68, 69, 71, 78, 79, 80, 81, 83, 84, 86, 88, 89, 90, 92, 95, 97, 833,
  98, 99, 100, 143, 105, 106, 107, 108, 110, 111, 132, 152, 154, 155, 216,
]);
const FOOD_SECTIONS = new Set(['frescos', 'lacteos', 'despensa', 'congelados']);

let catalogCache = null; // { version, lines, byId }

function plannerCatalog(version) {
  if (catalogCache?.version === version) return catalogCache;
  const rows = db.prepare('SELECT id, name, size, price, ref_price, ref_format, category_id, subcategory_id, image FROM products WHERE active = 1 ORDER BY position').all();
  const byId = new Map();
  const lines = [];
  for (const r of rows) {
    byId.set(r.id, r);
    if (!FOOD_SECTIONS.has(sectionIdFor(r.category_id)) || NOT_FOR_DINNER.has(r.subcategory_id)) continue;
    lines.push(`${r.id}|${r.name}|${r.size}|${r.price.toFixed(2)}`);
  }
  catalogCache = { version, lines, byId };
  return catalogCache;
}

// ───────────── Prompt y esquema ─────────────

const DAYS = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo'];

function buildPrompt(prefs, catalogLines, feedback) {
  const diets = prefs.diets.map((d) => DIETS[d]).join('; ') || DIETS.omnivora;
  const appliances = prefs.appliances.map((a) => APPLIANCES[a]).join(', ');
  return `Eres el planificador de cenas de ListPro, una app de lista de la compra con el catálogo real de Mercadona (España).
Crea ${prefs.nights} cenas distintas (${DAYS.slice(0, prefs.nights).join(', ')}) para ${prefs.people} ${prefs.people === 1 ? 'persona' : 'personas'}.

RESTRICCIONES (obligatorias):
- Presupuesto de la compra de toda la semana: ${prefs.budget} € como máximo. Suma lo que hay que comprar, no lo que se gasta en cada plato.
- Dieta: ${diets}.
- Alergias e intolerancias: ${prefs.allergies || 'ninguna'}. No uses NINGÚN producto que las contenga.
- No le gusta: ${prefs.dislikes || 'nada en especial'}.
- Electrodomésticos disponibles: ${appliances}. No uses ningún otro (si no hay horno, nada al horno).
- Tiempo máximo por cena: ${prefs.maxMinutes} minutos, incluida la preparación.
- Le gusta: ${prefs.likes || 'comida casera variada'}.
- Ya tiene en casa: ${prefs.pantry || 'solo lo básico'}. Úsalo primero y NO lo compres.
Se da por hecho que tiene aceite de oliva, sal, pimienta y agua: no los compres.

CÓMO PLANIFICAR:
- Reaprovecha productos entre cenas (si compras un paquete de 6 huevos o una bolsa de espinacas, úsalos en varias) para gastar menos y no tirar comida.
- Cenas equilibradas: verdura en casi todas, proteína en todas, raciones realistas.
- Usa SOLO productos de la lista, citando su id exacto. Formato de la lista: id|nombre|formato|precio en €.
- Para cada ingrediente indica "fraction": qué parte de UN envase se usa en esa receta para todas las raciones (0.25 = un cuarto del paquete, 2 = dos envases).
- Si un ingrediente es de lo que ya tiene en casa, pon "productId": "" y "fromPantry": true.
- Nutrición POR RACIÓN, estimada a partir de las cantidades.
- Pasos cortos y concretos (máximo 20 palabras cada uno), en español de España, tono cercano.
${feedback ? `\nCORRIGE ESTO DE TU INTENTO ANTERIOR: ${feedback}\n` : ''}
CATÁLOGO:
${catalogLines.join('\n')}`;
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
    title: { type: Type.STRING },
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
  required: ['title', 'summary', 'recipes'],
};

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

async function generate(prompt) {
  let last = null;
  for (const model of MODELS) {
    let config = {
      responseMimeType: 'application/json',
      responseSchema: SCHEMA,
      temperature: 0.7,
      maxOutputTokens: 16000,
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

/** Convierte la respuesta de la IA en un plan con precios reales y una lista de la compra exacta. */
export function finalizePlan(raw, prefs, byId) {
  const warnings = [];
  const use = new Map(); // productId → fracción total de envase usada en la semana
  const recipes = (Array.isArray(raw?.recipes) ? raw.recipes : []).slice(0, prefs.nights).map((r, i) => {
    const ingredients = [];
    let cost = 0;
    for (const ing of Array.isArray(r.ingredients) ? r.ingredients : []) {
      const name = cleanText(ing.name, 80);
      if (!name) continue;
      const product = ing.productId ? byId.get(String(ing.productId)) : null;
      if (ing.productId && !product) { warnings.push(`«${name}» no está en el catálogo y se ha quitado.`); continue; }
      const fraction = product ? num(ing.fraction, 0.02, 12) : 0;
      if (product) {
        use.set(product.id, (use.get(product.id) ?? 0) + fraction);
        cost += fraction * product.price;
      }
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
    const n = r.nutrition ?? {};
    return {
      day: DAYS[i],
      name: cleanText(r.name, 80) || `Cena ${i + 1}`,
      description: cleanText(r.description, 220),
      minutes: clampInt(r.minutes, 5, 180, prefs.maxMinutes),
      difficulty: ['fácil', 'media', 'elaborada'].includes(r.difficulty) ? r.difficulty : 'fácil',
      appliances: cleanList(r.appliances, APPLIANCES),
      servings: prefs.people,
      nutrition: {
        kcal: Math.round(num(n.kcal, 0, 3000)), protein: Math.round(num(n.protein, 0, 300)),
        carbs: Math.round(num(n.carbs, 0, 500)), fat: Math.round(num(n.fat, 0, 300)),
        fiber: Math.round(num(n.fiber, 0, 100)), salt: round2(num(n.salt, 0, 20)),
      },
      ingredients,
      steps: (Array.isArray(r.steps) ? r.steps : []).map((s) => cleanText(s, 220)).filter(Boolean).slice(0, 12),
      tip: cleanText(r.tip, 200),
      cost: round2(cost),
      costPerServing: round2(cost / prefs.people),
    };
  });

  // Lo que se compra: envases enteros. Lo usado entre varias cenas se suma antes de redondear.
  const shopping = [...use.entries()].map(([id, fraction]) => {
    const p = byId.get(id);
    const packs = Math.max(1, Math.ceil(fraction - 0.05));
    return { productId: id, name: p.name, size: p.size, image: p.image, price: p.price, packs, cost: round2(packs * p.price), usedIn: recipes.filter((r) => r.ingredients.some((x) => x.productId === id)).map((r) => r.day) };
  }).sort((a, b) => (a.usedIn.length === b.usedIn.length ? b.cost - a.cost : b.usedIn.length - a.usedIn.length));

  const shoppingTotal = round2(shopping.reduce((a, s) => a + s.cost, 0));
  const servings = recipes.length * prefs.people || 1;
  return {
    title: cleanText(raw?.title, 80) || 'Tus cenas de la semana',
    summary: cleanText(raw?.summary, 300),
    recipes,
    shopping,
    totals: {
      shopping: shoppingTotal,
      consumed: round2(recipes.reduce((a, r) => a + r.cost, 0)),
      perServing: round2(shoppingTotal / servings),
      kcalAvg: Math.round(recipes.reduce((a, r) => a + r.nutrition.kcal, 0) / (recipes.length || 1)),
      proteinAvg: Math.round(recipes.reduce((a, r) => a + r.nutrition.protein, 0) / (recipes.length || 1)),
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

export async function createPlan(rawPrefs, catalogVersion) {
  if (!ai && !MOCK) throw new PlannerError('El planificador no está disponible ahora mismo.', 503);
  const prefs = normalizePrefs(rawPrefs);
  const { lines, byId } = plannerCatalog(catalogVersion);
  if (lines.length < 100) throw new PlannerError('El catálogo aún se está cargando. Prueba en un minuto.', 503);

  const complete = (c) => c.recipes.length >= prefs.nights && c.recipes.every((r) => r.ingredients.length >= 2 && r.steps.length >= 2);
  let plan = null;
  let feedback = '';
  // Hasta dos intentos: si el primero se pasa de presupuesto o deja recetas cojas, se le explica y repite.
  for (let attempt = 1; attempt <= 2; attempt++) {
    const raw = MOCK ? mockPlan(prefs, lines) : await generate(buildPrompt(prefs, lines, feedback));
    const candidate = finalizePlan(raw, prefs, byId);
    const tooShort = !complete(candidate);
    const over = candidate.totals.shopping - prefs.budget;
    // Nos quedamos con el mejor intento: primero el completo, y entre iguales, el más barato
    const better = !plan || (complete(candidate) && !complete(plan))
      || (complete(candidate) === complete(plan) && candidate.totals.shopping < plan.totals.shopping);
    if (better) plan = candidate;
    if (!tooShort && over <= prefs.budget * 0.03) break;
    feedback = [
      over > 0 ? `La compra sale a ${candidate.totals.shopping.toFixed(2)} € y el máximo es ${prefs.budget} €: baja ${over.toFixed(2)} € eligiendo productos más baratos (marca Hacendado, legumbres, huevos, verdura de temporada, congelados) y reaprovechando más.` : '',
      tooShort ? `Faltan cenas o alguna no tiene ingredientes o pasos suficientes: necesito ${prefs.nights} cenas completas.` : '',
    ].filter(Boolean).join(' ');
  }
  if (!plan.recipes.length) throw new PlannerError('No hemos podido crear el plan. Prueba otra vez.', 502);
  if (plan.totals.overBudget) {
    plan.warnings.unshift(`La compra se pasa ${round2(plan.totals.shopping - prefs.budget).toFixed(2).replace('.', ',')} € de tu presupuesto. Prueba con menos cenas o quitando alguna restricción.`);
  }

  const id = crypto.randomBytes(9).toString('base64url');
  const createdAt = new Date().toISOString();
  const full = { id, createdAt, prefs, ...plan };
  db.prepare('INSERT INTO plans (id, created_at, prefs, plan) VALUES (?, ?, ?, ?)').run(id, createdAt, JSON.stringify(prefs), JSON.stringify(full));
  return full;
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
  stores: [{ id: 'mercadona', label: 'Mercadona' }],
});

// ───────────── Plan de prueba (PLANNER_MOCK=1) ─────────────

function mockPlan(prefs, lines) {
  const find = (q) => {
    const line = lines.find((l) => l.split('|')[1].toLowerCase().includes(q));
    return line ? line.split('|')[0] : '';
  };
  const dish = (name, description, minutes, appliances, ing, steps, nutrition) => ({
    name, description, minutes, difficulty: 'fácil', appliances, nutrition,
    ingredients: ing.map(([q, amount, fraction]) => ({ productId: find(q), name: q, amount, fraction })),
    steps, tip: 'Plato de prueba generado sin IA.',
  });
  const base = [
    dish('Pollo al limón con brócoli', 'Pechuga a la plancha con brócoli salteado.', 25, ['fuego'], [['pechuga de pollo', '400 g', 0.6], ['brócoli', '1 pieza', 1], ['limón', '1 ud', 0.2]], ['Corta el pollo en tiras.', 'Dóralo 6 minutos.', 'Saltea el brócoli y sirve con limón.'], { kcal: 420, protein: 42, carbs: 12, fat: 18, fiber: 6, salt: 1.1 }),
    dish('Tortilla de espinacas', 'Tortilla jugosa con espinacas y queso.', 20, ['fuego'], [['huevos', '4 huevos', 0.35], ['espinacas', '150 g', 0.6], ['queso rallado', '40 g', 0.25]], ['Saltea las espinacas.', 'Bate los huevos con el queso.', 'Cuaja la tortilla a fuego medio.'], { kcal: 360, protein: 24, carbs: 6, fat: 26, fiber: 3, salt: 1.3 }),
    dish('Arroz con verduras', 'Arroz salteado con verduras y huevo.', 30, ['fuego'], [['arroz', '200 g', 0.2], ['pimiento', '1 ud', 0.5], ['huevos', '2 huevos', 0.17]], ['Cuece el arroz.', 'Saltea el pimiento.', 'Mezcla con el huevo revuelto.'], { kcal: 480, protein: 16, carbs: 70, fat: 14, fiber: 5, salt: 0.9 }),
    dish('Salmón con patata', 'Salmón al horno sobre patata en láminas.', 35, ['horno'], [['salmón', '2 lomos', 1], ['patata', '400 g', 0.2]], ['Hornea la patata 15 minutos.', 'Añade el salmón y hornea 12 más.'], { kcal: 520, protein: 34, carbs: 30, fat: 28, fiber: 3, salt: 0.8 }),
    dish('Pasta con tomate y atún', 'Clásico rápido de despensa.', 15, ['fuego'], [['macarrones', '200 g', 0.4], ['tomate frito', '200 g', 0.5], ['atún', '2 latas', 0.67]], ['Cuece la pasta.', 'Calienta el tomate con el atún.', 'Mezcla y sirve.'], { kcal: 560, protein: 30, carbs: 72, fat: 16, fiber: 5, salt: 1.5 }),
    dish('Crema de calabacín', 'Crema suave con quesitos.', 25, ['fuego', 'batidora'], [['calabacín', '2 uds', 0.5], ['cebolla', '1 ud', 0.2]], ['Pocha la cebolla.', 'Cuece el calabacín 15 minutos.', 'Tritura y sirve.'], { kcal: 220, protein: 6, carbs: 18, fat: 14, fiber: 4, salt: 0.7 }),
    dish('Hamburguesa con ensalada', 'Burger casera al plato con ensalada.', 20, ['fuego'], [['hamburguesa', '2 uds', 0.5], ['lechuga', '1 bolsa', 0.5], ['tomate', '2 uds', 0.3]], ['Haz las hamburguesas a la plancha.', 'Prepara la ensalada.', 'Sirve junto.'], { kcal: 510, protein: 30, carbs: 10, fat: 38, fiber: 3, salt: 1.6 }),
  ];
  return { title: 'Plan de prueba', summary: 'Plan generado sin IA para probar la app.', recipes: base.slice(0, prefs.nights) };
}
