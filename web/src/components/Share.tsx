import { useEffect, useRef, useState } from 'react';
import { Check, Copy, LogOut, Pencil, Send, UserPlus, Users, X } from 'lucide-react';
import { inkOn, relativeTime } from '../lib';
import { initials } from '../share';
import { createSync, joinSync, leaveSync, toast, upsertMe, useStore, validCode } from '../store';
import type { Member } from '../types';
import { haptic } from '../fx/haptics';

/** Hoja inferior con fondo, Escape para cerrar y el scroll de la página bloqueado. */
export function Sheet({ onClose, label, className = '', children }: { onClose: () => void; label: string; className?: string; children: React.ReactNode }) {
  const ref = useRef<HTMLDivElement>(null);
  useEffect(() => {
    ref.current?.focus();
    const onKey = (e: KeyboardEvent) => e.key === 'Escape' && onClose();
    addEventListener('keydown', onKey);
    const prev = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    return () => { removeEventListener('keydown', onKey); document.body.style.overflow = prev; };
  }, [onClose]);
  return (
    <>
      <div className="sheet-scrim" onClick={onClose} />
      <div ref={ref} tabIndex={-1} className={`sheet ${className}`} role="dialog" aria-modal="true" aria-label={label}>
        <div className="grip" />
        <button className="icon-btn sheet-x" onClick={onClose} aria-label="Cerrar"><X /></button>
        {children}
      </div>
    </>
  );
}

export function Avatar({ m, size = 32, ring = false }: { m?: Member; size?: number; ring?: boolean }) {
  const hue = m?.hue ?? '#B9BCC4';
  return (
    <span className={`avatar ${ring ? 'ring' : ''}`} style={{ '--s': `${size}px`, background: hue, color: inkOn(hue) } as React.CSSProperties} title={m?.name} aria-hidden>
      {initials(m?.name ?? '?')}
    </span>
  );
}

export function AvatarStack({ members, max = 4, size = 30 }: { members: Member[]; max?: number; size?: number }) {
  const shown = members.slice(0, max);
  return (
    <span className="avatar-stack">
      {shown.map((m) => <Avatar key={m.id} m={m} size={size} ring />)}
      {members.length > max && <span className="avatar ring more num" style={{ '--s': `${size}px` } as React.CSSProperties}>+{members.length - max}</span>}
    </span>
  );
}

/** Botón de la cabecera de la cesta: muestra quién está en la lista o invita a compartirla. */
export function SharePill({ onClick }: { onClick: () => void }) {
  const code = useStore((s) => s.sync.code);
  const members = useStore((s) => s.members);
  return code && members.length > 1 ? (
    <button className="share-pill glass" onClick={onClick} aria-label={`Lista compartida con ${members.length} personas`}>
      <AvatarStack members={members} max={3} size={28} />
      <span className="live" aria-hidden />
    </button>
  ) : (
    <button className="share-pill glass invite" onClick={onClick}>
      <UserPlus size={19} strokeWidth={2.4} /> Compartir
    </button>
  );
}

const inviteLink = (code: string) => `${location.origin}/#/unirse/${code}`;

async function invite(code: string) {
  const url = inviteLink(code);
  const text = `Únete a nuestra lista de la compra en ListPro. Código: ${code}`;
  try {
    if (navigator.share) { await navigator.share({ title: 'ListPro', text, url }); return; }
  } catch (e) {
    if ((e as Error).name === 'AbortError') return;
  }
  await navigator.clipboard?.writeText(`${text}\n${url}`);
  toast('Invitación copiada');
}

export function ShareSheet({ onClose, joinCode }: { onClose: () => void; joinCode?: string }) {
  const sync = useStore((s) => s.sync);
  return (
    <Sheet onClose={onClose} label="Compartir la cesta" className="share-sheet">
      {sync.code ? <SharedView code={sync.code} updatedAt={sync.updatedAt} onClose={onClose} /> : <StartView joinCode={joinCode} />}
    </Sheet>
  );
}

function StartView({ joinCode }: { joinCode?: string }) {
  const me = useStore((s) => s.members.find((m) => m.id === s.meId));
  const [mode, setMode] = useState<'create' | 'join'>(joinCode ? 'join' : 'create');
  const [name, setName] = useState(me?.name ?? '');
  const [code, setCode] = useState(joinCode ?? '');
  const [busy, setBusy] = useState(false);
  const ok = name.trim().length > 0 && (mode === 'create' || validCode(code));

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!ok) return;
    setBusy(true);
    try {
      if (mode === 'create') { await createSync(name); toast('Lista compartida creada'); }
      else { await joinSync(code, name); toast('Ya estás dentro de la lista'); }
      haptic.success();
    } catch (err) {
      haptic.warn();
      toast((err as Error).message || 'No se pudo conectar');
    } finally { setBusy(false); }
  };

  return (
    <form onSubmit={submit}>
      <div className="share-art" aria-hidden>
        <span style={{ background: '#7C5CFF' }}>A</span>
        <span style={{ background: '#22C55E' }}><Users size={30} strokeWidth={2.3} /></span>
        <span style={{ background: '#FF8A2A' }}>L</span>
      </div>
      <h2 className="share-title">{mode === 'join' ? 'Únete a la lista' : 'Haced la compra juntos'}</h2>
      <p className="muted center-text">
        {mode === 'join'
          ? 'Verás la misma cesta que quien te invitó, al momento, y podréis repartir lo que cuesta.'
          : 'Tu pareja, tu familia o tus compis de piso ven la misma cesta al momento: qué ha pedido cada uno, qué está ya en el carro y cuánto os debéis.'}
      </p>

      <div className="segmented two-seg" role="group" aria-label="Qué quieres hacer">
        <button type="button" aria-pressed={mode === 'create'} onClick={() => setMode('create')}>Crear lista</button>
        <button type="button" aria-pressed={mode === 'join'} onClick={() => setMode('join')}>Tengo un código</button>
      </div>

      <label className="text-field">
        ¿Cómo te llamas?
        <input className="input" value={name} onChange={(e) => setName(e.target.value)} maxLength={24} placeholder="Así te verán los demás" autoComplete="given-name" />
      </label>
      {mode === 'join' && (
        <label className="text-field">
          Código de la lista
          <input className="input code-input num" value={code} onChange={(e) => setCode(e.target.value.toUpperCase().replace(/[^A-Z0-9]/g, ''))}
            maxLength={8} placeholder="ABCD2345" autoCapitalize="characters" autoComplete="off" spellCheck={false} />
        </label>
      )}
      <button className="btn primary block cta" type="submit" disabled={busy || !ok}>
        {mode === 'create' ? <><Users size={20} /> Crear lista compartida</> : <><Check size={20} /> Unirme</>}
      </button>
      {mode === 'create' && <p className="muted small center-text" style={{ marginTop: 10 }}>Lo que ya tienes en la cesta pasa a la lista compartida.</p>}
    </form>
  );
}

function SharedView({ code, updatedAt, onClose }: { code: string; updatedAt: string | null; onClose: () => void }) {
  const members = useStore((s) => s.members);
  const meId = useStore((s) => s.meId);
  const me = members.find((m) => m.id === meId);
  const [editing, setEditing] = useState(!me);
  const [name, setName] = useState(me?.name ?? '');
  const [leaving, setLeaving] = useState(false);
  const signedIn = useStore((s) => s.auth != null);

  return (
    <>
      <h2 className="share-title" style={{ marginTop: 4 }}>{members.length > 1 ? 'Lista compartida' : 'Comparte tu lista'}</h2>
      <p className="muted center-text small"><span className="pulse-dot" /> En directo · sincronizada {relativeTime(updatedAt)}</p>

      <button className="code big-code" onClick={() => navigator.clipboard?.writeText(code).then(() => { haptic.tick(); toast('Código copiado'); })} aria-label={`Copiar código ${code.split('').join(' ')}`}>
        {code}<Copy size={20} />
      </button>
      <button className="btn primary block cta" style={{ marginTop: 0 }} onClick={() => invite(code)}><Send size={19} /> Invitar a alguien</button>

      <h3 className="h-block" style={{ marginTop: 24 }}>En la lista <span className="muted num" style={{ fontWeight: 500 }}>{members.length}</span></h3>
      <ul className="members">
        {members.map((m) => (
          <li key={m.id}>
            <Avatar m={m} size={40} />
            {m.id === meId && editing ? (
              <form className="rename" onSubmit={(e) => { e.preventDefault(); if (name.trim()) { upsertMe(name); setEditing(false); } }}>
                <input className="input" autoFocus value={name} onChange={(e) => setName(e.target.value)} maxLength={24} placeholder="Tu nombre" aria-label="Tu nombre" />
                <button className="btn" type="submit" disabled={!name.trim()}>Guardar</button>
              </form>
            ) : (
              <>
                <div className="who"><b>{m.name}</b>{m.id === meId && <span className="you">tú</span>}</div>
                {m.id === meId && <button className="icon-btn" onClick={() => setEditing(true)} aria-label="Cambiar mi nombre"><Pencil size={18} /></button>}
              </>
            )}
          </li>
        ))}
        {!me && !editing && (
          <li><button className="btn ghost" onClick={() => setEditing(true)}><UserPlus size={18} /> Ponerme nombre</button></li>
        )}
      </ul>

      {(!signedIn || members.length > 1) && <div style={{ marginTop: 22 }}>
        {leaving ? (
          <div className="confirm-row">
            <span className="small muted">Dejarás de ver los cambios de los demás en este dispositivo.</span>
            <div style={{ display: 'flex', gap: 8 }}>
              <button className="btn" onClick={() => setLeaving(false)}>No</button>
              <button className="btn danger" onClick={() => { leaveSync(); haptic.warn(); toast('Has salido de la lista'); onClose(); }}>Salir</button>
            </div>
          </div>
        ) : (
          <button className="btn danger block" onClick={() => setLeaving(true)}><LogOut size={18} /> Salir de la lista en este dispositivo</button>
        )}
      </div>}
    </>
  );
}
