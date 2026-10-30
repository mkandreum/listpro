// Súper con catálogo. Mercadona fue el primero y conserva sus ids tal cual (productos "27348",
// categorías 1-999) para no romper cestas, historiales ni apps ya instaladas. El resto lleva
// prefijo en los productos ("c:4667") y un desplazamiento en las categorías, así nunca chocan.
//
// Carrefour, Alcampo y Lidl se probaron (octubre de 2026): los dos primeros protegen su tienda
// online contra bots y Lidl solo publica online unas pocas ofertas, no su surtido. Si alguno
// abre un catálogo legible, basta con añadirlo aquí y escribir su lector en scrapers/.

export const STORES = [
  { id: 'mercadona', name: 'Mercadona', hue: '#00A650', prefix: '', categoryOffset: 0 },
  { id: 'consum', name: 'Consum', hue: '#F28C00', prefix: 'c:', categoryOffset: 1_000_000 },
];

export const DEFAULT_STORE = 'mercadona';
const byId = new Map(STORES.map((s) => [s.id, s]));

export const getStore = (id) => byId.get(id) ?? null;
export const isStore = (id) => byId.has(id);
/** El súper de un producto, por su id. */
export const storeOfProduct = (productId) => STORES.find((s) => s.prefix && String(productId).startsWith(s.prefix))?.id ?? DEFAULT_STORE;
/** El súper de una categoría, por su id. */
export const storeOfCategory = (categoryId) => [...STORES].sort((a, b) => b.categoryOffset - a.categoryOffset).find((s) => categoryId >= s.categoryOffset)?.id ?? DEFAULT_STORE;
/** Lee el súper que pide un cliente (?store=), con Mercadona por defecto para las apps antiguas. */
export const storeParam = (v) => (typeof v === 'string' && isStore(v) ? v : DEFAULT_STORE);
/** Claves de meta por súper. Las de Mercadona mantienen su nombre de siempre. */
export const metaKey = (store, key) => (store === DEFAULT_STORE ? key : `${key}:${store}`);
