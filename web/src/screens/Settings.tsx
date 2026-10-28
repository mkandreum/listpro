import { useRef } from 'react';
import { ChevronRight, Download, RefreshCw, Upload, Users } from 'lucide-react';
import { href } from '../router';
import { checkCatalogUpdate, exportBackup, importBackup, setTheme, toast, useStore, type ThemePref } from '../store';

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

      <a className="share-promo" href={href({ name: 'basket', join: '' })} style={{ marginTop: 16 }}>
        <span className="art"><Users size={22} /></span>
        <span>
          <b>{sync.code ? `Lista compartida con ${members.length} ${members.length === 1 ? 'persona' : 'personas'}` : 'Compartir la cesta'}</b>
          <span className="muted small">{sync.code ? `Código ${sync.code} · invita o gestiona quién está` : 'Comprad entre varios y repartid los gastos'}</span>
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
