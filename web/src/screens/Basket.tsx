import { useMemo, useState } from 'react';
import { Check, ChevronRight, RotateCcw, ShoppingBasket, Trash2, TrendingDown, TrendingUp } from 'lucide-react';
import { inkOn, money, units } from '../lib';
import { href } from '../router';
import { basketLines, clearBasket, deleteBasket, dropMissing, reuseBasket, saveBasket, toast, toggleInCart, useStore } from '../store';
import type { Product } from '../types';
import { Price, ProductImage, QtyControl } from '../components/ProductTag';
import { Empty } from '../components/Chrome';

function defaultName() {
  return `Compra del ${new Date().toLocaleDateString('es-ES', { day: 'numeric', month: 'long' })}`;
}

export function Basket({ onOpen }: { onOpen: (p: Product) => void }) {
  const catalog = useStore((s) => s.catalog);
  const basket = useStore((s) => s.basket);
  const addedPrices = useStore((s) => s.addedPrices);
  const inCart = useStore((s) => s.inCart);
  const idx = useStore((s) => s.idx);
  const saved = useStore((s) => s.savedBaskets);
  const [name, setName] = useState(defaultName);
  const [confirmClear, setConfirmClear] = useState(false);

  const lines = useMemo(() => basketLines(), [basket, idx]); // eslint-disable-line react-hooks/exhaustive-deps
  const missing = Object.keys(basket).filter((id) => catalog && !idx.products.has(id));
  const total = lines.reduce((a, l) => a + l.p.price * l.qty, 0);
  const doneCount = lines.filter((l) => inCart.includes(l.p.id)).length;

  let changed = 0, delta = 0;
  for (const l of lines) {
    const was = addedPrices[l.p.id];
    if (was != null && Math.abs(was - l.p.price) >= 0.005) { changed++; delta += (l.p.price - was) * l.qty; }
  }

  // Agrupada por pasillo en el orden en que te los encuentras en la tienda
  const groups = useMemo(() => {
    const m = new Map<string, typeof lines>();
    for (const l of lines) {
      if (!m.has(l.p.section)) m.set(l.p.section, []);
      m.get(l.p.section)!.push(l);
    }
    return [...m.entries()];
  }, [lines]);

  return (
    <>
      <div className="row-head" style={{ marginTop: 6 }}>
        <h1 className="h-section">Tu cesta</h1>
        {lines.length > 0 && (
          confirmClear
            ? <div style={{ display: 'flex', gap: 8 }}>
                <button className="btn" onClick={() => setConfirmClear(false)}>No</button>
                <button className="btn danger" onClick={() => { clearBasket(); setConfirmClear(false); toast('Cesta vaciada'); }}>Sí, vaciar</button>
              </div>
            : <button className="btn" onClick={() => setConfirmClear(true)}><Trash2 size={18} /> Vaciar</button>
        )}
      </div>

      {lines.length === 0 && missing.length === 0 ? (
        <Empty icon={<ShoppingBasket size={34} strokeWidth={2.5} />} title="Tu cesta está vacía">
          <p className="muted">Entra en un pasillo y pulsa + en lo que necesites.</p>
          <a className="btn primary" href={href({ name: 'home' })} style={{ textDecoration: 'none', marginTop: 8 }}>Ir a los pasillos</a>
        </Empty>
      ) : (
        <>
          <section className="total-block" aria-label="Total de la cesta">
            <div className="label num">{units(basket)} unidades · {lines.length} productos</div>
            <div className="big"><Price value={total} className="" /></div>
            <div className="progress" role="progressbar" aria-valuemin={0} aria-valuemax={lines.length} aria-valuenow={doneCount} aria-label="Productos ya en el carro">
              <span style={{ width: `${lines.length ? (doneCount / lines.length) * 100 : 0}%` }} />
            </div>
            <div className="small num" style={{ fontWeight: 700, marginTop: 8 }}>
              {doneCount === lines.length ? '¡Todo en el carro!' : `${doneCount} de ${lines.length} en el carro · marca lo que vas cogiendo`}
            </div>
          </section>

          {changed > 0 && (
            <div className={`notice ${delta > 0 ? 'up' : 'down'}`}>
              {delta > 0 ? <TrendingUp size={20} /> : <TrendingDown size={20} />}
              <span className="num">{changed} producto{changed > 1 ? 's han' : ' ha'} cambiado de precio desde que lo{changed > 1 ? 's' : ''} añadiste ({delta > 0 ? '+' : ''}{money(delta)}).</span>
            </div>
          )}
          {missing.length > 0 && (
            <div className="notice up">
              <span>{missing.length} producto{missing.length > 1 ? 's ya no están' : ' ya no está'} a la venta. <button onClick={dropMissing}>Quitar</button></span>
            </div>
          )}

          {groups.map(([sectionId, ls]) => {
            const s = idx.sections.get(sectionId);
            const hue = s?.hue ?? '#B9BCC4';
            return (
              <section key={sectionId} aria-label={s?.name}>
                <div className="aisle-band" style={{ '--hue': hue, '--on-hue': inkOn(hue) } as React.CSSProperties}>
                  <span>{s?.name ?? 'Otros'}</span>
                  <span className="num small">{money(ls.reduce((a, l) => a + l.p.price * l.qty, 0))}</span>
                </div>
                {ls.map(({ p, qty }) => {
                  const done = inCart.includes(p.id);
                  return (
                    <div key={p.id} className={`line ${done ? 'done' : ''}`}>
                      <button className="check-hit" role="checkbox" aria-checked={done} aria-label={`${p.name} en el carro`} onClick={() => toggleInCart(p.id)}>
                        <span className="check" aria-checked={done}>{done && <Check size={18} strokeWidth={3.5} />}</span>
                      </button>
                      <button onClick={() => onOpen(p)} aria-label={`Ver ${p.name}`} style={{ display: 'contents' }}><ProductImage p={p} /></button>
                      <div className="info">
                        <div className="n">{p.name}</div>
                        <div className="s num">{p.size} · {money(p.price)}/ud</div>
                        <div className="line-foot">
                          <div className="p num">{money(p.price * qty)}</div>
                          <QtyControl id={p.id} name={p.name} />
                        </div>
                      </div>
                    </div>
                  );
                })}
              </section>
            );
          })}

          <form className="save-form" onSubmit={(e) => {
            e.preventDefault();
            if (!name.trim()) return toast('Ponle un nombre a la cesta');
            if (saveBasket(name.trim())) { toast(`Guardada: ${name.trim()}`); setName(defaultName()); }
          }}>
            <label className="sr-only" htmlFor="basket-name">Nombre de la cesta</label>
            <input id="basket-name" className="input" value={name} onChange={(e) => setName(e.target.value)} maxLength={60} />
            <button className="btn primary" type="submit">Guardar</button>
          </form>
          <p className="muted small" style={{ marginTop: 8 }}>Guardar la cesta la vacía y la añade a tus gastos.</p>
        </>
      )}

      <div className="row-head" style={{ marginTop: 40 }}>
        <h2 className="h-section">Cestas guardadas</h2>
        {saved.length > 0 && <span className="stamp num">{saved.length}</span>}
      </div>
      {saved.length === 0 ? (
        <p className="muted">Cuando guardes una cesta aparecerá aquí para repetirla con un toque.</p>
      ) : (
        [...saved].reverse().map((b) => {
          const now = Object.entries(b.items).reduce((a, [id, q]) => a + (idx.products.get(id)?.price ?? 0) * q, 0);
          const diff = now - b.total;
          return (
            <details key={b.id} className="saved">
              <summary>
                <div className="t">
                  <b>{b.name}</b>
                  <span className="muted small num">{new Date(b.date).toLocaleDateString('es-ES')} · {units(b.items)} uds. · {money(b.total)}</span>
                </div>
                <ChevronRight className="chev" aria-hidden />
              </summary>
              <div className="body">
                {Math.abs(diff) >= 0.01 && (
                  <div className={`notice ${diff > 0 ? 'up' : 'down'}`}><span className="num">Hoy costaría {money(now)} ({diff > 0 ? '+' : ''}{money(diff)})</span></div>
                )}
                <div style={{ marginTop: 10 }}>
                  {Object.entries(b.items).map(([id, q]) => {
                    const p = idx.products.get(id);
                    return (
                      <div key={id} className="item">
                        <span>{q} × {p?.name ?? b.snapshot?.[id]?.name ?? 'Producto retirado'}</span>
                        <b className="num">{money((p?.price ?? b.snapshot?.[id]?.price ?? 0) * q)}</b>
                      </div>
                    );
                  })}
                </div>
                <div className="total-actions">
                  <button className="btn primary" onClick={() => toast(`${reuseBasket(b.id)} productos añadidos a la cesta`)}><RotateCcw size={18} /> Repetir compra</button>
                  <button className="btn danger" onClick={() => { deleteBasket(b.id); toast('Cesta borrada'); }}><Trash2 size={18} /> Borrar</button>
                </div>
              </div>
            </details>
          );
        })
      )}
    </>
  );
}
