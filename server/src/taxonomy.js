// Taxonomía de ListPro (web y Android pintan cada pasillo con su `hue`) sobre el árbol de Mercadona.
//
// Mercadona organiza la tienda en 3 niveles:
//   categoría (26, p. ej. "Carne") → subcategoría (~150, "Aves y pollo") → grupo ("Pollo entero y troceado")
// La app antigua aplanaba todo con palabras clave y fallaba ("sal" casaba con "salsas", "fruta" con
// "frutos secos", "pan" con "panadería"...). Aquí se agrupa por ID, que es estable, en pasillos
// pensados para hacer la compra, y cada nivel lleva su emoji. Lo que Mercadona añada y no esté
// mapeado cae en el pasillo "Otros" con el emoji de su categoría, nunca se pierde.

export const SECTIONS = [
  { id: 'frescos', hue: '#2FD27A', name: 'Frescos', emoji: '🥬', categories: [1, 3, 2, 4, 5] },
  { id: 'lacteos', hue: '#7CC8FF', name: 'Lácteos y huevos', emoji: '🥛', categories: [6, 11] },
  { id: 'despensa', hue: '#FF8A2A', name: 'Despensa', emoji: '🥫', categories: [12, 13, 14, 7, 8, 9, 15] },
  { id: 'congelados', hue: '#5C7CFF', name: 'Congelados y platos listos', emoji: '❄️', categories: [17, 16] },
  { id: 'bebidas', hue: '#FF5FA8', name: 'Bebidas', emoji: '🥤', categories: [18, 10, 19] },
  { id: 'cuidado', hue: '#B691FF', name: 'Cuidado personal', emoji: '🧴', categories: [20, 21, 22, 23] },
  { id: 'hogar', hue: '#FFD23F', name: 'Limpieza y hogar', emoji: '🧹', categories: [26] },
  { id: 'bebe', hue: '#6FE3CF', name: 'Bebé', emoji: '👶', categories: [24] },
  { id: 'mascotas', hue: '#D9935A', name: 'Mascotas', emoji: '🐾', categories: [25] },
];

export const OTHER_SECTION = { id: 'otros', hue: '#B9BCC4', name: 'Otros', emoji: '🛒' };

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

export function sectionIdFor(categoryId) {
  return sectionByCategory.get(categoryId) ?? OTHER_SECTION.id;
}

export function categoryEmoji(categoryId) {
  return CATEGORY_META[categoryId]?.emoji ?? '🛒';
}

export function subcategoryEmoji(subcategoryId, parentId) {
  return SUBCATEGORY_EMOJI[subcategoryId] ?? categoryEmoji(parentId);
}

/**
 * Construye el árbol pasillo → categoría → subcategoría a partir de las filas de la BD,
 * respetando el orden de Mercadona dentro de cada pasillo.
 */
export function buildTree(categories, subcategories, counts) {
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

  const sections = SECTIONS.map((s) => {
    const cats = s.categories.map(toCat).filter(Boolean);
    return { id: s.id, name: s.name, emoji: s.emoji, hue: s.hue, count: cats.reduce((a, c) => a + c.count, 0), categories: cats };
  }).filter((s) => s.categories.length);

  const known = new Set(SECTIONS.flatMap((s) => s.categories));
  const others = categories.filter((c) => !known.has(c.id)).map((c) => toCat(c.id)).filter(Boolean);
  if (others.length) {
    sections.push({ ...OTHER_SECTION, count: others.reduce((a, c) => a + c.count, 0), categories: others });
  }
  return sections;
}
