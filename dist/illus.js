/* رسوم توضيحية SVG لخطوات البطاقات الحرجة (تعمل دون إنترنت): ILLUS[id][خطوة-1] */
(function(){
const A=(x1,y1,x2,y2,c='arr')=>{const a=Math.atan2(y2-y1,x2-x1),l=9,b=.5;return `<path class="${c}" d="M${x1} ${y1}L${x2} ${y2}M${x2} ${y2}l${(-l*Math.cos(a-b)).toFixed(1)} ${(-l*Math.sin(a-b)).toFixed(1)}M${x2} ${y2}l${(-l*Math.cos(a+b)).toFixed(1)} ${(-l*Math.sin(a+b)).toFixed(1)}"/>`;};
const T=(x,y,t,x2='')=>`<text x="${x}" y="${y}" ${x2}>${t}</text>`;
const W=(inner,label)=>`<svg class="il" viewBox="0 0 220 150" role="img" aria-label="${label}"><rect class="bgc" width="220" height="150" rx="14"/>${inner}</svg>`;
const lying=(x,y)=>`<rect class="sh2" x="${x+16}" y="${y}" width="86" height="22" rx="9"/><circle class="sk" cx="${x+8}" cy="${y+11}" r="12"/><rect class="pt" x="${x+98}" y="${y+2}" width="58" height="18" rx="8"/>`;
const phone=(x,y,t)=>`<rect fill="#243153" x="${x}" y="${y}" width="42" height="70" rx="8"/><rect class="wh" x="${x+4}" y="${y+9}" width="34" height="50" rx="4"/>${T(x+21,y+39,t,'style="font-size:13px;font-weight:800"')}`;
const heart=(x,y,s=1)=>`<path class="rd" transform="translate(${x} ${y}) scale(${s})" d="M12 21s-7.500-4.600-9.600-9.200C.9 8.400 3 5 6.400 5c2 0 3.600 1.100 4.600 2.700h2C14 6.100 15.600 5 17.600 5 21 5 23.100 8.400 21.600 11.800 19.500 16.400 12 21 12 21z"/>`;

/* ===== الإنعاش القلبي الرئوي p3 ===== */
const cpr=[
 W(lying(30,96)+`<circle class="sk" cx="78" cy="86" r="7"/><path class="ln" d="M68 76l-4-7M78 73v-9M88 76l4-7"/><rect class="wh" x="84" y="14" width="112" height="32" rx="14"/>${T(140,35,'هل تسمعني؟')}${T(110,142,'تحقق من التنفس 10 ثوانٍ فقط')}`,'هز كتفي المصاب وناده وتحقق من تنفسه'),
 W(phone(28,38,'997')+`<rect class="wh" x="116" y="46" width="76" height="58" rx="12"/><path class="rd" d="M158 54l-12 20h10l-4 18 16-24h-10z"/>${T(154,118,'جهاز AED')}${A(76,72,110,72)}`,'اتصل بـ997 واطلب جهاز الصدمات'),
 W(`<rect class="sh2" x="72" y="38" width="76" height="92" rx="22"/><circle class="sk" cx="110" cy="26" r="13"/><circle fill="none" stroke="var(--red)" stroke-width="2.500" cx="110" cy="78" r="17"/><circle class="rd" cx="110" cy="78" r="5"/><rect class="sk" x="94" y="70" width="32" height="15" rx="7.500"/><rect class="sk" x="98" y="76" width="32" height="15" rx="7.500" opacity=".85"/>${T(176,80,'منتصف','style="font-size:10px"')}${T(176,93,'الصدر','style="font-size:10px"')}${A(160,86,128,80)}`,'ضع كعب اليد في منتصف الصدر وشبّك اليد الأخرى'),
 W(lying(20,114)+`<g class="press"><circle class="sk" cx="106" cy="30" r="10"/><path d="M106 42L102 76" stroke="var(--acc)" stroke-width="17" stroke-linecap="round"/><path d="M106 52L104 110" stroke="#E8B48C" stroke-width="6" stroke-linecap="round"/></g><path d="M102 76L124 98L158 104" stroke="#33405F" stroke-width="12" stroke-linecap="round" stroke-linejoin="round" fill="none"/>${A(198,92,198,116)}${T(196,82,'110 / د','style="font-size:10px"')}${T(196,132,'5–6 سم','style="font-size:10px"')}`,'ذراعان مستقيمتان وضغط بوزن الجسم بمعدل 110 في الدقيقة'),
 W(`<circle cx="70" cy="72" r="38" fill="var(--card)" stroke="var(--ink)" stroke-width="3"/><path class="ln" d="M70 72V48M70 72l18 10"/><circle class="rd" cx="70" cy="72" r="4"/>${A(122,52,160,52)}${A(160,72,122,72)}${A(122,92,160,92)}${T(150,120,'بلا توقف')}${T(150,134,'حتى وصول المسعفين','style="font-size:10px"')}`,'استمر بالضغط دون توقف')
];

/* ===== الغصة للبالغين p1 ===== */
const choke=[
 W(`<circle class="sk" cx="92" cy="38" r="12"/><rect class="sh2" x="76" y="52" width="34" height="64" rx="14" transform="rotate(10 93 84)"/><circle class="sk" cx="132" cy="30" r="12"/><rect class="sh" x="116" y="44" width="38" height="72" rx="14"/><path d="M124 80Q90 78 98 94" stroke="#E8B48C" stroke-width="8" fill="none" stroke-linecap="round"/>${T(110,138,'قف خلفه وأمِل جذعه للأمام')}`,'قف خلف المصاب ولف ذراعيك حول خصره'),
 W(`<rect class="sh2" x="72" y="18" width="76" height="112" rx="26"/><circle fill="var(--card)" cx="110" cy="100" r="3.500"/><circle class="sk" cx="110" cy="70" r="13"/>${T(110,74,'قبضة','style="font-size:8px"')}<path class="ln" d="M148 70v30M144 70h8M144 100h8"/>${T(188,88,'إصبعان','style="font-size:10px"')}${T(110,144,'فوق السرة، أسفل عظمة القص','style="font-size:10px"')}`,'ضع القبضة فوق السرة بمسافة إصبعين'),
 W(`<rect class="sh2" x="72" y="18" width="76" height="112" rx="26"/><circle class="sk" cx="110" cy="92" r="12"/><path class="arr draw" d="M152 108C130 108 114 104 110 78"/><path class="arr" d="M103 86l7-9 7 9"/>${T(190,100,'للداخل','style="font-size:10px"')}${T(190,113,'وللأعلى','style="font-size:10px"')}`,'ضغطات سريعة للداخل وللأعلى بحركة حرف J'),
 W(`${[0,1,2,3,4].map(i=>`<circle class="rd pulse" style="animation-delay:${i*.15}s" cx="${50+i*30}" cy="62" r="10"/>${T(50+i*30,66,i+1,'style="fill:#fff;font-size:11px"')}`).join('')}${T(110,112,'كرر 5 ضغطات متتالية')}${T(110,128,'حتى يخرج الجسم الغريب','style="font-size:10px"')}`,'كرر الضغطات خمس مرات'),
 W(lying(18,92)+phone(150,30,'997')+`<rect class="rd" x="76" y="30" width="66" height="26" rx="13"/>${T(109,48,'CPR','style="fill:#fff;font-size:13px;font-weight:800"')}`,'إذا فقد الوعي: أنزله وعلى الأرض واتصل بـ997 وابدأ الإنعاش')
];

/* ===== النزيف الشرياني p2 ===== */
const bleed=[
 W(`<rect class="sk" x="24" y="64" width="124" height="28" rx="14"/><ellipse class="rd" cx="86" cy="78" rx="14" ry="10"/>${phone(164,30,'997')}${T(86,118,'اكشف الجرح واتصل بـ997')}`,'اكشف الجرح واتصل بالإسعاف'),
 W(`<rect class="sk" x="20" y="58" width="150" height="28" rx="14"/><ellipse class="rd" cx="52" cy="72" rx="13" ry="9"/><rect class="rd" x="112" y="50" width="14" height="44" rx="4"/><path class="ln" d="M65 108h47M65 103v10M112 103v10"/>${T(88,126,'5–7 سم','style="font-size:12px"')}${heart(176,52,1.300)}${A(166,112,176,86)}${T(112,40,'أعلى الجرح')}`,'ضع العاصبة أعلى الجرح بـ 5 إلى 7 سم'),
 W(`<rect class="sk" x="20" y="60" width="130" height="30" rx="15"/><rect class="rd" x="76" y="52" width="16" height="46" rx="4"/><path d="M80 44l26-22" stroke="var(--ink)" stroke-width="5" stroke-linecap="round"/><path class="arr" d="M110 20a18 18 0 0 1 16 14"/><path class="ln" d="M20 120h32l6-14 8 24 6-14h22"/><path class="arr" d="M64 106l30 26M94 106l-30 26" opacity=".9"/>${T(170,120,'النبض يختفي','style="font-size:10px"')}`,'شد العاصبة حتى يتوقف النزيف ويختفي النبض'),
 W(`<rect class="wh" x="40" y="46" width="140" height="44" rx="10" stroke="var(--ink)" stroke-width="2.500"/>${T(110,78,'14:35','style="font-size:24px;font-weight:800"')}<path class="rd" d="M168 108l10-26 8 3-10 26z"/>${T(110,126,'سجّل وقت التركيب')}`,'اكتب وقت تركيب العاصبة'),
 W(`<circle class="sk" cx="110" cy="26" r="13"/><rect class="sh2" x="80" y="42" width="60" height="86" rx="20"/><circle class="rd" cx="110" cy="44" r="5"/><circle class="rd" cx="82" cy="62" r="5"/><circle class="rd" cx="98" cy="112" r="5"/><rect class="sk" x="150" y="62" width="40" height="15" rx="7.500"/><rect class="sk" x="150" y="80" width="40" height="15" rx="7.500"/>${A(150,70,114,50)}${T(176,116,'3–5 دقائق','style="font-size:11px"')}${T(110,144,'للرقبة أو الإبط أو الفخذ','style="font-size:10px"')}`,'احشُ الجرح بالشاش واضغط بكلتا اليدين')
];

/* ===== غصة الرضيع p14 ===== */
const infant=[
 W(`<rect class="sk" x="26" y="86" width="128" height="16" rx="8"/><rect class="sh" x="62" y="64" width="62" height="22" rx="11"/><circle class="sk" cx="52" cy="90" r="11"/><circle class="sk" cx="40" cy="98" r="9"/>${T(110,130,'الرأس أخفض من الجسم')}${T(110,30,'منبطح على الساعد')}`,'ضع الرضيع منبطحاً على ساعدك مع إسناد الفك'),
 W(`<rect class="sk" x="26" y="92" width="128" height="16" rx="8"/><rect class="sh" x="62" y="70" width="62" height="22" rx="11"/><circle class="sk" cx="52" cy="96" r="11"/><circle class="sk" cx="106" cy="28" r="9"/>${A(106,40,106,64)}${T(160,40,'5 ضربات','style="font-size:11px"')}${T(160,54,'بين لوحي الكتف','style="font-size:10px"')}`,'خمس ضربات بين لوحي الكتف'),
 W(`<rect class="sk" x="26" y="92" width="128" height="16" rx="8"/><rect class="sh" x="62" y="70" width="62" height="22" rx="11"/><circle class="sk" cx="52" cy="96" r="11"/><path class="arr" d="M150 56a34 34 0 0 0-70-6"/>${T(110,134,'اقلبه على ظهره والرأس منخفض')}`,'اقلب الرضيع على ظهره'),
 W(`<rect class="sk" x="26" y="94" width="128" height="16" rx="8"/><rect class="sh" x="62" y="66" width="70" height="30" rx="12"/><path class="ln" d="M62 78h70" stroke-dasharray="3 3"/><rect class="sk" x="96" y="30" width="7" height="30" rx="3.500"/><rect class="sk" x="106" y="30" width="7" height="30" rx="3.500"/>${A(104,26,104,56)}${T(162,60,'4 سم','style="font-size:11px"')}${T(110,130,'إصبعان تحت خط الحلمتين، 5 ضغطات')}`,'خمس ضغطات صدرية بإصبعين'),
 W(`<path class="arr" d="M70 40a28 28 0 1 1-4 36"/><path class="arr" d="M150 110a28 28 0 1 1 4-36"/>${T(64,102,'5 ظهر','style="font-size:11px"')}${T(158,52,'5 صدر','style="font-size:11px"')}${phone(88,44,'997')}`,'كرر الدورة واتصل بـ997')
];
window.ILLUS={p3:cpr,p1:choke,p2:bleed,p14:infant};
window.illus=(id,i)=>{const a=window.ILLUS[id];return a&&a[i]||'';};
})();
