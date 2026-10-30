// Service worker: la app abre sin red con el último catálogo descargado.
// La navegación va a red primero para que un redeploy se vea al instante.
const SHELL = 'listpro-shell-v3';
const DATA = 'listpro-data-v1';
const SHELL_FILES = ['/', '/index.html', '/manifest.webmanifest', '/icons/icon.svg'];

self.addEventListener('install', (e) => {
  e.waitUntil(caches.open(SHELL).then((c) => c.addAll(SHELL_FILES)).then(() => self.skipWaiting()));
});

self.addEventListener('activate', (e) => {
  e.waitUntil(caches.keys()
    .then((keys) => Promise.all(keys.filter((k) => k !== SHELL && k !== DATA).map((k) => caches.delete(k))))
    .then(() => self.clients.claim()));
});

self.addEventListener('fetch', (e) => {
  const url = new URL(e.request.url);
  if (e.request.method !== 'GET' || url.origin !== location.origin) return;
  if (url.pathname === '/sw.js') return;

  // Catálogo: red primero (precios frescos), caché si no hay conexión.
  if (url.pathname === '/api/catalog') {
    e.respondWith(fetch(e.request).then((res) => {
      if (res.ok) { const copy = res.clone(); caches.open(DATA).then((c) => c.put('/api/catalog', copy)); }
      return res;
    }).catch(() => caches.match('/api/catalog')));
    return;
  }
  if (url.pathname.startsWith('/api/')) return;

  // Navegación y HTML: red primero. Así el index nuevo apunta a los assets nuevos.
  const isDocument = e.request.mode === 'navigate' || url.pathname === '/' || url.pathname.endsWith('.html');
  if (isDocument) {
    e.respondWith(fetch(e.request).then((res) => {
      if (res.ok) { const copy = res.clone(); caches.open(SHELL).then((c) => c.put('/index.html', copy)); }
      return res;
    }).catch(() => caches.match('/index.html')));
    return;
  }

  // Assets con hash de Vite: caché primero; si no está, red y se guarda.
  e.respondWith(caches.match(e.request, { ignoreSearch: true }).then((hit) => hit || fetch(e.request).then((res) => {
    if (res.ok) { const copy = res.clone(); caches.open(SHELL).then((c) => c.put(e.request, copy)); }
    return res;
  })));
});
