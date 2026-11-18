(()=>{
const D=window.SAFARNA_DATA,$=(s,r=document)=>r.querySelector(s),main=$('#main');
const store={get(k,d){try{return JSON.parse(localStorage.getItem('sf_'+k))??d}catch{return d}},set(k,v){try{localStorage.setItem('sf_'+k,JSON.stringify(v))}catch{}}};
let lang=store.get('lang','ar'),state={q:'',region:'',visa:'',budget:'',season:'',month:0,fav:'all'};
/* تعديلات لوحة الإدارة (CMS تجريبي) */
const ov=store.get('admin_overrides',{});
let dests=D.destinations.map(d=>ov[d.id]?{...d,req:{...d.req,...ov[d.id]}}:d);
let apiOn=false;const apiRev={};
const api=async(path,o={})=>{const t=store.get('token');const r=await fetch('/api'+path,{method:o.method||'GET',headers:{...(o.body?{'content-type':'application/json'}:{}),...(t?{authorization:'Bearer '+t}:{})},body:o.body?JSON.stringify(o.body):undefined});const txt=await r.text();let j=null;try{j=JSON.parse(txt)}catch{}if(!r.ok)throw Object.assign(new Error(j?.message||'error'),{status:r.status});return j};
const byId=id=>dests.find(d=>d.id===id);
const nm=d=>lang==='en'?d.en:d.ar;
const esc=s=>String(s).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const favs=()=>store.get('favs',[]);
const toast=m=>{const t=$('#toast');t.textContent=m;t.classList.add('show');setTimeout(()=>t.classList.remove('show'),2200)};
const SHORT={free:'مسموح',gcc:'بالهوية',voa:'عند الوصول',evisa:'إلكترونية',eta:'ETA',required:'تأشيرة مسبقة'};
const KIND={ok:'ok',warn:'warn',bad:'bad'};
const tag=d=>{const v=D.visaLabels[d.req.visa];return `<span class="tag t-${v.c}">${SHORT[d.req.visa]}</span>`};
const SEAL={free:'مسموح بالدخول بدون تأشيرة',gcc:'مسموح بالدخول بالهوية الوطنية',voa:'تأشيرة عند الوصول',evisa:'تحتاج تأشيرة إلكترونية قبل السفر',eta:'تحتاج تصريح سفر إلكتروني (ETA)',required:'تحتاج تأشيرة من السفارة قبل السفر'};
const sealC=d=>D.visaLabels[d.req.visa].c;
const ar=x=>String(x).replace(/\d/g,d=>'٠١٢٣٤٥٦٧٨٩'[d]);
const norm=x=>String(x||'').toLowerCase().replace(/[\u064B-\u065F\u0640]/g,'').replace(/[أإآٱ]/g,'ا').replace(/ة/g,'ه').replace(/ى/g,'ي').replace(/ؤ/g,'و').replace(/ئ/g,'ي').replace(/\s+/g,' ').trim();
const ALIAS={us:'امريكا usa نيويورك لوس انجلوس new york los angeles miami',gb:'انجلترا بريطانيا لندن uk المملكه المتحده london england',ae:'دبي ابوظبي الشارقه الامارات dubai abu dhabi',tr:'اسطنبول استانبول istanbul istanbul antalya',fr:'باريس نيس paris nice',it:'روما ميلانو فينيسيا rome milan venice',es:'مدريد برشلونه اسبانيا madrid barcelona',de:'برلين ميونخ المانيا berlin munich',jp:'طوكيو اوساكا tokyo osaka kyoto',eg:'القاهره شرم الشيخ الغردقه الاقصر cairo sharm el sheikh hurghada luxor',th:'بانكوك بوكيت bangkok phuket',my:'كوالالمبور لنكاوي kuala lumpur langkawi',id:'بالي جاكرتا bali jakarta',mv:'ماليه male maldives',ge:'تبليسي باتومي tbilisi batumi',az:'باكو baku',ma:'مراكش الدار البيضاء marrakech casablanca',jo:'البتراء العقبه petra amman aqaba',lb:'بيروت beirut',ch:'زيورخ جنيف zurich geneva',gr:'اثينا سانتوريني athens santorini',nl:'امستردام هولندا amsterdam',at:'فيينا vienna',pt:'لشبونه lisbon',cn:'بكين شنغهاي beijing shanghai',kr:'سيول seoul',in:'دلهي مومباي delhi mumbai',sg:'سنغافوره singapore',ru:'موسكو moscow',ca:'تورنتو فانكوفر toronto vancouver',au:'سيدني ملبورن sydney melbourne',za:'كيب تاون cape town',qa:'الدوحه doha',kw:'الكويت',bh:'المنامه',om:'مسقط صلاله muscat salalah',sa:'',cz:'براغ',hu:'بودابست',pl:'وارسو',ie:'دبلن',se:'ستوكهولم',no:'اوسلو',dk:'كوبنهاجن',fi:'هلسنكي',be:'بروكسل',ba:'سراييفو',al:'تيرانا',am:'يريفان',uz:'طشقند سمرقند',kz:'الماتي استانا',lk:'كولومبو',ph:'مانيلا',vn:'هانوي',kh:'بنوم بنه',mx:'مكسيكو كانكون',br:'ريو ساو باولو',ar:'بوينس ايرس',cy:'قبرص',mt:'مالطا',tn:'تونس',dz:'الجزائر',ke:'نيروبي',tz:'زنجبار',mu:'موريشيوس',sc:'سيشل'};
const match=(d,q)=>{const h=norm([d.ar,d.en,d.city,d.iata,ALIAS[d.id]||''].join(' '));return norm(q).split(' ').filter(Boolean).every(t=>h.includes(t))};
const MONTHS=['يناير','فبراير','مارس','أبريل','مايو','يونيو','يوليو','أغسطس','سبتمبر','أكتوبر','نوفمبر','ديسمبر'];
const SEAS={winter:[12,1,2],spring:[3,4,5],summer:[6,7,8],autumn:[9,10,11]};
const monthsOf=d=>d.months?.length?d.months:[...new Set((d.seasons||[]).flatMap(s=>SEAS[s]||[]))];
const ORDER={free:0,gcc:0,voa:1,evisa:1,eta:1,required:2};
const bud=n=>'💰'.repeat(n);
function applyLang(){document.documentElement.lang=lang;document.documentElement.dir=lang==='ar'?'rtl':'ltr';$('#langBtn').textContent=lang==='ar'?'EN':'عربي';$('#brandName').textContent=lang==='ar'?'سفرنا':'Safarna'}
function theme(t){document.documentElement.dataset.theme=t;store.set('theme',t);$('#themeBtn').textContent=t==='dark'?'☀️':'🌙'}
const heart=d=>{const on=favs().includes(d.id);return `<button class="heart" data-fav="${d.id}" aria-pressed="${on}" aria-label="${on?'إزالة':'حفظ'} ${esc(d.ar)} ${on?'من':'في'} المفضلة">${on?'♥':'♡'}</button>`};
const passCard=d=>`<article class="pass"><a class="main" href="#/d/${d.id}"><div class="eyebrow">من الرياض${d.flight?' · ✈️ '+ar(d.flight):''}</div><div class="code big">${d.iata}</div><div class="dest">${nm(d)}${d.city?' · '+d.city:''}</div></a><div class="stub">${tag(d)}${heart(d)}</div></article>`;
const verifyNote=d=>{const r=d.req,dt=String(r.verified).slice(0,10);return r.reviewed?`<p class="note">✓ راجعها محرر · تاريخ النشر ${dt} · المصدر: ${esc(r.source)}${r.stale?' · ⚠️ مضى أكثر من 30 يومًا، تحقق من الجهة الرسمية':''}</p>`:`<p class="note">⚠️ غير موثّقة رسميًا. نوع الدخول مأخوذ من ${esc(r.source)} بتاريخ ${dt}${r.stale?' (مضى عليه أكثر من 30 يومًا)':''}. تأكد من سفارة الدولة أو شركة الطيران قبل الحجز.</p>`};
const bigPass=d=>{const r=d.req;return `<div class="pass vpass"><div class="main"><div class="eyebrow">بطاقة صعود · جواز سعودي</div>
 <div class="route"><span class="code big">RUH</span><span class="line"></span><span class="code big">${d.iata}</span></div>
 <div class="cities"><span>الرياض</span><span>${d.city||nm(d)}</span></div>
 <div class="facts"><div><b>${r.stay?ar(r.stay)+' يومًا':'غير محددة'}</b><span>مدة الإقامة</span></div><div><b>${r.passport?ar(esc(r.passport)):'تحقق من المصدر'}</b><span>صلاحية الجواز</span></div><div><b>${r.vaccines?(r.vaccines.startsWith('لا')?'لا يوجد':'يوجد'):'غير محدد'}</b><span>تطعيم</span></div></div>
 <div class="seal ${sealC(d)}"><span>${sealC(d)==='ok'?'✓':sealC(d)==='warn'?'!':'✕'}</span>${SEAL[r.visa]}</div><div class="bars" aria-hidden="true"></div></div></div>`};
const recommend=()=>{const p=store.get('prefs',{});return [...dests].map(d=>{let s=d.rating;if(p.interests)s+=d.tags.filter(t=>p.interests.includes(t)).length*2;if(p.budget&&d.budget<=p.budget)s+=1.5;if(['free','gcc'].includes(d.req.visa))s+=1;return{d,s}}).sort((a,b)=>b.s-a.s).map(x=>x.d)};

const views={
 onboarding(step=0){
  const sample=byId('tr');
  const steps=[['اعرف إن كنت تدخل البلد قبل أن تحجز التذكرة','اختر الوجهة، ونعرض لك التأشيرة والمستندات المطلوبة لجواز سفرك السعودي.'],['شروط دخول بتاريخ تحقق ومصدر','كل وجهة تعرض آخر تحقق ومصدرها الرسمي، لتعرف مدى حداثة المعلومة.'],['خصّص اقتراحاتك','اختر اهتماماتك وميزانيتك لنقترح عليك وجهات مناسبة.']];
  const s=steps[step];
  main.innerHTML=`<section class="onb"><h1>${s[0]}</h1><p class="mu">${s[1]}</p>
  ${step<2?bigPass(sample):`<div class="panel"><div class="chips" style="flex-wrap:wrap" id="int">${['تاريخ','شاطئ','تسوق','طبيعة','عائلي','طعام','ثقافة','اقتصادي'].map(t=>`<button class="chip" aria-pressed="false">${t}</button>`).join('')}</div>
  <label for="bud">ميزانيتك</label><select id="bud"><option value="1">اقتصادية</option><option value="2" selected>متوسطة</option><option value="3">مريحة</option></select></div>`}
  <div class="dots" aria-hidden="true">${[0,1,2].map(i=>`<i class="${i===step?'on':''}"></i>`).join('')}</div>
  <button class="btn" id="next">${step===2?'ابدأ':'التالي'}</button><button class="link mu" id="skip" style="min-height:44px">تخطّي</button></section>`;
  $('#int')?.addEventListener('click',e=>{const b=e.target.closest('.chip');if(b)b.setAttribute('aria-pressed',b.getAttribute('aria-pressed')!=='true')});
  $('#skip').onclick=()=>{store.set('onboarded',true);location.hash='#/'};
  $('#next').onclick=()=>{if(step<2)return views.onboarding(step+1);
   store.set('prefs',{interests:[...document.querySelectorAll('#int [aria-pressed=true]')].map(b=>b.textContent),budget:+$('#bud').value});store.set('onboarded',true);location.hash='#/'}},
 home(){
  const GROUPS={all:['الكل',()=>true],free:['بدون تأشيرة',d=>['free','gcc'].includes(d.req.visa)],easy:['إلكترونية أو عند الوصول',d=>['voa','evisa','eta'].includes(d.req.visa)],need:['تأشيرة مسبقة',d=>d.req.visa==='required']};
  const cnt=k=>dests.filter(GROUPS[k][1]).length;
  const rows=q=>{const f=dests.filter(d=>GROUPS[state.hf||'all'][1](d)&&(!q||match(d,q))).sort((a,b)=>ORDER[a.req.visa]-ORDER[b.req.visa]||(b.rating||0)-(a.rating||0)||a.ar.localeCompare(b.ar,'ar'));
   return f.map(d=>`<a class="crow" href="#/d/${d.id}"><span class="code">${d.iata}</span><span class="cn"><b>${d.emoji||''} ${nm(d)}</b>${d.city?`<small>${d.city}</small>`:''}</span>${tag(d)}<span class="chev" aria-hidden="true">‹</span></a>`).join('')||'<div class="hint">ما لقينا هذي الوجهة. جرّب اسمًا ثانيًا، أو اسم مدينة، أو افتح «استكشف».</div>'};
  const recent=store.get('recent',[]).map(byId).filter(Boolean);
  main.innerHTML=`<h1>وين رايح؟</h1><p class="mu">اكتب اسم الدولة أو المدينة وشوف إذا تحتاج تأشيرة بجواز سفرك السعودي.</p>
  <div class="search"><span aria-hidden="true">🔍</span><input id="q" type="search" placeholder="مثال: تركيا، لندن، امريكا، IST" autocomplete="off" aria-label="ابحث عن وجهة"></div>
  ${recent.length?`<p class="mu" style="margin:2px 0">شاهدتها مؤخرًا</p><div class="chips" id="recent">${recent.map(d=>`<a class="chip" href="#/d/${d.id}">${d.emoji||''} ${nm(d)}</a>`).join('')}</div>`:''}
  <div class="chips" id="hf" role="group" aria-label="تصفية حسب التأشيرة">${Object.keys(GROUPS).map(k=>`<button class="chip" data-g="${k}" aria-pressed="${(state.hf||'all')===k}">${GROUPS[k][0]} (${cnt(k)})</button>`).join('')}</div>
  <button class="crow" id="mnow" style="width:100%;text-align:right;border:0;color:inherit"><span style="font-size:24px;width:58px;text-align:center" aria-hidden="true">🗓️</span><span class="cn"><b>وين أروح في ${MONTHS[new Date().getMonth()]}؟</b><small>وجهات مناسبة لهذا الشهر · أو اختر شهرًا آخر</small></span><span class="chev" aria-hidden="true">‹</span></button>
  <div id="list">${rows('')}</div>
  <p class="note">⚠️ نوع الدخول مأخوذ من مصدر مفتوح (Passport Index) ولم تراجعه جهة رسمية. تأكد من سفارة الدولة أو شركة الطيران قبل الحجز.</p>`;
  $('#mnow').onclick=()=>{state.month=new Date().getMonth()+1;location.hash='#/explore'};
  $('#q').oninput=e=>{$('#list').innerHTML=rows(e.target.value)};
  $('#hf').onclick=e=>{const b=e.target.closest('[data-g]');if(!b)return;state.hf=b.dataset.g;document.querySelectorAll('#hf .chip').forEach(c=>c.setAttribute('aria-pressed',c===b));$('#list').innerHTML=rows($('#q').value)};
  $('#q').onkeydown=e=>{if(e.key==='Enter'){const a=$('#list .crow');if(a)location.hash=a.getAttribute('href')}}},
 explore(){
  const f=dests.filter(d=>(!state.q||match(d,state.q))&&(!state.region||d.region===state.region)&&(!state.visa||d.req.visa===state.visa)&&(!state.budget||d.budget==state.budget)&&(!state.season||d.seasons.includes(state.season))&&(!state.month||monthsOf(d).includes(state.month)));
  if(state.month)f.sort((a,b)=>ORDER[a.req.visa]-ORDER[b.req.visa]||(b.rating||0)-(a.rating||0)||a.ar.localeCompare(b.ar,'ar'));
  const known=dests.filter(d=>monthsOf(d).length).length;
  const opt=(o,sel)=>Object.entries(o).map(([k,v])=>`<option value="${k}" ${sel===k?'selected':''}>${v}</option>`).join('');
  main.innerHTML=`<h1>استكشف الوجهات</h1><div class="search"><span aria-hidden="true">🔍</span><input id="q" type="search" placeholder="ابحث…" value="${esc(state.q)}" aria-label="بحث"></div>
  <div class="chips" id="mchips" role="group" aria-label="الشهر"><button class="chip" data-m="0" aria-pressed="${!state.month}">كل الأشهر</button>${MONTHS.map((n,i)=>`<button class="chip" data-m="${i+1}" aria-pressed="${state.month===i+1}">${n}</button>`).join('')}</div>
  <div class="two" style="grid-template-columns:repeat(auto-fit,minmax(140px,1fr))">
  <select id="region" aria-label="المنطقة"><option value="">كل المناطق</option>${opt(D.regions,state.region)}</select>
  <select id="visa" aria-label="التأشيرة"><option value="">كل أنواع الدخول</option>${opt(Object.fromEntries(Object.entries(D.visaLabels).map(([k,v])=>[k,v.ar])),state.visa)}</select>
  <select id="budget" aria-label="الميزانية"><option value="">كل الميزانيات</option>${opt({1:'اقتصادية',2:'متوسطة',3:'مريحة'},state.budget)}</select>
  <select id="season" aria-label="الموسم"><option value="">كل المواسم</option>${opt({winter:'شتاء',spring:'ربيع',summer:'صيف',autumn:'خريف'},state.season)}</select></div>
  <p class="mu" aria-live="polite">${state.month?`${f.length} وجهة مناسبة لشهر ${MONTHS[state.month-1]}، الأسهل دخولًا أولًا`:`${f.length} وجهة`}</p>
  ${state.month?`<p class="note">تظهر الوجهات التي لدينا بيانات أشهرها فقط (${known} وجهة). الأشهر تقديرات عامة للطقس وليست مراجعة رسمية.</p>`:''}
  ${f.map(passCard).join('')||'<div class="hint">لا نتائج مطابقة. جرّب تخفيف الفلاتر.</div>'}`;
  $('#q').oninput=e=>{state.q=e.target.value;clearTimeout(views._t);views._t=setTimeout(()=>{views.explore();const q=$('#q');q.focus();q.setSelectionRange(99,99)},250)};
  ['region','visa','budget','season'].forEach(k=>$('#'+k).onchange=e=>{state[k]=e.target.value;views.explore()});
  document.querySelectorAll('#mchips [data-m]').forEach(b=>b.onclick=()=>{state.month=+b.dataset.m;views.explore()});
  const act=$('#mchips [aria-pressed=true]');if(act&&state.month)act.parentElement.scrollLeft=0,act.scrollIntoView({inline:'center',block:'nearest'})},
 check(){
  const sel=state.chk||'tr';
  main.innerHTML=`<h1>تحقق من التأشيرة</h1>
  <div class="field"><label for="pp">جواز السفر</label><select id="pp"><option>🇸🇦 المملكة العربية السعودية</option></select></div>
  <div class="two"><div class="field"><label for="dd">الوجهة</label><select id="dd">${[...dests].sort((a,b)=>a.ar.localeCompare(b.ar,'ar')).map(d=>`<option value="${d.id}" ${d.id===sel?'selected':''}>${d.ar}</option>`).join('')}</select></div>
  <div class="field"><label for="dt">تاريخ السفر</label><input id="dt" type="date"></div></div>
  <button class="btn" id="go">تحقق</button><div id="res" aria-live="polite" style="margin-top:16px"></div>`;
  const show=()=>{const d=byId($('#dd').value);state.chk=d.id;const r=d.req;
   $('#res').innerHTML=bigPass(d)+`<div class="panel"><h2 style="margin-top:0">المستندات المطلوبة</h2><ul class="list">${r.docs.map(x=>`<li>${esc(x)}</li>`).join('')}</ul></div>${verifyNote(d)}
   <div class="actions"><a class="btn ghost" href="#/d/${d.id}">تفاصيل الوجهة</a><button class="btn" id="addt">＋ رحلتي</button></div>`;
   $('#addt').onclick=()=>{const t=store.get('trips',[]);t.push({id:Date.now(),d:d.id,start:$('#dt').value,days:5,plan:[]});store.set('trips',t);toast('أُضيفت إلى رحلاتي')}};
  $('#go').onclick=show;$('#dd').onchange=show;show()},
 detail(id,tab='req'){const d=byId(id);if(!d)return views.home();const r=d.req;
  store.set('recent',[id,...store.get('recent',[]).filter(x=>x!==id)].slice(0,6));document.title=nm(d)+' — سفرنا';
  const rv=store.get('reviews',[]).filter(x=>x.d===id).concat(apiOn?(apiRev[id]||[]):D.reviews.filter(x=>x.d===id));
  if(apiOn&&!apiRev[id]){apiRev[id]=[];api('/destinations/'+id+'/reviews').then(j=>{apiRev[id]=j.items.map(x=>({d:id,n:x.author,s:x.rating,t:x.body}));if(location.hash==='#/d/'+id)views.detail(id,tab)}).catch(()=>{delete apiRev[id]})}
  const T3={req:'شروط الدخول',ov:'نظرة عامة',rev:`المراجعات (${rv.length})`};
  let body='';
  if(tab==='req')body=`${bigPass(d)}<div class="panel"><h2 style="margin-top:0">المستندات المطلوبة</h2>${(()=>{const done=store.get('docs_'+id,[]);return `<p class="mu" id="dprog">جهّزت ${ar(done.length)} من ${ar(r.docs.length)}</p><div class="prog" aria-hidden="true"><i id="dbar" style="--w:${Math.round(done.length/Math.max(1,r.docs.length)*100)}%"></i></div>`+r.docs.map((x,i)=>`<label class="doc ${done.includes(i)?'done':''}"><input type="checkbox" data-doc="${i}" ${done.includes(i)?'checked':''}><span>${esc(x)}</span></label>`).join('')})()}</div>
   <div class="panel"><div class="kv"><b>التطعيمات</b><span>${esc(r.vaccines||'غير محدد، تحقق من المصدر')}</span><b>الحجر الصحي</b><span>${esc(r.quarantine||'غير محدد، تحقق من المصدر')}</span></div>
   ${r.embassy?.name?`<h2>السفارة / القنصلية</h2><div class="kv"><b>الجهة</b><span>${esc(r.embassy.name)}</span><b>الهاتف</b><a href="tel:${esc((r.embassy.phone||'').replace(/\s/g,''))}">${esc(r.embassy.phone||'')}</a><b>العنوان</b><span>${esc(r.embassy.addr||'')}</span></div>`:''}</div>${verifyNote(d)}`;
  if(tab==='ov')body=`<div class="panel"><p>${esc(d.about||'لا توجد نبذة عن هذه الوجهة بعد.')}</p>${(()=>{const ms=monthsOf(d);return ms.length?`<h2>أفضل وقت للزيارة</h2><div class="mstrip" role="img" aria-label="أفضل الأشهر: ${ms.map(m=>MONTHS[m-1]).join('، ')}">${MONTHS.map((n,i)=>`<span class="${ms.includes(i+1)?'on':''}">${n}</span>`).join('')}</div>${d.monthsNote?`<p class="mu">${esc(d.monthsNote)}</p>`:''}<p class="mu">تقدير عام للطقس، وليس مراجعة رسمية.</p>`:''})()}<div class="kv" style="margin-top:10px">${d.flight?`<b>مدة الطيران</b><span>${ar(d.flight)} من الرياض</span>`:''}${d.budget?`<b>الميزانية</b><span>${bud(d.budget)}</span>`:''}${d.seasons?.length?`<b>أفضل المواسم</b><span>${d.seasons.map(s=>({winter:'شتاء',spring:'ربيع',summer:'صيف',autumn:'خريف'}[s])).join('، ')}</span>`:''}${d.tags?.length?`<b>الاهتمامات</b><span>${d.tags.join('، ')}</span>`:''}</div>
   <h2>احجز</h2><div class="actions"><a class="btn ghost" rel="sponsored noopener" target="_blank" href="https://www.example.com/flights?to=${d.id}">✈️ رحلات</a><a class="btn ghost" rel="sponsored noopener" target="_blank" href="https://www.example.com/hotels?c=${d.id}">🏨 فنادق</a></div>
   <p class="mu">روابط تجريبية — تُستبدل بروابط الشركاء. قد نحصل على عمولة.</p></div>`;
  if(tab==='rev'){const avg=(rv.reduce((a,x)=>a+x.s,0)/(rv.length||1)).toFixed(1);
   body=`<div class="pass vpass"><div class="main"><div class="sum"><div><div class="code big" style="font-size:72px">${rv.length?avg:'—'}</div><div class="stars" aria-hidden="true">★★★★★</div><div class="eyebrow">${rv.length} مراجعة</div></div></div></div></div>
   ${rv.map(x=>`<article class="slip"><div class="who"><span>المسافر: ${esc(x.n)}</span></div><div class="stars" aria-label="${x.s} من 5">${'★'.repeat(x.s)}${'☆'.repeat(5-x.s)}</div><p>${esc(x.t)}</p></article>`).join('')||'<div class="hint">كن أول من يكتب مراجعة.</div>'}
   <form id="rf" class="panel"><label for="rt">مراجعتك</label><textarea id="rt" rows="3" required maxlength="300"></textarea><label for="rs">التقييم</label><select id="rs"><option>5</option><option>4</option><option>3</option><option>2</option><option>1</option></select><button class="btn" style="margin-top:12px">اكتب مراجعتك</button></form>`}
  main.innerHTML=`<div class="row" style="display:flex;justify-content:space-between;align-items:center"><div><h1>${nm(d)}</h1><div class="mu">${[d.city,d.flight?'✈️ '+ar(d.flight):'',d.rating?'★ '+ar(d.rating):''].filter(Boolean).join(' · ')}</div></div><div style="display:flex;gap:8px"><button class="heart" id="share" style="color:var(--ink)" aria-label="مشاركة">⤴</button>${heart(d)}</div></div>
  <div class="tabs" role="tablist">${Object.entries(T3).map(([k,v])=>`<button role="tab" class="tab" data-tab="${k}" aria-selected="${k===tab}">${v}</button>`).join('')}</div>${body}
  <div class="actions"><button class="btn" id="addtrip">＋ أضف إلى رحلتي</button><button class="btn ghost" id="alert">🔔 نبّهني</button></div>`;
  document.querySelectorAll('[data-tab]').forEach(b=>b.onclick=()=>views.detail(id,b.dataset.tab));
  main.querySelectorAll('[data-doc]').forEach(c=>c.onchange=()=>{let done=store.get('docs_'+id,[]);const i=+c.dataset.doc;done=c.checked?[...new Set([...done,i])]:done.filter(x=>x!==i);store.set('docs_'+id,done);c.closest('.doc').classList.toggle('done',c.checked);$('#dprog').textContent=`جهّزت ${ar(done.length)} من ${ar(r.docs.length)}`;$('#dbar').style.setProperty('--w',Math.round(done.length/Math.max(1,r.docs.length)*100)+'%')});
  $('#share').onclick=async()=>{const url=location.href.split('#')[0]+'#/d/'+id,text=`${nm(d)}: ${SEAL[r.visa]} بجواز سعودي${r.reviewed?'':' (غير موثّقة رسميًا، تأكد من الجهة الرسمية)'} — سفرنا`;try{if(navigator.share){await navigator.share({title:nm(d)+' — سفرنا',text,url})}else{await navigator.clipboard.writeText(text+'\n'+url);toast('نُسخ الرابط')}}catch{try{await navigator.clipboard.writeText(text+'\n'+url);toast('نُسخ الرابط')}catch{toast('تعذّرت المشاركة')}}};
  $('#addtrip').onclick=()=>{const t=store.get('trips',[]);t.push({id:Date.now(),d:id,start:'',days:5,plan:[]});store.set('trips',t);toast('أُضيفت إلى رحلاتي')};
  $('#alert').onclick=async()=>{const a=store.get('alerts',[]);if(!a.includes(id))a.push(id);store.set('alerts',a);if('Notification'in window&&Notification.permission==='default')await Notification.requestPermission();toast('سننبهك عند تغيّر شروط '+d.ar)};
  $('#rf')?.addEventListener('submit',async e=>{e.preventDefault();const body=$('#rt').value.trim(),rating=+$('#rs').value;
   if(apiOn&&store.get('token')){try{await api('/destinations/'+id+'/reviews',{method:'POST',body:{rating,body}});toast('استلمنا مراجعتك وستظهر بعد المراجعة');$('#rt').value='';return}catch(err){toast(err.status===401?'سجّل الدخول من «الأدوات» أولًا':'تعذّر الإرسال: '+err.message);return}}
   const l=store.get('reviews',[]);l.unshift({d:id,n:'أنت',s:rating,t:body});store.set('reviews',l);toast('نُشرت مراجعتك');views.detail(id,'rev')})},
 saved(){const all=favs().map(byId).filter(Boolean);
  const l=all.filter(d=>state.fav==='all'||(state.fav==='free'?['free','gcc'].includes(d.req.visa):!['free','gcc'].includes(d.req.visa)));
  main.innerHTML=`<div style="display:flex;justify-content:space-between;align-items:end"><h1>المفضلة</h1><span class="mu">${all.length} وجهات</span></div>
  <div class="seg" role="group" aria-label="تصفية">${[['all','الكل'],['free','بدون تأشيرة'],['need','تحتاج تأشيرة']].map(([k,v])=>`<button data-f="${k}" aria-pressed="${state.fav===k}">${v}</button>`).join('')}</div>
  ${l.map(passCard).join('')}<div class="hint">${all.length?'':'لم تحفظ أي وجهة بعد. '}اضغط ♡ في أي وجهة لتظهر هنا، وتبقى متاحة بدون إنترنت.</div>`;
  main.querySelectorAll('[data-f]').forEach(b=>b.onclick=()=>{state.fav=b.dataset.f;views.saved()})},
 trips(){const t=store.get('trips',[]);
  main.innerHTML=`<h1>رحلاتي</h1>${t.map(x=>{const d=byId(x.d);return `<div class="panel"><div style="display:flex;justify-content:space-between;align-items:center"><b class="dest">${d.ar} <span class="code" style="font-size:22px">${d.iata}</span></b><button class="icon-btn" data-del="${x.id}" aria-label="حذف الرحلة">🗑️</button></div>
   <div class="two"><div><label for="s${x.id}">تاريخ السفر</label><input type="date" id="s${x.id}" value="${x.start}" data-k="start" data-i="${x.id}"></div>
   <div><label for="n${x.id}">الأيام</label><input type="number" min="1" max="30" id="n${x.id}" value="${x.days}" data-k="days" data-i="${x.id}"></div></div>
   ${Array.from({length:x.days},(_,i)=>`<div class="day"><b>اليوم ${i+1}</b><input aria-label="خطة اليوم ${i+1}" placeholder="مثال: زيارة المعالم…" value="${esc(x.plan[i]||'')}" data-plan="${i}" data-i="${x.id}"></div>`).join('')}
   ${x.start&&!['free','gcc'].includes(d.req.visa)?`<p class="note">🛂 تذكير: ابدأ إجراءات الدخول (${D.visaLabels[d.req.visa].ar}) قبل السفر بوقت كافٍ.</p>`:''}</div>`}).join('')||'<div class="hint">لا رحلات بعد. افتح أي وجهة واضغط «أضف إلى رحلتي».</div>'}`;
  main.oninput=e=>{const el=e.target,i=el.dataset.i;if(!i)return;const all=store.get('trips',[]),x=all.find(y=>y.id==i);if(el.dataset.k)x[el.dataset.k]=el.dataset.k==='days'?Math.min(30,Math.max(1,+el.value||1)):el.value;if(el.dataset.plan!==undefined)x.plan[el.dataset.plan]=el.value;store.set('trips',all)};
  main.onchange=e=>{if(e.target.dataset.k==='days')views.trips()};
  main.onclick=e=>{const b=e.target.closest('[data-del]');if(b){store.set('trips',store.get('trips',[]).filter(x=>x.id!=b.dataset.del));views.trips()}}},
 tools(){const cur=Object.keys(D.ratesPerSAR);
  main.innerHTML=`<h1>الأدوات</h1><div class="panel"><h2 style="margin-top:0">💱 محوّل العملات</h2>
  <label for="amt">المبلغ</label><input id="amt" type="number" inputmode="decimal" value="1000">
  <div class="two"><div><label for="from">من</label><select id="from">${cur.map(c=>`<option ${c==='SAR'?'selected':''}>${c}</option>`).join('')}</select></div><div><label for="to">إلى</label><select id="to">${cur.map(c=>`<option ${c==='TRY'?'selected':''}>${c}</option>`).join('')}</select></div></div>
  <p class="code" id="res" style="font-size:34px;margin:10px 0" aria-live="polite"></p><p class="mu" id="src"></p></div>
  <div class="panel"><h2 style="margin-top:0">🔔 التنبيهات</h2><p class="mu">تنبيهات عند تغيّر شروط الدخول للوجهات المتابَعة.</p>
  <p>الوجهات المتابَعة: ${store.get('alerts',[]).map(i=>byId(i)?.ar).join('، ')||'لا شيء'}</p>
  <label for="em">البريد للتنبيهات</label><input id="em" type="email" placeholder="name@example.com" value="${esc(store.get('email',''))}"><button class="btn" id="pushbtn" style="margin-top:10px">تفعيل التنبيهات</button></div>
  ${apiOn?`<div class="panel" id="acct"><h2 style="margin-top:0">👤 الحساب</h2>${store.get('token')?`<p>أنت مسجّل الدخول بـ <b>${esc(store.get('email','')) }</b>. تُزامَن مفضلتك مع حسابك.</p><button class="btn ghost" id="logout">تسجيل الخروج</button>`:`<p class="mu">سجّل لتحفظ المفضلة وتكتب المراجعات.</p><label for="ae">البريد</label><input id="ae" type="email" autocomplete="email"><label for="ap">كلمة المرور (٨ أحرف على الأقل)</label><input id="ap" type="password" autocomplete="current-password" minlength="8"><div class="actions"><button class="btn" id="login">دخول</button><button class="btn ghost" id="signup">حساب جديد</button></div><p class="mu" id="aerr" role="alert"></p>`}</div>`:''}<div class="panel"><h2 style="margin-top:0">⚙️ الإعدادات</h2><div class="actions"><button class="btn ghost" id="reset">إعادة الإعداد الأولي</button><a class="btn ghost" href="admin.html">لوحة الإدارة</a></div></div>`;
  let rates={...D.ratesPerSAR};
  const calc=()=>{const a=+$('#amt').value||0,f=$('#from').value,t=$('#to').value;$('#res').textContent=(a/rates[f]*rates[t]).toLocaleString('en',{maximumFractionDigits:2})+' '+t};
  ['amt','from','to'].forEach(i=>$('#'+i).oninput=calc);calc();$('#src').textContent='أسعار تقريبية (دون اتصال)';
  fetch('https://open.er-api.com/v6/latest/SAR').then(r=>r.json()).then(j=>{if(j.rates){cur.forEach(c=>{if(j.rates[c])rates[c]=j.rates[c]});store.set('rates',rates);calc();$('#src').textContent='أسعار مباشرة · '+new Date().toLocaleDateString('ar-SA')}}).catch(()=>{const c=store.get('rates');if(c){rates=c;calc();$('#src').textContent='آخر أسعار محفوظة'}});
  $('#pushbtn').onclick=async()=>{store.set('email',$('#em').value);if('Notification'in window){const p=await Notification.requestPermission();toast(p==='granted'?'تم تفعيل التنبيهات':'لم يُمنح الإذن')}else toast('المتصفح لا يدعم التنبيهات')};
  $('#reset').onclick=()=>{store.set('onboarded',false);location.hash='#/welcome'};
  const auth=async path=>{const email=$('#ae').value.trim(),password=$('#ap').value;try{const j=await api(path,{method:'POST',body:{email,password}});store.set('token',j.token);store.set('email',j.user.email);
    const server=(await api('/me/favorites')).items.map(x=>x.id),local=favs(),all=[...new Set([...server,...local])];for(const id of local.filter(x=>!server.includes(x)))await api('/me/favorites/'+id,{method:'PUT'}).catch(()=>{});store.set('favs',all);toast('تم تسجيل الدخول');views.tools()}catch(err){$('#aerr').textContent=err.status===401?'البريد أو كلمة المرور غير صحيحة.':err.status===409?'هذا البريد مسجّل مسبقًا. اضغط «دخول».':err.status===400?'تحقق من البريد وأن كلمة المرور ٨ أحرف على الأقل.':'تعذّر الاتصال. حاول مرة أخرى.'}};
  $('#login')&&($('#login').onclick=()=>auth('/auth/login'));$('#signup')&&($('#signup').onclick=()=>auth('/auth/register'));
  $('#logout')&&($('#logout').onclick=()=>{store.set('token',null);toast('تم تسجيل الخروج');views.tools()})}
};
function route(){document.title='سفرنا — دليل المسافر السعودي';if(apiOn&&Date.now()-lastFetch>60000)refresh();const h=location.hash.replace(/^#\/?/,'').split('/'),r=h[0]||'home';
  document.querySelectorAll('.tabbar a').forEach(a=>a.classList.toggle('on',a.dataset.r===(r==='d'?'explore':r)));
 document.querySelector('.tabbar').hidden=r==='welcome';
 if(r==='welcome')views.onboarding();else if(r==='d')views.detail(h[1]);else (views[r]||views.home)();
 window.scrollTo(0,0);main.focus({preventScroll:true})}
document.addEventListener('click',e=>{const b=e.target.closest('[data-fav]');if(!b)return;e.preventDefault();
 let f=favs();const id=b.dataset.fav;f=f.includes(id)?f.filter(x=>x!==id):[...f,id];store.set('favs',f);if(apiOn&&store.get('token'))api('/me/favorites/'+id,{method:f.includes(id)?'PUT':'DELETE'}).catch(()=>{});toast(f.includes(id)?'أُضيفت إلى المفضلة':'أُزيلت من المفضلة');const y=scrollY;route();scrollTo(0,y)});
$('#langBtn').onclick=()=>{lang=lang==='ar'?'en':'ar';store.set('lang',lang);applyLang();route()};
$('#themeBtn').onclick=()=>theme(document.documentElement.dataset.theme==='dark'?'light':'dark');
const off=()=>$('#offline').hidden=navigator.onLine;addEventListener('online',off);addEventListener('offline',off);off();
theme(store.get('theme',matchMedia('(prefers-color-scheme:dark)').matches?'dark':'light'));applyLang();
addEventListener('hashchange',route);route();
let lastFetch=0;
const refresh=()=>{if(!location.protocol.startsWith('http'))return;lastFetch=Date.now();api('/destinations?limit=300').then(j=>{if(!j?.items?.length)return;const changed=!apiOn||JSON.stringify(j.items)!==JSON.stringify(dests);dests=j.items;apiOn=true;if(changed&&!document.activeElement?.matches('input,textarea,select'))route()}).catch(()=>{})};
refresh();document.addEventListener('visibilitychange',()=>{if(!document.hidden&&Date.now()-lastFetch>30000)refresh()});
if('serviceWorker'in navigator)addEventListener('load',()=>navigator.serviceWorker.register('sw.js').catch(()=>{}));
})();
