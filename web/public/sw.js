// Service worker: la app abre sin red con el último catálogo descargado.
const SHELL = 'listpro-shell-v2';
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

  // Catálogo: red primero (precios frescos), caché si no hay conexión.
  if (url.pathname === '/api/catalog') {
    e.respondWith(fetch(e.request).then((res) => {
      if (res.ok) { const copy = res.clone(); caches.open(DATA).then((c) => c.put('/api/catalog', copy)); }
      return res;
    }).catch(() => caches.match('/api/catalog')));
    return;
  }
  if (url.pathname.startsWith('/api/')) return;

  // Interfaz: caché al instante y se refresca en segundo plano.
  e.respondWith(caches.match(e.request, { ignoreSearch: true }).then((hit) => {
    const net = fetch(e.request).then((res) => {
      if (res.ok) { const copy = res.clone(); caches.open(SHELL).then((c) => c.put(e.request, copy)); }
      return res;
    }).catch(() => hit);
    return hit || net;
  }));
});
