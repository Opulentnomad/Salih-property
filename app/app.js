(()=>{
const D=window.SAFARNA_DATA,$=(s,r=document)=>r.querySelector(s),main=$('#main');
const store={get(k,d){try{return JSON.parse(localStorage.getItem('sf_'+k))??d}catch{return d}},set(k,v){try{localStorage.setItem('sf_'+k,JSON.stringify(v))}catch{}}};
const T={ar:{nav_home:'الرئيسية',nav_explore:'استكشف',nav_trips:'رحلاتي',nav_saved:'المفضلة',nav_tools:'أدوات'},
         en:{nav_home:'Home',nav_explore:'Explore',nav_trips:'Trips',nav_saved:'Saved',nav_tools:'Tools'}};
let lang=store.get('lang','ar'),state={q:'',region:'',visa:'',budget:'',season:''};
/* دمج تعديلات لوحة الإدارة (CMS تجريبي) */
const ov=store.get('admin_overrides',{});
const dests=D.destinations.map(d=>ov[d.id]?{...d,req:{...d.req,...ov[d.id]}}:d);
const byId=id=>dests.find(d=>d.id===id);
const nm=d=>lang==='en'?d.en:d.ar;
const esc=s=>String(s).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const favs=()=>store.get('favs',[]);
const toast=m=>{const t=$('#toast');t.textContent=m;t.classList.add('show');setTimeout(()=>t.classList.remove('show'),2200)};
const vis=d=>{const v=D.visaLabels[d.req.visa];return `<span class="pill ${v.c}">${v[lang]}</span>`};
const bud=n=>'💰'.repeat(n);
function applyLang(){document.documentElement.lang=lang;document.documentElement.dir=lang==='ar'?'rtl':'ltr';
  $('#langBtn').textContent=lang==='ar'?'EN':'عربي';$('#brandName').textContent=lang==='ar'?'سفرنا':'Safarna';
  document.querySelectorAll('[data-t]').forEach(e=>e.textContent=T[lang][e.dataset.t])}
function theme(t){document.documentElement.dataset.theme=t;store.set('theme',t);$('#themeBtn').textContent=t==='dark'?'☀️':'🌙'}

function card(d){const on=favs().includes(d.id);
 return `<article class="card"><a class="dest" href="#/d/${d.id}"><div class="cover" style="background:linear-gradient(135deg,${d.g[0]},${d.g[1]})">${d.emoji}
 </div></a><button class="fav ${on?'on':''}" data-fav="${d.id}" aria-pressed="${on}" aria-label="حفظ ${esc(d.ar)} في المفضلة">${on?'❤️':'🤍'}</button>
 <a class="dest" href="#/d/${d.id}"><div class="body"><div class="row"><b>${nm(d)}</b><span class="stars" aria-label="تقييم ${d.rating}">★ ${d.rating}</span></div>
 <p class="mu">${d.city} · ✈️ ${d.flight} · ${bud(d.budget)}</p>${vis(d)}</div></a></article>`}

function recommend(){const p=store.get('prefs',{});
 return [...dests].map(d=>{let s=d.rating;if(p.interests)s+=d.tags.filter(t=>p.interests.includes(t)).length*2;
  if(p.budget&&d.budget<=p.budget)s+=1.5;if(d.req.visa==='free'||d.req.visa==='gcc')s+=1;return{d,s}}).sort((a,b)=>b.s-a.s).map(x=>x.d)}

const views={
 onboarding(step=0){
  const steps=[
   ['🧳','أهلاً بك في سفرنا','دليلك لشروط الدخول والتأشيرة لأي وجهة بجواز سعودي، مع تخطيط الرحلة في مكان واحد.'],
   ['🛂','شروط دخول موثوقة','نعرض نوع التأشيرة والمستندات والتطعيمات وتاريخ آخر تحقق، مع رابط المصدر الرسمي.'],
   ['🎯','خصّص تجربتك','اختر اهتماماتك وميزانيتك لنقترح عليك وجهات مناسبة.']];
  const s=steps[step];
  main.innerHTML=`<section class="onb"><div class="big">${s[0]}</div><h1>${s[1]}</h1><p class="mu">${s[2]}</p>
  ${step===2?`<div class="chips" style="justify-content:center;flex-wrap:wrap" id="int">${['تاريخ','شاطئ','تسوق','طبيعة','عائلي','طعام','ثقافة','اقتصادي'].map(t=>`<button class="chip" aria-pressed="false">${t}</button>`).join('')}</div>
  <label for="bud">ميزانيتك</label><select id="bud"><option value="1">اقتصادية</option><option value="2" selected>متوسطة</option><option value="3">مريحة</option></select>`:''}
  <div class="dots">${[0,1,2].map(i=>`<i class="${i===step?'on':''}"></i>`).join('')}</div>
  <button class="btn pri" id="next">${step===2?'ابدأ':'التالي'}</button><button class="btn ghost" id="skip">تخطي</button></section>`;
  $('#int')?.addEventListener('click',e=>{const b=e.target.closest('.chip');if(b)b.setAttribute('aria-pressed',b.getAttribute('aria-pressed')!=='true')});
  $('#skip').onclick=()=>{store.set('onboarded',true);location.hash='#/'};
  $('#next').onclick=()=>{if(step<2)return views.onboarding(step+1);
   store.set('prefs',{interests:[...document.querySelectorAll('#int [aria-pressed=true]')].map(b=>b.textContent),budget:+$('#bud').value});
   store.set('onboarded',true);location.hash='#/'}},
 home(){const rec=recommend().slice(0,4);
  main.innerHTML=`<section class="hero"><h1>إلى أين وجهتك القادمة؟</h1><p>اعرف شروط الدخول بجواز سفرك السعودي قبل الحجز.</p>
  <form class="search" id="sf" role="search"><label class="sr" for="q" style="position:absolute;left:-999px">ابحث عن وجهة</label><input id="q" placeholder="ابحث عن دولة أو مدينة…" autocomplete="off"><button class="btn">بحث</button></form></section>
  <h2>وجهات بدون تأشيرة مسبقة</h2><div class="grid">${dests.filter(d=>['free','gcc'].includes(d.req.visa)).slice(0,3).map(card).join('')}</div>
  <h2>مقترحة لك</h2><div class="grid">${rec.map(card).join('')}</div>
  <p class="note" style="margin-top:20px">⚠️ البيانات في هذه النسخة تجريبية. تحقّق دائمًا من الجهة الرسمية أو شركة الطيران قبل السفر.</p>`;
  $('#sf').onsubmit=e=>{e.preventDefault();state.q=$('#q').value;location.hash='#/explore'}},
 explore(){
  const f=dests.filter(d=>(!state.q||(d.ar+d.en+d.city).toLowerCase().includes(state.q.toLowerCase()))&&(!state.region||d.region===state.region)
   &&(!state.visa||d.req.visa===state.visa)&&(!state.budget||d.budget==state.budget)&&(!state.season||d.seasons.includes(state.season)));
  const opt=(o,sel)=>Object.entries(o).map(([k,v])=>`<option value="${k}" ${sel===k?'selected':''}>${v}</option>`).join('');
  main.innerHTML=`<h1>استكشف الوجهات</h1><input id="q" type="search" placeholder="ابحث…" value="${esc(state.q)}" aria-label="بحث">
  <div class="grid" style="grid-template-columns:repeat(auto-fit,minmax(150px,1fr));margin:10px 0">
  <select id="region" aria-label="المنطقة"><option value="">كل المناطق</option>${opt(D.regions,state.region)}</select>
  <select id="visa" aria-label="التأشيرة"><option value="">كل أنواع الدخول</option>${opt(Object.fromEntries(Object.entries(D.visaLabels).map(([k,v])=>[k,v.ar])),state.visa)}</select>
  <select id="budget" aria-label="الميزانية"><option value="">كل الميزانيات</option>${opt({1:'اقتصادية',2:'متوسطة',3:'مريحة'},state.budget)}</select>
  <select id="season" aria-label="الموسم"><option value="">كل المواسم</option>${opt({winter:'شتاء',spring:'ربيع',summer:'صيف',autumn:'خريف'},state.season)}</select></div>
  <p class="mu" aria-live="polite">${f.length} وجهة</p>
  <div class="grid">${f.map(card).join('')||'<div class="empty">لا نتائج مطابقة. جرّب تخفيف الفلاتر.</div>'}</div>`;
  $('#q').oninput=e=>{state.q=e.target.value;clearTimeout(views._t);views._t=setTimeout(()=>{views.explore();$('#q').focus();$('#q').setSelectionRange(99,99)},250)};
  ['region','visa','budget','season'].forEach(k=>$('#'+k).onchange=e=>{state[k]=e.target.value;views.explore()})},
 detail(id,tab='req'){const d=byId(id);if(!d)return views.home();const r=d.req;
  const rv=store.get('reviews',[]).filter(x=>x.d===id).concat(D.reviews.filter(x=>x.d===id));
  const T3={req:'شروط الدخول',ov:'نظرة عامة',rev:`المراجعات (${rv.length})`};
  let body='';
  if(tab==='req')body=`<div class="card body">
   <div class="row"><b>نوع الدخول</b>${vis(d)}</div>
   <div class="kv" style="margin-top:12px"><b>مدة الإقامة</b><span>حتى ${r.stay} يومًا</span><b>صلاحية الجواز</b><span>${esc(r.passport)}</span>
   <b>التطعيمات</b><span>${esc(r.vaccines)}</span><b>الحجر الصحي</b><span>${esc(r.quarantine)}</span></div>
   <h2>المستندات المطلوبة</h2><ul class="list">${r.docs.map(x=>`<li>${esc(x)}</li>`).join('')}</ul>
   <h2>السفارة / القنصلية</h2><div class="kv"><b>الجهة</b><span>${esc(r.embassy.name)}</span><b>الهاتف</b><a href="tel:${r.embassy.phone.replace(/\s/g,'')}">${r.embassy.phone}</a><b>العنوان</b><span>${esc(r.embassy.addr)}</span></div>
   <p class="mu" style="margin-top:12px">آخر تحقق: ${r.verified} · المصدر: ${esc(r.source)}</p>
   <p class="note">⚠️ بيانات تجريبية. المتطلبات تتغير؛ تحقق من المصدر الرسمي أو Timatic عبر شركة الطيران.</p></div>`;
  if(tab==='ov')body=`<div class="card body"><p>${esc(d.about)}</p><div class="kv" style="margin-top:10px"><b>مدة الطيران</b><span>${d.flight} من الرياض</span><b>الميزانية</b><span>${bud(d.budget)}</span><b>أفضل المواسم</b><span>${d.seasons.map(s=>({winter:'شتاء',spring:'ربيع',summer:'صيف',autumn:'خريف'}[s])).join('، ')}</span><b>الاهتمامات</b><span>${d.tags.join('، ')}</span></div>
   <h2>احجز</h2><div class="row" style="flex-wrap:wrap;justify-content:flex-start"><a class="btn sm" rel="sponsored noopener" target="_blank" href="https://www.example.com/flights?to=${d.id}">✈️ رحلات</a><a class="btn sm" rel="sponsored noopener" target="_blank" href="https://www.example.com/hotels?c=${d.id}">🏨 فنادق</a></div>
   <p class="mu">روابط تجريبية — تُستبدل بروابط الشركاء/الأفلييت. قد نحصل على عمولة.</p></div>`;
  if(tab==='rev')body=`<div class="card body">${rv.map(x=>`<div style="padding:8px 0;border-bottom:1px dashed var(--bd)"><b>${esc(x.n)}</b> <span class="stars">${'★'.repeat(x.s)}</span><p>${esc(x.t)}</p></div>`).join('')||'<p class="empty">كن أول من يكتب مراجعة.</p>'}
   <form id="rf"><label for="rt">مراجعتك</label><textarea id="rt" rows="3" required maxlength="300"></textarea><label for="rs">التقييم</label><select id="rs"><option>5</option><option>4</option><option>3</option><option>2</option><option>1</option></select><button class="btn pri" style="margin-top:10px">نشر</button></form></div>`;
  const on=favs().includes(id);
  main.innerHTML=`<div class="dhero" style="background:linear-gradient(135deg,${d.g[0]},${d.g[1]})"><div class="e">${d.emoji}</div><h1>${nm(d)}</h1><p>${d.city}</p></div>
  <div class="row" style="margin-top:12px"><button class="btn pri" data-fav="${id}" aria-pressed="${on}">${on?'❤️ في المفضلة':'🤍 احفظ'}</button><button class="btn" id="addtrip">＋ أضف لرحلة</button><button class="btn ghost" id="alert">🔔 نبّهني</button></div>
  <div class="tabs" role="tablist">${Object.entries(T3).map(([k,v])=>`<button role="tab" class="tab" data-tab="${k}" aria-selected="${k===tab}">${v}</button>`).join('')}</div>${body}`;
  document.querySelectorAll('[data-tab]').forEach(b=>b.onclick=()=>views.detail(id,b.dataset.tab));
  $('#addtrip').onclick=()=>{const t=store.get('trips',[]);t.push({id:Date.now(),d:id,start:'',days:5,plan:[]});store.set('trips',t);toast('أُضيفت إلى رحلاتي');};
  $('#alert').onclick=async()=>{const a=store.get('alerts',[]);if(!a.includes(id))a.push(id);store.set('alerts',a);
    if('Notification'in window&&Notification.permission==='default')await Notification.requestPermission();toast('سننبهك عند تغيّر شروط '+d.ar)};
  $('#rf')?.addEventListener('submit',e=>{e.preventDefault();const l=store.get('reviews',[]);l.unshift({d:id,n:'أنت',s:+$('#rs').value,t:$('#rt').value.trim()});store.set('reviews',l);toast('شكرًا لمراجعتك');views.detail(id,'rev')})},
 saved(){const l=favs().map(byId).filter(Boolean);
  main.innerHTML=`<h1>المفضلة</h1>${l.length?`<div class="grid">${l.map(card).join('')}</div>`:'<div class="empty">🤍<p>لم تحفظ أي وجهة بعد.</p><a class="btn" href="#/explore">استكشف</a></div>'}`},
 trips(){const t=store.get('trips',[]);
  main.innerHTML=`<h1>رحلاتي</h1>${t.map(x=>{const d=byId(x.d);return `<div class="card body" style="margin-bottom:12px"><div class="row"><b>${d.emoji} ${d.ar}</b><button class="btn ghost sm" data-del="${x.id}" aria-label="حذف">🗑️</button></div>
   <div class="row" style="margin-top:8px"><div style="flex:1"><label for="s${x.id}">تاريخ السفر</label><input type="date" id="s${x.id}" value="${x.start}" data-k="start" data-i="${x.id}"></div>
   <div style="flex:1"><label for="n${x.id}">الأيام</label><input type="number" min="1" max="30" id="n${x.id}" value="${x.days}" data-k="days" data-i="${x.id}"></div></div>
   ${Array.from({length:x.days},(_,i)=>`<div class="day"><b>اليوم ${i+1}</b><input aria-label="خطة اليوم ${i+1}" placeholder="مثال: زيارة المعالم…" value="${esc(x.plan[i]||'')}" data-plan="${i}" data-i="${x.id}"></div>`).join('')}
   ${x.start&&d.req.visa!=='free'&&d.req.visa!=='gcc'?`<p class="note">🛂 تذكير: ابدأ إجراءات الدخول (${D.visaLabels[d.req.visa].ar}) قبل السفر بوقت كافٍ.</p>`:''}</div>`}).join('')||'<div class="empty">🗓️<p>لا رحلات بعد. افتح أي وجهة واضغط «أضف لرحلة».</p></div>'}`;
  main.oninput=e=>{const el=e.target,i=el.dataset.i;if(!i)return;const all=store.get('trips',[]),x=all.find(y=>y.id==i);
   if(el.dataset.k)x[el.dataset.k]=el.dataset.k==='days'?Math.min(30,Math.max(1,+el.value||1)):el.value;if(el.dataset.plan!==undefined)x.plan[el.dataset.plan]=el.value;store.set('trips',all)};
  main.onchange=e=>{if(e.target.dataset.k==='days')views.trips()};
  main.onclick=e=>{const b=e.target.closest('[data-del]');if(b){store.set('trips',store.get('trips',[]).filter(x=>x.id!=b.dataset.del));views.trips()}}},
 tools(){const cur=Object.keys(D.ratesPerSAR);
  main.innerHTML=`<h1>الأدوات</h1><div class="card body"><h2 style="margin-top:0">💱 محوّل العملات</h2>
  <label for="amt">المبلغ</label><input id="amt" type="number" inputmode="decimal" value="1000">
  <div class="row" style="margin-top:8px"><div style="flex:1"><label for="from">من</label><select id="from">${cur.map(c=>`<option ${c==='SAR'?'selected':''}>${c}</option>`).join('')}</select></div>
  <div style="flex:1"><label for="to">إلى</label><select id="to">${cur.map(c=>`<option ${c==='TRY'?'selected':''}>${c}</option>`).join('')}</select></div></div>
  <p id="res" style="font-size:24px;font-weight:800;margin:12px 0" aria-live="polite"></p><p class="mu" id="src"></p></div>
  <div class="card body" style="margin-top:14px"><h2 style="margin-top:0">🔔 التنبيهات</h2><p class="mu">تنبيهات عند تغيّر شروط الدخول للوجهات المتابَعة (Push / بريد).</p>
  <p>الوجهات المتابَعة: ${store.get('alerts',[]).map(i=>byId(i)?.ar).join('، ')||'لا شيء'}</p>
  <label for="em">البريد للتنبيهات</label><input id="em" type="email" placeholder="name@example.com" value="${esc(store.get('email',''))}"><button class="btn pri" id="pushbtn" style="margin-top:10px">تفعيل التنبيهات</button></div>
  <div class="card body" style="margin-top:14px"><h2 style="margin-top:0">⚙️ الإعدادات</h2><button class="btn ghost" id="reset">إعادة الإعداد الأولي</button> <a class="btn ghost" href="admin.html">لوحة الإدارة</a></div>`;
  let rates={...D.ratesPerSAR};
  const calc=()=>{const a=+$('#amt').value||0,f=$('#from').value,t=$('#to').value;$('#res').textContent=(a/rates[f]*rates[t]).toLocaleString(lang==='ar'?'ar-SA':'en',{maximumFractionDigits:2})+' '+t};
  ['amt','from','to'].forEach(i=>$('#'+i).oninput=calc);calc();$('#src').textContent='أسعار تقريبية (دون اتصال)';
  fetch('https://open.er-api.com/v6/latest/SAR').then(r=>r.json()).then(j=>{if(j.rates){cur.forEach(c=>{if(j.rates[c])rates[c]=j.rates[c]});store.set('rates',rates);calc();$('#src').textContent='أسعار مباشرة · '+new Date().toLocaleDateString('ar-SA')}}).catch(()=>{const c=store.get('rates');if(c){rates=c;calc();$('#src').textContent='آخر أسعار محفوظة'}});
  $('#pushbtn').onclick=async()=>{store.set('email',$('#em').value);if('Notification'in window){const p=await Notification.requestPermission();toast(p==='granted'?'تم تفعيل التنبيهات':'لم يُمنح الإذن')}else toast('المتصفح لا يدعم التنبيهات')};
  $('#reset').onclick=()=>{store.set('onboarded',false);location.hash='#/welcome'}}
};
function route(){const h=location.hash.replace(/^#\/?/,'').split('/'),r=h[0]||'home';
 if(!store.get('onboarded',false)&&r!=='welcome'){location.hash='#/welcome';return}
 document.querySelectorAll('.tabbar a').forEach(a=>a.classList.toggle('on',a.dataset.r===(r==='d'?'explore':r)));
 document.querySelector('.tabbar').hidden=r==='welcome';
 if(r==='welcome')views.onboarding();else if(r==='d')views.detail(h[1]);else (views[r]||views.home)();
 main.scrollTo?.(0,0);window.scrollTo(0,0);main.focus({preventScroll:true})}
document.addEventListener('click',e=>{const b=e.target.closest('[data-fav]');if(!b)return;e.preventDefault();
 let f=favs();const id=b.dataset.fav;f=f.includes(id)?f.filter(x=>x!==id):[...f,id];store.set('favs',f);toast(f.includes(id)?'أُضيفت إلى المفضلة':'أُزيلت من المفضلة');route()});
$('#langBtn').onclick=()=>{lang=lang==='ar'?'en':'ar';store.set('lang',lang);applyLang();route()};
$('#themeBtn').onclick=()=>theme(document.documentElement.dataset.theme==='dark'?'light':'dark');
const off=()=>$('#offline').hidden=navigator.onLine;addEventListener('online',off);addEventListener('offline',off);off();
theme(store.get('theme',matchMedia('(prefers-color-scheme:dark)').matches?'dark':'light'));applyLang();
addEventListener('hashchange',route);route();
if('serviceWorker'in navigator)addEventListener('load',()=>navigator.serviceWorker.register('sw.js').catch(()=>{}));
})();
