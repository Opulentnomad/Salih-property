// يبني ملف HTML واحدًا مستقلًا (بدون خادم) من app/ مع كل الوجهات من القاعدة: ../preview.html
import { readFileSync, writeFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { dirname, join } from 'node:path';
import { openDb } from '../src/db.js';
import { seed, loadClientData } from '../src/seed.js';
import { destDto, latestPublished } from '../src/dto.js';

const here = dirname(fileURLToPath(import.meta.url));
const app = join(here, '../../app');
const db = openDb(':memory:');
await seed(db);
const base = loadClientData();
const destinations = db.prepare('SELECT * FROM destinations ORDER BY rating DESC, id').all().map((d) => destDto(d, latestPublished(db, d.id)));
const data = { ...base, regions: { ...base.regions }, destinations };
const json = JSON.stringify(data).replace(/</g, '\\u003c');

let html = readFileSync(join(app, 'index.html'), 'utf8');
const css = readFileSync(join(app, 'styles.css'), 'utf8');
const js = readFileSync(join(app, 'app.js'), 'utf8').replace(/<\/script/gi, '<\\/script');
html = html
  .replace('<link rel="stylesheet" href="styles.css">', `<style>${css}</style>`)
  .replace('<link rel="manifest" href="manifest.webmanifest">\n', '')
  .replace('<script src="data.js"></script>', `<script>window.SAFARNA_DATA=${json};</script>`)
  .replace('<script src="app.js"></script>', `<script>${js}</script>`);
const icon = 'data:image/svg+xml;base64,' + Buffer.from(readFileSync(join(app, 'icon.svg'))).toString('base64');
html = html.replaceAll('icon.svg', icon);
writeFileSync(join(here, '../../preview.html'), html);
console.log('preview.html', Math.round(html.length / 1024) + 'KB', destinations.length, 'destinations');
