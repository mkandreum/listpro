import { useMemo } from 'react';
import { ArrowRight, ArrowUpRight, Sparkles, TrendingDown } from 'lucide-react';
import { inkOn, priceDelta } from '../lib';
import { href } from '../router';
import { useStore } from '../store';
import type { Category, Product, Section } from '../types';
import { ProductTag } from '../components/ProductTag';

function CategoryCard({ c, wide }: { c: Category; wide: boolean }) {
  return (
    <a href={href({ name: 'category', id: c.id })} className={`cat-card ${wide ? 'wide' : ''}`} style={{ textDecoration: 'none' }}>
      <span className="go" aria-hidden><ArrowUpRight size={18} strokeWidth={2.75} /></span>
      <div>
        <div className="name">{c.name}</div>
        <div className="meta num">{c.count} productos · {c.subcategories.length} secciones</div>
      </div>
      <div className="stickers" aria-hidden>
        {c.images.map((src) => <img key={src} className="sticker" src={src} alt="" loading="lazy" decoding="async" />)}
      </div>
    </a>
  );
}

function Aisle({ s }: { s: Section }) {
  // La categoría más grande de cada pasillo ocupa dos columnas: el mosaico respira y no es una rejilla plana.
  const biggest = s.categories.reduce((a, c) => (c.count > a.count ? c : a), s.categories[0]);
  return (
    <section className="aisle" id={`pasillo-${s.id}`} style={{ '--hue': s.hue, '--on-hue': inkOn(s.hue) } as React.CSSProperties} aria-labelledby={`h-${s.id}`}>
      <div className="aisle-head">
        <h2 className="h-section" id={`h-${s.id}`}>{s.name}</h2>
        <span className="count num">{s.count.toLocaleString('es-ES')}</span>
      </div>
      <div className="cat-grid">
        {s.categories.map((c) => <CategoryCard key={c.id} c={c} wide={c === biggest && s.categories.length > 2} />)}
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
      <nav className="chips" aria-label="Ir a un pasillo">
        {deals.length > 0 && (
          <a className="chip text-only" href={href({ name: 'deals', kind: 'down' })} style={{ textDecoration: 'none' }}>
            <TrendingDown size={18} strokeWidth={2.75} /> Bajadas <span className="n num">{deals.length}</span>
          </a>
        )}
        {fresh > 0 && (
          <a className="chip text-only" href={href({ name: 'deals', kind: 'new' })} style={{ textDecoration: 'none' }}>
            <Sparkles size={18} strokeWidth={2.5} /> Novedades <span className="n num">{fresh}</span>
          </a>
        )}
        {catalog.sections.map((s) => (
          <button key={s.id} className="chip text-only" onClick={() => document.getElementById(`pasillo-${s.id}`)?.scrollIntoView({ behavior: 'smooth' })}>
            <span className="dot" style={{ background: s.hue, marginLeft: -4 }} />{s.name}
          </button>
        ))}
      </nav>

      {deals.length > 0 && (
        <section aria-labelledby="h-deals">
          <div className="row-head">
            <h2 className="h-section" id="h-deals">Han bajado</h2>
            <a href={href({ name: 'deals', kind: 'down' })} className="btn" style={{ minHeight: 44, textDecoration: 'none' }}>
              Ver {deals.length} <ArrowRight size={18} />
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
      <div className="row-head"><div className="skeleton" style={{ width: 200, height: 36 }} /></div>
      <div className="deals">{Array.from({ length: 4 }, (_, i) => <div key={i} className="skeleton" style={{ height: 290 }} />)}</div>
      <div className="skeleton" style={{ height: 380, marginTop: 26 }} />
    </div>
  );
}
