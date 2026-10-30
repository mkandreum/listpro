import { useEffect, useMemo, useState } from 'react';
import { ArrowRightLeft, ChevronDown, Plus, Scale, Sparkles, TrendingDown } from 'lucide-react';
import { api } from '../api';
import { money } from '../lib';
import { changeQty, moveToStore, storeInfo, storeOf, toast, useStore } from '../store';
import type { Alternative, Alternatives, BasketCost, MatchKind, Product } from '../types';
import { haptic } from '../fx/haptics';
import { Sheet } from './Share';
import { StoreDot, StoreName, Thumb } from './Stores';

type Line = { p: Product; qty: number };

export const MATCH_LABEL: Record<MatchKind, string> = { same: 'Mismo producto', equivalent: 'Equivalente', similar: 'Parecido' };
const perLabel = (per?: { value: number; label: string } | null) => (per ? `${money(per.value)}/${per.label}` : '');

/** La mejor alternativa que de verdad ahorra (mismo producto o equivalente, al menos 5 céntimos). */
export const bestSaving = (a?: Alternatives[string]) => a?.options.find((o) => o.match !== 'similar' && o.saving >= 0.05) ?? null;

/** Alternativas de toda la cesta y coste en cada súper; se piden al servidor cuando cambia la cesta. */
export function useBasketCompare(lines: Line[]) {
  const key = lines.map((l) => `${l.p.id}:${l.qty}`).join(',');
  const [alts, setAlts] = useState<Alternatives>({});
  const [cost, setCost] = useState<BasketCost | null>(null);
  useEffect(() => {
    if (!key) { setAlts({}); setCost(null); return; }
    let alive = true;
    const items = key.split(',').map((x) => { const [id, q] = x.split(':'); return { id, qty: Number(q) }; });
    const t = setTimeout(() => {
      api.alternatives(items).then((a) => alive && setAlts(a)).catch(() => {});
      api.basketCost(items).then((c) => alive && setCost(c)).catch(() => {});
    }, 350);
    return () => { alive = false; clearTimeout(t); };
  }, [key]);
  return { alts, cost };
}

/** Debajo de cada producto de la cesta: dónde sale más barato, o un acceso discreto a compararlo. */
export function SavingChip({ a, onOpen }: { a?: Alternatives[string]; onOpen: () => void }) {
  if (!a || !a.options.length) return null;
  const best = bestSaving(a);
  return best ? (
    <button className="alt-chip good" onClick={() => { haptic.tick(); onOpen(); }} aria-label={`Más barato en ${storeInfo(best.store).name}: ahorras ${money(best.saving)}. Ver equivalentes`}>
      <TrendingDown size={14} strokeWidth={2.8} />
      <span className="num">−{money(best.saving)}</span>
      <span className="where">en <StoreDot id={best.store} size={16} /> {storeInfo(best.store).name}</span>
    </button>
  ) : (
    <button className="alt-chip" onClick={() => { haptic.tick(); onOpen(); }} aria-label="Comparar en otros súper">
      <Scale size={14} strokeWidth={2.4} /> Comparar
    </button>
  );
}

function OptionCard({ o, from, qty }: { o: Alternative; from: Product; qty: number }) {
  const inBasket = useStore((s) => s.basket[o.id] ?? 0);
  const name = storeInfo(o.store).name;
  return (
    <div className={`alt-card ${o.saving >= 0.05 && o.match !== 'similar' ? 'good' : ''}`}>
      <div className="alt-main">
        <Thumb src={o.image} />
        <div className="info">
          <span className={`cmp-kind ${o.match}`}>{MATCH_LABEL[o.match]}</span>
          <b>{o.name}</b>
          <span className="muted small num">{[o.size, perLabel(o.per)].filter(Boolean).join(' · ')}</span>
        </div>
        <div className="right num">
          <b>{money(o.cost)}</b>
          <span className="muted small">{o.packs > 1 ? `${o.packs} × ${money(o.price)}` : 'para lo mismo'}</span>
          {Math.abs(o.saving) >= 0.01 && (
            <span className={o.saving > 0 ? 'save-pill' : 'more-pill'}>{o.saving > 0 ? '−' : '+'}{money(Math.abs(o.saving))}{o.approx ? '*' : ''}</span>
          )}
        </div>
      </div>
      <div className="alt-actions">
        <button className="btn small-btn primary" onClick={() => {
          haptic.success();
          moveToStore(from.id, o.id, o.price, o.packs);
          toast(`Cambiado por el de ${name}`);
        }} aria-label={`Cambiar ${from.name} por ${o.name} de ${name}`}><ArrowRightLeft size={15} /> Cambiar</button>
        <button className="btn small-btn" onClick={() => {
          haptic.add();
          changeQty(o.id, o.packs);
          toast(`Añadido a tu cesta de ${name}`);
        }} aria-label={`Añadir ${o.name} a la cesta de ${name}`}>
          <Plus size={15} /> {inBasket ? `Añadido (${inBasket})` : 'Añadir'}
        </button>
      </div>
      {qty > 1 && <span className="sr-only">Para {qty} unidades</span>}
    </div>
  );
}

/** Hoja de un producto de la cesta: sus equivalentes en cada súper, para cambiarlo o añadirlo a esa cesta. */
export function AlternativesSheet({ line, a, onClose }: { line: Line; a?: Alternatives[string]; onClose: () => void }) {
  const { p, qty } = line;
  const [showSimilar, setShowSimilar] = useState(false);
  const groups = useMemo(() => {
    const m = new Map<string, Alternative[]>();
    for (const o of a?.options ?? []) {
      if (!m.has(o.store)) m.set(o.store, []);
      m.get(o.store)!.push(o);
    }
    // El súper con más ahorro primero
    return [...m.entries()].sort((x, y) => Math.max(...y[1].map((o) => o.saving)) - Math.max(...x[1].map((o) => o.saving)));
  }, [a]);
  const similarCount = (a?.options ?? []).filter((o) => o.match === 'similar').length;

  return (
    <Sheet onClose={onClose} label={`Equivalentes de ${p.name}`} className="alt-sheet">
      <div className="alt-head">
        <Thumb src={p.image ?? null} />
        <div className="info">
          <span className="muted small"><StoreName id={p.store ?? storeOf(p.id)} /></span>
          <b>{p.name}</b>
          <span className="muted small num">{[p.size, perLabel(a?.per)].filter(Boolean).join(' · ')}</span>
        </div>
        <div className="right num"><b>{money(p.price * qty)}</b>{qty > 1 && <span className="muted small">{qty} × {money(p.price)}</span>}</div>
      </div>

      {!groups.length && <p className="muted" style={{ marginTop: 16 }}>No hemos encontrado este producto ni uno equivalente en los otros súper.</p>}
      {groups.map(([store, opts]) => {
        const main = opts.filter((o) => o.match !== 'similar').slice(0, 3);
        const rest = opts.filter((o) => o.match === 'similar');
        if (!main.length && !showSimilar) return null;
        return (
          <section key={store} className="alt-group">
            <h3><StoreName id={store} />{main.some((o) => o.saving >= 0.05) && <span className="best-tag"><Sparkles size={12} /> Más barato</span>}</h3>
            {main.map((o) => <OptionCard key={o.id} o={o} from={p} qty={qty} />)}
            {showSimilar && rest.map((o) => <OptionCard key={o.id} o={o} from={p} qty={qty} />)}
          </section>
        );
      })}
      {similarCount > 0 && !showSimilar && (
        <button className="link-btn more-similar" onClick={() => setShowSimilar(true)}><ChevronDown size={16} /> Ver {similarCount} parecidos (otra marca o variante)</button>
      )}
      <p className="muted small" style={{ marginTop: 14 }}>
        El precio es para la misma cantidad que llevas{qty > 1 ? ` (${qty} uds.)` : ''}. «Equivalente» es el mismo producto de otra marca; «Parecido», del mismo tipo con alguna diferencia.
        {(a?.options ?? []).some((o) => o.approx) && ' * Calculado por kilo o litro porque los envases no cuadran.'}
      </p>
    </Sheet>
  );
}

/** Tarjeta única de la cesta: lo que costaría en cada súper y cuánto puedes ahorrar cambiando productos. */
export function CompareCard({ store, total, cost, savings, onSavings }: {
  store: string; total: number; cost: BasketCost | null; savings: { line: Line; o: Alternative }[]; onSavings: () => void;
}) {
  const others = (cost?.stores ?? []).filter((s) => s.id !== store && s.matched > 0);
  if (!others.length && !savings.length) return null;
  const totalSaving = savings.reduce((acc, x) => acc + x.o.saving, 0);
  const rows = others.map((s) => ({ ...s, diff: s.total - (s.comparedTotal ?? total) }));
  const cheapest = rows.filter((r) => r.diff < -0.05).sort((x, y) => x.diff - y.diff)[0];
  return (
    <section className="cmp-card" aria-label="Comparar la cesta con otros súper">
      <div className="cmp-card-head">
        <h2>{cheapest ? <>En {storeInfo(cheapest.id).name} te sale <em className="num">{money(-cheapest.diff)}</em> más barata</> : 'Tu cesta en otros súper'}</h2>
      </div>
      <div className="cmp-stores">
        <div className="cmp-store-tile here">
          <StoreDot id={store} size={28} />
          <span className="n">{storeInfo(store).name}</span>
          <b className="num">{money(total)}</b>
          <span className="muted small">Aquí</span>
        </div>
        {rows.map((r) => (
          <div key={r.id} className={`cmp-store-tile ${r.id === cheapest?.id ? 'best' : ''}`}>
            <StoreDot id={r.id} size={28} />
            <span className="n">{r.name}</span>
            <b className="num">{money(r.total)}</b>
            <span className={`num small ${r.diff < -0.05 ? 'down' : r.diff > 0.05 ? 'up' : 'muted'}`}>
              {Math.abs(r.diff) < 0.05 ? 'Igual' : `${r.diff < 0 ? '−' : '+'}${money(Math.abs(r.diff))}`}
            </span>
            <span className="muted tiny num">{r.matched}/{cost!.totalItems} productos</span>
          </div>
        ))}
      </div>
      {savings.length > 0 && (
        <button className="btn primary block cmp-cta" onClick={() => { haptic.tap(); onSavings(); }}>
          <TrendingDown size={18} /> Ahorra {money(totalSaving)} con {savings.length} cambio{savings.length > 1 ? "s" : ""}
        </button>
      )}
      <p className="muted tiny" style={{ marginTop: 8 }}>
        Cada súper se compara solo con los productos que tiene; con los mismos o equivalentes y para la misma cantidad. Sin ofertas.
      </p>
    </section>
  );
}

/** Todos los cambios que ahorran, para hacerlos de uno en uno o de golpe. */
export function SavingsSheet({ savings, onOpenLine, onClose }: { savings: { line: Line; o: Alternative }[]; onOpenLine: (l: Line) => void; onClose: () => void }) {
  const total = savings.reduce((acc, x) => acc + x.o.saving, 0);
  return (
    <Sheet onClose={onClose} label="Cambios para ahorrar" className="alt-sheet">
      <h2 className="sheet-h">Ahorra <span className="num">{money(total)}</span></h2>
      <p className="muted">Cambia cada producto por su equivalente más barato. Se pasa a tu cesta de ese súper con la cantidad que hace falta.</p>
      {savings.map(({ line, o }) => (
        <div key={line.p.id} className="swap-row">
          <button className="swap-from" onClick={() => onOpenLine(line)} aria-label={`Ver más opciones para ${line.p.name}`}>
            <Thumb src={line.p.image ?? null} />
            <span className="info"><b>{line.p.name}</b><span className="muted small num">{money(line.p.price * line.qty)} · más opciones</span></span>
          </button>
          <div className="swap-to">
            <Thumb src={o.image} />
            <span className="info">
              <span className="small"><StoreName id={o.store} /> <span className={`cmp-kind ${o.match}`}>{MATCH_LABEL[o.match]}</span></span>
              <b>{o.name}</b>
              <span className="muted small num">{o.packs > 1 ? `${o.packs} × ${money(o.price)} = ` : ''}{money(o.cost)}</span>
            </span>
            <span className="save-pill num">−{money(o.saving)}{o.approx ? '*' : ''}</span>
          </div>
          <button className="btn small-btn" onClick={() => {
            haptic.success();
            moveToStore(line.p.id, o.id, o.price, o.packs);
            toast(`Cambiado por el de ${storeInfo(o.store).name}`);
          }}><ArrowRightLeft size={15} /> Cambiar</button>
        </div>
      ))}
      {savings.length > 1 && (
        <button className="btn primary block cta" onClick={() => {
          haptic.success();
          for (const { line, o } of savings) moveToStore(line.p.id, o.id, o.price, o.packs);
          toast(`${savings.length} productos cambiados: ahorras ${money(total)}`);
          onClose();
        }}><ArrowRightLeft size={18} /> Cambiarlos todos</button>
      )}
      <p className="muted small" style={{ marginTop: 10 }}>Tendrás que comprar en más de un súper. * Ahorro calculado por kilo o litro.</p>
    </Sheet>
  );
}
