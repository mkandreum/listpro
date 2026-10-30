import { useState as useLocalState } from 'react';
import { ChevronRight, Copy, KeyRound, LogOut, MonitorSmartphone, Pencil, Trash2, UserRound, Users } from 'lucide-react';
import { Avatar } from '../components/Share';
import { StoreDot, StoreName, StoreSheet } from '../components/Stores';
import { href } from '../router';
import {
  changePassword, deleteAccount, logout, logoutEverywhere, renameAccount, setTheme, toast, useStore, wantAccount,
  type ThemePref,
} from '../store';
import { haptic } from '../fx/haptics';

export function Settings() {
  const sync = useStore((s) => s.sync);
  const catalog = useStore((s) => s.catalog);
  const store = useStore((s) => s.store);
  const [picking, setPicking] = useLocalState(false);
  const theme = useStore((s) => s.theme);
  const members = useStore((s) => s.members);
  const inviteCode = useStore((s) => s.auth?.user.inviteCode);

  const copyInvite = async () => {
    if (!inviteCode) return;
    try {
      await navigator.clipboard.writeText(inviteCode);
      toast('Invitación copiada');
    } catch {
      toast(`Tu invitación es ${inviteCode}`);
    }
  };

  return (
    <>
      <h1 className="h-section" style={{ marginTop: 6 }}>Ajustes</h1>
      <AccountPanel />

      <a className="share-promo" href={href({ name: 'basket', join: '' })} style={{ marginTop: 16 }}>
        <span className="art"><Users size={22} /></span>
        <span>
          <b>{sync.code && members.length > 1 ? `Lista compartida con ${members.length} personas` : 'Compartir la cesta'}</b>
          <span className="muted small">{sync.code && members.length > 1 ? `Código ${sync.code} · invita o gestiona quién está` : 'Comprad entre varios y repartid los gastos'}</span>
        </span>
        <ChevronRight className="chev" aria-hidden />
      </a>

      <section className="panel" aria-labelledby="h-theme">
        <h2 className="h-block" id="h-theme">Apariencia</h2>
        <div className="segmented" role="group" aria-label="Tema">
          {([['system', 'Automático'], ['light', 'Claro'], ['dark', 'Oscuro']] as [ThemePref, string][]).map(([k, label]) => (
            <button key={k} aria-pressed={theme === k} onClick={() => setTheme(k)}>{label}</button>
          ))}
        </div>
      </section>

      <section className="panel" aria-labelledby="h-cat">
        <h2 className="h-block" id="h-cat">Catálogo</h2>
        <div style={{ marginTop: 8 }}>
          <div className="kv"><span>Súper</span><StoreName id={store} /></div>
          <div className="kv"><span>Productos</span><b className="num">{catalog?.count.toLocaleString('es-ES') ?? '—'}</b></div>
          <div className="kv"><span>Precios actualizados</span><b className="num">{catalog?.updatedAt ? new Date(catalog.updatedAt).toLocaleString('es-ES', { dateStyle: 'medium', timeStyle: 'short' }) : '—'}</b></div>
          {catalog?.warehouse && <div className="kv"><span>Zona de precios</span><b>{catalog.warehouse === 'mad1' ? 'Madrid' : catalog.warehouse}</b></div>}
        </div>
        <p className="muted small" style={{ marginTop: 8 }}>Los precios se descargan cada mañana de las tiendas online de cada súper. ListPro no está afiliada a ningún súper.</p>
        <div className="stack-btns">
          <button className="btn" onClick={() => setPicking(true)}><StoreDot id={store} size={20} /> Cambiar de súper</button>
        </div>
      </section>

      {picking && <StoreSheet onClose={() => setPicking(false)} />}

      {inviteCode && (
        <section className="panel" aria-labelledby="h-invite">
          <h2 className="h-block" id="h-invite">Tu invitación</h2>
          <p className="muted small">Comparte este código para que alguien pueda crear su cuenta en ListPro.</p>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginTop: 10, flexWrap: 'wrap' }}>
            <b className="num" style={{ fontSize: '1.35rem', letterSpacing: '.12em', background: 'var(--surface-2)', padding: '10px 14px', borderRadius: 12 }}>{inviteCode}</b>
            <button className="btn" onClick={copyInvite}><Copy size={18} /> Copiar</button>
          </div>
        </section>
      )}
    </>
  );
}

type Panel = null | 'name' | 'password' | 'delete' | 'logout';

function AccountPanel() {
  const auth = useStore((s) => s.auth);
  const me = useStore((s) => s.members.find((m) => m.id === s.meId));
  const [panel, setPanel] = useLocalState<Panel>(null);
  const [a, setA] = useLocalState('');
  const [b, setB] = useLocalState('');
  const [busy, setBusy] = useLocalState(false);
  const open = (p: Panel) => { setPanel(panel === p ? null : p); setA(auth?.user.name && p === 'name' ? auth.user.name : ''); setB(''); };
  const run = async (fn: () => Promise<unknown>, ok: string) => {
    setBusy(true);
    try { await fn(); haptic.success(); toast(ok); setPanel(null); } catch (e) { haptic.warn(); toast((e as Error).message); } finally { setBusy(false); }
  };

  if (!auth) {
    return (
      <section className="panel">
        <div className="account">
          <span className="share-promo-art"><UserRound size={24} /></span>
          <div className="t"><b>Sin cuenta</b><span className="muted small">Crea una para tener tu cesta en todos tus móviles.</span></div>
        </div>
        <div className="stack-btns"><button className="btn primary" onClick={wantAccount}>Crear cuenta o entrar</button></div>
      </section>
    );
  }

  const { user } = auth;
  return (
    <section className="panel" aria-labelledby="h-account">
      <div className="account">
        <Avatar m={me ?? { id: user.id, name: user.name, hue: '#22C55E', joinedAt: '' }} size={54} />
        <div className="t">
          <b id="h-account">{user.name}</b>
          <span className="muted small">{user.email}</span>
        </div>
        <button className="icon-btn" onClick={() => open('name')} aria-label="Cambiar el nombre"><Pencil size={18} /></button>
      </div>

      {panel === 'name' && (
        <form className="sub-form" onSubmit={(e) => { e.preventDefault(); run(() => renameAccount(a), 'Nombre cambiado'); }}>
          <input className="input" autoFocus value={a} onChange={(e) => setA(e.target.value)} maxLength={24} aria-label="Tu nombre" />
          <button className="btn primary" disabled={busy || !a.trim()}>Guardar nombre</button>
        </form>
      )}

      <div className="stack-btns">
        <button className="btn" onClick={() => open('password')}><KeyRound size={18} /> Cambiar contraseña</button>
        {panel === 'password' && (
          <form className="sub-form" onSubmit={(e) => { e.preventDefault(); run(() => changePassword(a, b), 'Contraseña cambiada. Las demás sesiones se han cerrado.'); }}>
            <input className="input" type="password" autoComplete="current-password" placeholder="Contraseña actual" value={a} onChange={(e) => setA(e.target.value)} />
            <input className="input" type="password" autoComplete="new-password" placeholder="Nueva (mínimo 8 caracteres)" value={b} onChange={(e) => setB(e.target.value)} />
            <button className="btn primary" disabled={busy || !a || b.length < 8}>Cambiar</button>
          </form>
        )}
        <button className="btn" onClick={() => run(logoutEverywhere, 'Sesión cerrada en los demás dispositivos')} disabled={busy}><MonitorSmartphone size={18} /> Cerrar sesión en los demás</button>
        {panel === 'logout' ? (
          <div className="confirm-row">
            <span className="small muted">Tu cesta se queda en este dispositivo, pero deja de sincronizarse hasta que vuelvas a entrar.</span>
            <div style={{ display: 'flex', gap: 8 }}>
              <button className="btn" onClick={() => setPanel(null)}>No</button>
              <button className="btn danger" onClick={() => { logout(); toast('Sesión cerrada'); }}>Cerrar sesión</button>
            </div>
          </div>
        ) : (
          <button className="btn" onClick={() => open('logout')}><LogOut size={18} /> Cerrar sesión</button>
        )}
        <button className="btn danger" onClick={() => open('delete')}><Trash2 size={18} /> Borrar mi cuenta</button>
        {panel === 'delete' && (
          <form className="sub-form" onSubmit={(e) => { e.preventDefault(); run(() => deleteAccount(a), 'Cuenta borrada'); }}>
            <span className="small">Se borra tu cuenta para siempre. Escribe tu contraseña para confirmarlo.</span>
            <input className="input" type="password" autoComplete="current-password" placeholder="Contraseña" value={a} onChange={(e) => setA(e.target.value)} />
            <button className="btn danger" disabled={busy || !a}>Borrar para siempre</button>
          </form>
        )}
      </div>
    </section>
  );
}
