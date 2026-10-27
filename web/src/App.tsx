import { useCallback, useEffect, useState } from 'react';
import { WifiOff } from 'lucide-react';
import { parse, useHash } from './router';
import { checkCatalogUpdate, loadCatalog, pullSync, useStore } from './store';
import { haptic } from './fx/haptics';
import type { Product } from './types';
import { Empty, NavBar, SearchBar, Toasts, TopBar } from './components/Chrome';
import { ProductSheet } from './components/ProductSheet';
import { Home } from './screens/Home';
import { CategoryScreen } from './screens/Category';
import { Results } from './screens/Results';
import { Basket } from './screens/Basket';
import { Stats } from './screens/Stats';
import { Settings } from './screens/Settings';

const CATALOG_POLL_MS = 10 * 60_000;
const SYNC_POLL_MS = 20_000;

function useTheme() {
  const pref = useStore((s) => s.theme);
  useEffect(() => {
    const mq = matchMedia('(prefers-color-scheme: dark)');
    const apply = () => {
      document.documentElement.dataset.theme = pref === 'system' ? (mq.matches ? 'dark' : 'light') : pref;
    };
    apply();
    mq.addEventListener('change', apply);
    return () => mq.removeEventListener('change', apply);
  }, [pref]);
}

export function App() {
  const route = parse(useHash());
  const catalogError = useStore((s) => s.catalogError);
  const [open, setOpen] = useState<Product | null>(null);
  const onOpen = useCallback((p: Product) => setOpen(p), []);
  const onClose = useCallback(() => setOpen(null), []);
  useTheme();

  useEffect(() => {
    loadCatalog();
    pullSync();
    const a = setInterval(checkCatalogUpdate, CATALOG_POLL_MS);
    const b = setInterval(pullSync, SYNC_POLL_MS);
    const onVisible = () => { if (document.visibilityState === 'visible') { checkCatalogUpdate(); pullSync(); } };
    document.addEventListener('visibilitychange', onVisible);
    // Un toque suave en todo lo que se pulsa y no tiene ya su propia háptica
    const onTap = (e: Event) => { if ((e.target as Element).closest?.('.chip, .stat, .cat-tile, .segmented button, .back, .btn:not(.primary)')) haptic.tick(); };
    document.addEventListener('click', onTap, { capture: true });
    return () => { clearInterval(a); clearInterval(b); document.removeEventListener('visibilitychange', onVisible); document.removeEventListener('click', onTap, { capture: true }); };
  }, []);

  useEffect(() => { if (route.name !== 'category') window.scrollTo({ top: 0 }); }, [route.name]);

  const shopping = ['home', 'search', 'deals', 'category'].includes(route.name);

  let screen: React.ReactNode;
  if (catalogError && shopping) {
    screen = (
      <Empty icon={<WifiOff size={34} strokeWidth={2.5} />} title="No llegan los precios">
        <p className="muted">Revisa la conexión. En cuanto vuelva, cargamos el catálogo.</p>
        <button className="btn primary" onClick={() => loadCatalog()} style={{ marginTop: 8 }}>Reintentar</button>
      </Empty>
    );
  } else {
    switch (route.name) {
      case 'home': screen = <Home onOpen={onOpen} />; break;
      case 'search': screen = <Results key="search" mode="search" q={route.q} onOpen={onOpen} />; break;
      case 'deals': screen = <Results key={route.kind} mode={route.kind} onOpen={onOpen} />; break;
      case 'category': screen = <CategoryScreen key={route.id} id={route.id} sub={route.sub} onOpen={onOpen} />; break;
      case 'basket': screen = <Basket onOpen={onOpen} />; break;
      case 'stats': screen = <Stats />; break;
      case 'settings': screen = <Settings />; break;
    }
  }

  return (
    <div className="app">
      <main className="page screen" key={route.name === 'category' ? `c${route.id}` : route.name === 'search' ? 'search' : route.name}>
        {route.name !== 'category' && <TopBar />}
        {shopping && route.name !== 'category' && <SearchBar value={route.name === 'search' ? route.q : ''} autoFocus={route.name === 'search'} />}
        {screen}
      </main>
      <NavBar current={route.name} />
      <Toasts />
      {open && <ProductSheet p={open} onClose={onClose} />}
    </div>
  );
}
