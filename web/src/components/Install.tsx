import { useEffect, useState } from 'react';
import { Copy, Download, MoreHorizontal, Plus, X } from 'lucide-react';
import { toast, useStore } from '../store';
import { haptic } from '../fx/haptics';

// Instalar la web como app. En iPhone no hay botón de instalar: Apple solo deja hacerlo a mano desde el menú
// Compartir, así que el banner enseña los pasos. En Android Chrome sí hay aviso de instalación de un toque.

type Kind = 'ios-safari' | 'ios-other' | 'ios-inapp' | 'android';

interface BeforeInstallPromptEvent extends Event { prompt: () => Promise<void>; userChoice: Promise<{ outcome: string }> }

const ua = typeof navigator !== 'undefined' ? navigator.userAgent : '';
const isIOS = /iP(hone|ad|od)/.test(ua) || (/Macintosh/.test(ua) && typeof navigator !== 'undefined' && navigator.maxTouchPoints > 1);
const isIPad = /iPad/.test(ua) || (/Macintosh/.test(ua) && typeof navigator !== 'undefined' && navigator.maxTouchPoints > 1);
/** Ya abierta como app (desde la pantalla de inicio). */
export const isStandalone = () =>
  (typeof navigator !== 'undefined' && (navigator as Navigator & { standalone?: boolean }).standalone === true) ||
  (typeof matchMedia !== 'undefined' && matchMedia('(display-mode: standalone)').matches);
// Navegadores dentro de otras apps: desde ahí no se puede añadir a la pantalla de inicio
const inApp = /FBAN|FBAV|Instagram|WhatsApp|Line\/|GSA\/|Twitter|TikTok|musical_ly/.test(ua);
// Safari 26 esconde Compartir dentro del botón ⋯
const safariMajor = Number(/Version\/(\d+)/.exec(ua)?.[1] ?? 0);

const DISMISS_KEY = 'listproInstallDismissed';
const DAYS = 14;

let deferred: BeforeInstallPromptEvent | null = null;
const listeners = new Set<() => void>();
if (typeof window !== 'undefined') {
  addEventListener('beforeinstallprompt', (e) => { e.preventDefault(); deferred = e as BeforeInstallPromptEvent; listeners.forEach((l) => l()); });
  addEventListener('appinstalled', () => { deferred = null; listeners.forEach((l) => l()); });
}

/** Se puede instalar en este dispositivo (y aún no lo está). */
export function installKind(): Kind | null {
  if (isStandalone()) return null;
  if (isIOS) return inApp ? 'ios-inapp' : /CriOS|FxiOS|EdgiOS|OPiOS/.test(ua) ? 'ios-other' : 'ios-safari';
  return deferred ? 'android' : null;
}

/** Abre las instrucciones de instalación a demanda (desde Ajustes). */
export function openInstall() { forced = true; listeners.forEach((l) => l()); }
let forced = false;

function dismissedRecently() {
  try { return Date.now() - Number(localStorage.getItem(DISMISS_KEY) ?? 0) < DAYS * 86_400_000; } catch { return false; }
}

const ShareIcon = () => (
  <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
    <path d="M12 3v12" /><path d="m8 7 4-4 4 4" /><path d="M8 11H6a2 2 0 0 0-2 2v6a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-6a2 2 0 0 0-2-2h-2" />
  </svg>
);

export function InstallBanner() {
  const ready = useStore((s) => s.onboarded && (s.auth != null || s.guest) && !s.pendingJoin);
  const [, force] = useState(0);
  const [show, setShow] = useState(false);
  useEffect(() => { const l = () => force((n) => n + 1); listeners.add(l); return () => { listeners.delete(l); }; }, []);
  const kind = installKind();

  // Unos segundos después de entrar, para no tapar lo primero que ve
  useEffect(() => {
    if (!ready || !kind) return;
    if (forced) { setShow(true); return; }
    if (dismissedRecently()) return;
    const t = setTimeout(() => setShow(true), 4000);
    return () => clearTimeout(t);
  }, [ready, kind, forced]); // eslint-disable-line react-hooks/exhaustive-deps

  if (!show || !kind) return null;
  const close = () => {
    haptic.tick();
    setShow(false);
    forced = false;
    try { localStorage.setItem(DISMISS_KEY, String(Date.now())); } catch { /* sin almacenamiento */ }
  };
  const device = isIPad ? 'iPad' : isIOS ? 'iPhone' : 'móvil';

  return (
    <aside className={`install ${isIPad ? 'top' : ''}`} role="dialog" aria-label="Instalar ListPro">
      <button className="icon-btn install-x" onClick={close} aria-label="Cerrar"><X size={18} /></button>
      <div className="install-head">
        <img src="/icons/apple-touch-icon-180.png" alt="" width={52} height={52} />
        <div>
          <b>Instala ListPro en tu {device}</b>
          <span className="muted small">Se abre a pantalla completa, como una app, y la tienes a un toque.</span>
        </div>
      </div>

      {kind === 'android' && (
        <button className="btn primary block" onClick={async () => {
          haptic.tap();
          await deferred?.prompt();
          const r = await deferred?.userChoice;
          if (r?.outcome === 'accepted') { haptic.success(); setShow(false); }
          deferred = null;
        }}><Download size={18} /> Instalar</button>
      )}

      {(kind === 'ios-safari' || kind === 'ios-other') && (
        <ol className="install-steps">
          {kind === 'ios-safari' && safariMajor >= 26 && <li><span className="key"><MoreHorizontal size={18} /></span>Pulsa <b>⋯</b> abajo a la derecha</li>}
          <li><span className="key"><ShareIcon /></span>Pulsa <b>Compartir</b>{kind === 'ios-other' ? ' en la barra de direcciones' : ''}</li>
          <li><span className="key"><Plus size={18} /></span>Elige <b>Añadir a pantalla de inicio</b></li>
        </ol>
      )}

      {kind === 'ios-inapp' && (
        <>
          <p className="small" style={{ marginTop: 10 }}>Desde el navegador de esta app no se puede instalar. Ábrela en <b>Safari</b> y añádela desde ahí.</p>
          <button className="btn block" style={{ marginTop: 10 }} onClick={() => navigator.clipboard?.writeText(location.origin).then(() => { haptic.tick(); toast('Enlace copiado: pégalo en Safari'); })}>
            <Copy size={16} /> Copiar enlace
          </button>
        </>
      )}

      {isIOS && <p className="muted tiny" style={{ marginTop: 8 }}>La primera vez que la abras desde el icono, entra con tu cuenta y tendrás todo igual.</p>}
      {isIOS && !isIPad && kind === 'ios-safari' && <span className="install-arrow" aria-hidden />}
    </aside>
  );
}
