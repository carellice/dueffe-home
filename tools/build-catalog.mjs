#!/usr/bin/env node
// Genera catalog/catalog.json leggendo repository pubblici e gist di GitHub.
// Uso: node tools/build-catalog.mjs [--out catalog/catalog.json]
// Token: GITHUB_TOKEN / GH_TOKEN, altrimenti `gh auth token`, altrimenti anonimo.
import { execSync } from 'node:child_process';
import { mkdirSync, writeFileSync, readFileSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const ROOT = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const OWNER = 'carellice';
const SELF = 'dueffe-home';
const outArg = process.argv.indexOf('--out');
const OUT = resolve(ROOT, outArg > 0 ? process.argv[outArg + 1] : 'catalog/catalog.json');
const overrides = JSON.parse(readFileSync(resolve(ROOT, 'tools/catalog-overrides.json'), 'utf8'));

let token = process.env.GITHUB_TOKEN || process.env.GH_TOKEN || '';
if (!token) {
  try { token = execSync('gh auth token', { stdio: ['ignore', 'pipe', 'ignore'] }).toString().trim(); } catch { /* anonimo */ }
}

async function gh(path, { raw = false, allow404 = false } = {}) {
  const headers = { 'User-Agent': 'dueffe-home-catalog', Accept: raw ? 'application/vnd.github.raw' : 'application/vnd.github+json' };
  if (token) headers.Authorization = `Bearer ${token}`;
  let res = await fetch(`https://api.github.com/${path}`, { headers });
  // Il GITHUB_TOKEN delle Action non può leggere i gist: per quelli pubblici basta una richiesta anonima.
  if (res.status === 403 && token) {
    delete headers.Authorization;
    res = await fetch(`https://api.github.com/${path}`, { headers });
  }
  if (allow404 && (res.status === 404 || res.status === 409)) return null;
  if (!res.ok) throw new Error(`GitHub ${res.status} su ${path}`);
  return raw ? res.text() : res.json();
}

const rawUrl = (repo, branch, p) =>
  `https://raw.githubusercontent.com/${OWNER}/${repo}/${branch}/${p.replace(/^\.?\//, '').split('/').map(encodeURIComponent).join('/')}`;

function resolveAsset(repo, branch, src) {
  if (!src) return null;
  src = src.trim().replace(/&amp;/g, '&');
  if (src.startsWith('data:')) return null;
  const blob = src.match(/^https?:\/\/github\.com\/([^/]+)\/([^/]+)\/(?:blob|raw)\/([^/]+)\/(.+?)(\?.*)?$/);
  if (blob) return `https://raw.githubusercontent.com/${blob[1]}/${blob[2]}/${blob[3]}/${blob[4]}`;
  if (/^https?:\/\//.test(src)) return src;
  return rawUrl(repo, branch, decodeURIComponent(src.split('#')[0].split('?')[0]));
}

function readmeImages(md) {
  const out = [];
  const re = /<img\b[^>]*>|!\[([^\]]*)\]\(\s*<?([^)\s>]+)>?(?:\s+"[^"]*")?\s*\)/gi;
  let m;
  while ((m = re.exec(md))) {
    if (m[0].startsWith('<')) {
      const tag = m[0];
      const src = tag.match(/\bsrc\s*=\s*["']([^"']+)["']/i)?.[1];
      const alt = tag.match(/\balt\s*=\s*["']([^"']*)["']/i)?.[1] || '';
      const width = parseInt(tag.match(/\bwidth\s*=\s*["']?(\d+)/i)?.[1] || '0', 10);
      if (src) out.push({ src, alt, width });
    } else out.push({ src: m[2], alt: m[1] || '', width: 0 });
  }
  return out;
}

const isBadge = (s) => /shields\.io|badge|badgen\.net|github\.com\/.*\/workflows|buymeacoffee|ko-fi/i.test(s);
const isIconLike = (img) => /\b(icon|icona|logo|favicon|appicon|app-icon|pwa-\d+|logo\d+)\b|icon\.|logo\./i.test(img.alt + ' ' + img.src);
const IMG_EXT = /\.(png|jpe?g|webp|gif|svg)$/i;

function plain(md) {
  return md
    .replace(/<!--[\s\S]*?-->/g, '')
    .replace(/```[\s\S]*?```/g, '')
    .replace(/<[^>]+>/g, ' ')
    .replace(/!\[[^\]]*\]\([^)]*\)/g, '')
    .replace(/\[([^\]]*)\]\([^)]*\)/g, '$1')
    .replace(/[*_`>#|]/g, '')
    .replace(/&nbsp;/g, ' ').replace(/&amp;/g, '&').replace(/&[a-z]+;/g, '')
    .replace(/[ \t]+/g, ' ');
}

function readmeTitle(md) {
  const h = md.match(/<h1[^>]*>\s*([^<]+?)\s*<\/h1>/i)?.[1] || md.match(/^#\s+(.+)$/m)?.[1];
  return h ? plain(h).replace(/[^\p{L}\p{N}\s'’.\-:&+]/gu, '').trim() : null;
}

function readmeFeatures(md) {
  // Riunisce i punti elenco che proseguono sulla riga successiva.
  const lines = [];
  for (const line of md.split(/\r?\n/)) {
    const prev = lines.length - 1;
    if (prev >= 0 && /^\s{0,2}[-*]\s+/.test(lines[prev]) && line.trim() && !/^\s*([-*]|#|<|\||\d+\.)/.test(line)) lines[prev] += ' ' + line.trim();
    else lines.push(line);
  }
  const feats = [];
  for (const line of lines) {
    const m = line.match(/^\s{0,2}[-*]\s+(.+)$/);
    if (m) {
      const t = plain(m[1]).trim();
      if (t.length > 8 && t.length < 320 &&!/^https?:/.test(t)) feats.push(t);
      if (feats.length >= 6) break;
    } else if (feats.length && line.trim() === '') {
      if (feats.length >= 3) break;
    } else if (feats.length && !/^\s/.test(line)) {
      if (feats.length >= 3) break;
      feats.length = 0;
    }
  }
  return feats;
}

function readmeAbout(md, title, description = '') {
  const norm = (s) => s.toLowerCase().replace(/[^\p{L}\p{N}]+/gu, '');
  const desc = norm(description);
  const paragraphs = plain(md.replace(/^#.*$/gm, '\n')).split(/\n\s*\n/)
    .map((p) => p.replace(/\s+/g, ' ').trim())
    .filter((p) => p.length > 40 && !/^[-•]/.test(p) && p !== title && !(desc && (norm(p).includes(desc) || desc.includes(norm(p)))));
  return paragraphs.slice(0, 2).join('\n\n').slice(0, 700) || null;
}

function prettyName(name) {
  return name.replace(/^dueffe-/, 'Dueffe ').replace(/[-_]+/g, ' ')
    .replace(/\b\p{L}/gu, (c) => c.toUpperCase()).trim();
}

const PLATFORM_RULES = [
  ['android', /\bandroid\b|\.apk\b|jetpack compose/i],
  ['windows', /\bwindows\b|\bwinui\b|\.exe\b|\.msi\b|\bpc\b/i],
  ['macos', /\bmac(os)?\b|\.dmg\b|swiftui/i],
  ['web', /\bbrowser\b|\bweb\b|\bpwa\b|github\.io/i],
];

function detectPlatforms(text, repo, assets, override) {
  if (override?.platforms) return override.platforms;
  const set = new Set();
  for (const [p, re] of PLATFORM_RULES) if (re.test(text)) set.add(p);
  for (const a of assets) {
    if (/\.apk$/i.test(a.name)) set.add('android');
    if (/\.(exe|msi|msix|appx)$/i.test(a.name)) set.add('windows');
    if (/\.dmg$/i.test(a.name)) set.add('macos');
  }
  if (repo.has_pages || /github\.io/.test(repo.homepage || '')) set.add('web');
  if (!set.size && ['Kotlin', 'Java'].includes(repo.language)) set.add('android');
  return [...set];
}

function detectCategory(repo, platforms, override) {
  if (override?.category) return override.category;
  if (repo.language === 'GDScript') return 'giochi';
  if (/\b(gioco|game|giochi|party game|trivia)\b/i.test(repo.description || '')) return 'giochi';
  if (platforms.includes('android')) return 'android';
  if (platforms.includes('windows') || platforms.includes('macos')) return 'desktop';
  if (platforms.includes('web')) return 'web';
  return 'sviluppo';
}

const ICON_CANDIDATES = [
  /(^|\/)ic_launcher-playstore\.png$/i,
  /mipmap-xxxhdpi\/ic_launcher\.(png|webp)$/i,
  /(^|\/)(app-?icon|icon|icona)\.(png|webp|svg)$/i,
  /(^|\/)(logo|logo512|pwa-512x512|favicon-512|android-chrome-512x512)\.(png|svg|webp)$/i,
  /(^|\/)favicon\.(svg|png)$/i,
];

async function buildProject(repo) {
  const ov = overrides.projects?.[repo.name] || {};
  const branch = repo.default_branch;
  const [readme, release, tree] = await Promise.all([
    gh(`repos/${OWNER}/${repo.name}/readme`, { raw: true, allow404: true }),
    gh(`repos/${OWNER}/${repo.name}/releases/latest`, { allow404: true }),
    gh(`repos/${OWNER}/${repo.name}/git/trees/${branch}?recursive=1`, { allow404: true }),
  ]);
  const md = readme || '';
  const imgs = readmeImages(md).filter((i) => !isBadge(i.src));
  const paths = (tree?.tree || []).filter((t) => t.type === 'blob').map((t) => t.path)
    .filter((p) => !/(^|\/)(node_modules|build|dist|\.git)\//.test(p));

  let icon = ov.icon ? resolveAsset(repo.name, branch, ov.icon) : null;
  const iconImg = imgs.find((i) => isIconLike(i) && !/\.(mp4|mov)$/i.test(i.src));
  if (!icon && iconImg) icon = resolveAsset(repo.name, branch, iconImg.src);
  if (!icon) {
    for (const re of ICON_CANDIDATES) {
      const hit = paths.filter((p) => re.test(p)).sort((a, b) => a.length - b.length)[0];
      if (hit) { icon = rawUrl(repo.name, branch, hit); break; }
    }
  }

  let shots = imgs.filter((i) => i !== iconImg && IMG_EXT.test(i.src.split('?')[0]) && !isIconLike(i))
    .map((i) => resolveAsset(repo.name, branch, i.src)).filter(Boolean);
  if (shots.length < 2) {
    const fromTree = paths.filter((p) => /(screenshot|screens|schermat|preview|docs\/img|docs\/images|artifacts)\//i.test(p) && /\.(png|jpe?g|webp)$/i.test(p))
      .filter((p) => !/icon|logo|favicon/i.test(p)).slice(0, 8).map((p) => rawUrl(repo.name, branch, p));
    shots = [...shots, ...fromTree];
  }
  shots = [...new Set(shots)].filter((s) => s !== icon).slice(0, 10);
  const video = md.match(/(?:src|href)=["']([^"']+\.mp4)["']|\]\(([^)]+\.mp4)\)/i);

  // Package Android: serve all'app per riconoscere le app già installate e aprirle.
  let androidPackage = ov.androidPackage || null;
  if (!androidPackage && (release?.assets || []).some((a) => /\.apk$/i.test(a.name))) {
    const gradle = paths.filter((p) => /(^|\/)build\.gradle(\.kts)?$/.test(p)).sort((a, b) => b.split('/').length - a.split('/').length || a.length - b.length);
    for (const g of gradle) {
      const src = await gh(`repos/${OWNER}/${repo.name}/contents/${g}?ref=${branch}`, { raw: true, allow404: true });
      const id = src?.match(/applicationId\s*=?\s*["']([\w.]+)["']/)?.[1];
      if (id) { androidPackage = id; break; }
    }
  }

  const assets = (release?.assets || []).filter((a) => !/\.(json|txt|sha256|sig|asc)$/i.test(a.name)).map((a) => ({
    name: a.name, url: a.browser_download_url, size: a.size, downloads: a.download_count,
  }));
  const title = ov.name || readmeTitle(md) || prettyName(repo.name);
  const description = ov.description || repo.description || readmeAbout(md, title)?.split('\n')[0]?.slice(0, 180) || 'Progetto open source di Dueffe.';
  const text = [repo.description, plain(md).slice(0, 1500)].join(' ');
  const platforms = detectPlatforms(text, repo, assets, ov);
  const pagesUrl = repo.has_pages ? `https://${OWNER}.github.io/${repo.name}/` : null;
  const homepage = repo.homepage || null;

  const links = [{ type: 'github', label: 'Codice sorgente', url: repo.html_url }];
  const web = homepage && !/\/releases/.test(homepage) ? homepage : pagesUrl;
  if (web) links.unshift({ type: 'web', label: 'Apri online', url: web });
  if (release) links.push({ type: 'release', label: `Release ${release.tag_name}`, url: release.html_url });
  links.push({ type: 'releases', label: 'Tutte le release', url: `${repo.html_url}/releases` });
  if (repo.has_issues) links.push({ type: 'issues', label: 'Segnala un problema', url: `${repo.html_url}/issues` });
  for (const l of ov.links || []) links.push(l);

  return {
    id: repo.name,
    name: title,
    description,
    about: ov.about || readmeAbout(md, title, description),
    features: ov.features || readmeFeatures(md),
    category: detectCategory(repo, platforms, ov),
    platforms,
    language: repo.language,
    topics: repo.topics || [],
    stars: repo.stargazers_count,
    forks: repo.forks_count,
    license: repo.license?.spdx_id && repo.license.spdx_id !== 'NOASSERTION' ? repo.license.spdx_id : null,
    createdAt: repo.created_at,
    updatedAt: repo.pushed_at,
    repoUrl: repo.html_url,
    webUrl: web || null,
    links,
    icon,
    screenshots: shots,
    video: video ? resolveAsset(repo.name, branch, video[1] || video[2]) : null,
    release: release ? {
      tag: release.tag_name, name: release.name || release.tag_name, date: release.published_at,
      url: release.html_url, notes: (release.body || '').slice(0, 1500), assets,
    } : null,
    featured: Boolean(ov.featured),
    // Nascosto finché non si sbloccano i contenuti nascosti dalla pagina Info.
    hidden: Boolean(ov.hidden),
    androidPackage,
    dueffe: repo.name.startsWith('dueffe-'),
  };
}

async function main() {
  const [user, repos, gists] = await Promise.all([
    gh(`users/${OWNER}`),
    gh(`users/${OWNER}/repos?per_page=100&type=owner&sort=pushed`),
    gh(`users/${OWNER}/gists?per_page=100`),
  ]);
  const exclude = new Set([SELF, ...(overrides.exclude || [])]);
  const visible = repos.filter((r) => !r.private && !r.fork && !r.archived && !exclude.has(r.name));
  const projects = [];
  for (let i = 0; i < visible.length; i += 6) {
    projects.push(...await Promise.all(visible.slice(i, i + 6).map(buildProject)));
  }
  const order = overrides.categories.map((c) => c.id);
  projects.sort((a, b) => (order.indexOf(a.category) - order.indexOf(b.category)) ||
    (Number(b.featured) - Number(a.featured)) || (new Date(b.updatedAt) - new Date(a.updatedAt)));

  const gistList = await Promise.all(gists.filter((g) => g.public).map(async (g) => {
    const full = await gh(`gists/${g.id}`);
    return {
      id: g.id,
      description: g.description || Object.keys(g.files)[0],
      url: g.html_url,
      createdAt: g.created_at,
      updatedAt: g.updated_at,
      files: Object.values(full.files).map((f) => ({
        name: f.filename, language: f.language, size: f.size, rawUrl: f.raw_url,
        preview: (f.content || '').split(/\r?\n/).slice(0, 40).join('\n'),
      })),
    };
  }));

  const catalog = {
    schemaVersion: 1,
    generatedAt: new Date().toISOString(),
    owner: { login: user.login, name: overrides.ownerName || user.name || user.login, avatar: user.avatar_url, url: user.html_url },
    categories: overrides.categories.filter((c) => projects.some((p) => p.category === c.id)),
    projects,
    gists: gistList,
  };
  mkdirSync(dirname(OUT), { recursive: true });
  writeFileSync(OUT, JSON.stringify(catalog, null, 1) + '\n');
  console.log(`Catalogo: ${projects.length} progetti, ${gistList.length} gist -> ${OUT}`);
}

main().catch((e) => { console.error(e); process.exit(1); });
