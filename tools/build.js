/* يبني مجلد dist للنشر: SITE_URL=https://موقعك node tools/build.js
   - ينسخ الملفات، ويكتب صفحات المشاركة share/<id>.html بمعاينة Open Graph، ويضبط الرابط في config.js وindex.html */
const fs=require('fs'),path=require('path');
const root=path.join(__dirname,'..'),dist=path.join(root,'dist');
const SITE=(process.env.SITE_URL||'https://thawani.app').replace(/\/+$/,'');
const src=fs.readFileSync(path.join(root,'data.js'),'utf8');
const {PRESETS}=new Function(src+';return {PRESETS}')();
fs.rmSync(dist,{recursive:true,force:true});fs.mkdirSync(dist,{recursive:true});
const cp=(a,b)=>{fs.mkdirSync(path.dirname(path.join(dist,b||a)),{recursive:true});fs.copyFileSync(path.join(root,a),path.join(dist,b||a));};
['styles.css','app.js','data.js','illus.js','manifest.webmanifest','sw.js'].forEach(f=>cp(f));
cp('vendor/qrcode.js');
['icon.svg','icon-maskable.svg','icon-192.png','icon-512.png','icon-maskable-512.png','apple-touch-icon.png'].forEach(f=>cp('icons/'+f));
if(fs.existsSync(path.join(root,'i18n')))fs.readdirSync(path.join(root,'i18n')).filter(f=>/^(ur|fil|bn)\.json$/.test(f)).forEach(f=>cp('i18n/'+f));
let idx=fs.readFileSync(path.join(root,'index.html'),'utf8').replace(/__SITE__/g,SITE).replace('<meta property="og:site_name" content="ثواني">','<meta property="og:site_name" content="ثواني">\n<meta property="og:url" content="'+SITE+'/">');
fs.writeFileSync(path.join(dist,'index.html'),idx);
let cfg=fs.readFileSync(path.join(root,'config.js'),'utf8').replace("site:''","site:'"+SITE+"'");
fs.writeFileSync(path.join(dist,'config.js'),cfg);
const esc=s=>String(s).replace(/[&<>"]/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;'}[c]));
fs.mkdirSync(path.join(dist,'share/img'),{recursive:true});
const imgDir=path.join(root,'share','img');
if(fs.existsSync(imgDir))fs.readdirSync(imgDir).forEach(f=>fs.copyFileSync(path.join(imgDir,f),path.join(dist,'share/img',f)));
for(const p of PRESETS){
 const desc=(p.level+' — '+p.golden).slice(0,170),url=SITE+'/share/'+p.id+'.html',img=SITE+'/share/img/'+p.id+'.jpg';
 fs.writeFileSync(path.join(dist,'share',p.id+'.html'),`<!doctype html>
<html lang="ar" dir="rtl"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>${esc(p.title)} | ثواني</title><meta name="description" content="${esc(desc)}">
<link rel="canonical" href="${url}">
<meta property="og:type" content="article"><meta property="og:locale" content="ar_SA"><meta property="og:site_name" content="ثواني">
<meta property="og:title" content="${esc(p.title)}"><meta property="og:description" content="${esc(desc)}">
<meta property="og:url" content="${url}"><meta property="og:image" content="${img}"><meta property="og:image:width" content="1200"><meta property="og:image:height" content="630">
<meta name="twitter:card" content="summary_large_image"><meta name="twitter:image" content="${img}">
<meta http-equiv="refresh" content="0;url=../?card=${p.id}">
<style>body{font-family:system-ui,sans-serif;background:#F7F7F3;color:#0F2A22;margin:0;display:grid;place-items:center;min-height:100vh;padding:24px;text-align:center}a{color:#087E68;font-weight:700}</style></head>
<body><main><h1>${esc(p.title)}</h1><p>${esc(p.level)}</p><p><a href="../?card=${p.id}">افتح البطاقة في ثواني</a> · <a href="tel:${p.phone}">اتصل ${p.phone}</a></p></main>
<script>location.replace('../?card=${p.id}')</script></body></html>`);
}
fs.writeFileSync(path.join(dist,'_headers'),`/sw.js
  Cache-Control: no-cache
/index.html
  Cache-Control: no-cache
/manifest.webmanifest
  Content-Type: application/manifest+json
  Cache-Control: no-cache
/config.js
  Cache-Control: no-cache
/share/*
  Cache-Control: public, max-age=3600
/*
  X-Content-Type-Options: nosniff
  Referrer-Policy: strict-origin-when-cross-origin
  Permissions-Policy: geolocation=(self), microphone=(self)
`);
console.log('dist ready for',SITE,'with',PRESETS.length,'share pages');
