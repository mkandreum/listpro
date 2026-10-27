import { useMemo } from 'react';
import { ArrowDown, ChevronRight, Sparkles, TrendingDown } from 'lucide-react';
import { priceDelta } from '../lib';
import { href } from '../router';
import { useStore } from '../store';
import type { Category, Product, Section } from '../types';
import { ProductTag } from '../components/ProductTag';
import { SectionIcon } from '../components/SectionIcon';

const hueStyle = (hue: string) => ({ '--hue': hue } as React.CSSProperties);

function CategoryTile({ c }: { c: Category }) {
  return (
    <a href={href({ name: 'category', id: c.id })} className="cat-tile" aria-label={`${c.name}, ${c.count} productos`}>
      <div className="pics" aria-hidden>
        {c.images.map((src) => <img key={src} src={src} alt="" loading="lazy" decoding="async" />)}
      </div>
      <div className="label"><b>{c.name}</b><ChevronRight size={18} /></div>
      <div className="meta">{c.count} productos</div>
    </a>
  );
}

function Aisle({ s }: { s: Section }) {
  return (
    <section className="aisle" id={`pasillo-${s.id}`} style={hueStyle(s.hue)} aria-labelledby={`h-${s.id}`}>
      <div className="banner">
        <div className="banner-copy">
          <div className="banner-title">
            <span className="h-icon"><SectionIcon name={s.icon} size={22} strokeWidth={2.2} /></span>
            <h2 id={`h-${s.id}`}>{s.name}</h2>
          </div>
          <p>{s.blurb}</p>
        </div>
        <div className="collage" aria-hidden>
          {s.images.slice(0, 3).map((src) => <img key={src} src={src} alt="" loading="lazy" decoding="async" />)}
        </div>
        <span className="count">{s.count.toLocaleString('es-ES')} <ChevronRight size={16} /></span>
      </div>
      <div className="cat-row">
        {s.categories.map((c) => <CategoryTile key={c.id} c={c} />)}
      </div>
    </section>
  );
}

export function Home({ onOpen }: { onOpen: (p: Product) => void }) {
  const catalog = useStore((s) => s.catalog);
  const deals = useMemo(() => {
    if (!catalog) return [];
    return catalog.products
      .filter((p) => (priceDelta(p) ?? 0) < 0)
      .sort((a, b) => priceDelta(a)! / a.previousPrice! - priceDelta(b)! / b.previousPrice!);
  }, [catalog]);
  const fresh = useMemo(() => catalog?.products.filter((p) => p.isNew).length ?? 0, [catalog]);

  if (!catalog) return <HomeSkeleton />;

  return (
    <>
      <nav className="stats" aria-label="Accesos rápidos">
        {deals.length > 0 && (
          <a className="stat" href={href({ name: 'deals', kind: 'down' })}>
            <span className="ic"><TrendingDown size={24} strokeWidth={2.4} /></span>
            <span><span className="t">Bajadas</span><span className="v">{deals.length}</span></span>
          </a>
        )}
        {fresh > 0 && (
          <a className="stat" href={href({ name: 'deals', kind: 'new' })} style={hueStyle('#7C5CFF')}>
            <span className="ic"><Sparkles size={22} strokeWidth={2.2} /></span>
            <span><span className="t">Novedades</span><span className="v" style={{ color: 'var(--violet)' }}>{fresh}</span></span>
          </a>
        )}
        {catalog.sections.map((s) => (
          <button key={s.id} className="stat" style={hueStyle(s.hue)} onClick={() => document.getElementById(`pasillo-${s.id}`)?.scrollIntoView({ behavior: 'smooth' })}>
            <span className="ic"><SectionIcon name={s.icon} size={22} strokeWidth={2.2} /></span>
            <span><span className="t">{s.name}</span><span className="v plain">{s.count.toLocaleString('es-ES')}</span></span>
          </button>
        ))}
      </nav>

      {deals.length > 0 && (
        <section aria-labelledby="h-deals">
          <div className="row-head">
            <h2 className="h-section" id="h-deals"><span className="h-icon"><ArrowDown size={24} strokeWidth={2.6} /></span>Han bajado</h2>
            <a href={href({ name: 'deals', kind: 'down' })} className="btn ghost">
              Ver <span className="num">{deals.length}</span> <ChevronRight size={18} />
            </a>
          </div>
          <div className="deals">
            {deals.slice(0, 14).map((p) => <ProductTag key={p.id} p={p} onOpen={onOpen} className="deal" />)}
          </div>
        </section>
      )}

      {catalog.sections.map((s) => <Aisle key={s.id} s={s} />)}
    </>
  );
}

function HomeSkeleton() {
  return (
    <div aria-busy="true" aria-label="Cargando precios">
      <div className="stats">{Array.from({ length: 3 }, (_, i) => <div key={i} className="skeleton" style={{ height: 72 }} />)}</div>
      <div className="row-head"><div className="skeleton" style={{ width: 220, height: 40 }} /></div>
      <div className="deals">{Array.from({ length: 4 }, (_, i) => <div key={i} className="skeleton" style={{ height: 300 }} />)}</div>
      <div className="skeleton" style={{ height: 152, marginTop: 30 }} />
    </div>
  );
}
