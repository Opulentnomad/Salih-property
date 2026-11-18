/* لوحة الإدارة — تعمل على واجهة /api/admin (تحتاج حسابًا بدور editor أو reviewer أو admin). */
(() => {
const m = document.getElementById('m'), $ = (s) => document.querySelector(s);
const VL = { free: 'بدون تأشيرة', gcc: 'الدخول بالهوية', voa: 'عند الوصول', evisa: 'تأشيرة إلكترونية', eta: 'ETA', required: 'تأشيرة مسبقة' };
const esc = (s) => String(s ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const MN = ['يناير', 'فبراير', 'مارس', 'أبريل', 'مايو', 'يونيو', 'يوليو', 'أغسطس', 'سبتمبر', 'أكتوبر', 'نوفمبر', 'ديسمبر'];
const FN = { visa: 'نوع الدخول', stay_days: 'مدة الإقامة', passport_validity: 'صلاحية الجواز', docs: 'المستندات', vaccines: 'التطعيمات', quarantine: 'الحجر', embassy: 'السفارة', source: 'المصدر', source_url: 'رابط المصدر' };
let token = sessionStorage.getItem('adm_token'), role = sessionStorage.getItem('adm_role'), tab = 'dest';
const toast = (t) => { const e = $('#toast'); e.textContent = t; e.classList.add('show'); setTimeout(() => e.classList.remove('show'), 2400); };
const api = async (path, o = {}) => {
  const r = await fetch('/api' + path, { method: o.method || 'GET', headers: { ...(o.body ? { 'content-type': 'application/json' } : {}), ...(token ? { authorization: 'Bearer ' + token } : {}) }, body: o.body ? JSON.stringify(o.body) : undefined });
  let j = null; try { j = await r.json(); } catch {}
  if (!r.ok) throw Object.assign(new Error(j?.message || 'تعذّر تنفيذ الطلب'), { status: r.status });
  return j;
};

function login(msg = '') {
  $('#out').hidden = true;
  m.innerHTML = `<h1>تسجيل الدخول</h1><form class="panel" id="lf"><label for="e">البريد</label><input id="e" type="email" required autocomplete="username"><label for="p">كلمة المرور</label><input id="p" type="password" required autocomplete="current-password"><button class="btn" style="margin-top:12px">دخول</button><p class="mu" role="alert" id="err">${esc(msg)}</p></form>`;
  $('#lf').onsubmit = async (ev) => {
    ev.preventDefault();
    try {
      const j = await api('/auth/login', { method: 'POST', body: { email: $('#e').value.trim(), password: $('#p').value } });
      if (!['editor', 'reviewer', 'admin'].includes(j.user.role)) return login('هذا الحساب ليس لديه صلاحية الإدارة.');
      token = j.token; role = j.user.role; sessionStorage.setItem('adm_token', token); sessionStorage.setItem('adm_role', role); shell();
    } catch (e) { login(e.status === 401 ? 'البريد أو كلمة المرور غير صحيحة.' : 'تعذّر الاتصال بالخادم. شغّل الخادم من مجلد backend.'); }
  };
}
$('#out').onclick = () => { sessionStorage.clear(); token = null; login(); };

function shell() {
  $('#out').hidden = false;
  const tabs = [['dest', 'الوجهات'], ['cal', 'التقويم'], ...(role !== 'editor' ? [['rev', 'المراجعات']] : []), ...(role === 'admin' ? [['aud', 'سجل التدقيق']] : [])];
  m.innerHTML = `<div class="tabs" role="tablist">${tabs.map(([k, v]) => `<button class="tab" role="tab" data-t="${k}" aria-selected="${k === tab}">${v}</button>`).join('')}</div><div id="body"></div>`;
  m.querySelectorAll('[data-t]').forEach((b) => b.onclick = () => { tab = b.dataset.t; shell(); });
  ({ dest: listDest, cal: listCal, rev: listRev, aud: listAudit })[tab]().catch(fail);
}
const fail = (e) => { if (e.status === 401) { sessionStorage.clear(); token = null; return login('انتهت الجلسة. سجّل الدخول من جديد.'); } toast(e.message); };

let flt = { q: '', unrev: false };
async function listDest() {
  const j = await api('/destinations?limit=300');
  const items = j.items.filter((d) => (!flt.unrev || !d.req.reviewed) && (!flt.q || (d.ar + d.en + d.iata).toLowerCase().includes(flt.q.toLowerCase())));
  const left = j.items.filter((d) => !d.req.reviewed).length;
  $('#body').innerHTML = `<div class="search"><input id="fq" type="search" placeholder="ابحث عن دولة" value="${esc(flt.q)}" aria-label="بحث"></div>
  <div class="seg" role="group"><button data-u="0" aria-pressed="${!flt.unrev}">الكل (${j.total})</button><button data-u="1" aria-pressed="${flt.unrev}">بانتظار المراجعة (${left})</button></div>` +
    items.map((d) => `<article class="pass"><div class="main"><div class="eyebrow">${esc(VL[d.req.visa])} · إصدار ${d.req.version} · ${d.req.reviewed ? '✓ مراجَع' : 'غير مراجَع'}${d.req.stale ? ' · ⚠️ قديم' : ''}</div><div class="code big" style="font-size:40px">${esc(d.iata)}</div><div class="dest">${esc(d.ar)}</div></div><div class="stub"><button class="btn sm" data-e="${esc(d.id)}">تحرير</button></div></article>`).join('');
  $('#fq').oninput = (e) => { flt.q = e.target.value; clearTimeout(listDest._t); listDest._t = setTimeout(() => listDest().then(() => { const q = $('#fq'); q.focus(); q.setSelectionRange(99, 99); }).catch(fail), 250); };
  m.querySelectorAll('[data-u]').forEach((b) => b.onclick = () => { flt.unrev = b.dataset.u === '1'; listDest().catch(fail); });
  m.querySelectorAll('[data-e]').forEach((b) => b.onclick = () => edit(b.dataset.e).catch(fail));
}

async function edit(id) {
  const [d, hist] = await Promise.all([api('/destinations/' + id), api('/admin/requirements?destination=' + id)]);
  const r = hist.items.find((x) => x.status === 'draft') || d.req;
  $('#body').innerHTML = `<h1>${esc(d.ar)} <span class="code" style="font-size:26px">${esc(d.iata)}</span></h1>
  <form class="panel" id="ef"><p class="mu">الحفظ ينشئ مسودة إصدار جديد ولا يظهر للمستخدمين حتى يُنشر${role === 'editor' ? ' (النشر للمراجع)' : ''}.</p>
  <label for="v">نوع الدخول</label><select id="v">${Object.entries(VL).map(([k, v]) => `<option value="${k}" ${r.visa === k ? 'selected' : ''}>${v}</option>`).join('')}</select>
  <div class="two"><div><label for="st">أقصى مدة (يوم)</label><input id="st" type="number" min="0" value="${r.stay ?? 0}"></div><div><label for="pp">صلاحية الجواز</label><input id="pp" maxlength="80" value="${esc(r.passport)}"></div></div>
  <label for="dc">المستندات (سطر لكل مستند)</label><textarea id="dc" rows="6">${esc(r.docs.join('\n'))}</textarea>
  <label for="vc">التطعيمات</label><input id="vc" maxlength="200" value="${esc(r.vaccines)}">
  <label for="qr">الحجر الصحي</label><input id="qr" maxlength="200" value="${esc(r.quarantine)}">
  <label for="src">المصدر الرسمي</label><input id="src" maxlength="150" value="${esc(r.source)}">
  <label for="su">رابط المصدر (https)</label><input id="su" type="url" pattern="https://.*" value="${esc(r.sourceUrl || '')}">
  <div class="actions" style="margin-top:14px"><button class="btn">حفظ كمسودة</button><button type="button" class="btn ghost" id="bk">رجوع</button></div></form>
  <h2>أفضل أشهر الزيارة</h2><form class="panel" id="mf"><div class="mstrip" role="group" aria-label="الأشهر">${MN.map((n, i) => `<label class="chip" style="display:flex;gap:6px;align-items:center;margin:0;min-height:44px"><input type="checkbox" style="width:auto;min-height:0" value="${i + 1}" ${(d.months || []).includes(i + 1) ? 'checked' : ''}>${n}</label>`).join('')}</div>
  <label for="mn">ملاحظة قصيرة (اختياري)</label><input id="mn" maxlength="160" value="${esc(d.monthsNote || '')}"><button class="btn" style="margin-top:12px">حفظ الأشهر</button></form>
  <h2>الإصدارات</h2>${hist.items.map((h) => `<div class="slip"><div class="who"><span>إصدار ${h.version} · ${({ published: 'منشور', draft: 'مسودة', archived: 'مؤرشف' })[h.status]}</span><span>${esc(h.verified.slice(0, 10))}</span></div>${h.status === 'draft' && role !== 'editor' ? `<button class="btn sm" data-pub="${h.id}">نشر هذا الإصدار</button>` : `<span class="mu">${esc(VL[h.visa])} · ${h.stay} يومًا</span>`}</div>`).join('')}`;
  $('#mf').onsubmit = async (ev) => {
    ev.preventDefault();
    const months = [...m.querySelectorAll('#mf input[type=checkbox]:checked')].map((c) => +c.value);
    try { await api('/admin/destinations/' + id + '/months', { method: 'PUT', body: { months, note: $('#mn').value || null } }); toast('حُفظت الأشهر'); } catch (e) { fail(e); }
  };
  $('#bk').onclick = () => shell();
  $('#ef').onsubmit = async (ev) => {
    ev.preventDefault();
    const body = { visa: $('#v').value, stay: +$('#st').value, passport: $('#pp').value, docs: $('#dc').value.split('\n').map((s) => s.trim()).filter(Boolean), vaccines: $('#vc').value, quarantine: $('#qr').value, source: $('#src').value };
    if ($('#su').value) body.sourceUrl = $('#su').value;
    try { await api('/admin/requirements/' + id, { method: 'PUT', body }); toast('حُفظت المسودة'); edit(id); } catch (e) { fail(e); }
  };
  m.querySelectorAll('[data-pub]').forEach((b) => b.onclick = async () => { try { const j = await api(`/admin/requirements/${b.dataset.pub}/publish`, { method: 'POST' }); toast('نُشر الإصدار. تغيّر: ' + (j.changed.map((k) => FN[k] || k).join('، ') || 'لا شيء')); edit(id); } catch (e) { fail(e); } });
}

const KINDS = { religious: 'ديني', national: 'وطني', school: 'مدارس', custom: 'أخرى' };
async function listCal() {
  const j = await api('/calendar?from=2025-01-01&to=2032-12-31');
  $('#body').innerHTML = `<p class="mu">تواريخ الأعياد محسوبة من تقويم أم القرى. بعد الإعلان الرسمي عدّل التاريخ إن اختلف واضغط «مؤكد». أضف إجازات المدارس من إعلان وزارة التعليم.</p><button class="btn" id="ne" style="margin:8px 0 14px">＋ إضافة حدث</button>` +
    j.items.map((e) => `<article class="slip"><div class="who"><span>${esc(KINDS[e.kind])} · ${e.confirmed ? '✓ مؤكد' : 'تقديري'}</span><span>${esc(e.start)}${e.end !== e.start ? ' → ' + esc(e.end) : ''}</span></div><b>${esc(e.name)}</b>${e.hijri ? `<span class="mu"> · ${esc(e.hijri)}</span>` : ''}<div class="actions"><button class="btn sm ghost" data-ce="${e.id}">تعديل</button></div></article>`).join('');
  const byId = Object.fromEntries(j.items.map((e) => [e.id, e]));
  $('#ne').onclick = () => editCal({ name: '', kind: 'school', start: '', end: '', note: '', confirmed: true });
  m.querySelectorAll('[data-ce]').forEach((b) => b.onclick = () => editCal(byId[b.dataset.ce]));
}
function editCal(e) {
  $('#body').innerHTML = `<h1>${e.id ? 'تعديل حدث' : 'حدث جديد'}</h1><form class="panel" id="cf">
  <label for="cn">الاسم</label><input id="cn" required minlength="2" maxlength="80" value="${esc(e.name)}">
  <label for="ck">النوع</label><select id="ck">${Object.entries(KINDS).map(([k, v]) => `<option value="${k}" ${e.kind === k ? 'selected' : ''}>${v}</option>`).join('')}</select>
  <div class="two"><div><label for="cs">من</label><input id="cs" type="date" required value="${esc(e.start)}"></div><div><label for="ce">إلى</label><input id="ce" type="date" required value="${esc(e.end)}"></div></div>
  <label for="cno">ملاحظة</label><input id="cno" maxlength="200" value="${esc(e.note || '')}">
  <label class="doc"><input type="checkbox" id="cc" ${e.confirmed ? 'checked' : ''}><span>مؤكد رسميًا</span></label>
  <div class="actions" style="margin-top:12px"><button class="btn">حفظ</button><button type="button" class="btn ghost" id="cb">رجوع</button>${e.id && role !== 'editor' ? '<button type="button" class="btn ghost" id="cd">حذف</button>' : ''}</div></form>`;
  $('#cb').onclick = () => shell();
  $('#cd')?.addEventListener('click', async () => { if (!confirm('حذف هذا الحدث؟')) return; try { await api('/admin/calendar/' + e.id, { method: 'DELETE' }); toast('حُذف الحدث'); shell(); } catch (er) { fail(er); } });
  $('#cf').onsubmit = async (ev) => {
    ev.preventDefault();
    const body = { name: $('#cn').value.trim(), kind: $('#ck').value, start: $('#cs').value, end: $('#ce').value, note: $('#cno').value || null, confirmed: $('#cc').checked };
    try { await api(e.id ? '/admin/calendar/' + e.id : '/admin/calendar', { method: e.id ? 'PUT' : 'POST', body }); toast('حُفظ الحدث'); shell(); } catch (er) { fail(er); }
  };
}

async function listRev() {
  const j = await api('/admin/reviews?status=pending');
  $('#body').innerHTML = j.items.map((r) => `<article class="slip"><div class="who"><span>${esc(r.author)} · ${esc(r.destination_id)}</span><span>${'★'.repeat(r.rating)}</span></div><p>${esc(r.body)}</p><div class="actions"><button class="btn sm" data-ok="${r.id}">نشر</button><button class="btn sm ghost" data-no="${r.id}">رفض</button></div></article>`).join('') || '<div class="hint">لا مراجعات بانتظار الموافقة.</div>';
  const mod = (id, status) => api(`/admin/reviews/${id}/moderate`, { method: 'POST', body: { status } }).then(() => { toast(status === 'published' ? 'نُشرت المراجعة' : 'رُفضت المراجعة'); listRev(); }).catch(fail);
  m.querySelectorAll('[data-ok]').forEach((b) => b.onclick = () => mod(b.dataset.ok, 'published'));
  m.querySelectorAll('[data-no]').forEach((b) => b.onclick = () => mod(b.dataset.no, 'rejected'));
}

async function listAudit() {
  const j = await api('/admin/audit');
  $('#body').innerHTML = j.items.map((a) => `<div class="slip"><div class="who"><span>${esc(a.action)}</span><span>${esc(a.at)}</span></div><span class="mu">${esc(a.entity)} #${esc(a.entity_id)} · بواسطة ${esc(a.actor ?? '—')} · ${esc(JSON.stringify(a.meta))}</span></div>`).join('') || '<div class="hint">لا سجلات بعد.</div>';
}

token ? shell() : login();
})();
