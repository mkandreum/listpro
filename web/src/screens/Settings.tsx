import { useRef } from 'react';
import { useState as useLocalState } from 'react';
import { ChevronRight, Download, KeyRound, LogOut, MonitorSmartphone, Pencil, RefreshCw, Trash2, Upload, UserRound, Users } from 'lucide-react';
import { Avatar } from '../components/Share';
import { href } from '../router';
import {
  changePassword, checkCatalogUpdate, deleteAccount, exportBackup, importBackup, logout, logoutEverywhere, renameAccount, setTheme, toast, useStore, wantAccount,
  type ThemePref,
} from '../store';
import { haptic } from '../fx/haptics';

export function Settings() {
  const sync = useStore((s) => s.sync);
  const catalog = useStore((s) => s.catalog);
  const theme = useStore((s) => s.theme);
  const members = useStore((s) => s.members);
  const file = useRef<HTMLInputElement>(null);

  const run = async (fn: () => Promise<unknown>, ok: string) => {
    try { await fn(); toast(ok); } catch (e) { toast((e as Error).message || 'No se pudo conectar'); }
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
          <div className="kv"><span>Productos</span><b className="num">{catalog?.count.toLocaleString('es-ES') ?? '—'}</b></div>
          <div className="kv"><span>Precios actualizados</span><b className="num">{catalog?.updatedAt ? new Date(catalog.updatedAt).toLocaleString('es-ES', { dateStyle: 'medium', timeStyle: 'short' }) : '—'}</b></div>
          <div className="kv"><span>Zona de precios</span><b>{catalog?.warehouse === 'mad1' ? 'Madrid' : catalog?.warehouse ?? '—'}</b></div>
        </div>
        <p className="muted small" style={{ marginTop: 8 }}>Los precios se descargan cada mañana de la tienda online de Mercadona. ListPro no está afiliada a Mercadona.</p>
        <div className="stack-btns">
          <button className="btn" onClick={() => run(async () => { await checkCatalogUpdate(); }, 'Catálogo al día')}><RefreshCw size={18} /> Comprobar ahora</button>
        </div>
      </section>

      <section className="panel" aria-labelledby="h-backup">
        <h2 className="h-block" id="h-backup">Copia de seguridad</h2>
        <div className="stack-btns">
          <button className="btn" onClick={exportBackup}><Download size={18} /> Descargar copia</button>
          <button className="btn" onClick={() => file.current?.click()}><Upload size={18} /> Restaurar copia</button>
        </div>
        <input ref={file} type="file" accept="application/json,.json" hidden onChange={async (e) => {
          const f = e.target.files?.[0];
          e.target.value = '';
          if (!f) return;
          try { await importBackup(f); toast('Datos restaurados'); } catch { toast('Ese archivo no es una copia de ListPro'); }
        }} />
      </section>
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
