import { useEffect, useMemo, useState } from 'react';
import { Check, ChevronRight, ReceiptText, RotateCcw, ShoppingBasket, Trash2, TrendingDown, TrendingUp } from 'lucide-react';
import { money, units } from '../lib';
import { href, navigate } from '../router';
import {
  basketLines, checkoutLines, clearBasket, deleteBasket, dropMissing, finishShop, isShared, missingIds, purchaseShares, reuseBasket, storeInfo, storeOf,
  toast, toggleInCart, useStore,
} from '../store';
import type { Product, SplitMode } from '../types';
import { PillBar } from '../components/Pills';
import { StoreName } from '../components/Stores';
import { AlternativesSheet, bestSaving, CompareCard, SavingChip, SavingsSheet, useBasketCompare } from '../components/Compare';
import { Price, ProductImage, QtyControl } from '../components/ProductTag';
import { Empty } from '../components/Chrome';
import { Avatar, SharePill, ShareSheet, Sheet } from '../components/Share';
import { haptic } from '../fx/haptics';
import { useTweened } from '../fx/motion';

function defaultName(store?: string) {
  const day = new Date().toLocaleDateString('es-ES', { day: 'numeric', month: 'long' });
  return store ? `Compra en ${storeInfo(store).name} del ${day}` : `Compra del ${day}`;
}

export function Basket({ onOpen, join }: { onOpen: (p: Product) => void; join?: string }) {
  const basket = useStore((s) => s.basket);
  const store = useStore((s) => s.store);
  const allStores = useStore((s) => s.stores);
  const catalogs = useStore((s) => s.catalogs);
  const addedPrices = useStore((s) => s.addedPrices);
  const inCart = useStore((s) => s.inCart);
  const idx = useStore((s) => s.idx);
  const saved = useStore((s) => s.savedBaskets);
  const members = useStore((s) => s.members);
  const meId = useStore((s) => s.meId);
  const addedBy = useStore((s) => s.addedBy);
  const pickedBy = useStore((s) => s.pickedBy);
  // Con cuenta siempre hay lista en el servidor; "compartida" es cuando hay alguien más
  const shared = useStore((s) => s.sync.code != null && s.members.length > 1);
  const [confirmClear, setConfirmClear] = useState(false);
  const [sharing, setSharing] = useState(false);
  const [checkout, setCheckout] = useState(false);
  const member = (id?: string) => members.find((m) => m.id === id);
  const showWho = shared && members.length > 1;

  // #/compartir (desde Ajustes) abre la hoja de compartir; las invitaciones (#/unirse/...) las lleva App
  useEffect(() => { if (join === '') setSharing(true); }, [join]);
  const closeShare = () => { setSharing(false); if (join !== undefined) navigate({ name: 'basket' }, true); };

  // Una cesta por súper: el activo primero y luego los demás que tengan algo
  const stores = useMemo(() => {
    const inBasket = new Set(Object.keys(basket).map(storeOf));
    return allStores.map((s) => s.id).filter((id) => inBasket.has(id)).sort((a, b) => (a === store ? -1 : b === store ? 1 : 0));
  }, [basket, store, allStores]);
  const [picked, setPicked] = useState<string | null>(null);
  const tab = picked && stores.includes(picked) ? picked : stores.includes(store) ? store : stores[0] ?? store;
  const multi = stores.length > 1;
  const lines = useMemo(() => basketLines(tab), [basket, idx, tab]); // eslint-disable-line react-hooks/exhaustive-deps
  const subtotal = (s: string) => basketLines(s).reduce((a, l) => a + l.p.price * l.qty, 0);
  const missing = missingIds().filter((id) => storeOf(id) === tab);
  const loadingStore = Object.keys(basket).some((id) => storeOf(id) === tab) && !catalogs[tab];

  // Equivalentes de cada producto en los otros súper y coste de la cesta en cada uno
  const { alts, cost: basketCost } = useBasketCompare(lines);
  const savings = lines.flatMap((l) => { const o = bestSaving(alts[l.p.id]); return o ? [{ line: l, o }] : []; });
  const [altLine, setAltLine] = useState<{ p: Product; qty: number } | null>(null);
  const [showSavings, setShowSavings] = useState(false);
  const total = lines.reduce((a, l) => a + l.p.price * l.qty, 0);
  const shownTotal = useTweened(total);
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
        <h1 className="h-section">{shared ? 'Cesta común' : 'Tu cesta'}</h1>
        <SharePill onClick={() => setSharing(true)} />
      </div>

      {multi && (
        <>
          <PillBar label="Cestas por súper" active={stores.indexOf(tab)} onSelect={(i) => { haptic.tap(); setPicked(stores[i]); }}
            items={stores.map((s) => ({ key: s, label: storeInfo(s).name, hue: storeInfo(s).hue, sub: money(subtotal(s)) }))} />
          <p className="muted small" style={{ margin: '-6px 4px 12px' }}>
            Tienes cesta en {stores.length} súper: en total {money(stores.reduce((a, s) => a + subtotal(s), 0))}. Termina la compra de cada uno por separado.
          </p>
        </>
      )}
      {!multi && lines.length > 0 && tab !== store && (
        <p className="muted small" style={{ margin: '-6px 4px 12px' }}>Esta cesta es de <StoreName id={tab} /> aunque ahora estés viendo {storeInfo(store).name}.</p>
      )}

      {loadingStore ? (
        <div className="skeleton" style={{ height: 220 }} />
      ) : lines.length === 0 && missing.length === 0 ? (
        <Empty icon={<ShoppingBasket size={34} strokeWidth={2.5} />} title="Tu cesta está vacía">
          <p className="muted">Entra en un pasillo y pulsa + en lo que necesites.</p>
          <a className="btn primary" href={href({ name: 'home' })} style={{ textDecoration: 'none', marginTop: 8 }}>Ir a los pasillos</a>
        </Empty>
      ) : (
        <>
          <section className="total-block" aria-label="Total de la cesta" style={multi ? { '--store': storeInfo(tab).hue } as React.CSSProperties : undefined}>
            <div className="label num">{multi && `${storeInfo(tab).name} · `}{lines.reduce((a, l) => a + l.qty, 0)} unidades · {lines.length} productos</div>
            <div className="big num"><Price value={shownTotal} className="" /></div>
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
          <CompareCard store={tab} total={total} cost={basketCost} savings={savings} onSavings={() => setShowSavings(true)} />
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
                <div className="aisle-band" style={{ '--hue': hue } as React.CSSProperties}>
                  <span>{s?.name ?? 'Otros'}</span>
                  <span className="sum">{money(ls.reduce((a, l) => a + l.p.price * l.qty, 0))}</span>
                </div>
                {ls.map(({ p, qty }, i) => {
                  const done = inCart.includes(p.id);
                  return (
                    <div key={p.id} className={`line ${done ? 'done' : ''}`} style={{ '--d': Math.min(i, 8) } as React.CSSProperties}>
                      <button className="check-hit" role="checkbox" aria-checked={done} aria-label={`${p.name} en el carro`} onClick={() => { if (inCart.includes(p.id)) haptic.tick(); else haptic.success(); toggleInCart(p.id); }}>
                        <span className="check" aria-checked={done}>{done && <Check size={18} strokeWidth={3.5} />}</span>
                      </button>
                      <button onClick={() => onOpen(p)} aria-label={`Ver ${p.name}`} style={{ display: 'contents' }}><ProductImage p={p} /></button>
                      <div className="info">
                        <div className="n">{p.name}</div>
                        <div className="s num">{p.size} · {money(p.price)}/ud</div>
                        {showWho && (addedBy[p.id] || (done && pickedBy[p.id])) && (
                          <div className="who-line">
                            {addedBy[p.id] && <span><Avatar m={member(addedBy[p.id])} size={18} />{addedBy[p.id] === meId ? 'Lo pediste tú' : `Lo pide ${member(addedBy[p.id])?.name ?? 'alguien'}`}</span>}
                            {done && pickedBy[p.id] && <span className="picked"><Check size={13} strokeWidth={3} />{pickedBy[p.id] === meId ? 'Lo cogiste tú' : `Lo cogió ${member(pickedBy[p.id])?.name ?? 'alguien'}`}</span>}
                          </div>
                        )}
                        <div className="line-foot">
                          <div className="p num">{money(p.price * qty)}</div>
                          <QtyControl id={p.id} name={p.name} />
                        </div>
                        <SavingChip a={alts[p.id]} onOpen={() => setAltLine({ p, qty })} />
                      </div>
                    </div>
                  );
                })}
              </section>
            );
          })}

          <button className="btn primary block cta" onClick={() => { haptic.tap(); setCheckout(true); }}>
            <ReceiptText size={20} /> {doneCount > 0 && doneCount < lines.length ? `Terminar compra (${doneCount} en el carro)` : 'Terminar compra'}
          </button>
          <p className="muted small center-text" style={{ marginTop: 8 }}>
            {shared ? 'Se guarda en el historial y, si queréis, se reparte en las cuentas.' : 'Se guarda en tu historial de gastos y vacía la cesta.'}
          </p>
          <div className="center-text" style={{ marginTop: 14 }}>
            {confirmClear
              ? <div style={{ display: 'inline-flex', gap: 8 }}>
                  <button className="btn" onClick={() => setConfirmClear(false)}>No</button>
                  <button className="btn danger" onClick={() => { haptic.warn(); clearBasket(); setConfirmClear(false); toast('Cesta vaciada'); }}>Sí, vaciar{shared ? ' para todos' : ''}</button>
                </div>
              : <button className="link-btn" onClick={() => setConfirmClear(true)}><Trash2 size={16} /> Vaciar la cesta</button>}
          </div>
        </>
      )}

      {!shared && lines.length > 0 && (
        <button className="share-promo" onClick={() => setSharing(true)}>
          <span className="art"><ShoppingBasket size={22} /></span>
          <span><b>¿Compráis entre varios?</b><span className="muted small">Comparte la cesta y repartid los gastos sin hacer cuentas.</span></span>
          <ChevronRight className="chev" aria-hidden />
        </button>
      )}

      {sharing && <ShareSheet onClose={closeShare} />}
      {altLine && <AlternativesSheet line={{ p: altLine.p, qty: basket[altLine.p.id] ?? altLine.qty }} a={alts[altLine.p.id]} onClose={() => setAltLine(null)} />}
      {showSavings && <SavingsSheet savings={savings} onOpenLine={(l) => { setShowSavings(false); setAltLine(l); }} onClose={() => setShowSavings(false)} />}
      {checkout && <CheckoutSheet store={multi ? tab : undefined} onClose={() => setCheckout(false)} />}

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
                  <button className="btn primary" onClick={() => { haptic.success(); toast(`${reuseBasket(b.id)} productos añadidos a la cesta`); }}><RotateCcw size={18} /> Repetir compra</button>
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

const parseAmount = (v: string) => Number(v.replace(/\s|€/g, '').replace(',', '.'));
const fmtAmount = (v: number) => v.toFixed(2).replace('.', ',');

function CheckoutSheet({ store, onClose }: { store?: string; onClose: () => void }) {
  const members = useStore((s) => s.members);
  const meId = useStore((s) => s.meId);
  const lines = useMemo(() => checkoutLines(store), [store]);
  const all = basketLines(store).length;
  const estimate = lines.reduce((a, l) => a + l.p.price * l.qty, 0);
  const canSplit = isShared() && members.length > 1;
  const [name, setName] = useState(() => defaultName(store));
  const [amount, setAmount] = useState(fmtAmount(estimate));
  const [paidBy, setPaidBy] = useState(meId && members.some((m) => m.id === meId) ? meId : members[0]?.id);
  const [mode, setMode] = useState<SplitMode | 'none'>('equal');
  const [among, setAmong] = useState(members.map((m) => m.id));
  const value = parseAmount(amount);
  const valid = name.trim() && Number.isFinite(value) && value > 0;
  const shares = canSplit && mode !== 'none' && valid ? purchaseShares({ amount: value, mode, among, store }) : {};

  const submit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!valid) return;
    if (canSplit && mode !== 'none' && !among.length) return toast('Elige entre quién se reparte');
    finishShop({ name: name.trim(), amount: value, store, ...(canSplit && mode !== 'none' ? { paidBy, mode, among } : {}) });
    haptic.success();
    toast(canSplit && mode !== 'none' ? 'Compra guardada y repartida' : `Guardada: ${name.trim()}`);
    onClose();
  };

  return (
    <Sheet onClose={onClose} label="Terminar compra">
      <form onSubmit={submit}>
        <h2 className="share-title" style={{ textAlign: 'left', marginTop: 4 }}>Terminar compra</h2>
        <p className="muted">
          {lines.length < all
            ? `Entran los ${lines.length} productos que están en el carro. Los otros ${all - lines.length} se quedan en la cesta.`
            : `Entran los ${lines.length} productos de la cesta${store ? ` de ${storeInfo(store).name}` : ''}.`}
        </p>

        <label className="text-field">
          Nombre
          <input className="input" value={name} onChange={(e) => setName(e.target.value)} maxLength={60} />
        </label>
        <label className="text-field">
          ¿Cuánto ha sido?
          <span className="money-input">
            <input className="input num" inputMode="decimal" value={amount} onChange={(e) => setAmount(e.target.value)} aria-describedby="amount-hint" />
            <span>€</span>
          </span>
          <span id="amount-hint" className="muted small" style={{ fontWeight: 500 }}>Con los precios de hoy salen {money(estimate)}. Cámbialo si el tique dice otra cosa.</span>
        </label>

        {canSplit && (
          <>
            <div className="text-field">
              ¿Quién ha pagado?
              <div className="chips wrap">
                {members.map((m) => (
                  <button key={m.id} type="button" className="chip person" aria-pressed={paidBy === m.id} onClick={() => setPaidBy(m.id)}>
                    <Avatar m={m} size={30} />{m.id === meId ? 'Yo' : m.name}
                  </button>
                ))}
              </div>
            </div>
            <div className="text-field">
              ¿Cómo lo repartís?
              <div className="segmented" role="group" aria-label="Cómo repartir">
                <button type="button" aria-pressed={mode === 'equal'} onClick={() => setMode('equal')}>A partes iguales</button>
                <button type="button" aria-pressed={mode === 'items'} onClick={() => setMode('items')}>Por producto</button>
                <button type="button" aria-pressed={mode === 'none'} onClick={() => setMode('none')}>No repartir</button>
              </div>
            </div>
            {mode !== 'none' && (
              <div className="text-field">
                {mode === 'equal' ? 'Entre quién' : 'Lo que no es de nadie, entre quién'}
                <div className="chips wrap">
                  {members.map((m) => {
                    const on = among.includes(m.id);
                    return (
                      <button key={m.id} type="button" className="chip person" aria-pressed={on}
                        onClick={() => setAmong(on ? among.filter((x) => x !== m.id) : [...among, m.id])}>
                        <Avatar m={m} size={30} />{m.id === meId ? 'Yo' : m.name}
                      </button>
                    );
                  })}
                </div>
              </div>
            )}
            {mode !== 'none' && Object.keys(shares).length > 0 && (
              <div className="split-preview">
                {members.filter((m) => shares[m.id]).map((m) => (
                  <div key={m.id}>
                    <Avatar m={m} size={26} />
                    <span>{m.id === meId ? 'Tú' : m.name}</span>
                    <b className="num">{money(shares[m.id])}</b>
                  </div>
                ))}
                {mode === 'items' && <p className="muted small">Cada uno paga lo que añadió a la cesta; lo que no tiene dueño se reparte.</p>}
              </div>
            )}
          </>
        )}
        {isShared() && members.length < 2 && <p className="notice">Cuando alguien más se una a la lista, podréis repartir las compras.</p>}

        <button className="btn primary block cta" type="submit" disabled={!valid}><Check size={20} /> Guardar compra</button>
      </form>
    </Sheet>
  );
}
