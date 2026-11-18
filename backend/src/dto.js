export const STALE_DAYS = 30;
export const parse = (s, d) => { try { return JSON.parse(s) ?? d; } catch { return d; } };
export const DISCLAIMER = 'المعلومات استرشادية وقد تتغير. تحقق من الجهة الرسمية أو شركة الطيران قبل السفر.';

export function reqDto(r) {
  if (!r) return null;
  const ageDays = (Date.now() - new Date(r.verified_at).getTime()) / 86400000;
  return {
    id: r.id, version: r.version, status: r.status, passportCountry: r.passport_country,
    visa: r.visa, stay: r.stay_days, reviewed: !!r.reviewed, passport: r.passport_validity,
    docs: parse(r.docs, []), vaccines: r.vaccines, quarantine: r.quarantine,
    embassy: parse(r.embassy, {}), source: r.source, sourceUrl: r.source_url,
    verified: r.verified_at, stale: ageDays > STALE_DAYS, disclaimer: DISCLAIMER,
  };
}

/** الشكل المتوافق مع بيانات الواجهة (app/data.js). */
export function destDto(d, req) {
  return {
    id: d.id, iata: d.iata || d.cca3, ar: d.name_ar, en: d.name_en, city: d.city, emoji: d.emoji,
    g: [d.g1, d.g2], region: d.region, budget: d.budget, seasons: parse(d.seasons, []),
    tags: parse(d.tags, []), months: parse(d.best_months, []), monthsNote: d.months_note, flight: d.flight, cur: d.currency, rating: d.rating, about: d.about_ar,
    req: reqDto(req),
  };
}

export function latestPublished(db, destId, passport = 'SA') {
  return db.prepare(`SELECT * FROM entry_requirements WHERE destination_id=? AND passport_country=? AND status='published' ORDER BY version DESC LIMIT 1`).get(destId, passport);
}

export const SEASONS = ['winter', 'spring', 'summer', 'autumn'];
export const VISAS = ['free', 'gcc', 'voa', 'evisa', 'eta', 'required'];
