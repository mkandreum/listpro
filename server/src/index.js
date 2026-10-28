import express from 'express';
import compression from 'compression';
import crypto from 'node:crypto';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { db, getMeta } from './db.js';
import { getCatalog, getPriceHistory } from './catalog.js';
import { isScraping, scheduleScraping, scrapeCatalog } from './scraper.js';
import { createPlan, getPlan, plannerOptions, PlannerError } from './planner.js';
import {
  checkPassword, cleanName, CODE_RE, createUser, findByEmail, hashPassword, normalizeEmail, PASSWORD_MAX, PASSWORD_MIN,
  rateLimit, requireAuth, signSession, toUser, validEmail,
} from './auth.js';

const PORT = Number(process.env.PORT ?? 3000);
const ADMIN_TOKEN = process.env.ADMIN_TOKEN ?? '';
const publicDir = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../public');

const app = express();
app.disable('x-powered-by');
app.set('trust proxy', true);
app.use(express.json({ limit: '1mb' }));

// La web se sirve desde aquí mismo; la app Android llama a la API desde otro origen.
app.use('/api', (req, res, next) => {
  res.set('Access-Control-Allow-Origin', '*');
  res.set('Access-Control-Allow-Headers', 'Content-Type, If-None-Match, Authorization');
  res.set('Access-Control-Allow-Methods', 'GET, PUT, POST, DELETE, OPTIONS');
  if (req.method === 'OPTIONS') return res.sendStatus(204);
  next();
});

app.get('/api/health', (_req, res) => {
  res.json({ ok: true, catalogUpdatedAt: getMeta('catalog_updated_at'), scraping: isScraping() });
});

// Metadatos ligeros: los clientes los consultan para saber si tienen que bajar el catálogo.
app.get('/api/catalog/meta', (_req, res) => {
  res.set('Cache-Control', 'no-cache').json({ ...getCatalog().meta, scraping: isScraping() });
});

app.get('/api/catalog', (req, res) => {
  const c = getCatalog();
  res.set({ ETag: c.etag, 'Cache-Control': 'no-cache', Vary: 'Accept-Encoding', 'Content-Type': 'application/json; charset=utf-8' });
  if (req.headers['if-none-match'] === c.etag) return res.sendStatus(304);
  if (/\bgzip\b/.test(req.headers['accept-encoding'] ?? '')) {
    res.set('Content-Encoding', 'gzip').send(c.gzip);
  } else {
    res.send(c.json);
  }
});

app.get('/api/products/:id/history', (req, res) => {
  const h = getPriceHistory(req.params.id);
  if (!h) return res.status(404).json({ error: 'Producto no encontrado' });
  res.json(h);
});

// --- Listas compartidas: la misma cesta en el móvil, en la web y en el móvil de tu pareja ---
const ALPHABET = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
const newCode = () => Array.from(crypto.randomBytes(8), (b) => ALPHABET[b % ALPHABET.length]).join('');

function validList(data) {
  return data && typeof data === 'object' && data.basket && typeof data.basket === 'object' && Array.isArray(data.savedBaskets);
}

app.post('/api/lists', (req, res) => {
  if (!validList(req.body?.data)) return res.status(400).json({ error: 'Datos de lista inválidos' });
  const code = newCode();
  const now = new Date().toISOString();
  db.prepare('INSERT INTO lists (code, data, updated_at) VALUES (?, ?, ?)').run(code, JSON.stringify(req.body.data), now);
  res.status(201).json({ code, updatedAt: now });
});

app.get('/api/lists/:code', (req, res) => {
  const code = req.params.code.toUpperCase();
  if (!CODE_RE.test(code)) return res.status(400).json({ error: 'Código inválido' });
  const row = db.prepare('SELECT data, updated_at FROM lists WHERE code = ?').get(code);
  if (!row) return res.status(404).json({ error: 'Lista no encontrada' });
  res.set('Cache-Control', 'no-store').json({ code, data: JSON.parse(row.data), updatedAt: row.updated_at });
});

// Última escritura gana; si el cliente manda baseUpdatedAt y alguien escribió después, se avisa con 409.
app.put('/api/lists/:code', (req, res) => {
  const code = req.params.code.toUpperCase();
  if (!CODE_RE.test(code)) return res.status(400).json({ error: 'Código inválido' });
  if (!validList(req.body?.data)) return res.status(400).json({ error: 'Datos de lista inválidos' });
  const row = db.prepare('SELECT data, updated_at FROM lists WHERE code = ?').get(code);
  if (!row) return res.status(404).json({ error: 'Lista no encontrada' });
  const base = req.body.baseUpdatedAt;
  if (base && base !== row.updated_at && !req.body.force) {
    return res.status(409).json({ code, data: JSON.parse(row.data), updatedAt: row.updated_at });
  }
  const now = new Date().toISOString();
  db.prepare('UPDATE lists SET data = ?, updated_at = ? WHERE code = ?').run(JSON.stringify(req.body.data), now, code);
  res.json({ code, updatedAt: now });
});

// --- Cuentas ---
const ip = (req) => req.ip || 'x';
const authLimit = rateLimit(10, 15 * 60_000, (req) => `${ip(req)}:${normalizeEmail(req.body?.email)}`, 'Demasiados intentos. Espera unos minutos.');
const registerLimit = rateLimit(5, 60 * 60_000, ip, 'Demasiadas cuentas nuevas desde aquí. Prueba más tarde.');
const session = (res, u, status = 200) => res.status(status).json({ token: signSession(u), user: toUser(u) });
const listExists = (code) => CODE_RE.test(code ?? '') && !!db.prepare('SELECT 1 FROM lists WHERE code = ?').get(code);

app.post('/api/auth/register', registerLimit, async (req, res) => {
  const name = cleanName(req.body?.name);
  const email = normalizeEmail(req.body?.email);
  const password = typeof req.body?.password === 'string' ? req.body.password : '';
  if (!name) return res.status(400).json({ error: 'Dinos cómo te llamas.' });
  if (!validEmail(email)) return res.status(400).json({ error: 'Ese email no parece un email.' });
  if (password.length < PASSWORD_MIN || password.length > PASSWORD_MAX)
    return res.status(400).json({ error: `La contraseña necesita al menos ${PASSWORD_MIN} caracteres.` });
  if (findByEmail(email)) return res.status(409).json({ error: 'Ya hay una cuenta con ese email. Entra con ella.' });
  const listCode = typeof req.body?.listCode === 'string' ? req.body.listCode.toUpperCase() : null;
  try {
    const u = createUser({ id: req.body?.memberId, name, email, passwordHash: await hashPassword(password), listCode: listExists(listCode) ? listCode : null });
    session(res, u, 201);
  } catch (e) {
    if (String(e?.message).includes('UNIQUE')) return res.status(409).json({ error: 'Ya hay una cuenta con ese email. Entra con ella.' });
    console.error('[auth]', e);
    res.status(500).json({ error: 'No se ha podido crear la cuenta. Prueba otra vez.' });
  }
});

app.post('/api/auth/login', authLimit, async (req, res) => {
  const email = normalizeEmail(req.body?.email);
  const password = typeof req.body?.password === 'string' ? req.body.password.slice(0, PASSWORD_MAX) : '';
  const u = email ? findByEmail(email) : null;
  if (!(await checkPassword(u, password))) return res.status(401).json({ error: 'Email o contraseña incorrectos.' });
  session(res, u);
});

app.get('/api/me', requireAuth, (req, res) => res.json(toUser(req.user)));

/** Cambiar el nombre o la lista a la que apunta la cuenta (al crear o unirse a una lista compartida). */
app.put('/api/me', requireAuth, (req, res) => {
  const name = req.body?.name === undefined ? req.user.name : cleanName(req.body.name);
  if (!name) return res.status(400).json({ error: 'Dinos cómo te llamas.' });
  let code = req.user.list_code;
  if (req.body?.listCode !== undefined) {
    const next = req.body.listCode === null ? null : String(req.body.listCode).toUpperCase();
    if (next !== null && !listExists(next)) return res.status(400).json({ error: 'Esa lista no existe.' });
    code = next;
  }
  db.prepare('UPDATE users SET name = ?, list_code = ? WHERE id = ?').run(name, code, req.user.id);
  res.json(toUser({ ...req.user, name, list_code: code }));
});

app.put('/api/me/password', requireAuth, authLimit, async (req, res) => {
  if (!(await checkPassword(req.user, String(req.body?.current || ''))))
    return res.status(403).json({ error: 'La contraseña actual no es correcta.' });
  const next = typeof req.body?.next === 'string' ? req.body.next : '';
  if (next.length < PASSWORD_MIN || next.length > PASSWORD_MAX)
    return res.status(400).json({ error: `La nueva necesita al menos ${PASSWORD_MIN} caracteres.` });
  // Contraseña nueva: se cierran las demás sesiones y este dispositivo recibe una nueva.
  db.prepare('UPDATE users SET password_hash = ?, token_version = token_version + 1 WHERE id = ?').run(await hashPassword(next), req.user.id);
  session(res, { ...req.user, token_version: req.user.token_version + 1 });
});

app.post('/api/auth/logout-all', requireAuth, (req, res) => {
  db.prepare('UPDATE users SET token_version = token_version + 1 WHERE id = ?').run(req.user.id);
  session(res, { ...req.user, token_version: req.user.token_version + 1 });
});

app.delete('/api/me', requireAuth, authLimit, async (req, res) => {
  if (!(await checkPassword(req.user, String(req.body?.password || ''))))
    return res.status(403).json({ error: 'La contraseña no es correcta.' });
  db.prepare('DELETE FROM users WHERE id = ?').run(req.user.id);
  res.status(204).end();
});

// --- Planes de cenas con IA ---
app.get('/api/planner/options', (_req, res) => res.json(plannerOptions()));

// Cada plan cuesta una llamada a la IA: como mucho 8 por hora y dirección.
const planHits = new Map();
function planLimited(ip) {
  const now = Date.now();
  const recent = (planHits.get(ip) ?? []).filter((t) => now - t < 3600_000);
  if (recent.length >= 8) return true;
  recent.push(now);
  planHits.set(ip, recent);
  return false;
}

app.post('/api/plans', async (req, res) => {
  if (planLimited(req.ip)) return res.status(429).json({ error: 'Has creado muchos planes seguidos. Prueba dentro de un rato.' });
  try {
    res.status(201).json(await createPlan(req.body?.prefs ?? {}, getMeta('catalog_version') ?? '0'));
  } catch (e) {
    const status = e instanceof PlannerError ? e.status : 502;
    if (!(e instanceof PlannerError)) console.error('[planner]', e?.message ?? e);
    res.status(status).json({ error: e instanceof PlannerError ? e.message : 'La IA no ha respondido. Prueba otra vez en un momento.' });
  }
});

app.get('/api/plans/:id', (req, res) => {
  const plan = /^[\w-]{6,20}$/.test(req.params.id) ? getPlan(req.params.id) : null;
  if (!plan) return res.status(404).json({ error: 'Plan no encontrado' });
  res.json(plan);
});

app.post('/api/admin/refresh', (req, res) => {
  if (!ADMIN_TOKEN || req.headers.authorization !== `Bearer ${ADMIN_TOKEN}`) return res.sendStatus(401);
  if (isScraping()) return res.status(202).json({ status: 'ya en marcha' });
  scrapeCatalog().catch((e) => console.error('[scraper] Error:', e.message));
  res.status(202).json({ status: 'iniciado' });
});

app.use(compression());
app.use(express.static(publicDir, {
  setHeaders(res, file) {
    if (file.endsWith('.html') || file.endsWith('sw.js')) res.set('Cache-Control', 'no-cache');
  },
}));

app.listen(PORT, () => {
  console.log(`ListPro escuchando en http://localhost:${PORT}`);
  if (process.env.DISABLE_SCRAPER !== '1') scheduleScraping();
});
