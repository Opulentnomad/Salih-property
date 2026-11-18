/* يولّد قالب الترجمة للبطاقات الحرجة: node tools/gen-i18n-template.js */
const fs=require('fs'),path=require('path');
const root=path.join(__dirname,'..');
const src=fs.readFileSync(path.join(root,'data.js'),'utf8');
const {PRESETS,EN}=new Function(src+';return {PRESETS,EN}')();
const ids=Object.keys(EN);
const t={lang:'ur',dir:'rtl',name:'اردو',reviewed:false,translator:'',reviewer:'',date:'',
 ui:{call:'',first:'',steps:'',avoid:'',note:''},cards:{}};
for(const id of ids){const p=PRESETS.find(x=>x.id===id);
 t.cards[id]={_ar:{title:p.title,level:p.level,golden:p.golden,steps:p.steps,avoid:p.avoid},
  title:'',level:'',golden:'',steps:p.steps.map(()=>''),avoid:p.avoid.map(()=>'')};}
fs.mkdirSync(path.join(root,'i18n'),{recursive:true});
fs.writeFileSync(path.join(root,'i18n','template.json'),JSON.stringify(t,null,1));
console.log('template for',ids.length,'cards');
