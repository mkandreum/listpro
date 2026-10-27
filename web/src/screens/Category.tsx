import { Fragment, useEffect, useMemo, useRef, useState } from 'react';
import { ChevronLeft } from 'lucide-react';
import { money } from '../lib';
import { SectionIcon } from '../components/SectionIcon';
import { href } from '../router';
import { useStore } from '../store';
import type { Product } from '../types';
import { ProductTag } from '../components/ProductTag';
import { Empty } from '../components/Chrome';

export function CategoryScreen({ id, sub, onOpen }: { id: number; sub?: number; onOpen: (p: Product) => void }) {
  const catalog = useStore((s) => s.catalog);
  const cat = useStore((s) => s.idx.categories.get(id));
  const section = useStore((s) => (cat ? s.idx.sections.get(cat.section) : undefined));
  const [active, setActive] = useState<number | undefined>(sub ?? cat?.subcategories[0]?.id);
  const railRef = useRef<HTMLDivElement>(null);

  const bySub = useMemo(() => {
    const m = new Map<number, Product[]>();
    if (!catalog || !cat) return m;
    for (const s of cat.subcategories) m.set(s.id, []);
    for (const p of catalog.products) if (p.category === id) m.get(p.subcategory)?.push(p);
    return m;
  }, [catalog, cat, id]);

  const cheapest = useMemo(() => {
    let min: Product | null = null;
    for (const list of bySub.values()) for (const p of list) if (p.refPrice != null && (!min || p.price < min.price)) min = p;
    return min;
  }, [bySub]);

  useEffect(() => {
    window.scrollTo({ top: 0 });
    if (sub) requestAnimationFrame(() => document.getElementById(`sub-${sub}`)?.scrollIntoView());
  }, [id, sub]);

  // El raíl sigue al scroll: la subcategoría visible queda marcada y centrada.
  useEffect(() => {
    if (!cat) return;
    const io = new IntersectionObserver((entries) => {
      const visible = entries.filter((e) => e.isIntersecting).sort((a, b) => a.boundingClientRect.top - b.boundingClientRect.top)[0];
      if (visible) setActive(Number((visible.target as HTMLElement).dataset.sub));
    }, { rootMargin: '-160px 0px -60% 0px' });
    document.querySelectorAll('[data-sub]').forEach((el) => io.observe(el));
    return () => io.disconnect();
  }, [cat, bySub]);

  useEffect(() => {
    const chip = railRef.current?.querySelector<HTMLElement>(`[data-chip="${active}"]`);
    chip?.scrollIntoView({ inline: 'center', block: 'nearest', behavior: 'smooth' });
  }, [active]);

  if (!catalog) return <div className="skeleton" style={{ height: 260, marginTop: 16 }} />;
  if (!cat || !section) {
    return <Empty icon={<ChevronLeft />} title="Esta categoría ya no existe"><a className="btn" href="#/">Volver a los pasillos</a></Empty>;
  }

  const hueStyle = { '--hue': section.hue } as React.CSSProperties;

  return (
    <>
      <header className="cat-hero" style={hueStyle}>
        <div className="banner">
          <div className="banner-copy">
            <a className="back" href={href({ name: 'home' })} onClick={(e) => { if (history.length > 1) { e.preventDefault(); history.back(); } }}>
              <ChevronLeft size={20} strokeWidth={2.4} /> {section.name}
            </a>
            <div className="banner-title">
              <span className="h-icon"><SectionIcon name={section.icon} size={22} strokeWidth={2.2} /></span>
              <h1>{cat.name}</h1>
            </div>
            <p className="facts num">{cat.count} productos{cheapest ? ` · desde ${money(cheapest.price)}` : ''}</p>
          </div>
          <div className="collage" aria-hidden>
            {cat.images.map((src) => <img key={src} src={src} alt="" />)}
          </div>
        </div>
      </header>

      {cat.subcategories.length > 1 && (
        <div className="subrail glass" style={{ boxShadow: 'var(--hard)' }}>
          <div className="chips" ref={railRef} role="tablist" aria-label={`Secciones de ${cat.name}`}>
            {cat.subcategories.map((s) => (
              <button
                key={s.id}
                data-chip={s.id}
                role="tab"
                className="chip"
                aria-pressed={active === s.id}
                aria-selected={active === s.id}
                onClick={() => document.getElementById(`sub-${s.id}`)?.scrollIntoView({ behavior: 'smooth' })}
              >
                {s.image ? <img src={s.image} alt="" /> : <span className="dot" style={{ background: section.hue }} />}
                {s.name} <span className="n num">{s.count}</span>
              </button>
            ))}
          </div>
        </div>
      )}

      {cat.subcategories.map((s) => {
        const list = bySub.get(s.id) ?? [];
        let lastGroup = '';
        return (
          <section key={s.id} id={`sub-${s.id}`} data-sub={s.id} aria-labelledby={`hs-${s.id}`}>
            <div className="sub-head">
              <h2 className="h-block" id={`hs-${s.id}`}>{s.name}</h2>
              <span className="muted small num">{list.length}</span>
            </div>
            <div className="grid">
              {list.map((p) => {
                const showGroup = p.group && p.group !== lastGroup && p.group !== s.name;
                lastGroup = p.group;
                return (
                  <Fragment key={p.id}>
                    {showGroup && <div className="group-label">{p.group}</div>}
                    <ProductTag p={p} onOpen={onOpen} />
                  </Fragment>
                );
              })}
            </div>
          </section>
        );
      })}
    </>
  );
}
