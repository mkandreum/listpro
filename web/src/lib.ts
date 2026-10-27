import type { Product } from './types';

const eur = new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' });
export const money = (v: number | null | undefined) => eur.format(v ?? 0);

/** Precio partido para pintarlo a escala de folleto: "3" + ",45 €". */
export function splitPrice(v: number) {
  const [int, dec] = v.toFixed(2).split('.');
  return { int, dec: `,${dec}` };
}

export const norm = (s: string) => s.toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '');

export const uid = () => Date.now().toString(36) + Math.random().toString(36).slice(2, 7);

export const units = (items: Record<string, number>) => Object.values(items).reduce((a, q) => a + q, 0);

export function priceDelta(p: Product): number | null {
  if (p.previousPrice == null || Math.abs(p.previousPrice - p.price) < 0.005) return null;
  return p.price - p.previousPrice;
}

export function relativeTime(iso: string | null | undefined): string {
  if (!iso) return 'sin datos';
  const mins = Math.round((Date.now() - Date.parse(iso)) / 60000);
  if (mins < 1) return 'ahora mismo';
  if (mins < 60) return `hace ${mins} min`;
  const h = Math.round(mins / 60);
  if (h < 24) return `hace ${h} h`;
  const d = Math.round(h / 24);
  return d === 1 ? 'ayer' : `hace ${d} días`;
}

export function freshnessLabel(iso: string | null | undefined): string {
  if (!iso) return 'Sin precios';
  const d = new Date(iso);
  const today = new Date();
  const time = d.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' });
  if (d.toDateString() === today.toDateString()) return `Precios de hoy · ${time}`;
  return `Precios ${relativeTime(iso)}`;
}

const escapeRe = (s: string) => s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
const normCache = new WeakMap<Product, { n: string; g: string }>();

export function searchProducts(products: Product[], query: string): Product[] {
  const tokens = norm(query.trim()).split(/\s+/).filter(Boolean);
  if (!tokens.length) return [];
  const first = new RegExp(`\\b${escapeRe(tokens[0])}`);
  const scored: [number, Product][] = [];
  for (const p of products) {
    let c = normCache.get(p);
    if (!c) { c = { n: norm(p.name), g: norm(p.group) }; normCache.set(p, c); }
    if (!tokens.every((t) => c.n.includes(t) || c.g.includes(t))) continue;
    let score = 0;
    if (c.n.startsWith(tokens[0])) score += 3;
    if (first.test(c.n)) score += 2;
    if (tokens.some((t) => c.g.includes(t))) score += 2;
    score -= c.n.length / 200;
    scored.push([score, p]);
  }
  return scored.sort((a, b) => b[0] - a[0]).map((x) => x[1]);
}

/** Texto legible sobre el color de un pasillo. */
export function inkOn(hex: string | undefined): string {
  if (!hex) return '#0E0E12';
  const n = parseInt(hex.slice(1), 16);
  const r = (n >> 16) & 255, g = (n >> 8) & 255, b = n & 255;
  const lum = (0.2126 * r + 0.7152 * g + 0.0722 * b) / 255;
  return lum > 0.45 ? '#0E0E12' : '#FFFFFF';
}
