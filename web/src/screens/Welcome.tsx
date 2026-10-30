import { useEffect, useMemo, useRef, useState, type FormEvent } from 'react';
import { ArrowDown, ArrowRight, Check, ChefHat, Eye, EyeOff, ShoppingBasket, Sparkles, Users } from 'lucide-react';
import { money } from '../lib';
import { api } from '../api';
import { continueAsGuest, finishOnboarding, getState, login, register, setStore, useStore } from '../store';
import { StoreChoice } from '../components/Stores';
import { haptic } from '../fx/haptics';

/** Fotos de producto reales del catálogo para decorar (las que más han bajado, si las hay). */
function useStickers(n: number) {
  const catalog = useStore((s) => s.catalog);
  return useMemo(() => {
    const FOOD = ['frescos', 'lacteos', 'despensa', 'bebidas', 'congelados'];
    const ps = catalog?.products.filter((p) => p.image && FOOD.includes(p.section)) ?? [];
    // Una por pasillo, para que no salgan tres botellas de aceite
    const seen = new Set<string>();
    const deals = ps.filter((p) => p.previousPrice != null && p.previousPrice > p.price && !seen.has(p.section) && seen.add(p.section));
    const pick = [...deals, ...ps.filter((p, i) => i % 211 === 0 && !seen.has(p.section) && seen.add(p.section)), ...ps.filter((_, i) => i % 97 === 0)].slice(0, n);
    return pick.map((p) => ({ id: p.id, image: p.image!, price: p.price, was: p.previousPrice ?? null }));
  }, [catalog, n]);
}

// ───────────── Presentación ─────────────

const SLIDES = [
  {
    key: 'precios',
    eyebrow: 'Precios de hoy',
    title: <>Tu súper,<br /><em>en un clic.</em></>,
    body: 'Más de 4.000 productos de Mercadona con el precio de esta mañana. Ves lo que ha bajado antes de salir de casa.',
  },
  {
    key: 'cenas',
    eyebrow: 'Recetas con IA',
    title: <>Recetas que<br /><em>cuadran.</em></>,
    body: 'Dinos tu presupuesto, tu dieta y lo que tienes en la cocina. Te montamos desayunos, comidas, meriendas y cenas de la semana, y la lista de la compra.',
  },
  {
    key: 'juntos',
    eyebrow: 'En compañía',
    title: <>Comprad<br /><em>juntos.</em></>,
    body: 'Una cesta para toda la casa: quién pide qué, quién lo ha cogido y cuánto os debéis. Sin hacer cuentas.',
  },
] as const;

export function Onboarding() {
  const [i, setI] = useState(0);
  const drag = useRef<{ x: number; t: number } | null>(null);
  const last = i === SLIDES.length - 1;

  const go = (n: number) => {
    const next = Math.max(0, Math.min(SLIDES.length - 1, n));
    if (next !== i) { haptic.tap(); setI(next); }
  };
  const done = () => { haptic.success(); finishOnboarding(); };

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'ArrowRight') go(i + 1);
      if (e.key === 'ArrowLeft') go(i - 1);
    };
    addEventListener('keydown', onKey);
    return () => removeEventListener('keydown', onKey);
  });

  return (
    <div
      className="ob"
      data-step={SLIDES[i].key}
      onPointerDown={(e) => { drag.current = { x: e.clientX, t: Date.now() }; }}
      onPointerUp={(e) => {
        const d = drag.current;
        drag.current = null;
        if (!d) return;
        const dx = e.clientX - d.x;
        if (Math.abs(dx) > 50 && Date.now() - d.t < 600) go(i + (dx < 0 ? 1 : -1));
      }}
    >
      <div className="ob-glow" aria-hidden />
      <header className="ob-top">
        <span className="ob-brand"><span className="brand-mark"><ShoppingBasket size={22} strokeWidth={2.3} /></span><span>List<em>Pro</em></span></span>
        {!last && <button className="ob-skip" onClick={done}>Saltar</button>}
      </header>

      <div className="ob-track" style={{ '--i': i } as React.CSSProperties}>
        {SLIDES.map((s, n) => (
          <section key={s.key} className={`ob-slide ${n === i ? 'on' : ''}`} aria-hidden={n !== i} aria-roledescription="diapositiva" aria-label={`${n + 1} de ${SLIDES.length}`}>
            <div className="ob-art">{s.key === 'precios' ? <PricesArt /> : s.key === 'cenas' ? <DinnersArt /> : <TogetherArt />}</div>
            <div className="ob-copy">
              <span className="ob-eyebrow">{s.eyebrow}</span>
              <h1>{s.title}</h1>
              <p>{s.body}</p>
            </div>
          </section>
        ))}
      </div>

      <footer className="ob-foot">
        <div className="ob-dots" role="tablist" aria-label="Pasos">
          {SLIDES.map((s, n) => (
            <button key={s.key} role="tab" aria-selected={n === i} aria-label={`Paso ${n + 1}`} className={n === i ? 'on' : n < i ? 'past' : ''} onClick={() => go(n)} />
          ))}
        </div>
        <button className={`ob-next ${last ? 'wide' : ''}`} onClick={() => (last ? done() : go(i + 1))} aria-label={last ? 'Empezar' : 'Siguiente'}>
          <span className="lbl">Empezar</span>
          <ArrowRight size={24} strokeWidth={2.6} />
        </button>
      </footer>
    </div>
  );
}

function PricesArt() {
  const stickers = useStickers(3);
  const fallback = ['🥑', '🥛', '🍝'];
  return (
    <div className="art-prices">
      {[0, 1, 2].map((n) => {
        const s = stickers[n];
        return (
          <div key={n} className={`sticker s${n}`}>
            {s ? <img src={s.image} alt="" draggable={false} /> : <span className="emoji">{fallback[n]}</span>}
            <span className="ptag num">{s ? money(s.price) : ['1,20 €', '0,89 €', '0,95 €'][n]}</span>
            {n === 0 && (
              <span className="drop num"><ArrowDown size={14} strokeWidth={3} />{s?.was ? money(s.was - s.price) : '0,30 €'}</span>
            )}
          </div>
        );
      })}
      <span className="spark a"><Sparkles size={22} /></span>
      <span className="spark b"><Sparkles size={16} /></span>
    </div>
  );
}

function DinnersArt() {
  const rows = [['Lun', 'Pollo al limón', '2,10 €'], ['Mar', 'Crema de calabaza', '1,35 €'], ['Mié', 'Tortilla de patatas', '0,95 €']];
  return (
    <div className="art-dinners">
      <div className="pot big"><span /><span /><span /><ChefHat size={46} strokeWidth={2} /></div>
      <div className="menu">
        {rows.map(([d, n, p], k) => (
          <div key={d} className="dish" style={{ '--k': k } as React.CSSProperties}>
            <b className="num">{d}</b><span>{n}</span><i className="num">{p}</i>
          </div>
        ))}
      </div>
    </div>
  );
}

function TogetherArt() {
  return (
    <div className="art-together">
      <div className="ring" />
      <span className="who w0">A</span>
      <span className="who w1"><Users size={30} /></span>
      <span className="who w2">L</span>
      <div className="bubble"><span className="ok"><Check size={14} strokeWidth={3.5} /></span>Lucía te paga <b className="num">12,40 €</b></div>
    </div>
  );
}

// ───────────── Registro y entrada ─────────────

export function AuthScreen() {
  const [mode, setMode] = useState<'register' | 'login'>('register');
  // Si ya usaba una lista compartida, propone el mismo nombre
  const [name, setName] = useState(() => { const st = getState(); return st.members.find((m) => m.id === st.meId)?.name ?? ''; });
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [invite, setInvite] = useState('');
  const [forgot, setForgot] = useState(false);
  const [show, setShow] = useState(false);
  // Súper preferido: el que verá nada más terminar el registro
  const [store, setStoreChoice] = useState(() => getState().store);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [shake, setShake] = useState(0);
  const stickers = useStickers(6);
  const isNew = mode === 'register';
  const ready = email.includes('@') && password.length >= (isNew ? 8 : 1) && (!isNew || name.trim().length > 0);

  if (forgot) return <ForgotPassword initialEmail={email} onBack={() => setForgot(false)} />;

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    if (busy || !ready) return;
    setBusy(true);
    setError(null);
    try {
      if (isNew) await register(name.trim(), email, password, store, invite.trim().toUpperCase());
      else await login(email, password);
      haptic.success();
    } catch (err) {
      haptic.warn();
      setError((err as Error).message || 'Algo ha fallado. Prueba otra vez.');
      setShake((n) => n + 1);
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className={`auth ${isNew ? 'is-new' : 'is-back'}`}>
      <section className="auth-hero" aria-hidden>
        <div className="ob-glow" />
        <div className="auth-float">
          {stickers.map((s, n) => (
            <span key={s.id} className={`float f${n}`}><img src={s.image} alt="" draggable={false} /></span>
          ))}
        </div>
        <span className="brand-mark big"><ShoppingBasket size={40} strokeWidth={2.2} /></span>
        <p className="auth-word">List<em>Pro</em></p>
      </section>

      <section className="auth-card" aria-label={isNew ? 'Crear cuenta' : 'Entrar'}>
        <div className="segmented two-seg" role="tablist">
          <button role="tab" aria-selected={isNew} aria-pressed={isNew} onClick={() => { setMode('register'); setError(null); }}>Soy nuevo</button>
          <button role="tab" aria-selected={!isNew} aria-pressed={!isNew} onClick={() => { setMode('login'); setError(null); }}>Ya tengo cuenta</button>
        </div>
        <h1 key={mode} className="auth-title">{isNew ? <>Crea tu <em>cuenta</em></> : <>Hola de <em>nuevo</em></>}</h1>
        <p className="muted">{isNew ? 'Tu cesta, tus recetas y tus cuentas, en todos tus móviles.' : 'Entra y sigue donde lo dejaste.'}</p>

        <form key={shake} className={`auth-form ${shake ? 'shake' : ''}`} onSubmit={submit} noValidate>
          {isNew && (
            <label className="text-field">
              ¿Cómo te llamas?
              <input className="input" value={name} onChange={(e) => setName(e.target.value)} autoComplete="given-name" maxLength={24} placeholder="Así te verán en las listas compartidas" />
            </label>
          )}
          <label className="text-field">
            Email
            <input className="input" type="email" inputMode="email" autoComplete="email" autoCapitalize="none" spellCheck={false}
              value={email} onChange={(e) => setEmail(e.target.value)} placeholder="tu@email.com" />
          </label>
          <label className="text-field">
            Contraseña
            <span className="pass">
              <input className="input" type={show ? 'text' : 'password'} autoComplete={isNew ? 'new-password' : 'current-password'}
                value={password} onChange={(e) => setPassword(e.target.value)} placeholder={isNew ? 'Mínimo 8 caracteres' : '••••••••'} />
              <button type="button" className="icon-btn" onClick={() => setShow((v) => !v)} aria-label={show ? 'Ocultar contraseña' : 'Ver contraseña'} aria-pressed={show}>
                {show ? <EyeOff size={20} /> : <Eye size={20} />}
              </button>
            </span>
            {isNew && <PasswordMeter value={password} />}
          </label>
          {isNew && (
            <label className="text-field">
              Código de invitación
              <input className="input" value={invite} onChange={(e) => setInvite(e.target.value.toUpperCase().replace(/[^A-Z2-9]/g, '').slice(0, 8))}
                autoComplete="one-time-code" autoCapitalize="characters" spellCheck={false} placeholder="Si te lo han dado" />
              <span className="muted small" style={{ fontWeight: 500 }}>Te lo da alguien que ya usa ListPro. La primera cuenta de una instalación nueva no lo necesita.</span>
            </label>
          )}
          {isNew && (
            <div className="text-field">
              Tu súper
              <span className="muted small" style={{ fontWeight: 500 }}>Es el que verás al entrar. Lo puedes cambiar cuando quieras desde arriba.</span>
              <StoreChoice value={store} onChange={setStoreChoice} />
            </div>
          )}
          {error && <p className="auth-error" role="alert">{error}</p>}
          <button className="btn primary block cta" disabled={busy || !ready}>
            {busy ? (isNew ? 'Preparando tu cesta…' : 'Entrando…') : isNew ? 'Crear cuenta' : 'Entrar'}
            {!busy && <ArrowRight size={20} />}
          </button>
        </form>
        {!isNew && (
          <button className="link-btn" type="button" onClick={() => { setForgot(true); setError(null); }}>He olvidado mi contraseña</button>
        )}
        <button className="link-btn guest" onClick={() => { haptic.tick(); if (isNew) setStore(store, false); continueAsGuest(); }}>Seguir sin cuenta</button>
        <p className="muted small center-text">Solo guardamos tu nombre, tu email y tu lista. Puedes borrar la cuenta cuando quieras desde Ajustes.</p>
      </section>
    </div>
  );
}

function RecoveryHero() {
  return (
    <section className="auth-hero" aria-hidden>
      <span className="brand-mark big"><ShoppingBasket size={40} strokeWidth={2.2} /></span>
      <p className="auth-word">List<em>Pro</em></p>
    </section>
  );
}

function ForgotPassword({ initialEmail, onBack }: { initialEmail: string; onBack: () => void }) {
  const [email, setEmail] = useState(initialEmail);
  const [sent, setSent] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    if (busy || !email.includes('@')) return;
    setBusy(true);
    setError(null);
    try {
      await api.forgot(email);
      haptic.success();
      setSent(true);
    } catch (err) {
      haptic.warn();
      setError((err as Error).message || 'No se pudo enviar el correo.');
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="auth is-back">
      <RecoveryHero />
      <section className="auth-card" aria-label="Recuperar contraseña">
        <h1 className="auth-title">Recuperar <em>contraseña</em></h1>
        {sent ? (
          <>
            <p className="muted">Si el email existe, te hemos enviado un enlace para cambiar la contraseña. Revisa también la carpeta de spam.</p>
            <button className="btn primary block" onClick={onBack} style={{ marginTop: 14 }}>Volver a entrar</button>
          </>
        ) : (
          <form className="auth-form" onSubmit={submit} noValidate>
            <label className="text-field">
              Email
              <input className="input" type="email" inputMode="email" autoComplete="email" autoCapitalize="none" spellCheck={false}
                value={email} onChange={(e) => setEmail(e.target.value)} placeholder="tu@email.com" />
            </label>
            {error && <p className="auth-error" role="alert">{error}</p>}
            <button className="btn primary block cta" disabled={busy || !email.includes('@')}>
              {busy ? 'Enviando…' : 'Enviar enlace'}
              {!busy && <ArrowRight size={20} />}
            </button>
          </form>
        )}
        {!sent && <button className="link-btn" type="button" onClick={onBack}>Volver</button>}
      </section>
    </div>
  );
}

export function ResetPassword({ token }: { token: string }) {
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [busy, setBusy] = useState(false);
  const [done, setDone] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    if (busy) return;
    if (!token) return setError('El enlace no es válido. Pide otro.');
    if (password.length < 8) return setError('La contraseña necesita al menos 8 caracteres.');
    if (password !== confirm) return setError('Las dos contraseñas no coinciden.');
    setBusy(true);
    setError(null);
    try {
      await api.resetPassword(token, password);
      haptic.success();
      setDone(true);
    } catch (err) {
      haptic.warn();
      setError((err as Error).message || 'No se pudo cambiar la contraseña.');
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="auth is-back">
      <RecoveryHero />
      <section className="auth-card" aria-label="Nueva contraseña">
        <h1 className="auth-title">Nueva <em>contraseña</em></h1>
        {done ? (
          <>
            <p className="muted">Contraseña cambiada. Ya puedes entrar con la nueva.</p>
            <button className="btn primary block" onClick={() => { location.hash = '#/'; }} style={{ marginTop: 14 }}>Entrar</button>
          </>
        ) : (
          <form className="auth-form" onSubmit={submit} noValidate>
            <label className="text-field">
              Contraseña
              <input className="input" type="password" autoComplete="new-password" value={password}
                onChange={(e) => setPassword(e.target.value)} placeholder="Mínimo 8 caracteres" />
            </label>
            <label className="text-field">
              Repite la contraseña
              <input className="input" type="password" autoComplete="new-password" value={confirm}
                onChange={(e) => setConfirm(e.target.value)} placeholder="Otra vez" />
            </label>
            {error && <p className="auth-error" role="alert">{error}</p>}
            <button className="btn primary block cta" disabled={busy || password.length < 8 || password !== confirm}>
              {busy ? 'Guardando…' : 'Cambiar contraseña'}
              {!busy && <ArrowRight size={20} />}
            </button>
          </form>
        )}
      </section>
    </div>
  );
}

function PasswordMeter({ value }: { value: string }) {
  const score = Math.min(4, (value.length >= 8 ? 1 : 0) + (value.length >= 12 ? 1 : 0) + (/[A-Z]/.test(value) && /[a-z]/.test(value) ? 1 : 0) + (/\d|[^\w]/.test(value) ? 1 : 0));
  const label = ['Muy corta', 'Vale', 'Bien', 'Fuerte', 'Muy fuerte'][value.length < 8 ? 0 : score];
  if (!value) return null;
  return (
    <span className="meter" data-score={value.length < 8 ? 0 : score}>
      <span className="meter-bars">{[0, 1, 2, 3].map((n) => <i key={n} className={n < (value.length < 8 ? 1 : score) ? 'on' : ''} />)}</span>
      <span className="small">{label}</span>
    </span>
  );
}
