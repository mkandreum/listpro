import { useSyncExternalStore } from 'react';

export type Route =
  | { name: 'home' }
  | { name: 'search'; q: string }
  | { name: 'deals'; kind: 'down' | 'new' }
  | { name: 'category'; id: number; sub?: number }
  | { name: 'plan'; recipe?: number }
  | { name: 'basket' }
  | { name: 'stats' }
  | { name: 'settings' };

export function parse(hash: string): Route {
  const [path, query = ''] = hash.replace(/^#/, '').split('?');
  const parts = path.split('/').filter(Boolean);
  const params = new URLSearchParams(query);
  switch (parts[0]) {
    case 'buscar': return { name: 'search', q: params.get('q') ?? '' };
    case 'bajadas': return { name: 'deals', kind: 'down' };
    case 'novedades': return { name: 'deals', kind: 'new' };
    case 'c': return { name: 'category', id: Number(parts[1]), sub: parts[2] ? Number(parts[2]) : undefined };
    case 'plan': return { name: 'plan', recipe: parts[1] ? Number(parts[1]) : undefined };
    case 'cesta': return { name: 'basket' };
    case 'gastos': return { name: 'stats' };
    case 'ajustes': return { name: 'settings' };
    default: return { name: 'home' };
  }
}

export function href(r: Route): string {
  switch (r.name) {
    case 'home': return '#/';
    case 'search': return `#/buscar?q=${encodeURIComponent(r.q)}`;
    case 'deals': return r.kind === 'down' ? '#/bajadas' : '#/novedades';
    case 'category': return `#/c/${r.id}${r.sub ? `/${r.sub}` : ''}`;
    case 'plan': return `#/plan${r.recipe != null ? `/${r.recipe}` : ''}`;
    case 'basket': return '#/cesta';
    case 'stats': return '#/gastos';
    case 'settings': return '#/ajustes';
  }
}

export const navigate = (r: Route, replace = false) => {
  if (replace) history.replaceState(null, '', href(r));
  else location.hash = href(r);
  if (replace) dispatchEvent(new HashChangeEvent('hashchange'));
};

const sub = (cb: () => void) => { addEventListener('hashchange', cb); return () => removeEventListener('hashchange', cb); };
export const useHash = () => useSyncExternalStore(sub, () => location.hash);
