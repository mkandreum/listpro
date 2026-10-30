import express from 'express';
import compression from 'compression';
import crypto from 'node:crypto';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { db, getMeta, setMeta } from './db.js';
import { getCatalog, getPriceHistory } from './catalog.js';
import { isScraping, scheduleScraping, scrapeAll, scrapeCatalog } from './scraper.js';
import { basketCost, cheaperElsewhere, compareProduct } from './compare.js';
import { isStore, metaKey, storeParam, STORES } from './stores.js';
import { createPlan, getPlan, plannerOptions, PlannerError, swapRecipe } from './planner.js';
import {
  checkPassword, cleanName, clearResetToken, CODE_RE, consumeInvite, createUser, findByEmail, findById, findInvite,
  hashPassword, newInviteCode, normalizeEmail, PASSWORD_MAX, PASSWORD_MIN, rateLimit, requireAuth, setResetToken,
  signSession, toUser, validEmail,
} from './auth.js';
import { encryptSecret } from './secret.js';
import { publicSmtp, sendPasswordResetEmail, sendTestEmail, SMTP_MASK, smtpConfigured } from './mailer.js';

const PORT = Number(process.env.PORT ?? 3000);
const ADMIN_TOKEN = process.env.ADMIN_TOKEN ?? '';
const APP_URL = (process.env.APP_URL ?? 'https://listpro.xyoncloud.win').replace(/\/$/, '');
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

function requireAdmin(req, res, next) {
  if (!ADMIN_TOKEN) return res.status(503).json({ error: 'ADMIN_TOKEN no configurado en el servidor.' });
  const [kind, token] = String(req.header('authorization') || '').split(' ');
  const ok = /^Bearer$/i.test(kind) && token
    && token.length === ADMIN_TOKEN.length
    && crypto.timingSafeEqual(Buffer.from(token), Buffer.from(ADMIN_TOKEN));
  if (!ok) return res.status(401).json({ error: 'No autorizado.' });
  next();
}

app.get('/api/health', (_req, res) => {
  res.json({ ok: true, catalogUpdatedAt: getMeta('catalog_updated_at'), scraping: isScraping(), stores: Object.fromEntries(STORES.map((s) => [s.id, getMeta(metaKey(s.id, 'catalog_updated_at'))])) });
});

// Súper disponibles, con cuántos productos tiene cada uno y cuándo se actualizó
app.get('/api/stores', (_req, res) => {
  res.set('Cache-Control', 'no-cache').json(STORES.map((s) => {
    const m = getCatalog(s.id).meta;
    return { id: s.id, name: s.name, hue: s.hue, count: m.count, updatedAt: m.updatedAt };
  }).filter((s) => s.count > 0));
});

// Metadatos ligeros: los clientes los consultan para saber si tienen que bajar el catálogo.
// Sin ?store es Mercadona, como siempre (apps antiguas).
app.get('/api/catalog/meta', (req, res) => {
  const store = storeParam(req.query.store);
  res.set('Cache-Control', 'no-cache').json({ ...getCatalog(store).meta, scraping: isScraping(store) });
});

app.get('/api/catalog', (req, res) => {
  const c = getCatalog(storeParam(req.query.store));
  res.set({ ETag: c.etag, 'Cache-Control': 'no-cache', Vary: 'Accept-Encoding', 'Content-Type': 'application/json; charset=utf-8' });
  if (req.headers['if-none-match'] === c.etag) return res.sendStatus(304);
  if (/\bgzip\b/.test(req.headers['accept-encoding'] ?? '')) {
    res.set('Content-Encoding', 'gzip').send(c.gzip);
  } else {
    res.send(c.json);
  }
});

// El mismo producto (o uno parecido) en los demás súper
app.get('/api/products/:id/compare', (req, res) => {
  const c = compareProduct(req.params.id);
  if (!c) return res.status(404).json({ error: 'Producto no encontrado' });
  res.json(c);
});

// Para la cesta: qué productos salen más baratos en otro súper
app.post('/api/compare', (req, res) => {
  const ids = Array.isArray(req.body?.ids) ? req.body.ids.filter((x) => typeof x === 'string').slice(0, 300) : [];
  res.json(cheaperElsewhere(ids));
});

// Coste aproximado de la cesta en cada súper, con la cobertura de productos emparejados.
app.post('/api/basket/compare', (req, res) => {
  const items = Array.isArray(req.body?.items)
    ? req.body.items
      .filter((x) => x && typeof x.id === 'string' && Number.isFinite(Number(x.qty)) && Number(x.qty) > 0)
      .slice(0, 300)
      .map((x) => ({ id: x.id, qty: Number(x.qty) }))
    : [];
  res.json(basketCost(items));
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
const forgotLimit = rateLimit(5, 15 * 60_000, (req) => `${ip(req)}:${normalizeEmail(req.body?.email)}`, 'Demasiadas solicitudes. Espera unos minutos.');
const sha256 = (s) => crypto.createHash('sha256').update(String(s)).digest('hex');
const RESET_TTL_MS = 60 * 60_000;
const RESET_COOLDOWN_MS = 10 * 60_000;

/** Genera un token de reset, lo guarda hasheado y envía el correo. Nunca devuelve el token. */
async function issueReset(user) {
  const now = Date.now();
  if (user.reset_token_expires_at) {
    const expires = Date.parse(user.reset_token_expires_at);
    if (Number.isFinite(expires) && expires - now > RESET_TTL_MS - RESET_COOLDOWN_MS) {
      return { sent: false, skipped: true };
    }
  }
  const token = crypto.randomBytes(32).toString('hex');
  setResetToken(user.id, sha256(token), new Date(now + RESET_TTL_MS).toISOString(), new Date(now).toISOString());
  try {
    await sendPasswordResetEmail(user.email, `${APP_URL}/#/reset?token=${token}`);
    return { sent: true };
  } catch (e) {
    console.error('[mail] reset:', e.message);
    clearResetToken(user.id);
    return { sent: false, error: e.message };
  }
}

app.post('/api/auth/register', registerLimit, async (req, res) => {
  const name = cleanName(req.body?.name);
  const email = normalizeEmail(req.body?.email);
  const password = typeof req.body?.password === 'string' ? req.body.password : '';
  if (!name) return res.status(400).json({ error: 'Dinos cómo te llamas.' });
  if (!validEmail(email)) return res.status(400).json({ error: 'Ese email no parece un email.' });
  if (password.length < PASSWORD_MIN || password.length > PASSWORD_MAX)
    return res.status(400).json({ error: `La contraseña necesita al menos ${PASSWORD_MIN} caracteres.` });
  if (findByEmail(email)) return res.status(409).json({ error: 'Ya hay una cuenta con ese email. Entra con ella.' });

  // Invitaciones: por defecto hace falta una; el panel puede abrir el registro.
  const invite = String(req.body?.invite ?? '').trim().toUpperCase();
  const mode = getMeta('registration_mode') ?? 'invite';
  const inviter = invite ? findInvite(invite) : null;
  // La primera cuenta de una instalación nueva puede crearse sin invitación.
  const bootstrap = db.prepare('SELECT COUNT(*) AS n FROM users').get().n === 0;
  if (mode === 'invite' && !inviter && !bootstrap)
    return res.status(403).json({ error: 'Necesitas una invitación válida para crear la cuenta.', code: 'INVITE_REQUIRED' });
  if (invite && !inviter)
    return res.status(400).json({ error: 'Ese código de invitación no es válido o ya se ha usado.', code: 'INVALID_INVITE' });

  const listCode = typeof req.body?.listCode === 'string' ? req.body.listCode.toUpperCase() : null;
  try {
    const plan = getMeta('new_user_plan') === 'free' ? 'free' : 'pro';
    const u = createUser({
      id: req.body?.memberId, name, email, passwordHash: await hashPassword(password),
      listCode: listExists(listCode) ? listCode : null, store: isStore(req.body?.store) ? req.body.store : null,
      plan, invitedBy: inviter?.id ?? null,
    });
    if (inviter) consumeInvite(invite, new Date().toISOString());
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
  db.prepare('UPDATE users SET last_login_at = ? WHERE id = ?').run(new Date().toISOString(), u.id);
  session(res, u);
});

// Siempre la misma respuesta: no revelamos si el email existe.
app.post('/api/auth/forgot', forgotLimit, async (req, res) => {
  const email = normalizeEmail(req.body?.email);
  const generic = { ok: true, message: 'Si el email existe, te hemos enviado un enlace para cambiar la contraseña.' };
  if (!validEmail(email)) return res.json(generic);
  const u = findByEmail(email);
  if (u) {
    const result = await issueReset(u);
    if (!result.sent && !result.skipped) console.error('[auth] no se pudo enviar el reset a', email);
  }
  res.json(generic);
});

app.post('/api/auth/reset', authLimit, async (req, res) => {
  const token = typeof req.body?.token === 'string' ? req.body.token : '';
  const password = typeof req.body?.password === 'string' ? req.body.password : '';
  if (token.length < 32 || token.length > 200) return res.status(400).json({ error: 'El enlace no es válido.' });
  if (password.length < PASSWORD_MIN || password.length > PASSWORD_MAX)
    return res.status(400).json({ error: `La contraseña necesita al menos ${PASSWORD_MIN} caracteres.` });
  const u = db.prepare('SELECT * FROM users WHERE reset_token_hash = ?').get(sha256(token));
  if (!u || !u.reset_token_expires_at || Date.parse(u.reset_token_expires_at) < Date.now())
    return res.status(400).json({ error: 'El enlace ha caducado. Pide otro.' });
  db.prepare('UPDATE users SET password_hash = ?, token_version = token_version + 1 WHERE id = ?')
    .run(await hashPassword(password), u.id);
  clearResetToken(u.id);
  res.json({ ok: true });
});

app.get('/api/me', requireAuth, (req, res) => res.json(toUser(req.user)));

/** Cambiar el nombre, el súper preferido o la lista a la que apunta la cuenta (al crear o unirse a una lista compartida). */
app.put('/api/me', requireAuth, (req, res) => {
  const name = req.body?.name === undefined ? req.user.name : cleanName(req.body.name);
  if (!name) return res.status(400).json({ error: 'Dinos cómo te llamas.' });
  let code = req.user.list_code;
  if (req.body?.listCode !== undefined) {
    const next = req.body.listCode === null ? null : String(req.body.listCode).toUpperCase();
    if (next !== null && !listExists(next)) return res.status(400).json({ error: 'Esa lista no existe.' });
    code = next;
  }
  if (req.body?.store !== undefined && req.body.store !== null && !isStore(req.body.store)) return res.status(400).json({ error: 'Ese súper no está disponible.' });
  const store = req.body?.store === undefined ? req.user.store : req.body.store;
  db.prepare('UPDATE users SET name = ?, list_code = ?, store = ? WHERE id = ?').run(name, code, store, req.user.id);
  res.json(toUser({ ...req.user, name, list_code: code, store }));
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

// --- Administración (panel ListProUserWeb) ---
const adminUser = (u) => ({
  id: u.id, name: u.name, email: u.email, plan: u.plan ?? 'free',
  planStartedAt: u.plan_started_at ?? null, createdAt: u.created_at,
  lastLoginAt: u.last_login_at ?? null, inviteCode: u.invite_code ?? null,
  inviteUsedAt: u.invite_used_at ?? null, invitedBy: u.invited_by ?? null,
  listCode: u.list_code ?? null, store: u.store ?? null,
});
const logAdmin = (action, userId, details = null) =>
  db.prepare('INSERT INTO admin_audit (action, user_id, details, at) VALUES (?, ?, ?, ?)')
    .run(action, userId ?? null, details ? JSON.stringify(details) : null, new Date().toISOString());

app.get('/api/admin/users', requireAdmin, (req, res) => {
  const q = String(req.query.q ?? '').trim();
  const plan = req.query.plan === 'pro' || req.query.plan === 'free' ? req.query.plan : null;
  const page = Math.max(1, Number(req.query.page) || 1);
  const limit = Math.min(100, Math.max(1, Number(req.query.limit) || 25));
  const where = [];
  const params = [];
  if (q) { where.push('(name LIKE ? OR email LIKE ?)'); params.push(`%${q}%`, `%${q}%`); }
  if (plan) { where.push('plan = ?'); params.push(plan); }
  const clause = where.length ? `WHERE ${where.join(' AND ')}` : '';
  const total = db.prepare(`SELECT COUNT(*) AS n FROM users ${clause}`).get(...params).n;
  const users = db.prepare(`SELECT * FROM users ${clause} ORDER BY created_at DESC LIMIT ? OFFSET ?`)
    .all(...params, limit, (page - 1) * limit)
    .map(adminUser);
  res.json({ users, total, page, pageSize: limit });
});

app.get('/api/admin/users/:id', requireAdmin, (req, res) => {
  const u = findById(req.params.id);
  if (!u) return res.status(404).json({ error: 'Usuario no encontrado.' });
  res.json(adminUser(u));
});

app.patch('/api/admin/users/:id', requireAdmin, (req, res) => {
  const u = findById(req.params.id);
  if (!u) return res.status(404).json({ error: 'Usuario no encontrado.' });
  const plan = req.body?.plan;
  if (plan !== 'pro' && plan !== 'free') return res.status(400).json({ error: 'Plan no válido.' });
  db.prepare('UPDATE users SET plan = ?, plan_started_at = COALESCE(plan_started_at, ?) WHERE id = ?')
    .run(plan, new Date().toISOString(), u.id);
  logAdmin('user.plan', u.id, { from: u.plan, to: plan });
  res.json(adminUser(findById(u.id)));
});

app.post('/api/admin/users/:id/invite', requireAdmin, (req, res) => {
  const u = findById(req.params.id);
  if (!u) return res.status(404).json({ error: 'Usuario no encontrado.' });
  let code = newInviteCode();
  while (db.prepare('SELECT 1 FROM users WHERE invite_code = ?').get(code)) code = newInviteCode();
  db.prepare('UPDATE users SET invite_code = ?, invite_used_at = NULL WHERE id = ?').run(code, u.id);
  logAdmin('user.invite', u.id, { code });
  res.json(adminUser(findById(u.id)));
});

app.post('/api/admin/users/:id/reset', requireAdmin, async (req, res) => {
  const u = findById(req.params.id);
  if (!u) return res.status(404).json({ error: 'Usuario no encontrado.' });
  const result = await issueReset(u);
  logAdmin('user.reset', u.id, { sent: result.sent, skipped: !!result.skipped });
  if (!result.sent) {
    return res.status(400).json({
      error: result.skipped ? 'Ya se envió un enlace hace menos de 10 minutos.' : (result.error || 'No se pudo enviar el correo.'),
      code: 'MAIL_FAILED',
    });
  }
  res.json({ ok: true });
});

app.get('/api/admin/settings', requireAdmin, (_req, res) => {
  res.json({
    newUserPlan: getMeta('new_user_plan') === 'free' ? 'free' : 'pro',
    registrationMode: getMeta('registration_mode') === 'open' ? 'open' : 'invite',
    smtp: publicSmtp(),
  });
});

app.put('/api/admin/settings', requireAdmin, (req, res) => {
  if (req.body?.newUserPlan !== undefined)
    setMeta('new_user_plan', req.body.newUserPlan === 'free' ? 'free' : 'pro');
  if (req.body?.registrationMode !== undefined)
    setMeta('registration_mode', req.body.registrationMode === 'open' ? 'open' : 'invite');
  logAdmin('settings.update', null, req.body);
  res.json({
    newUserPlan: getMeta('new_user_plan') === 'free' ? 'free' : 'pro',
    registrationMode: getMeta('registration_mode') === 'open' ? 'open' : 'invite',
    smtp: publicSmtp(),
  });
});

app.put('/api/admin/smtp', requireAdmin, (req, res) => {
  const b = req.body ?? {};
  if (b.host !== undefined) setMeta('smtp_host', String(b.host).trim());
  if (b.port !== undefined) {
    const port = Number(b.port);
    setMeta('smtp_port', String(Number.isFinite(port) && port > 0 ? port : 587));
  }
  if (b.user !== undefined) setMeta('smtp_user', String(b.user).trim());
  if (b.secure !== undefined) setMeta('smtp_secure', b.secure ? 'true' : 'false');
  if (b.from !== undefined) setMeta('smtp_from', String(b.from).trim());
  if (b.pass && b.pass !== SMTP_MASK) setMeta('smtp_pass', encryptSecret(String(b.pass)));
  logAdmin('smtp.update', null, { host: b.host, user: b.user });
  res.json(publicSmtp());
});

app.post('/api/admin/smtp/test', requireAdmin, async (req, res) => {
  const to = normalizeEmail(req.body?.to) || publicSmtp().user;
  if (!to) return res.status(400).json({ error: 'Indica un email de destino.' });
  try {
    await sendTestEmail(to);
    logAdmin('smtp.test', null, { to });
    res.json({ ok: true });
  } catch (e) {
    res.status(400).json({ error: e.message || 'No se pudo enviar el correo.' });
  }
});

app.get('/api/admin/stats', requireAdmin, (_req, res) => {
  const stats = db.prepare(`SELECT
    COUNT(*) AS total,
    SUM(CASE WHEN plan = 'pro' THEN 1 ELSE 0 END) AS pro,
    SUM(CASE WHEN plan = 'free' THEN 1 ELSE 0 END) AS free,
    SUM(CASE WHEN invite_used_at IS NOT NULL THEN 1 ELSE 0 END) AS invitesUsed,
    SUM(CASE WHEN invite_code IS NOT NULL AND invite_used_at IS NULL THEN 1 ELSE 0 END) AS invitesPending
    FROM users`).get();
  res.json({ ...stats, smtpConfigured: smtpConfigured() });
});

// --- Planes de recetas con IA ---
app.get('/api/planner/options', (_req, res) => res.json(plannerOptions()));

// Cada plan cuesta varias llamadas a la IA: como mucho 8 por hora y dirección.
const planHits = new Map();
function planLimited(ip, max = 8, key = 'plan') {
  const now = Date.now();
  const recent = (planHits.get(key + ip) ?? []).filter((t) => now - t < 3600_000);
  if (recent.length >= max) return true;
  recent.push(now);
  planHits.set(key + ip, recent);
  return false;
}

app.post('/api/plans', async (req, res) => {
  if (planLimited(req.ip)) return res.status(429).json({ error: 'Has creado muchos planes seguidos. Prueba dentro de un rato.' });
  try {
    res.status(201).json(await createPlan(req.body?.prefs ?? {}, req.body?.avoid));
  } catch (e) {
    planFailed(res, e);
  }
});

// Cambiar una receta suelta: más barato que un plan entero, hasta 30 por hora.
app.post('/api/plans/:id/swap', async (req, res) => {
  if (!/^[\w-]{6,20}$/.test(req.params.id)) return res.status(404).json({ error: 'Plan no encontrado' });
  if (planLimited(req.ip, 30, 'swap')) return res.status(429).json({ error: 'Has cambiado muchas recetas seguidas. Prueba dentro de un rato.' });
  try {
    res.status(201).json(await swapRecipe(req.params.id, Number(req.body?.index)));
  } catch (e) {
    planFailed(res, e);
  }
});

function planFailed(res, e) {
  const status = e instanceof PlannerError ? e.status : 502;
  if (!(e instanceof PlannerError)) console.error('[planner]', e?.message ?? e);
  res.status(status).json({ error: e instanceof PlannerError ? e.message : 'La IA no ha respondido. Prueba otra vez en un momento.' });
}

app.get('/api/plans/:id', (req, res) => {
  const plan = /^[\w-]{6,20}$/.test(req.params.id) ? getPlan(req.params.id) : null;
  if (!plan) return res.status(404).json({ error: 'Plan no encontrado' });
  res.json(plan);
});

app.post('/api/admin/refresh', (req, res) => {
  if (!ADMIN_TOKEN || req.headers.authorization !== `Bearer ${ADMIN_TOKEN}`) return res.sendStatus(401);
  // ?store=consum actualiza solo ese súper; sin él, todos
  const store = isStore(req.query.store) ? req.query.store : null;
  if (isScraping(store ?? undefined)) return res.status(202).json({ status: 'ya en marcha' });
  (store ? scrapeCatalog(store) : scrapeAll()).catch((e) => console.error('[scraper] Error:', e.message));
  res.status(202).json({ status: 'iniciado' });
});

app.use(compression());
app.use(express.static(publicDir, {
  setHeaders(res, file) {
    if (file.endsWith('.html') || file.endsWith('sw.js') || file.endsWith('manifest.webmanifest')) {
      // El HTML y el service worker no se cachean: un redeploy se ve al instante.
      res.set('Cache-Control', 'no-store, must-revalidate');
    } else if (file.includes(`${path.sep}assets${path.sep}`)) {
      // Vite pone un hash en el nombre: estos sí pueden cachearse para siempre.
      res.set('Cache-Control', 'public, max-age=31536000, immutable');
    } else {
      res.set('Cache-Control', 'public, max-age=3600');
    }
  },
}));

app.listen(PORT, () => {
  console.log(`ListPro escuchando en http://localhost:${PORT}`);
  if (process.env.DISABLE_SCRAPER !== '1') scheduleScraping();
});
