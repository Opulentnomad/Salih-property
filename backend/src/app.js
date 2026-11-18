import Fastify from 'fastify';
import cors from '@fastify/cors';
import jwt from '@fastify/jwt';
import rateLimit from '@fastify/rate-limit';
import fstatic from '@fastify/static';
import { randomBytes } from 'node:crypto';
import { existsSync } from 'node:fs';
import publicRoutes from './routes/public.js';
import accountRoutes from './routes/account.js';
import adminRoutes from './routes/admin.js';

const CSP = [
  "default-src 'self'", "script-src 'self'", "style-src 'self' 'unsafe-inline'",
  "font-src 'self'", "img-src 'self' data:", "connect-src 'self'",
  "frame-ancestors 'none'", "base-uri 'none'", "form-action 'self'",
].join('; ');

export async function buildApp({ db, jwtSecret, staticDir, corsOrigin, logger = false, authMax = 10, fxLive = process.env.FX_LIVE !== '0' && process.env.NODE_ENV !== 'test' } = {}) {
  const prod = process.env.NODE_ENV === 'production';
  let secret = jwtSecret || process.env.JWT_SECRET;
  if (!secret) {
    if (prod) throw new Error('JWT_SECRET مطلوب في بيئة الإنتاج (32 حرفًا على الأقل).');
    secret = randomBytes(32).toString('hex'); // تطوير فقط: تُبطَل الرموز عند إعادة التشغيل
  }
  if (prod && secret.length < 32) throw new Error('JWT_SECRET قصير جدًا.');

  const app = Fastify({ logger, bodyLimit: 64 * 1024, trustProxy: process.env.TRUST_PROXY === '1' });
  if (corsOrigin) await app.register(cors, { origin: corsOrigin.split(','), methods: ['GET', 'POST', 'PUT', 'DELETE'] });
  await app.register(rateLimit, { global: true, max: 300, timeWindow: '1 minute' });
  await app.register(jwt, { secret, sign: { algorithm: 'HS256' }, verify: { algorithms: ['HS256'] } });

  app.decorate('fxLive', fxLive); // تحديث أسعار الصرف من الخادم (لا يتصل المتصفح بأي طرف ثالث)
  app.decorate('authMax', authMax); // حدّ طلبات الدخول/التسجيل في الدقيقة
  app.decorate('authenticate', async (req, reply) => {
    try { await req.jwtVerify(); } catch { return reply.code(401).send({ error: 'unauthorized', message: 'سجّل الدخول للمتابعة.' }); }
    const u = db.prepare('SELECT id, role FROM users WHERE id=?').get(Number(req.user.sub));
    if (!u) return reply.code(401).send({ error: 'unauthorized', message: 'سجّل الدخول للمتابعة.' });
    req.user.role = u.role; // الدور الحالي من القاعدة لا من الرمز
  });

  app.addHook('onSend', async (req, reply) => {
    reply.header('X-Content-Type-Options', 'nosniff');
    reply.header('Referrer-Policy', 'strict-origin-when-cross-origin');
    reply.header('Permissions-Policy', 'camera=(), microphone=(), geolocation=()');
    if (prod) reply.header('Strict-Transport-Security', 'max-age=15552000; includeSubDomains');
    if (!req.url.startsWith('/api')) reply.header('Content-Security-Policy', CSP);
  });

  await app.register(async (api) => {
    await api.register(publicRoutes, { db });
    await api.register(accountRoutes, { db });
    await api.register(adminRoutes, { db, prefix: '/admin' });
    api.setNotFoundHandler((req, reply) => reply.code(404).send({ error: 'not_found', message: 'المسار غير موجود' }));
  }, { prefix: '/api' });

  if (staticDir && existsSync(staticDir)) {
    await app.register(fstatic, { root: staticDir, wildcard: true, index: ['index.html'], dotfiles: 'deny' });
  }

  app.setErrorHandler((err, req, reply) => {
    if (err.validation) return reply.code(400).send({ error: 'bad_request', message: 'بيانات غير صالحة', details: err.validation.map((v) => `${v.instancePath || v.params?.missingProperty || ''} ${v.message}`.trim()) });
    if (err.statusCode && err.statusCode < 500) return reply.code(err.statusCode).send({ error: err.code || 'error', message: err.message });
    req.log.error(err);
    return reply.code(500).send({ error: 'internal', message: 'حدث خطأ غير متوقع. حاول مرة أخرى.' });
  });
  return app;
}
