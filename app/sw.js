const V='safarna-v3',SHELL=['./','index.html','styles.css','app.js','data.js','icon.svg','manifest.webmanifest'];
const PUBLIC_API=/^\/api\/(destinations|entry-requirements|fx|recommendations)/;
self.addEventListener('install',e=>e.waitUntil(caches.open(V).then(c=>c.addAll(SHELL)).then(()=>self.skipWaiting())));
self.addEventListener('activate',e=>e.waitUntil(caches.keys().then(k=>Promise.all(k.filter(x=>x!==V).map(x=>caches.delete(x)))).then(()=>self.clients.claim())));
self.addEventListener('fetch',e=>{
  const req=e.request;if(req.method!=='GET')return;
  const url=new URL(req.url);
  if(url.origin===location.origin&&url.pathname.startsWith('/api')){
    if(!PUBLIC_API.test(url.pathname))return;               // حساب/إدارة/رحلات: شبكة فقط، لا تخزين
    e.respondWith(fetch(req).then(r=>{if(r.ok){const c=r.clone();caches.open(V).then(x=>x.put(req,c))}return r}).catch(()=>caches.match(req)));
    return;
  }
  e.respondWith(caches.match(req).then(hit=>hit||fetch(req).then(r=>{
    if(r.ok&&(url.origin===location.origin||/fonts\.g/.test(req.url))){const c=r.clone();caches.open(V).then(x=>x.put(req,c));}
    return r;}).catch(()=>caches.match('index.html'))));
});
self.addEventListener('push',e=>{const d=e.data?e.data.json():{title:'سفرنا',body:'تحديث جديد'};
  e.waitUntil(self.registration.showNotification(d.title,{body:d.body,icon:'icon.svg',dir:'rtl',lang:'ar'}));});
