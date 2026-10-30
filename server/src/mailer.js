import nodemailer from 'nodemailer';
import { getMeta } from './db.js';
import { decryptSecret, isEncrypted } from './secret.js';

export const SMTP_MASK = '••••••••';

export const smtpSettings = () => {
  const port = Number(getMeta('smtp_port') ?? 587);
  return {
    host: getMeta('smtp_host') ?? '',
    port: Number.isFinite(port) && port > 0 ? port : 587,
    user: getMeta('smtp_user') ?? '',
    pass: getMeta('smtp_pass') ?? '',
    secure: getMeta('smtp_secure') === 'true',
    from: getMeta('smtp_from') ?? '',
  };
};

export const smtpConfigured = () => {
  const s = smtpSettings();
  return Boolean(s.host && s.user && s.pass);
};

export function publicSmtp() {
  const s = smtpSettings();
  return {
    host: s.host,
    port: s.port,
    user: s.user,
    secure: s.secure,
    from: s.from,
    configured: smtpConfigured(),
    pass: s.pass ? SMTP_MASK : '',
  };
}

async function transporter() {
  const s = smtpSettings();
  if (!s.host || !s.user || !s.pass) {
    throw new Error('SMTP no configurado. Añade host, usuario y contraseña en el panel.');
  }
  const pass = isEncrypted(s.pass) ? decryptSecret(s.pass) : s.pass;
  const t = nodemailer.createTransport({
    host: s.host,
    port: s.port,
    secure: s.secure,
    auth: { user: s.user, pass },
    tls: { rejectUnauthorized: process.env.SMTP_TLS_INSECURE !== 'true' },
    connectionTimeout: 15_000,
    greetingTimeout: 15_000,
    socketTimeout: 20_000,
  });
  return { t, from: s.from || `"ListPro" <${s.user}>` };
}

const escapeHtml = (s) => String(s ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));

function layout({ title, intro, cta, href }) {
  return `<!doctype html>
<html lang="es"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
<body style="margin:0;background:#F3F6F4;font-family:Arial,Helvetica,sans-serif;color:#0C1714">
  <div style="max-width:560px;margin:0 auto;padding:28px 18px">
    <div style="background:#0C1714;border-radius:22px 22px 0 0;padding:24px 26px;color:#fff">
      <div style="display:inline-block;background:#22C55E;width:38px;height:38px;border-radius:12px;text-align:center;line-height:38px;font-weight:800;color:#062313">L</div>
      <span style="font-size:24px;font-weight:800;letter-spacing:-.03em;margin-left:10px;vertical-align:middle">List<span style="color:#4ADE80">Pro</span></span>
    </div>
    <div style="background:#fff;padding:28px 26px;border-radius:0 0 22px 22px">
      <h1 style="margin:0 0 12px;font-size:24px;line-height:1.2">${escapeHtml(title)}</h1>
      <p style="margin:0 0 20px;font-size:15px;line-height:1.55;color:#4E5C57">${escapeHtml(intro)}</p>
      <p style="margin:0 0 22px"><a href="${href}" style="display:inline-block;background:#22C55E;color:#062313;text-decoration:none;font-weight:700;padding:13px 22px;border-radius:14px">${escapeHtml(cta)}</a></p>
      <p style="margin:0;font-size:12px;line-height:1.5;color:#75827D">Si no has solicitado este cambio, puedes ignorar este correo. El enlace caduca en 1 hora.</p>
      <p style="margin:18px 0 0;font-size:12px;color:#75827D;word-break:break-all">${href}</p>
    </div>
  </div>
</body></html>`;
}

export async function sendPasswordResetEmail(to, link) {
  const { t, from } = await transporter();
  await t.sendMail({
    from,
    to,
    subject: 'Restablecer contraseña - ListPro',
    html: layout({
      title: 'Restablecer contraseña',
      intro: 'Has pedido cambiar la contraseña de tu cuenta de ListPro. Pulsa el botón para elegir una nueva.',
      cta: 'Elegir nueva contraseña',
      href: link,
    }),
  });
}

export async function sendTestEmail(to) {
  const { t, from } = await transporter();
  await t.sendMail({
    from,
    to,
    subject: 'Prueba de SMTP - ListPro',
    html: layout({
      title: 'El correo funciona',
      intro: 'Esta es una prueba enviada desde el panel de ListPro. Si la ves, el SMTP está bien configurado.',
      cta: 'Abrir ListPro',
      href: process.env.APP_URL || 'https://listpro.xyoncloud.win',
    }),
  });
}
