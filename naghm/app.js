(() => {
'use strict';
const APP = 'naghm';
const $ = (s, r = document) => r.querySelector(s);
const esc = s => String(s ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const SRC = { audius: 'Audius', jamendo: 'Jamendo', youtube: 'YouTube' };

/* ---------------- native (Android) ---------------- */
const NATIVE = !!(window.Capacitor && window.Capacitor.isNativePlatform && window.Capacitor.isNativePlatform());
const NP = n => (NATIVE && window.Capacitor.Plugins && window.Capacitor.Plugins[n]) || null;
const Bg = {
  on: false, timer: null, plugin: () => NP('BackgroundMode'),
  opts(t) {
    return { title: t ? t.title : 'نغم', text: t ? (t.artist || SRC[t.src]) : 'جاهز للتشغيل', subText: 'نغم', bigText: false, resume: true, silent: false, hidden: false,
      color: '#7656F5', icon: 'ic_stat_naghm', channelName: 'تشغيل الموسيقى', channelDescription: 'يُبقي الموسيقى تعمل عند إغلاق الشاشة', allowClose: false,
      showWhen: false, visibility: 'public', disableWebViewOptimization: true, allowMicrophoneInBackground: false };
  },
  async start(t) {
    const B = this.plugin(); if (!B) return; clearTimeout(this.timer);
    try {
      if (!this.on) {
        try { const st = await B.checkNotificationsPermission(); if (st.notifications !== 'granted') await B.requestNotificationsPermission(); } catch {}
        await B.enable(this.opts(t)); await B.disableWebViewOptimizations(); this.on = true;
      } else await B.updateNotification(this.opts(t));
    } catch (e) { console.error('bg', e); }
  },
  idle() { if (!this.on) return; clearTimeout(this.timer); this.timer = setTimeout(() => this.stop(), 5 * 60 * 1000); },
  async stop() { const B = this.plugin(); if (!B || !this.on) return; try { await B.enableWebViewOptimizations(); await B.disable(); } catch {} this.on = false; }
};

/* ---------------- moods ---------------- */
const MOODS = [
  { id: 'focus',  ar: 'تركيز', g: 'a2', q: ['lofi', 'focus', 'ambient study'],       jt: ['lofi', 'ambient'] },
  { id: 'chill',  ar: 'هدوء',  g: 'a3', q: ['chill', 'acoustic chill', 'downtempo'], jt: ['chillout', 'relaxation'] },
  { id: 'energy', ar: 'طاقة',  g: 'a1', q: ['workout', 'edm', 'upbeat'],             jt: ['energetic', 'dance'] },
  { id: 'sleep',  ar: 'نوم',   g: 'a4', q: ['sleep', 'ambient piano', 'calm'],       jt: ['ambient', 'newage'] },
  { id: 'happy',  ar: 'سعادة', g: 'a5', q: ['feel good', 'happy pop', 'summer'],     jt: ['happy', 'pop'] },
  { id: 'sad',    ar: 'شجن',   g: 'a6', q: ['sad', 'melancholy', 'emotional'],       jt: ['sad', 'melancholic'] }
];
const GENRES = [['pop','بوب'],['lofi','لوفاي'],['rock','روك'],['electronic','إلكترونيك'],['jazz','جاز'],['hip hop','هيب هوب'],['acoustic','أكوستيك']];

/* ---------------- store ---------------- */
const KEY = 'naghm.v1';
const DEF = { onboarded: false, moods: [], src: { audius: true, jamendo: false, youtube: true }, jamendoId: '', favs: [], lists: [], recent: [], daily: null };
let S = load();
function load() { try { const o = JSON.parse(localStorage.getItem(KEY) || '{}'); return { ...DEF, ...o, src: { ...DEF.src, ...(o.src || {}) } }; } catch { return JSON.parse(JSON.stringify(DEF)); } }
function save() { try { localStorage.setItem(KEY, JSON.stringify(S)); } catch {} }

/* ---------------- helpers ---------------- */
function hash(s) { let h = 2166136261; for (let i = 0; i < s.length; i++) { h ^= s.charCodeAt(i); h = Math.imul(h, 16777619); } return h >>> 0; }
function rng(seed) { let a = seed >>> 0; return () => { a |= 0; a = a + 0x6D2B79F5 | 0; let t = Math.imul(a ^ a >>> 15, 1 | a); t = t + Math.imul(t ^ t >>> 7, 61 | t) ^ t; return ((t ^ t >>> 14) >>> 0) / 4294967296; }; }
function shuffle(arr, seed) { const r = rng(seed), a = arr.slice(); for (let i = a.length - 1; i > 0; i--) { const j = Math.floor(r() * (i + 1)); [a[i], a[j]] = [a[j], a[i]]; } return a; }
const today = () => new Date().toISOString().slice(0, 10);
const grad = id => 'a' + (hash(id) % 6 + 1);
const fmt = s => { s = Math.max(0, Math.floor(s || 0)); return Math.floor(s / 60) + ':' + String(s % 60).padStart(2, '0'); };
const slim = t => ({ id: t.id, src: t.src, sid: t.sid, title: t.title, artist: t.artist, art: t.art || '', dur: t.dur || 0, url: t.src === 'jamendo' ? t.url : undefined, page: t.page });
let toastT; function toast(m) { const e = $('#toast'); e.textContent = m; e.hidden = false; clearTimeout(toastT); toastT = setTimeout(() => e.hidden = true, 2400); }
const fetchJSON = async (u, ms = 10000) => { const r = await fetch(u, { signal: AbortSignal.timeout(ms) }); if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); };
const known = new Map(); const reg = ts => ts.forEach(t => known.set(t.id, t));
const trackById = id => known.get(id) || S.favs.find(t => t.id === id) || S.recent.find(t => t.id === id) || S.lists.flatMap(l => l.items).find(t => t.id === id);

/* ---------------- providers ---------------- */
const Audius = {
  hosts: null, h: null,
  async init() {
    if (this.hosts) return;
    try { const j = await fetchJSON('https://api.audius.co', 8000); this.hosts = (j.data || []).slice(0, 4); } catch { this.hosts = []; }
    if (!this.hosts.length) this.hosts = ['https://api.audius.co', 'https://discoveryprovider.audius.co'];
  },
  async call(path, params) {
    await this.init(); let err;
    for (const h of this.hosts) {
      try {
        const u = new URL(h + path);
        Object.entries({ ...params, app_name: APP }).forEach(([k, v]) => u.searchParams.set(k, v));
        const j = await fetchJSON(u); this.h = h; return j.data;
      } catch (e) { err = e; }
    }
    throw err || new Error('audius');
  },
  map(t) {
    const a = t.artwork || {};
    return { id: 'audius:' + t.id, src: 'audius', sid: t.id, title: t.title, artist: (t.user && t.user.name) || '', art: a['480x480'] || a['150x150'] || '', dur: t.duration || 0, page: t.permalink ? 'https://audius.co' + t.permalink : 'https://audius.co' };
  },
  async search(q, limit = 20) { const d = await this.call('/v1/tracks/search', { query: q, limit }); return (d || []).filter(t => t.is_streamable !== false).map(t => this.map(t)); },
  async trending(limit = 20) { const d = await this.call('/v1/tracks/trending', { limit }); return (d || []).map(t => this.map(t)); },
  async streamUrl(sid) { await this.init(); return `${this.h || this.hosts[0]}/v1/tracks/${sid}/stream?app_name=${APP}`; }
};
const Jamendo = {
  async get(params) {
    if (!S.jamendoId) throw new Error('no-jamendo-id');
    const u = new URL('https://api.jamendo.com/v3.0/tracks/');
    Object.entries({ client_id: S.jamendoId, format: 'json', limit: 20, audioformat: 'mp32', imagesize: 300, ...params }).forEach(([k, v]) => u.searchParams.set(k, v));
    const j = await fetchJSON(u);
    if (j.headers && j.headers.status === 'failed') throw new Error(j.headers.error_message || 'jamendo');
    return (j.results || []).map(t => ({ id: 'jamendo:' + t.id, src: 'jamendo', sid: t.id, title: t.name, artist: t.artist_name, art: t.image || '', dur: +t.duration || 0, url: t.audio, page: t.shareurl }));
  }
};
const YT = {
  parse(s) { s = (s || '').trim(); const m = s.match(/(?:youtu\.be\/|[?&]v=|embed\/|shorts\/|live\/)([\w-]{11})/) || (/^[\w-]{11}$/.test(s) ? [0, s] : null); return m ? m[1] : null; },
  async make(id) {
    let title = 'مقطع YouTube', artist = '';
    try { const j = await fetchJSON('https://www.youtube.com/oembed?format=json&url=' + encodeURIComponent('https://www.youtube.com/watch?v=' + id), 6000); title = j.title || title; artist = j.author_name || ''; } catch {}
    return { id: 'youtube:' + id, src: 'youtube', sid: id, title, artist, art: `https://i.ytimg.com/vi/${id}/hqdefault.jpg`, dur: 0, page: 'https://www.youtube.com/watch?v=' + id };
  },
  loadApi() { return new Promise(res => { if (window.YT && window.YT.Player) return res(); const s = document.createElement('script'); s.src = 'https://www.youtube.com/iframe_api'; window.onYouTubeIframeAPIReady = () => res(); document.head.appendChild(s); setTimeout(res, 10000); }); }
};

/* mood / daily */
async function fetchMood(m, seed) {
  const r = rng(seed), pick = a => a[Math.floor(r() * a.length)], jobs = [];
  if (S.src.audius) { const qs = shuffle(m.q, seed).slice(0, 2); qs.forEach(q => jobs.push(Audius.search(q, 15))); }
  if (S.src.jamendo && S.jamendoId) jobs.push(Jamendo.get({ tags: pick(m.jt), boost: 'popularity_month', limit: 15 }));
  if (!jobs.length) throw new Error('no-sources');
  const res = await Promise.allSettled(jobs), ok = res.filter(x => x.status === 'fulfilled').flatMap(x => x.value);
  if (!ok.length) throw res[0].reason || new Error('fail');
  return dedupe(ok);
}
const dedupe = ts => { const s = new Set(); return ts.filter(t => !s.has(t.id) && s.add(t.id)); };
const capArtist = (ts, n) => { const c = {}; return ts.filter(t => (c[t.artist] = (c[t.artist] || 0) + 1) <= n); };
async function makeDaily(force) {
  if (!force && S.daily && S.daily.date === today() && S.daily.items.length) return S.daily.items;
  const moods = MOODS.filter(m => !S.moods.length || S.moods.includes(m.id));
  const seed = hash(today() + (force ? Date.now() : ''));
  const res = await Promise.allSettled(moods.map((m, i) => fetchMood(m, seed + i)));
  let all = res.filter(x => x.status === 'fulfilled').flatMap(x => x.value);
  if (!all.length) throw (res.find(x => x.status === 'rejected') || {}).reason || new Error('fail');
  const seen = new Set(S.recent.slice(0, 100).map(t => t.id));
  let pool = shuffle(dedupe(all), seed), fresh = pool.filter(t => !seen.has(t.id));
  const items = capArtist(fresh.length >= 12 ? fresh : pool, 2).slice(0, 20).map(slim);
  S.daily = { date: today(), items }; save(); return items;
}

/* ---------------- player ---------------- */
const P = {
  q: [], i: -1, playing: false, audio: new Audio(), yt: null, ytTimer: null, errs: 0,
  cur() { return this.q[this.i]; },
  async play(q, i) { this.q = q.slice(); this.i = i; this.errs = 0; await this.load(); },
  async load() {
    const t = this.cur(); if (!t) return;
    this.stopAll(); this.setUI(t); this.showStage(t); Bg.start(t);
    if (t.src === 'youtube') await this.loadYT(t);
    else {
      try { this.audio.src = t.src === 'audius' ? await Audius.streamUrl(t.sid) : t.url; await this.audio.play(); }
      catch (e) { if (e && e.name === 'NotAllowedError') this.setPlaying(false); else this.fail(); }
    }
    S.recent = [slim(t), ...S.recent.filter(x => x.id !== t.id)].slice(0, 200); save();
    this.media(t); renderNow();
  },
  fail() { toast('تعذّر تشغيل هذا المقطع، ننتقل للتالي'); if (++this.errs < 4) setTimeout(() => this.next(true), 900); else this.setPlaying(false); },
  stopAll() { this.audio.pause(); clearInterval(this.ytTimer); if (this.yt && this.yt.pauseVideo) try { this.yt.pauseVideo(); } catch {} },
  async loadYT(t) {
    await YT.loadApi();
    if (!(window.YT && window.YT.Player)) { toast('تعذّر تحميل مشغّل YouTube'); return this.fail(); }
    if (this.yt && this.yt.loadVideoById) { this.yt.loadVideoById(t.sid); return; }
    this.yt = new window.YT.Player('ytbox', { videoId: t.sid, playerVars: { playsinline: 1, rel: 0, autoplay: 1, enablejsapi: 1, origin: location.origin, widget_referrer: location.href },
      events: { onReady: e => e.target.playVideo(), onStateChange: e => this.ytState(e.data), onError: () => this.fail() } });
  },
  ytState(s) {
    if (!this.cur() || this.cur().src !== 'youtube') return;
    if (s === 1) { this.setPlaying(true); clearInterval(this.ytTimer); this.ytTimer = setInterval(() => this.tick(this.yt.getCurrentTime(), this.yt.getDuration()), 500); }
    else if (s === 2) this.setPlaying(false);
    else if (s === 0) { this.setPlaying(false); this.next(true); }
  },
  toggle() {
    const t = this.cur(); if (!t) return;
    if (t.src === 'youtube') { if (!this.yt) return; this.playing ? this.yt.pauseVideo() : this.yt.playVideo(); }
    else this.audio.paused ? this.audio.play().catch(() => {}) : this.audio.pause();
  },
  next(auto) { if (!this.q.length) return; if (this.i < this.q.length - 1) { this.i++; this.load(); } else if (auto) this.setPlaying(false); else { this.i = 0; this.load(); } },
  prev() { if (!this.q.length) return; const t = this.cur(); if (t.src !== 'youtube' && this.audio.currentTime > 4) { this.audio.currentTime = 0; return; } this.i = Math.max(0, this.i - 1); this.load(); },
  seek(r) { const t = this.cur(); if (!t) return; if (t.src === 'youtube') { if (this.yt && this.yt.getDuration) this.yt.seekTo(r * this.yt.getDuration(), true); } else if (isFinite(this.audio.duration)) this.audio.currentTime = r * this.audio.duration; },
  playNext(t) { if (!this.q.length) return this.play([t], 0); this.q.splice(this.i + 1, 0, t); toast('سيُشغَّل بعد المقطع الحالي'); },
  tick(c, d) { const r = d > 0 ? c / d : 0; $('#pfill').style.width = r * 100 + '%'; $('#mFill').style.width = r * 100 + '%'; $('#pcur').textContent = fmt(c); $('#ptot').textContent = fmt(d); },
  setPlaying(b) { this.playing = b; b ? Bg.start(this.cur()) : Bg.idle(); if ('mediaSession' in navigator) try { navigator.mediaSession.playbackState = b ? 'playing' : 'paused'; } catch {} const g = b ? '❚❚' : '▶'; $('#pPlay').textContent = g; $('#mPlay').textContent = g; $('#pdisc').classList.toggle('spin', b); },
  setUI(t) {
    $('#mini').hidden = false; $('#mTitle').textContent = t.title; $('#mArtist').textContent = t.artist || SRC[t.src];
    $('#mArt').className = 'art ' + grad(t.id); $('#mArt').innerHTML = t.art ? `<img src="${esc(t.art)}" alt="" referrerpolicy="no-referrer">` : '';
    $('#ptitle').textContent = t.title; $('#partist').textContent = t.artist; $('#psrc').textContent = SRC[t.src]; $('#psrc').className = 'src s-' + t.src;
    const im = $('#pimg'); im.hidden = !t.art; if (t.art) im.src = t.art;
    const l = $('#pLink'); l.href = t.page || '#'; l.hidden = !t.page;
    $('#pfill').style.width = '0'; $('#mFill').style.width = '0'; $('#pcur').textContent = '0:00'; $('#ptot').textContent = fmt(t.dur);
    favUI();
  },
  showStage(t) { const y = t.src === 'youtube'; $('#ytwrap').hidden = !y; $('#pdisc').hidden = y; if (y) openPlayer(); },
  media(t) {
    if (!('mediaSession' in navigator)) return;
    try {
      navigator.mediaSession.metadata = new MediaMetadata({ title: t.title, artist: t.artist || SRC[t.src], album: 'نغم', artwork: t.art ? [{ src: t.art, sizes: '300x300' }] : [] });
      navigator.mediaSession.setActionHandler('play', () => this.toggle()); navigator.mediaSession.setActionHandler('pause', () => this.toggle());
      navigator.mediaSession.setActionHandler('nexttrack', () => this.next()); navigator.mediaSession.setActionHandler('previoustrack', () => this.prev());
      navigator.mediaSession.setActionHandler('seekto', d => { if (d.seekTime != null && this.audio.duration) this.audio.currentTime = d.seekTime; });
    } catch {}
  }
};
P.audio.preload = 'auto';
P.audio.addEventListener('play', () => P.setPlaying(true));
P.audio.addEventListener('pause', () => { if (!P.audio.ended) P.setPlaying(false); });
P.audio.addEventListener('ended', () => P.next(true));
P.audio.addEventListener('timeupdate', () => { const t = P.cur(); if (t && t.src !== 'youtube') P.tick(P.audio.currentTime, P.audio.duration || t.dur); });
P.audio.addEventListener('error', () => { if (P.cur() && P.cur().src !== 'youtube' && P.audio.src) P.fail(); });

/* ---------------- views ---------------- */
const V = { name: 'home', arg: null, stack: [] };
const view = () => $('#view');
function go(name, arg, push) { if (push) V.stack.push({ name: V.name, arg: V.arg }); V.name = name; V.arg = arg; render(); view().scrollTop = 0; }
function back() { const p = V.stack.pop() || { name: 'home' }; V.name = p.name; V.arg = p.arg; render(); }
function render() {
  const main = ['home', 'discover', 'library'].includes(V.name) ? V.name : (V.stack[0] && V.stack[0].name) || 'home';
  document.querySelectorAll('#tabs button').forEach(b => b.classList.toggle('on', b.dataset.t === main));
  $('#tabs').hidden = V.name === 'onboard';
  (Views[V.name] || Views.home)();
}
function art(t, cls = '') { return `<div class="art ${grad(t.id)} ${cls}">${t.art ? `<img src="${esc(t.art)}" alt="" loading="lazy" referrerpolicy="no-referrer">` : ''}</div>`; }
function rows(list, ctx) {
  reg(list); CTX[ctx] = list; const cur = P.cur();
  return `<div class="rows">${list.map((t, i) => `<div class="row${cur && cur.id === t.id ? ' now' : ''}" role="button" tabindex="0" data-a="play" data-ctx="${ctx}" data-i="${i}">${art(t)}<div class="tx"><span>${esc(t.title)}</span><small>${esc(t.artist)}${t.dur ? ' · ' + fmt(t.dur) : ''}</small></div><span class="src s-${t.src}">${SRC[t.src]}</span><button class="more" data-a="menu" data-id="${esc(t.id)}" aria-label="المزيد">⋯</button></div>`).join('')}</div>`;
}
const CTX = {};
const skel = n => `<div class="rows">${'<div class="skel"></div>'.repeat(n)}</div>`;
const errBox = (msg, act) => `<div class="empty"><div>${msg}</div>${act || ''}</div>`;
function errMsg(e) {
  if (e && e.message === 'no-sources') return 'فعّل مصدرًا واحدًا على الأقل من الإعدادات.';
  if (e && e.message === 'no-jamendo-id') return 'أدخل مفتاح Jamendo في الإعدادات.';
  return 'تعذّر الاتصال بمصادر الموسيقى. تأكد من الإنترنت ثم أعد المحاولة.';
}
const retry = a => `<button class="lightbtn" data-a="${a}">إعادة المحاولة</button>`;
const greet = () => { const h = new Date().getHours(); return h < 5 ? 'ليلة هادئة' : h < 12 ? 'صباح الخير' : h < 18 ? 'نهارك سعيد' : 'مساء الخير'; };
const head = (title, sub) => `<div class="hdr"><button class="back" data-a="back" aria-label="رجوع">→</button><h2>${title}</h2></div>${sub ? `<div class="sub">${sub}</div>` : ''}`;
const moodTiles = (sel, a) => `<div class="tiles">${MOODS.map(m => `<button class="tile ${m.g}${sel && sel.includes(m.id) ? ' sel' : ''}" data-a="${a}" data-m="${m.id}" aria-pressed="${!!(sel && sel.includes(m.id))}">${m.ar}</button>`).join('')}</div>`;

const Views = {
  onboard() {
    view().innerHTML = `<span class="pill">أهلًا بك</span><h1 class="t">كيف تحب<br>أن تسمع؟</h1><p class="sub">اختر مزاجًا أو أكثر لنخصّص اختيارات اليوم. يمكنك التغيير لاحقًا.</p>
      ${moodTiles(S.moods, 'ob-mood')}
      <div class="sec">المصادر</div>${srcRows()}<div style="flex:1"></div>
      <button class="bigbtn" data-a="ob-done"><b>←</b>ابدأ الاستماع</button>`;
  },
  home() {
    view().innerHTML = `<div class="hdr"><div style="flex:1"><div class="sub">${greet()}</div><h1 class="t" style="font-size:26px">اختيارات اليوم</h1></div><button class="back" data-a="settings" aria-label="الإعدادات">⚙</button></div>
      <div id="dailyBox">${skel(4)}</div>
      <div class="sec">حسب مزاجك</div>${moodTiles(null, 'mood')}
      <div id="recentBox"></div>`;
    renderRecent();
    makeDaily().then(items => { if (V.name !== 'home') return; reg(items); CTX.daily = items;
      $('#dailyBox').innerHTML = `<div class="hero">${items.slice(0, 6).map((t, i) => `<button class="card" data-a="play" data-ctx="daily" data-i="${i}">${art(t)}<b>${esc(t.title)}</b><small>${esc(t.artist)}</small></button>`).join('')}</div>
        <button class="bigbtn" data-a="playall" data-ctx="daily"><b>▶</b>شغّل اختيارات اليوم (${items.length})</button>
        <div class="sec">القائمة كاملة <button data-a="refreshDaily">تحديث</button></div>${rows(items, 'daily')}`;
    }).catch(e => { if (V.name === 'home') $('#dailyBox').innerHTML = errBox(errMsg(e), retry('retryHome')); });
  },
  mood() {
    const m = MOODS.find(x => x.id === V.arg); view().innerHTML = head(m.ar, 'مقاطع من مصادرك المفعّلة') + `<div id="mBox">${skel(6)}</div>`;
    fetchMood(m, hash(today() + m.id)).then(ts => { if (V.name !== 'mood') return; const l = shuffle(ts, hash(today() + m.id)).slice(0, 30);
      $('#mBox').innerHTML = `<button class="bigbtn" data-a="playall" data-ctx="mood"><b>▶</b>شغّل الكل</button><div style="height:10px"></div>${rows(l, 'mood')}`;
    }).catch(e => { if (V.name === 'mood') $('#mBox').innerHTML = errBox(errMsg(e), retry('retryMood')); });
  },
  discover() {
    view().innerHTML = `<h1 class="t" style="font-size:26px">اعثر على<br>المفضّلة</h1>
      <form class="search" id="sf"><input id="q" type="search" placeholder="فنان أو أغنية، أو الصق رابط YouTube" aria-label="بحث" autocomplete="off" value="${esc(V.q || '')}"><button type="submit">بحث</button></form>
      <div class="chips">${GENRES.map(g => `<button class="chip" data-a="genre" data-q="${esc(g[0])}">${g[1]}</button>`).join('')}</div>
      <div id="rBox">${V.q ? '' : `<div class="empty">ابحث في Audius${S.jamendoId ? ' وJamendo' : ''}، أو الصق رابط YouTube لإضافته.</div>`}</div>`;
    $('#sf').addEventListener('submit', e => { e.preventDefault(); doSearch($('#q').value); });
    if (V.q && V.results) $('#rBox').innerHTML = rows(V.results, 'search');
  },
  library() {
    const t = V.libTab || 'lists';
    view().innerHTML = `<h1 class="t" style="font-size:26px">مكتبتي</h1>
      <div class="chips">${[['lists','قوائم'],['favs','المفضلة'],['recent','الأخيرة']].map(([k, n]) => `<button class="chip" data-a="libtab" data-t="${k}" aria-pressed="${t === k}">${n}</button>`).join('')}</div>
      ${t === 'lists' ? `<div class="mosaic">${S.lists.map(l => `<button class="tile ${grad(l.id)}" data-a="openlist" data-id="${l.id}" style="flex-direction:column;align-items:flex-start;justify-content:flex-end;height:96px"><span>${esc(l.name)}</span><small style="font-weight:400;opacity:.9">${l.items.length} مقطع</small></button>`).join('')}</div>
        <button class="lightbtn" data-a="newlist">＋ قائمة جديدة</button><button class="lightbtn" data-a="addyt">＋ إضافة رابط YouTube</button>`
      : t === 'favs' ? (S.favs.length ? `<button class="bigbtn" data-a="playall" data-ctx="favs"><b>▶</b>شغّل المفضلة</button>${rows(S.favs, 'favs')}` : errBox('لا مفضلة بعد. اضغط ♡ في المشغّل لإضافة مقطع.'))
      : (S.recent.length ? rows(S.recent.slice(0, 50), 'recent') : errBox('لم تستمع لشيء بعد.'))}`;
  },
  list() {
    const l = S.lists.find(x => x.id === V.arg); if (!l) return back();
    view().innerHTML = head(esc(l.name), `${l.items.length} مقطع`) + (l.items.length ? `<button class="bigbtn" data-a="playall" data-ctx="list"><b>▶</b>شغّل القائمة</button>${rows(l.items, 'list')}` : errBox('القائمة فارغة. أضف مقاطع من زر ⋯ بجانب أي أغنية.'))
      + `<button class="lightbtn" data-a="dellist" data-id="${l.id}">حذف القائمة</button>`;
  },
  settings() {
    view().innerHTML = head('الإعدادات') + `<div class="sec">المصادر</div>${srcRows()}
      <div class="sec">مفتاح Jamendo (اختياري)</div>
      <div class="setrow" style="flex-direction:column;align-items:stretch"><small>سجّل مجانًا في <a href="https://devportal.jamendo.com" target="_blank" rel="noopener">devportal.jamendo.com</a> وألصق الـ client_id هنا.</small>
      <input class="field" id="jid" dir="ltr" placeholder="client_id" value="${esc(S.jamendoId)}" autocomplete="off"></div>
      ${NATIVE ? `<div class="sec">العمل في الخلفية</div><div class="setrow" style="flex-direction:column;align-items:stretch"><small>يظهر إشعار ثابت أثناء التشغيل ليبقى الصوت يعمل عند قفل الشاشة. لأفضل نتيجة اسمح للتطبيق بتجاوز توفير البطارية.</small><button class="lightbtn" data-a="battery">السماح بتجاوز توفير البطارية</button></div>` : ''}
      <div class="sec">مزاجك المفضل</div>${moodTiles(S.moods, 'set-mood')}
      <div class="sec">البيانات</div><button class="lightbtn" data-a="reset">مسح المكتبة والإعدادات</button>
      <p class="sub">نغم للاستخدام الشخصي. Audius وJamendo تُشغَّلان داخل التطبيق، وYouTube داخل مشغّله المضمّن. كل شيء محفوظ على جهازك فقط.</p>`;
    $('#jid').addEventListener('change', e => { S.jamendoId = e.target.value.trim(); save(); toast('تم حفظ المفتاح'); });
  }
};
function srcRows() {
  const d = { audius: 'بث كامل · مستقلون', jamendo: 'بث كامل · يحتاج مفتاحًا مجانيًا', youtube: 'مشغّل مضمّن · أضف الروابط بنفسك' };
  return ['audius', 'jamendo', 'youtube'].map(k => `<div class="setrow"><span class="src s-${k}">${SRC[k]}</span><div class="tx">${SRC[k]}<small>${d[k]}</small></div><button class="tgl" role="switch" aria-checked="${!!S.src[k]}" aria-label="${SRC[k]}" data-a="src" data-k="${k}"></button></div>`).join('');
}
function renderRecent() { const b = $('#recentBox'); if (!b || !S.recent.length) return; reg(S.recent); b.innerHTML = `<div class="sec">استمع مجددًا <button data-a="tab" data-t="library">الكل</button></div>${rows(S.recent.slice(0, 4), 'recent')}`; }
function renderNow() { if (['home', 'library', 'mood', 'list', 'discover'].includes(V.name)) { const y = view().scrollTop; const c = P.cur(); document.querySelectorAll('.row').forEach(r => { const t = CTX[r.dataset.ctx] && CTX[r.dataset.ctx][+r.dataset.i]; r.classList.toggle('now', !!(t && c && t.id === c.id)); }); view().scrollTop = y; } }

async function doSearch(q) {
  q = (q || '').trim(); if (!q) return; V.q = q; const box = $('#rBox'); box.innerHTML = skel(5);
  const yid = YT.parse(q);
  if (yid && /youtu|^[\w-]{11}$/.test(q)) { const t = await YT.make(yid); reg([t]); V.results = [t]; box.innerHTML = rows([t], 'search') + `<button class="lightbtn" data-a="addlibyt" data-id="${esc(t.id)}">＋ حفظ في المفضلة</button>`; return; }
  const jobs = []; if (S.src.audius) jobs.push(Audius.search(q, 20)); if (S.src.jamendo && S.jamendoId) jobs.push(Jamendo.get({ search: q, limit: 15 }));
  if (!jobs.length) { box.innerHTML = errBox(errMsg(new Error('no-sources'))); return; }
  const res = await Promise.allSettled(jobs), ok = dedupe(res.filter(x => x.status === 'fulfilled').flatMap(x => x.value));
  V.results = ok;
  box.innerHTML = ok.length ? rows(ok, 'search') : res.every(x => x.status === 'rejected') ? errBox(errMsg(res[0].reason)) : errBox('لا نتائج. جرّب كلمات أخرى.');
}

/* ---------------- sheets ---------------- */
function sheet(html) { $('#sheet').innerHTML = html; $('#sheet').hidden = false; $('#sheetbg').hidden = false; }
function closeSheet() { $('#sheet').hidden = true; $('#sheetbg').hidden = true; }
const isFav = id => S.favs.some(t => t.id === id);
function toggleFav(t) { if (isFav(t.id)) { S.favs = S.favs.filter(x => x.id !== t.id); toast('أُزيل من المفضلة'); } else { S.favs.unshift(slim(t)); toast('أُضيف إلى المفضلة'); } save(); favUI(); }
function favUI() { const t = P.cur(), b = $('#pFav'); if (!t) return; const f = isFav(t.id); b.textContent = (f ? '♥' : '♡') + ' مفضلة'; b.classList.toggle('on', f); }
function trackMenu(t) {
  sheet(`<h3>${esc(t.title)}</h3><button class="opt" data-a="m-play" data-id="${esc(t.id)}">▶ تشغيل الآن</button><button class="opt" data-a="m-next" data-id="${esc(t.id)}">⏭ تشغيل بعد الحالي</button>
    <button class="opt" data-a="m-fav" data-id="${esc(t.id)}">${isFav(t.id) ? '♥ إزالة من المفضلة' : '♡ إضافة للمفضلة'}</button><button class="opt" data-a="m-list" data-id="${esc(t.id)}">＋ إضافة إلى قائمة</button>
    ${t.page ? `<a class="opt" href="${esc(t.page)}" target="_blank" rel="noopener" style="color:inherit;text-decoration:none">↗ فتح في ${SRC[t.src]}</a>` : ''}`);
}
function listPicker(t) {
  sheet(`<h3>إضافة إلى قائمة</h3>${S.lists.map(l => `<button class="opt" data-a="m-addto" data-id="${esc(t.id)}" data-l="${l.id}">${esc(l.name)}<small>${l.items.length}</small></button>`).join('')}
    <button class="opt" data-a="m-newto" data-id="${esc(t.id)}">＋ قائمة جديدة</button>`);
}
function inputSheet(title, ph, act, extra = '') { sheet(`<h3>${title}</h3><input class="field" id="sIn" placeholder="${ph}" autocomplete="off"><button class="bigbtn" data-a="${act}" ${extra}><b>←</b>حفظ</button>`); setTimeout(() => { const i = $('#sIn'); if (i) i.focus(); }, 50); }
function openPlayer() { $('#player').hidden = false; }

/* ---------------- events ---------------- */
function newList(name, t) { const l = { id: 'l' + Date.now().toString(36), name: name.trim() || 'قائمتي', items: t ? [slim(t)] : [] }; S.lists.push(l); save(); return l; }
document.addEventListener('keydown', e => { if ((e.key === 'Enter' || e.key === ' ') && e.target.matches('.row[data-a]')) { e.preventDefault(); e.target.click(); } if (e.key === 'Escape') closeSheet(); });
document.addEventListener('click', async e => {
  const el = e.target.closest('[data-a]'); if (!el) return; const a = el.dataset.a, d = el.dataset;
  const t = d.id ? trackById(d.id) : null;
  switch (a) {
    case 'tab': V.stack = []; V.q = V.q; go(d.t); break;
    case 'back': back(); break;
    case 'settings': go('settings', null, true); break;
    case 'mood': go('mood', d.m, true); break;
    case 'retryHome': Views.home(); break;
    case 'retryMood': Views.mood(); break;
    case 'refreshDaily': { $('#dailyBox').innerHTML = skel(4); try { await makeDaily(true); } catch (er) { toast(errMsg(er)); } if (V.name === 'home') Views.home(); break; }
    case 'play': { if (e.target.closest('.more')) break; P.play(CTX[d.ctx] || [], +d.i); break; }
    case 'playall': { const l = CTX[d.ctx]; if (l && l.length) P.play(l, 0); break; }
    case 'menu': e.stopPropagation(); if (t) trackMenu(t); break;
    case 'menucur': if (P.cur()) trackMenu(P.cur()); break;
    case 'm-play': closeSheet(); if (t) P.play([t], 0); break;
    case 'm-next': closeSheet(); if (t) P.playNext(t); break;
    case 'm-fav': closeSheet(); if (t) toggleFav(t); break;
    case 'm-list': if (t) listPicker(t); break;
    case 'm-addto': { const l = S.lists.find(x => x.id === d.l); if (l && t && !l.items.some(x => x.id === t.id)) l.items.push(slim(t)); save(); closeSheet(); toast('تمت الإضافة'); break; }
    case 'm-newto': inputSheet('اسم القائمة', 'مثلًا: دراسة', 'm-newgo', `data-id="${esc(d.id)}"`); break;
    case 'm-newgo': { const n = $('#sIn').value; newList(n, t); closeSheet(); toast('أُنشئت القائمة'); if (V.name === 'library') render(); break; }
    case 'favcur': if (P.cur()) toggleFav(P.cur()); break;
    case 'addcur': if (P.cur()) { $('#player').hidden = true; listPicker(P.cur()); } break;
    case 'openplayer': openPlayer(); break;
    case 'closeplayer': $('#player').hidden = true; break;
    case 'toggle': P.toggle(); break;
    case 'next': P.next(); break;
    case 'prev': P.prev(); break;
    case 'seek': { const r = el.getBoundingClientRect(); P.seek((e.clientX - r.left) / r.width); break; }
    case 'closesheet': closeSheet(); break;
    case 'genre': { V.q = d.q; $('#q').value = d.q; doSearch(d.q); break; }
    case 'libtab': V.libTab = d.t; render(); break;
    case 'openlist': go('list', d.id, true); break;
    case 'newlist': inputSheet('اسم القائمة', 'مثلًا: رياضة', 'newgo'); break;
    case 'newgo': { newList($('#sIn').value); closeSheet(); render(); break; }
    case 'dellist': S.lists = S.lists.filter(x => x.id !== d.id); save(); back(); toast('حُذفت القائمة'); break;
    case 'addyt': inputSheet('رابط YouTube', 'https://youtu.be/…', 'ytgo'); break;
    case 'ytgo': { const id = YT.parse($('#sIn').value); if (!id) { toast('الرابط غير صالح'); break; } const tr = await YT.make(id); S.favs.unshift(slim(tr)); save(); closeSheet(); toast('أُضيف إلى المفضلة'); V.libTab = 'favs'; render(); break; }
    case 'addlibyt': if (t) { if (!isFav(t.id)) S.favs.unshift(slim(t)); save(); toast('أُضيف إلى المفضلة'); } break;
    case 'ob-mood': case 'set-mood': { const i = S.moods.indexOf(d.m); i < 0 ? S.moods.push(d.m) : S.moods.splice(i, 1); S.daily = null; save(); el.classList.toggle('sel'); el.setAttribute('aria-pressed', i < 0); break; }
    case 'src': { S.src[d.k] = !S.src[d.k]; S.daily = null; save(); el.setAttribute('aria-checked', S.src[d.k]); break; }
    case 'ob-done': S.onboarded = true; save(); go('home'); break;
    case 'battery': { const B = Bg.plugin(); if (B) { try { const r = await B.checkBatteryOptimizations(); if (r && r.enabled === false) toast('مسموح بالفعل'); else await B.requestDisableBatteryOptimizations(); } catch { toast('تعذّر فتح الإعداد'); } } break; }
    case 'reset': S = JSON.parse(JSON.stringify(DEF)); save(); toast('تم المسح'); go('onboard'); break;
  }
});
$('#pbar').addEventListener('keydown', e => { if (e.key === 'ArrowLeft') P.seek(Math.max(0, (P.audio.currentTime + 5) / (P.audio.duration || 1))); });
const AppP = NP('App');
if (AppP) AppP.addListener('backButton', () => {
  if (!$('#sheet').hidden) return closeSheet();
  if (!$('#player').hidden) { $('#player').hidden = true; return; }
  if (V.stack.length) return back();
  if (['discover', 'library'].includes(V.name)) { go('home'); return; }
  AppP.minimizeApp();
});
if ('serviceWorker' in navigator && location.protocol.startsWith('http')) navigator.serviceWorker.register('sw.js').catch(() => {});
V.name = S.onboarded ? 'home' : 'onboard'; render();
window.__naghm = { P, S: () => S };
})();
