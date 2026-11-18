// يحوّل docs/legal/*.md إلى app/legal.js (عرضها داخل التطبيق). المصدر الوحيد للنص هو ملفات md: عدّلها ثم: npm run legal
import { readFileSync, writeFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { dirname, join } from 'node:path';

const here = dirname(fileURLToPath(import.meta.url));
const esc = (s) => s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
const inline = (s) => esc(s)
  .replace(/\*\*(.+?)\*\*/g, '<b>$1</b>')
  .replace(/\[([^\]]+)\]\((#\/[a-z]+)\)/g, '<a href="$2">$1</a>')           // روابط داخلية فقط
  .replace(/\[([^\]\n]+)\](?!\()/g, '<mark class="ph">[$1]</mark>');         // الأماكن المطلوب تعبئتها

export function mdToHtml(md) {
  const out = []; let list = false, quote = [];
  const flushList = () => { if (list) { out.push('</ul>'); list = false; } };
  const flushQuote = () => { if (quote.length) { out.push(`<p class="note">${inline(quote.join(' '))}</p>`); quote = []; } };
  for (const raw of md.split(/\r?\n/)) {
    const l = raw.trimEnd();
    if (/^>\s?/.test(l)) { flushList(); quote.push(l.replace(/^>\s?/, '')); continue; }
    flushQuote();
    if (/^# /.test(l)) { flushList(); out.push(`<h1>${inline(l.slice(2))}</h1>`); }
    else if (/^## /.test(l)) { flushList(); out.push(`<h2>${inline(l.slice(3))}</h2>`); }
    else if (/^- /.test(l)) { if (!list) { out.push('<ul>'); list = true; } out.push(`<li>${inline(l.slice(2))}</li>`); }
    else if (l.trim() === '') flushList();
    else { flushList(); out.push(`<p>${inline(l)}</p>`); }
  }
  flushList(); flushQuote();
  return out.join('\n');
}

const read = (f) => mdToHtml(readFileSync(join(here, '../../docs/legal', f), 'utf8'));
const data = { privacy: read('privacy-policy.md'), terms: read('terms-of-use.md') };
writeFileSync(join(here, '../../app/legal.js'), `/* مولَّد من docs/legal/*.md — لا تعدّله يدويًا (npm run legal) */\nwindow.SAFARNA_LEGAL=${JSON.stringify(data).replace(/</g, '\\u003c')};\n`);
console.log('legal.js', Object.fromEntries(Object.entries(data).map(([k, v]) => [k, v.length])));
