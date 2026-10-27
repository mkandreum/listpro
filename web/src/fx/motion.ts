import { useEffect, useRef, useState } from 'react';

export const reducedMotion = () => matchMedia('(prefers-reduced-motion: reduce)').matches;

/**
 * La foto del producto vuela en arco hasta el icono de la cesta y la cesta da un salto.
 * Es el gesto de la app: cada vez que algo entra en la cesta se ve entrar.
 */
export function flyToBasket(from: Element | null) {
  const target = document.querySelector('.nav-item[data-tab="basket"] svg');
  const img = from?.closest('.tag, .sheet, .line')?.querySelector('img');
  if (!img || !target || reducedMotion()) return;
  const a = img.getBoundingClientRect();
  const b = target.getBoundingClientRect();
  if (!a.width) return;
  const ghost = img.cloneNode() as HTMLImageElement;
  const size = Math.min(a.width, a.height, 120);
  Object.assign(ghost.style, {
    position: 'fixed', zIndex: '200', left: `${a.left + (a.width - size) / 2}px`, top: `${a.top + (a.height - size) / 2}px`,
    width: `${size}px`, height: `${size}px`, objectFit: 'contain', borderRadius: '18px', background: '#fff', padding: '6px',
    boxShadow: '0 12px 30px rgba(0,0,0,.25)', pointerEvents: 'none', willChange: 'transform, opacity',
  });
  document.body.appendChild(ghost);
  const dx = b.left + b.width / 2 - (a.left + a.width / 2);
  const dy = b.top + b.height / 2 - (a.top + a.height / 2);
  const anim = ghost.animate(
    [
      { transform: 'translate(0, 0) scale(1) rotate(0deg)', opacity: 1 },
      { transform: `translate(${dx * 0.45}px, ${dy * 0.25 - 90}px) scale(.62) rotate(-10deg)`, opacity: 1, offset: 0.45 },
      { transform: `translate(${dx}px, ${dy}px) scale(.14) rotate(6deg)`, opacity: 0.6 },
    ],
    { duration: 620, easing: 'cubic-bezier(.45, 0, .2, 1)' },
  );
  anim.onfinish = () => {
    ghost.remove();
    target.closest('.nav-item')?.animate(
      [{ transform: 'scale(1)' }, { transform: 'scale(1.18) rotate(-6deg)' }, { transform: 'scale(1)' }],
      { duration: 380, easing: 'cubic-bezier(.3, 1.4, .5, 1)' },
    );
  };
}

/** Número que se desliza hasta su nuevo valor (total de la cesta, contadores). */
export function useTweened(value: number, ms = 450) {
  const [shown, setShown] = useState(value);
  const from = useRef(value);
  useEffect(() => {
    if (reducedMotion()) { setShown(value); from.current = value; return; }
    const start = performance.now();
    const a = from.current;
    let raf = 0;
    const step = (t: number) => {
      const k = Math.min(1, (t - start) / ms);
      const e = 1 - Math.pow(1 - k, 4);
      setShown(a + (value - a) * e);
      if (k < 1) raf = requestAnimationFrame(step);
      else from.current = value;
    };
    raf = requestAnimationFrame(step);
    return () => { cancelAnimationFrame(raf); from.current = value; };
  }, [value, ms]);
  return shown;
}
