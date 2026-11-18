/* ثواني — الموقع (يعتمد على data.js: CATS, LV, PRESETS, META, CHECKLISTS, PLACES, CTX_DEF) */
(function(){
'use strict';
const $=id=>document.getElementById(id);
const LS={get(k,d){try{const v=localStorage.getItem(k);return v===null?d:v}catch(e){return d}},set(k,v){try{localStorage.setItem(k,v)}catch(e){}}};
const getJ=(k,d)=>{try{const v=JSON.parse(LS.get(k,''));return v==null?d:v}catch(e){return d}};
const esc=s=>String(s==null?'':s).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const norm=s=>String(s||'').toLowerCase().replace(/[ً-ٰٟـ]/g,'').replace(/[أإآ]/g,'ا').replace(/ة/g,'ه').replace(/ى/g,'ي').trim();
const pad=n=>String(n).padStart(2,'0');
const $$=s=>[...document.querySelectorAll(s)];

/* ---------- data ---------- */
let LIB=PRESETS.slice();
{const ai=getJ('ai_cards',[]);if(Array.isArray(ai))LIB=LIB.concat(ai);}
function ext(p){const m=META[p.id]||{};p.ctx=m.ctx||CTX_DEF[p.cat]||PLACES;p.refs=m.refs||p.refs||[];p.vids=m.vids||p.vids||[];return p;}
LIB=LIB.map(ext);
const byId=id=>LIB.find(p=>p.id===id);
const S={q:'',cat:'all',place:'all',season:'',kit:'home',deferred:null,swReady:false,kidsAll:false};
const GD={id:null,idx:0,mode:'over',lang:'en',talk:false,opener:null};
const profile=()=>LS.get('profile','');
const CFG=window.THAWANI||{},AN=CFG.analytics||{};
function siteUrl(){const u=(LS.get('siteUrl','')||CFG.site||'').trim();if(u)return u.replace(/\/+$/,'');if(/^https?:$/.test(location.protocol)&&!/^(localhost|127\.|\[::1\])/.test(location.hostname))return location.origin;return '';}
const shareUrl=id=>{const s=siteUrl();return s?s+'/share/'+id+'.html':'';};
const cardUrl=id=>{const s=siteUrl();return s?s+'/'+(id?'?card='+id:''):'';};
const statsOn=()=>LS.get('noStats','')!=='1'&&navigator.doNotTrack!=='1';
function track(path){if(!statsOn())return;try{if(AN.provider==='goatcounter'&&window.goatcounter&&window.goatcounter.count)window.goatcounter.count({path:'/ev/'+path,title:path,event:true});else if(AN.provider==='endpoint'&&AN.endpoint&&navigator.sendBeacon)navigator.sendBeacon(AN.endpoint,JSON.stringify({e:path,t:Date.now()}));}catch(e){}}
if(AN.provider==='goatcounter'&&AN.code&&statsOn()){const s=document.createElement('script');s.async=true;s.src='https://gc.zgo.at/count.js';s.dataset.goatcounter='https://'+AN.code+'.goatcounter.com/count';s.dataset.goatcounterSettings='{"no_onload":true}';document.head.appendChild(s);}
const isSimple=()=>document.documentElement.dataset.mode==='simple';
const SCALES=[1,1.12,1.25,1.4];
const say=t=>{const l=$('live');l.textContent='';setTimeout(()=>{l.textContent=t},60);};

/* ---------- pictograms ---------- */
const G={
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
 lift:'<path d="M5 3h14a1 1 0 0 1 1 1v16a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1zm3 7 4-4 4 4zm0 4h8l-4 4z" fill-rule="evenodd"/>'
};
function kindOf(p){const t=p.title;
 if(/غاز/.test(t))return'gas';if(/صعق|كهرب/.test(t))return'bolt';if(/مصعد/.test(t))return'lift';
 if(/حريق|اشتعال|حروق|حرائق/.test(t))return'flame';if(/غرق|سيول|مياه/.test(t))return'wave';
 if(/شمس|حرار|جفاف|الحر/.test(t))return'sun';if(/لدغ|لسعة|عضة/.test(t))return'warn';if(/عين/.test(t))return'eye';
 if(/كسور|عظام|عمود/.test(t))return'bone';if(/سكر|دواء|أدوية/.test(t)&&!/نوبة قلبية/.test(t))return'pill';
 if(/ربو|غبار|غصة|اختناق|الفحم|دوار/.test(t))return'wind';if(/نزيف|جروح|جرح|بتر|رعاف/.test(t))return'drop';
 if(/إنعاش|قلب|جلطة|إغماء|حساسية/.test(t))return'heart';if(/طفل|رضيع|أطفال|كبار|سقوط/.test(t))return'child';
 if(/مركبة|إطار|حوادث|المفتاح|بطارية|الخط السريع|المرور|التغريز|الصحراء|سفر/.test(t))return'car';
 if(/إخلاء|منزل|التسمم الغذائي/.test(t))return'home';return'cross';}
const icon=p=>`<span class="ic"><svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">${G[kindOf(p)]}</svg></span>`;
const QUICK=['p3','p1','p2','p5','p20','p21','p25','p15'];
const QLABEL={p3:'توقف القلب',p1:'اختناق بغصة',p2:'نزيف شديد',p5:'ألم صدر أو جلطة',p20:'رائحة غاز',p21:'حريق مطبخ',p25:'دخان فحم',p15:'طفل ابتلع سماً'};

/* ---------- helpers ---------- */
let toastT;
function toast(t){const e=$('toast');e.textContent=t;e.classList.add('show');clearTimeout(toastT);toastT=setTimeout(()=>e.classList.remove('show'),3500);}
const FDEF={loc:'منزل العائلة',tel:'911 / 997',pt:'خارج المبنى الرئيسي',kit:'مدخل المنزل / المطبخ'};
const family=()=>{const o={};for(const k in FDEF)o[k]=(LS.get('fam_'+k,'')||'').trim()||FDEF[k];return o;};
const catName=p=>(CATS[p.cat]||{}).name||'';

/* ---------- renderers ---------- */
function filtered(){const q=norm(S.q);
 return LIB.filter(p=>(S.cat==='all'||p.cat===S.cat)&&(S.place==='all'||(p.ctx||[]).includes(S.place))&&(!S.season||(SEASONS.find(s=>s.id===S.season)||{ids:[]}).ids.includes(p.id))&&(profile()!=='child'||S.kidsAll||KIDS_IDS.includes(p.id))&&(!q||norm([p.title,p.level,p.golden,catName(p),(p.steps||[]).join(' '),(p.avoid||[]).join(' '),(p.gear||[]).join(' '),(p.ctx||[]).join(' ')].join(' ')).includes(q)));}
function renderTiles(){
 $('tiles').innerHTML=QUICK.slice(0,isSimple()?6:8).map(id=>{const p=byId(id);if(!p)return'';
  return `<button class="tile lv-${p.lv}" data-card="${id}" type="button"><span class="top">${icon(p)}<span aria-hidden="true">←</span></span><h3>${esc(QLABEL[id]||p.title)}</h3><p>${esc(p.level)}</p>${p.lv==='c'||p.lv==='d'?'<span class="urgent">حالة عاجلة</span>':''}</button>`}).join('');}
function renderCats(){
 const tabs=[['all','الكل ('+LIB.length+')']].concat(Object.keys(CATS).map(k=>[k,CATS[k].name+' ('+LIB.filter(p=>p.cat===k).length+')']));
 $('cats').innerHTML=tabs.map(([k,l])=>`<button class="chip" type="button" data-cat="${k}" aria-pressed="${S.cat===k}">${esc(l)}</button>`).join('');
 $('seasons').innerHTML=SEASONS.map(s=>`<button class="chip" type="button" data-season="${s.id}" aria-pressed="${S.season===s.id}">${esc(s.name)}</button>`).join('');
 $('places').innerHTML=[['all','كل الأماكن']].concat(PLACES.map(x=>[x,x])).map(([k,l])=>`<button class="chip" type="button" data-place="${k}" aria-pressed="${S.place===k}">${esc(l)}</button>`).join('');}
function renderGrid(){
 const l=filtered();
 $('count').textContent=l.length+' بطاقة';
 $('empty').hidden=l.length>0;if(S.q&&!l.length)track('search/none');
 $('kidsNote').hidden=!(profile()==='child'&&!S.kidsAll);
 $('grid').innerHTML=l.map(p=>`<button class="gcard lv-${p.lv}" type="button" data-card="${p.id}"><span class="top">${icon(p)}<span class="num" aria-label="رقم الطوارئ">${esc(p.phone)}</span></span><span class="cat">${esc(catName(p))}</span><h3>${esc(p.title)}</h3><p>${esc(p.level)}</p><span class="foot"><span>عرض الخطوات ←</span></span></button>`).join('');
 const h=$('qhint');h.innerHTML=S.q?(l.length?`${l.length} بطاقة مطابقة. <a href="#browse" class="all">عرضها ←</a>`:'لا توجد بطاقة مطابقة. جرّب كلمة أخرى.'):'';
}
function renderKit(){
 const k=CHECKLISTS.find(x=>x.id===S.kit)||CHECKLISTS[0];S.kit=k.id;
 const saved=getJ('thawani-kit-'+k.id,[]),pct=Math.round(saved.length/k.items.length*100);
 $('kitTabs').innerHTML=CHECKLISTS.map(x=>`<button class="chip" type="button" data-kit="${x.id}" aria-pressed="${x.id===k.id}">${esc(x.title)}</button>`).join('');
 $('kitPanel').innerHTML=`<div class="head"><div><h3>قائمة ${esc(k.title)}</h3><p class="mut">${esc(k.note)}</p></div><div class="pct"><b class="num" id="kp">${pct}%</b><span class="mut">مكتمل</span></div></div>
  <div class="kbar" aria-hidden="true"><i id="kb" style="width:${pct}%"></i></div>
  <div class="kitems">${k.items.map((t,i)=>`<label><input type="checkbox" data-kitem="${i}" ${saved.includes(i)?'checked':''}><span>${esc(t)}</span></label>`).join('')}</div>
  <a class="srclink" href="${esc(k.refUrl)}" target="_blank" rel="noopener">${esc(k.refTitle)} ↗</a>
  <button class="btn ghostb" type="button" id="kitReset">إعادة ضبط هذه القائمة</button>`;}
const NUMS=[['997','الهلال الأحمر (الإسعاف)','حالات طبية وإصابات','#C5483A'],['911','الطوارئ الموحد','الشرطة والإسعاف والإطفاء','#8E1B2B'],['998','الدفاع المدني','حريق، غاز، سيول، احتجاز','#BA7A20'],['937','وزارة الصحة والسموم','استشارات وتسمم','#087E68'],['993','المرور','حوادث السير','#527CB3'],['996','أمن الطرق','تعطل أو حادث على الطريق','#527CB3']];
function renderNums(){$('nums').innerHTML=NUMS.map(([n,t,d,c])=>`<a class="numc" href="tel:${n}" style="--c:${c}"><b>${n}</b><span>${t}<small>${d}</small></span></a>`).join('');}
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
 ['المنزل والمركبة',[['r14','الرابطة الوطنية للحماية من الحرائق (NFPA)','https://www.nfpa.org','طفايات الحريق (PASS)، حرائق المطبخ، خطة الإخلاء.']]],
 ['جهات في المملكة العربية السعودية',[
  ['r15','هيئة الهلال الأحمر السعودي (997)','https://www.srca.gov.sa','الإسعاف والتوعية الإسعافية.'],
  ['r16','وزارة الصحة (937)','https://www.moh.gov.sa','الاستشارات الصحية واستشارات السموم.'],
  ['r17','وزارة الداخلية: الدفاع المدني (998) والمرور وأمن الطرق (996)','https://www.moi.gov.sa','الحرائق والسيول وتسرب الغاز وسلامة الطرق.'],
  ['r18','نجم لخدمات التأمين','https://www.najm.sa','الإبلاغ عن الحوادث المرورية البسيطة.']]]];
function renderRefs(){$('refs').innerHTML=REFS.map(([g,it])=>`<div><h3>${g}</h3>${it.map(([id,t,u,d])=>`<div class="ref" id="${id}"><span class="n">[${id.slice(1)}]</span><a href="${u}" target="_blank" rel="noopener">${t}</a><small>${d}</small><div class="u">${u.replace(/^https?:\/\/(www\.)?/,'')}</div></div>`).join('')}</div>`).join('');}
function hijriMonth(){try{const f=new Intl.DateTimeFormat('en-u-ca-islamic-umalqura',{month:'numeric'});return Number(f.formatToParts(new Date()).find(x=>x.type==='month').value);}catch(e){return 0;}}
function renderSeasonBand(){const m=hijriMonth(),s=m===9?SEASONS.find(x=>x.id==='ramadan'):(m===11||m===12)?SEASONS.find(x=>x.id==='hajj'):null,b=$('seasonBand');
 if(!s||isSimple()){b.hidden=true;return;}b.hidden=false;b.innerHTML=`<b>موسم ${esc(s.name)}</b><p>${esc(s.d)}</p><button class="btn solid sm" type="button" data-sgo="${s.id}">عرض بطاقات الموسم</button>`;}
function renderFaq(){const V={myth:'خرافة',true:'صحيح',depends:'يعتمد'};
 $('faqList').innerHTML=FAQ.map((f,i)=>`<details><summary><span class="vd ${f.v}">${V[f.v]}</span><span>${esc(f.q)}</span></summary><div class="ans"><p>${esc(f.a)}</p><div class="btnrow"><button class="btn solid sm" type="button" data-card="${f.card}">اقرأ البطاقة</button><button class="btn ghostb sm" type="button" data-act="faqshare" data-i="${i}">مشاركة</button></div></div></details>`).join('');}
const STICKERS=[
 {id:'k1',place:'المطبخ',card:'p20',t:'رائحة غاز؟',s:'لا تلمس الكهرباء. افتح النوافذ واخرج ثم اتصل.',nums:['998']},
 {id:'k2',place:'المطبخ',card:'p21',t:'حريق في المطبخ؟',s:'لا تصب الماء على الزيت. غطِّ المقلاة واخرج.',nums:['998']},
 {id:'f1',place:'الثلاجة',card:'',t:'أرقام الطوارئ وبطاقات الإسعاف',s:'امسح للوصول لكل البطاقات.',nums:['997','911','998','937']},
 {id:'c1',place:'السيارة',card:'p27',t:'تعطلت على الطريق؟',s:'الفلاشر، ثم الأبواب اليمنى، ثم خلف الحاجز.',nums:['996','911']},
 {id:'c2',place:'السيارة',card:'p31',t:'حريق في السيارة؟',s:'أوقف المحرك واخرجوا وابتعدوا 30 متراً.',nums:['998']},
 {id:'d1',place:'باب المنزل',card:'p24',t:'خطة الإخلاء',s:'اخرج أولاً ثم اتصل. نقطة التجمع خارج المبنى.',nums:['998','911']},
 {id:'m1',place:'خزانة الأدوية',card:'p15',t:'ابتلع طفل دواءً أو منظفاً؟',s:'لا تُجبره على القيء. اتصل واحتفظ بالعبوة.',nums:['937','997']},
 {id:'w1',place:'المسبح',card:'p17',t:'غرق؟',s:'اطلب النجدة وأخرجه بأمان وابدأ الإنعاش.',nums:['997']}];
function qrSvg(url){try{const q=qrcode(0,'M');q.addData(url);q.make();return q.createSvgTag({cellSize:4,margin:0,scalable:true});}catch(e){return '';}}
function renderStickers(){
 const base=siteUrl(),sel=getJ('stkSel',STICKERS.map(x=>x.id));
 $('siteInp').value=LS.get('siteUrl','')||base;
 $('stkPick').innerHTML=STICKERS.map(x=>`<label><input type="checkbox" data-stk="${x.id}" ${sel.includes(x.id)?'checked':''}><span>${esc(x.place)}: ${esc(x.t)}</span></label>`).join('');
 const n=$('stkNote');
 if(!base){n.hidden=false;n.textContent='اكتب رابط موقعك أعلاه (مثل https://example.com) ليُبنى عليه رمز QR. وإلا لن يعمل الرمز.';$('stickerSheet').innerHTML='';return;}
 n.hidden=true;
 $('stickerSheet').innerHTML=STICKERS.filter(x=>sel.includes(x.id)).map(x=>`<div class="stk"><span class="pl2">${esc(x.place)}</span><h3>${esc(x.t)}</h3><div class="qr" role="img" aria-label="رمز QR لـ ${esc(x.t)}">${qrSvg(cardUrl(x.card))}</div><div class="sub2">${esc(x.s)}</div><div class="nums2">${x.nums.map(n=>`<span>${n}</span>`).join('')}</div><div class="br">امسح للخطوات الكاملة · ثواني</div></div>`).join('');}
function renderAll(){renderTiles();renderCats();renderGrid();renderKit();renderSeasonBand();}

/* ---------- search / filters ---------- */
$('q').addEventListener('input',e=>{S.q=e.target.value;S.cat='all';S.place='all';renderCats();renderGrid();});
$('q').addEventListener('keydown',e=>{if(e.key==='Enter'){$('browse').scrollIntoView({behavior:'smooth'});}});
document.addEventListener('keydown',e=>{if((e.metaKey||e.ctrlKey)&&e.key.toLowerCase()==='k'){e.preventDefault();$('q').focus();$('quick').scrollIntoView({behavior:'smooth'});}});
const SR=window.SpeechRecognition||window.webkitSpeechRecognition;
if(SR){$('micBtn').hidden=false;$('micBtn').onclick=()=>{const b=$('micBtn'),r=new SR();r.lang='ar-SA';r.interimResults=false;b.classList.add('on');
 r.onresult=e=>{const t=e.results[0][0].transcript;$('q').value=t;S.q=t;S.cat='all';S.place='all';renderCats();renderGrid();toast('سمعت: '+t);const l=filtered();if(l.length===1)openCard(l[0].id);};
 r.onerror=()=>toast('لم أسمعك جيداً. حاول مرة أخرى');r.onend=()=>b.classList.remove('on');try{r.start();toast('تحدث الآن…');}catch(e){b.classList.remove('on');}};}

/* ---------- modal: card ---------- */
let speaking=false;
function stopSpeak(){try{speechSynthesis.cancel()}catch(e){}speaking=false;$$('.wave').forEach(w=>w.classList.remove('on'));const b=document.querySelector('[data-act=speak]');if(b)b.textContent='🔊 تشغيل الصوت';}
function speak(text){
 if(!('speechSynthesis' in window)){toast('جهازك لا يدعم القراءة الصوتية');return;}
 try{speechSynthesis.cancel();const u=new SpeechSynthesisUtterance(text);u.lang='ar-SA';u.rate=Number(LS.get('rate','0.95'))||.95;
  const v=(speechSynthesis.getVoices()||[]).find(v=>/^ar/i.test(v.lang));if(v)u.voice=v;else if(speechSynthesis.getVoices().length)toast('لم يُعثر على صوت عربي على جهازك');
  u.onstart=()=>{speaking=true;$$('.wave').forEach(w=>w.classList.add('on'));const b=document.querySelector('[data-act=speak]');if(b)b.textContent='⏹ إيقاف الصوت';};
  u.onend=u.onerror=()=>{speaking=false;$$('.wave').forEach(w=>w.classList.remove('on'));const b=document.querySelector('[data-act=speak]');if(b)b.textContent='🔊 تشغيل الصوت';};
  speechSynthesis.speak(u);}catch(e){toast('تعذر تشغيل الصوت');}}
const guideText=(p,i)=>i===0?'القاعدة الأولى. '+p.golden:'الخطوة '+i+'. '+p.steps[i-1];
function srcHTML(p){let h='';
 if(p.vids&&p.vids.length)h+=`<div class="srcbox"><h3>فيديو توضيحي</h3>${p.vids.map(v=>{const m=/youtu\.be\/([\w-]{6,})/.exec(v[1]);return `<a class="srclink" href="${esc(v[1])}" target="_blank" rel="noopener">▶ ${esc(v[0])}</a>`+(m?`<button class="btn ghostb sm vbtn" type="button" data-act="vid" data-id="${m[1]}" data-t="${esc(v[0])}">تشغيل داخل الصفحة</button>`:'');}).join('')}<small>المشاهدة تحتاج اتصالاً بالإنترنت.</small></div>`;
 if(p.refs&&p.refs.length)h+=`<div class="srcbox"><h3>المصادر</h3>${p.refs.map(r=>`<a class="srclink" href="${esc(r[1])}" target="_blank" rel="noopener">${esc(r[0])} ↗</a>`).join('')}</div>`;
 return h;}
function buildText(p){p=p||byId(GD.id);const f=family();
 return ['🚨 *'+p.title+'*','📞 رقم الطوارئ: '+p.phone+'  |  '+p.level,'','⭐ *القاعدة الأولى:*',p.golden,'','⚡ *الخطوات:*',...p.steps.map((s,i)=>(i+1)+'. '+s),'','⛔ *تجنب فوراً:*',...p.avoid.map(a=>'• '+a),'','🧰 *التجهيزات وعلامات الخطر:*',...p.gear.map(g=>'• '+g),'','🏠 *بيانات العائلة*','📍 الموقع: '+f.loc,'📞 طوارئ العائلة: '+f.tel,'🏃 نقطة التجمع: '+f.pt,'🩺 الإسعافات/الطفاية: '+f.kit,'',...(shareUrl(p.id)?['🔗 '+shareUrl(p.id)]:[]),'— ثواني'].join('\n');}
function overview(p){const f=family();
 return `<div class="xr"><a class="btn solid sm" target="_blank" rel="noopener" href="https://wa.me/?text=${encodeURIComponent(buildText(p))}">واتساب</a><button class="btn ghostb sm" type="button" data-act="copy">نسخ النص</button><button class="btn ghostb sm" type="button" data-act="copylink">نسخ رابط البطاقة</button><button class="btn ghostb sm" type="button" data-act="png">حفظ كصورة</button><button class="btn ghostb sm" type="button" data-act="print">طباعة A4</button>${navigator.share?'<button class="btn ghostb sm" type="button" data-act="share">مشاركة…</button>':''}</div>
 <div id="printArea"><article class="card lv-${p.lv}" id="cardEl">
  <div class="ch"><span class="mcat">${esc(catName(p))} • ${esc(LV[p.lv]||'')}</span><h2 style="margin-top:2px">${esc(p.title)}</h2><p>${esc(p.level)}</p><a class="callbig" href="tel:${esc(p.phone)}"><span>اتصل الآن</span><b>${esc(p.phone)}</b></a></div>
  <div class="cb"><div class="golden"><b>القاعدة الأولى</b>${esc(p.golden)}</div>
   <div><h3>الخطوات</h3><ol class="stepsl">${p.steps.map((s,i)=>{const il=illus(p.id,i);return `<li>${il?`<span class="thumb">${il}</span>`:''}<span class="t">${esc(s)}</span></li>`;}).join('')}</ol></div>
   <div class="avoidb"><h3>تجنب فوراً</h3><ul>${p.avoid.map(a=>`<li>${esc(a)}</li>`).join('')}</ul></div>
   <div class="gearb"><h3>التجهيزات وعلامات الخطر</h3><div class="gearlist">${p.gear.map(g=>`<label><input type="checkbox"><span>${esc(g)}</span></label>`).join('')}</div></div>
   ${srcHTML(p)}</div>
  <div class="cfoot"><div><em>الموقع:</em> <b>${esc(f.loc)}</b></div><div><em>طوارئ العائلة:</em> <b>${esc(f.tel)}</b></div><div><em>نقطة التجمع:</em> <b>${esc(f.pt)}</b></div><div><em>الإسعافات والطفاية:</em> <b>${esc(f.kit)}</b></div></div>
 </article></div>`;}
function guide(p){const n=p.steps.length,i=GD.idx;
 if(i>n)return `<div class="stage lv-${p.lv}"><div class="sart">${icon(p)}</div><h3 style="font-family:var(--fh);font-size:24px;margin-top:12px">أحسنت. هل اتصلت بـ ${esc(p.phone)}؟</h3><p class="mut">تابع حالة المصاب حتى وصول المسعفين.</p>
  <div class="avoidb" style="margin-top:12px"><h3>تجنب فوراً</h3><ul>${p.avoid.map(a=>`<li>${esc(a)}</li>`).join('')}</ul></div>
  <div class="cb" style="padding:12px 0 0">${srcHTML(p)}</div>
  <div class="btnrow"><a class="btn red" href="tel:${esc(p.phone)}">اتصل بـ ${esc(p.phone)}</a><button class="btn ${profile()==='child'?'solid':'ghostb'}" type="button" data-act="quiz">اختبر نفسك</button><button class="btn ghostb" type="button" data-act="restart">إعادة الخطوات</button><button class="btn ghostb" type="button" data-act="mode" data-m="over">عرض البطاقة كاملة</button></div></div>`;
 const bars=Array.from({length:n+1},(_,k)=>`<i class="${k<=i?'on':''}"></i>`).join('');
 const extra=(p.id==='p3'?'<button class="btn ghostb sm" type="button" data-act="tool" data-t="tools">افتح إيقاع الإنعاش</button>':'')+(p.id==='p2'?'<button class="btn ghostb sm" type="button" data-act="tool" data-t="tools">ابدأ مؤقت العاصبة</button>':'');
 const il=i>0?illus(p.id,i-1):'';
 return `<div class="stage lv-${p.lv}" id="stage"><div class="sart${il?' ill':''}">${il||icon(p)}<div class="wave">${'<i></i>'.repeat(26)}</div>${il?'':`<div class="cap">${esc(p.level)}</div>`}</div>
  <div class="stepno">${i===0?'قبل أن تبدأ':'الخطوة '+i+' من '+n}</div><div class="steptx ${i===0?'g':''}">${esc(i===0?p.golden:p.steps[i-1])}</div>
  <div class="pbar" aria-hidden="true">${bars}</div>
  <div class="ctl"><button class="btn ghostb" type="button" data-act="prev" aria-label="السابق" ${i===0?'disabled':''}>→</button><button class="btn solid" type="button" data-act="speak">${speaking?'⏹ إيقاف الصوت':'🔊 تشغيل الصوت'}</button><button class="btn solid" type="button" data-act="next">${i===n?'إنهاء':'التالي'} ←</button></div>
  ${extra?`<div class="btnrow">${extra}</div>`:''}</div>
  <a class="btn red" href="tel:${esc(p.phone)}" style="width:100%;margin-top:12px">اتصل الآن ${esc(p.phone)}</a>`;}
function drawModal(){
 const p=byId(GD.id);if(!p)return;
 $('mBody').innerHTML=`<div class="lv-${p.lv}">${GD.mode!=='over'?`<span class="mcat">${esc(catName(p))}${p.lv==='c'||p.lv==='d'?' · حالة عاجلة':''}</span><h2 id="mTitle">${esc(p.title)}</h2>`:'<span class="sr" id="mTitle">'+esc(p.title)+'</span>'}
  <div class="tabs" role="tablist"><button type="button" role="tab" data-act="mode" data-m="over" aria-selected="${GD.mode==='over'}">البطاقة كاملة</button><button type="button" role="tab" data-act="mode" data-m="guide" aria-selected="${GD.mode==='guide'||GD.mode==='quiz'}">خطوة بخطوة وبالصوت</button>${langsFor(p).map(l=>`<button type="button" role="tab" data-act="mode" data-m="lang" data-l="${l.id}" aria-selected="${GD.mode==='lang'&&GD.lang===l.id}" lang="${l.id}" dir="${l.dir}">${esc(l.name)}</button>`).join('')}</div>
  ${GD.mode==='over'?overview(p):GD.mode==='lang'?langView(p,GD.lang):GD.mode==='quiz'?quizView(p):guide(p)}</div>`;
 const st=$('stage');if(st){let x0=null;st.addEventListener('touchstart',e=>{x0=e.touches[0].clientX},{passive:true});st.addEventListener('touchend',e=>{if(x0===null)return;const dx=e.changedTouches[0].clientX-x0;x0=null;if(dx<-55)go(1);else if(dx>55)go(-1);});}
}
function go(d){const p=byId(GD.id),n=p.steps.length,ni=Math.max(0,Math.min(n+1,GD.idx+d));if(ni===GD.idx)return;GD.idx=ni;drawModal();track((ni>n?'done/':'step/')+p.id+(ni>n?'':'/'+ni));say(ni===0?p.golden:ni<=n?'الخطوة '+ni+' من '+n+'. '+p.steps[ni-1]:'انتهت الخطوات');
 if(GD.talk&&ni<=n)speak(guideText(p,ni));else if(ni>n)stopSpeak();}
function openCard(id,mode){
 const p=byId(id);if(!p)return;GD.id=id;GD.idx=0;GD.mode=mode||'over';GD.talk=false;GD.opener=document.activeElement;stopSpeak();track('card/'+id);
 drawModal();showModal();
 try{history.replaceState(null,'','?card='+id)}catch(e){}}
function closeModal(){if($('modal').hidden)return;$('modal').hidden=true;document.body.style.overflow='';setBg(false);stopSpeak();GD.id=null;
 try{history.replaceState(null,'',location.pathname+location.hash)}catch(e){}
 if(GD.opener&&GD.opener.focus)try{GD.opener.focus()}catch(e){}}
function openAbout(){GD.id=null;GD.opener=document.activeElement;
 $('mBody').innerHTML=`<span class="mcat">عن الدليل</span><h2 id="mTitle">ثواني</h2><p class="mut" style="margin-bottom:14px">دليل توعوي مبسّط للتصرف في مواقف الطوارئ في المملكة. يعرض خطوات عامة إلى حين وصول المساعدة، ولا يستبدل الاتصال بالطوارئ ولا التدريب المعتمد. بعد تثبيته يعمل بدون إنترنت، وبياناتك (خطة العائلة وقوائم التجهيز) تُحفظ على جهازك فقط.</p>
 <div class="nums">${NUMS.map(([n,t,d,c])=>`<a class="numc" href="tel:${n}" style="--c:${c}"><b>${n}</b><span>${t}<small>${d}</small></span></a>`).join('')}</div>
 <div class="btnrow"><a class="btn ghostb" href="#plan" data-close>خطة عائلتك</a><a class="btn ghostb" href="#references" data-close>المراجع</a><button class="btn ghostb" type="button" data-act="sharesite">مشاركة ثواني</button></div>
 <p class="note">للتعلم العملي راجع برامج التدريب الرسمية في الهلال الأحمر السعودي. ثواني مبادرة توعوية غير رسمية.</p>
 <p class="mut" style="margin-top:10px"><b>الخصوصية:</b> لا نجمع اسماً ولا رقماً ولا موقعاً ولا نص بحثك. إن فُعّلت الإحصاءات فهي أعداد مجهولة فقط (أي بطاقة فُتحت وإلى أي خطوة وصل القارئ) بلا كوكيز، ويمكنك إيقافها من «خطة عائلتك».</p>`;
 showModal();}
$('closeModal').onclick=closeModal;
$('modal').addEventListener('click',e=>{if(e.target===$('modal'))closeModal();});
document.addEventListener('keydown',e=>{if(e.key==='Escape')closeModal();
 if(GD.id&&GD.mode==='guide'&&!$('modal').hidden&&!/INPUT|TEXTAREA/.test((e.target||{}).tagName||'')){if(e.key==='ArrowLeft')go(1);if(e.key==='ArrowRight')go(-1);}});

/* ---------- English / quiz / emergency / script ---------- */
const TR={en:EN};
const LANGS=[{id:'en',name:'English',dir:'ltr',ui:{call:'Call now',first:'First rule',steps:'Steps',avoid:'Avoid immediately',note:'Draft translation for visitors who do not read Arabic. The Arabic card is the reference. Always call the emergency number first.'}},{id:'ur',name:'اردو',dir:'rtl'},{id:'fil',name:'Filipino',dir:'ltr'},{id:'bn',name:'বাংলা',dir:'ltr'}];
async function loadLangs(){for(const l of LANGS){if(l.id==='en')continue;try{const r=await fetch('i18n/'+l.id+'.json',{cache:'no-cache'});if(!r.ok)continue;const j=await r.json();if(j&&j.reviewed===true&&j.cards&&j.ui){TR[l.id]=j.cards;l.ui=j.ui;}}catch(e){}}}
const langsFor=p=>LANGS.filter(l=>TR[l.id]&&TR[l.id][p.id]&&l.ui);
function langView(p,lang){const l=LANGS.find(x=>x.id===lang),e=TR[lang][p.id],u=l.ui;
 return `<div class="en" dir="${l.dir}" lang="${l.id}" style="text-align:${l.dir==='rtl'?'right':'left'}"><div class="note" style="margin-top:0">${esc(u.note)}</div>
 <a class="callbig" href="tel:${esc(p.phone)}"><span>${esc(u.call)}</span><b>${esc(p.phone)}</b></a>
 <h3 class="enh">${esc(e.title)}</h3><p class="mut">${esc(e.level)}</p>
 <div class="golden" style="margin-top:10px"><b>${esc(u.first)}</b>${esc(e.golden)}</div>
 <h3 class="enh" style="font-size:20px">${esc(u.steps)}</h3><ol class="stepsl">${e.steps.map(s=>`<li><span class="t">${esc(s)}</span></li>`).join('')}</ol>
 <div class="avoidb" style="margin-top:12px"><h3>${esc(u.avoid)}</h3><ul>${e.avoid.map(a=>`<li>${esc(a)}</li>`).join('')}</ul></div></div>`;}
const QZ={qs:[],i:0,s:0,sel:null};
const shuffle=a=>a.map(v=>[Math.random(),v]).sort((x,y)=>x[0]-y[0]).map(x=>x[1]);
function buildQuiz(p){const nums=['997','911','998','937','993','996'],qs=[];
 qs.push({q:'ما الرقم المناسب للاتصال في هذه الحالة؟',ok:p.phone,opts:shuffle([p.phone,...shuffle(nums.filter(n=>n!==p.phone)).slice(0,2)]),why:'الرقم المناسب هو '+p.phone+'.'});
 if(p.avoid.length&&p.steps.length>=2){const a=p.avoid[Math.floor(Math.random()*p.avoid.length)];qs.push({q:'أي مما يلي يجب تجنبه في هذه الحالة؟',ok:a,opts:shuffle([a,...shuffle(p.steps.slice()).slice(0,2)]),why:'هذا ما يجب تجنبه: '+a});}
 if(p.avoid.length>=1){const s=p.steps[Math.floor(Math.random()*p.steps.length)];qs.push({q:'أي مما يلي خطوة صحيحة في هذه الحالة؟',ok:s,opts:shuffle([s,...shuffle(p.avoid.slice()).slice(0,2)]),why:'الخطوة الصحيحة: '+s});}
 return qs;}
function quizView(p){
 if(QZ.i>=QZ.qs.length){const n=QZ.qs.length,st='★'.repeat(QZ.s)+'☆'.repeat(n-QZ.s);
  return `<div class="stage"><div class="stars" aria-label="${QZ.s} من ${n}">${st}</div><h3 style="font-family:var(--fh);font-size:24px;text-align:center">${QZ.s===n?'ممتاز! أنت جاهز لهذه الحالة':QZ.s>0?'أحسنت، راجع الخطوات مرة أخرى':'لا بأس، اقرأ الخطوات مرة ثانية وحاول'}</h3>
  <div class="btnrow" style="justify-content:center"><button class="btn solid" type="button" data-act="quiz">اختبار جديد</button><button class="btn ghostb" type="button" data-act="restart">راجع الخطوات</button></div></div>`;}
 const q=QZ.qs[QZ.i],done=QZ.sel!==null;
 return `<div class="stage"><div class="stepno">سؤال ${QZ.i+1} من ${QZ.qs.length}</div><div class="steptx" style="min-height:0;font-size:calc(21px*var(--scale))">${esc(q.q)}</div>
 <div class="opts">${q.opts.map((o,k)=>`<button type="button" class="opt ${done?(o===q.ok?'ok':(k===QZ.sel?'bad':'')):''}" data-act="qans" data-k="${k}" ${done?'disabled':''}>${esc(o)}</button>`).join('')}</div>
 <div class="fb" aria-live="polite">${done?(q.opts[QZ.sel]===q.ok?'أحسنت! إجابة صحيحة.':esc(q.why)):''}</div>
 ${done?`<div class="btnrow"><button class="btn solid" type="button" data-act="qnext">${QZ.i+1>=QZ.qs.length?'النتيجة':'السؤال التالي'}</button></div>`:''}</div>`;}
function scriptHTML(){const f=LS.get('fam_loc','').trim();
 return `<div class="script"><h3>ماذا أقول عند الاتصال؟</h3><ol><li>«السلام عليكم، أحتاج إسعافاً.»</li><li>العنوان: ${f?esc(f):'اذكر المدينة والحي واسم الشارع وأقرب معلم'}.</li><li>ماذا حدث؟ مثال: «شخص لا يتنفس» أو «نزيف شديد» أو «حريق في المطبخ».</li><li>كم عدد المصابين وكم عمرهم تقريباً؟</li><li>هل المصاب واعٍ؟ هل يتنفس؟</li><li>اسمي … ورقم جوالي … وابقَ على الخط ولا تغلقه حتى يطلب منك.</li></ol><div class="btnrow"><button class="btn ghostb sm" type="button" data-act="copyscript">نسخ النص</button></div></div>`;}
function openEmergency(){track('emergency');GD.id=null;GD.opener=document.activeElement;
 const items=[['p3','لا يتنفس أو لا يستجيب'],['p2','ينزف بشدة'],['p1','يختنق'],['p4','حساسية شديدة'],['p20','رائحة غاز'],['p21','حريق']];
 $('mBody').innerHTML=`<span class="mcat">وضع الطوارئ</span><h2 id="mTitle">ماذا يحدث الآن؟</h2>
 <a class="callhuge" href="tel:997"><span>اتصل بالإسعاف</span><b>997</b></a>
 <div class="emgrid">${items.map(([id,l])=>`<button type="button" class="emb" data-act="em" data-id="${id}">${l}</button>`).join('')}</div>
 <div class="btnrow"><button class="btn solid" type="button" data-act="locate">شارك موقعي عبر واتساب</button><a class="btn ghostb" href="tel:911">الطوارئ الموحد 911</a><a class="btn ghostb" href="tel:998">الدفاع المدني 998</a></div>${scriptHTML()}`;
 showModal();}
function openScript(){GD.id=null;GD.opener=document.activeElement;$('mBody').innerHTML=`<span class="mcat">قبل أن تتصل</span><h2 id="mTitle">ماذا أقول للإسعاف؟</h2><a class="callhuge" href="tel:997"><span>اتصل بالإسعاف</span><b>997</b></a>${scriptHTML()}`;showModal();}
function locate(){$('locBtn').click();}
function showModal(){$('modal').hidden=false;document.body.style.overflow='hidden';setBg(true);$('closeModal').focus();}
function setBg(on){['main','top'].forEach(i=>{const e=$(i);if(e)e.inert=on;});$$('.foot,.mobile-nav').forEach(e=>e.inert=on);}

document.addEventListener('keydown',e=>{if(e.key!=='Tab'||$('modal').hidden)return;
 const f=$$('#modal a[href],#modal button:not([disabled]),#modal input,#modal [tabindex]:not([tabindex="-1"])').filter(x=>x.offsetParent!==null);if(!f.length)return;
 const a=f[0],z=f[f.length-1];if(e.shiftKey&&document.activeElement===a){e.preventDefault();z.focus();}else if(!e.shiftKey&&document.activeElement===z){e.preventDefault();a.focus();}});
/* ---------- export ---------- */
let h2c=null;
function loadH2C(){if(window.html2canvas)return Promise.resolve();if(!h2c)h2c=new Promise((res,rej)=>{const s=document.createElement('script');s.src='https://cdnjs.cloudflare.com/ajax/libs/html2canvas/1.4.1/html2canvas.min.js';s.onload=res;s.onerror=()=>rej(new Error('offline'));document.head.appendChild(s);});return h2c;}

/* ---------- CPR + timer ---------- */
const C={run:false,count:0,t0:0,acc:0,beat:null,clk:null,actx:null,lastSwap:0};
const T={run:false,acc:0,t0:0,int:null,since:''};
const cprElapsed=()=>Math.floor((C.acc+(C.run?Date.now()-C.t0:0))/1000);
function beep(){try{if(!C.actx)C.actx=new (window.AudioContext||window.webkitAudioContext)();const o=C.actx.createOscillator(),g=C.actx.createGain();o.type='square';o.frequency.value=880;g.gain.setValueAtTime(.35,C.actx.currentTime);g.gain.exponentialRampToValueAtTime(.001,C.actx.currentTime+.09);o.connect(g);g.connect(C.actx.destination);o.start();o.stop(C.actx.currentTime+.1);}catch(e){}}
function cprAlert(t){const a=$('cAlert');a.textContent=t;a.hidden=false;toast(t.split('.')[0]);say(t);}
function cprPaint(){const e=cprElapsed();$('cCount').textContent=C.count;$('cTime').textContent=pad(Math.floor(e/60))+':'+pad(e%60);
 if(C.run&&e>0&&e%120===0&&C.lastSwap!==e){C.lastSwap=e;cprAlert('مضت دقيقتان. بدّل المسعف إن وُجد من يساعدك.');}}
function cprTick(){if(C.run){C.count++;beep();try{navigator.vibrate&&navigator.vibrate(25)}catch(e){}const h=$('heartbox');h.classList.add('beat');setTimeout(()=>h.classList.remove('beat'),100);
 if(C.count%30===0)cprAlert('30 ضغطة. إن كنت مدرباً أعطِ نفسين إنقاذيين، وإلا واصل الضغط دون توقف.');}cprPaint();}
function cprDraw(){const b=$('cprBtn');b.classList.toggle('on',C.run);b.textContent=C.run?'إيقاف مؤقت':(C.count?'استئناف':'اضغط الآن');$('heartbox').classList.toggle('run',C.run);cprPaint();}
$('cprBtn').onclick=()=>{if(C.run){C.acc+=Date.now()-C.t0;C.run=false;clearInterval(C.beat);clearInterval(C.clk);}else{if(C.actx&&C.actx.state==='suspended')C.actx.resume();C.run=true;C.t0=Date.now();cprTick();C.beat=setInterval(cprTick,60000/110);C.clk=setInterval(cprPaint,500);}cprDraw();};
$('cprReset').onclick=()=>{C.run=false;clearInterval(C.beat);clearInterval(C.clk);C.count=0;C.acc=0;C.lastSwap=0;$('cAlert').hidden=true;cprDraw();};
function tShow(){const s=Math.floor((T.acc+(T.run?Date.now()-T.t0:0))/1000);$('tClock').textContent=pad(Math.floor(s/3600))+':'+pad(Math.floor(s%3600/60))+':'+pad(s%60);}
$('tGo').onclick=()=>{if(T.run)return;if(!T.acc)T.since='وقت التركيب: '+new Date().toLocaleTimeString('ar-SA',{hour:'2-digit',minute:'2-digit',second:'2-digit'});T.run=true;T.t0=Date.now();T.int=setInterval(tShow,250);$('tSince').textContent=T.since;$('tGo').textContent='استئناف';};
$('tPause').onclick=()=>{if(!T.run)return;T.acc+=Date.now()-T.t0;T.run=false;clearInterval(T.int);tShow();};
$('tReset').onclick=()=>{T.run=false;clearInterval(T.int);T.acc=0;T.since='';$('tSince').textContent='';$('tGo').textContent='بدء';tShow();};

/* ---------- location / share / theme / scale ---------- */
$('locBtn').onclick=()=>{if(!navigator.geolocation){toast('جهازك لا يدعم تحديد الموقع');return;}toast('جارٍ تحديد موقعك…');
 navigator.geolocation.getCurrentPosition(pos=>{const l='https://maps.google.com/?q='+pos.coords.latitude.toFixed(6)+','+pos.coords.longitude.toFixed(6);window.open('https://wa.me/?text='+encodeURIComponent('أحتاج مساعدة عاجلة. موقعي: '+l),'_blank','noopener');},()=>toast('تعذر تحديد الموقع. فعّل الإذن من الإعدادات'),{enableHighAccuracy:true,timeout:10000});};
async function shareSite(){if(location.protocol==='file:'){toast('بعد نشر الموقع على رابط HTTPS تقدر تشاركه وتثبته كتطبيق.');return;}
 const url=location.href.split('#')[0].split('?')[0];try{if(navigator.share)await navigator.share({title:'ثواني',text:'بطاقات طوارئ بالعربية خطوة بخطوة وبالصوت.',url});else{await navigator.clipboard.writeText(url);toast('تم نسخ رابط ثواني للمشاركة.');}}catch(e){if(e&&e.name!=='AbortError')toast('انسخ رابط الصفحة من شريط العنوان.');}}
$('shareBtn').onclick=shareSite;
function applyTheme(){const t=LS.get('theme','auto'),r=document.documentElement;if(t==='auto')r.removeAttribute('data-theme');else r.setAttribute('data-theme',t);
 const dark=t==='dark'||(t==='auto'&&matchMedia('(prefers-color-scheme:dark)').matches),m=document.querySelector('meta[name=theme-color]');if(m)m.content=dark?'#0E1714':'#F7F7F3';}
$('themeBtn').onclick=()=>{const cur=document.documentElement.getAttribute('data-theme');const dark=cur==='dark'||(!cur&&matchMedia('(prefers-color-scheme:dark)').matches);LS.set('theme',dark?'light':'dark');applyTheme();};
const applyScale=()=>document.documentElement.style.setProperty('--scale',LS.get('scale','1'));
$('aboutBtn').onclick=openAbout;$('aboutBtn2').onclick=openAbout;

/* ---------- AI ---------- */
$('keyBtn').onclick=()=>{$('keyInput').value=LS.get('gemini_key','');$('keyDlg').showModal();};
$('keyDlg').addEventListener('close',()=>{if($('keyDlg').returnValue==='save'){LS.set('gemini_key',$('keyInput').value.trim());toast('تم حفظ المفتاح');}});
$('aiGo').onclick=async()=>{const sc=$('aiText').value.trim(),key=LS.get('gemini_key','');
 if(!sc){toast('اكتب وصف الحالة أولاً');return;}if(!key){toast('أدخل مفتاح Gemini أولاً');$('keyBtn').click();return;}
 const b=$('aiGo');b.disabled=true;b.textContent='جارٍ التوليد…';
 const prompt=`أنت خبير سلامة وإسعاف في المملكة العربية السعودية. حوّل السيناريو التالي إلى بطاقة إرشادية بالعربية الفصحى المبسطة مبنية على إرشادات إسعاف معتمدة، وأعد JSON فقط بهذا الهيكل:
{"cat":"first_aid|kids_elderly|home_safety|vehicle_road|weather_outdoor","title":"","phone":"911|997|998|937|996","level":"وصف قصير لمستوى الخطورة","lv":"c|d|w|t|p","golden":"القاعدة الأولى","steps":["4 إلى 5 خطوات مرتبة"],"avoid":["2-3 أخطاء"],"gear":["2-3 تجهيزات أو علامات خطر"]}
lv: c حرج، d حرج جداً، w تحذير، t إجراء تقني، p وقائي. لا تذكر جرعات أدوية؛ وجّه إلى موظف الطوارئ.
السيناريو: ${sc}`;
 try{const r=await fetch('https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent',{method:'POST',headers:{'Content-Type':'application/json','x-goog-api-key':key},body:JSON.stringify({contents:[{parts:[{text:prompt}]}],generationConfig:{responseMimeType:'application/json'}})});
  if(!r.ok)throw new Error('HTTP '+r.status);let o=JSON.parse((await r.json()).candidates[0].content.parts[0].text);if(Array.isArray(o))o=o[0];
  if(!o.title||!Array.isArray(o.steps)||!o.steps.length)throw new Error('بنية غير صالحة');
  const card=ext({id:'ai'+Date.now(),cat:CATS[o.cat]?o.cat:'first_aid',title:String(o.title),phone:['911','997','998','937','996'].includes(String(o.phone))?String(o.phone):'911',level:String(o.level||'بطاقة مولّدة بالذكاء الاصطناعي'),lv:LV[o.lv]?o.lv:'c',golden:String(o.golden||''),steps:o.steps.map(String),avoid:(o.avoid||[]).map(String),gear:(o.gear||[]).map(String)});
  LIB.push(card);const sv=getJ('ai_cards',[]);sv.push(card);LS.set('ai_cards',JSON.stringify(sv));renderAll();openCard(card.id);toast('أُضيفت البطاقة. راجعها قبل الاعتماد عليها');
 }catch(e){toast('تعذر التوليد: '+e.message);}finally{b.disabled=false;b.textContent='توليد البطاقة';}};

/* ---------- global clicks ---------- */
document.addEventListener('click',async e=>{
 const tl=e.target.closest('a[href^="tel:"]');if(tl)track('call/'+tl.getAttribute('href').slice(4));
 const cd=e.target.closest('[data-card]');if(cd){openCard(cd.dataset.card);return;}
 const ct=e.target.closest('[data-cat]');if(ct){S.cat=ct.dataset.cat;renderCats();renderGrid();return;}
 const pl=e.target.closest('[data-place]');if(pl){S.place=pl.dataset.place;renderCats();renderGrid();return;}
 const kt=e.target.closest('[data-kit]');if(kt){S.kit=kt.dataset.kit;renderKit();return;}
 if(e.target.closest('#kitReset')){LS.set('thawani-kit-'+S.kit,'[]');renderKit();return;}
 const sn=e.target.closest('[data-season]');if(sn){S.season=S.season===sn.dataset.season?'':sn.dataset.season;S.cat='all';S.place='all';S.q='';$('q').value='';renderCats();renderGrid();return;}
 const sg=e.target.closest('[data-sgo]');if(sg){S.season=sg.dataset.sgo;S.cat='all';S.place='all';renderCats();renderGrid();$('browse').scrollIntoView({behavior:'smooth'});return;}
 const pfb=e.target.closest('[data-profile]');if(pfb){applyProfile(pfb.dataset.profile);if($('profDlg').open)$('profDlg').close();return;}
 const rt=e.target.closest('[data-rate]');if(rt){LS.set('rate',rt.dataset.rate);toast('تم ضبط سرعة القراءة');return;}
 const fs=e.target.closest('[data-fs]');if(fs){const cur=SCALES.indexOf(Number(LS.get('scale','1')));const ni=Math.max(0,Math.min(SCALES.length-1,(cur<0?0:cur)+Number(fs.dataset.fs)));LS.set('scale',String(SCALES[ni]));applyScale();return;}
 const sc=e.target.closest('[data-scale]');if(sc){LS.set('scale',sc.dataset.scale);applyScale();return;}
 if(e.target.closest('[data-close]')){closeModal();return;}
 const a=e.target.closest('[data-act]');if(!a)return;const act=a.dataset.act,p=GD.id?byId(GD.id):null;
 switch(act){
  case 'mode':GD.mode=a.dataset.m;if(GD.mode==='lang'){GD.lang=a.dataset.l;track('lang/'+GD.lang+'/'+p.id);}if(GD.mode==='over'||GD.mode==='lang'){GD.talk=false;stopSpeak();}drawModal();if(GD.mode==='guide'&&LS.get('autoread','')==='1'){GD.talk=true;speak(guideText(p,Math.min(GD.idx,p.steps.length)));}break;
  case 'quiz':QZ.qs=buildQuiz(p);QZ.i=0;QZ.s=0;QZ.sel=null;GD.mode='quiz';stopSpeak();drawModal();break;
  case 'qans':{const q=QZ.qs[QZ.i];QZ.sel=Number(a.dataset.k);if(q.opts[QZ.sel]===q.ok)QZ.s++;drawModal();say(q.opts[QZ.sel]===q.ok?'إجابة صحيحة':q.why);break;}
  case 'qnext':QZ.i++;QZ.sel=null;if(QZ.i>=QZ.qs.length){LS.set('stars',String((Number(LS.get('stars','0'))||0)+QZ.s));updateStars();}drawModal();break;
  case 'em':closeModal();openCard(a.dataset.id,'guide');if(LS.get('autoread','')==='1'){GD.talk=true;speak(guideText(byId(a.dataset.id),0));}break;
  case 'locate':locate();break;
  case 'copyscript':try{await navigator.clipboard.writeText($$('.script li').map((x,i)=>(i+1)+'. '+x.textContent).join('\n'));toast('تم نسخ النص');}catch(_){toast('تعذر النسخ');}break;
  case 'vid':if(!navigator.onLine){toast('تشغيل الفيديو يحتاج اتصالاً بالإنترنت');break;}{const w=document.createElement('div');w.className='vid';w.innerHTML='<iframe src="https://www.youtube-nocookie.com/embed/'+encodeURIComponent(a.dataset.id)+'" title="'+esc(a.dataset.t)+'" loading="lazy" allow="encrypted-media; picture-in-picture" allowfullscreen></iframe>';a.replaceWith(w);}break;
  case 'next':go(1);break;case 'prev':go(-1);break;case 'restart':GD.idx=0;drawModal();break;
  case 'speak':if(speaking){GD.talk=false;stopSpeak();}else{GD.talk=true;speak(guideText(p,Math.min(GD.idx,p.steps.length)));}break;
  case 'tool':closeModal();$(a.dataset.t).scrollIntoView({behavior:'smooth'});break;
  case 'copy':try{await navigator.clipboard.writeText(buildText());toast('تم نسخ النص');}catch(_){const ta=document.createElement('textarea');ta.value=buildText();document.body.appendChild(ta);ta.select();try{document.execCommand('copy');toast('تم نسخ النص')}catch(__){toast('تعذر النسخ')}ta.remove();}break;
  case 'share':navigator.share({title:p.title,text:buildText()}).catch(()=>{});break;
  case 'print':window.print();break;
  case 'png':try{toast('جارٍ إنشاء الصورة…');await loadH2C();const c=await html2canvas($('cardEl'),{scale:2,useCORS:true,backgroundColor:getComputedStyle(document.body).backgroundColor});const l=document.createElement('a');l.download=p.title.replace(/[\\/:*?"<>|()]/g,'').trim()+'.png';l.href=c.toDataURL('image/png');l.click();toast('تم حفظ الصورة');}catch(_){h2c=null;toast('حفظ الصورة يحتاج اتصالاً بالإنترنت مرة واحدة');}break;
  case 'sharesite':shareSite();break;
  case 'copylink':{const u=shareUrl(p.id)||cardUrl(p.id);if(!u){toast('انشر الموقع أولاً، أو اكتب رابطه في قسم ملصقات QR');break;}try{await navigator.clipboard.writeText(u);toast('تم نسخ رابط البطاقة');}catch(_){toast(u);}break;}
  case 'faqshare':{const f=FAQ[Number(a.dataset.i)],V={myth:'خرافة',true:'صحيح',depends:'يعتمد'},u=shareUrl(f.card),t='❓ '+f.q+'\n'+V[f.v]+': '+f.a+(u?'\n🔗 '+u:'')+'\n— ثواني';if(navigator.share){navigator.share({title:'ثواني',text:t}).catch(()=>{});}else window.open('https://wa.me/?text='+encodeURIComponent(t),'_blank','noopener');track('faqshare/'+f.card);break;}
  case 'stats':LS.set('noStats',statsOn()?'1':'');updateStatsBtn();toast(statsOn()?'تم تشغيل الإحصاءات المجهولة':'تم إيقاف الإحصاءات المجهولة');break;
 }});
$('siteInp').addEventListener('change',e=>{LS.set('siteUrl',e.target.value.trim());renderStickers();});
document.addEventListener('change',e=>{const sk=e.target.closest('[data-stk]');if(sk){const ids=$$('[data-stk]:checked').map(x=>x.dataset.stk);LS.set('stkSel',JSON.stringify(ids));renderStickers();return;}const c=e.target.closest('[data-kitem]');if(!c)return;const ids=$$('[data-kitem]:checked').map(x=>Number(x.dataset.kitem));LS.set('thawani-kit-'+S.kit,JSON.stringify(ids));
 const k=CHECKLISTS.find(x=>x.id===S.kit),pc=Math.round(ids.length/k.items.length*100);$('kp').textContent=pc+'%';$('kb').style.width=pc+'%';});
$$('[data-fam]').forEach(i=>{i.value=LS.get('fam_'+i.dataset.fam,'');i.placeholder=FDEF[i.dataset.fam];i.addEventListener('input',()=>{LS.set('fam_'+i.dataset.fam,i.value);if(!$('modal').hidden&&GD.id&&GD.mode==='over')drawModal();});});

function updateStatsBtn(){const b=$('statsBtn');if(b)b.textContent=statsOn()?'إيقاف الإحصاءات المجهولة':'تشغيل الإحصاءات المجهولة';}
$('stkPrint').onclick=()=>{if(!$('stickerSheet').children.length){toast('اكتب رابط موقعك أولاً لتظهر الملصقات');return;}document.body.classList.add('ps');window.print();};
window.addEventListener('afterprint',()=>document.body.classList.remove('ps'));
/* ---------- profile / font size / emergency ---------- */
function applyMode(){document.documentElement.dataset.mode=LS.get('mode','full');}
function updateStars(){const n=Number(LS.get('stars','0'))||0;const e=$('starsLine');if(e)e.textContent=n?'نجومك من الاختبارات: '+n+' ★':'';}
function applyProfile(pf){LS.set('profile',pf);document.documentElement.dataset.pf=pf;
 if(pf==='senior'){LS.set('mode','simple');LS.set('autoread','1');if(Number(LS.get('scale','1'))<1.25){LS.set('scale','1.25');applyScale();}}
 else{LS.set('mode','full');LS.set('autoread','');}
 if(pf==='child')S.kidsAll=false;applyMode();renderAll();updateStars();
 toast(pf==='child'?'تم ضبط ثواني لك':pf==='senior'?'تم ضبط عرض مبسّط وخط أكبر وقراءة بالصوت':'تم ضبط العرض الكامل');}
$('emBtn').onclick=openEmergency;$('scriptBtn').onclick=openScript;
$('fullBtn').onclick=()=>{LS.set('mode','full');applyMode();renderAll();};
$('kidsAll').onclick=()=>{S.kidsAll=true;renderGrid();};
$('profSkip').onclick=()=>$('profDlg').close();

/* ---------- PWA: status, install, SW, scroll spy ---------- */
function updateNet(){const n=$('net'),off=!navigator.onLine;n.classList.toggle('off',off);n.querySelector('span').textContent=off?'تعمل بدون إنترنت':(S.swReady?'جاهز للعمل دون اتصال':'يعمل دون اتصال بعد الزيارة الأولى');}
window.addEventListener('online',updateNet);window.addEventListener('offline',updateNet);
window.addEventListener('beforeinstallprompt',e=>{e.preventDefault();S.deferred=e;$('installBtn').hidden=false;});
$('installBtn').onclick=async()=>{if(!S.deferred)return;S.deferred.prompt();await S.deferred.userChoice;S.deferred=null;$('installBtn').hidden=true;};
window.addEventListener('appinstalled',()=>{$('installBtn').hidden=true;toast('تم تثبيت ثواني على جهازك.');});
if('serviceWorker' in navigator&&/^https?:$/.test(location.protocol))navigator.serviceWorker.register('sw.js').then(()=>navigator.serviceWorker.ready).then(()=>{S.swReady=true;updateNet();}).catch(()=>{});
if('IntersectionObserver' in window){const ids=['home','browse','checklists'],io=new IntersectionObserver(es=>{es.forEach(en=>{if(en.isIntersecting){const id=en.target.id;$$('[data-spy]').forEach(a=>a.classList.toggle('cur',a.dataset.spy===id));$$('.mobile-nav a[href^="#"]').forEach(a=>a.classList.toggle('cur',a.getAttribute('href')==='#'+id));}});},{rootMargin:'-45% 0px -50% 0px'});ids.forEach(i=>{const el=$(i);if(el)io.observe(el);});}
if('speechSynthesis' in window)try{speechSynthesis.getVoices();}catch(e){}

/* ---------- start ---------- */
document.documentElement.dataset.pf=profile();applyMode();applyTheme();applyScale();updateStars();updateStatsBtn();renderNums();renderRefs();renderFaq();renderStickers();renderAll();updateNet();tShow();cprDraw();
loadLangs();
const want=new URLSearchParams(location.search).get('card');if(want&&byId(want))openCard(want);
else if(!profile()&&$('profDlg').showModal)setTimeout(()=>{try{$('profDlg').showModal();}catch(e){}},600);
})();
