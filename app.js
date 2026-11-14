/* دليلك الآمن — منطق التطبيق (يعتمد على data.js: CATS, LV, PRESETS) */
(function(){
'use strict';
const $ = id => document.getElementById(id);
const LS = {
  get(k,d){try{const v=localStorage.getItem(k);return v===null?d:v}catch(e){return d}},
  set(k,v){try{localStorage.setItem(k,v)}catch(e){}}
};
const esc = s => String(s==null?'':s).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const norm = s => String(s||'').toLowerCase().replace(/[ً-ٰٟـ]/g,'').replace(/[أإآ]/g,'ا').replace(/ة/g,'ه').replace(/ى/g,'ي').trim();

let LIB = PRESETS.slice();
try{const ai=JSON.parse(LS.get('ai_cards','[]'));if(Array.isArray(ai))LIB=LIB.concat(ai);}catch(e){}
const state = {cat:'all', q:'', id:'p3'};
const QUICK = ['p3','p1','p2','p5','p20','p21','p25','p15'];
const QUICK_LABEL = {p3:'توقف القلب',p1:'اختناق بغصة',p2:'نزيف شديد',p5:'ألم صدر أو جلطة',p20:'رائحة غاز',p21:'حريق مطبخ',p25:'دخان فحم أو اختناق',p15:'طفل ابتلع سماً'};
const byId = id => LIB.find(p=>p.id===id);
const hay = p => norm([p.title,p.level,p.golden,p.phone,(p.steps||[]).join(' '),(p.avoid||[]).join(' '),(p.gear||[]).join(' ')].join(' '));

function toast(msg,err){
  const t=document.createElement('div');t.className='toast'+(err?' err':'');t.textContent=msg;
  $('toasts').appendChild(t);setTimeout(()=>t.remove(),3200);
}

/* ---------- family ---------- */
const FDEF={loc:'منزل العائلة',tel:'911 / 997',pt:'خارج المبنى الرئيسي',kit:'مدخل المنزل / المطبخ'};
const FMAP={loc:'f_loc',tel:'f_tel',pt:'f_pt',kit:'f_kit'};
function family(){const o={};for(const k in FMAP)o[k]=($(FMAP[k]).value||'').trim()||FDEF[k];return o;}
for(const k in FMAP){const el=$(FMAP[k]);el.value=LS.get('fam_'+k,FDEF[k]);el.addEventListener('input',()=>{LS.set('fam_'+k,el.value);renderTag();});}

/* ---------- render ---------- */
function filtered(){
  const q=norm(state.q);
  return LIB.filter(p=>(state.cat==='all'||p.cat===state.cat)&&(!q||hay(p).includes(q)));
}
function renderTiles(){
  $('tiles').innerHTML=QUICK.map(id=>{const p=byId(id);if(!p)return'';
    return `<button class="tile lv-${p.lv}" data-open="${id}">${esc(QUICK_LABEL[id]||p.title)}<small>اتصل ${esc(p.phone)}</small></button>`}).join('');
}
function renderTabs(){
  const tabs=[['all','الكل']].concat(Object.entries(CATS).map(([k,v])=>[k,v.name]));
  $('tabs').innerHTML=tabs.map(([k,l])=>{
    const n=k==='all'?LIB.length:LIB.filter(p=>p.cat===k).length;
    return `<button class="tab" data-cat="${k}" aria-pressed="${state.cat===k}">${l} <span class="num">(${n})</span></button>`}).join('');
}
function renderList(){
  const list=filtered();
  if(list.length&&!list.some(p=>p.id===state.id))state.id=list[0].id;
  $('count').textContent=list.length+' بطاقة';
  $('qx').hidden=!state.q;
  $('qhint').textContent=(!list.length&&state.q)?'لا توجد بطاقة مطابقة. جرّب كلمة أخرى، أو ولّد بطاقة بالذكاء الاصطناعي من قسم «خطة عائلتك».':'';
  $('list').innerHTML=list.length?list.map(p=>`<button class="item lv-${p.lv}" role="listitem" data-open="${p.id}" ${p.id===state.id?'aria-current="true"':''}>${esc(p.title)}<span class="ph">${esc(p.phone)}</span></button>`).join(''):'<div class="empty">لا توجد نتائج</div>';
  renderTag();
}
function renderTag(){
  const list=filtered(), tag=$('tag');
  if(!list.length){tag.className='tag';tag.innerHTML='<div class="empty" style="margin:20px">اختر بطاقة من القائمة.</div>';return;}
  const p=byId(state.id)||list[0], f=family(), cat=(CATS[p.cat]||{}).name||'';
  tag.className='tag lv-'+(p.lv||'c');
  tag.innerHTML=`
  <div class="tag-head"><span class="hole"></span>
    <div class="meta">${esc(cat)} • ${esc(LV[p.lv]||'')}</div>
    <h2>${esc(p.title)}</h2>
    <p class="lvl">${esc(p.level)}</p>
    <a class="callbox" href="tel:${esc(p.phone)}"><small>اتصل الآن</small><b>${esc(p.phone)}</b></a>
  </div>
  <div class="tag-body">
    <div class="golden"><h3>القاعدة الأولى</h3><p>${esc(p.golden)}</p></div>
    <div><h3 class="sec">الخطوات</h3><ol class="steps">${p.steps.map(s=>`<li><span>${esc(s)}</span></li>`).join('')}</ol></div>
    <div class="two">
      <div class="avoid"><h3>تجنب فوراً</h3><ul>${p.avoid.map(a=>`<li>${esc(a)}</li>`).join('')}</ul></div>
      <div class="gear"><h3>التجهيزات وعلامات الخطر</h3>${p.gear.map(g=>`<label><input type="checkbox"><span>${esc(g)}</span></label>`).join('')}</div>
    </div>
  </div>
  <div class="tag-foot">
    <div><em>الموقع:</em> <b>${esc(f.loc)}</b></div>
    <div><em>طوارئ العائلة:</em> <b>${esc(f.tel)}</b></div>
    <div><em>نقطة التجمع:</em> <b>${esc(f.pt)}</b></div>
    <div><em>الإسعافات والطفاية:</em> <b>${esc(f.kit)}</b></div>
  </div>`;
  $('bWA').href='https://wa.me/?text='+encodeURIComponent(buildText());
  try{history.replaceState(null,'','?card='+p.id)}catch(e){}
}
function renderAll(){renderTiles();renderTabs();renderList();}

function openCard(id,scroll){
  state.id=id;
  if(state.cat!=='all'&&byId(id)&&byId(id).cat!==state.cat)state.cat='all';
  state.q='';$('q').value='';
  renderTabs();renderList();
  if(scroll)$('viewer').scrollIntoView({behavior:'smooth',block:'start'});
}

/* ---------- events ---------- */
document.addEventListener('click',e=>{
  const o=e.target.closest('[data-open]');if(o){openCard(o.dataset.open,true);return;}
  const t=e.target.closest('[data-cat]');if(t){state.cat=t.dataset.cat;renderTabs();renderList();}
});
$('q').addEventListener('input',e=>{state.q=e.target.value;renderList();if(state.q)$('library').scrollIntoView({behavior:'smooth',block:'start'});});
$('qx').onclick=()=>{state.q='';$('q').value='';renderList();$('q').focus();};

/* ---------- export ---------- */
function buildText(){
  const p=byId(state.id)||LIB[0], f=family();
  return ['🚨 *'+p.title+'*','📞 رقم الطوارئ: '+p.phone+'  |  '+p.level,'','⭐ *القاعدة الأولى:*',p.golden,'',
    '⚡ *الخطوات:*',...p.steps.map((s,i)=>(i+1)+'. '+s),'','⛔ *تجنب فوراً:*',...p.avoid.map(a=>'• '+a),'',
    '🧰 *التجهيزات وعلامات الخطر:*',...p.gear.map(g=>'• '+g),'','🏠 *بيانات العائلة*',
    '📍 الموقع: '+f.loc,'📞 طوارئ العائلة: '+f.tel,'🏃 نقطة التجمع: '+f.pt,'🩺 الإسعافات/الطفاية: '+f.kit,'','— دليلك الآمن'].join('\n');
}
if(navigator.share){$('bShare').hidden=false;$('bShare').onclick=()=>navigator.share({title:(byId(state.id)||{}).title,text:buildText()}).catch(()=>{});}
$('bCopy').onclick=async()=>{
  const t=buildText();
  try{await navigator.clipboard.writeText(t);toast('تم نسخ النص');}
  catch(e){const ta=document.createElement('textarea');ta.value=t;document.body.appendChild(ta);ta.select();
    try{document.execCommand('copy');toast('تم نسخ النص');}catch(_){toast('تعذر النسخ',true);}ta.remove();}
};
let h2cLoading=null;
function loadH2C(){
  if(window.html2canvas)return Promise.resolve();
  if(!h2cLoading)h2cLoading=new Promise((res,rej)=>{const s=document.createElement('script');
    s.src='https://cdnjs.cloudflare.com/ajax/libs/html2canvas/1.4.1/html2canvas.min.js';s.onload=res;s.onerror=()=>rej(new Error('offline'));document.head.appendChild(s);});
  return h2cLoading;
}
$('bPng').onclick=async()=>{
  try{
    toast('جارٍ إنشاء الصورة…');await loadH2C();
    const c=await html2canvas($('tag'),{scale:2,useCORS:true,backgroundColor:getComputedStyle(document.body).backgroundColor});
    const a=document.createElement('a');a.download=(byId(state.id).title).replace(/[\\/:*?"<>|()]/g,'').trim()+'.png';
    a.href=c.toDataURL('image/png');a.click();toast('تم حفظ الصورة');
  }catch(e){toast('حفظ الصورة يحتاج اتصالاً بالإنترنت مرة واحدة',true);h2cLoading=null;}
};
$('bPrint').onclick=()=>window.print();

/* ---------- stopwatch ---------- */
let tAcc=0,tStart=0,tInt=null,tRun=false;
const pad=n=>String(n).padStart(2,'0');
function tShow(){const s=Math.floor((tAcc+(tRun?Date.now()-tStart:0))/1000);
  $('clock').textContent=pad(Math.floor(s/3600))+':'+pad(Math.floor(s%3600/60))+':'+pad(s%60);}
$('tGo').onclick=()=>{
  if(tRun)return;const first=tAcc===0;tRun=true;tStart=Date.now();tInt=setInterval(tShow,250);$('tGo').textContent='استئناف';
  if(first){$('since').textContent='وقت البدء: '+new Date().toLocaleTimeString('ar-SA',{hour:'2-digit',minute:'2-digit',second:'2-digit'});}
};
$('tPause').onclick=()=>{if(!tRun)return;tAcc+=Date.now()-tStart;tRun=false;clearInterval(tInt);tShow();};
$('tReset').onclick=()=>{tRun=false;clearInterval(tInt);tAcc=0;$('tGo').textContent='بدء';$('since').textContent='';tShow();};

/* ---------- CPR metronome (110 BPM) ---------- */
let mInt=null,actx=null;
function beep(){try{
  if(!actx)actx=new (window.AudioContext||window.webkitAudioContext)();
  const o=actx.createOscillator(),g=actx.createGain();o.type='square';o.frequency.value=880;
  g.gain.setValueAtTime(.35,actx.currentTime);g.gain.exponentialRampToValueAtTime(.001,actx.currentTime+.09);
  o.connect(g);g.connect(actx.destination);o.start();o.stop(actx.currentTime+.1);}catch(e){}}
function tick(){beep();const r=$('ring');r.classList.add('on');setTimeout(()=>r.classList.remove('on'),100);}
$('mBtn').onclick=()=>{
  if(mInt){clearInterval(mInt);mInt=null;$('mBtn').textContent='تشغيل الإيقاع';return;}
  if(actx&&actx.state==='suspended')actx.resume();
  tick();mInt=setInterval(tick,60000/110);$('mBtn').textContent='إيقاف الإيقاع';
};

/* ---------- Gemini ---------- */
const getKey=()=>LS.get('gemini_key','');
$('keyBtn').onclick=()=>{$('keyInput').value=getKey();$('keyDlg').showModal();};
$('keyDlg').addEventListener('close',()=>{if($('keyDlg').returnValue==='save'){LS.set('gemini_key',$('keyInput').value.trim());toast('تم حفظ المفتاح');}});
$('aiGo').onclick=async()=>{
  const sc=$('aiText').value.trim();
  if(!sc){toast('اكتب وصف الحالة أولاً',true);return;}
  if(!getKey()){toast('أدخل مفتاح Gemini أولاً',true);$('keyBtn').click();return;}
  const b=$('aiGo');b.disabled=true;b.textContent='جارٍ التوليد…';
  const prompt=`أنت خبير سلامة وإسعاف في المملكة العربية السعودية. حوّل السيناريو التالي إلى بطاقة إرشادية بالعربية الفصحى المبسطة مبنية على إرشادات إسعاف معتمدة، وأعد JSON فقط بهذا الهيكل:
{"cat":"first_aid|kids_elderly|home_safety|vehicle_road|weather_outdoor","title":"","phone":"911|997|998|937|996","level":"وصف قصير لمستوى الخطورة","lv":"c|d|w|t|p","golden":"القاعدة الأولى","steps":["5 خطوات مرتبة"],"avoid":["2-3 أخطاء"],"gear":["2-3 تجهيزات أو علامات خطر"]}
lv: c حرج، d حرج جداً، w تحذير، t إجراء تقني، p وقائي.
السيناريو: ${sc}`;
  try{
    const r=await fetch('https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent',{
      method:'POST',headers:{'Content-Type':'application/json','x-goog-api-key':getKey()},
      body:JSON.stringify({contents:[{parts:[{text:prompt}]}],generationConfig:{responseMimeType:'application/json'}})});
    if(!r.ok)throw new Error('HTTP '+r.status);
    let o=JSON.parse((await r.json()).candidates[0].content.parts[0].text);if(Array.isArray(o))o=o[0];
    if(!o.title||!Array.isArray(o.steps)||!o.steps.length)throw new Error('بنية غير صالحة');
    const card={id:'ai'+Date.now(),cat:CATS[o.cat]?o.cat:'first_aid',title:String(o.title),
      phone:['911','997','998','937','996'].includes(String(o.phone))?String(o.phone):'911',
      level:String(o.level||'بطاقة مولّدة بالذكاء الاصطناعي'),lv:LV[o.lv]?o.lv:'c',golden:String(o.golden||''),
      steps:o.steps.map(String),avoid:(o.avoid||[]).map(String),gear:(o.gear||[]).map(String)};
    LIB.push(card);let sv=[];try{sv=JSON.parse(LS.get('ai_cards','[]'));}catch(e){}
    sv.push(card);LS.set('ai_cards',JSON.stringify(sv));
    state.cat='all';renderAll();openCard(card.id,true);toast('أُضيفت البطاقة. راجعها قبل الاعتماد عليها');
  }catch(e){toast('تعذر التوليد: '+e.message,true);}
  finally{b.disabled=false;b.textContent='توليد البطاقة';}
};

/* ---------- PWA: status, install, service worker ---------- */
let swReady=false;
function net(){
  const el=$('net');
  if(!navigator.onLine){el.className='pill off';el.textContent='بدون إنترنت';}
  else if(swReady){el.className='pill ok';el.textContent='جاهز للعمل بدون إنترنت';}
  else{el.className='pill';el.textContent='متصل';}
}
window.addEventListener('online',net);window.addEventListener('offline',net);
let deferred=null;
window.addEventListener('beforeinstallprompt',e=>{e.preventDefault();deferred=e;$('install').hidden=false;});
$('install').onclick=async()=>{if(!deferred)return;deferred.prompt();await deferred.userChoice;deferred=null;$('install').hidden=true;};
window.addEventListener('appinstalled',()=>{$('install').hidden=true;toast('تم تثبيت التطبيق');});
if('serviceWorker' in navigator && /^https?:$/.test(location.protocol)){
  navigator.serviceWorker.register('sw.js').then(()=>navigator.serviceWorker.ready).then(()=>{swReady=true;net();}).catch(()=>{});
}

/* ---------- start ---------- */
const want=new URLSearchParams(location.search).get('card');
if(want&&byId(want))state.id=want;
renderAll();net();tShow();
})();
