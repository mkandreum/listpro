import crypto from 'node:crypto';
import { getMeta } from './db.js';

// Cifra secretos de configuración (la contraseña SMTP) con AES-256-GCM.
// Formato: v1:iv:tag:data, todo en hex.
const VERSION = 'v1';

function key() {
  const raw = process.env.ENCRYPTION_KEY;
  if (raw) {
    const bytes = Buffer.from(raw, 'base64');
    return bytes.length === 32 ? bytes : crypto.createHash('sha256').update(raw).digest();
  }
  const jwt = process.env.JWT_SECRET || getMeta('jwt_secret');
  if (jwt) {
    console.warn('[secret] ENCRYPTION_KEY no configurada: se deriva de JWT_SECRET. Configúrala en producción.');
    return crypto.createHash('sha256').update(`listpro-enc:${jwt}`).digest();
  }
  throw new Error('Falta ENCRYPTION_KEY o JWT_SECRET para cifrar la contraseña SMTP');
}

export function encryptSecret(value) {
  if (value == null || value === '') return null;
  const iv = crypto.randomBytes(12);
  const cipher = crypto.createCipheriv('aes-256-gcm', key(), iv);
  const data = Buffer.concat([cipher.update(String(value), 'utf8'), cipher.final()]);
  return [VERSION, iv.toString('hex'), cipher.getAuthTag().toString('hex'), data.toString('hex')].join(':');
}

export function isEncrypted(value) {
  if (typeof value !== 'string' || !value.startsWith(`${VERSION}:`)) return false;
  const parts = value.split(':');
  return parts.length === 4 && /^[0-9a-f]+$/i.test(parts[1]) && /^[0-9a-f]+$/i.test(parts[2]) && /^[0-9a-f]+$/i.test(parts[3]);
}

export function decryptSecret(value) {
  if (!value) return null;
  if (!isEncrypted(value)) return String(value);
  const [, ivHex, tagHex, dataHex] = value.split(':');
  const decipher = crypto.createDecipheriv('aes-256-gcm', key(), Buffer.from(ivHex, 'hex'));
  decipher.setAuthTag(Buffer.from(tagHex, 'hex'));
  return Buffer.concat([decipher.update(Buffer.from(dataHex, 'hex')), decipher.final()]).toString('utf8');
}
