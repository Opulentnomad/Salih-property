import { destDto, reqDto, latestPublished, parse, SEASONS, VISAS } from '../dto.js';

const like = (s) => `%${String(s).replace(/[\\%_]/g, (c) => '\\' + c)}%`;

export default async function publicRoutes(app, { db }) {
  app.get('/health', async () => ({ ok: true, destinations: db.prepare('SELECT COUNT(*) c FROM destinations').get().c }));

  app.get('/destinations', {
    schema: { querystring: { type: 'object', additionalProperties: false, properties: {
      q: { type: 'string', maxLength: 60 }, region: { type: 'string', maxLength: 20 },
      visa: { type: 'string', enum: VISAS }, budget: { type: 'integer', minimum: 1, maximum: 3 },
      season: { type: 'string', enum: SEASONS }, month: { type: 'integer', minimum: 1, maximum: 12 }, limit: { type: 'integer', minimum: 1, maximum: 300, default: 50 },
      offset: { type: 'integer', minimum: 0, default: 0 } } } },
  }, async (req) => {
    const { q, region, visa, budget, season, month, limit, offset } = req.query;
    const where = []; const args = [];
    if (q) { where.push(`(name_ar LIKE ? ESCAPE '\\' OR name_en LIKE ? ESCAPE '\\' OR city LIKE ? ESCAPE '\\' OR iata LIKE ? ESCAPE '\\' OR cca3 LIKE ? ESCAPE '\\')`); args.push(like(q), like(q), like(q), like(q), like(q)); }
    if (region) { where.push('region=?'); args.push(region); }
    if (budget) { where.push('budget=?'); args.push(budget); }
    if (season) { where.push('seasons LIKE ?'); args.push(`%"${season}"%`); }
    const rows = db.prepare(`SELECT * FROM destinations ${where.length ? 'WHERE ' + where.join(' AND ') : ''} ORDER BY rating DESC, id`).all(...args);
    let items = rows.map((d) => destDto(d, latestPublished(db, d.id)));
    if (visa) items = items.filter((d) => d.req?.visa === visa);
    if (month) items = items.filter((d) => d.months.includes(month));
    const total = items.length;
    items = items.slice(offset, offset + limit);
    return { items, total, nextOffset: offset + limit < total ? offset + limit : null };
  });

  app.get('/destinations/:id', async (req, reply) => {
    const d = db.prepare('SELECT * FROM destinations WHERE id=?').get(req.params.id);
    if (!d) return reply.code(404).send({ error: 'not_found', message: 'الوجهة غير موجودة' });
    const agg = db.prepare(`SELECT COUNT(*) count, ROUND(AVG(rating),1) avg FROM reviews WHERE destination_id=? AND status='published'`).get(d.id);
    return { ...destDto(d, latestPublished(db, d.id)), reviews: { count: agg.count, avg: agg.avg } };
  });

  app.get('/entry-requirements', {
    schema: { querystring: { type: 'object', required: ['destination'], properties: {
      destination: { type: 'string', maxLength: 10 }, passport: { type: 'string', pattern: '^[A-Za-z]{2}$', default: 'SA' } } } },
  }, async (req, reply) => {
    const dest = db.prepare('SELECT id,iata,name_ar FROM destinations WHERE id=? OR iata=? OR cca3=?').get(req.query.destination.toLowerCase(), req.query.destination.toUpperCase(), req.query.destination.toUpperCase());
    if (!dest) return reply.code(404).send({ error: 'not_found', message: 'الوجهة غير موجودة' });
    const r = latestPublished(db, dest.id, req.query.passport.toUpperCase());
    if (!r) return reply.code(404).send({ error: 'no_data', message: 'لا توجد بيانات منشورة لهذا الجواز بعد' });
    return { destination: dest.id, ...reqDto(r) };
  });

  app.get('/destinations/:id/reviews', async (req) => {
    const rows = db.prepare(`SELECT id, author, rating, body, created_at FROM reviews WHERE destination_id=? AND status='published' ORDER BY created_at DESC, id DESC LIMIT 100`).all(req.params.id);
    return { items: rows };
  });

  app.get('/recommendations', {
    schema: { querystring: { type: 'object', properties: { interests: { type: 'string', maxLength: 200 }, budget: { type: 'integer', minimum: 1, maximum: 3 }, limit: { type: 'integer', minimum: 1, maximum: 20, default: 6 } } } },
  }, async (req) => {
    const interests = (req.query.interests || '').split(',').map((s) => s.trim()).filter(Boolean);
    const scored = db.prepare('SELECT * FROM destinations').all().map((d) => {
      const r = latestPublished(db, d.id);
      let s = d.rating ?? 0;
      s += parse(d.tags, []).filter((t) => interests.includes(t)).length * 2;
      if (req.query.budget && d.budget <= req.query.budget) s += 1.5;
      if (r && ['free', 'gcc'].includes(r.visa)) s += 1;
      return { s, dto: destDto(d, r) };
    });
    scored.sort((a, b) => b.s - a.s);
    return { items: scored.slice(0, req.query.limit).map((x) => x.dto) };
  });

  app.get('/calendar', {
    schema: { querystring: { type: 'object', additionalProperties: false, properties: {
      from: { type: 'string', pattern: '^\\d{4}-\\d{2}-\\d{2}$' }, to: { type: 'string', pattern: '^\\d{4}-\\d{2}-\\d{2}$' } } } },
  }, async (req) => {
    const today = new Date().toISOString().slice(0, 10);
    const from = req.query.from || today;
    const to = req.query.to || new Date(Date.now() + 400 * 86400000).toISOString().slice(0, 10);
    const items = db.prepare('SELECT * FROM saudi_events WHERE end_date>=? AND start_date<=? ORDER BY start_date, id').all(from, to)
      .map((e) => ({ id: e.id, name: e.name_ar, kind: e.kind, start: e.start_date, end: e.end_date, hijri: e.hijri, note: e.note, confirmed: !!e.confirmed, source: e.source }));
    return { items, disclaimer: 'تواريخ الأعياد محسوبة من تقويم أم القرى وقد تختلف يومًا أو أكثر عن الإعلان الرسمي. إجازات المدارس تُدخلها الإدارة بعد إعلان وزارة التعليم.' };
  });

  let fxBusy = false;
  async function refreshFx() {
    if (fxBusy) return; fxBusy = true;
    try {
      const r = await fetch('https://open.er-api.com/v6/latest/SAR', { signal: AbortSignal.timeout(5000) });
      const j = await r.json();
      if (j?.rates) {
        const now = new Date().toISOString();
        const upd = db.prepare(`UPDATE fx_rates SET rate=?, fetched_at=? WHERE base='SAR' AND quote=?`);
        for (const q of db.prepare(`SELECT quote FROM fx_rates WHERE base='SAR'`).all()) if (j.rates[q.quote] > 0) upd.run(j.rates[q.quote], now, q.quote);
      }
    } catch { /* نُبقي آخر أسعار محفوظة */ } finally { fxBusy = false; }
  }
  app.get('/fx', async (req) => {
    const base = (req.query.base || 'SAR').toUpperCase();
    const rows = db.prepare('SELECT quote, rate, fetched_at FROM fx_rates WHERE base=?').all(base);
    const fetchedAt = rows[0]?.fetched_at ?? null;
    if (app.fxLive && base === 'SAR' && (!fetchedAt || Date.now() - new Date(fetchedAt).getTime() > 86400000)) refreshFx(); // في الخلفية، مرة يوميًا
    return { base, rates: Object.fromEntries(rows.map((r) => [r.quote, r.rate])), fetchedAt };
  });

  app.get('/affiliate/:id/click', async (req, reply) => {
    const l = db.prepare('SELECT * FROM affiliate_links WHERE id=? AND active=1').get(req.params.id);
    if (!l || !/^https:\/\//.test(l.url_template)) return reply.code(404).send({ error: 'not_found' });
    db.prepare('INSERT INTO affiliate_clicks(link_id) VALUES(?)').run(l.id);
    return reply.redirect(l.url_template, 302);
  });
}
