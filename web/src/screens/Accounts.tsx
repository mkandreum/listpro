import { useMemo, useState } from 'react';
import { ArrowRight, Check, ChevronRight, HandCoins, Plus, ReceiptText, Trash2, Users } from 'lucide-react';
import { money } from '../lib';
import { balances, settlements } from '../share';
import { addExpense, deleteExpense, settleUp, toast, useStore } from '../store';
import type { Expense, Member } from '../types';
import { Empty } from '../components/Chrome';
import { Avatar, ShareSheet, Sheet } from '../components/Share';
import { haptic } from '../fx/haptics';
import { useTweened } from '../fx/motion';

const day = (iso: string) => new Date(iso).toLocaleDateString('es-ES', { day: 'numeric', month: 'short' });

export function Accounts() {
  const shared = useStore((s) => s.sync.code != null && s.members.length > 1);
  const members = useStore((s) => s.members);
  const meId = useStore((s) => s.meId);
  const expenses = useStore((s) => s.expenses);
  const [sharing, setSharing] = useState(false);
  const [adding, setAdding] = useState(false);
  const [confirm, setConfirm] = useState<string | null>(null);

  const bal = useMemo(() => balances(expenses, members), [expenses, members]);
  const plan = useMemo(() => settlements(bal), [bal]);
  const mine = meId ? bal.get(meId) ?? 0 : 0;
  const shown = useTweened(Math.abs(mine));
  const spent = expenses.filter((e) => e.kind === 'purchase').reduce((a, e) => a + e.amount, 0);
  const byId = (id: string) => members.find((m) => m.id === id);
  const label = (id: string) => (id === meId ? 'Tú' : byId(id)?.name ?? 'Alguien');

  if (!shared) {
    return (
      <>
        <Empty icon={<Users size={34} strokeWidth={2.5} />} title="Repartid los gastos sin hacer cuentas">
          <p className="muted">Comparte la cesta con tu pareja, tu familia o tus compis de piso. Cada compra se apunta con quién la pagó, y ListPro os dice quién debe cuánto a quién.</p>
          <button className="btn primary" onClick={() => setSharing(true)} style={{ marginTop: 8 }}><Users size={18} /> Compartir la cesta</button>
        </Empty>
        {sharing && <ShareSheet onClose={() => setSharing(false)} />}
      </>
    );
  }

  return (
    <>
      <section className={`total-block balance ${mine < -0.004 ? 'owe' : ''}`} aria-label="Tu saldo">
        <div className="label">{mine > 0.004 ? 'Te deben' : mine < -0.004 ? 'Debes' : 'Tu saldo'}</div>
        <div className="big num">{Math.abs(mine) > 0.004 ? money(shown) : 'En paz'}</div>
        <div className="small num" style={{ fontWeight: 600, opacity: .9 }}>
          {members.length} en la lista · {money(spent)} gastados juntos
        </div>
      </section>

      {plan.length > 0 && (
        <section className="panel">
          <h2 className="h-block">Para quedar en paz</h2>
          <div className="settle-list">
            {plan.map((t) => {
              const key = `${t.from}>${t.to}`;
              return (
                <div key={key} className="settle">
                  <div className="pair">
                    <Avatar m={byId(t.from)} size={36} />
                    <ArrowRight size={18} className="muted" aria-hidden />
                    <Avatar m={byId(t.to)} size={36} />
                  </div>
                  <div className="what">
                    <span>{t.from === meId ? `Le pagas a ${label(t.to)}` : t.to === meId ? `${label(t.from)} te paga` : `${label(t.from)} le paga a ${label(t.to)}`}</span>
                    <b className="num">{money(t.amount)}</b>
                  </div>
                  {confirm === key ? (
                    <button className="btn primary" onClick={() => { settleUp(t.from, t.to, t.amount); haptic.success(); toast('Pago apuntado'); setConfirm(null); }}><Check size={18} /> Hecho</button>
                  ) : (
                    <button className="btn" onClick={() => setConfirm(key)}>Saldar</button>
                  )}
                </div>
              );
            })}
          </div>
          <p className="muted small" style={{ marginTop: 10 }}>ListPro no mueve dinero: pagaos como siempre (Bizum, efectivo…) y pulsad «Saldar» para apuntarlo.</p>
        </section>
      )}

      <section className="panel">
        <h2 className="h-block">Saldos</h2>
        <div style={{ marginTop: 6 }}>
          {members.map((m) => {
            const v = bal.get(m.id) ?? 0;
            return (
              <div key={m.id} className="kv person-row">
                <span><Avatar m={m} size={30} />{m.name}{m.id === meId && <span className="you">tú</span>}</span>
                <b className={`num ${v > 0.004 ? 'pos' : v < -0.004 ? 'neg' : ''}`}>{v > 0.004 ? '+' : ''}{money(v)}</b>
              </div>
            );
          })}
        </div>
      </section>

      <div className="row-head">
        <h2 className="h-section" style={{ fontSize: '1.5rem' }}>Movimientos</h2>
        <button className="btn ghost" onClick={() => setAdding(true)}><Plus size={18} /> Añadir gasto</button>
      </div>
      {expenses.length === 0 ? (
        <p className="muted">Cuando terminéis una compra desde la cesta, o añadáis un gasto a mano, aparecerá aquí.</p>
      ) : (
        [...expenses].reverse().map((e, i) => <ExpenseRow key={e.id} e={e} i={i} members={members} meId={meId} label={label} />)
      )}

      {adding && <ExpenseSheet onClose={() => setAdding(false)} />}
    </>
  );
}

function ExpenseRow({ e, i, members, meId, label }: { e: Expense; i: number; members: Member[]; meId: string | null; label: (id: string) => string }) {
  const [confirm, setConfirm] = useState(false);
  const payer = members.find((m) => m.id === e.paidBy);
  const myShare = meId ? e.shares[meId] ?? 0 : 0;
  const settle = e.kind === 'settle';
  const to = settle ? Object.keys(e.shares)[0] : undefined;
  return (
    <details className="saved expense" style={{ '--d': Math.min(i, 8) } as React.CSSProperties}>
      <summary>
        <span className={`exp-icon ${settle ? 'settle' : ''}`}>{settle ? <HandCoins size={20} /> : <ReceiptText size={20} />}</span>
        <div className="t">
          <b>{settle ? `${label(e.paidBy)} → ${label(to!)}` : e.title}</b>
          <span className="muted small num">
            {day(e.at)} · {settle ? 'pago' : `pagó ${label(e.paidBy).toLowerCase() === 'tú' ? 'tú' : payer?.name ?? 'alguien'}`}
            {!settle && myShare > 0 && ` · tu parte ${money(myShare)}`}
          </span>
        </div>
        <b className="num amount">{money(e.amount)}</b>
        <ChevronRight className="chev" aria-hidden />
      </summary>
      <div className="body">
        {!settle && Object.entries(e.shares).map(([id, v]) => (
          <div key={id} className="item person-row">
            <span><Avatar m={members.find((m) => m.id === id)} size={24} />{label(id)}</span>
            <b className="num">{money(v)}</b>
          </div>
        ))}
        {!settle && <p className="muted small" style={{ marginTop: 8 }}>{e.mode === 'items' ? 'Cada uno paga lo que pidió.' : 'A partes iguales.'}</p>}
        <div className="total-actions">
          {confirm
            ? <><button className="btn" onClick={() => setConfirm(false)}>No</button><button className="btn danger" onClick={() => { deleteExpense(e.id); haptic.warn(); toast('Movimiento borrado'); }}>Sí, borrar</button></>
            : <button className="btn danger" onClick={() => setConfirm(true)}><Trash2 size={18} /> Borrar</button>}
        </div>
      </div>
    </details>
  );
}

function ExpenseSheet({ onClose }: { onClose: () => void }) {
  const members = useStore((s) => s.members);
  const meId = useStore((s) => s.meId);
  const [title, setTitle] = useState('');
  const [amount, setAmount] = useState('');
  const [paidBy, setPaidBy] = useState(meId && members.some((m) => m.id === meId) ? meId : members[0]?.id);
  const [among, setAmong] = useState(members.map((m) => m.id));
  const value = Number(amount.replace(/\s|€/g, '').replace(',', '.'));
  const valid = title.trim() && Number.isFinite(value) && value > 0 && paidBy && among.length > 0;

  return (
    <Sheet onClose={onClose} label="Añadir gasto">
      <form onSubmit={(e) => {
        e.preventDefault();
        if (!valid) return;
        addExpense({ title: title.trim(), amount: value, paidBy: paidBy!, among });
        haptic.success();
        toast('Gasto apuntado');
        onClose();
      }}>
        <h2 className="share-title" style={{ textAlign: 'left', marginTop: 4 }}>Añadir gasto</h2>
        <p className="muted">Para lo que no compráis con la cesta: la fruta del mercado, el pan, la bombona…</p>
        <label className="text-field">
          ¿Qué es?
          <input className="input" autoFocus value={title} onChange={(e) => setTitle(e.target.value)} maxLength={60} placeholder="Fruta del mercado" />
        </label>
        <label className="text-field">
          ¿Cuánto?
          <span className="money-input">
            <input className="input num" inputMode="decimal" value={amount} onChange={(e) => setAmount(e.target.value)} placeholder="0,00" />
            <span>€</span>
          </span>
        </label>
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
          A partes iguales entre
          <div className="chips wrap">
            {members.map((m) => {
              const on = among.includes(m.id);
              return (
                <button key={m.id} type="button" className="chip person" aria-pressed={on} onClick={() => setAmong(on ? among.filter((x) => x !== m.id) : [...among, m.id])}>
                  <Avatar m={m} size={30} />{m.id === meId ? 'Yo' : m.name}
                </button>
              );
            })}
          </div>
        </div>
        {valid && <p className="muted small" style={{ marginTop: 10 }}>Sale a {money(value / among.length)} por cabeza.</p>}
        <button className="btn primary block cta" type="submit" disabled={!valid}><Check size={20} /> Apuntar gasto</button>
      </form>
    </Sheet>
  );
}
