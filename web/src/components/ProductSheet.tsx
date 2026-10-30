import { useEffect, useRef, useState } from 'react';
import { ExternalLink, X } from 'lucide-react';
import { api } from '../api';
import { money } from '../lib';
import { storeInfo, storeOf, useStore } from '../store';
import { CompareBlock, StoreName } from './Stores';
import type { PriceHistory, Product } from '../types';
import { Price, ProductImage, QtyControl, Seal } from './ProductTag';
import { useSheet } from '../fx/sheet';

function HistoryChart({ history }: { history: PriceHistory['history'] }) {
  if (history.length < 2) return <p className="muted small" style={{ marginTop: 10 }}>Sin cambios de precio desde que lo seguimos ({new Date(history[0]?.at ?? Date.now()).toLocaleDateString('es-ES')}).</p>;
  const pts = [...history, { price: history.at(-1)!.price, at: new Date().toISOString() }];
  const t0 = Date.parse(pts[0].at), t1 = Date.parse(pts.at(-1)!.at);
  const prices = pts.map((x) => x.price);
  const lo = Math.min(...prices), hi = Math.max(...prices);
  const x = (t: string) => 8 + ((Date.parse(t) - t0) / Math.max(1, t1 - t0)) * 284;
  const y = (v: number) => 96 - ((v - lo) / Math.max(0.01, hi - lo)) * 80;
  let d = `M${x(pts[0].at)},${y(pts[0].price)}`;
  for (let i = 1; i < pts.length; i++) d += ` H${x(pts[i].at)} V${y(pts[i].price)}`;
  return (
    <>
      <svg className="spark" viewBox="0 0 300 110" preserveAspectRatio="none" role="img" aria-label="Historial de precio">
        <path d={d} fill="none" stroke="currentColor" strokeWidth="3" vectorEffect="non-scaling-stroke" />
      </svg>
      <p className="muted small num" style={{ marginTop: 8 }}>
        Mín. {money(lo)} · Máx. {money(hi)} · {history.length - 1} cambio{history.length > 2 ? 's' : ''} desde {new Date(history[0].at).toLocaleDateString('es-ES')}
      </p>
    </>
  );
}

export function ProductSheet({ p, onClose }: { p: Product; onClose: () => void }) {
  const [history, setHistory] = useState<PriceHistory | null>(null);
  const sub = useStore((s) => s.idx.subcategories.get(p.subcategory));
  const store = p.store ?? storeOf(p.id);
  const storeName = useStore((s) => storeInfo(store, s).name);
  const closeRef = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    let alive = true;
    api.history(p.id).then((h) => alive && setHistory(h)).catch(() => {});
    return () => { alive = false; };
  }, [p.id]);

  const { ref, close } = useSheet(onClose);
  useEffect(() => { closeRef.current?.focus({ preventScroll: true }); }, []);

  return (
    <>
      <div className="sheet-scrim" onClick={close} />
      <div ref={ref} className="sheet" role="dialog" aria-modal="true" aria-labelledby="sheet-title">
        <div className="grip" />
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
          <span className="sheet-tags"><StoreName id={store} />{sub && <span className="stamp">{sub.name}</span>}</span>
          <button ref={closeRef} className="icon-btn" onClick={close} aria-label="Cerrar"><X /></button>
        </div>
        <div className="media" style={{ position: 'relative' }}>
          <Seal p={p} />
          <ProductImage p={{ ...p, image: p.image?.replace('h=300&w=300', 'h=600&w=600') ?? null }} />
        </div>
        <h2 id="sheet-title">{p.name}</h2>
        <p className="muted" style={{ fontWeight: 600 }}>{p.size}</p>
        <div style={{ display: 'flex', alignItems: 'end', justifyContent: 'space-between', gap: 12, marginTop: 14 }}>
          <div>
            <Price value={p.price} />
            {p.refPrice != null && p.refFormat && <div className="price-ref num">{money(p.refPrice)}/{p.refFormat}</div>}
          </div>
          <QtyControl id={p.id} name={p.name} />
        </div>
        <h3 className="h-block" style={{ marginTop: 22 }}>En otros súper</h3>
        <CompareBlock p={p} />

        <h3 className="h-block" style={{ marginTop: 22 }}>Historial de precio</h3>
        {history ? <HistoryChart history={history.history} /> : <div className="skeleton" style={{ height: 110, marginTop: 10 }} />}
        {p.url && (
          <a className="btn block" style={{ marginTop: 18, textDecoration: 'none' }} href={p.url} target="_blank" rel="noopener noreferrer">
            Ver en la tienda de {storeName} <ExternalLink size={18} />
          </a>
        )}
      </div>
    </>
  );
}
