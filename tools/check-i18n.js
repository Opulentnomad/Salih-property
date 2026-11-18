/* يتحقق من ملف ترجمة قبل التفعيل: node tools/check-i18n.js i18n/ur.json */
const fs=require('fs'),path=require('path');
const f=process.argv[2];if(!f){console.log('usage: node tools/check-i18n.js i18n/ur.json');process.exit(1);}
const src=fs.readFileSync(path.join(__dirname,'..','data.js'),'utf8');
const {PRESETS,EN}=new Function(src+';return {PRESETS,EN}')();
const j=JSON.parse(fs.readFileSync(f,'utf8'));const bad=[];
['lang','dir','name','translator','reviewer','date'].forEach(k=>{if(!j[k])bad.push('missing field '+k);});
['call','first','steps','avoid','note'].forEach(k=>{if(!j.ui||!j.ui[k])bad.push('missing ui.'+k);});
for(const id of Object.keys(EN)){const c=j.cards&&j.cards[id],p=PRESETS.find(x=>x.id===id);
 if(!c){bad.push('missing card '+id);continue;}
 ['title','level','golden'].forEach(k=>{if(!c[k]||!c[k].trim())bad.push(id+'.'+k+' empty');});
 if(!Array.isArray(c.steps)||c.steps.length!==p.steps.length)bad.push(id+' steps count must be '+p.steps.length);
 else c.steps.forEach((s,i)=>{if(!s.trim())bad.push(id+' step '+(i+1)+' empty');});
 if(!Array.isArray(c.avoid)||c.avoid.length!==p.avoid.length)bad.push(id+' avoid count must be '+p.avoid.length);
 else c.avoid.forEach((s,i)=>{if(!s.trim())bad.push(id+' avoid '+(i+1)+' empty');});}
if(j.reviewed!==true)bad.push('reviewed is not true (set it only after a first-aid specialist signs off)');
if(bad.length){console.log('FAILED ('+bad.length+')');bad.forEach(b=>console.log(' -',b));process.exit(1);}
console.log('OK: '+f+' is complete and marked reviewed. Commit it and rebuild.');
