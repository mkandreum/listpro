import { useEffect, useState } from 'react';
import { ArrowRightLeft, Check, ChevronDown, Plus } from 'lucide-react';
import { api } from '../api';
import { money } from '../lib';
import { changeQty, moveToStore, setStore, storeInfo, toast, useStore } from '../store';
import type { Comparison, Product, StoreMatch } from '../types';
import { haptic } from '../fx/haptics';
import { Sheet } from './Share';
import { MATCH_LABEL } from './Compare';

/** Foto de producto pequeña; si no carga (algunas tiendas cambian las URL), un hueco en vez del icono roto. */
export function Thumb({ src }: { src: string | null }) {
  const [broken, setBroken] = useState(false);
  return src && !broken ? <img src={src} alt="" loading="lazy" onError={() => setBroken(true)} /> : <span className="ph" />;
}

/** Logotipo original de cada súper; si no hay, la inicial en su color. */
const STORE_LOGOS: Record<string, string> = {
  mercadona: '/logos/mercadona.png',
  consum: '/logos/consum.png',
  eci: '/logos/eci.png',
};

export function StoreDot({ id, size = 22 }: { id: string; size?: number }) {
  const s = useStore((st) => storeInfo(id, st));
  const logo = STORE_LOGOS[id];
  return (
    <span
      className={`store-dot${logo ? ' has-logo' : ''}`}
      style={{ '--hue': s.hue, '--s': `${size}px` } as React.CSSProperties}
      aria-hidden
    >
      {logo ? <img src={logo} alt="" loading="lazy" /> : s.name[0]}
    </span>
  );
}

export function StoreName({ id }: { id: string }) {
  const s = useStore((st) => storeInfo(id, st));
  return <span className="store-name"><StoreDot id={id} size={18} />{s.name}</span>;
}

/** Elegir súper: baldosas con el color de cada uno, cuántos productos tiene y cuándo se actualizó. */
export function StoreChoice({ value, onChange }: { value: string; onChange: (id: string) => void }) {
  const stores = useStore((s) => s.stores);
  return (
    <div className="store-choice" role="radiogroup" aria-label="Súper">
      {stores.map((s) => (
        <button key={s.id} type="button" role="radio" aria-checked={value === s.id} className="store-tile" style={{ '--hue': s.hue } as React.CSSProperties}
          onClick={() => { haptic.tick(); onChange(s.id); }}>
          <StoreDot id={s.id} size={40} />
          <span className="t"><b>{s.name}</b>{s.count > 0 && <span className="muted small num">{s.count.toLocaleString('es-ES')} productos</span>}</span>
          <span className="tick" aria-hidden><Check size={14} strokeWidth={3} /></span>
        </button>
      ))}
    </div>
  );
}

/** Botón de la cabecera: el súper que se ve y, al pulsar, la hoja para cambiarlo. */
export function StoreButton() {
  const store = useStore((s) => s.store);
  const info = useStore((s) => storeInfo(s.store, s));
  const [open, setOpen] = useState(false);
  return (
    <>
      <button className="store-btn" onClick={() => { haptic.tap(); setOpen(true); }} aria-label={`Súper: ${info.name}. Cambiar súper`} style={{ '--hue': info.hue } as React.CSSProperties}>
        <StoreDot id={store} size={24} />
        <span className="l"><span className="k">Cambiar súper</span><b>{info.name}</b></span>
        <ChevronDown size={16} strokeWidth={2.6} />
      </button>
      {open && <StoreSheet onClose={() => setOpen(false)} />}
    </>
  );
}

export function StoreSheet({ onClose }: { onClose: () => void }) {
  const store = useStore((s) => s.store);
  return (
    <Sheet onClose={onClose} label="Cambiar de súper">
      <h2 style={{ marginTop: 4 }}>¿En qué súper compras?</h2>
      <p className="muted" style={{ marginTop: 6 }}>Verás sus productos y sus precios de hoy. Tu cesta guarda lo de cada súper por separado.</p>
      <div style={{ marginTop: 16 }}>
        <StoreChoice value={store} onChange={(id) => {
          setStore(id);
          toast(`Ahora ves ${storeInfo(id).name}`);
          onClose();
        }} />
      </div>
      <p className="muted small" style={{ marginTop: 12 }}>ListPro no está afiliada a ningún súper. Los precios se leen cada día de sus tiendas online públicas.</p>
    </Sheet>
  );
}

const perUnit = (price: number | null, format: string | null) => (price != null && format ? `${money(price)}/${format}` : '');

/** En la ficha: lo mismo (o lo más parecido) en los otros súper, con su precio y para añadirlo a su cesta. */
export function CompareBlock({ p }: { p: Product }) {
  const [data, setData] = useState<Comparison | null>(null);
  const [failed, setFailed] = useState(false);
  const [more, setMore] = useState<string | null>(null);
  const basket = useStore((s) => s.basket);
  useEffect(() => {
    let alive = true;
    setData(null);
    setFailed(false);
    api.compare(p.id).then((c) => alive && setData(c)).catch(() => alive && setFailed(true));
    return () => { alive = false; };
  }, [p.id]);

  if (failed) return null;
  if (!data) return <div className="skeleton" style={{ height: 120, marginTop: 10 }} />;
  if (!data.matches.length) return <p className="muted small" style={{ marginTop: 8 }}>No lo hemos encontrado en los otros súper.</p>;
  const bestSame = data.matches.filter((m) => m.match === 'same').sort((a, b) => a.product.price - b.product.price)[0];
  const cheapest = Math.min(p.price, bestSame?.product.price ?? Infinity);

  const row = (m: StoreMatch, x = m.product, alt = false) => {
    // Mismo producto: por precio. Equivalente o parecido: por kilo/litro calculado igual en todos los súper
    const mine = data.per, theirs = x.per;
    const cheaper = m.match === 'same' ? x.price < p.price - 0.004 : mine && theirs && mine.label === theirs.label ? theirs.value < mine.value - 0.004 : false;
    const inBasket = basket[x.id] ?? 0;
    return (
      <div key={x.id} className={`cmp-row ${alt ? 'alt' : ''}`}>
        <Thumb src={x.image} />
        <div className="info">
          <span className="cmp-store"><StoreName id={m.store} />{!alt && <span className={`cmp-kind ${x.match ?? m.match}`}>{MATCH_LABEL[x.match ?? m.match]}</span>}</span>
          <b>{x.name}</b>
          <span className="muted small num">{[x.size, x.per ? `${money(x.per.value)}/${x.per.label}` : perUnit(x.refPrice, x.refFormat)].filter(Boolean).join(' · ')}</span>
        </div>
        <div className="right num">
          <b className={cheaper ? 'cheaper' : undefined}>{money(x.price)}</b>
          {cheaper && m.match === 'same' && <span className="saving">−{money(p.price - x.price)}</span>}
          {cheaper && m.match !== 'same' && <span className="saving">más barato/{x.per?.label}</span>}
          <button className={`cmp-add ${inBasket ? 'on' : ''}`} onClick={() => {
            haptic.add();
            changeQty(x.id, 1);
            toast(`Añadido a tu cesta de ${storeInfo(m.store).name}`);
          }} aria-label={`Añadir ${x.name} a la cesta de ${storeInfo(m.store).name}`}>
            {inBasket ? <span className="num">{inBasket}</span> : <Plus size={18} strokeWidth={2.6} />}
          </button>
        </div>
      </div>
    );
  };

  return (
    <div className="cmp">
      {data.matches.map((m) => (
        <div key={m.store}>
          {row(m)}
          {m.alternatives.length > 0 && (
            more === m.store
              ? m.alternatives.map((a) => row(m, a, true))
              : <button className="link-btn cmp-more" onClick={() => setMore(m.store)}>Ver {m.alternatives.length} parecidos más en {storeInfo(m.store).name}</button>
          )}
        </div>
      ))}
      {basket[p.id] > 0 && bestSame && bestSame.product.price < p.price - 0.004 && (
        <button className="btn block" style={{ marginTop: 10 }} onClick={() => {
          haptic.success();
          moveToStore(p.id, bestSame.product.id, bestSame.product.price);
          toast(`Movido a tu cesta de ${storeInfo(bestSame.store).name}`);
        }}><ArrowRightLeft size={18} /> Pasar a la cesta de {storeInfo(bestSame.store).name}</button>
      )}
      <p className="muted small" style={{ marginTop: 8 }}>
        {cheapest < p.price - 0.004 ? 'Hay un súper donde te sale más barato. ' : data.matches.some((m) => m.match === 'same') ? 'Aquí ya está al mejor precio. ' : ''}
        «Equivalente» es el mismo producto de otra marca; «Parecido», del mismo tipo con alguna diferencia. Se comparan por kilo o litro.
      </p>
    </div>
  );
}

