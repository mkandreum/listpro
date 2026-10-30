// Taxonomía de ListPro (web y Android pintan cada pasillo con su `hue`) sobre el árbol de Mercadona.
//
// Mercadona organiza la tienda en 3 niveles:
//   categoría (26, p. ej. "Carne") → subcategoría (~150, "Aves y pollo") → grupo ("Pollo entero y troceado")
// La app antigua aplanaba todo con palabras clave y fallaba ("sal" casaba con "salsas", "fruta" con
// "frutos secos", "pan" con "panadería"...). Aquí se agrupa por ID, que es estable, en pasillos
// pensados para hacer la compra, y cada nivel lleva su emoji. Lo que Mercadona añada y no esté
// mapeado cae en el pasillo "Otros" con el emoji de su categoría, nunca se pierde.

import { db } from './db.js';

export const SECTIONS = [
  { id: 'frescos', icon: 'leaf', blurb: 'Fruta, verdura, carne, pescado, charcutería y pan del día.', hue: '#2FD27A', name: 'Frescos', emoji: '🥬', categories: [1, 3, 2, 4, 5] },
  { id: 'lacteos', icon: 'milk', blurb: 'Leche, huevos, mantequilla, yogures y postres.', hue: '#7CC8FF', name: 'Lácteos y huevos', emoji: '🥛', categories: [6, 11] },
  { id: 'despensa', icon: 'wheat', blurb: 'Aceite, pasta, arroz, conservas, café, cereales y dulces.', hue: '#FF8A2A', name: 'Despensa', emoji: '🥫', categories: [12, 13, 14, 7, 8, 9, 15] },
  { id: 'congelados', icon: 'snowflake', blurb: 'Congelados, pizzas y platos listos para calentar.', hue: '#5C7CFF', name: 'Congelados y platos listos', emoji: '❄️', categories: [17, 16] },
  { id: 'bebidas', icon: 'wine', blurb: 'Agua, refrescos, zumos, cerveza y vino.', hue: '#FF5FA8', name: 'Bebidas', emoji: '🥤', categories: [18, 10, 19] },
  { id: 'cuidado', icon: 'sparkles', blurb: 'Higiene, cabello, maquillaje y parafarmacia.', hue: '#B691FF', name: 'Cuidado personal', emoji: '🧴', categories: [20, 21, 22, 23] },
  { id: 'hogar', icon: 'spray', blurb: 'Detergentes, limpieza, papel y menaje.', hue: '#FFD23F', name: 'Limpieza y hogar', emoji: '🧹', categories: [26] },
  { id: 'bebe', icon: 'baby', blurb: 'Alimentación infantil, pañales y cuidado del bebé.', hue: '#6FE3CF', name: 'Bebé', emoji: '👶', categories: [24] },
  { id: 'mascotas', icon: 'paw', blurb: 'Comida y cuidado para perro, gato y otras mascotas.', hue: '#D9935A', name: 'Mascotas', emoji: '🐾', categories: [25] },
];

export const OTHER_SECTION = { id: 'otros', icon: 'bag', blurb: 'Lo que Mercadona añada y aún no tenga pasillo.', hue: '#B9BCC4', name: 'Otros', emoji: '🛒' };

// Categorías (nivel 0 de Mercadona)
export const CATEGORY_META = {
  1: { emoji: '🍎' }, 2: { emoji: '🐟' }, 3: { emoji: '🥩' }, 4: { emoji: '🧀' },
  5: { emoji: '🥖' }, 6: { emoji: '🥚' }, 7: { emoji: '🥣' }, 8: { emoji: '☕' },
  9: { emoji: '🍫' }, 10: { emoji: '🧃' }, 11: { emoji: '🍮' }, 12: { emoji: '🫒' },
  13: { emoji: '🍝' }, 14: { emoji: '🥫' }, 15: { emoji: '🥜' }, 16: { emoji: '🍕' },
  17: { emoji: '🧊' }, 18: { emoji: '💧' }, 19: { emoji: '🍷' }, 20: { emoji: '🧼' },
  21: { emoji: '💇' }, 22: { emoji: '💄' }, 23: { emoji: '🌿' }, 24: { emoji: '👶' },
  25: { emoji: '🐾' }, 26: { emoji: '🧽' },
};

// Subcategorías (nivel 1 de Mercadona)
export const SUBCATEGORY_EMOJI = {
  // Fruta y verdura
  27: '🍎', 28: '🥗', 29: '🥕',
  // Marisco y pescado
  31: '🐟', 32: '🦐', 34: '🧊', 36: '🐠',
  // Carne
  37: '🐖', 38: '🍗', 40: '🐄', 42: '🐇', 43: '🌭', 44: '🍔', 45: '🍖', 46: '🥘', 47: '🧊',
  // Charcutería y quesos
  48: '🍖', 49: '🥪', 50: '🍖', 51: '🥓', 52: '🥓', 53: '🧀', 54: '🧀', 56: '🧀', 58: '🥫',
  // Panadería y pastelería
  59: '🥖', 60: '🍞', 62: '🍞', 64: '🥨', 65: '🥐', 66: '🧁', 68: '🎂', 69: '🌾', 71: '🕯️',
  // Huevos, leche y mantequilla
  72: '🥛', 75: '🧈', 77: '🥚',
  // Cereales y galletas
  78: '🥣', 79: '🍘', 80: '🍪',
  // Cacao, café e infusiones
  81: '☕', 83: '☕', 84: '☕', 86: '🍫', 88: '🍵',
  // Azúcar, caramelos y chocolate
  89: '🍬', 90: '🍯', 92: '🍫', 95: '🍭', 97: '🍬', 833: '🎄',
  // Zumos
  98: '🍅', 99: '🧃', 100: '🍑', 143: '🍊',
  // Postres y yogures
  103: '🥛', 104: '🍓', 105: '🥛', 106: '🌱', 107: '🧒', 108: '🥤', 109: '🇬🇷', 110: '🍮', 111: '🍧',
  // Aceite, especias y salsas
  112: '🫒', 115: '🌶️', 116: '🍅', 117: '🥫',
  // Arroz, legumbres y pasta
  118: '🍚', 120: '🍝', 121: '🫘',
  // Conservas, caldos y cremas
  122: '🐟', 123: '🦪', 126: '🍅', 127: '🥫', 129: '🍲', 130: '🥣',
  // Aperitivos
  132: '🍟', 133: '🥜', 135: '🫒',
  // Pizzas y platos preparados
  138: '🍕', 140: '🍲', 142: '🥗', 897: '🍱',
  // Congelados
  145: '🥦', 147: '🍝', 148: '🥩', 149: '🐟', 150: '🦐', 151: '🍕', 152: '🍰', 154: '🍦', 155: '🧊', 884: '🍤',
  // Agua y refrescos
  156: '💧', 158: '🥤', 159: '🍋', 161: '🍸', 162: '🧋', 163: '⚡',
  // Bodega
  164: '🍺', 166: '🍺', 168: '🍹', 169: '🍷', 170: '🥂', 171: '🌸', 173: '🍾', 174: '🍾', 181: '🥃',
  // Cuidado facial y corporal
  185: '🧖', 186: '🦷', 187: '🧼', 188: '🌬️', 189: '🧴', 190: '🌸', 191: '🪒', 192: '🧔', 194: '💅', 196: '🌹', 198: '☀️',
  // Cuidado del cabello
  199: '🧴', 201: '💆', 202: '💇', 203: '🎨',
  // Maquillaje
  206: '🎨', 207: '🌸', 208: '💋', 210: '👁️', 212: '🖌️',
  // Fitoterapia y parafarmacia
  213: '🌿', 214: '💊',
  // Bebé
  216: '🍼', 217: '🧷', 218: '🛁', 219: '🍼',
  // Mascotas
  221: '🐶', 222: '🐱', 225: '🐦',
  // Limpieza y hogar
  226: '🧺', 229: '🍽️', 230: '🍳', 231: '🚽', 232: '🪑', 233: '🧹', 234: '🧪', 235: '🪟', 237: '🧽',
  238: '🧻', 239: '🔋', 241: '🦟', 243: '🥡', 244: '🪣',
};

const sectionByCategory = new Map();
for (const s of SECTIONS) for (const c of s.categories) sectionByCategory.set(c, s.id);

// ───────────── Consum ─────────────
// Su nivel 1 decide el pasillo; algunas categorías de nivel 2 se recolocan para que caigan donde en Mercadona.
const CONSUM_TOP = {
  2811: 'despensa', 1690: 'bebidas', 2812: 'frescos', 4108: 'frescos', 1833: 'congelados', 1783: 'congelados',
  5281: 'despensa', 2297: 'bebe', 1239: 'hogar', 2814: 'cuidado', 2469: 'mascotas', 1486: 'hogar',
};
const CONSUM_CATEGORY = { 2055: 'lacteos', 5283: 'frescos', 5284: 'hogar', 5285: 'cuidado', 5286: 'hogar' };
export const consumSection = (topId, categoryId) => CONSUM_CATEGORY[categoryId] ?? CONSUM_TOP[topId] ?? OTHER_SECTION.id;

// Emojis de las categorías de Consum (nivel 2, id de Consum)
const CONSUM_EMOJI = {
  5298: '🍬', 1970: '🥜', 1639: '🍝', 2490: '🍲', 1525: '🫒', 1852: '☕', 5153: '🍞', 1646: '🌾', 2055: '🥛', 2366: '🌮', 2218: '🥗',
  2482: '💧', 1701: '🍾', 1707: '🍺', 2484: '🫧', 1720: '⚡', 1721: '🥃', 1728: '🥤', 1739: '🍷', 1744: '🍷', 1779: '🧃',
  2148: '🥩', 4058: '🔪', 2005: '🥓', 4070: '🍖', 2019: '🧀', 4093: '🧀', 2171: '🐟', 2187: '🥕', 2179: '🍎',
  1959: '🥖', 5019: '🍞', 5017: '🍔', 5020: '🥨', 5021: '🥐', 5022: '🥟', 5031: '🎂',
  4111: '🍱', 4112: '🥫', 2505: '🥩', 2521: '🦐', 1792: '🍝', 5211: '🍤', 2514: '🥦', 5215: '🍕', 2487: '🧊', 1784: '🍦', 5096: '🥐',
  5282: '🌱', 5283: '🌱', 5284: '🌱', 5285: '🌱', 5286: '🌱', 2298: '🍼', 2314: '🍼', 2312: '🛁', 2320: '🧷', 2325: '👶',
  1277: '🧻', 5051: '🧺', 5074: '🧹', 5053: '🍽️', 5057: '🚽', 1240: '🧽', 1259: '🌬️', 1314: '🦟', 5067: '👟',
  1475: '💇', 1403: '🧴', 1413: '🧖', 5131: '💅', 5129: '🧼', 1452: '🌬️', 1463: '🌸', 1456: '🦷', 5132: '🪒', 1412: '☀️',
  1427: '💄', 1442: '🪒', 1393: '🌹', 1399: '🎀', 5050: '💊', 1489: '🦴', 4055: '🐱', 1491: '🐶', 4006: '🐹',
  1487: '🎁', 4540: '📚', 5011: '🍳', 4542: '🥡', 1299: '🥤', 4543: '🌻', 2462: '🔋', 1509: '🕯️',
};
const CONSUM_OFFSET = 1_000_000;
const ECI_OFFSET = 2_000_000;

// Pasillo de las categorías de otros súper: lo guarda el lector al descargar el catálogo
let extraSections = null;
export function reloadSections() {
  extraSections = new Map(db.prepare('SELECT id, section FROM categories WHERE section IS NOT NULL').all().map((r) => [r.id, r.section]));
}

export function sectionIdFor(categoryId) {
  if (sectionByCategory.has(categoryId)) return sectionByCategory.get(categoryId);
  if (!extraSections) reloadSections();
  return extraSections.get(categoryId) ?? OTHER_SECTION.id;
}

export function categoryEmoji(categoryId) {
  if (categoryId >= ECI_OFFSET) {
    const section = sectionIdFor(categoryId);
    return SECTIONS.find((s) => s.id === section)?.emoji ?? '🛒';
  }
  if (categoryId >= CONSUM_OFFSET) return CONSUM_EMOJI[categoryId - CONSUM_OFFSET] ?? '🛒';
  return CATEGORY_META[categoryId]?.emoji ?? '🛒';
}

export function subcategoryEmoji(subcategoryId, parentId) {
  return SUBCATEGORY_EMOJI[subcategoryId] ?? categoryEmoji(parentId);
}

/**
 * Construye el árbol pasillo → categoría → subcategoría a partir de las filas de la BD,
 * respetando el orden de cada súper dentro de cada pasillo.
 */
export function buildTree(categories, subcategories, counts, store = 'mercadona') {
  const subsByParent = new Map();
  for (const s of subcategories) {
    const n = counts.get(s.id) ?? 0;
    if (!n) continue;
    if (!subsByParent.has(s.parent_id)) subsByParent.set(s.parent_id, []);
    subsByParent.get(s.parent_id).push({ id: s.id, name: s.name, emoji: subcategoryEmoji(s.id, s.parent_id), count: n });
  }
  const catById = new Map(categories.map((c) => [c.id, c]));
  const toCat = (id) => {
    const c = catById.get(id);
    const subs = subsByParent.get(id);
    if (!c || !subs?.length) return null;
    return { id, name: c.name, emoji: categoryEmoji(id), count: subs.reduce((a, s) => a + s.count, 0), subcategories: subs };
  };

  // En Mercadona el orden de cada pasillo es el de SECTIONS; en los demás, el de su tienda
  const idsOf = (s) => (store === 'mercadona' ? s.categories : categories.filter((c) => sectionIdFor(c.id) === s.id).map((c) => c.id));
  const sections = SECTIONS.map((s) => {
    const cats = idsOf(s).map(toCat).filter(Boolean);
    return { id: s.id, name: s.name, emoji: s.emoji, hue: s.hue, icon: s.icon, blurb: s.blurb, count: cats.reduce((a, c) => a + c.count, 0), categories: cats };
  }).filter((s) => s.categories.length);

  const known = new Set(SECTIONS.flatMap(idsOf));
  const others = categories.filter((c) => !known.has(c.id)).map((c) => toCat(c.id)).filter(Boolean);
  if (others.length) {
    sections.push({ ...OTHER_SECTION, count: others.reduce((a, c) => a + c.count, 0), categories: others });
  }
  return sections;
}
