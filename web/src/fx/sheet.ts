import { useCallback, useEffect, useRef } from 'react';
import { haptic } from './haptics';
import { reducedMotion } from './motion';

/**
 * Hojas inferiores con tacto de app: se cierran arrastrando hacia abajo (con inercia), salen con
 * animación en vez de desaparecer de golpe y dan un toque al abrir y al cerrar.
 * Devuelve la ref de la hoja y `close`, que hay que usar en vez de `onClose` (botón, fondo, Escape).
 */
export function useSheet<T extends HTMLElement = HTMLDivElement>(onClose: () => void) {
  const ref = useRef<T>(null);
  const closing = useRef(false);
  const latest = useRef(onClose);
  latest.current = onClose;

  const close = useCallback((fromDrag = false) => {
    const el = ref.current;
    if (!el || closing.current) return;
    closing.current = true;
    if (!fromDrag) haptic.tick();
    const scrim = el.previousElementSibling as HTMLElement | null;
    if (reducedMotion()) return latest.current();
    el.classList.add('closing');
    scrim?.classList.add('closing');
    setTimeout(() => latest.current(), 230);
  }, []);

  useEffect(() => {
    const el = ref.current;
    if (!el) return;
    const onKey = (e: KeyboardEvent) => e.key === 'Escape' && close();
    addEventListener('keydown', onKey);
    const prev = document.body.style.overflow;
    document.body.style.overflow = 'hidden';

    // Arrastre solo con el dedo y solo si la hoja está arriba del todo (si no, el gesto es para hacer scroll)
    const scrim = el.previousElementSibling as HTMLElement | null;
    let startY = 0, dy = 0, lastY = 0, lastT = 0, v = 0, active = false, dragging = false;
    const onStart = (e: TouchEvent) => {
      if (e.touches.length !== 1) return;
      startY = lastY = e.touches[0].clientY;
      lastT = e.timeStamp;
      dy = 0; v = 0;
      active = el.scrollTop <= 0 || !!(e.target as Element).closest('.grip');
      dragging = false;
    };
    const onMove = (e: TouchEvent) => {
      if (!active) return;
      const y = e.touches[0].clientY;
      const d = y - startY;
      if (d <= 0 && !dragging) { active = false; return; }
      e.preventDefault();
      if (!dragging) { dragging = true; el.style.transition = 'none'; if (scrim) scrim.style.transition = 'none'; }
      // Un poco de resistencia hacia arriba, libre hacia abajo
      dy = d < 0 ? d / 4 : d;
      const dt = Math.max(1, e.timeStamp - lastT);
      v = (y - lastY) / dt;
      lastY = y;
      lastT = e.timeStamp;
      el.style.setProperty('--drag', `${dy}px`);
      if (scrim) scrim.style.opacity = String(Math.max(0, 1 - dy / (el.offsetHeight || 600)));
    };
    const onEnd = () => {
      if (!dragging) { active = false; return; }
      active = dragging = false;
      if (dy > Math.min(140, el.offsetHeight * 0.3) || v > 0.55) {
        haptic.tick();
        el.style.transition = 'translate .24s cubic-bezier(.4, 0, 1, 1)';
        el.style.setProperty('--drag', `${el.offsetHeight + 40}px`);
        if (scrim) { scrim.style.transition = 'opacity .24s'; scrim.style.opacity = '0'; }
        closing.current = true;
        setTimeout(() => latest.current(), 240);
      } else {
        el.style.transition = 'translate .45s cubic-bezier(.3, 1.35, .5, 1)';
        el.style.setProperty('--drag', '0px');
        if (scrim) { scrim.style.transition = 'opacity .3s'; scrim.style.opacity = ''; }
      }
    };
    el.addEventListener('touchstart', onStart, { passive: true });
    el.addEventListener('touchmove', onMove, { passive: false });
    el.addEventListener('touchend', onEnd);
    el.addEventListener('touchcancel', onEnd);
    haptic.tick();
    return () => {
      removeEventListener('keydown', onKey);
      document.body.style.overflow = prev;
      el.removeEventListener('touchstart', onStart);
      el.removeEventListener('touchmove', onMove);
      el.removeEventListener('touchend', onEnd);
      el.removeEventListener('touchcancel', onEnd);
    };
  }, [close]);

  return { ref, close: () => close() };
}
