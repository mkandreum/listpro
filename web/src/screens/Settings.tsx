import { useRef, useState } from 'react';
import { Copy, Download, Link2, RefreshCw, Smartphone, Unlink, Upload } from 'lucide-react';
import { relativeTime } from '../lib';
import {
  checkCatalogUpdate, createSync, exportBackup, importBackup, joinSync, leaveSync, setTheme, toast, useStore, type ThemePref,
} from '../store';

export function Settings() {
  const sync = useStore((s) => s.sync);
  const catalog = useStore((s) => s.catalog);
  const theme = useStore((s) => s.theme);
  const [code, setCode] = useState('');
  const [busy, setBusy] = useState(false);
  const file = useRef<HTMLInputElement>(null);

  const run = async (fn: () => Promise<unknown>, ok: string) => {
    setBusy(true);
    try { await fn(); toast(ok); } catch (e) { toast((e as Error).message || 'No se pudo conectar'); } finally { setBusy(false); }
  };

  return (
    <>
      <h1 className="h-section" style={{ marginTop: 6 }}>Ajustes</h1>

      <section className="panel" aria-labelledby="h-sync">
        <h2 className="h-block" id="h-sync"><Link2 size={22} style={{ verticalAlign: '-4px', marginRight: 6 }} />Compartir la lista</h2>
        {sync.code ? (
          <>
            <p className="muted" style={{ marginTop: 6 }}>Este código une tu cesta en la web, en la app Android y en los móviles de tu casa.</p>
            <div className="code" aria-label={`Código ${sync.code.split('').join(' ')}`}>{sync.code}</div>
            <p className="muted small" style={{ textAlign: 'center' }}>Sincronizado {relativeTime(sync.updatedAt)}</p>
            <div className="stack-btns">
              <button className="btn" onClick={() => navigator.clipboard?.writeText(sync.code!).then(() => toast('Código copiado'))}><Copy size={18} /> Copiar código</button>
              <button className="btn danger" onClick={() => { leaveSync(); toast('Dispositivo desvinculado'); }}><Unlink size={18} /> Desvincular este dispositivo</button>
            </div>
          </>
        ) : (
          <>
            <p className="muted" style={{ marginTop: 6 }}>Crea un código y escríbelo en otro móvil u ordenador para compartir la misma cesta. En Android también puedes pasarla acercando dos móviles (NFC).</p>
            <div className="stack-btns">
              <button className="btn primary" disabled={busy} onClick={() => run(createSync, 'Código creado')}><Smartphone size={18} /> Crear código</button>
            </div>
            <form className="save-form" onSubmit={(e) => { e.preventDefault(); run(() => joinSync(code), 'Dispositivo vinculado'); }}>
              <label className="sr-only" htmlFor="sync-code">Código de 8 caracteres</label>
              <input id="sync-code" className="input num" placeholder="Código de 8 caracteres" maxLength={8} value={code}
                onChange={(e) => setCode(e.target.value.toUpperCase())} autoCapitalize="characters" autoComplete="off" spellCheck={false} />
              <button className="btn" type="submit" disabled={busy || code.length !== 8}>Unirme</button>
            </form>
          </>
        )}
      </section>

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
