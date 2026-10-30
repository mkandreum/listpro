import { Children, forwardRef, useCallback, useEffect, useLayoutEffect, useRef, useState } from 'react';
import type { LucideIcon } from 'lucide-react';
import { haptic } from '../fx/haptics';
import { reducedMotion } from '../fx/motion';

export interface PillItem {
  key: string;
  label: string;
  icon?: LucideIcon;
  /** Color del indicador cuando esta pestaña está activa. */
  hue: string;
  /** Texto pequeño bajo la etiqueta (p. ej. el total de esa cesta). */
  sub?: string;
}

/**
 * Barra de secciones en píldora con un indicador que se desliza. Sin Pager, el indicador va a la
 * pestaña activa; con Pager, sigue al dedo (--p lo actualiza el Pager mientras se desliza).
 */
export const PillBar = forwardRef<HTMLDivElement, { items: PillItem[]; active: number; onSelect: (i: number) => void; label: string; ids?: boolean }>(
  function PillBar({ items, active, onSelect, label, ids = false }, ref) {
    return (
      <div ref={ref} className="pill-tabs glass" role="tablist" aria-label={label}
        style={{ '--n': items.length, '--p': active, '--active': items[active]?.hue ?? 'var(--green)' } as React.CSSProperties}>
        <span className="pill-tabs-ind" aria-hidden />
        {items.map((t, i) => {
          const Icon = t.icon;
          return (
            <button key={t.key} role="tab" id={ids ? `tab-${t.key}` : undefined} aria-controls={ids ? `page-${t.key}` : undefined}
              aria-selected={i === active} tabIndex={i === active ? 0 : -1}
              style={{ '--meal': t.hue } as React.CSSProperties} onClick={() => onSelect(i)}
              onKeyDown={(e) => {
                if (e.key === 'ArrowRight' || e.key === 'ArrowLeft') {
                  const n = (i + (e.key === 'ArrowRight' ? 1 : -1) + items.length) % items.length;
                  onSelect(n);
                  (e.currentTarget.parentElement?.children[n + 1] as HTMLElement | undefined)?.focus();
                }
              }}>
              {Icon && <Icon size={19} strokeWidth={2.3} aria-hidden />}
              <span>{t.label}</span>
              {t.sub && <small className="num">{t.sub}</small>}
            </button>
          );
        })}
      </div>
    );
  },
);

/**
 * Píldora de secciones + páginas que se deslizan con el dedo (scroll-snap). El indicador sigue al
 * dedo en tiempo real y la altura se ajusta a la página visible para no dejar huecos.
 */
export function Pager({ tabs, initial, label, children }: { tabs: PillItem[]; initial?: string; label: string; children: React.ReactNode }) {
  const pages = Children.toArray(children);
  const track = useRef<HTMLDivElement>(null);
  const bar = useRef<HTMLDivElement>(null);
  const [active, setActive] = useState(() => Math.max(0, initial ? tabs.findIndex((t) => t.key === initial) : 0));
  const [height, setHeight] = useState<number | undefined>();
  const key = tabs.map((t) => t.key).join();

  // Al volver de una receta o cambiar de plan, nos colocamos sin animación
  useLayoutEffect(() => {
    const el = track.current;
    if (!el) return;
    const i = Math.min(active, tabs.length - 1);
    el.scrollLeft = i * el.clientWidth;
    bar.current?.style.setProperty('--p', String(i));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [key]);

  useEffect(() => {
    const el = track.current;
    if (!el) return;
    let raf = 0;
    const onScroll = () => {
      cancelAnimationFrame(raf);
      raf = requestAnimationFrame(() => {
        const p = el.scrollLeft / Math.max(1, el.clientWidth);
        bar.current?.style.setProperty('--p', p.toFixed(3));
        const i = Math.round(p);
        setActive((a) => (a === i ? a : (haptic.tick(), i)));
      });
    };
    el.addEventListener('scroll', onScroll, { passive: true });
    return () => { el.removeEventListener('scroll', onScroll); cancelAnimationFrame(raf); };
  }, []);

  // La altura sigue a la página activa (también cuando crece, p. ej. al cargar imágenes)
  useEffect(() => {
    const page = track.current?.children[active] as HTMLElement | undefined;
    if (!page) return;
    const ro = new ResizeObserver(() => setHeight(page.offsetHeight));
    ro.observe(page);
    return () => ro.disconnect();
  }, [active, key]);

  const go = useCallback((i: number) => {
    const el = track.current;
    if (!el) return;
    haptic.tap();
    el.scrollTo({ left: i * el.clientWidth, behavior: reducedMotion() ? 'auto' : 'smooth' });
    const top = (bar.current?.getBoundingClientRect().top ?? 0) + scrollY - 12;
    if (scrollY > top) window.scrollTo({ top, behavior: reducedMotion() ? 'auto' : 'smooth' });
  }, []);

  return (
    <>
      <PillBar ref={bar} items={tabs} active={active} onSelect={go} label={label} ids />
      <div ref={track} className="pager" style={{ height }}>
        {pages.map((c, i) => (
          <div key={tabs[i]?.key ?? i} className="pager-page" role="tabpanel" id={`page-${tabs[i]?.key}`} aria-labelledby={`tab-${tabs[i]?.key}`}
            aria-hidden={i !== active} {...(i !== active ? { inert: '' } : {})}>
            {c}
          </div>
        ))}
      </div>
    </>
  );
}
