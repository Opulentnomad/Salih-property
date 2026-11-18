import vm from 'node:vm';
import { readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { dirname, join } from 'node:path';
import { openDb, tx } from './db.js';
import { hashPassword } from './auth.js';
import { importPassportIndex } from './import-passport-index.js';

const here = dirname(fileURLToPath(import.meta.url));

/** يقرأ البيانات التجريبية من app/data.js (المصدر الوحيد للبذر الأولي). */
export function loadClientData() {
  const ctx = { window: {} };
  vm.runInNewContext(readFileSync(join(here, '../../app/data.js'), 'utf8'), ctx);
  return ctx.window.SAFARNA_DATA;
}

export async function seed(db, { adminEmail, adminPassword, importOpen = true } = {}) {
  const D = loadClientData();
  const adminHash = adminEmail && adminPassword ? await hashPassword(adminPassword) : null;
  tx(db, () => {
    if (db.prepare('SELECT COUNT(*) c FROM destinations').get().c > 0) return;
    const insD = db.prepare(`INSERT INTO destinations(id,iata,name_ar,name_en,city,emoji,g1,g2,region,budget,seasons,tags,flight,currency,rating,about_ar) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)`);
    const insR = db.prepare(`INSERT INTO entry_requirements(destination_id,passport_country,version,status,visa,stay_days,passport_validity,docs,vaccines,quarantine,embassy,source,verified_at) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)`);
    for (const d of D.destinations) {
      insD.run(d.id, d.iata, d.ar, d.en, d.city, d.emoji, d.g[0], d.g[1], d.region, d.budget, JSON.stringify(d.seasons), JSON.stringify(d.tags), d.flight, d.cur, d.rating, d.about);
      const r = d.req;
      insR.run(d.id, 'SA', 1, 'published', r.visa, r.stay, r.passport, JSON.stringify(r.docs), r.vaccines, r.quarantine, JSON.stringify(r.embassy), r.source, r.verified);
    }
    const insRev = db.prepare(`INSERT INTO reviews(destination_id,author,rating,body,status) VALUES(?,?,?,?,'published')`);
    for (const v of D.reviews) insRev.run(v.d, v.n, v.s, v.t);
    const now = new Date().toISOString();
    const insFx = db.prepare('INSERT INTO fx_rates(base,quote,rate,fetched_at) VALUES(?,?,?,?)');
    for (const [q, rate] of Object.entries(D.ratesPerSAR)) insFx.run('SAR', q, rate, now);
    const links = db.prepare('INSERT INTO affiliate_links(destination_id,partner,kind,url_template) VALUES(?,?,?,?)');
    for (const d of D.destinations) {
      links.run(d.id, 'example', 'flights', `https://www.example.com/flights?to=${d.id}`);
      links.run(d.id, 'example', 'hotels', `https://www.example.com/hotels?c=${d.id}`);
    }
    if (adminHash) db.prepare(`INSERT INTO users(email,password_hash,name,role) VALUES(?,?,?,'admin')`).run(adminEmail, adminHash, 'Admin');
  });
  if (importOpen) return importPassportIndex(db);
}

if (process.argv[1] === fileURLToPath(import.meta.url)) {
  const db = openDb(process.env.DB_FILE || join(here, '../data/safarna.db'));
  await seed(db, { adminEmail: process.env.ADMIN_EMAIL, adminPassword: process.env.ADMIN_PASSWORD });
  console.log('seeded');
}
