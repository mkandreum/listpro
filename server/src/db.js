import { DatabaseSync } from 'node:sqlite';
import fs from 'node:fs';
import path from 'node:path';

const file = process.env.DB_PATH ?? path.resolve('data/listpro.db');
fs.mkdirSync(path.dirname(file), { recursive: true });

export const db = new DatabaseSync(file);
db.exec(`
  PRAGMA journal_mode = WAL;
  PRAGMA foreign_keys = ON;

  CREATE TABLE IF NOT EXISTS categories (
    id INTEGER PRIMARY KEY,
    parent_id INTEGER,            -- NULL = categoría de nivel 0
    name TEXT NOT NULL,
    position INTEGER NOT NULL DEFAULT 0
  );

  CREATE TABLE IF NOT EXISTS products (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    size TEXT NOT NULL DEFAULT '',
    packaging TEXT,
    category_id INTEGER NOT NULL,   -- nivel 0
    subcategory_id INTEGER NOT NULL,-- nivel 1
    grp TEXT NOT NULL DEFAULT '',   -- nivel 2 (nombre del grupo)
    position INTEGER NOT NULL DEFAULT 0,
    price REAL NOT NULL,
    bulk_price REAL,
    ref_price REAL,
    ref_format TEXT,
    previous_price REAL,            -- precio tachado que muestra Mercadona
    image TEXT,
    share_url TEXT,
    is_new INTEGER NOT NULL DEFAULT 0,
    active INTEGER NOT NULL DEFAULT 1,
    first_seen TEXT NOT NULL,
    last_seen TEXT NOT NULL,
    price_changed_at TEXT,
    last_price REAL                 -- precio anterior en nuestro histórico
  );
  CREATE INDEX IF NOT EXISTS products_sub ON products(subcategory_id);

  CREATE TABLE IF NOT EXISTS price_history (
    product_id TEXT NOT NULL,
    price REAL NOT NULL,
    at TEXT NOT NULL,
    PRIMARY KEY (product_id, at)
  );

  CREATE TABLE IF NOT EXISTS meta (key TEXT PRIMARY KEY, value TEXT NOT NULL);

  -- Listas sincronizadas entre web y app con un código compartido
  CREATE TABLE IF NOT EXISTS lists (
    code TEXT PRIMARY KEY,
    data TEXT NOT NULL,
    updated_at TEXT NOT NULL
  );
`);

export function getMeta(key) {
  return db.prepare('SELECT value FROM meta WHERE key = ?').get(key)?.value ?? null;
}

export function setMeta(key, value) {
  db.prepare('INSERT INTO meta (key, value) VALUES (?, ?) ON CONFLICT(key) DO UPDATE SET value = excluded.value').run(key, String(value));
}

export function transaction(fn) {
  db.exec('BEGIN');
  try {
    const r = fn();
    db.exec('COMMIT');
    return r;
  } catch (e) {
    db.exec('ROLLBACK');
    throw e;
  }
}
