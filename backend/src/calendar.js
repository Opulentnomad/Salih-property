// تقويم المواسم السعودية: التواريخ الهجرية تُحسب من تقويم أم القرى (ICU) لا تُكتب يدويًا.
// الإعلان الرسمي (رؤية الهلال/وزارة التعليم) قد يخالفها بيوم أو أكثر، لذلك تبقى «غير مؤكدة» حتى يؤكدها محرر.
const HIJRI_MONTHS = ['محرم', 'صفر', 'ربيع الأول', 'ربيع الآخر', 'جمادى الأولى', 'جمادى الآخرة', 'رجب', 'شعبان', 'رمضان', 'شوال', 'ذو القعدة', 'ذو الحجة'];
const fmt = new Intl.DateTimeFormat('en-US-u-ca-islamic-umalqura-nu-latn', { day: 'numeric', month: 'numeric', year: 'numeric', timeZone: 'UTC' });
const AR = (n) => String(n).replace(/\d/g, (d) => '٠١٢٣٤٥٦٧٨٩'[d]);
const iso = (t) => new Date(t).toISOString().slice(0, 10);
const DAY = 864e5;

function hijriParts(t) {
  const p = Object.fromEntries(fmt.formatToParts(new Date(t)).map((x) => [x.type, x.value]));
  return { y: +p.year, m: +p.month, d: +p.day };
}
export const hijriLabel = (t) => { const h = hijriParts(t); return `${AR(h.d)} ${HIJRI_MONTHS[h.m - 1]} ${AR(h.y)}`; };

/** أحداث السنة الميلادية: رمضان، عيد الفطر، عيد الأضحى، يوم التأسيس، اليوم الوطني. */
export function computeEvents(year) {
  const days = [];
  for (let t = Date.UTC(year, 0, 1, 12); t < Date.UTC(year + 1, 0, 1, 12); t += DAY) days.push([t, hijriParts(t)]);
  const find = (m, d) => days.find(([, h]) => h.m === m && h.d === d)?.[0];
  const out = [];
  const ram = find(9, 1), shaw = find(10, 1), adha = find(12, 10);
  if (ram) out.push({ key: `ramadan-${year}`, name_ar: 'شهر رمضان', kind: 'religious', start: ram, end: (shaw ?? ram + 29 * DAY) - DAY, note: 'تبدأ الإجازات والسفر عادةً مع نهاية الشهر. الأسعار ترتفع في العشر الأواخر.' });
  if (shaw) out.push({ key: `eid-fitr-${year}`, name_ar: 'عيد الفطر', kind: 'religious', start: shaw, end: shaw + 2 * DAY, note: 'الإجازة الرسمية غالبًا أطول من أيام العيد وتُعلن قبله، فتحقق من إعلان الجهة الرسمية.' });
  if (adha) out.push({ key: `eid-adha-${year}`, name_ar: 'عيد الأضحى', kind: 'religious', start: adha, end: adha + 3 * DAY, note: 'الإجازة الرسمية غالبًا أطول من أيام العيد وتُعلن قبله. موسم الحج قبله بأيام.' });
  out.push({ key: `founding-${year}`, name_ar: 'يوم التأسيس', kind: 'national', start: Date.UTC(year, 1, 22, 12), end: Date.UTC(year, 1, 22, 12), note: 'إجازة رسمية ثابتة في 22 فبراير، وقد تُرحَّل إن وافقت نهاية الأسبوع.' });
  out.push({ key: `national-${year}`, name_ar: 'اليوم الوطني', kind: 'national', start: Date.UTC(year, 8, 23, 12), end: Date.UTC(year, 8, 23, 12), note: 'إجازة رسمية ثابتة في 23 سبتمبر، وقد تُرحَّل إن وافقت نهاية الأسبوع.' });
  return out.map((e) => ({ ...e, start_date: iso(e.start), end_date: iso(e.end), hijri: e.kind === 'religious' ? hijriLabel(e.start) : null }));
}

/** يضيف أحداث السنوات المطلوبة إن لم تكن موجودة (لا يعدّل ما حرّره محرر). */
export function seedCalendar(db, years = [0, 1, 2].map((i) => new Date().getUTCFullYear() + i)) {
  const ins = db.prepare(`INSERT OR IGNORE INTO saudi_events(key,name_ar,kind,start_date,end_date,hijri,note,source,confirmed) VALUES(?,?,?,?,?,?,?,'umalqura-computed',?)`);
  let n = 0;
  for (const y of years) for (const e of computeEvents(y)) n += Number(ins.run(e.key, e.name_ar, e.kind, e.start_date, e.end_date, e.hijri, e.note, e.kind === 'national' ? 1 : 0).changes);
  return n;
}
