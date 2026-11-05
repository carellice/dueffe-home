// Dueffe Home — versione web. Stesse schermate, dati e animazioni dell'app Android.
const REPO = 'carellice/dueffe-home';
const CATALOG_URLS = ['catalog.json', `https://raw.githubusercontent.com/${REPO}/main/catalog/catalog.json`];

const P = {
  arrow: 'M12 4l-1.41 1.41L16.17 11H4v2h12.17l-5.58 5.59L12 20l8-8z',
  back: 'M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z',
  share: 'M18 16.08c-.76 0-1.44.3-1.96.77L8.91 12.7c.05-.23.09-.46.09-.7s-.04-.47-.09-.7l7.05-4.11c.54.5 1.25.81 2.04.81 1.66 0 3-1.34 3-3s-1.34-3-3-3-3 1.34-3 3c0 .24.04.47.09.7L8.04 9.81C7.5 9.31 6.79 9 6 9c-1.66 0-3 1.34-3 3s1.34 3 3 3c.79 0 1.5-.31 2.04-.81l7.12 4.16c-.05.21-.08.43-.08.65 0 1.61 1.31 2.92 2.92 2.92s2.92-1.31 2.92-2.92-1.31-2.92-2.92-2.92z',
  download: 'M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z',
  web: 'M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zm6.93 6h-2.95c-.32-1.25-.78-2.45-1.38-3.56 1.84.63 3.37 1.91 4.33 3.56zM12 4.04c.83 1.2 1.48 2.53 1.91 3.96h-3.82c.43-1.43 1.08-2.76 1.91-3.96zM4.26 14C4.1 13.36 4 12.69 4 12s.1-1.36.26-2h3.38c-.08.66-.14 1.32-.14 2s.06 1.34.14 2H4.26zm.82 2h2.95c.32 1.25.78 2.45 1.38 3.56-1.84-.63-3.37-1.9-4.33-3.56zm2.95-8H5.08c.96-1.66 2.49-2.93 4.33-3.56C8.81 5.55 8.35 6.75 8.03 8zM12 19.96c-.83-1.2-1.48-2.53-1.91-3.96h3.82c-.43 1.43-1.08 2.76-1.91 3.96zM14.34 14H9.66c-.09-.66-.16-1.32-.16-2s.07-1.35.16-2h4.68c.09.65.16 1.32.16 2s-.07 1.34-.16 2zm.25 5.56c.6-1.11 1.06-2.31 1.38-3.56h2.95c-.96 1.65-2.49 2.93-4.33 3.56zM16.36 14c.08-.66.14-1.32.14-2s-.06-1.34-.14-2h3.38c.16.64.26 1.31.26 2s-.1 1.36-.26 2h-3.38z',
  code: 'M9.4 16.6L4.8 12l4.6-4.6L8 6l-6 6 6 6 1.4-1.4zm5.2 0l4.6-4.6-4.6-4.6L16 6l6 6-6 6-1.4-1.4z',
  release: 'M23 12l-2.44-2.78.34-3.68-3.61-.82-1.89-3.18L12 3 8.6 1.54 6.71 4.72l-3.61.81.34 3.68L1 12l2.44 2.78-.34 3.69 3.61.82 1.89 3.18L12 21l3.4 1.46 1.89-3.18 3.61-.82-.34-3.68L23 12zm-10 5h-2v-2h2v2zm0-4h-2V7h2v6z',
  star: 'M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z',
  close: 'M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z',
  search: 'M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z',
  searchOff: 'M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3 6.08 3 3.28 5.64 3.03 9h2.02C5.3 6.75 7.18 5 9.5 5 11.99 5 14 7.01 14 9.5S11.99 14 9.5 14c-.17 0-.33-.03-.5-.05v2.02c.17.02.33.03.5.03 1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zM6.47 10.82 4 13.29l-2.47-2.47-.71.71L3.29 14 .82 16.47l.71.71L4 14.71l2.47 2.47.71-.71L4.71 14l2.47-2.47z',
  sort: 'M3 18h6v-2H3v2zM3 6v2h18V6H3zm0 7h12v-2H3v2z',
  box: 'M20 2H4c-1 0-2 .9-2 2v3.01c0 .72.43 1.34 1 1.69V20c0 1.1 1.1 2 2 2h14c.9 0 2-.9 2-2V8.7c.57-.35 1-.97 1-1.69V4c0-1.1-1-2-2-2zm-5 12H9v-2h6v2zm5-7H4V4l16-.02V7z',
  bug: 'M20 8h-2.81c-.45-.78-1.07-1.45-1.82-1.96L17 4.41 15.59 3l-2.17 2.17C12.96 5.06 12.49 5 12 5s-.96.06-1.41.17L8.41 3 7 4.41l1.62 1.63C7.88 6.55 7.26 7.22 6.81 8H4v2h2.09c-.05.33-.09.66-.09 1v1H4v2h2v1c0 .34.04.67.09 1H4v2h2.81c1.04 1.79 2.97 3 5.19 3s4.15-1.21 5.19-3H20v-2h-2.09c.05-.33.09-.66.09-1v-1h2v-2h-2v-1c0-.34-.04-.67-.09-1H20V8zm-6 8h-4v-2h4v2zm0-4h-4v-2h4v2z',
  open: 'M19 19H5V5h7V3H5c-1.11 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2v-7h-2v7zM14 3v2h3.59l-9.83 9.83 1.41 1.41L19 6.41V10h2V3h-7z',
  play: 'M8 5v14l11-7z',
  expand: 'M16.59 8.59L12 13.17 7.41 8.59 6 10l6 6 6-6z',
  copy: 'M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z',
  file: 'M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z',
  check: 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z',
  refresh: 'M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z',
  phoneDl: 'M17 1.01L7 1c-1.1 0-1.99.9-1.99 2v18c0 1.1.89 2 1.99 2h10c1.1 0 2-.9 2-2V3c0-1.1-.9-1.99-2-1.99zM17 19H7V5h10v14zm-1-6h-3V8h-2v5H8l4 4 4-4z',
  person: 'M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z',
  android: 'M17.6 9.48l1.84-3.18c.16-.31.04-.69-.26-.85-.29-.15-.65-.06-.83.22l-1.88 3.24c-2.86-1.21-6.08-1.21-8.94 0L5.65 5.67c-.19-.29-.58-.38-.87-.2-.28.18-.37.54-.22.83L6.4 9.48C3.3 11.25 1.28 14.44 1 18h22c-.28-3.56-2.3-6.75-5.4-8.52zM7 15.25c-.69 0-1.25-.56-1.25-1.25s.56-1.25 1.25-1.25 1.25.56 1.25 1.25-.56 1.25-1.25 1.25zm10 0c-.69 0-1.25-.56-1.25-1.25s.56-1.25 1.25-1.25 1.25.56 1.25 1.25-.56 1.25-1.25 1.25z',
  windows: 'M21 2H3c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h7v2H8v2h8v-2h-2v-2h7c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H3V4h18v12z',
  macos: 'M20 18c1.1 0 1.99-.9 1.99-2L22 5c0-1.1-.9-2-2-2H4c-1.1 0-2 .9-2 2v11c0 1.1.9 2 2 2H0c0 1.1.9 2 2 2h20c1.1 0 2-.9 2-2h-4zM4 5h16v11H4V5zm8 14c-.55 0-1-.45-1-1s.45-1 1-1 1 .45 1 1-.45 1-1 1z',
  phone: 'M16 1H8C6.34 1 5 2.34 5 4v16c0 1.66 1.34 3 3 3h8c1.66 0 3-1.34 3-3V4c0-1.66-1.34-3-3-3zm-2 20h-4v-1h4v1zm3.25-3H6.75V4h10.5v14z',
  game: 'M21.58 16.09l-1.09-7.66C20.21 6.46 18.52 5 16.53 5H7.47C5.48 5 3.79 6.46 3.51 8.43l-1.09 7.66C2.2 17.63 3.39 19 4.94 19c.68 0 1.32-.27 1.8-.75L9 16h6l2.25 2.25c.48.48 1.13.75 1.8.75 1.56 0 2.75-1.37 2.53-2.91zM11 11H9v2H8v-2H6v-1h2V8h1v2h2v1zm4-1c-.55 0-1-.45-1-1s.45-1 1-1 1 .45 1 1-.45 1-1 1zm2 3c-.55 0-1-.45-1-1s.45-1 1-1 1 .45 1 1-.45 1-1 1z',
  terminal: 'M20 4H4c-1.11 0-2 .9-2 2v12c0 1.1.89 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.89-2-2-2zm0 14H4V8h16v10zm-2-1h-6v-2h6v2zM7.5 17l-1.41-1.41L8.67 13l-2.59-2.59L7.5 9l4 4-4 4z',
  widgets: 'M13 13v8h8v-8h-8zM3 21h8v-8H3v8zM3 3v8h8V3H3zm13.66-1.31L11 7.34 16.66 13l5.66-5.66-5.66-5.65z',
  error: 'M11 15h2v2h-2zm0-8h2v6h-2zm.99-5C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8z',
  left: 'M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z',
  right: 'M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z',
  info: 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 15c-.55 0-1-.45-1-1v-4c0-.55.45-1 1-1s1 .45 1 1v4c0 .55-.45 1-1 1zm1-8h-2V7h2v2z',
  apps: 'M4 8h4V4H4v4zm6 12h4v-4h-4v4zm-6 0h4v-4H4v4zm0-6h4v-4H4v4zm6 0h4v-4h-4v4zm6-10v4h4V4h-4zm-6 4h4V4h-4v4zm6 6h4v-4h-4v4zm0 6h4v-4h-4v4z',
  lock: 'M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zM9 8V6c0-1.66 1.34-3 3-3s3 1.34 3 3v2H9z',
  unlock: 'M12 17c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm6-9h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6h2c0-1.66 1.34-3 3-3s3 1.34 3 3v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm0 12H6V10h12v10z',
};
const svg = (name, cls = '') => `<svg class="${cls}" viewBox="0 0 24 24" aria-hidden="true"><path d="${P[name]}"/></svg>`;
const esc = (s) => String(s ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const $ = (sel, root = document) => root.querySelector(sel);
const $$ = (sel, root = document) => [...root.querySelectorAll(sel)];
// localStorage può mancare (navigazione privata): l'app funziona lo stesso, senza ricordare.
function store(key, value) {
  try {
    if (value === undefined) return localStorage.getItem(key);
    if (value === null) localStorage.removeItem(key); else localStorage.setItem(key, value);
  } catch { return null; }
  return value;
}
const reduced = matchMedia('(prefers-reduced-motion: reduce)').matches;

const PLATFORM = { android: ['Android', 'android'], windows: ['Windows', 'windows'], macos: ['macOS', 'macos'], web: ['Web', 'web'] };
const CAT_ICON = { android: 'phone', giochi: 'game', desktop: 'windows', web: 'web', sviluppo: 'terminal' };
const CAT_LABEL = { android: 'App Android', giochi: 'Gioco', desktop: 'Windows e Mac', web: 'Web app', sviluppo: 'AI e sviluppo' };
const platformRow = (ps, size = 14) => `<span class="plat" style="--ps:${size}px">${ps.map((p) => `<span title="${PLATFORM[p]?.[0] ?? p}">${svg(PLATFORM[p]?.[1] ?? 'widgets')}</span>`).join('')}</span>`;

const state = {
  catalog: null,
  route: null,
  tab: 'home',
  category: null,
  query: '',
  platform: null,
  sort: 0,
  latest: null,
  version: null,
  splashDone: false,
  full: null,
  unlocked: store('unlocked') === '1',
  // App scaricate da questo browser: id del progetto -> tag della release scaricata.
  myApps: JSON.parse(store('myApps') || '{}'),
  // Posizione di scorrimento di ogni scheda: chi la visita per la prima volta parte dall'alto.
  scroll: {},
  expanded: new Set(),
};

// ---------- formati ----------
function relativeDate(iso) {
  const t = Date.parse(iso);
  if (!t) return '';
  const d = Math.floor((Date.now() - t) / 864e5);
  if (d < 1) return 'oggi';
  if (d < 2) return 'ieri';
  if (d < 7) return `${d} giorni fa`;
  if (d < 30) return `${Math.floor(d / 7)} sett. fa`;
  if (d < 365) return Math.floor(d / 30) === 1 ? '1 mese fa' : `${Math.floor(d / 30)} mesi fa`;
  return Math.floor(d / 365) === 1 ? '1 anno fa' : `${Math.floor(d / 365)} anni fa`;
}
const fullDate = (iso) => (Date.parse(iso) ? new Date(iso).toLocaleDateString('it-IT', { day: 'numeric', month: 'long', year: 'numeric' }) : '');
const size = (b) => (!b ? '' : b < 1048576 ? `${Math.round(b / 1024)} KB` : `${(b / 1048576).toFixed(1).replace('.', ',')} MB`);
function hashHue(s) { let h = 0; for (const c of s) h = (h * 31 + c.charCodeAt(0)) | 0; return Math.abs(h) % 360; }

// ---------- icone dei progetti e colore d'accento ----------
function icon(p, px, origin) {
  const hue = hashHue(p.id);
  const mono = `<span class="mono" style="--fs:${Math.round(px * 0.46)}px;background:linear-gradient(135deg,hsl(${hue} 55% 42%),hsl(${(hue + 40) % 360} 70% 14%))">${esc(p.name.trim()[0]?.toUpperCase())}</span>`;
  const img = p.icon ? `<img src="${esc(p.icon)}" alt="" loading="lazy" decoding="async" onload="this.classList.add('loaded')" onerror="this.remove()">` : '';
  return `<span class="icon" data-icon="${esc(origin)}/${esc(p.id)}" style="width:${px}px;height:${px}px">${mono}${img}</span>`;
}

const accentCache = new Map();
function accentOf(url) {
  if (!url) return Promise.resolve(null);
  if (accentCache.has(url)) return accentCache.get(url);
  const job = new Promise((resolve) => {
    const img = new Image();
    img.crossOrigin = 'anonymous';
    img.onload = () => {
      try {
        const c = document.createElement('canvas');
        c.width = c.height = 32;
        const ctx = c.getContext('2d', { willReadFrequently: true });
        ctx.drawImage(img, 0, 0, 32, 32);
        const d = ctx.getImageData(0, 0, 32, 32).data;
        let r = 0, g = 0, b = 0, w = 0;
        for (let i = 0; i < d.length; i += 4) {
          if (d[i + 3] < 160) continue;
          const [h, s, v] = rgbToHsv(d[i], d[i + 1], d[i + 2]);
          if (s < 0.25 || v < 0.3) continue;
          const k = s * v; r += d[i] * k; g += d[i + 1] * k; b += d[i + 2] * k; w += k;
        }
        if (w < 6) return resolve(null);
        let [h, s, v] = rgbToHsv(r / w, g / w, b / w);
        s = Math.min(0.8, Math.max(0.35, s)); v = Math.max(0.88, v);
        resolve(hsvToHex(h, s, v));
      } catch { resolve(null); }
    };
    img.onerror = () => resolve(null);
    img.src = url;
  });
  accentCache.set(url, job);
  return job;
}
function rgbToHsv(r, g, b) {
  r /= 255; g /= 255; b /= 255;
  const max = Math.max(r, g, b), min = Math.min(r, g, b), d = max - min;
  let h = 0;
  if (d) h = max === r ? ((g - b) / d) % 6 : max === g ? (b - r) / d + 2 : (r - g) / d + 4;
  return [(h * 60 + 360) % 360, max ? d / max : 0, max];
}
function hsvToHex(h, s, v) {
  const f = (n) => { const k = (n + h / 60) % 6; return v - v * s * Math.max(Math.min(k, 4 - k, 1), 0); };
  return '#' + [f(5), f(3), f(1)].map((x) => Math.round(x * 255).toString(16).padStart(2, '0')).join('');
}
const luminance = (hex) => { const n = parseInt(hex.slice(1), 16); return (0.2126 * (n >> 16) + 0.7152 * ((n >> 8) & 255) + 0.0722 * (n & 255)) / 255; };
function applyAccent(el, url) {
  accentOf(url).then((c) => {
    if (!c || !el) return;
    el.style.setProperty('--accent', c);
    el.style.setProperty('--on-accent', luminance(c) > 0.45 ? '#161006' : '#fffaf0');
  });
}

// ---------- dati ----------
async function loadCatalog() {
  const stamp = Math.floor(Date.now() / 60000);
  for (const u of CATALOG_URLS) {
    try {
      const r = await fetch(`${u}?t=${stamp}`, { cache: 'no-store' });
      if (r.ok) { const c = await r.json(); localStorage.setItem('catalog', JSON.stringify(c)); return c; }
    } catch { /* prova la fonte successiva */ }
  }
  try { return JSON.parse(localStorage.getItem('catalog')); } catch { return null; }
}
async function loadLatest() {
  try {
    const r = await fetch(`https://api.github.com/repos/${REPO}/releases/latest`);
    if (!r.ok) return;
    const j = await r.json();
    const apk = j.assets.find((a) => a.name.endsWith('.apk'));
    state.latest = { tag: j.tag_name, version: j.tag_name.replace(/^v/, ''), url: j.html_url, apk: apk?.browser_download_url, size: apk?.size, notes: j.body || '', date: j.published_at };
    if ((state.tab === 'info' || state.tab === 'home') && !state.route.startsWith('#/p/')) render();
  } catch { /* offline */ }
}
async function loadVersion() {
  try { state.version = (await (await fetch('version.json', { cache: 'no-store' })).json()).version; } catch { state.version = null; }
}

// I progetti nascosti restano fuori dal catalogo finché non si inserisce il PIN nella pagina Info.
const PIN_HASH = 'd7fdeffc0f8ba1b3f6b504bed173480e62a2915fb118d0e2a6f946131ac6fbcc';
function setCatalog(c) {
  state.full = c;
  if (!c) { state.catalog = null; return; }
  const projects = state.unlocked ? c.projects : c.projects.filter((p) => !p.hidden);
  state.catalog = { ...c, projects, categories: c.categories.filter((k) => projects.some((p) => p.category === k.id)) };
  updateAppsDot();
}
async function unlock(pin) {
  const bytes = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(pin.trim()));
  const hash = [...new Uint8Array(bytes)].map((b) => b.toString(16).padStart(2, '0')).join('');
  if (hash !== PIN_HASH) return false;
  state.unlocked = true;
  store('unlocked', '1');
  setCatalog(state.full);
  return true;
}
function lock() {
  state.unlocked = false;
  store('unlocked', null);
  setCatalog(state.full);
}

// ---------- le tue app ----------
const myAppList = () => state.catalog.projects.filter((p) => state.myApps[p.id]);
const hasUpdate = (p) => Boolean(p.release && state.myApps[p.id] && state.myApps[p.id] !== p.release.tag && state.myApps[p.id] !== 'web');
function remember(p, tag) {
  state.myApps[p.id] = tag;
  store('myApps', JSON.stringify(state.myApps));
  updateAppsDot();
}
function updateAppsDot() {
  $('#nav a[data-tab="apps"]')?.classList.toggle('dot', Boolean(state.catalog?.projects.some(hasUpdate)));
}

// ---------- routing ----------
const TAB_ORDER = ['home', 'explore', 'gist', 'apps'];
let lastTabIndex = 0;
let pendingShared = null;

function parseRoute() {
  const h = decodeURIComponent(location.hash.replace(/^#\/?/, ''));
  if (h.startsWith('p/')) return { name: 'detail', id: h.slice(2) };
  if (h.startsWith('esplora')) return { name: 'explore', cat: new URLSearchParams(h.split('?')[1] || '').get('cat') };
  if (h === 'gist') return { name: 'gist' };
  if (h === 'info') return { name: 'info' };
  if (h === 'app') return { name: 'apps' };
  return { name: 'home' };
}

function navigate(hash) { if (location.hash !== hash) location.hash = hash; else render(); }

function onRoute() {
  const prev = state.route;
  if (prev && !prev.startsWith('#/p/')) state.scroll[state.tab] = scrollY;
  const r = parseRoute();
  const key = location.hash || '#/';
  const goingDetail = r.name === 'detail';
  const leavingDetail = prev?.startsWith('#/p/') && !goingDetail;
  state.route = key;
  if (r.name === 'explore' && r.cat !== undefined) state.category = r.cat || null;
  const swap = () => render(r, prev, r.name === 'detail' ? 0 : state.scroll[r.name] || 0);
  if (document.startViewTransition && !reduced && (goingDetail || leavingDetail)) {
    // Icona condivisa tra lista e dettaglio con le View Transitions.
    const from = goingDetail ? pendingShared : $('.hero [data-icon]');
    if (from) from.style.viewTransitionName = 'shared-icon';
    document.startViewTransition(() => {
      swap();
      const to = goingDetail ? $('.hero [data-icon]') : (pendingShared && $(`[data-icon="${CSS.escape(pendingShared.dataset.icon)}"]`));
      if (to) to.style.viewTransitionName = 'shared-icon';
    }).finished.catch(() => {}).finally(() => { $$('[style*="view-transition-name"]').forEach((e) => (e.style.viewTransitionName = '')); });
  } else swap();
}

// ---------- render ----------
// `top` è lo scorrimento della pagina in arrivo; senza, si ridisegna la pagina corrente restando dove si è.
function render(r = parseRoute(), prev = null, top = scrollY) {
  const view = $('#view');
  const c = state.catalog;
  if (!c) { view.innerHTML = ''; return; }
  const nav = $('#nav');
  if (r.name === 'detail') {
    const p = c.projects.find((x) => x.id === r.id);
    if (!p) return navigate('#/');
    nav.classList.add('hidden');
    view.innerHTML = `<div class="page detail-in">${detail(p)}</div>`;
    wireDetail(p);
    scrollTo(0, top);
    return;
  }
  state.tab = r.name;
  let dirClass = '';
  // Info si apre dal pulsante in alto nella Home: la barra si nasconde come nel dettaglio.
  if (r.name === 'info') {
    nav.classList.add('hidden');
    if (prev !== '#/info') state.infoFrom = prev;
  } else {
    nav.classList.remove('hidden');
    const idx = TAB_ORDER.indexOf(r.name);
    dirClass = prev && !prev.startsWith('#/p/') && prev !== '#/info' && idx < lastTabIndex ? 'from-left' : '';
    moveIndicator(idx);
    lastTabIndex = idx;
  }
  const html = { home, explore, gist, apps, info }[r.name]();
  view.innerHTML = `<div class="page ${dirClass}">${html}</div>`;
  ({ home: wireHome, explore: wireExplore, gist: wireGist, apps: wireApps, info: wireInfo })[r.name]();
  scrollTo(0, top);
}

function moveIndicator(idx) {
  const ind = $('.nav-indicator');
  const prevIdx = Number(ind.dataset.idx ?? idx);
  ind.classList.toggle('right', idx > prevIdx);
  ind.classList.toggle('left', idx < prevIdx);
  ind.dataset.idx = idx;
  ind.style.setProperty('--l', `calc(6px + (100% - 12px) * ${idx / 4})`);
  ind.style.setProperty('--r', `calc(6px + (100% - 12px) * ${(3 - idx) / 4})`);
  $$('#nav a').forEach((a, i) => a.classList.toggle('on', i === idx));
}

const sectionHead = (title, sub, action, href) => `<div class="section-head"><div><h2 class="headline-s">${esc(title)}</h2>${sub ? `<p class="body-m muted">${esc(sub)}</p>` : ''}</div>${action ? `<a class="action press" href="${href}">${esc(action)}${svg('arrow')}</a>` : ''}</div>`;

// ---------- Home ----------
function home() {
  const c = state.catalog;
  const d = state.splashDone ? 0 : 2300;
  const words = [['Tutti', 'i', 'miei'], ['progetti,', 'in', 'un'], ['solo', 'posto.']];
  let i = 0;
  const headline = words.map((line) => `<div>${line.map((w, wi) => {
    const gold = w.startsWith('progetti');
    const txt = esc(w) + (wi < line.length - 1 ? ' ' : '');
    return `<span class="w ${gold ? 'g' : ''}" style="--i:${i++};--d:${d}ms">${gold ? `progetti<svg viewBox="0 0 100 12" preserveAspectRatio="none"><path style="--d:${d}ms" d="M0 8 C 20 2, 35 12, 55 6 S 85 3, 100 4"/></svg>, ` : txt}</span>`;
  }).join('')}</div>`).join('');
  const releases = c.projects.filter((p) => p.release).length;
  const featured = c.projects.filter((p) => p.featured);
  const recent = c.projects.filter((p) => p.release?.date).sort((a, b) => b.release.date.localeCompare(a.release.date)).slice(0, 5);
  return `
  <header class="home-head">
    <div class="brand"><img class="mark" src="assets/ff-mark.png" alt=""><span class="title-m">Dueffe Home</span>
      <a href="#/info" class="press avatar${state.latest?.apk ? ' dot' : ''}" aria-label="Info">${svg('info')}</a></div>
    <h1 class="display-m headline">${headline}</h1>
    <p class="body-l muted reveal" style="--i:4;margin-top:10px">App Android, giochi, strumenti per il PC e web app di ${esc(c.owner.name)}. Tutto aggiornato in tempo reale da GitHub.</p>
    <div class="stats">
      ${[[c.projects.length, 'progetti'], [releases, 'con release'], [c.gists.length, 'gist']].map(([n, l], k) => `<div class="card reveal" style="--i:${5 + k}"><b data-count="${n}">0</b><span class="label-m muted">${l}</span></div>`).join('')}
    </div>
  </header>
  ${state.latest?.apk ? `<a class="banner press" href="#/info">${svg('phoneDl', 'i')}<div><div class="title-m">Dueffe Home per Android</div><div class="body-s" style="opacity:.8">Scarica l'app nativa · ${esc(state.latest.tag)}</div></div>${svg('arrow', 'i')}</a>` : ''}
  ${featured.length ? `<section class="reveal" style="--i:1">${sectionHead('In evidenza', 'I progetti a cui tengo di più')}
    <div class="carousel" id="carousel">${featured.map((p) => feat(p)).join('')}</div>
    <div class="dots" id="dots">${featured.map((_, k) => `<i class="${k ? '' : 'on'}"></i>`).join('')}</div></section>` : ''}
  <section class="section reveal" style="--i:2">${sectionHead('Categorie', 'Ordinati per tipo, per trovare subito tutto', 'Tutti', '#/esplora?cat=')}
    <div class="hscroll" style="gap:12px">${c.categories.map((cat, k) => `<a class="card cat-card press reveal" style="--i:${k + 2};--t:${1.8 + k * 0.23}s" href="#/esplora?cat=${cat.id}">
      <div class="badge">${svg(CAT_ICON[cat.id] || 'widgets')}</div><div class="title-m ellipsis">${esc(cat.title)}</div>
      <div class="body-s muted">${c.projects.filter((p) => p.category === cat.id).length} progetti</div></a>`).join('')}</div></section>
  ${recent.length ? `<section class="section">${sectionHead('Ultime release', 'Le versioni appena uscite')}
    ${recent.map((p, k) => `<a class="row-item press reveal" style="--i:${k + 3}" href="#/p/${encodeURIComponent(p.id)}" data-origin="recent">${icon(p, 54, 'recent')}
      <div class="mid"><div class="title-m ellipsis">${esc(p.name)}</div><div class="body-s muted ellipsis">${esc(p.description)}</div></div>
      <div class="end"><div class="label-m gold">${svg('release')}${esc(p.release.tag)}</div><div class="label-s muted">${relativeDate(p.release.date)}</div></div></a>`).join('')}</section>` : ''}
  ${c.categories.map((cat) => `<section class="section">${sectionHead(cat.title, cat.subtitle, 'Vedi tutti', `#/esplora?cat=${cat.id}`)}
    <div class="hscroll">${c.projects.filter((p) => p.category === cat.id).map((p, k) => tile(p, `row-${cat.id}`, k)).join('')}</div></section>`).join('')}
  ${c.gists.length ? `<a class="card press" href="#/gist" style="display:flex;align-items:center;gap:14px;margin:32px 20px 0">
    <span style="width:52px;height:52px;border-radius:16px;display:grid;place-items:center;background:var(--surface-higher);border:1px solid var(--line-strong);color:var(--primary)">${svg('code', 'i')}</span>
    <span style="flex:1"><span class="title-m" style="display:block">Gist e script</span><span class="body-s muted">${c.gists.length} piccoli strumenti pronti da usare</span></span><span class="gold">${svg('arrow', 'i')}</span></a>` : ''}
  <footer class="footer"><img src="assets/ff-mark.png" alt=""><p class="body-s muted">Fatto con cura da ${esc(c.owner.name)}</p>
    <p class="label-s" style="color:#b8aa8d99">Catalogo aggiornato ${relativeDate(c.generatedAt)}</p></footer>`;
}

function feat(p) {
  const shot = p.screenshots[0];
  const bg = shot ? `<img class="bg" src="${esc(shot)}" alt="" loading="lazy">` : `${p.icon ? `<img class="bg blur" src="${esc(p.icon)}" alt="" loading="lazy">` : ''}<div class="glow"></div>`;
  return `<a class="feat" href="#/p/${encodeURIComponent(p.id)}" data-accent="${esc(p.icon || '')}">${bg}<div class="scrim"></div><div class="halo"></div>
    <span class="pill top" style="--c:var(--secondary)">${esc((CAT_LABEL[p.category] || p.category).toUpperCase())}</span>
    <div class="bottom"><div class="row">${icon(p, 62, 'featured')}<div><div class="headline-m ellipsis">${esc(p.name)}</div><span style="color:var(--secondary)">${platformRow(p.platforms)}</span></div></div>
      <p class="body-m clamp2" style="margin-top:12px;color:#fffaf0d1">${esc(p.description)}</p>
      <div class="row" style="margin-top:14px">${p.release ? `<span class="pill" style="--c:var(--accent,var(--primary))">${esc(p.release.tag)}</span>` : ''}<span style="flex:1"></span><span class="cta">Scopri${svg('arrow')}</span></div></div></a>`;
}

function tile(p, origin, k) {
  return `<a class="tile press reveal" style="--i:${k}" href="#/p/${encodeURIComponent(p.id)}" data-accent="${esc(p.icon || '')}">
    <div class="wrap">${icon(p, 104, origin)}</div><div class="title-s name ellipsis">${esc(p.name)}</div>
    <div class="meta">${platformRow(p.platforms, 12)}${p.release ? `<span class="label-s muted ellipsis">${esc(p.release.tag)}</span>` : ''}</div></a>`;
}

function wireHome() {
  // Conteggio animato dei numeri.
  $$('[data-count]').forEach((el) => {
    const target = Number(el.dataset.count);
    const start = performance.now() + (state.splashDone ? 300 : 2600);
    const step = (t) => {
      const k = Math.min(1, Math.max(0, (t - start) / 1400));
      el.textContent = Math.round(target * (1 - Math.pow(1 - k, 3)));
      if (k < 1) requestAnimationFrame(step);
    };
    requestAnimationFrame(step);
  });
  $$('[data-accent]').forEach((el) => applyAccent(el, el.dataset.accent));
  const car = $('#carousel');
  if (!car) return;
  const cards = $$('.feat', car);
  const dots = $$('#dots i');
  // Carosello 3D: rotazione, scala e parallasse in base alla distanza dal centro.
  const update = () => {
    const mid = car.scrollLeft + car.clientWidth / 2;
    let best = 0, bestD = Infinity;
    cards.forEach((card, k) => {
      const center = card.offsetLeft + card.offsetWidth / 2;
      const off = (center - mid) / (card.offsetWidth + 14);
      const a = Math.min(1, Math.abs(off));
      card.style.transform = `rotateY(${-off * 16}deg) scale(${1 - 0.14 * a})`;
      card.style.opacity = 1 - 0.45 * a;
      card.style.setProperty('--px', `${-off * 90}px`);
      card.style.setProperty('--active', String(1 - a));
      if (Math.abs(off) < bestD) { bestD = Math.abs(off); best = k; }
    });
    dots.forEach((d, k) => d.classList.toggle('on', k === best));
    return best;
  };
  car.addEventListener('scroll', () => requestAnimationFrame(update), { passive: true });
  update();
  let timer;
  const auto = () => {
    clearTimeout(timer);
    timer = setTimeout(() => {
      if (!document.body.contains(car)) return;
      const next = (update() + 1) % cards.length;
      car.scrollTo({ left: cards[next].offsetLeft - (car.clientWidth - cards[next].offsetWidth) / 2, behavior: 'smooth' });
      auto();
    }, 5200);
  };
  car.addEventListener('pointerdown', () => clearTimeout(timer));
  car.addEventListener('pointerup', auto);
  auto();
}

// ---------- Esplora ----------
const SORTS = ['Aggiornati', 'A-Z', 'Stelle'];
function results() {
  const q = state.query.trim().toLowerCase();
  let list = state.catalog.projects
    .filter((p) => !state.category || p.category === state.category)
    .filter((p) => !state.platform || p.platforms.includes(state.platform))
    .filter((p) => !q || [p.name, p.description, p.id, p.language || '', ...p.topics].some((s) => s.toLowerCase().includes(q)));
  if (state.sort === 0) list = list.sort((a, b) => (b.updatedAt || '').localeCompare(a.updatedAt || ''));
  if (state.sort === 1) list = list.sort((a, b) => a.name.localeCompare(b.name, 'it'));
  if (state.sort === 2) list = list.sort((a, b) => b.stars - a.stars);
  return list;
}
function explore() {
  const c = state.catalog;
  return `<div class="page-head"><h1 class="display-s">Esplora</h1><p class="body-m muted">Cerca tra ${c.projects.length} progetti</p></div>
  <label class="search ${state.query ? 'has' : ''}">${svg('search')}<input id="q" type="search" placeholder="App, giochi, linguaggi…" value="${esc(state.query)}" autocomplete="off"><button id="clear" aria-label="Cancella">${svg('close', 'clear')}</button></label>
  <div class="chips" style="margin-top:14px" id="cats"><button class="chip ${state.category ? '' : 'on'}" data-cat="">Tutti</button>${c.categories.map((cat) => `<button class="chip ${state.category === cat.id ? 'on' : ''}" data-cat="${cat.id}">${svg(CAT_ICON[cat.id] || 'widgets')}${esc(cat.title)}</button>`).join('')}</div>
  <div class="chips" style="margin-top:8px" id="plats">${Object.entries(PLATFORM).map(([k, [l, ic]]) => `<button class="chip ${state.platform === k ? 'on' : ''}" data-plat="${k}">${svg(ic)}${l}</button>`).join('')}</div>
  <div class="result-bar"><span class="label-l muted" id="count"></span><button class="sort press" id="sort">${svg('sort')}<span class="label-l">${SORTS[state.sort]}</span></button></div>
  <div id="results"></div>`;
}
function renderResults() {
  const list = results();
  $('#count').textContent = list.length === 1 ? '1 risultato' : `${list.length} risultati`;
  $('#results').innerHTML = list.length ? list.map((p, k) => `<a class="x-row press" style="--i:${k}" href="#/p/${encodeURIComponent(p.id)}">${icon(p, 64, 'explore')}
    <div class="mid"><div style="display:flex;align-items:center;gap:6px"><span class="title-m ellipsis">${esc(p.name)}</span>${p.stars ? `<span class="star">${svg('star')}${p.stars}</span>` : ''}</div>
    <div class="body-s muted clamp2">${esc(p.description)}</div>
    <div class="meta">${platformRow(p.platforms, 13)}${p.language ? `<span class="label-s muted">${esc(p.language)}</span>` : ''}${p.release ? `<span class="pill">${esc(p.release.tag)}</span>` : ''}</div></div></a>`).join('')
    : `<div class="empty"><div class="bubble">${svg('searchOff')}</div><div class="title-l">Nessun progetto trovato</div><p class="body-m muted">Prova con un'altra parola o togli un filtro</p></div>`;
}
function wireExplore() {
  renderResults();
  const q = $('#q');
  q.addEventListener('input', () => { state.query = q.value; q.parentNode.classList.toggle('has', !!q.value); renderResults(); });
  $('#clear').addEventListener('click', (e) => { e.preventDefault(); q.value = ''; q.dispatchEvent(new Event('input')); q.focus(); });
  $('#cats').addEventListener('click', (e) => {
    const b = e.target.closest('[data-cat]'); if (!b) return;
    state.category = b.dataset.cat && state.category !== b.dataset.cat ? b.dataset.cat : null;
    $$('#cats .chip').forEach((x) => x.classList.toggle('on', (x.dataset.cat || null) === state.category));
    history.replaceState(null, '', `#/esplora?cat=${state.category || ''}`);
    state.route = location.hash;
    renderResults();
  });
  $('#plats').addEventListener('click', (e) => {
    const b = e.target.closest('[data-plat]'); if (!b) return;
    state.platform = state.platform === b.dataset.plat ? null : b.dataset.plat;
    $$('#plats .chip').forEach((x) => x.classList.toggle('on', x.dataset.plat === state.platform));
    renderResults();
  });
  $('#sort').addEventListener('click', () => { state.sort = (state.sort + 1) % SORTS.length; $('#sort span').textContent = SORTS[state.sort]; renderResults(); });
}

// ---------- Gist ----------
const KW = /\b(echo|set|if|else|goto|call|exit|for|do|in|not|exist|pause|rem|setlocal|endlocal|start|choice|errorlevel|cls|title)\b/gi;
function highlight(line) {
  const t = line.trimStart();
  if (t.startsWith('::') || /^rem /i.test(t)) return `<span class="cm">${esc(line)}</span>`;
  if (t.startsWith(':')) return `<span class="lb">${esc(line)}</span>`;
  const tokens = [];
  for (const m of line.matchAll(/%[^%\s]+%|!\w+!|"[^"]*"/g)) tokens.push(m);
  for (const m of line.matchAll(KW)) tokens.push(m);
  tokens.sort((a, b) => a.index - b.index);
  let out = '', last = 0;
  for (const m of tokens) {
    if (m.index < last) continue;
    out += esc(line.slice(last, m.index));
    const cls = /^[%!]/.test(m[0]) ? 'var' : m[0].startsWith('"') ? 'str' : 'kw';
    out += `<span class="${cls}">${esc(m[0])}</span>`;
    last = m.index + m[0].length;
  }
  return out + esc(line.slice(last));
}
function gist() {
  return `<div class="page-head" style="padding-bottom:10px"><h1 class="display-s">Gist</h1><p class="body-m muted">Script e piccoli strumenti condivisi su GitHub Gist</p></div>
  ${state.catalog.gists.map((g, k) => {
    const f = g.files[0];
    const lines = f.preview.split('\n');
    const open = state.expanded.has(g.id);
    return `<article class="card gist reveal" style="--i:${k}" data-id="${g.id}">
      <div class="head"><span class="ic">${svg('file')}</span><div style="flex:1;min-width:0"><div class="fname">${esc(f.name)}</div>
        <p class="body-m muted" style="margin-top:4px">${esc(g.description)}</p>
        <div class="pills">${f.language ? `<span class="pill">${esc(f.language)}</span>` : ''}<span class="pill" style="--c:var(--muted)">${size(f.size)}</span><span class="pill" style="--c:var(--muted)">${relativeDate(g.updatedAt)}</span></div></div></div>
      <div class="code"><pre>${(open ? lines : lines.slice(0, 6)).map((l, n) => `<span class="ln">${String(n + 1).padStart(3)}  </span>${highlight(l)}`).join('\n')}</pre></div>
      <div class="actions"><button class="g-btn press" data-copy="${esc(f.rawUrl)}" data-name="${esc(f.name)}">${svg('copy')}Copia</button>
        <a class="g-btn press" href="${esc(g.url)}" target="_blank" rel="noopener">${svg('open')}GitHub</a><span class="sp"></span>
        ${lines.length > 6 ? `<button class="more ${open ? 'open' : ''}" data-toggle="${g.id}">${open ? 'Riduci' : 'Mostra tutto'}${svg('expand')}</button>` : ''}</div></article>`;
  }).join('')}`;
}
function wireGist() { /* il click è delegato una sola volta in start() */ }
async function onGistClick(e) {
  if (state.tab !== 'gist') return;
  const copy = e.target.closest('[data-copy]');
  const toggle = e.target.closest('[data-toggle]');
  if (copy) {
    try {
      const text = await (await fetch(copy.dataset.copy)).text();
      await navigator.clipboard.writeText(text);
      toast(`${copy.dataset.name} copiato negli appunti`);
    } catch { toast('Copia non riuscita: sei offline?'); }
  }
  if (toggle) {
    const id = toggle.dataset.toggle;
    state.expanded.has(id) ? state.expanded.delete(id) : state.expanded.add(id);
    const card = toggle.closest('.gist');
    const code = $('.code', card);
    const from = code.offsetHeight;
    const g = state.catalog.gists.find((x) => x.id === id);
    const lines = g.files[0].preview.split('\n');
    const open = state.expanded.has(id);
    $('pre', code).innerHTML = (open ? lines : lines.slice(0, 6)).map((l, n) => `<span class="ln">${String(n + 1).padStart(3)}  </span>${highlight(l)}`).join('\n');
    const to = code.scrollHeight;
    code.style.maxHeight = `${from}px`;
    requestAnimationFrame(() => { code.style.maxHeight = `${to}px`; });
    setTimeout(() => { code.style.maxHeight = ''; }, 520);
    toggle.classList.toggle('open', open);
    toggle.firstChild.textContent = open ? 'Riduci' : 'Mostra tutto';
  }
}

// ---------- Le tue app ----------
function apps() {
  const list = myAppList();
  const updates = list.filter(hasUpdate);
  const rest = list.filter((p) => !hasUpdate(p));
  const row = (p, k) => {
    const dl = p.release?.assets?.find((a) => a.name.toLowerCase().endsWith('.apk')) || p.release?.assets?.[0];
    const action = hasUpdate(p) && dl ? `<a class="btn press small" href="${esc(dl.url)}" data-update="${esc(p.id)}">Aggiorna</a>`
      : p.webUrl ? `<a class="btn ghost press small" href="${esc(p.webUrl)}" target="_blank" rel="noopener">Apri</a>`
        : '<span class="label-m gold">Aggiornata</span>';
    const sub = hasUpdate(p) ? `<span class="gold">${esc(state.myApps[p.id])} → ${esc(p.release.tag)}</span>`
      : p.release ? `Versione ${esc(p.release.tag)}` : 'Web app';
    return `<div class="row-item my-app reveal" style="--i:${k}"><a class="press" href="#/p/${encodeURIComponent(p.id)}">${icon(p, 54, 'apps')}</a>
      <a class="mid press" href="#/p/${encodeURIComponent(p.id)}"><div class="title-m ellipsis">${esc(p.name)}</div><div class="body-s muted ellipsis">${sub}</div></a>
      <div class="end">${action}</div></div>`;
  };
  return `<header class="page-head"><h1 class="display-s">Le tue app</h1><p class="body-m muted">Le app scaricate da questo browser</p>
    <button class="btn ghost press" id="check" style="width:100%;margin-top:16px">${svg('refresh')}Controlla aggiornamenti</button></header>
    ${list.length ? '' : `<div class="empty"><div class="bubble">${svg('apps')}</div><div class="title-l">Nessuna app scaricata</div>
      <p class="body-m muted" style="margin:6px 32px 18px">Scarica un'app dal catalogo: la ritroverai qui, con gli aggiornamenti.</p><a class="btn press" href="#/esplora">${svg('search')}Esplora le app</a></div>`}
    ${updates.length ? `<section class="section">${sectionHead('Aggiornamenti', updates.length === 1 ? '1 app da aggiornare' : `${updates.length} app da aggiornare`)}${updates.map(row).join('')}</section>` : ''}
    ${rest.length ? `<section class="section">${sectionHead('Scaricate', rest.length === 1 ? '1 app' : `${rest.length} app`)}${rest.map(row).join('')}</section>` : ''}`;
}
function wireApps() {
  $('#check').addEventListener('click', async () => {
    const c = await loadCatalog();
    if (!c) return toast("Sei offline: mostro l'ultimo catalogo salvato");
    setCatalog(c);
    const n = state.catalog.projects.filter(hasUpdate).length;
    toast(n ? (n === 1 ? '1 aggiornamento disponibile' : `${n} aggiornamenti disponibili`) : 'Tutte le app sono aggiornate');
    render();
  });
  $$('[data-update]').forEach((a) => a.addEventListener('click', () => {
    const p = state.catalog.projects.find((x) => x.id === a.dataset.update);
    remember(p, p.release.tag);
    setTimeout(() => render(), 300);
  }));
}

// ---------- Info ----------
function info() {
  const c = state.catalog;
  const l = state.latest;
  const status = (ic, color, title, sub) => `<div class="status"><span class="ic" style="--c:${color}">${svg(ic)}</span><div><div class="title-m">${esc(title)}</div><div class="body-s muted">${esc(sub)}</div></div></div>`;
  const update = l?.apk
    ? `${status('phoneDl', 'var(--primary)', `App Android ${l.version}`, `Versione nativa con aggiornamenti in-app · ${size(l.size)}`)}
       <a class="btn" style="width:100%;margin-top:14px" href="${esc(l.apk)}">${svg('download')}Scarica l'APK</a>`
    : `${status('check', 'var(--success)', 'Sei aggiornato', 'La versione web è sempre l\'ultima')}
       <a class="btn ghost" style="width:100%;margin-top:14px" href="https://github.com/${REPO}/releases">${svg('release')}Release su GitHub</a>`;
  const link = (ic, t, s, href, attrs = '') => `<a class="link-row press" href="${href}" ${attrs}>${svg(ic)}<div><div class="title-s">${esc(t)}</div><div class="body-s muted">${esc(s)}</div></div>${svg('arrow')}</a>`;
  const hidden = state.unlocked
    ? `${status('unlock', 'var(--success)', 'Contenuti nascosti visibili', 'Li trovi nel catalogo insieme agli altri')}
       <button class="btn ghost press" id="lock" style="width:100%;margin-top:14px">${svg('lock')}Nascondi di nuovo</button>`
    : `${status('lock', 'var(--primary)', 'Contenuti nascosti', 'Inserisci il PIN per mostrarli')}
       <form id="pin-form" class="pin-form"><input id="pin" type="password" inputmode="numeric" autocomplete="off" maxlength="12" placeholder="PIN" aria-label="PIN">
       <button class="btn press" type="submit">${svg('unlock')}Sblocca</button></form>`;
  return `<div class="info">
    <button class="round press info-back" id="info-back" aria-label="Indietro">${svg('back')}</button>
    <div class="orbit reveal" style="--i:0"><span class="glow"></span>
      <svg class="rings" viewBox="0 0 150 150"><g class="r1" fill="none" stroke-width="1.5" stroke-linecap="round"><path d="M75 10 A65 65 0 0 1 136 52" stroke="#ffd84d"/><path d="M75 140 A65 65 0 0 1 22 112" stroke="#ffd84d57"/></g>
      <g class="r2" fill="none" stroke-width="1.5" stroke-linecap="round"><path d="M75 24 A51 51 0 0 1 124 62" stroke="#fff0ad99"/></g></svg>
      <div class="box"><img src="assets/ff-mark.png" alt=""></div></div>
    <h1 class="display-s reveal" style="--i:1;margin-top:18px">Dueffe Home</h1>
    <p class="body-m muted reveal" style="--i:2">Versione web${state.version ? ` ${esc(state.version)}` : ''}</p>
    <div class="card reveal" style="--i:3;margin-top:26px">${update}</div>
    <div class="card reveal" style="--i:4;margin-top:16px"><div class="owner"><span class="ff-badge"><img src="assets/ff-mark.png" alt=""></span><div><div class="title-l">${esc(c.owner.name)}</div><div class="body-s muted">@${esc(c.owner.login)} su GitHub</div></div></div></div>
    <div class="stack reveal" style="--i:5;margin-top:16px">
      ${link('person', 'Profilo GitHub', 'Tutti i repository', esc(c.owner.url), 'target="_blank" rel="noopener"')}
      ${link('code', 'Codice di Dueffe Home', 'Repository pubblica', `https://github.com/${REPO}`, 'target="_blank" rel="noopener"')}
      ${link('android', 'App per Android', 'Ultima release su GitHub', `https://github.com/${REPO}/releases/latest`, 'target="_blank" rel="noopener"')}
      ${link('refresh', 'Aggiorna il catalogo', `Ultimo aggiornamento ${relativeDate(c.generatedAt)}`, '#/info', 'id="refresh"')}
    </div>
    <div class="card reveal" style="--i:6;margin-top:16px">${hidden}</div>
    <p class="body-s" style="color:#b8aa8dbf;margin-top:24px">Il catalogo si rigenera ogni giorno da GitHub: nuovi progetti, release e gist compaiono da soli.</p></div>`;
}
function wireInfo() {
  $('#info-back').addEventListener('click', () => (state.infoFrom ? history.back() : navigate('#/')));
  $('#lock')?.addEventListener('click', () => { lock(); toast('Contenuti nascosti'); render(); });
  $('#pin-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const input = $('#pin');
    if (await unlock(input.value)) { toast('Contenuti nascosti sbloccati'); render(); } else {
      input.value = '';
      input.classList.remove('wrong'); void input.offsetWidth; input.classList.add('wrong');
      toast('PIN non corretto');
    }
  });
  $('#refresh')?.addEventListener('click', async (e) => {
    e.preventDefault();
    const c = await loadCatalog();
    if (c) { setCatalog(c); toast('Catalogo aggiornato'); render(); } else toast("Sei offline: mostro l'ultimo catalogo salvato");
  });
}

// ---------- Dettaglio ----------
function cleanNotes(md) {
  return md.replace(/!\[[^\]]*\]\([^)]*\)/g, '').replace(/\[([^\]]*)\]\([^)]*\)/g, '$1').replace(/^#+\s*/gm, '').replace(/\*\*|__|`/g, '').replace(/\n{3,}/g, '\n\n').trim();
}
const LINK_ICON = { web: 'web', github: 'code', release: 'release', releases: 'release', issues: 'bug' };
function detail(p) {
  const c = state.catalog;
  const origin = pendingShared?.dataset.icon?.split('/')[0] || 'detail';
  const dl = p.release?.assets?.find((a) => a.name.toLowerCase().endsWith('.apk')) || p.release?.assets?.[0];
  const primary = dl ? `<a class="btn press" href="${esc(dl.url)}" data-get="${esc(p.id)}">${svg('download')}${dl.name.toLowerCase().endsWith('.apk') ? 'Scarica APK' : 'Scarica'}</a>`
    : p.webUrl ? `<a class="btn press" href="${esc(p.webUrl)}" target="_blank" rel="noopener">${svg('web')}Apri</a>`
      : `<a class="btn press" href="${esc(p.repoUrl)}" target="_blank" rel="noopener">${svg('code')}Vedi su GitHub</a>`;
  const secondary = dl && p.webUrl ? `<a class="btn ghost press" href="${esc(p.webUrl)}" target="_blank" rel="noopener">${svg('web')}Apri</a>`
    : dl || p.webUrl ? `<a class="btn ghost press" href="${esc(p.repoUrl)}" target="_blank" rel="noopener">${svg('code')}GitHub</a>` : '';
  const stats = [[p.release?.tag || '—', 'versione'], [p.stars, p.stars === 1 ? 'stella' : 'stelle'], [relativeDate(p.updatedAt) || '—', 'aggiornato']];
  if (p.license) stats.push([p.license, 'licenza']);
  const related = c.projects.filter((x) => x.category === p.category && x.id !== p.id).slice(0, 8);
  return `<div class="detail" id="detail">
    <div class="hero-bg">${p.screenshots[0] || p.icon ? `<img src="${esc(p.screenshots[0] || p.icon)}" alt="">` : ''}</div>
    <div class="topbar" id="topbar"><button class="round press" id="back" aria-label="Indietro">${svg('back')}</button><span class="t title-l ellipsis">${esc(p.name)}</span><button class="round press" id="share" aria-label="Condividi">${svg('share')}</button></div>
    <div class="hero">
      <div class="top">${icon(p, 116, origin)}<div class="reveal" style="--i:1"><span class="pill" style="--c:var(--accent,var(--primary))">${esc((CAT_LABEL[p.category] || p.category).toUpperCase())}</span>
        <h1 class="display-s" style="margin-top:8px">${esc(p.name)}</h1>${p.language ? `<p class="body-m muted">${esc(p.language)}</p>` : ''}</div></div>
      <p class="body-l reveal" style="--i:2;margin-top:18px;color:#fffaf0db">${esc(p.description)}</p>
      <div class="statsbar reveal" style="--i:3">${stats.map(([v, l]) => `<div><div class="title-m ellipsis">${esc(v)}</div><div class="label-s muted">${l}</div></div>`).join('')}</div>
      <div class="reveal" style="--i:4;margin-top:18px"><span style="color:var(--secondary)">${platformRow(p.platforms, 18)}</span><div class="actions-row">${primary}${secondary}</div></div>
    </div>
    ${p.screenshots.length || p.video ? `<section class="section reveal" style="--i:5">${sectionHead('Anteprima', p.screenshots.length === 1 ? '1 immagine' : `${p.screenshots.length} immagini`)}
      <div class="hscroll" style="gap:12px">${p.screenshots.map((s, k) => `<button class="shot press reveal" style="--i:${k}" data-shot="${k}"><img src="${esc(s)}" alt="Schermata ${k + 1}" loading="lazy" onload="this.classList.add('loaded');if(this.naturalWidth>this.naturalHeight)this.parentNode.classList.add('wide')"></button>`).join('')}
      ${p.video ? `<a class="video-tile press" href="${esc(p.video)}" target="_blank" rel="noopener"><div><div class="play">${svg('play')}</div><span class="label-l">Guarda il video</span></div></a>` : ''}</div></section>` : ''}
    ${p.about ? `<section class="section reveal" style="--i:6">${sectionHead('Informazioni')}<div class="pad"><p class="body-l about clamp4" id="about" style="color:#fffaf0d9">${esc(p.about)}</p>
      ${p.about.length > 220 ? `<button class="more" id="more" style="padding-left:0">Leggi tutto${svg('expand')}</button>` : ''}</div></section>` : ''}
    ${p.features.length ? `<section class="section">${sectionHead('Cosa sa fare')}<div class="features">${p.features.map((f, k) => `<div class="reveal" style="--i:${k}"><i style="--i:${k}"></i><span class="body-m" style="color:#fffaf0e0">${esc(f)}</span></div>`).join('')}</div></section>` : ''}
    ${p.release ? `<section class="section">${sectionHead('Novità', `${p.release.name} · ${fullDate(p.release.date)}`)}<div class="card" style="margin:0 20px">
      ${p.release.notes.trim() ? `<p class="body-m muted notes clamp" style="margin-bottom:14px;max-height:200px;overflow:hidden">${esc(cleanNotes(p.release.notes))}</p>` : ''}
      ${p.release.assets.map((a) => `<a class="asset press" href="${esc(a.url)}" data-get="${esc(p.id)}">${svg('box')}<div><div class="title-s ellipsis">${esc(a.name)}</div><div class="body-s muted">${[size(a.size), a.downloads ? `${a.downloads} download` : ''].filter(Boolean).join(' · ')}</div></div>${svg('download')}</a>`).join('')}</div></section>` : ''}
    <section class="section">${sectionHead('Link utili')}<div class="links">${p.links.map((l) => `<a class="link press" href="${esc(l.url)}" target="_blank" rel="noopener">${svg(LINK_ICON[l.type] || 'open')}<div><div class="title-s">${esc(l.label)}</div><div class="body-s muted ellipsis">${esc(l.url.replace('https://', ''))}</div></div>${svg('open')}</a>`).join('')}</div></section>
    ${p.topics.length ? `<div class="topics">${p.topics.map((t) => `<span class="pill" style="--c:var(--muted)">#${esc(t)}</span>`).join('')}</div>` : ''}
    ${related.length ? `<section class="section" style="margin-top:24px">${sectionHead('Potrebbe piacerti anche', CAT_LABEL[p.category])}<div class="hscroll">${related.map((r, k) => tile(r, 'related', k)).join('')}</div></section>` : ''}
    <div style="height:40px"></div></div>`;
}

function wireDetail(p) {
  const root = $('#detail');
  applyAccent(root, p.icon);
  $$('[data-accent]', root).forEach((el) => applyAccent(el, el.dataset.accent));
  $('#back').addEventListener('click', () => (history.length > 1 && state.cameFromApp ? history.back() : navigate('#/')));
  $('#share').addEventListener('click', async () => {
    const url = `${location.origin}${location.pathname}#/p/${encodeURIComponent(p.id)}`;
    if (navigator.share) navigator.share({ title: p.name, text: p.description, url }).catch(() => {});
    else { await navigator.clipboard.writeText(url); toast('Link copiato negli appunti'); }
  });
  const more = $('#more');
  more?.addEventListener('click', () => {
    const a = $('#about');
    const open = a.classList.toggle('clamp4') === false;
    more.classList.toggle('open', open);
    more.firstChild.textContent = open ? 'Mostra meno' : 'Leggi tutto';
  });
  // Ogni download finisce in "Le tue app", per accorgersi quando esce una versione nuova.
  $$('[data-get]', root).forEach((a) => a.addEventListener('click', () => remember(p, p.release?.tag || 'web')));
  $$('[data-shot]', root).forEach((b) => b.addEventListener('click', () => openViewer(p.screenshots, Number(b.dataset.shot))));
  const bar = $('#topbar');
  const onScroll = () => bar.classList.toggle('solid', scrollY > 420);
  addEventListener('scroll', onScroll, { passive: true });
  const bg = $('.hero-bg', root);
  addEventListener('scroll', () => { if (bg) bg.style.transform = `translateY(${Math.min(scrollY, 600) * 0.5}px)`; }, { passive: true });
}

function openViewer(urls, start) {
  const v = document.createElement('div');
  v.className = 'viewer';
  v.innerHTML = `<div class="track">${urls.map((u) => `<div class="slide"><img src="${esc(u)}" alt=""></div>`).join('')}</div>
    <div class="bar"><button class="round press" data-close aria-label="Chiudi">${svg('close')}</button><span class="sp"></span><span class="count"></span></div>
    <button class="round press nav-btn prev" aria-label="Precedente">${svg('left')}</button><button class="round press nav-btn next" aria-label="Successiva">${svg('right')}</button>
    <div class="dots">${urls.map(() => '<i></i>').join('')}</div>`;
  document.body.appendChild(v);
  document.body.style.overflow = 'hidden';
  const track = $('.track', v);
  const go = (i) => track.scrollTo({ left: i * track.clientWidth, behavior: 'smooth' });
  const sync = () => {
    const i = Math.round(track.scrollLeft / track.clientWidth);
    $('.count', v).textContent = `${i + 1} / ${urls.length}`;
    $$('.dots i', v).forEach((d, k) => d.classList.toggle('on', k === i));
    return i;
  };
  requestAnimationFrame(() => { track.scrollLeft = start * track.clientWidth; sync(); });
  track.addEventListener('scroll', () => requestAnimationFrame(sync), { passive: true });
  const close = () => { v.remove(); document.body.style.overflow = ''; removeEventListener('keydown', key); history.state?.viewer && history.back(); };
  const key = (e) => { if (e.key === 'Escape') close(); if (e.key === 'ArrowRight') go(sync() + 1); if (e.key === 'ArrowLeft') go(sync() - 1); };
  addEventListener('keydown', key);
  $('[data-close]', v).addEventListener('click', close);
  $('.prev', v).addEventListener('click', () => go(sync() - 1));
  $('.next', v).addEventListener('click', () => go(sync() + 1));
  $$('.slide img', v).forEach((img) => img.addEventListener('dblclick', () => img.classList.toggle('zoom')));
  // Trascina verso il basso per chiudere.
  let y0 = null;
  track.addEventListener('touchstart', (e) => { y0 = e.touches.length === 1 ? e.touches[0].clientY : null; }, { passive: true });
  track.addEventListener('touchmove', (e) => { if (y0 != null) { const dy = e.touches[0].clientY - y0; track.style.transform = `translateY(${dy}px)`; v.style.background = `rgba(0,0,0,${Math.max(0.3, 0.96 - Math.abs(dy) / 1400)})`; } }, { passive: true });
  track.addEventListener('touchend', (e) => {
    if (y0 == null) return;
    const dy = e.changedTouches[0].clientY - y0;
    if (Math.abs(dy) > 160) close(); else { track.style.transition = 'transform .4s cubic-bezier(.34,1.56,.64,1)'; track.style.transform = ''; v.style.background = ''; setTimeout(() => (track.style.transition = ''), 400); }
    y0 = null;
  });
}

// ---------- toast ----------
let toastTimer;
function toast(text) {
  const t = $('#toast');
  t.textContent = text;
  t.classList.add('on');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => t.classList.remove('on'), 2800);
}

// ---------- fondale animato ----------
function aurora() {
  const cv = $('#aurora');
  const ctx = cv.getContext('2d');
  const dpr = Math.min(devicePixelRatio || 1, 2);
  const seed = (i) => { const v = Math.sin(i * 12.9898) * 43758.547; return v - Math.floor(v); };
  const dust = Array.from({ length: 34 }, (_, i) => ({ x: seed(i * 3 + 1), y: seed(i * 7 + 2), s: 0.35 + seed(i * 11 + 3), r: 0.6 + seed(i * 5 + 4) * 1.8 }));
  const resize = () => { cv.width = innerWidth * dpr; cv.height = innerHeight * dpr; };
  addEventListener('resize', resize);
  resize();
  const blob = (x, y, r, color) => { const g = ctx.createRadialGradient(x, y, 0, x, y, r); g.addColorStop(0, color); g.addColorStop(1, 'rgba(0,0,0,0)'); ctx.fillStyle = g; ctx.fillRect(x - r, y - r, r * 2, r * 2); };
  const frame = (t) => {
    const a = ((t / 32000) % 1) * Math.PI * 2;
    const w = cv.width, h = cv.height;
    const s = (state.tab === 'home' && !state.route?.startsWith('#/p/') ? Math.min(scrollY, 900) : 900) * dpr;
    const bw = Math.min(w, 700 * dpr);
    ctx.fillStyle = '#050505';
    ctx.fillRect(0, 0, w, h);
    blob(w / 2 - bw / 2 + bw * (0.18 + 0.14 * Math.cos(a)), h * 0.06 + 50 * dpr * Math.sin(a * 2) - s * 0.2, bw * 0.95, 'rgba(255,184,0,0.22)');
    blob(w / 2 - bw / 2 + bw * (0.95 + 0.08 * Math.sin(a)), h * (0.38 + 0.05 * Math.cos(a)) - s * 0.1, bw * 0.75, 'rgba(255,216,77,0.14)');
    blob(w / 2 - bw / 2 + bw * (0.35 + 0.2 * Math.sin(a + 1.3)), h * (0.92 + 0.04 * Math.sin(a * 3)), bw * 1.05, 'rgba(138,90,0,0.12)');
    const phase = a / (Math.PI * 2);
    for (const d of dust) {
      const y = (((d.y - phase * d.s) % 1) + 1) % 1;
      const tw = 0.5 + 0.5 * Math.sin(a * 6 * d.s + d.x * 20);
      ctx.fillStyle = `rgba(255,240,173,${0.08 + 0.28 * tw * (1 - y * 0.6)})`;
      ctx.beginPath();
      ctx.arc(w * d.x + 14 * dpr * Math.sin(a * 2 + d.y * 9), h * y, d.r * dpr * 1.6, 0, Math.PI * 2);
      ctx.fill();
    }
    if (!reduced) requestAnimationFrame(frame);
  };
  requestAnimationFrame(frame);
}

// ---------- avvio ----------
function splash() {
  const s = $('#splash');
  const done = () => { s.classList.add('gone'); state.splashDone = true; };
  if (reduced || sessionStorage.getItem('splash')) { done(); return; }
  sessionStorage.setItem('splash', '1');
  setTimeout(() => s.classList.add('out'), 2150);
  setTimeout(done, 2150 + 860);
}

document.addEventListener('click', (e) => {
  const a = e.target.closest('a[href^="#/p/"]');
  if (a) { pendingShared = $('[data-icon]', a); state.cameFromApp = true; }
});
addEventListener('hashchange', onRoute);

(async function start() {
  if ('scrollRestoration' in history) history.scrollRestoration = 'manual';
  $('#view').addEventListener('click', onGistClick);
  aurora();
  splash();
  const [catalog] = await Promise.all([loadCatalog(), loadVersion()]);
  setCatalog(catalog);
  if (!catalog) { $('#view').innerHTML = '<div class="empty"><div class="title-l">Catalogo non disponibile</div><p class="body-m muted">Controlla la connessione e ricarica.</p></div>'; return; }
  onRoute();
  loadLatest();
})();
