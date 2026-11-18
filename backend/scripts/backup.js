// نسخة احتياطية متسقة من قاعدة SQLite (آمنة أثناء عمل الخادم): npm run backup
import { fileURLToPath } from 'node:url';
import { dirname, join } from 'node:path';
import { mkdirSync, readdirSync, rmSync } from 'node:fs';
import { openDb } from '../src/db.js';

const here = dirname(fileURLToPath(import.meta.url));
const src = process.env.DB_FILE || join(here, '../data/safarna.db');
const dir = process.env.BACKUP_DIR || join(dirname(src), 'backups');
const keep = Number(process.env.BACKUP_KEEP || 14);
mkdirSync(dir, { recursive: true });
const file = join(dir, `safarna-${new Date().toISOString().replace(/[:T]/g, '-').slice(0, 16)}.db`);
openDb(src).exec(`VACUUM INTO '${file.replace(/'/g, "''")}'`);
const all = readdirSync(dir).filter((f) => f.startsWith('safarna-') && f.endsWith('.db')).sort();
for (const old of all.slice(0, Math.max(0, all.length - keep))) rmSync(join(dir, old));
console.log('نُسخت إلى', file);
