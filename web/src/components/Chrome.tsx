import { useEffect, useRef, useState } from 'react';
import { BarChart3, Search, Settings, ShoppingBasket, Store, X } from 'lucide-react';
import { freshnessLabel, units } from '../lib';
import { href, navigate, type Route } from '../router';
import { useStore } from '../store';

export function TopBar() {
  const updatedAt = useStore((s) => s.catalog?.updatedAt);
  return (
    <header className="topbar">
      <a href="#/" className="wordmark" style={{ textDecoration: 'none' }} aria-label="ListPro, inicio">
        <span className="wordmark-mark"><ShoppingBasket size={22} strokeWidth={2.75} /></span>
        ListPro
      </a>
      {updatedAt && <span className="stamp num"><span className="pulse" />{freshnessLabel(updatedAt)}</span>}
    </header>
  );
}

export function SearchBar({ value = '', autoFocus = false }: { value?: string; autoFocus?: boolean }) {
  const [q, setQ] = useState(value);
  const ref = useRef<HTMLInputElement>(null);
  useEffect(() => setQ(value), [value]);
  useEffect(() => { if (autoFocus) ref.current?.focus(); }, [autoFocus]);

  const update = (next: string) => {
    setQ(next);
    if (next.trim().length >= 2) navigate({ name: 'search', q: next }, location.hash.startsWith('#/buscar'));
    else if (!next && location.hash.startsWith('#/buscar')) navigate({ name: 'home' }, true);
  };

  return (
    <div className="searchbar" role="search">
      <label className="search glass">
        <Search size={22} strokeWidth={2.5} aria-hidden />
        <span className="sr-only">Buscar productos</span>
        <input
          ref={ref}
          type="search"
          value={q}
          onChange={(e) => update(e.target.value)}
          placeholder="Busca leche, aguacate, detergente…"
          autoComplete="off"
          enterKeyHint="search"
        />
        {q && <button type="button" className="icon-btn" onClick={() => { update(''); ref.current?.focus(); }} aria-label="Borrar búsqueda"><X size={20} /></button>}
      </label>
    </div>
  );
}

const TABS: { route: Route; label: string; icon: typeof Store; match: string[] }[] = [
  { route: { name: 'home' }, label: 'Productos', icon: Store, match: ['home', 'search', 'deals', 'category'] },
  { route: { name: 'basket' }, label: 'Cesta', icon: ShoppingBasket, match: ['basket'] },
  { route: { name: 'stats' }, label: 'Gastos', icon: BarChart3, match: ['stats'] },
  { route: { name: 'settings' }, label: 'Ajustes', icon: Settings, match: ['settings'] },
];

export function NavBar({ current }: { current: Route['name'] }) {
  const count = useStore((s) => units(s.basket));
  const badgeRef = useRef<HTMLSpanElement>(null);
  const prev = useRef(count);
  useEffect(() => {
    if (count > prev.current && badgeRef.current) {
      const el = badgeRef.current;
      el.classList.remove('bump');
      void el.offsetWidth;
      el.classList.add('bump');
    }
    prev.current = count;
  }, [count]);

  const active = Math.max(0, TABS.findIndex((t) => t.match.includes(current)));
  return (
    <nav className="nav glass" aria-label="Principal" style={{ '--i': active } as React.CSSProperties}>
      <span className="nav-pill" aria-hidden />
      {TABS.map(({ route, label, icon: Icon, match }) => (
        <a key={label} href={href(route)} className="nav-item" style={{ textDecoration: 'none' }} aria-current={match.includes(current) ? 'page' : undefined}>
          <Icon size={24} strokeWidth={2.4} aria-hidden />
          {label}
          {route.name === 'basket' && count > 0 && (
            <span ref={badgeRef} className="badge num" aria-label={`${count} unidades`}>{count > 99 ? '99+' : count}</span>
          )}
        </a>
      ))}
    </nav>
  );
}

export function Toasts() {
  const toasts = useStore((s) => s.toasts);
  return (
    <div className="toasts" role="status" aria-live="polite">
      {toasts.map((t) => <div key={t.id} className="toast glass">{t.text}</div>)}
    </div>
  );
}

export function Empty({ icon, title, children }: { icon: React.ReactNode; title: string; children?: React.ReactNode }) {
  return (
    <div className="empty">
      <div className="art">{icon}</div>
      <h2 className="h-block">{title}</h2>
      {children}
    </div>
  );
}
