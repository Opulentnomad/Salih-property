// يولّد قائمة مراجعة الوجهات ذات الأولوية من القاعدة: docs/06-review-list.md و docs/review-list.csv
import { writeFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { dirname, join } from 'node:path';
import { openDb } from '../src/db.js';
import { seed } from '../src/seed.js';

const here = dirname(fileURLToPath(import.meta.url));
const docs = join(here, '../../docs');

const SCH = 'https://home-affairs.ec.europa.eu/policies/schengen-borders-and-visa/visa-policy_en';
// ملاحظة: الروابط جذور مواقع رسمية معروفة؛ تأكد من الصفحة الدقيقة وقت المراجعة.
const SRC = {
  ae: ['https://icp.gov.ae'], bh: ['https://www.evisa.gov.bh'], kw: ['https://www.moi.gov.kw'], qa: ['https://hia.gov.qa'], om: ['https://www.rop.gov.om'],
  tr: ['https://www.mfa.gov.tr/visa-information-for-foreigners.en.mfa', 'https://www.evisa.gov.tr'],
  eg: ['https://visa2egypt.gov.eg'], jo: ['https://mfa.gov.jo'], ge: ['https://www.geoconsul.gov.ge'], az: ['https://evisa.gov.az'],
  mv: ['https://immigration.gov.mv'], th: ['https://www.mfa.go.th', 'https://tdac.immigration.go.th'], my: ['https://www.imi.gov.my', 'https://imigresen-online.imi.gov.my/mdac/main'],
  id: ['https://evisa.imigrasi.go.id'], sg: ['https://www.ica.gov.sg'], gb: ['https://www.gov.uk/eta', 'https://www.gov.uk/check-uk-visa'],
  us: ['https://travel.state.gov/content/travel/en/us-visas.html'], ca: ['https://www.canada.ca/en/immigration-refugees-citizenship.html'], au: ['https://immi.homeaffairs.gov.au'],
  fr: ['https://france-visas.gouv.fr', SCH], de: ['https://www.auswaertiges-amt.de', SCH], it: ['https://vistoperitalia.esteri.it', SCH], es: ['https://www.exteriores.gob.es', SCH],
  gr: ['https://www.mfa.gr', SCH], nl: ['https://www.netherlandsworldwide.nl', SCH], at: ['https://www.bmeia.gv.at', SCH], pt: ['https://vistos.mne.gov.pt', SCH],
  be: ['https://diplomatie.belgium.be', SCH], ch: ['https://www.eda.admin.ch', SCH],
  jp: ['https://www.mofa.go.jp/j_info/visit/visa/index.html', 'https://www.evisa.mofa.go.jp'], kr: ['https://www.visa.go.kr', 'https://www.k-eta.go.kr'],
  cn: ['https://cs.mfa.gov.cn'], in: ['https://indianvisaonline.gov.in/evisa'], lk: ['https://www.eta.gov.lk'], ma: ['https://www.diplomatie.ma'],
  am: ['https://evisa.mfa.am'], uz: ['https://e-visa.gov.uz'], ru: ['https://electronic-visa.kdmid.ru'], za: ['https://www.dha.gov.za'],
};
const TIERS = [
  ['١ — ابدأ بها (الأكثر زيارة من السعوديين)', ['ae', 'bh', 'kw', 'qa', 'om', 'eg', 'jo', 'tr', 'ge', 'az', 'mv', 'th', 'my', 'id', 'gb', 'fr', 'de', 'it', 'es', 'us', 'jp']],
  ['٢ — الدفعة الثانية', ['ch', 'gr', 'nl', 'at', 'pt', 'be', 'sg', 'ma', 'tn', 'ca', 'au', 'kr', 'cn', 'in', 'lk', 'ba', 'al', 'am', 'uz', 'kz', 'ru', 'za', 'lb', 'iq']],
];
const VL = { free: 'بدون تأشيرة', gcc: 'الدخول بالهوية', voa: 'عند الوصول', evisa: 'تأشيرة إلكترونية', eta: 'ETA', required: 'تأشيرة مسبقة' };
const CHECK = {
  free: 'هل فعلًا بلا تأشيرة؟ أقصى مدة، شروط الجواز، هل يلزم تسجيل وصول إلكتروني (بطاقة وصول)؟',
  gcc: 'هل تكفي الهوية الوطنية أم الجواز؟ شروط السفر بالسيارة (تأمين، رخصة)؟ الأطفال والمرافقون؟',
  voa: 'الرسوم، شروط الأهلية، هل يلزم نموذج إلكتروني قبل الوصول؟ المنافذ التي تنطبق عليها؟',
  evisa: 'رابط الطلب الرسمي، الرسوم، مدة الاستخراج، صلاحية التأشيرة وعدد الدخلات.',
  eta: 'رابط التصريح الرسمي، الرسوم، مدة الصلاحية، هل يُطلب قبل الرحلة بكم ساعة؟',
  required: 'السفارة أو مركز الطلبات المعتمد، المستندات (تأمين، كشف حساب، حجوزات)، مدة المعالجة، الرسوم.',
};
const esc = (s) => `"${String(s ?? '').replace(/"/g, '""')}"`;

const db = openDb(':memory:');
await seed(db);
const get = db.prepare(`SELECT d.id, d.name_ar, d.name_en, r.visa, r.stay_days, r.reviewed FROM destinations d LEFT JOIN entry_requirements r ON r.destination_id=d.id AND r.passport_country='SA' AND r.status='published' WHERE d.id=?`);

let md = `# قائمة مراجعة الوجهات ذات الأولوية

> مولَّدة آليًا من القاعدة بتاريخ ${new Date().toISOString().slice(0, 10)} (\`cd backend && npm run review-list\`). القيمة الحالية مصدرها Passport Index (فبراير 2026) وهي **غير موثّقة رسميًا**؛ مهمتك أن تؤكدها أو تصححها.

## طريقة المراجعة لكل وجهة (٥–١٠ دقائق)
1. افتح المصدر الرسمي من العمود الأخير، وابحث عن متطلبات **حامل جواز المملكة العربية السعودية** تحديدًا.
2. قارن مع مصدر ثانٍ مستقل: [IATA Travel Centre](https://www.iatatravelcentre.com) (قائم على Timatic) أو موقع سفارة الدولة في الرياض.
3. افتح \`admin.html\` ← «بانتظار المراجعة» ← ابحث بالاسم ← **تحرير** ← صحّح الحقول ← «حفظ كمسودة».
4. يفتح **مراجع آخر** (غير الكاتب) الإصدار وينشره. بعد النشر تظهر الوجهة «راجعها محرر» ولا يستبدلها أي استيراد لاحق.
5. دوّن تاريخ المراجعة في عمود الملاحظات.

## تحقق من هذه الحقول في كل وجهة
نوع الدخول · أقصى مدة · صلاحية الجواز المطلوبة · المستندات · التطعيمات · الحجر · هاتف وعنوان سفارتها في الرياض · رابط المصدر.

## تحذيرات السفر السعودية (تُفحص لكل وجهة)
بيانات التأشيرات لا تشمل **قيود السفر الصادرة من الجهات السعودية** على بعض الوجهات. راجع تعليمات وزارة الخارجية السعودية ([mofa.gov.sa](https://www.mofa.gov.sa)) ووزارة الداخلية ([moi.gov.sa](https://www.moi.gov.sa)) قبل اعتماد أي وجهة، خصوصًا الدول المضطربة أمنيًا. للوجهات التي عليها تحذير أضف تنبيهًا واضحًا في المستندات.

`;
let csv = '﻿الأولوية,الرمز,الدولة,النوع الحالي (Passport Index),المدة الحالية,مراجَعة؟,ما يجب التحقق منه,المصادر,المراجع,تاريخ المراجعة,ملاحظات\n';
let n = 0;
for (const [title, ids] of TIERS) {
  md += `## الدفعة ${title}\n\n| ☐ | الدولة | النوع الحالي | ما يُتحقق منه | المصدر الرسمي المتوقع |\n|---|---|---|---|---|\n`;
  for (const id of ids) {
    const r = get.get(id);
    if (!r) { md += `| ☐ | ${id.toUpperCase()} | — غير موجودة في البيانات | — | — |\n`; continue; }
    n++;
    const cur = `${VL[r.visa]}${r.stay_days ? ' · ' + r.stay_days + ' يومًا' : ''}`;
    const links = (SRC[id] || []).map((u) => `[${new URL(u).hostname.replace(/^www\./, '')}](${u})`).join(' · ') || 'موقع سفارة الدولة في الرياض أو وزارة خارجيتها';
    const sch = ['fr', 'de', 'it', 'es', 'gr', 'nl', 'at', 'pt', 'be', 'ch'].includes(id) ? ' (شنقن: مركز الطلبات المعتمد في السعودية)' : '';
    md += `| ☐ | ${r.name_ar} (${id.toUpperCase()}) | ${cur} | ${CHECK[r.visa]}${sch} | ${links} |\n`;
    csv += [title.split(' ')[0], id.toUpperCase(), r.name_ar, VL[r.visa], r.stay_days ?? '', r.reviewed ? 'نعم' : 'لا', CHECK[r.visa], (SRC[id] || []).join(' | '), '', '', ''].map(esc).join(',') + '\n';
  }
  md += '\n';
}
md += `> إجمالي الوجهات في القائمة: ${n}. ملف الجدول للمراجعين: \`docs/review-list.csv\` (يُفتح في Excel).\n`;
writeFileSync(join(docs, '06-review-list.md'), md);
writeFileSync(join(docs, 'review-list.csv'), csv);
console.log('written', n);
