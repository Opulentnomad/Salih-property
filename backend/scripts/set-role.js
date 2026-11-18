// ترقية مستخدم مسجّل إلى دور: node scripts/set-role.js user@example.com admin|reviewer|editor|user
import { fileURLToPath } from 'node:url';
import { dirname, join } from 'node:path';
import { openDb } from '../src/db.js';

const [email, role] = process.argv.slice(2);
if (!email || !['user', 'editor', 'reviewer', 'admin'].includes(role)) { console.error('الاستخدام: npm run set-role -- user@example.com admin|reviewer|editor|user'); process.exit(1); }
const db = openDb(process.env.DB_FILE || join(dirname(fileURLToPath(import.meta.url)), '../data/safarna.db'));
const r = db.prepare('UPDATE users SET role=? WHERE email=?').run(role, email);
console.log(r.changes ? `تم: ${email} ← ${role}` : 'لا يوجد مستخدم بهذا البريد (يجب أن يسجّل أولًا).');
process.exit(r.changes ? 0 : 2);
