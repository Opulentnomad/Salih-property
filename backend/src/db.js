import { DatabaseSync } from 'node:sqlite';
import { mkdirSync } from 'node:fs';
import { dirname } from 'node:path';

const SCHEMA = `
PRAGMA journal_mode=WAL; PRAGMA foreign_keys=ON;
CREATE TABLE IF NOT EXISTS users(
  id INTEGER PRIMARY KEY, email TEXT UNIQUE NOT NULL COLLATE NOCASE, password_hash TEXT NOT NULL,
  name TEXT, role TEXT NOT NULL DEFAULT 'user' CHECK(role IN('user','editor','reviewer','admin')),
  prefs TEXT NOT NULL DEFAULT '{}', created_at TEXT NOT NULL DEFAULT (datetime('now')));
CREATE TABLE IF NOT EXISTS destinations(
  id TEXT PRIMARY KEY, iata TEXT NOT NULL, name_ar TEXT NOT NULL, name_en TEXT, city TEXT, emoji TEXT,
  g1 TEXT, g2 TEXT, region TEXT, budget INTEGER CHECK(budget BETWEEN 1 AND 3), seasons TEXT NOT NULL DEFAULT '[]',
  tags TEXT NOT NULL DEFAULT '[]', flight TEXT, currency TEXT, rating REAL, about_ar TEXT, cca3 TEXT, best_months TEXT, months_note TEXT);
CREATE TABLE IF NOT EXISTS entry_requirements(
  id INTEGER PRIMARY KEY, destination_id TEXT NOT NULL REFERENCES destinations(id) ON DELETE CASCADE,
  passport_country TEXT NOT NULL DEFAULT 'SA', version INTEGER NOT NULL,
  status TEXT NOT NULL CHECK(status IN('draft','published','archived')),
  visa TEXT NOT NULL CHECK(visa IN('free','gcc','voa','evisa','eta','required')),
  stay_days INTEGER, passport_validity TEXT, docs TEXT NOT NULL DEFAULT '[]', vaccines TEXT, quarantine TEXT,
  embassy TEXT NOT NULL DEFAULT '{}', source TEXT NOT NULL, source_url TEXT, verified_at TEXT NOT NULL,
  reviewed INTEGER NOT NULL DEFAULT 0,
  created_by INTEGER REFERENCES users(id), created_at TEXT NOT NULL DEFAULT (datetime('now')),
  UNIQUE(destination_id, passport_country, version));
CREATE INDEX IF NOT EXISTS idx_req_pub ON entry_requirements(destination_id, passport_country, status);
CREATE TABLE IF NOT EXISTS requirement_changes(
  id INTEGER PRIMARY KEY, requirement_id INTEGER NOT NULL REFERENCES entry_requirements(id) ON DELETE CASCADE,
  diff TEXT NOT NULL, changed_by INTEGER REFERENCES users(id), at TEXT NOT NULL DEFAULT (datetime('now')));
CREATE TABLE IF NOT EXISTS favorites(
  user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  destination_id TEXT NOT NULL REFERENCES destinations(id) ON DELETE CASCADE,
  created_at TEXT NOT NULL DEFAULT (datetime('now')), PRIMARY KEY(user_id,destination_id));
CREATE TABLE IF NOT EXISTS trips(
  id INTEGER PRIMARY KEY, user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  destination_id TEXT NOT NULL REFERENCES destinations(id), start_date TEXT, days INTEGER NOT NULL DEFAULT 5,
  plan TEXT NOT NULL DEFAULT '[]', created_at TEXT NOT NULL DEFAULT (datetime('now')));
CREATE TABLE IF NOT EXISTS reviews(
  id INTEGER PRIMARY KEY, user_id INTEGER REFERENCES users(id) ON DELETE SET NULL,
  destination_id TEXT NOT NULL REFERENCES destinations(id) ON DELETE CASCADE, author TEXT NOT NULL,
  rating INTEGER NOT NULL CHECK(rating BETWEEN 1 AND 5), body TEXT NOT NULL CHECK(length(body)<=1000),
  status TEXT NOT NULL DEFAULT 'pending' CHECK(status IN('pending','published','rejected')),
  created_at TEXT NOT NULL DEFAULT (datetime('now')));
CREATE TABLE IF NOT EXISTS alert_subscriptions(
  user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  destination_id TEXT NOT NULL REFERENCES destinations(id) ON DELETE CASCADE,
  channels TEXT NOT NULL DEFAULT '["push","email"]', created_at TEXT NOT NULL DEFAULT (datetime('now')),
  PRIMARY KEY(user_id,destination_id));
CREATE TABLE IF NOT EXISTS fx_rates(base TEXT NOT NULL, quote TEXT NOT NULL, rate REAL NOT NULL, fetched_at TEXT NOT NULL, PRIMARY KEY(base,quote));
CREATE TABLE IF NOT EXISTS affiliate_links(
  id INTEGER PRIMARY KEY, destination_id TEXT REFERENCES destinations(id), partner TEXT NOT NULL,
  kind TEXT NOT NULL CHECK(kind IN('flights','hotels')), url_template TEXT NOT NULL, active INTEGER NOT NULL DEFAULT 1);
CREATE TABLE IF NOT EXISTS affiliate_clicks(id INTEGER PRIMARY KEY, link_id INTEGER REFERENCES affiliate_links(id), user_id INTEGER, at TEXT NOT NULL DEFAULT (datetime('now')));
CREATE TABLE IF NOT EXISTS saudi_events(
  id INTEGER PRIMARY KEY, key TEXT UNIQUE, name_ar TEXT NOT NULL,
  kind TEXT NOT NULL CHECK(kind IN('religious','national','school','custom')),
  start_date TEXT NOT NULL, end_date TEXT NOT NULL, hijri TEXT, note TEXT,
  source TEXT NOT NULL DEFAULT 'manual', confirmed INTEGER NOT NULL DEFAULT 0);
CREATE INDEX IF NOT EXISTS idx_events_start ON saudi_events(start_date);
CREATE TABLE IF NOT EXISTS audit_log(id INTEGER PRIMARY KEY, actor INTEGER, action TEXT NOT NULL, entity TEXT, entity_id TEXT, meta TEXT, at TEXT NOT NULL DEFAULT (datetime('now')));
`;

export function openDb(file = ':memory:') {
  if (file !== ':memory:') mkdirSync(dirname(file), { recursive: true });
  const db = new DatabaseSync(file);
  db.exec(SCHEMA);
  // ترحيل قواعد أُنشئت قبل إضافة هذه الأعمدة
  const has = (t, c) => db.prepare(`PRAGMA table_info(${t})`).all().some((r) => r.name === c);
  if (!has('destinations', 'cca3')) db.exec('ALTER TABLE destinations ADD COLUMN cca3 TEXT');
  if (!has('destinations', 'best_months')) db.exec('ALTER TABLE destinations ADD COLUMN best_months TEXT');
  if (!has('destinations', 'months_note')) db.exec('ALTER TABLE destinations ADD COLUMN months_note TEXT');
  if (!has('entry_requirements', 'reviewed')) db.exec('ALTER TABLE entry_requirements ADD COLUMN reviewed INTEGER NOT NULL DEFAULT 0');
  return db;
}

export const tx = (db, fn) => {
  db.exec('BEGIN');
  try { const r = fn(); db.exec('COMMIT'); return r; } catch (e) { db.exec('ROLLBACK'); throw e; }
};

export const audit = (db, actor, action, entity, entityId, meta = {}) =>
  db.prepare('INSERT INTO audit_log(actor,action,entity,entity_id,meta) VALUES(?,?,?,?,?)')
    .run(actor ?? null, action, entity, String(entityId), JSON.stringify(meta));
