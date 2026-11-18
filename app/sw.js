const V='safarna-v2',SHELL=['./','index.html','styles.css','app.js','data.js','icon.svg','manifest.webmanifest'];
self.addEventListener('install',e=>e.waitUntil(caches.open(V).then(c=>c.addAll(SHELL)).then(()=>self.skipWaiting())));
self.addEventListener('activate',e=>e.waitUntil(caches.keys().then(k=>Promise.all(k.filter(x=>x!==V).map(x=>caches.delete(x)))).then(()=>self.clients.claim())));
self.addEventListener('fetch',e=>{
  if(e.request.method!=='GET')return;
  e.respondWith(caches.match(e.request).then(hit=>hit||fetch(e.request).then(r=>{
    if(r.ok&&(new URL(e.request.url).origin===location.origin||/fonts\.g/.test(e.request.url))){const c=r.clone();caches.open(V).then(x=>x.put(e.request,c));}
    return r;}).catch(()=>caches.match('index.html'))));
});
self.addEventListener('push',e=>{const d=e.data?e.data.json():{title:'سفرنا',body:'تحديث جديد'};
  e.waitUntil(self.registration.showNotification(d.title,{body:d.body,icon:'icon.svg',dir:'rtl',lang:'ar'}));});
