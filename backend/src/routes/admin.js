import { audit, tx } from '../db.js';
import { reqDto, parse, VISAS } from '../dto.js';
import { seasonsFromMonths } from '../best-months.js';

const FIELDS = ['visa', 'stay', 'passport', 'docs', 'vaccines', 'quarantine', 'embassy', 'source', 'sourceUrl'];

export default async function adminRoutes(app, { db }) {
  app.addHook('onRequest', app.authenticate);
  const uid = (req) => Number(req.user.sub);
  const can = (...roles) => async (req, reply) => {
    if (!roles.includes(req.user.role)) return reply.code(403).send({ error: 'forbidden', message: 'ليست لديك صلاحية لهذا الإجراء.' });
  };
  const EDIT = can('editor', 'reviewer', 'admin');
  const REVIEW = can('reviewer', 'admin');

  app.get('/requirements', { preHandler: EDIT, schema: { querystring: { type: 'object', required: ['destination'], properties: { destination: { type: 'string', maxLength: 10 }, passport: { type: 'string', default: 'SA' } } } } }, async (req) => ({
    items: db.prepare('SELECT * FROM entry_requirements WHERE destination_id=? AND passport_country=? ORDER BY version DESC').all(req.query.destination, req.query.passport.toUpperCase()).map(reqDto),
  }));

  // إنشاء مسودة إصدار جديد (لا تظهر للعامة قبل النشر)
  app.put('/requirements/:destId', {
    preHandler: EDIT,
    schema: { body: { type: 'object', additionalProperties: false, properties: {
      visa: { enum: VISAS }, stay: { type: 'integer', minimum: 0, maximum: 3650 }, passport: { type: 'string', maxLength: 80 },
      docs: { type: 'array', maxItems: 30, items: { type: 'string', minLength: 1, maxLength: 200 } },
      vaccines: { type: 'string', maxLength: 200 }, quarantine: { type: 'string', maxLength: 200 },
      embassy: { type: 'object', additionalProperties: false, properties: { name: { type: 'string', maxLength: 100 }, phone: { type: 'string', maxLength: 30 }, addr: { type: 'string', maxLength: 200 } } },
      source: { type: 'string', minLength: 2, maxLength: 150 }, sourceUrl: { type: ['string', 'null'], pattern: '^https://', maxLength: 300 } } } },
  }, async (req, reply) => {
    const { destId } = req.params;
    if (!db.prepare('SELECT 1 FROM destinations WHERE id=?').get(destId)) return reply.code(404).send({ error: 'not_found' });
    const base = db.prepare(`SELECT * FROM entry_requirements WHERE destination_id=? AND passport_country='SA' ORDER BY version DESC LIMIT 1`).get(destId);
    const b = req.body;
    const row = tx(db, () => {
      const version = (base?.version ?? 0) + 1;
      const r = db.prepare(`INSERT INTO entry_requirements(destination_id,passport_country,version,status,visa,stay_days,passport_validity,docs,vaccines,quarantine,embassy,source,source_url,verified_at,created_by)
        VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)`).run(destId, 'SA', version, 'draft',
        b.visa ?? base.visa, b.stay ?? base.stay_days, b.passport ?? base.passport_validity, b.docs ? JSON.stringify(b.docs) : base.docs,
        b.vaccines ?? base.vaccines, b.quarantine ?? base.quarantine, b.embassy ? JSON.stringify(b.embassy) : base.embassy,
        b.source ?? base.source, b.sourceUrl !== undefined ? b.sourceUrl : base.source_url, base.verified_at, uid(req));
      audit(db, uid(req), 'requirement.draft', 'entry_requirement', r.lastInsertRowid, { destId, version });
      return db.prepare('SELECT * FROM entry_requirements WHERE id=?').get(r.lastInsertRowid);
    });
    return reply.code(201).send(reqDto(row));
  });

  // نشر مسودة: يؤرشف المنشور السابق ويسجّل الفروق
  app.post('/requirements/:id/publish', { preHandler: REVIEW }, async (req, reply) => {
    const draft = db.prepare('SELECT * FROM entry_requirements WHERE id=?').get(req.params.id);
    if (!draft) return reply.code(404).send({ error: 'not_found' });
    if (draft.status !== 'draft') return reply.code(409).send({ error: 'not_draft', message: 'يمكن نشر المسودات فقط.' });
    const prev = db.prepare(`SELECT * FROM entry_requirements WHERE destination_id=? AND passport_country=? AND status='published'`).get(draft.destination_id, draft.passport_country);
    const keys = ['visa', 'stay_days', 'passport_validity', 'docs', 'vaccines', 'quarantine', 'embassy', 'source', 'source_url'];
    const diff = {};
    for (const k of keys) if (!prev || prev[k] !== draft[k]) diff[k] = { from: prev?.[k] ?? null, to: draft[k] };
    const now = new Date().toISOString();
    tx(db, () => {
      if (prev) db.prepare(`UPDATE entry_requirements SET status='archived' WHERE id=?`).run(prev.id);
      db.prepare(`UPDATE entry_requirements SET status='published', verified_at=?, reviewed=1 WHERE id=?`).run(now, draft.id);
      db.prepare('INSERT INTO requirement_changes(requirement_id,diff,changed_by) VALUES(?,?,?)').run(draft.id, JSON.stringify(diff), uid(req));
      audit(db, uid(req), 'requirement.publish', 'entry_requirement', draft.id, { destId: draft.destination_id, version: draft.version });
    });
    return { ...reqDto(db.prepare('SELECT * FROM entry_requirements WHERE id=?').get(draft.id)), changed: Object.keys(diff) };
  });

  // أفضل أشهر الزيارة (بيانات الوجهة لا شروط الدخول)
  app.put('/destinations/:id/months', {
    preHandler: EDIT,
    schema: { body: { type: 'object', required: ['months'], additionalProperties: false, properties: {
      months: { type: 'array', maxItems: 12, uniqueItems: true, items: { type: 'integer', minimum: 1, maximum: 12 } }, note: { type: ['string', 'null'], maxLength: 160 } } } },
  }, async (req, reply) => {
    const months = [...req.body.months].sort((a, b) => a - b);
    const r = db.prepare('UPDATE destinations SET best_months=?, months_note=?, seasons=? WHERE id=?').run(JSON.stringify(months), req.body.note ?? null, JSON.stringify(seasonsFromMonths(months)), req.params.id);
    if (!r.changes) return reply.code(404).send({ error: 'not_found' });
    audit(db, uid(req), 'destination.months', 'destination', req.params.id, { months });
    return { id: req.params.id, months, seasons: seasonsFromMonths(months) };
  });

  // تقويم المواسم (إجازات المدارس وتأكيد تواريخ الأعياد بعد الإعلان الرسمي)
  const evBody = { type: 'object', additionalProperties: false, properties: {
    name: { type: 'string', minLength: 2, maxLength: 80 }, kind: { enum: ['religious', 'national', 'school', 'custom'] },
    start: { type: 'string', pattern: '^\\d{4}-\\d{2}-\\d{2}$' }, end: { type: 'string', pattern: '^\\d{4}-\\d{2}-\\d{2}$' },
    note: { type: ['string', 'null'], maxLength: 200 }, confirmed: { type: 'boolean' } } };
  const badRange = (s, e) => Number.isNaN(Date.parse(s)) || Number.isNaN(Date.parse(e)) || e < s;
  app.post('/calendar', { preHandler: EDIT, schema: { body: { ...evBody, required: ['name', 'kind', 'start', 'end'] } } }, async (req, reply) => {
    const b = req.body;
    if (badRange(b.start, b.end)) return reply.code(400).send({ error: 'bad_range', message: 'تاريخ النهاية يجب أن يكون بعد البداية.' });
    const r = db.prepare(`INSERT INTO saudi_events(name_ar,kind,start_date,end_date,note,source,confirmed) VALUES(?,?,?,?,?,'manual',?)`).run(b.name, b.kind, b.start, b.end, b.note ?? null, b.confirmed ? 1 : 0);
    audit(db, uid(req), 'calendar.create', 'saudi_event', r.lastInsertRowid, { name: b.name });
    return reply.code(201).send({ id: Number(r.lastInsertRowid) });
  });
  app.put('/calendar/:id', { preHandler: EDIT, schema: { body: evBody } }, async (req, reply) => {
    const e = db.prepare('SELECT * FROM saudi_events WHERE id=?').get(req.params.id);
    if (!e) return reply.code(404).send({ error: 'not_found' });
    const b = req.body, start = b.start ?? e.start_date, end = b.end ?? e.end_date;
    if (badRange(start, end)) return reply.code(400).send({ error: 'bad_range', message: 'تاريخ النهاية يجب أن يكون بعد البداية.' });
    db.prepare('UPDATE saudi_events SET name_ar=?, kind=?, start_date=?, end_date=?, note=?, confirmed=?, source=? WHERE id=?')
      .run(b.name ?? e.name_ar, b.kind ?? e.kind, start, end, b.note !== undefined ? b.note : e.note, b.confirmed === undefined ? e.confirmed : (b.confirmed ? 1 : 0), 'manual', e.id);
    audit(db, uid(req), 'calendar.update', 'saudi_event', e.id, b);
    return { id: e.id };
  });
  app.delete('/calendar/:id', { preHandler: REVIEW }, async (req, reply) => {
    const r = db.prepare('DELETE FROM saudi_events WHERE id=?').run(req.params.id);
    if (!r.changes) return reply.code(404).send({ error: 'not_found' });
    audit(db, uid(req), 'calendar.delete', 'saudi_event', req.params.id);
    return reply.code(204).send();
  });

  app.get('/reviews', { preHandler: REVIEW, schema: { querystring: { type: 'object', properties: { status: { enum: ['pending', 'published', 'rejected'], default: 'pending' } } } } }, async (req) => ({
    items: db.prepare('SELECT * FROM reviews WHERE status=? ORDER BY id DESC LIMIT 200').all(req.query.status),
  }));
  app.post('/reviews/:id/moderate', { preHandler: REVIEW, schema: { body: { type: 'object', required: ['status'], additionalProperties: false, properties: { status: { enum: ['published', 'rejected'] } } } } }, async (req, reply) => {
    const r = db.prepare('UPDATE reviews SET status=? WHERE id=?').run(req.body.status, req.params.id);
    if (!r.changes) return reply.code(404).send({ error: 'not_found' });
    audit(db, uid(req), 'review.' + req.body.status, 'review', req.params.id);
    return { id: Number(req.params.id), status: req.body.status };
  });

  app.get('/audit', { preHandler: can('admin') }, async () => ({ items: db.prepare('SELECT * FROM audit_log ORDER BY id DESC LIMIT 100').all().map((a) => ({ ...a, meta: parse(a.meta, {}) })) }));
}
