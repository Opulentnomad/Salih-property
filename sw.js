/* دليلك الآمن — Service Worker: يعمل بدون إنترنت بعد أول زيارة */
const VERSION = 'v3';
const CORE = `salih-core-${VERSION}`;
const RUNTIME = `salih-runtime-${VERSION}`;
const ASSETS = ['./','index.html','styles.css','app.js','data.js','manifest.webmanifest',
  'icons/icon.svg','icons/icon-192.png','icons/icon-512.png','icons/apple-touch-icon.png'];

self.addEventListener('install', e => {
  e.waitUntil(caches.open(CORE).then(c => c.addAll(ASSETS)).then(() => self.skipWaiting()));
});
self.addEventListener('activate', e => {
  e.waitUntil(caches.keys()
    .then(keys => Promise.all(keys.filter(k => ![CORE, RUNTIME].includes(k)).map(k => caches.delete(k))))
    .then(() => self.clients.claim()));
});

// يعيد النسخة المحفوظة فوراً ويحدّثها في الخلفية
function staleWhileRevalidate(req, cacheName) {
  return caches.open(cacheName).then(cache =>
    cache.match(req, {ignoreSearch: true}).then(hit => {
      const net = fetch(req).then(res => {
        if (res && (res.ok || res.type === 'opaque')) cache.put(req, res.clone());
        return res;
      }).catch(() => hit);
      return hit || net;
    }));
}

self.addEventListener('fetch', e => {
  const req = e.request;
  if (req.method !== 'GET') return;
  const url = new URL(req.url);
  if (url.hostname === 'generativelanguage.googleapis.com') return; // لا نخزّن طلبات الذكاء الاصطناعي
  if (req.mode === 'navigate') {
    e.respondWith(caches.match('index.html').then(hit => hit || fetch(req)).catch(() => caches.match('index.html')));
    return;
  }
  if (url.origin === location.origin) {
    e.respondWith(staleWhileRevalidate(req, CORE));
  } else if (['fonts.googleapis.com','fonts.gstatic.com','cdnjs.cloudflare.com'].includes(url.hostname)) {
    e.respondWith(staleWhileRevalidate(req, RUNTIME));
  }
});
