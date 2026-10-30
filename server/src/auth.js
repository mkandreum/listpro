// Cuentas con el mismo patrón que Puchero y XyonWork: contraseña con bcrypt (coste 12), JWT HS256 con
// tokenVersion para revocar sesiones y errores que no delatan qué emails existen.
// La web y la app Android envían el token como "Authorization: Bearer".
import crypto from 'node:crypto';
import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';
import { db, getMeta, setMeta } from './db.js';

const SESSION_SECONDS = 90 * 24 * 60 * 60; // 90 días: es una lista de la compra, no un banco
export const PASSWORD_MIN = 8;
export const PASSWORD_MAX = 128;

// Sin JWT_SECRET no tumbamos el despliegue: se genera uno aleatorio y se guarda en la base de datos
// (que vive en el volumen), así las sesiones sobreviven a los reinicios. Mejor configurarlo en Coolify.
const secret = (() => {
  const s = process.env.JWT_SECRET;
  if (s && s.length >= 32) return s;
  let stored = getMeta('jwt_secret');
  if (!stored) {
    stored = crypto.randomBytes(48).toString('base64');
    setMeta('jwt_secret', stored);
  }
  return stored;
})();

// Hash ficticio para que el login tarde lo mismo exista o no el email.
const DUMMY_HASH = bcrypt.hashSync('listpro-dummy-password', 12);

export const normalizeEmail = (v) => (typeof v === 'string' ? v.trim().toLowerCase().slice(0, 254) : '');
export const validEmail = (e) => /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(e);
export const cleanName = (v) => (typeof v === 'string' ? v.replace(/\s+/g, ' ').trim().slice(0, 24) : '');
export const hashPassword = (p) => bcrypt.hash(p, 12);
const ID_RE = /^[a-z0-9]{8,24}$/;
export const CODE_RE = /^[A-Z2-9]{8}$/;

export async function checkPassword(user, password) {
  const ok = await bcrypt.compare(password, user?.password_hash || DUMMY_HASH);
  return Boolean(user?.password_hash) && ok;
}

export const signSession = (u) =>
  jwt.sign({ sub: u.id, tv: u.token_version }, secret, { expiresIn: SESSION_SECONDS, algorithm: 'HS256' });

const INVITE_ALPHABET = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';

/** Código de invitación de 8 caracteres, sin letras/números que se confunden (I, O, 0, 1). */
export const newInviteCode = () =>
  Array.from({ length: 8 }, () => INVITE_ALPHABET[crypto.randomInt(INVITE_ALPHABET.length)]).join('');

export const toUser = (u) => ({
  id: u.id,
  name: u.name,
  email: u.email,
  listCode: u.list_code ?? null,
  store: u.store ?? null,
  createdAt: u.created_at,
  plan: u.plan ?? 'free',
  planStartedAt: u.plan_started_at ?? null,
  inviteCode: u.invite_code ?? null,
});

export const findByEmail = (email) => db.prepare('SELECT * FROM users WHERE email = ?').get(email) ?? null;
export const findById = (id) => db.prepare('SELECT * FROM users WHERE id = ?').get(id) ?? null;
export const findInvite = (code) => db.prepare('SELECT * FROM users WHERE invite_code = ? AND invite_used_at IS NULL').get(String(code ?? '').toUpperCase()) ?? null;

/** El id puede venir del dispositivo (su id de miembro de la lista), para que su historial de cuentas siga siendo suyo. */
export function createUser({ id, name, email, passwordHash, listCode, store = null, plan = 'pro', invitedBy = null }) {
  const userId = ID_RE.test(id ?? '') && !findById(id) ? id : crypto.randomBytes(9).toString('base64url').toLowerCase().replace(/[^a-z0-9]/g, 'x');
  const now = new Date().toISOString();
  let inviteCode = newInviteCode();
  while (db.prepare('SELECT 1 FROM users WHERE invite_code = ?').get(inviteCode)) inviteCode = newInviteCode();
  db.prepare(`INSERT INTO users (id, email, name, password_hash, token_version, list_code, store, created_at, plan, plan_started_at, invite_code, invited_by)
    VALUES (?, ?, ?, ?, 0, ?, ?, ?, ?, ?, ?, ?)`)
    .run(userId, email, name, passwordHash, CODE_RE.test(listCode ?? '') ? listCode : null, store, now, plan === 'free' ? 'free' : 'pro', now, inviteCode, invitedBy);
  return findById(userId);
}

/** Marca la invitación como usada y devuelve el usuario que la emitió. */
export function consumeInvite(code, usedAt) {
  const inviter = findInvite(code);
  if (!inviter) return null;
  db.prepare('UPDATE users SET invite_used_at = ? WHERE id = ?').run(usedAt, inviter.id);
  return inviter;
}

/** La primera vez que arranca con invitaciones, cada usuario existente recibe la suya. */
export function ensureInviteCodes() {
  const rows = db.prepare('SELECT id FROM users WHERE invite_code IS NULL').all();
  if (!rows.length) return;
  const update = db.prepare('UPDATE users SET invite_code = ? WHERE id = ?');
  for (const row of rows) {
    let code = newInviteCode();
    while (db.prepare('SELECT 1 FROM users WHERE invite_code = ?').get(code)) code = newInviteCode();
    update.run(code, row.id);
  }
}

export function setResetToken(userId, hash, expiresAt, requestedAt) {
  db.prepare('UPDATE users SET reset_token_hash = ?, reset_token_expires_at = ?, reset_requested_at = ? WHERE id = ?')
    .run(hash, expiresAt, requestedAt, userId);
}

export function clearResetToken(userId) {
  db.prepare('UPDATE users SET reset_token_hash = NULL, reset_token_expires_at = NULL WHERE id = ?').run(userId);
}

ensureInviteCodes();

/** Verifica el JWT y recarga el usuario: una cuenta borrada o una sesión revocada dejan de valer al instante. */
export function requireAuth(req, res, next) {
  const [kind, token] = String(req.header('authorization') || '').split(' ');
  if (!/^Bearer$/i.test(kind) || !token) return res.status(401).json({ error: 'Inicia sesión para seguir.', code: 'NO_TOKEN' });
  let payload;
  try {
    payload = jwt.verify(token, secret, { algorithms: ['HS256'] });
  } catch {
    return res.status(401).json({ error: 'Tu sesión ha caducado. Vuelve a entrar.', code: 'INVALID_TOKEN' });
  }
  const user = findById(String(payload.sub));
  if (!user || (payload.tv ?? 0) !== user.token_version)
    return res.status(401).json({ error: 'Tu sesión ha caducado. Vuelve a entrar.', code: 'SESSION_REVOKED' });
  req.user = user;
  next();
}

/** Límite por clave en ventana deslizante (memoria del proceso; suficiente para una sola instancia). */
export function rateLimit(max, windowMs, keyOf, msg) {
  const hits = new Map();
  return (req, res, next) => {
    const key = keyOf(req);
    const now = Date.now();
    const recent = (hits.get(key) || []).filter((t) => now - t < windowMs);
    if (recent.length >= max) return res.status(429).json({ error: msg });
    recent.push(now);
    hits.set(key, recent);
    if (hits.size > 10_000) hits.clear();
    next();
  };
}
