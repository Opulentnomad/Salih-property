import { readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { dirname, join } from 'node:path';

const here = dirname(fileURLToPath(import.meta.url));
const SEASON_MONTHS = { winter: [12, 1, 2], spring: [3, 4, 5], summer: [6, 7, 8], autumn: [9, 10, 11] };

/** فصل يُعدّ مناسبًا إن كان شهران على الأقل من أشهره ضمن الأشهر الأنسب. */
export function seasonsFromMonths(months) {
  return Object.entries(SEASON_MONTHS).filter(([, ms]) => ms.filter((m) => months.includes(m)).length >= 2).map(([s]) => s);
}

/** يضيف أفضل الأشهر للوجهات التي لا أشهر لها بعد (لا يستبدل ما حرّره محرر). بيانات تقريبية عامة. */
export function applyBestMonths(db, file = join(here, '../data/best-months.json')) {
  const data = JSON.parse(readFileSync(file, 'utf8'));
  const upd = db.prepare('UPDATE destinations SET best_months=?, months_note=?, seasons=? WHERE id=? AND best_months IS NULL');
  let n = 0;
  for (const [id, v] of Object.entries(data)) {
    const months = [...v.months].sort((a, b) => a - b);
    n += Number(upd.run(JSON.stringify(months), v.note || null, JSON.stringify(seasonsFromMonths(months)), id).changes);
  }
  return n;
}
