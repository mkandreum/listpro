import { memo, useState } from 'react';
import { ArrowDown, ArrowUp, Minus, Plus, Sparkles } from 'lucide-react';
import { money, priceDelta, splitPrice } from '../lib';
import { changeQty, useStore } from '../store';
import type { Product } from '../types';

export function Price({ value, className = 'price' }: { value: number; className?: string }) {
  const { int, dec } = splitPrice(value);
  return <span className={className}>{int}<span className="dec">{dec}&nbsp;€</span></span>;
}

export function ProductImage({ p, className }: { p: Product; className?: string }) {
  const [failed, setFailed] = useState(false);
  const emoji = useStore((s) => s.idx.subcategories.get(p.subcategory)?.emoji ?? '🛒');
  if (!p.image || failed) return <span className={`fallback ${className ?? ''}`} aria-hidden>{emoji}</span>;
  return <img src={p.image} alt="" loading="lazy" decoding="async" className={className} onError={() => setFailed(true)} />;
}

export function Seal({ p }: { p: Product }) {
  const d = priceDelta(p);
  if (d != null) {
    const Icon = d < 0 ? ArrowDown : ArrowUp;
    return <span className={`seal ${d < 0 ? 'down' : 'up'}`}><Icon size={13} strokeWidth={2.8} />{d < 0 ? 'BAJA' : 'SUBE'} {money(Math.abs(d))}</span>;
  }
  if (p.isNew) return <span className="seal new"><Sparkles size={12} strokeWidth={2.6} />NUEVO</span>;
  return null;
}

export function QtyControl({ id, name }: { id: string; name: string }) {
  const qty = useStore((s) => s.basket[id] ?? 0);
  if (!qty) {
    return (
      <button className="add" onClick={() => changeQty(id, 1)} aria-label={`Añadir ${name} a la cesta`}>
        <Plus size={24} strokeWidth={2.6} />
      </button>
    );
  }
  return (
    <div className="stepper" role="group" aria-label={`Cantidad de ${name}`}>
      <button onClick={() => changeQty(id, -1)} aria-label="Quitar uno"><Minus size={18} strokeWidth={3} /></button>
      <output aria-live="polite">{qty}</output>
      <button onClick={() => changeQty(id, 1)} aria-label="Añadir uno"><Plus size={18} strokeWidth={3} /></button>
    </div>
  );
}

interface TagProps {
  p: Product;
  onOpen: (p: Product) => void;
  className?: string;
}

export const ProductTag = memo(function ProductTag({ p, onOpen, className = '' }: TagProps) {
  const inBasket = useStore((s) => (s.basket[p.id] ?? 0) > 0);
  const d = priceDelta(p);
  return (
    <article className={`tag ${inBasket ? 'in-basket' : ''} ${className}`}>
      <Seal p={p} />
      <button className="tag-media" onClick={() => onOpen(p)} aria-label={`Ver detalle de ${p.name}`}>
        <ProductImage p={p} />
      </button>
      <div className="tag-body">
        <h3 className="tag-name">{p.name}</h3>
        <div className="tag-size">{p.size}</div>
        <div className="tag-foot">
          <div>
            {d != null && d < 0 && p.previousPrice != null && <div className="price-old num">{money(p.previousPrice)}</div>}
            <Price value={p.price} className={`price ${d != null && d < 0 ? 'drop' : ''}`} />
            {p.refPrice != null && p.refFormat && <div className="price-ref num">{money(p.refPrice)}/{p.refFormat}</div>}
          </div>
          <QtyControl id={p.id} name={p.name} />
        </div>
      </div>
    </article>
  );
});
