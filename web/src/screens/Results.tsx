import { useDeferredValue, useMemo, useState } from 'react';
import { SearchX, Sparkles, TrendingDown } from 'lucide-react';
import { priceDelta, searchProducts } from '../lib';
import { useStore } from '../store';
import type { Product } from '../types';
import { ProductTag } from '../components/ProductTag';
import { Empty } from '../components/Chrome';

const PAGE = 60;

type Sort = 'relevance' | 'price' | 'unit';

export function Results({ mode, q = '', onOpen }: { mode: 'search' | 'down' | 'new'; q?: string; onOpen: (p: Product) => void }) {
  const catalog = useStore((s) => s.catalog);
  const sections = catalog?.sections ?? [];
  const query = useDeferredValue(q);
  const [section, setSection] = useState<string | null>(null);
  const [sort, setSort] = useState<Sort>('relevance');
  const [limit, setLimit] = useState(PAGE);

  const base = useMemo(() => {
    if (!catalog) return [];
    if (mode === 'down') {
      return catalog.products.filter((p) => (priceDelta(p) ?? 0) < 0)
        .sort((a, b) => priceDelta(a)! / a.previousPrice! - priceDelta(b)! / b.previousPrice!);
    }
    if (mode === 'new') return catalog.products.filter((p) => p.isNew);
    return searchProducts(catalog.products, query);
  }, [catalog, mode, query]);

  const list = useMemo(() => {
    const filtered = section ? base.filter((p) => p.section === section) : base;
    if (sort === 'price') return [...filtered].sort((a, b) => a.price - b.price);
    if (sort === 'unit') return [...filtered].sort((a, b) => (a.refPrice ?? Infinity) - (b.refPrice ?? Infinity));
    return filtered;
  }, [base, section, sort]);

  const counts = useMemo(() => {
    const m = new Map<string, number>();
    for (const p of base) m.set(p.section, (m.get(p.section) ?? 0) + 1);
    return m;
  }, [base]);

  if (!catalog) return <div className="skeleton" style={{ height: 300 }} />;

  const title = mode === 'down' ? 'Han bajado de precio' : mode === 'new' ? 'Novedades' : `«${q.trim()}»`;
  const Icon = mode === 'down' ? TrendingDown : Sparkles;

  return (
    <>
      <div className="row-head" style={{ marginTop: 6 }}>
        <h1 className="h-section">{mode !== 'search' && <Icon size={30} strokeWidth={3} style={{ verticalAlign: '-3px', marginRight: 8 }} />}{title}</h1>
        <span className="stamp num">{list.length}</span>
      </div>

      {base.length > 0 && (
        <>
          <div className="chips" aria-label="Filtrar por pasillo">
            <button className="chip text-only" aria-pressed={section === null} onClick={() => setSection(null)}>Todo</button>
            {sections.filter((s) => counts.get(s.id)).map((s) => (
              <button key={s.id} className="chip text-only" aria-pressed={section === s.id} onClick={() => setSection(section === s.id ? null : s.id)}>
                <span className="dot" style={{ background: s.hue, marginLeft: -4 }} />{s.name} <span className="n num">{counts.get(s.id)}</span>
              </button>
            ))}
          </div>
          <div className="chips" aria-label="Ordenar" style={{ marginTop: 4 }}>
            {([['relevance', mode === 'down' ? 'Mayor bajada' : 'Relevancia'], ['price', 'Más barato'], ['unit', 'Mejor €/kg']] as [Sort, string][]).map(([k, label]) => (
              <button key={k} className="chip text-only" aria-pressed={sort === k} onClick={() => setSort(k)}>{label}</button>
            ))}
          </div>
        </>
      )}

      {list.length === 0 ? (
        <Empty icon={<SearchX size={34} strokeWidth={2.5} />} title={mode === 'search' ? 'Nada con ese nombre' : 'Nada por aquí hoy'}>
          <p className="muted">{mode === 'search' ? 'Prueba con otra palabra, como «yogur» o «tomate».' : 'Vuelve mañana: los precios se actualizan cada día.'}</p>
        </Empty>
      ) : (
        <div className="grid" style={{ marginTop: 14 }}>
          {list.slice(0, limit).map((p) => <ProductTag key={p.id} p={p} onOpen={onOpen} />)}
        </div>
      )}
      {list.length > limit && (
        <button className="btn block" style={{ marginTop: 18 }} onClick={() => setLimit(limit + PAGE)}>
          Ver más ({list.length - limit})
        </button>
      )}
    </>
  );
}
