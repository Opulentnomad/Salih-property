import { test, before } from 'node:test';
import assert from 'node:assert/strict';
import { openDb } from '../src/db.js';
import { seed } from '../src/seed.js';
import { buildApp } from '../src/app.js';

let app, db;
const call = async (method, url, { token, body } = {}) => {
  const res = await app.inject({ method, url, payload: body, headers: token ? { authorization: `Bearer ${token}` } : {} });
  let json; try { json = res.json(); } catch { json = null; }
  return { status: res.statusCode, json, res };
};
const register = async (email, name = 'مستخدم') => (await call('POST', '/api/auth/register', { body: { email, password: 'password123', name } })).json;
let admin;

before(async () => {
  db = openDb(':memory:');
  await seed(db, { adminEmail: 'admin@example.com', adminPassword: 'adminpass123' });
  app = await buildApp({ db, jwtSecret: 'x'.repeat(40), authMax: 1000 });
  admin = (await call('POST', '/api/auth/login', { body: { email: 'admin@example.com', password: 'adminpass123' } })).json;
});

test('health and destinations list', async () => {
  assert.ok((await call('GET', '/api/health')).json.destinations >= 190);
  const { json } = await call('GET', '/api/destinations?limit=300');
  assert.ok(json.total >= 190 && json.items.length === json.total);
  assert.ok(json.items[0].req.visa && json.items[0].iata);
});

test('filters: visa, season, search, validation', async () => {
  const free = (await call('GET', '/api/destinations?visa=free')).json;
  assert.ok(free.items.length >= 20 && free.items.every((d) => d.req.visa === 'free'));
  assert.ok((await call('GET', '/api/destinations?q=IST')).json.items.some((d) => d.id === 'tr'));
  assert.ok((await call('GET', '/api/destinations?q=إسطنبول')).json.items.some((d) => d.id === 'tr'));
  assert.equal((await call('GET', '/api/destinations?q=%25')).json.total, 0); // لا يُفسَّر % كشرط عام
  assert.equal((await call('GET', '/api/destinations?visa=bogus')).status, 400);
  assert.equal((await call('GET', '/api/destinations?limit=2')).json.items.length, 2);
});

test('entry requirements: by id or IATA, 404s, freshness fields', async () => {
  const r = await call('GET', '/api/entry-requirements?destination=tr');
  assert.equal(r.status, 200);
  assert.equal(r.json.visa, 'free');
  assert.ok(r.json.disclaimer && r.json.source && r.json.verified);
  assert.equal(typeof r.json.stale, 'boolean');
  assert.equal((await call('GET', '/api/entry-requirements?destination=IST')).json.destination, 'tr');
  assert.equal((await call('GET', '/api/entry-requirements?destination=zz')).status, 404);
  assert.equal((await call('GET', '/api/entry-requirements?destination=tr&passport=US')).status, 404);
});

test('auth: register, duplicate, login, bad password, protected routes', async () => {
  const a = await register('a@example.com');
  assert.ok(a.token && a.user.role === 'user');
  assert.equal((await call('POST', '/api/auth/register', { body: { email: 'A@example.com', password: 'password123' } })).status, 409);
  assert.equal((await call('POST', '/api/auth/register', { body: { email: 'x@example.com', password: 'short' } })).status, 400);
  assert.equal((await call('POST', '/api/auth/login', { body: { email: 'a@example.com', password: 'wrongpass1' } })).status, 401);
  assert.equal((await call('POST', '/api/auth/login', { body: { email: 'nobody@example.com', password: 'wrongpass1' } })).status, 401);
  assert.equal((await call('GET', '/api/me')).status, 401);
  assert.equal((await call('GET', '/api/me', { token: a.token })).json.email, 'a@example.com');
  assert.equal((await call('GET', '/api/me', { token: 'garbage' })).status, 401);
});

test('favorites add/list/remove and are per user', async () => {
  const a = await register('fav-a@example.com'), b = await register('fav-b@example.com');
  assert.equal((await call('PUT', '/api/me/favorites/tr', { token: a.token })).status, 204);
  assert.equal((await call('PUT', '/api/me/favorites/tr', { token: a.token })).status, 204); // idempotent
  assert.equal((await call('PUT', '/api/me/favorites/zz', { token: a.token })).status, 404);
  assert.equal((await call('GET', '/api/me/favorites', { token: a.token })).json.items.length, 1);
  assert.equal((await call('GET', '/api/me/favorites', { token: b.token })).json.items.length, 0);
  await call('DELETE', '/api/me/favorites/tr', { token: a.token });
  assert.equal((await call('GET', '/api/me/favorites', { token: a.token })).json.items.length, 0);
});

test('trips CRUD and ownership isolation', async () => {
  const a = await register('trip-a@example.com'), b = await register('trip-b@example.com');
  const c = await call('POST', '/api/trips', { token: a.token, body: { destinationId: 'tr', startDate: '2026-12-01', days: 4 } });
  assert.equal(c.status, 201);
  const id = c.json.id;
  assert.equal((await call('PUT', `/api/trips/${id}`, { token: a.token, body: { plan: ['يوم أول'] } })).json.plan[0], 'يوم أول');
  assert.equal((await call('PUT', `/api/trips/${id}`, { token: b.token, body: { days: 9 } })).status, 404); // ليس صاحبها
  await call('DELETE', `/api/trips/${id}`, { token: b.token });
  assert.equal((await call('GET', '/api/trips', { token: a.token })).json.items.length, 1);
  assert.equal((await call('POST', '/api/trips', { token: a.token, body: { destinationId: 'tr', days: 99 } })).status, 400);
  assert.equal((await call('POST', '/api/trips', { token: a.token, body: { destinationId: 'zz' } })).status, 404);
});

test('reviews are moderated before they become public', async () => {
  const u = await register('rev@example.com', 'ريم');
  const before = (await call('GET', '/api/destinations/tr/reviews')).json.items.length;
  const p = await call('POST', '/api/destinations/tr/reviews', { token: u.token, body: { rating: 5, body: 'تجربة ممتازة' } });
  assert.equal(p.status, 202);
  assert.equal((await call('GET', '/api/destinations/tr/reviews')).json.items.length, before);
  assert.equal((await call('GET', '/api/admin/reviews', { token: u.token })).status, 403);
  assert.equal((await call('POST', `/api/admin/reviews/${p.json.id}/moderate`, { token: admin.token, body: { status: 'published' } })).status, 200);
  assert.equal((await call('GET', '/api/destinations/tr/reviews')).json.items.length, before + 1);
  assert.equal((await call('POST', '/api/destinations/tr/reviews', { token: u.token, body: { rating: 9, body: 'x' } })).status, 400);
});

test('admin: draft → publish creates a new version, archives old, logs audit', async () => {
  const u = await register('plain@example.com');
  assert.equal((await call('PUT', '/api/admin/requirements/tr', { token: u.token, body: { stay: 30 } })).status, 403);
  assert.equal((await call('PUT', '/api/admin/requirements/tr', { body: { stay: 30 } })).status, 401);
  const d = await call('PUT', '/api/admin/requirements/tr', { token: admin.token, body: { stay: 60, docs: ['جواز ساري', 'حجز فندق'] } });
  assert.equal(d.status, 201);
  assert.equal(d.json.version, 2);
  assert.equal((await call('GET', '/api/entry-requirements?destination=tr')).json.stay, 90); // المسودة غير ظاهرة
  const pub = await call('POST', `/api/admin/requirements/${d.json.id}/publish`, { token: admin.token });
  assert.equal(pub.status, 200);
  assert.ok(pub.json.changed.includes('stay_days'));
  const now = (await call('GET', '/api/entry-requirements?destination=tr')).json;
  assert.equal(now.version, 2); assert.equal(now.stay, 60); assert.equal(now.stale, false);
  assert.equal((await call('POST', `/api/admin/requirements/${d.json.id}/publish`, { token: admin.token })).status, 409);
  const hist = (await call('GET', '/api/admin/requirements?destination=tr', { token: admin.token })).json.items;
  assert.deepEqual(hist.map((h) => h.status), ['published', 'archived']);
  assert.ok((await call('GET', '/api/admin/audit', { token: admin.token })).json.items.some((a) => a.action === 'requirement.publish'));
  assert.equal((await call('PUT', '/api/admin/requirements/tr', { token: admin.token, body: { visa: 'nope' } })).status, 400);
  assert.equal((await call('PUT', '/api/admin/requirements/tr', { token: admin.token, body: { sourceUrl: 'javascript:alert(1)' } })).status, 400);
});

test('recommendations, fx, alerts, affiliate redirect', async () => {
  const rec = (await call('GET', '/api/recommendations?interests=شاطئ&budget=3')).json.items;
  assert.ok(rec.length > 0);
  const fx = (await call('GET', '/api/fx')).json;
  assert.equal(fx.base, 'SAR'); assert.ok(fx.rates.USD > 0.26 && fx.rates.USD < 0.27);
  const u = await register('alert@example.com');
  assert.equal((await call('POST', '/api/alerts/subscriptions', { token: u.token, body: { destinationId: 'tr' } })).status, 201);
  assert.equal((await call('GET', '/api/alerts/subscriptions', { token: u.token })).json.items.length, 1);
  const link = db.prepare('SELECT id FROM affiliate_links LIMIT 1').get();
  const r = await call('GET', `/api/affiliate/${link.id}/click`);
  assert.equal(r.status, 302);
  assert.match(r.res.headers.location, /^https:\/\//);
});

test('unknown api route returns JSON 404; security headers present', async () => {
  const r = await call('GET', '/api/nope');
  assert.equal(r.status, 404); assert.equal(r.json.error, 'not_found');
  assert.equal(r.res.headers['x-content-type-options'], 'nosniff');
});

import { importPassportIndex, mapRequirement } from '../src/import-passport-index.js';

test('open-data import: ~198 destinations, correct mapping, flagged as unreviewed', async () => {
  const bh = (await call('GET', '/api/entry-requirements?destination=bh')).json;
  assert.equal(bh.visa, 'gcc'); assert.equal(bh.reviewed, false); assert.match(bh.source, /Passport Index/);
  assert.equal(bh.stale, true); // مجموعة البيانات بتاريخ 2026-02-17
  assert.equal((await call('GET', '/api/entry-requirements?destination=us')).json.visa, 'required');
  assert.equal((await call('GET', '/api/entry-requirements?destination=jp')).json.visa, 'evisa');
  assert.equal((await call('GET', '/api/entry-requirements?destination=sa')).status, 404); // لا وجهة للجواز نفسه
  assert.equal((await call('GET', '/api/entry-requirements?destination=BHR')).json.destination, 'bh'); // بالرمز الثلاثي
  const eg = (await call('GET', '/api/entry-requirements?destination=eg')).json;
  assert.equal(eg.visa, 'free'); assert.equal(eg.stay, 180); // صُحّح من القيمة التجريبية السابقة
  const d = (await call('GET', '/api/destinations/us')).json;
  assert.equal(d.ar, 'الولايات المتحدة'); assert.equal(d.iata, 'USA'); assert.equal(d.region, 'americas');
  assert.equal((await call('GET', '/api/destinations?q=%D8%A7%D9%84%D8%A8%D8%AD%D8%B1%D9%8A%D9%86')).json.items[0].id, 'bh');
});

test('import is idempotent and never overwrites human-reviewed data', async () => {
  const again = importPassportIndex(db);
  assert.equal(again.created + again.updated, 0);
  // محرر يراجع وينشر بيانات الكويت، ثم يعاد الاستيراد
  const dr = await call('PUT', '/api/admin/requirements/kw', { token: admin.token, body: { stay: 21, docs: ['الهوية الوطنية'] } });
  await call('POST', `/api/admin/requirements/${dr.json.id}/publish`, { token: admin.token });
  const kw = (await call('GET', '/api/entry-requirements?destination=kw')).json;
  assert.equal(kw.reviewed, true); assert.equal(kw.stay, 21);
  db.prepare("UPDATE entry_requirements SET visa='required' WHERE destination_id='kw' AND status='published'").run();
  const r = importPassportIndex(db);
  assert.ok(r.skippedReviewed >= 1);
  assert.equal((await call('GET', '/api/entry-requirements?destination=kw')).json.visa, 'required'); // لم يُستبدل
});

test('mapRequirement covers every dataset value', () => {
  assert.deepEqual(mapRequirement('90', 'TR'), { visa: 'free', stay: 90 });
  assert.deepEqual(mapRequirement('visa free', 'BH'), { visa: 'gcc', stay: null });
  assert.deepEqual(mapRequirement('visa on arrival', 'MV'), { visa: 'voa', stay: null });
  assert.deepEqual(mapRequirement('e-visa', 'JP'), { visa: 'evisa', stay: null });
  assert.deepEqual(mapRequirement('eta', 'GB'), { visa: 'eta', stay: null });
  assert.deepEqual(mapRequirement('visa required', 'US'), { visa: 'required', stay: null });
  assert.equal(mapRequirement('-1', 'SA'), null);
  assert.equal(mapRequirement('no admission', 'XX'), null);
});

test('best months: filter by month, derived seasons, admin edit', async () => {
  const jan = (await call('GET', '/api/destinations?month=1&limit=300')).json.items.map((d) => d.id);
  assert.ok(jan.includes('mv') && jan.includes('ae') && !jan.includes('gb'));
  const jul = (await call('GET', '/api/destinations?month=7&limit=300')).json.items.map((d) => d.id);
  assert.ok(jul.includes('id') && !jul.includes('ae'));
  assert.equal((await call('GET', '/api/destinations?month=13')).status, 400);
  const mv = (await call('GET', '/api/destinations/mv')).json;
  assert.deepEqual(mv.months, [1, 2, 3, 4, 11, 12]);
  assert.ok(mv.seasons.includes('winter') && !mv.seasons.includes('summer'));
  assert.equal((await call('GET', '/api/destinations?season=winter&month=1&limit=300')).json.items.every((d) => d.months.includes(1)), true);
  const u = await register('m@example.com');
  assert.equal((await call('PUT', '/api/admin/destinations/gb/months', { token: u.token, body: { months: [1] } })).status, 403);
  const ok = await call('PUT', '/api/admin/destinations/gb/months', { token: admin.token, body: { months: [7, 6, 8], note: 'تجربة' } });
  assert.deepEqual(ok.json.months, [6, 7, 8]); assert.deepEqual(ok.json.seasons, ['summer']);
  assert.equal((await call('PUT', '/api/admin/destinations/gb/months', { token: admin.token, body: { months: [0] } })).status, 400);
  assert.deepEqual((await call('GET', '/api/destinations/gb')).json.months, [6, 7, 8]);
});

test('login is rate limited by default (brute-force protection)', async () => {
  const strict = await buildApp({ db, jwtSecret: 'y'.repeat(40) });
  let last;
  for (let i = 0; i < 12; i++) last = (await strict.inject({ method: 'POST', url: '/api/auth/login', payload: { email: 'nobody@example.com', password: 'wrongpass1' } })).statusCode;
  assert.equal(last, 429);
});
