import { hashPassword, verifyPassword } from '../auth.js';
import { parse } from '../dto.js';

const userDto = (u) => ({ id: u.id, email: u.email, name: u.name, role: u.role, prefs: parse(u.prefs, {}) });
const noStore = (reply) => reply.header('Cache-Control', 'no-store');

export default async function accountRoutes(app, { db }) {
  const authRate = { config: { rateLimit: { max: app.authMax, timeWindow: '1 minute' } } };
  const sign = (u) => app.jwt.sign({ sub: String(u.id), role: u.role }, { expiresIn: '7d' });
  const bodyCred = { type: 'object', required: ['email', 'password'], additionalProperties: false, properties: {
    email: { type: 'string', format: 'email', maxLength: 254 }, password: { type: 'string', minLength: 8, maxLength: 128 }, name: { type: 'string', maxLength: 60 } } };

  app.post('/auth/register', { ...authRate, schema: { body: bodyCred } }, async (req, reply) => {
    const { email, password, name } = req.body;
    try {
      const r = db.prepare('INSERT INTO users(email,password_hash,name) VALUES(?,?,?)').run(email, await hashPassword(password), name ?? null);
      const u = db.prepare('SELECT * FROM users WHERE id=?').get(r.lastInsertRowid);
      noStore(reply);
      return reply.code(201).send({ token: sign(u), user: userDto(u) });
    } catch (e) {
      if (/UNIQUE/.test(String(e.message))) return reply.code(409).send({ error: 'email_taken', message: 'هذا البريد مسجّل مسبقًا. سجّل الدخول بدلًا من ذلك.' });
      throw e;
    }
  });

  app.post('/auth/login', { ...authRate, schema: { body: { ...bodyCred, required: ['email', 'password'] } } }, async (req, reply) => {
    const u = db.prepare('SELECT * FROM users WHERE email=?').get(req.body.email);
    // نفس الرسالة وزمن مشابه سواء كان البريد موجودًا أم لا
    const ok = await verifyPassword(req.body.password, u?.password_hash ?? 'scrypt$00$00');
    if (!u || !ok) return reply.code(401).send({ error: 'invalid_credentials', message: 'البريد أو كلمة المرور غير صحيحة.' });
    noStore(reply);
    return { token: sign(u), user: userDto(u) };
  });

  app.register(async (priv) => {
    priv.addHook('onRequest', app.authenticate);
    const uid = (req) => Number(req.user.sub);

    priv.get('/me', async (req, reply) => {
      const u = db.prepare('SELECT * FROM users WHERE id=?').get(uid(req));
      if (!u) return reply.code(401).send({ error: 'unauthorized' });
      return userDto(u);
    });
    priv.put('/me/prefs', { schema: { body: { type: 'object', additionalProperties: false, properties: {
      interests: { type: 'array', maxItems: 12, items: { type: 'string', maxLength: 30 } }, budget: { type: 'integer', minimum: 1, maximum: 3 } } } } }, async (req) => {
      db.prepare('UPDATE users SET prefs=? WHERE id=?').run(JSON.stringify(req.body), uid(req));
      return { prefs: req.body };
    });

    // المفضلة
    priv.get('/me/favorites', async (req) => ({ items: db.prepare('SELECT destination_id id, created_at FROM favorites WHERE user_id=? ORDER BY created_at DESC').all(uid(req)) }));
    priv.put('/me/favorites/:id', async (req, reply) => {
      if (!db.prepare('SELECT 1 FROM destinations WHERE id=?').get(req.params.id)) return reply.code(404).send({ error: 'not_found' });
      db.prepare('INSERT OR IGNORE INTO favorites(user_id,destination_id) VALUES(?,?)').run(uid(req), req.params.id);
      return reply.code(204).send();
    });
    priv.delete('/me/favorites/:id', async (req, reply) => { db.prepare('DELETE FROM favorites WHERE user_id=? AND destination_id=?').run(uid(req), req.params.id); return reply.code(204).send(); });

    // الرحلات
    const tripBody = { type: 'object', additionalProperties: false, properties: {
      destinationId: { type: 'string', maxLength: 10 }, startDate: { type: ['string', 'null'], pattern: '^\\d{4}-\\d{2}-\\d{2}$' },
      days: { type: 'integer', minimum: 1, maximum: 30 }, plan: { type: 'array', maxItems: 30, items: { type: 'string', maxLength: 300 } } } };
    const tripDto = (t) => ({ id: t.id, destinationId: t.destination_id, startDate: t.start_date, days: t.days, plan: parse(t.plan, []) });
    priv.get('/trips', async (req) => ({ items: db.prepare('SELECT * FROM trips WHERE user_id=? ORDER BY id DESC').all(uid(req)).map(tripDto) }));
    priv.post('/trips', { schema: { body: { ...tripBody, required: ['destinationId'] } } }, async (req, reply) => {
      const b = req.body;
      if (!db.prepare('SELECT 1 FROM destinations WHERE id=?').get(b.destinationId)) return reply.code(404).send({ error: 'not_found', message: 'الوجهة غير موجودة' });
      const r = db.prepare('INSERT INTO trips(user_id,destination_id,start_date,days,plan) VALUES(?,?,?,?,?)').run(uid(req), b.destinationId, b.startDate ?? null, b.days ?? 5, JSON.stringify(b.plan ?? []));
      return reply.code(201).send(tripDto(db.prepare('SELECT * FROM trips WHERE id=?').get(r.lastInsertRowid)));
    });
    priv.put('/trips/:id', { schema: { body: tripBody } }, async (req, reply) => {
      const t = db.prepare('SELECT * FROM trips WHERE id=? AND user_id=?').get(req.params.id, uid(req));
      if (!t) return reply.code(404).send({ error: 'not_found' });
      const b = req.body;
      db.prepare('UPDATE trips SET start_date=?, days=?, plan=? WHERE id=?').run(b.startDate !== undefined ? b.startDate : t.start_date, b.days ?? t.days, b.plan ? JSON.stringify(b.plan) : t.plan, t.id);
      return tripDto(db.prepare('SELECT * FROM trips WHERE id=?').get(t.id));
    });
    priv.delete('/trips/:id', async (req, reply) => { db.prepare('DELETE FROM trips WHERE id=? AND user_id=?').run(req.params.id, uid(req)); return reply.code(204).send(); });

    // المراجعات (تمرّ بالإشراف)
    priv.post('/destinations/:id/reviews', { schema: { body: { type: 'object', required: ['rating', 'body'], additionalProperties: false, properties: {
      rating: { type: 'integer', minimum: 1, maximum: 5 }, body: { type: 'string', minLength: 3, maxLength: 1000 } } } } }, async (req, reply) => {
      if (!db.prepare('SELECT 1 FROM destinations WHERE id=?').get(req.params.id)) return reply.code(404).send({ error: 'not_found' });
      const u = db.prepare('SELECT name,email FROM users WHERE id=?').get(uid(req));
      const author = u.name || u.email.split('@')[0];
      const r = db.prepare(`INSERT INTO reviews(user_id,destination_id,author,rating,body) VALUES(?,?,?,?,?)`).run(uid(req), req.params.id, author, req.body.rating, req.body.body.trim());
      return reply.code(202).send({ id: r.lastInsertRowid, status: 'pending', message: 'استلمنا مراجعتك وستظهر بعد المراجعة.' });
    });

    // التنبيهات
    priv.get('/alerts/subscriptions', async (req) => ({ items: db.prepare('SELECT destination_id id, channels FROM alert_subscriptions WHERE user_id=?').all(uid(req)).map((r) => ({ id: r.id, channels: parse(r.channels, []) })) }));
    priv.post('/alerts/subscriptions', { schema: { body: { type: 'object', required: ['destinationId'], additionalProperties: false, properties: {
      destinationId: { type: 'string', maxLength: 10 }, channels: { type: 'array', items: { enum: ['push', 'email'] }, minItems: 1, uniqueItems: true } } } } }, async (req, reply) => {
      if (!db.prepare('SELECT 1 FROM destinations WHERE id=?').get(req.body.destinationId)) return reply.code(404).send({ error: 'not_found' });
      db.prepare('INSERT INTO alert_subscriptions(user_id,destination_id,channels) VALUES(?,?,?) ON CONFLICT(user_id,destination_id) DO UPDATE SET channels=excluded.channels').run(uid(req), req.body.destinationId, JSON.stringify(req.body.channels ?? ['push', 'email']));
      return reply.code(201).send({ ok: true });
    });
    priv.delete('/alerts/subscriptions/:id', async (req, reply) => { db.prepare('DELETE FROM alert_subscriptions WHERE user_id=? AND destination_id=?').run(uid(req), req.params.id); return reply.code(204).send(); });
  });
}
