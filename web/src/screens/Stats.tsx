import { useMemo } from 'react';
import { BarChart3 } from 'lucide-react';
import { money } from '../lib';
import { href } from '../router';
import { useStore } from '../store';
import { Empty } from '../components/Chrome';

export function Stats() {
  const saved = useStore((s) => s.savedBaskets);
  const idx = useStore((s) => s.idx);

  const data = useMemo(() => {
    const now = new Date();
    const months = Array.from({ length: 6 }, (_, i) => new Date(now.getFullYear(), now.getMonth() - 5 + i, 1));
    const key = (d: Date) => `${d.getFullYear()}-${d.getMonth()}`;
    const byMonth = new Map(months.map((m) => [key(m), 0]));
    for (const b of saved) {
      const k = key(new Date(b.date));
      if (byMonth.has(k)) byMonth.set(k, byMonth.get(k)! + b.total);
    }
    const bySection = new Map<string, number>();
    const byProduct = new Map<string, number>();
    for (const b of saved) {
      for (const [id, q] of Object.entries(b.items)) {
        const snap = b.snapshot?.[id];
        const p = idx.products.get(id);
        const sec = snap?.section ?? p?.section ?? 'otros';
        bySection.set(sec, (bySection.get(sec) ?? 0) + (snap?.price ?? p?.price ?? 0) * q);
        byProduct.set(id, (byProduct.get(id) ?? 0) + q);
      }
    }
    return {
      months: months.map((m) => ({ label: m.toLocaleDateString('es-ES', { month: 'short' }), value: byMonth.get(key(m))!, current: key(m) === key(now) })),
      thisMonth: byMonth.get(key(now))!,
      avg: saved.reduce((a, b) => a + b.total, 0) / Math.max(1, saved.length),
      sections: [...bySection.entries()].sort((a, b) => b[1] - a[1]),
      top: [...byProduct.entries()].sort((a, b) => b[1] - a[1]).slice(0, 6),
    };
  }, [saved, idx]);

  if (!saved.length) {
    return (
      <>
        <h1 className="h-section" style={{ marginTop: 6 }}>Tus gastos</h1>
        <Empty icon={<BarChart3 size={34} strokeWidth={2.5} />} title="Aún no hay compras guardadas">
          <p className="muted">Guarda tu cesta después de comprar y aquí verás cuánto gastas cada mes y en qué.</p>
          <a className="btn primary" href={href({ name: 'basket' })} style={{ textDecoration: 'none', marginTop: 8 }}>Ir a la cesta</a>
        </Empty>
      </>
    );
  }

  const max = Math.max(1, ...data.months.map((m) => m.value));
  const secTotal = data.sections.reduce((a, [, v]) => a + v, 0) || 1;

  return (
    <>
      <h1 className="h-section" style={{ marginTop: 6 }}>Tus gastos</h1>
      <section className="panel">
        <div className="figures">
          <div className="figure"><b>{money(data.thisMonth)}</b><span>este mes</span></div>
          <div className="figure"><b>{money(data.avg)}</b><span>por compra</span></div>
          <div className="figure"><b className="num">{saved.length}</b><span>compras</span></div>
        </div>
        <div className="bars" role="img" aria-label="Gasto de los últimos seis meses">
          {data.months.map((m) => (
            <div key={m.label} className={`bar ${m.current ? 'current' : ''}`}>
              {m.value > 0 && <b>{Math.round(m.value)} €</b>}
              <i style={{ height: `${(m.value / max) * 100}%` }} />
              <small>{m.label}</small>
            </div>
          ))}
        </div>
      </section>

      <section className="panel">
        <h2 className="h-block">¿En qué se va el dinero?</h2>
        <div className="stack" aria-hidden>
          {data.sections.map(([id, v]) => <span key={id} style={{ width: `${(v / secTotal) * 100}%`, background: idx.sections.get(id)?.hue ?? '#B9BCC4' }} />)}
        </div>
        <div className="legend">
          {data.sections.map(([id, v]) => (
            <div key={id}>
              <i style={{ background: idx.sections.get(id)?.hue ?? '#B9BCC4' }} />
              <span>{idx.sections.get(id)?.name ?? 'Otros'}</span>
              <span>{money(v)} · {Math.round((v / secTotal) * 100)}%</span>
            </div>
          ))}
        </div>
      </section>

      <section className="panel">
        <h2 className="h-block">Lo que más compras</h2>
        {data.top.map(([id, q]) => {
          const p = idx.products.get(id);
          const name = p?.name ?? saved.find((b) => b.snapshot?.[id])?.snapshot?.[id]?.name ?? 'Producto retirado';
          return <div key={id} className="kv"><span>{name}</span><b className="num">{q} uds.{p ? ` · ${money(p.price)}` : ''}</b></div>;
        })}
      </section>
    </>
  );
}
