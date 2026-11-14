/* دليلك الآمن — تطبيق PWA تفاعلي (يعتمد على data.js: CATS, LV, PRESETS) */
(function(){
'use strict';
const $ = id => document.getElementById(id);
const LS = {
  get(k,d){try{const v=localStorage.getItem(k);return v===null?d:v}catch(e){return d}},
  set(k,v){try{localStorage.setItem(k,v)}catch(e){}}
};
const esc = s => String(s==null?'':s).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const norm = s => String(s||'').toLowerCase().replace(/[ً-ٰٟـ]/g,'').replace(/[أإآ]/g,'ا').replace(/ة/g,'ه').replace(/ى/g,'ي').trim();
const pad = n => String(n).padStart(2,'0');

let LIB = PRESETS.slice();
try{const ai=JSON.parse(LS.get('ai_cards','[]'));if(Array.isArray(ai))LIB=LIB.concat(ai);}catch(e){}
const byId = id => LIB.find(p=>p.id===id);

/* ================= icons ================= */
const svgU = d => `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">${d}</svg>`;
const U = {
  home:svgU('<path d="M3 11l9-8 9 8"/><path d="M5 10v10h5v-6h4v6h5V10"/>'),
  grid:svgU('<rect x="3" y="3" width="7" height="7" rx="2"/><rect x="14" y="3" width="7" height="7" rx="2"/><rect x="3" y="14" width="7" height="7" rx="2"/><rect x="14" y="14" width="7" height="7" rx="2"/>'),
  pulse:svgU('<path d="M2 12h5l2-7 4 14 2-7h7"/>'),
  user:svgU('<circle cx="12" cy="8" r="4"/><path d="M4 21c0-4 3.500-7 8-7s8 3 8 7"/>'),
  book:svgU('<path d="M4 4h6a3 3 0 0 1 2 1 3 3 0 0 1 2-1h6v15h-6a3 3 0 0 0-2 1 3 3 0 0 0-2-1H4z"/><path d="M12 5v15"/>'),
  search:svgU('<circle cx="11" cy="11" r="7"/><path d="M20 20l-4-4"/>'),
  mic:svgU('<rect x="9" y="3" width="6" height="11" rx="3"/><path d="M5 11a7 7 0 0 0 14 0M12 18v3"/>'),
  back:svgU('<path d="M9 5l7 7-7 7"/>'),
  next:svgU('<path d="M15 5l-7 7 7 7"/>'),
  prev:svgU('<path d="M9 5l7 7-7 7"/>'),
  spk:svgU('<path d="M4 9v6h4l5 4V5L8 9z"/><path d="M16 9a4 4 0 0 1 0 6M18.500 6.500a8 8 0 0 1 0 11"/>'),
  stop:svgU('<rect x="6" y="6" width="12" height="12" rx="2"/>'),
  moon:svgU('<path d="M20 14A8 8 0 1 1 10 4a6 6 0 0 0 10 10z"/>')
};
const G = { // glyphs for pictograms (filled)
  heart:'<path d="M12 21s-7.500-4.600-9.600-9.200C.9 8.400 3 5 6.400 5c2 0 3.600 1.100 4.600 2.700h2C14 6.100 15.600 5 17.600 5 21 5 23.100 8.400 21.600 11.800 19.500 16.400 12 21 12 21z"/>',
  drop:'<path d="M12 2.500s6.500 7 6.500 12a6.500 6.500 0 0 1-13 0c0-5 6.500-12 6.500-12z"/>',
  flame:'<path d="M12 2c.8 3.500 5.500 5.500 5.500 11A5.500 5.500 0 0 1 12 19a5.500 5.500 0 0 1-5.500-5.500c0-2.200 1-3.600 2.200-4.800.2 1.600.9 2.400 1.800 2.800C10.200 8.500 11 5 12 2zM12 22a3 3 0 0 1-3-3h6a3 3 0 0 1-3 3z"/>',
  bolt:'<path d="M13 2 4 14h6l-1 8 9-12h-6z"/>',
  wave:'<path d="M2 8c2.500 0 2.500-2 5-2s2.500 2 5 2 2.500-2 5-2 2.500 2 5 2v3c-2.500 0-2.500-2-5-2s-2.500 2-5 2-2.500-2-5-2-2.500 2-5 2zM2 15c2.500 0 2.500-2 5-2s2.500 2 5 2 2.500-2 5-2 2.500 2 5 2v3c-2.500 0-2.500-2-5-2s-2.500 2-5 2-2.500-2-5-2-2.500 2-5 2z"/>',
  sun:'<circle cx="12" cy="12" r="4.500"/><path d="M11 1h2v4h-2zM11 19h2v4h-2zM1 11h4v2H1zM19 11h4v2h-4zM4.200 5.600l1.400-1.400 2.800 2.800-1.400 1.400zM15.600 17l1.400-1.400 2.800 2.800-1.400 1.400zM17 8.400l2.800-2.800 1.400 1.400-2.800 2.800zM3.800 18.400l2.800-2.800 1.400 1.400-2.800 2.800z"/>',
  gas:'<path d="M9 2h6v2h-1v2.500c2.500.8 4 3 4 5.500v8a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2v-8c0-2.500 1.500-4.700 4-5.500V4H9z"/>',
  car:'<path d="M5 11l1.600-4.500A2 2 0 0 1 8.500 5h7a2 2 0 0 1 1.900 1.500L19 11a2 2 0 0 1 2 2v4h-2v1.500a1.500 1.500 0 0 1-3 0V17H8v1.500a1.500 1.500 0 0 1-3 0V17H3v-4a2 2 0 0 1 2-2zm3-4-1.200 4h10.400L16 7z" fill-rule="evenodd"/>',
  child:'<circle cx="12" cy="6" r="3.500"/><path d="M5 21c0-4.500 3-8 7-8s7 3.500 7 8z"/>',
  warn:'<path d="M12 2 1 21h22zm-1 7h2v6h-2zm0 8h2v2h-2z" fill-rule="evenodd"/>',
  eye:'<path d="M12 5C6 5 2 12 2 12s4 7 10 7 10-7 10-7-4-7-10-7zm0 11a4 4 0 1 1 0-8 4 4 0 0 1 0 8z" fill-rule="evenodd"/><circle cx="12" cy="12" r="2"/>',
  bone:'<path d="M17 3.500a3 3 0 0 1 3 3c0 .8-.3 1.500-.9 2.100l-9.200 9.200c-.6.600-1.300.9-2.100.9a3 3 0 0 1-3-3c0-.8.3-1.500.9-2.100l9.200-9.200c.6-.6 1.300-.9 2.100-.9z"/>',
  cross:'<path d="M9 3h6v6h6v6h-6v6H9v-6H3V9h6z"/>',
  wind:'<path d="M3 8h11a3 3 0 1 0-3-3h2a1 1 0 1 1 1 1H3zM3 13h15a3 3 0 1 1-3 3h2a1 1 0 1 0 1-1H3zM3 18h8v2H3z"/>',
  pill:'<path d="M8 3a5 5 0 0 0-3.500 8.500l8 8A5 5 0 0 0 19.500 12.500l-8-8A5 5 0 0 0 8 3zm0 2a3 3 0 0 1 2.100.9l2.100 2.100-5.200 5.200L4.900 11A3 3 0 0 1 8 5z" fill-rule="evenodd"/>',
  home:'<path d="M12 3 2 12h3v8h5v-5h4v5h5v-8h3z"/>',
  lift:'<path d="M5 3h14a1 1 0 0 1 1 1v16a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1zm3 7 4-4 4 4zm0 4h8l-4 4z" fill-rule="evenodd"/>',
  siren:'<path d="M12 3a6 6 0 0 1 6 6v6H6V9a6 6 0 0 1 6-6zM4 17h16v3H4zM11 0h2v2h-2z"/>'
};
function kindOf(p){
  const t=p.title;
  if(/غاز/.test(t))return'gas';
  if(/صعق|كهرب/.test(t))return'bolt';
  if(/مصعد/.test(t))return'lift';
  if(/حريق|اشتعال|حروق|حرائق/.test(t))return'flame';
  if(/غرق|سيول|مياه/.test(t))return'wave';
  if(/شمس|حرار/.test(t))return'sun';
  if(/لدغ/.test(t))return'warn';
  if(/عين/.test(t))return'eye';
  if(/كسور|عظام|عمود/.test(t))return'bone';
  if(/سكر|دواء|أدوية/.test(t)&&!/نوبة قلبية/.test(t))return'pill';
  if(/ربو|غبار|غصة|اختناق|الفحم/.test(t))return'wind';
  if(/نزيف|جروح|بتر|رعاف/.test(t))return'drop';
  if(/إنعاش|قلب|جلطة|إغماء|حساسية/.test(t))return'heart';
  if(/طفل|رضيع|أطفال|كبار|سقوط/.test(t))return'child';
  if(/مركبة|إطار|حوادث|المفتاح|بطارية|الخط السريع|المرور|التغريز|الصحراء/.test(t))return'car';
  if(/إخلاء|منزل|التسمم الغذائي/.test(t))return'home';
  return'cross';
}
const pic = p => `<span class="pic lv-${p.lv||'c'}"><svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">${G[kindOf(p)]}</svg></span>`;
const catIcon = g => `<svg class="ic" viewBox="0 0 24 24" fill="#fff" opacity=".95" aria-hidden="true">${G[g]}</svg>`;

const CATUI = {
  first_aid:{g:'g-red',ic:'heart',t:'حالات حرجة',d:'مثل نزيف، غصة، توقف قلب'},
  kids_elderly:{g:'g-orange',ic:'child',t:'الأطفال وكبار السن',d:'غصة رضيع، تسمم، سقوط'},
  home_safety:{g:'g-green',ic:'home',t:'سلامة المنزل',d:'غاز، حريق، كهرباء، مصعد'},
  vehicle_road:{g:'g-blue',ic:'car',t:'المركبة والطريق',d:'تعطل، إطار، حوادث، حريق'},
  weather_outdoor:{g:'g-teal',ic:'sun',t:'الطقس والبر',d:'ضربة شمس، لدغات، سيول'}
};
const QUICK = ['p3','p1','p2','p5','p20','p21','p25','p15'];
const QLABEL = {p3:'توقف القلب',p1:'اختناق بغصة',p2:'نزيف شديد',p5:'ألم صدر أو جلطة',p20:'رائحة غاز',p21:'حريق مطبخ',p25:'دخان فحم',p15:'طفل ابتلع سماً'};

/* ================= toast / speech / helpers ================= */
function toast(msg,err){const t=document.createElement('div');t.className='toast'+(err?' err':'');t.textContent=msg;$('toasts').appendChild(t);setTimeout(()=>t.remove(),3200);}
const FDEF={loc:'منزل العائلة',tel:'911 / 997',pt:'خارج المبنى الرئيسي',kit:'مدخل المنزل / المطبخ'};
function family(){const o={};for(const k in FDEF)o[k]=(LS.get('fam_'+k,'')||'').trim()||FDEF[k];return o;}

let speaking=false;
function stopSpeak(){try{speechSynthesis.cancel()}catch(e){}speaking=false;document.querySelectorAll('.wave').forEach(w=>w.classList.remove('on'));const b=document.querySelector('[data-act=speak]');if(b)b.innerHTML=U.spk+' تشغيل الصوت';}
function speak(text){
  if(!('speechSynthesis' in window)){toast('جهازك لا يدعم القراءة الصوتية',true);return;}
  try{
    speechSynthesis.cancel();
    const u=new SpeechSynthesisUtterance(text);u.lang='ar-SA';u.rate=.95;
    const v=(speechSynthesis.getVoices()||[]).find(v=>/^ar/i.test(v.lang));
    if(v)u.voice=v;else if(speechSynthesis.getVoices().length)toast('لم يُعثر على صوت عربي على جهازك',true);
    u.onstart=()=>{speaking=true;document.querySelectorAll('.wave').forEach(w=>w.classList.add('on'));const b=document.querySelector('[data-act=speak]');if(b)b.innerHTML=U.stop+' إيقاف الصوت';};
    u.onend=u.onerror=()=>{speaking=false;document.querySelectorAll('.wave').forEach(w=>w.classList.remove('on'));const b=document.querySelector('[data-act=speak]');if(b)b.innerHTML=U.spk+' تشغيل الصوت';};
    speechSynthesis.speak(u);
  }catch(e){toast('تعذر تشغيل الصوت',true);}
}

/* ================= state ================= */
const S={q:'',cat:'all',focus:false,scrollTo:null,swReady:false,deferred:null};
const GD={id:null,idx:0,mode:'steps',talk:false};
const hasVoiceIn = !!(window.SpeechRecognition||window.webkitSpeechRecognition);
const recent=()=>{try{return JSON.parse(LS.get('recent','[]'))}catch(e){return[]}};
function pushRecent(id){const r=recent().filter(x=>x!==id);r.unshift(id);LS.set('recent',JSON.stringify(r.slice(0,4)));}

/* ================= views ================= */
const NAV=[['home','الرئيسية',U.home],['lib','البطاقات',U.grid],['cpr','إنعاش',U.pulse],['me','خطتي',U.user],['refs','المراجع',U.book]];
function renderNav(cur){
  $('nav').innerHTML=NAV.map(([k,l,i])=>`<button data-go="#/${k}" class="${k==='cpr'?'mid':''}" ${k===cur?'aria-current="page"':''} aria-label="${l}">${i}${k==='cpr'?'':'<span>'+l+'</span>'}</button>`).join('');
}
const netPill = () => `<span class="pill" data-net></span>`;
function updateNet(){
  document.querySelectorAll('[data-net]').forEach(el=>{
    const off=!navigator.onLine;el.className='pill'+(off?' off':'');
    el.innerHTML='<i></i>'+(off?'تعمل بدون إنترنت':(S.swReady?'متاح بلا إنترنت':'متصل'));
  });
}
const searchBox = ph => `<div class="search"><span class="mag">${U.search}</span><label class="sr" for="q">بحث</label><input id="q" type="search" autocomplete="off" placeholder="${ph}" value="${esc(S.q)}">${hasVoiceIn?`<button class="mic" data-act="voice" aria-label="بحث صوتي">${U.mic}</button>`:''}</div>`;

function viewHome(){
  $('hdr').className='hdr';
  $('hdr').innerHTML=`<div class="hrow"><h1>دليلك الآمن</h1>${netPill()}<button class="ib" data-act="theme" aria-label="تبديل المظهر">${U.moon}</button></div>${searchBox('ابحث عن حالة أو إصابة…')}`;
  const rc=recent().map(byId).filter(Boolean);
  const cats=Object.keys(CATUI).map(k=>{const n=LIB.filter(p=>p.cat===k).length,c=CATUI[k];
    return `<button class="cat ${c.g}" data-go="#/lib/${k}">${catIcon(c.ic)}<div class="t"><h3>${c.t}</h3><p>${c.d}</p><span class="badge num">${n} بطاقة</span></div></button>`}).join('');
  $('main').innerHTML=`
  <div class="sos-wrap"><button class="sos" data-act="sos" aria-label="اتصال بالطوارئ"><small>اتصال</small>بالطوارئ<b>997</b></button></div>
  <h2 class="sec">ماذا يحدث الآن؟</h2>
  <div class="quick">${QUICK.map(id=>{const p=byId(id);return p?`<button class="qk lv-${p.lv}" data-go="#/card/${id}">${pic(p)}<span>${esc(QLABEL[id]||p.title)}</span></button>`:''}).join('')}</div>
  ${rc.length?`<h2 class="sec">شاهدتها مؤخراً</h2><div class="rows">${rc.map(rowHTML).join('')}</div>`:''}
  <h2 class="sec">الوصول السريع</h2><div class="cats">${cats}</div>
  <h2 class="sec">لماذا هذا مهم؟</h2>
  <p class="fact">عند توقف القلب تنخفض فرص النجاة بنحو 7 إلى 10% مع كل دقيقة تمر دون إنعاش أو صدمة كهربائية. <a href="#/refs" data-ref="r1">[1]</a></p>
  <p class="fact">النزيف الشديد قد يُنهي الحياة خلال دقائق، وإيقافه يبدأ بيد أول شخص موجود. <a href="#/refs" data-ref="r4">[4]</a></p>
  <p class="fact">أول أكسيد الكربون بلا لون ولا رائحة، ويصيب الجالسين حول الفحم قبل أن يشعروا به. <a href="#/refs" data-ref="r7">[7]</a></p>
  <p class="sub" style="margin-top:8px">البطاقات للتوعية ولا تغني عن الاتصال بالإسعاف أو دورة إسعافات معتمدة.</p>`;
  const q=$('q');q.addEventListener('focus',()=>{S.focus=true;location.hash='#/lib';});
}
function rowHTML(p){return `<button class="row lv-${p.lv}" data-go="#/card/${p.id}">${pic(p)}<span class="tx">${esc(p.title)}<small>${esc(p.level)}</small></span><span class="ph num">${esc(p.phone)}</span></button>`;}

function filtered(){const q=norm(S.q);return LIB.filter(p=>(S.cat==='all'||p.cat===S.cat)&&(!q||norm([p.title,p.level,p.golden,(p.steps||[]).join(' '),(p.avoid||[]).join(' '),(p.gear||[]).join(' ')].join(' ')).includes(q)));}
function listHTML(){
  const l=filtered();
  return l.length?`<div class="rows">${l.map(rowHTML).join('')}</div>`:`<div class="empty">لا توجد بطاقة مطابقة لـ «${esc(S.q)}». جرّب كلمة أخرى، أو ولّد بطاقة من صفحة «خطتي».</div>`;
}
function viewLib(cat){
  if(cat!==undefined)S.cat=cat||'all';
  $('hdr').className='hdr';
  $('hdr').innerHTML=`<div class="hrow"><button class="ib" data-go="#/home" aria-label="رجوع">${U.back}</button><h2>${S.cat==='all'?'كل البطاقات':CATUI[S.cat]?CATUI[S.cat].t:''}</h2>${netPill()}</div>${searchBox('ابحث: غصة، لدغة، حريق…')}`;
  const chips=[['all','الكل ('+LIB.length+')']].concat(Object.keys(CATUI).map(k=>[k,CATUI[k].t+' ('+LIB.filter(p=>p.cat===k).length+')']));
  $('main').innerHTML=`<div class="chips" id="chips">${chips.map(([k,l])=>`<button class="chip" data-chip="${k}" aria-pressed="${S.cat===k}">${l}</button>`).join('')}</div><p class="sub" id="cnt" style="margin:4px 2px 10px"></p><div id="lst"></div>`;
  const fill=()=>{$('lst').innerHTML=listHTML();$('cnt').textContent=filtered().length+' بطاقة';document.querySelectorAll('.chip').forEach(c=>c.setAttribute('aria-pressed',c.dataset.chip===S.cat));};
  fill();
  $('q').addEventListener('input',e=>{S.q=e.target.value;fill();});
  $('chips').addEventListener('click',e=>{const c=e.target.closest('[data-chip]');if(c){S.cat=c.dataset.chip;fill();}});
  if(S.focus){S.focus=false;$('q').focus();}
}

/* ---- guide ---- */
function guideText(p,i){return i===0?'القاعدة الأولى. '+p.golden:'الخطوة '+i+'. '+p.steps[i-1];}
function viewCard(id){
  const p=byId(id);if(!p){location.hash='#/lib';return;}
  if(GD.id!==id){GD.id=id;GD.idx=0;GD.mode='steps';GD.talk=false;stopSpeak();pushRecent(id);}
  $('hdr').className='hdr slim';
  $('hdr').innerHTML=`<div class="hrow"><button class="ib" data-act="back" aria-label="رجوع">${U.back}</button><h2>${esc(p.title.replace(/\s*\(.*\)\s*$/,''))}</h2><button class="ib" data-act="sos" aria-label="اتصال طوارئ" style="background:var(--red)"><b class="num" style="font-size:15px">${esc(p.phone)}</b></button></div>`;
  drawGuide();
}
function drawGuide(){
  const p=byId(GD.id),n=p.steps.length,i=GD.idx,main=$('main');
  const modes=`<div class="modes" role="tablist"><button role="tab" data-act="mode" data-m="steps" aria-selected="${GD.mode==='steps'}">خطوة بخطوة</button><button role="tab" data-act="mode" data-m="full" aria-selected="${GD.mode==='full'}">البطاقة كاملة</button></div>`;
  let body='';
  if(GD.mode==='full'){body=fullCard(p);}
  else if(i<=n){
    const bars=Array.from({length:n+1},(_,k)=>`<i class="${k<=i?'on':''}"></i>`).join('');
    const extra=(p.id==='p3'?`<a class="btn ghost sm" href="#/cpr" data-go="#/cpr">${U.pulse} افتح إيقاع الإنعاش</a>`:'')+(p.id==='p2'?`<a class="btn ghost sm" href="#/cpr" data-go="#/cpr">ابدأ مؤقت العاصبة</a>`:'');
    body=`<div class="stage lv-${p.lv}" id="stage">
      <div class="art">${pic(p)}<div class="wave">${'<i></i>'.repeat(26)}</div><div class="cap">${esc(p.level)}</div></div>
      <div class="stepno">${i===0?'قبل أن تبدأ':'الخطوة '+i+' من '+n}</div>
      <div class="steptx ${i===0?'gold':''}" id="stx">${esc(i===0?p.golden:p.steps[i-1])}</div>
      <div class="bar" aria-hidden="true">${bars}</div>
      <div class="ctl"><button class="btn ghost" data-act="prev" aria-label="السابق" ${i===0?'disabled':''}>${U.prev}</button><button class="btn blue" data-act="speak">${speaking?U.stop+' إيقاف الصوت':U.spk+' تشغيل الصوت'}</button><button class="btn green" data-act="next">${i===n?'إنهاء':'التالي'} ${U.next}</button></div>
      ${extra?`<div class="act">${extra}</div>`:''}
    </div>
    <a class="btn red" href="tel:${esc(p.phone)}" style="margin-top:12px;width:100%">اتصل الآن ${esc(p.phone)}</a>`;
  } else {
    body=`<div class="stage lv-${p.lv} done"><div class="art">${pic(p)}</div>
      <h3>أحسنت. هل اتصلت بـ ${esc(p.phone)}؟</h3><p class="sub">تابع حالة المصاب حتى وصول المسعفين.</p>
      <div class="avoid"><h3>تجنب فوراً</h3><ul>${p.avoid.map(a=>`<li>${esc(a)}</li>`).join('')}</ul></div>
      <div class="act"><a class="btn red" href="tel:${esc(p.phone)}">اتصل بـ ${esc(p.phone)}</a><button class="btn ghost" data-act="restart">إعادة الخطوات</button><button class="btn ghost" data-act="mode" data-m="full">عرض البطاقة كاملة</button></div></div>`;
  }
  main.innerHTML=modes+body;
  main.scrollTop=0;
  const st=$('stage');if(st){let x0=null;st.addEventListener('touchstart',e=>{x0=e.touches[0].clientX},{passive:true});
    st.addEventListener('touchend',e=>{if(x0===null)return;const dx=e.changedTouches[0].clientX-x0;x0=null;if(dx<-55)go(1);else if(dx>55)go(-1);});}
}
function go(d){
  const p=byId(GD.id),n=p.steps.length;
  const ni=Math.max(0,Math.min(n+1,GD.idx+d));if(ni===GD.idx)return;
  GD.idx=ni;drawGuide();
  if(GD.talk&&ni>=0&&ni<=n)speak(guideText(p,ni));else if(ni>n)stopSpeak();
}
function buildText(p){
  p=p||byId(GD.id)||LIB[0];const f=family();
  return ['🚨 *'+p.title+'*','📞 رقم الطوارئ: '+p.phone+'  |  '+p.level,'','⭐ *القاعدة الأولى:*',p.golden,'','⚡ *الخطوات:*',...p.steps.map((s,i)=>(i+1)+'. '+s),'',
   '⛔ *تجنب فوراً:*',...p.avoid.map(a=>'• '+a),'','🧰 *التجهيزات وعلامات الخطر:*',...p.gear.map(g=>'• '+g),'','🏠 *بيانات العائلة*',
   '📍 الموقع: '+f.loc,'📞 طوارئ العائلة: '+f.tel,'🏃 نقطة التجمع: '+f.pt,'🩺 الإسعافات/الطفاية: '+f.kit,'','— دليلك الآمن'].join('\n');
}
function fullCard(p){
  const f=family(),cat=(CATS[p.cat]||{}).name||'';
  return `<div class="xr"><a class="btn green sm" target="_blank" rel="noopener" href="https://wa.me/?text=${encodeURIComponent(buildText(p))}">واتساب</a><button class="btn ghost sm" data-act="copy">نسخ النص</button><button class="btn ghost sm" data-act="png">حفظ كصورة</button><button class="btn ghost sm" data-act="print">طباعة A4</button>${navigator.share?'<button class="btn ghost sm" data-act="share" style="grid-column:1/-1">مشاركة…</button>':''}</div>
  <div class="tagwrap" id="tagwrap"><article class="tag lv-${p.lv}" id="tag">
    <div class="th"><span class="hole"></span><small>${esc(cat)} • ${esc(LV[p.lv]||'')}</small><h2>${esc(p.title)}</h2><p>${esc(p.level)}</p><a class="call" href="tel:${esc(p.phone)}"><small>اتصل الآن</small><b>${esc(p.phone)}</b></a></div>
    <div class="tb"><div class="gold"><h3>القاعدة الأولى</h3>${esc(p.golden)}</div>
      <div><h3 class="h">الخطوات</h3><ol class="stp">${p.steps.map(s=>`<li><span>${esc(s)}</span></li>`).join('')}</ol></div>
      <div class="avoid" style="margin:0"><h3>تجنب فوراً</h3><ul>${p.avoid.map(a=>`<li>${esc(a)}</li>`).join('')}</ul></div>
      <div class="gear"><h3>التجهيزات وعلامات الخطر</h3>${p.gear.map(g=>`<label><input type="checkbox"><span>${esc(g)}</span></label>`).join('')}</div></div>
    <div class="tf"><div><em>الموقع:</em> <b>${esc(f.loc)}</b></div><div><em>طوارئ العائلة:</em> <b>${esc(f.tel)}</b></div><div><em>نقطة التجمع:</em> <b>${esc(f.pt)}</b></div><div><em>الإسعافات والطفاية:</em> <b>${esc(f.kit)}</b></div></div>
  </article></div>`;
}

/* ---- CPR + timer ---- */
const C={run:false,count:0,t0:0,acc:0,beat:null,clk:null,actx:null};
const T={run:false,acc:0,t0:0,int:null,since:''};
function cprElapsed(){return Math.floor((C.acc+(C.run?Date.now()-C.t0:0))/1000);}
function cprTick(){
  if(C.run){
    C.count++;beep();try{navigator.vibrate&&navigator.vibrate(25)}catch(e){}
    const h=$('heart');if(h){h.classList.add('beat');setTimeout(()=>h.classList.remove('beat'),100);}
    if(C.count%30===0)cprAlert('30 ضغطة. إن كنت مدرباً أعطِ نفسين إنقاذيين، وإلا واصل الضغط دون توقف.');
  }
  cprPaint();
}
function cprAlert(t){const a=$('cAlert');if(a){a.textContent=t;a.hidden=false;}toast(t.split('.')[0]);}
function cprPaint(){
  const e=cprElapsed(),m=Math.floor(e/60),s=e%60;
  if($('cCount'))$('cCount').textContent=C.count;
  if($('cTime'))$('cTime').textContent=pad(m)+':'+pad(s);
  if(C.run&&e>0&&e%120===0&&C.lastSwap!==e){C.lastSwap=e;cprAlert('مضت دقيقتان. بدّل المسعف إن وُجد من يساعدك.');}
}
function beep(){try{
  if(!C.actx)C.actx=new (window.AudioContext||window.webkitAudioContext)();
  const o=C.actx.createOscillator(),g=C.actx.createGain();o.type='square';o.frequency.value=880;
  g.gain.setValueAtTime(.35,C.actx.currentTime);g.gain.exponentialRampToValueAtTime(.001,C.actx.currentTime+.09);
  o.connect(g);g.connect(C.actx.destination);o.start();o.stop(C.actx.currentTime+.1);}catch(e){}}
function cprToggle(){
  if(C.run){C.acc+=Date.now()-C.t0;C.run=false;clearInterval(C.beat);clearInterval(C.clk);}
  else{if(C.actx&&C.actx.state==='suspended')C.actx.resume();C.run=true;C.t0=Date.now();cprTick();C.beat=setInterval(cprTick,60000/110);C.clk=setInterval(cprPaint,500);}
  cprDraw();markMid();
}
function cprReset(){C.run=false;clearInterval(C.beat);clearInterval(C.clk);C.count=0;C.acc=0;C.lastSwap=0;cprDraw();markMid();}
function cprDraw(){
  const b=$('press');if(!b)return;
  b.classList.toggle('on',C.run);b.textContent=C.run?'إيقاف مؤقت':(C.count?'استئناف':'اضغط الآن');
  const h=$('heart');if(h)h.classList.toggle('run',C.run);cprPaint();
}
function markMid(){const m=document.querySelector('.nav .mid');if(m)m.style.animation=C.run?'pulse 1s infinite':'';}
function tShow(){const s=Math.floor((T.acc+(T.run?Date.now()-T.t0:0))/1000);const el=$('tClock');if(el)el.textContent=pad(Math.floor(s/3600))+':'+pad(Math.floor(s%3600/60))+':'+pad(s%60);}
function viewCpr(){
  $('hdr').className='hdr slim cprhdr';
  $('hdr').innerHTML=`<div class="hrow"><button class="ib" data-go="#/home" aria-label="رجوع">${U.back}</button><h2>إنعاش قلبي رئوي <small style="font-weight:500;opacity:.7">CPR</small></h2>${netPill()}</div>`;
  $('main').innerHTML=`
  <div class="heart ${C.run?'run':''}" id="heart"><svg class="ecg" viewBox="0 0 300 80" preserveAspectRatio="none" aria-hidden="true"><path d="M0 40h70l8-24 10 48 10-40 6 16h40l8-22 10 44 10-30 6 8h60"/></svg>
    <svg class="h" viewBox="0 0 24 24" fill="var(--red)" aria-hidden="true">${G.heart}</svg></div>
  <button class="press ${C.run?'on':''}" id="press" data-act="cprToggle">${C.run?'إيقاف مؤقت':(C.count?'استئناف':'اضغط الآن')}</button>
  <div class="stats"><div class="stat"><b id="cCount" class="num">${C.count}</b><span>ضغطة</span></div><div class="stat"><b class="num">110</b><span>نبضة/دقيقة</span></div><div class="stat"><b id="cTime" class="num">00:00</b><span>الزمن</span></div></div>
  <p class="alert" id="cAlert" hidden></p>
  <div class="three"><a class="btn red sm" href="tel:997">اتصل 997</a><button class="btn ghost sm" data-act="cprReset">تصفير</button><a class="btn ghost sm" href="#/card/p3" data-go="#/card/p3">الخطوات</a></div>
  <p class="sub" style="margin-top:10px">اضغط في منتصف الصدر بعمق 5 إلى 6 سم مع كل نبضة، واتركه يرتد بالكامل. ضع الهاتف على مكبر الصوت وتحدث مع الإسعاف.</p>
  <div class="panel"><h3 style="font-size:19px;font-weight:800">مؤقت العاصبة</h3><p class="sub">سجّل وقت تركيبها وأبلغ به المسعفين.</p>
    <div class="clock" id="tClock">00:00:00</div><div class="since" id="tSince">${esc(T.since)}</div>
    <div class="three"><button class="btn green sm" data-act="tGo">${T.acc?'استئناف':'بدء'}</button><button class="btn ghost sm" data-act="tPause">إيقاف</button><button class="btn ghost sm" data-act="tReset">تصفير</button></div></div>`;
  cprPaint();tShow();
}

/* ---- me / refs ---- */
function viewMe(){
  $('hdr').className='hdr slim';
  $('hdr').innerHTML=`<div class="hrow"><button class="ib" data-go="#/home" aria-label="رجوع">${U.back}</button><h2>خطتي وإعداداتي</h2>${netPill()}</div>`;
  const f=family(),th=LS.get('theme','auto'),sc=LS.get('scale','1');
  $('main').innerHTML=`
  <h2 class="sec">خطة عائلتي</h2><p class="sub" style="margin-bottom:10px">تظهر أسفل كل بطاقة وفي الطباعة والمشاركة، وتُحفظ على جهازك فقط.</p>
  <div class="fld"><label for="f_loc">الموقع / المنزل</label><input id="f_loc" data-fam="loc" value="${esc(LS.get('fam_loc',''))}" placeholder="${esc(FDEF.loc)}"></div>
  <div class="fld"><label for="f_tel">رقم طوارئ العائلة البديل</label><input id="f_tel" data-fam="tel" inputmode="tel" value="${esc(LS.get('fam_tel',''))}" placeholder="${esc(FDEF.tel)}"></div>
  <div class="fld"><label for="f_pt">نقطة التجمع الآمنة</label><input id="f_pt" data-fam="pt" value="${esc(LS.get('fam_pt',''))}" placeholder="${esc(FDEF.pt)}"></div>
  <div class="fld"><label for="f_kit">موقع حقيبة الإسعافات والطفاية</label><input id="f_kit" data-fam="kit" value="${esc(LS.get('fam_kit',''))}" placeholder="${esc(FDEF.kit)}"></div>
  <h2 class="sec">المظهر</h2>
  <div class="seg" role="group" aria-label="المظهر">${[['auto','تلقائي'],['light','فاتح'],['dark','داكن']].map(([k,l])=>`<button data-act="themeSet" data-v="${k}" aria-pressed="${th===k}">${l}</button>`).join('')}</div>
  <h2 class="sec">حجم الخط</h2>
  <div class="seg" role="group" aria-label="حجم الخط">${[['1','عادي'],['1.12','كبير'],['1.25','كبير جداً']].map(([k,l])=>`<button data-act="scaleSet" data-v="${k}" aria-pressed="${sc===k}">${l}</button>`).join('')}</div>
  ${S.deferred?'<button class="btn blue" data-act="install" style="width:100%;margin-top:16px">تثبيت التطبيق على الشاشة الرئيسية</button>':'<p class="sub" style="margin-top:14px">للتثبيت على آيفون: زر المشاركة ثم «إضافة إلى الشاشة الرئيسية». على أندرويد: قائمة المتصفح ثم «تثبيت التطبيق».</p>'}
  <h2 class="sec">ولّد بطاقة بالذكاء الاصطناعي</h2>
  <div class="box"><div class="fld"><label for="aiText">وصف الحالة</label><textarea id="aiText" rows="3" placeholder="مثال: لدغة نحل لطفل" value=""></textarea></div>
   <div style="display:flex;gap:8px"><button class="btn blue" style="flex:2" data-act="ai" id="aiGo">توليد البطاقة</button><button class="btn ghost" style="flex:1" data-act="key">المفتاح</button></div>
   <p class="sub" style="margin-top:8px">يحتاج مفتاح Gemini خاصاً بك. راجع أي بطاقة مولّدة قبل الاعتماد عليها.</p></div>`;
  document.querySelectorAll('[data-fam]').forEach(i=>i.addEventListener('input',()=>LS.set('fam_'+i.dataset.fam,i.value)));
}
const REFS=[
 ['الإنعاش والإسعاف الأولي (دولية)',[
  ['r1','الجمعية الأمريكية للقلب (AHA): إرشادات الإنعاش القلبي الرئوي والرعاية القلبية الطارئة','https://cpr.heart.org','الإنعاش، الغصة، الجلطة والنوبة القلبية، الغرق.'],
  ['r2','المجلس الأوروبي للإنعاش (ERC): الإرشادات','https://www.cprguidelines.eu','الإنعاش وإنقاذ الغرقى والصدمة التحسسية.'],
  ['r3','الاتحاد الدولي للصليب والهلال الأحمر: الإرشادات الدولية للإسعاف الأولي والإنعاش','https://www.globalfirstaidcentre.org','الحروق والكسور والجروح ولدغات الثعابين وضربة الشمس.'],
  ['r4','برنامج Stop the Bleed (الكلية الأمريكية للجراحين)','https://www.stopthebleed.org','الضغط المباشر وحشو الجرح والعاصبة.']]],
 ['حالات محددة',[
  ['r5','الجمعية الأمريكية للسكتة الدماغية: علامات FAST','https://www.stroke.org','التعرف على الجلطة الدماغية وتسجيل وقت بدء الأعراض.'],
  ['r6','منظمة الصحة العالمية: الغرق','https://www.who.int/news-room/fact-sheets/detail/drowning','الوقاية والإنقاذ المائي ومراقبة الأطفال.'],
  ['r7','مراكز مكافحة الأمراض (CDC): التسمم بأول أكسيد الكربون','https://www.cdc.gov/carbon-monoxide/','الأعراض والوقاية وأجهزة الكشف.'],
  ['r8','مراكز مكافحة الأمراض (CDC): الأمراض المرتبطة بالحرارة','https://www.cdc.gov/heat-health/','الإجهاد الحراري وضربة الشمس.'],
  ['r9','المنظمة العالمية للحساسية (WAO)','https://www.worldallergy.org','الصدمة التحسسية والإبينفرين.'],
  ['r10','مؤسسة الصرع (Epilepsy Foundation)','https://www.epilepsy.com','الإسعاف الأولي لنوبات التشنج.'],
  ['r11','الجمعية الأمريكية للسكري (ADA)','https://diabetes.org','هبوط السكر وقاعدة 15-15.'],
  ['r12','هيئة الخدمات الصحية البريطانية (NHS): الإسعافات الأولية','https://www.nhs.uk/conditions/first-aid/','الرعاف، العين، الإغماء، الكسور وإصابات الرأس.'],
  ['r13','مايو كلينك: الإسعافات الأولية','https://www.mayoclinic.org/first-aid','بتر الأطراف، التسمم الغذائي والجفاف، الربو.']]],
 ['المنزل والمركبة',[
  ['r14','الرابطة الوطنية للحماية من الحرائق (NFPA)','https://www.nfpa.org','طفايات الحريق (PASS)، حرائق المطبخ، خطة الإخلاء.']]],
 ['جهات في المملكة العربية السعودية',[
  ['r15','هيئة الهلال الأحمر السعودي (997)','https://www.srca.gov.sa','الإسعاف والتوعية الإسعافية.'],
  ['r16','وزارة الصحة (937)','https://www.moh.gov.sa','الاستشارات الصحية واستشارات السموم.'],
  ['r17','وزارة الداخلية: الدفاع المدني (998) والمرور وأمن الطرق (996)','https://www.moi.gov.sa','الحرائق والسيول وتسرب الغاز وسلامة الطرق.'],
  ['r18','نجم لخدمات التأمين','https://www.najm.sa','الإبلاغ عن الحوادث المرورية البسيطة.']]]
];
function viewRefs(){
  $('hdr').className='hdr slim';
  $('hdr').innerHTML=`<div class="hrow"><button class="ib" data-go="#/home" aria-label="رجوع">${U.back}</button><h2>المراجع العلمية</h2>${netPill()}</div>`;
  $('main').innerHTML=`<p class="sub" style="margin-bottom:12px">المصادر التي بُنيت عليها الإرشادات، لتقرأ التفاصيل أو تحضر دورة معتمدة. أرقام الأقواس في التطبيق تشير إلى هذه القائمة.</p>`+
   REFS.map(([g,items])=>`<h2 class="sec">${g}</h2>`+items.map(([id,t,u,d])=>`<div class="ref" id="${id}"><span class="n">[${id.slice(1)}]</span><a href="${u}" target="_blank" rel="noopener">${t}</a><small>${d}</small><div class="u">${u.replace(/^https?:\/\/(www\.)?/,'')}</div></div>`).join('')).join('')+
   `<p class="disc"><b>تنبيه.</b> المحتوى للتوعية العامة وقد تتحدث الإرشادات مع الوقت. تأكد من الإصدار الأحدث عند المراجع، وخذ دورة إسعافات أولية معتمدة. في الخطر الحقيقي اتصل أولاً بالرقم المناسب.</p>`;
  if(S.scrollTo){const el=$(S.scrollTo);S.scrollTo=null;if(el){el.scrollIntoView({block:'center'});el.style.borderColor='var(--red)';}}
}

/* ================= SOS sheet ================= */
const NUMS=[['997','الهلال الأحمر (الإسعاف)','حالات طبية وإصابات','var(--red)'],['911','الطوارئ الموحد','الشرطة والإسعاف والإطفاء','#B3182B'],['998','الدفاع المدني','حريق، غاز، سيول، احتجاز','#E98A00'],['937','وزارة الصحة والسموم','استشارات وتسمم','var(--green)'],['996','أمن الطرق','تعطل أو حادث على الطريق','var(--blue)']];
function openSOS(){
  const sh=$('sheet');sh.hidden=false;
  sh.innerHTML=`<div class="sheetp" role="dialog" aria-label="أرقام الطوارئ"><div class="grab"></div><h2>اتصل بالطوارئ</h2>
   <div class="nums">${NUMS.map(([n,t,d,c])=>`<a class="numr" href="tel:${n}" style="--c:${c}"><b>${n}</b><span>${t}<small>${d}</small></span></a>`).join('')}</div>
   <div style="display:grid;gap:8px;margin-top:12px"><button class="btn blue" data-act="locate">شارك موقعي عبر واتساب</button><button class="btn ghost" data-act="closeSheet">إغلاق</button></div></div>`;
}
function closeSheet(){$('sheet').hidden=true;$('sheet').innerHTML='';}
function locate(){
  if(!navigator.geolocation){toast('جهازك لا يدعم تحديد الموقع',true);return;}
  toast('جارٍ تحديد موقعك…');
  navigator.geolocation.getCurrentPosition(pos=>{
    const l='https://maps.google.com/?q='+pos.coords.latitude.toFixed(6)+','+pos.coords.longitude.toFixed(6);
    window.open('https://wa.me/?text='+encodeURIComponent('أحتاج مساعدة عاجلة. موقعي: '+l),'_blank','noopener');
  },()=>toast('تعذر تحديد الموقع. فعّل الإذن من الإعدادات',true),{enableHighAccuracy:true,timeout:10000});
}

/* ================= welcome ================= */
const RESP=`<svg viewBox="0 0 200 270" aria-hidden="true"><ellipse cx="100" cy="258" rx="58" ry="8" fill="#14203F" opacity=".12"/>
<rect x="66" y="150" width="30" height="96" rx="12" fill="#1B2A5C"/><rect x="104" y="150" width="30" height="96" rx="12" fill="#1B2A5C"/>
<rect x="60" y="238" width="40" height="14" rx="7" fill="#101828"/><rect x="100" y="238" width="40" height="14" rx="7" fill="#101828"/>
<rect x="52" y="78" width="96" height="86" rx="26" fill="#3B82F6"/><rect x="52" y="112" width="96" height="9" fill="#fff" opacity=".9"/><rect x="52" y="140" width="96" height="9" fill="#fff" opacity=".9"/>
<rect x="28" y="84" width="26" height="78" rx="13" fill="#3B82F6"/><rect x="146" y="84" width="26" height="70" rx="13" fill="#3B82F6"/>
<rect x="120" y="150" width="62" height="48" rx="9" fill="#E5283A"/><rect x="136" y="146" width="30" height="8" rx="4" fill="#9E1220"/><path d="M148 162h10v8h8v10h-8v8h-10v-8h-8v-10h8z" fill="#fff" transform="translate(-2 -4)"/>
<circle cx="100" cy="52" r="30" fill="#F1B98A"/><path d="M70 50c0-22 16-30 32-30 16 0 28 10 28 28-8-8-18-10-28-10-12 0-22 4-32 12z" fill="#6B3E26"/>
<circle cx="90" cy="56" r="3.500" fill="#14203F"/><circle cx="112" cy="56" r="3.500" fill="#14203F"/><path d="M92 68q9 8 18 0" stroke="#14203F" stroke-width="3" fill="none" stroke-linecap="round"/></svg>`;
const SL2=`<svg viewBox="0 0 200 220" aria-hidden="true"><rect x="52" y="10" width="96" height="190" rx="20" fill="#14203F"/><rect x="58" y="22" width="84" height="166" rx="12" fill="#F2F5FF"/><circle cx="100" cy="86" r="26" fill="#14B866"/><path d="M88 86l8 9 16-18" stroke="#fff" stroke-width="7" fill="none" stroke-linecap="round" stroke-linejoin="round"/><rect x="70" y="128" width="60" height="9" rx="4" fill="#C6CDE6"/><rect x="78" y="146" width="44" height="9" rx="4" fill="#C6CDE6"/><path d="M148 30l12-10M152 44l16-4" stroke="#FFB347" stroke-width="5" stroke-linecap="round"/></svg>`;
const SL3=`<svg viewBox="0 0 200 220" aria-hidden="true"><rect x="20" y="20" width="160" height="50" rx="16" fill="#fff"/><circle cx="46" cy="45" r="14" fill="#E5283A"/><text x="46" y="51" font-size="16" font-weight="900" fill="#fff" text-anchor="middle" font-family="Tajawal">1</text><rect x="70" y="38" width="92" height="9" rx="4" fill="#C6CDE6"/><rect x="70" y="52" width="60" height="7" rx="3" fill="#E1E6F5"/>
<rect x="20" y="82" width="160" height="50" rx="16" fill="#fff"/><circle cx="46" cy="107" r="14" fill="#FF8A1F"/><text x="46" y="113" font-size="16" font-weight="900" fill="#fff" text-anchor="middle" font-family="Tajawal">2</text><rect x="70" y="100" width="92" height="9" rx="4" fill="#C6CDE6"/><rect x="70" y="114" width="60" height="7" rx="3" fill="#E1E6F5"/>
<rect x="20" y="144" width="160" height="50" rx="16" fill="#fff"/><circle cx="46" cy="169" r="14" fill="#14B866"/><text x="46" y="175" font-size="16" font-weight="900" fill="#fff" text-anchor="middle" font-family="Tajawal">3</text><rect x="70" y="162" width="92" height="9" rx="4" fill="#C6CDE6"/><rect x="70" y="176" width="60" height="7" rx="3" fill="#E1E6F5"/></svg>`;
const SLIDES=[[RESP,'مرحباً بك في دليلك الآمن','نتعلم معاً لننقذ حياة'],[SL2,'يعمل بدون إنترنت','بعد التثبيت، البطاقات معك في أي مكان'],[SL3,'خطوة بخطوة وبالصوت','تعليمات قصيرة تقرؤها أو تسمعها']];
let wi=0;
function showWelcome(){
  const w=$('welcome');w.hidden=false;
  w.innerHTML=`<button class="skip" data-act="skip">تخطي</button><div class="slides" id="slides">${SLIDES.map(s=>`<div class="slide">${s[0]}<h2>${s[1]}</h2><p>${s[2]}</p></div>`).join('')}</div>
   <div class="dots" id="dots">${SLIDES.map(()=>'<i></i>').join('')}</div><button class="go" data-act="wnext" id="wgo">التالي</button>`;
  paintW();
  let x0=null;const sl=$('slides');sl.addEventListener('touchstart',e=>{x0=e.touches[0].clientX},{passive:true});
  sl.addEventListener('touchend',e=>{if(x0===null)return;const dx=e.changedTouches[0].clientX-x0;x0=null;if(dx<-50&&wi<SLIDES.length-1){wi++;paintW();}else if(dx>50&&wi>0){wi--;paintW();}});
}
function paintW(){
  document.querySelectorAll('.slide').forEach(s=>s.style.transform=`translateX(${wi*100}%)`);
  document.querySelectorAll('#dots i').forEach((d,i)=>d.classList.toggle('on',i===wi));
  $('wgo').textContent=wi===SLIDES.length-1?'ابدأ الآن':'التالي';
}
function endWelcome(){LS.set('welcomed','1');$('welcome').hidden=true;$('welcome').innerHTML='';}

/* ================= router ================= */
function route(){
  stopSpeakIfLeaving();
  const h=location.hash.replace(/^#\/?/,'')||'home';
  const [a,b]=h.split('/');
  closeSheet();
  if(a==='card'){renderNav('lib');viewCard(b);}
  else if(a==='lib'){renderNav('lib');viewLib(b);}
  else if(a==='cpr'){renderNav('cpr');viewCpr();}
  else if(a==='me'){renderNav('me');viewMe();}
  else if(a==='refs'){renderNav('refs');viewRefs();}
  else{renderNav('home');viewHome();}
  if(a!=='card')GD.id=null;
  updateNet();markMid();
  if(a!=='card'&&a!=='lib'||!S.focus)$('main').scrollTop=0;
}
function stopSpeakIfLeaving(){if(speaking)stopSpeak();}

/* ================= actions ================= */
let h2c=null;
function loadH2C(){if(window.html2canvas)return Promise.resolve();
  if(!h2c)h2c=new Promise((res,rej)=>{const s=document.createElement('script');s.src='https://cdnjs.cloudflare.com/ajax/libs/html2canvas/1.4.1/html2canvas.min.js';s.onload=res;s.onerror=()=>rej(new Error('offline'));document.head.appendChild(s);});return h2c;}
function applyTheme(){const t=LS.get('theme','auto');if(t==='auto')document.documentElement.removeAttribute('data-theme');else document.documentElement.setAttribute('data-theme',t);
  const m=document.querySelector('meta[name=theme-color]');if(m)m.content=(t==='dark'||(t==='auto'&&matchMedia('(prefers-color-scheme:dark)').matches))?'#0A0E1C':'#16224B';}
function applyScale(){document.documentElement.style.setProperty('--scale',LS.get('scale','1'));}

async function aiGen(){
  const sc=($('aiText')||{}).value;const key=LS.get('gemini_key','');
  if(!sc||!sc.trim()){toast('اكتب وصف الحالة أولاً',true);return;}
  if(!key){toast('أدخل مفتاح Gemini أولاً',true);$('keyInput').value='';$('keyDlg').showModal();return;}
  const b=$('aiGo');b.disabled=true;b.textContent='جارٍ التوليد…';
  const prompt=`أنت خبير سلامة وإسعاف في المملكة العربية السعودية. حوّل السيناريو التالي إلى بطاقة إرشادية بالعربية الفصحى المبسطة مبنية على إرشادات إسعاف معتمدة، وأعد JSON فقط بهذا الهيكل:
{"cat":"first_aid|kids_elderly|home_safety|vehicle_road|weather_outdoor","title":"","phone":"911|997|998|937|996","level":"وصف قصير لمستوى الخطورة","lv":"c|d|w|t|p","golden":"القاعدة الأولى","steps":["5 خطوات مرتبة"],"avoid":["2-3 أخطاء"],"gear":["2-3 تجهيزات أو علامات خطر"]}
lv: c حرج، d حرج جداً، w تحذير، t إجراء تقني، p وقائي.
السيناريو: ${sc}`;
  try{
    const r=await fetch('https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent',{method:'POST',headers:{'Content-Type':'application/json','x-goog-api-key':key},
      body:JSON.stringify({contents:[{parts:[{text:prompt}]}],generationConfig:{responseMimeType:'application/json'}})});
    if(!r.ok)throw new Error('HTTP '+r.status);
    let o=JSON.parse((await r.json()).candidates[0].content.parts[0].text);if(Array.isArray(o))o=o[0];
    if(!o.title||!Array.isArray(o.steps)||!o.steps.length)throw new Error('بنية غير صالحة');
    const card={id:'ai'+Date.now(),cat:CATS[o.cat]?o.cat:'first_aid',title:String(o.title),phone:['911','997','998','937','996'].includes(String(o.phone))?String(o.phone):'911',
      level:String(o.level||'بطاقة مولّدة بالذكاء الاصطناعي'),lv:LV[o.lv]?o.lv:'c',golden:String(o.golden||''),steps:o.steps.map(String),avoid:(o.avoid||[]).map(String),gear:(o.gear||[]).map(String)};
    LIB.push(card);let sv=[];try{sv=JSON.parse(LS.get('ai_cards','[]'))}catch(e){}sv.push(card);LS.set('ai_cards',JSON.stringify(sv));
    toast('أُضيفت البطاقة. راجعها قبل الاعتماد عليها');location.hash='#/card/'+card.id;
  }catch(e){toast('تعذر التوليد: '+e.message,true);b.disabled=false;b.textContent='توليد البطاقة';}
}

function startVoice(btn){
  const SR=window.SpeechRecognition||window.webkitSpeechRecognition;if(!SR){toast('البحث الصوتي غير مدعوم في هذا المتصفح',true);return;}
  const r=new SR();r.lang='ar-SA';r.interimResults=false;btn.classList.add('on');
  r.onresult=e=>{const t=e.results[0][0].transcript;S.q=t;S.cat='all';const l=filtered();toast('سمعت: '+t);
    if(l.length===1)location.hash='#/card/'+l[0].id;else{S.focus=false;if(location.hash==='#/lib'||location.hash.startsWith('#/lib/'))route();else location.hash='#/lib';}};
  r.onerror=()=>toast('لم أسمعك جيداً. حاول مرة أخرى',true);
  r.onend=()=>btn.classList.remove('on');
  try{r.start();toast('تحدث الآن…');}catch(e){btn.classList.remove('on');}
}

document.addEventListener('click',async e=>{
  const ref=e.target.closest('[data-ref]');if(ref){e.preventDefault();S.scrollTo=ref.dataset.ref;if(location.hash==='#/refs')route();else location.hash='#/refs';return;}
  const g=e.target.closest('[data-go]');if(g){e.preventDefault();const t=g.dataset.go;if(location.hash===t)route();else location.hash=t;return;}
  const a=e.target.closest('[data-act]');if(!a)return;
  const act=a.dataset.act,p=GD.id?byId(GD.id):null;
  switch(act){
    case 'sos':openSOS();break;
    case 'closeSheet':closeSheet();break;
    case 'locate':locate();break;
    case 'back':history.length>1?history.back():(location.hash='#/home');break;
    case 'theme':{const cur=document.documentElement.getAttribute('data-theme')||(matchMedia('(prefers-color-scheme:dark)').matches?'dark':'light');LS.set('theme',cur==='dark'?'light':'dark');applyTheme();break;}
    case 'themeSet':LS.set('theme',a.dataset.v);applyTheme();viewMe();break;
    case 'scaleSet':LS.set('scale',a.dataset.v);applyScale();viewMe();break;
    case 'mode':GD.mode=a.dataset.m;drawGuide();break;
    case 'next':go(1);break;
    case 'prev':go(-1);break;
    case 'restart':GD.idx=0;drawGuide();break;
    case 'speak':if(speaking){GD.talk=false;stopSpeak();}else{GD.talk=true;speak(guideText(p,Math.min(GD.idx,p.steps.length)));}break;
    case 'copy':try{await navigator.clipboard.writeText(buildText());toast('تم نسخ النص');}catch(_){const ta=document.createElement('textarea');ta.value=buildText();document.body.appendChild(ta);ta.select();try{document.execCommand('copy');toast('تم نسخ النص')}catch(__){toast('تعذر النسخ',true)}ta.remove();}break;
    case 'share':navigator.share({title:p.title,text:buildText()}).catch(()=>{});break;
    case 'print':window.print();break;
    case 'png':try{toast('جارٍ إنشاء الصورة…');await loadH2C();const c=await html2canvas($('tag'),{scale:2,useCORS:true,backgroundColor:getComputedStyle(document.body).backgroundColor});
      const l=document.createElement('a');l.download=p.title.replace(/[\\/:*?"<>|()]/g,'').trim()+'.png';l.href=c.toDataURL('image/png');l.click();toast('تم حفظ الصورة');}catch(_){h2c=null;toast('حفظ الصورة يحتاج اتصالاً بالإنترنت مرة واحدة',true);}break;
    case 'cprToggle':cprToggle();break;
    case 'cprReset':cprReset();break;
    case 'tGo':if(T.run)break;if(!T.acc)T.since='وقت التركيب: '+new Date().toLocaleTimeString('ar-SA',{hour:'2-digit',minute:'2-digit',second:'2-digit'});T.run=true;T.t0=Date.now();T.int=setInterval(tShow,250);$('tSince').textContent=T.since;a.textContent='استئناف';break;
    case 'tPause':if(!T.run)break;T.acc+=Date.now()-T.t0;T.run=false;clearInterval(T.int);tShow();break;
    case 'tReset':T.run=false;clearInterval(T.int);T.acc=0;T.since='';$('tSince').textContent='';tShow();document.querySelector('[data-act=tGo]').textContent='بدء';break;
    case 'voice':startVoice(a);break;
    case 'install':if(S.deferred){S.deferred.prompt();await S.deferred.userChoice;S.deferred=null;viewMe();}break;
    case 'ai':aiGen();break;
    case 'key':$('keyInput').value=LS.get('gemini_key','');$('keyDlg').showModal();break;
    case 'skip':endWelcome();break;
    case 'wnext':if(wi<SLIDES.length-1){wi++;paintW();}else endWelcome();break;
  }
});
$('sheet').addEventListener('click',e=>{if(e.target===$('sheet'))closeSheet();});
$('keyDlg').addEventListener('close',()=>{if($('keyDlg').returnValue==='save'){LS.set('gemini_key',$('keyInput').value.trim());toast('تم حفظ المفتاح');}});
document.addEventListener('keydown',e=>{
  if(e.key==='Escape')closeSheet();
  if(GD.id&&GD.mode==='steps'&&!/INPUT|TEXTAREA/.test((e.target||{}).tagName||'')){if(e.key==='ArrowLeft')go(1);if(e.key==='ArrowRight')go(-1);}
});
window.addEventListener('hashchange',route);
window.addEventListener('online',updateNet);window.addEventListener('offline',updateNet);
window.addEventListener('beforeinstallprompt',e=>{e.preventDefault();S.deferred=e;});
window.addEventListener('appinstalled',()=>{S.deferred=null;toast('تم تثبيت التطبيق');});
if('speechSynthesis' in window)try{speechSynthesis.getVoices();}catch(e){}
if('serviceWorker' in navigator&&/^https?:$/.test(location.protocol)){
  navigator.serviceWorker.register('sw.js').then(()=>navigator.serviceWorker.ready).then(()=>{S.swReady=true;updateNet();}).catch(()=>{});
}

/* ================= start ================= */
applyTheme();applyScale();
const qs=new URLSearchParams(location.search).get('card');
if(qs&&byId(qs)&&!location.hash)location.hash='#/card/'+qs;
if(!LS.get('welcomed','')&&!qs)showWelcome();
route();
})();
