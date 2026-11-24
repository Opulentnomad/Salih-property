/* يولّد بيانات تطبيق أندرويد (Kotlin) من data.js وapp.js وillus.js: node tools/gen-kotlin.js */
const fs=require('fs'),path=require('path');
const root=path.join(__dirname,'..');
const out=path.join(root,'android/app/src/main/java/app/thawani');
const rd=f=>fs.readFileSync(path.join(root,f),'utf8');
const D=new Function(rd('data.js')+';return {CATS,LV,PRESETS,META,CHECKLISTS,SEASONS,FAQ,EN,KIDS_IDS,PLACES,CTX_DEF}')();
const appjs=rd('app.js');
const refsSrc=appjs.slice(appjs.indexOf('const REFS=['),appjs.indexOf('function renderRefs'));
const REFS=new Function(refsSrc+';return REFS')();
const numsSrc=appjs.slice(appjs.indexOf('const NUMS=['),appjs.indexOf('function renderNums'));
const NUMS=new Function(numsSrc+';return NUMS')();
const fakeWin={};new Function('window',rd('illus.js'))(fakeWin);
const q=s=>'"'+String(s).replace(/\\/g,'\\\\').replace(/"/g,'\\"').replace(/\$/g,'\\$').replace(/\n/g,'\\n')+'"';
const L=a=>'listOf('+a.map(q).join(',')+')';
const LK=a=>'listOf('+(a||[]).map(l=>`Link(${q(l[0])},${q(l[1])})`).join(',')+')';
const head='package app.thawani\n\n';
/* cards in chunks */
const cards=D.PRESETS.map(p=>{const m=D.META[p.id]||{};
 const ctx=m.ctx||D.CTX_DEF[p.cat]||D.PLACES;
 return `Card(${q(p.id)},${q(p.cat)},${q(p.title)},${q(p.phone)},${q(p.level)},${q(p.lv)},${q(p.golden)},${L(p.steps)},${L(p.avoid)},${L(p.gear)},${L(ctx)},${LK(m.refs)},${LK(m.vids)})`;});
const chunk=6,n=Math.ceil(cards.length/chunk);
for(let i=0;i<n;i++){
 fs.writeFileSync(path.join(out,`DataCards${i+1}.kt`),head+`internal fun cards${i+1}(): List<Card> = listOf(\n${cards.slice(i*chunk,(i+1)*chunk).join(',\n')}\n)\n`);}
/* small tables */
const catsKt='mapOf('+Object.entries(D.CATS).map(([k,v])=>`${q(k)} to ${q(v.name)}`).join(',')+')';
const lvKt='mapOf('+Object.entries(D.LV).map(([k,v])=>`${q(k)} to ${q(v)}`).join(',')+')';
const kits=D.CHECKLISTS.map(k=>`Checklist(${q(k.id)},${q(k.title)},${q(k.note)},${q(k.refTitle)},${q(k.refUrl)},${L(k.items)})`).join(',\n');
const seasons=D.SEASONS.map(s=>`Season(${q(s.id)},${q(s.name)},${q(s.d)},${L(s.ids)})`).join(',\n');
const faq=D.FAQ.map(f=>`Faq(${q(f.q)},${q(f.v)},${q(f.a)},${q(f.card)})`).join(',\n');
const en=Object.entries(D.EN).map(([id,e])=>`${q(id)} to EnCard(${q(e.title)},${q(e.level)},${q(e.golden)},${L(e.steps)},${L(e.avoid)})`).join(',\n');
const refs=REFS.map(([g,it])=>`RefGroup(${q(g)},listOf(${it.map(r=>`RefItem(${q(r[0])},${q(r[1])},${q(r[2])},${q(r[3])})`).join(',')}))`).join(',\n');
const nums=NUMS.map(x=>`Num(${q(x[0])},${q(x[1])},${q(x[2])})`).join(',');
fs.writeFileSync(path.join(out,'DataMisc.kt'),head+`internal fun catsMap(): Map<String,String> = ${catsKt}
internal fun lvMap(): Map<String,String> = ${lvKt}
internal fun placesList(): List<String> = ${L(D.PLACES)}
internal fun kidsIds(): List<String> = ${L(D.KIDS_IDS)}
internal fun checklists(): List<Checklist> = listOf(\n${kits}\n)
internal fun seasons(): List<Season> = listOf(\n${seasons}\n)
internal fun faqs(): List<Faq> = listOf(\n${faq}\n)
internal fun enCards(): Map<String,EnCard> = mapOf(\n${en}\n)
internal fun refGroups(): List<RefGroup> = listOf(\n${refs}\n)
internal fun numbers(): List<Num> = listOf(${nums})
`);
/* illustrations: resolve classes to presentation attributes so AndroidSVG can draw them */
const cls={bgc:'fill="#E8F4F0"',sk:'fill="#E8B48C"',sh:'fill="#087E68"',sh2:'fill="#5E7FB5"',pt:'fill="#33405F"',rd:'fill="#C5483A"',wh:'fill="#FFFFFF"',
 ln:'stroke="#0F2A22" stroke-width="2.2" fill="none" stroke-linecap="round"',arr:'stroke="#C5483A" stroke-width="3" fill="none" stroke-linecap="round" stroke-linejoin="round"',
 press:'',draw:'',pulse:'',ink:''};
function resolve(svg){
 svg=svg.replace(/var\(--card\)/g,'#FFFFFF').replace(/var\(--ink\)/g,'#0F2A22').replace(/var\(--red\)/g,'#C5483A').replace(/var\(--acc\)/g,'#087E68');
 svg=svg.replace(/<text /g,'<text fill="#0F2A22" font-size="12" font-weight="bold" text-anchor="middle" ');
 svg=svg.replace(/<svg class="[^"]*"/,'<svg xmlns="http://www.w3.org/2000/svg"');
 svg=svg.replace(/class="([^"]*)"/g,(m,c)=>c.split(/\s+/).map(x=>cls[x]).filter(Boolean).join(' '));
 return svg;
}
const il=[];for(const [id,arr] of Object.entries(fakeWin.ILLUS))arr.forEach((s,i)=>il.push(`Triple(${q(id)},${i},${q(resolve(s))})`));
fs.writeFileSync(path.join(out,'DataIllus.kt'),head+`internal fun illusList(): List<Triple<String,Int,String>> = listOf(\n${il.join(',\n')}\n)\n`);
console.log('cards',cards.length,'files',n,'illus',il.length,'refs',REFS.length,'faq',D.FAQ.length);
