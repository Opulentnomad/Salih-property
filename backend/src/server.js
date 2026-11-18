import { fileURLToPath } from 'node:url';
import { dirname, join } from 'node:path';
import { openDb } from './db.js';
import { seed } from './seed.js';
import { buildApp } from './app.js';

const here = dirname(fileURLToPath(import.meta.url));
const db = openDb(process.env.DB_FILE || join(here, '../data/safarna.db'));
await seed(db, { adminEmail: process.env.ADMIN_EMAIL, adminPassword: process.env.ADMIN_PASSWORD });

const app = await buildApp({
  db, logger: { level: process.env.LOG_LEVEL || 'info' },
  staticDir: process.env.STATIC_DIR || join(here, '../../app'),
  corsOrigin: process.env.CORS_ORIGIN,
});
const port = Number(process.env.PORT || 3000);
await app.listen({ port, host: process.env.HOST || '0.0.0.0' });
