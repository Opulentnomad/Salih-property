import { readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { dirname, join } from 'node:path';
import { openDb, tx, audit } from './db.js';

const here = dirname(fileURLToPath(import.meta.url));
const DATA = join(here, '../data');
export const PI_SOURCE = 'Passport Index (passportindex.org)';
export const PI_URL = 'https://www.passportindex.org';
export const PI_DATE = '2026-02-17T00:00:00.000Z'; // تاريخ آخر تحديث لمجموعة البيانات المستوردة
const GCC = new Set(['AE', 'BH', 'KW', 'OM', 'QA']);
const REGION = { Americas: 'americas', Oceania: 'oceania', Europe: 'europe', Africa: 'africa', Asia: 'asia' };
const CAUCASUS = new Set(['GE', 'AZ', 'AM']);

const GENERIC_DOCS = {
  free: ['جواز سفر ساري', 'تذكرة عودة وإثبات إقامة (قد يُطلبان)'],
  gcc: ['الهوية الوطنية أو جواز سفر ساري'],
  voa: ['جواز سفر ساري', 'تذكرة عودة وإثبات إقامة (قد يُطلبان)', 'رسوم التأشيرة عند الوصول إن وُجدت'],
  evisa: ['جواز سفر ساري', 'تأشيرة إلكترونية تُستخرج قبل السفر'],
  eta: ['جواز سفر ساري', 'تصريح سفر إلكتروني (ETA) يُستخرج قبل السفر'],
  required: ['جواز سفر ساري', 'تأشيرة تُستخرج من سفارة الدولة أو قنصليتها قبل السفر'],
};

/** يحوّل قيمة مجموعة البيانات إلى (نوع الدخول، عدد الأيام). */
export function mapRequirement(value, iso2) {
  const v = String(value).trim().toLowerCase();
  if (/^\d+$/.test(v)) return { visa: GCC.has(iso2) ? 'gcc' : 'free', stay: Number(v) };
  if (v === 'visa free') return { visa: GCC.has(iso2) ? 'gcc' : 'free', stay: null };
  if (v === 'visa on arrival') return { visa: 'voa', stay: null };
  if (v === 'e-visa') return { visa: 'evisa', stay: null };
  if (v === 'eta') return { visa: 'eta', stay: null };
  if (v === 'visa required') return { visa: 'required', stay: null };
  return null; // -1 (نفس البلد) أو no admission أو قيمة غير معروفة: لا نستوردها
}

const hue = (s) => [...s].reduce((a, c) => (a * 31 + c.charCodeAt(0)) % 360, 7);
function hsl2hex(h, s, l) {
  s /= 100; l /= 100;
  const k = (n) => (n + h / 30) % 12, a = s * Math.min(l, 1 - l);
  const f = (n) => Math.round(255 * (l - a * Math.max(-1, Math.min(k(n) - 3, Math.min(9 - k(n), 1)))));
  return '#' + [f(0), f(8), f(4)].map((x) => x.toString(16).padStart(2, '0')).join('');
}
const gradient = (iso2) => { const h = hue(iso2); return [hsl2hex(h, 45, 32), hsl2hex((h + 40) % 360, 55, 58)]; };

function parseCsv(text) {
  return text.trim().split(/\r?\n/).slice(1).map((l) => l.split(',')).filter((c) => c.length >= 3);
}

/**
 * يستورد متطلبات الدخول للجواز `passport` من CSV بصيغة tidy.
 * - لا يلمس أي إصدار نشره محرر بشريًا (reviewed=1).
 * - لا يعيد الكتابة إن لم يتغيّر نوع الدخول أو عدد الأيام (آمن للتكرار).
 */
export function importPassportIndex(db, { csvPath = join(DATA, 'passport-index-SA.csv'), passport = 'SA' } = {}) {
  const countries = Object.fromEntries(JSON.parse(readFileSync(join(DATA, 'countries.json'), 'utf8')).map((c) => [c.cca2, c]));
  const rows = parseCsv(readFileSync(csvPath, 'utf8'));
  const stats = { created: 0, updated: 0, unchanged: 0, skippedReviewed: 0, skippedOther: 0 };

  tx(db, () => {
    const getDest = db.prepare('SELECT id, cca3 FROM destinations WHERE id=?');
    const insDest = db.prepare(`INSERT INTO destinations(id,iata,name_ar,name_en,city,emoji,g1,g2,region,budget,seasons,tags,flight,currency,rating,about_ar,cca3) VALUES(?,?,?,?,?,?,?,?,?,NULL,'[]','[]',NULL,?,NULL,NULL,?)`);
    const getReq = db.prepare(`SELECT * FROM entry_requirements WHERE destination_id=? AND passport_country=? AND status='published' ORDER BY version DESC LIMIT 1`);
    const maxVer = db.prepare('SELECT COALESCE(MAX(version),0) v FROM entry_requirements WHERE destination_id=? AND passport_country=?');
    const insReq = db.prepare(`INSERT INTO entry_requirements(destination_id,passport_country,version,status,visa,stay_days,passport_validity,docs,vaccines,quarantine,embassy,source,source_url,verified_at,reviewed) VALUES(?,?,?,'published',?,?,NULL,?,NULL,?,'{}',?,?,?,0)`);

    for (const [, destRaw, value] of rows) {
      const iso2 = destRaw.toUpperCase();
      const m = mapRequirement(value, iso2);
      const c = countries[iso2];
      if (!m || !c || iso2 === passport) { stats.skippedOther++; continue; }
      const id = iso2.toLowerCase();
      const existed = getDest.get(id);
      if (!existed) {
        const [g1, g2] = gradient(iso2);
        const region = GCC.has(iso2) ? 'gulf' : CAUCASUS.has(iso2) ? 'caucasus' : (REGION[c.region] || 'asia');
        insDest.run(id, '', c.ar, c.en, '', c.flag, g1, g2, region, c.currency, c.cca3);
      } else if (!existed.cca3) db.prepare('UPDATE destinations SET cca3=? WHERE id=?').run(c.cca3, id);

      const cur = getReq.get(id, passport);
      if (cur?.reviewed) { stats.skippedReviewed++; continue; }
      if (cur && cur.visa === m.visa && (cur.stay_days ?? null) === m.stay) { stats.unchanged++; continue; }
      if (cur && cur.stay_days != null && m.stay == null && cur.visa === m.visa) { stats.unchanged++; continue; }
      const version = maxVer.get(id, passport).v + 1;
      const docs = JSON.stringify([...GENERIC_DOCS[m.visa]]);
      if (cur) db.prepare(`UPDATE entry_requirements SET status='archived' WHERE id=?`).run(cur.id);
      const r = insReq.run(id, passport, version, m.visa, m.stay, docs, null, PI_SOURCE, PI_URL, PI_DATE);
      if (cur) {
        db.prepare('INSERT INTO requirement_changes(requirement_id,diff) VALUES(?,?)').run(r.lastInsertRowid, JSON.stringify({ visa: { from: cur.visa, to: m.visa }, stay_days: { from: cur.stay_days, to: m.stay }, import: PI_SOURCE }));
        audit(db, null, 'requirement.import_update', 'entry_requirement', r.lastInsertRowid, { destId: id, from: cur.visa, to: m.visa });
        stats.updated++;
      } else stats.created++;
    }
  });
  return stats;
}

if (process.argv[1] === fileURLToPath(import.meta.url)) {
  const db = openDb(process.env.DB_FILE || join(here, '../data/safarna.db'));
  console.log(importPassportIndex(db));
}
