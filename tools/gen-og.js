/* يولّد صور المعاينة (Open Graph) لكل بطاقة: node tools/gen-og.js
   يحتاج Playwright (npm i playwright). الصور تُحفظ في share/img/ وتُرفع مع المشروع. */
const fs=require('fs'),path=require('path');
const {chromium}=require('playwright');
const root=path.join(__dirname,'..');
const src=fs.readFileSync(path.join(root,'data.js'),'utf8');
const {PRESETS,CATS}=new Function(src+';return {PRESETS,CATS}')();
const col={c:'#B13A2E',d:'#8E1B2B',w:'#B7791F',t:'#3F67A0',p:'#3F7A5E'};
const page=(title,sub,phone,cat,lv)=>`<!doctype html><html lang="ar" dir="rtl"><head><meta charset="utf-8">
<link href="https://fonts.googleapis.com/css2?family=IBM+Plex+Sans+Arabic:wght@400;600&family=Readex+Pro:wght@600;700&display=swap" rel="stylesheet">
<style>*{box-sizing:border-box}body{margin:0;width:1200px;height:630px;background:#F7F7F3;font-family:'IBM Plex Sans Arabic',sans-serif;color:#0F2A22;position:relative;overflow:hidden}
.bar{position:absolute;inset-inline:0;top:0;height:18px;background:${col[lv]||'#087E68'}}
.wrap{position:absolute;inset:60px 70px 50px;display:flex;flex-direction:column;justify-content:space-between}
.brand{display:flex;align-items:center;gap:16px;font-family:'Readex Pro';font-weight:700;font-size:44px}
.mark{width:68px;height:68px;border-radius:20px;background:#087E68;color:#fff;display:grid;place-items:center;font-size:42px}
.cat{color:#087E68;font-weight:600;font-size:30px}
h1{font-family:'Readex Pro';font-weight:700;font-size:${title.length>46?58:70}px;line-height:1.3;margin:10px 0 14px;max-width:1000px}
p{font-size:34px;color:#5F6B66;margin:0;max-width:980px;line-height:1.5}
.foot{display:flex;align-items:center;justify-content:space-between}
.call{display:flex;align-items:center;gap:18px;background:#D0392B;color:#fff;border-radius:22px;padding:10px 34px;font-weight:600;font-size:34px}
.call b{font-family:'Readex Pro';font-size:64px}
.tag{font-size:28px;color:#5F6B66}</style></head><body><div class="bar"></div><div class="wrap">
<div class="brand"><span class="mark">ث</span><span>ثواني.</span></div>
<div><div class="cat">${cat}</div><h1>${title}</h1><p>${sub}</p></div>
<div class="foot"><div class="call"><span>اتصل الآن</span><b>${phone}</b></div><div class="tag">خطوات بسيطة، لوقت لا يحتمل الانتظار</div></div></div></body></html>`;
(async()=>{
 const b=await chromium.launch();const p=await b.newPage({viewport:{width:1200,height:630}});
 const out=path.join(root,'share','img');fs.mkdirSync(out,{recursive:true});
 const items=[{id:'default',t:'دليل الطوارئ',s:'47 بطاقة للإسعافات الأولية وطوارئ المنزل والطريق والبر، خطوة بخطوة وبالصوت.',ph:'997',c:'بطاقات طوارئ بالعربية',lv:'p'}]
  .concat(PRESETS.map(x=>({id:x.id,t:x.title,s:x.level,ph:x.phone,c:(CATS[x.cat]||{}).name||'',lv:x.lv})));
 for(const it of items){await p.setContent(page(it.t,it.s,it.ph,it.c,it.lv),{waitUntil:'networkidle'});await p.waitForTimeout(250);
  await p.screenshot({path:path.join(out,it.id+'.jpg'),type:'jpeg',quality:84});}
 await b.close();console.log('images',items.length);
})();
