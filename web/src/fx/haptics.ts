// Vibración: Android vía navigator.vibrate; iPhone (iOS 18+) vía el truco del
// <input type="checkbox" switch>, que dispara un "tic" háptico del sistema al conmutarse.
let iosLabel: HTMLLabelElement | null = null;
const canVibrate = typeof navigator !== 'undefined' && typeof navigator.vibrate === 'function';
// iPadOS se presenta como un Mac: se reconoce por la pantalla táctil
const isIOS = typeof navigator !== 'undefined' && (/iP(hone|ad|od)/.test(navigator.userAgent) || (/Macintosh/.test(navigator.userAgent) && navigator.maxTouchPoints > 1));

function iosTick() {
  if (!iosLabel) {
    const input = document.createElement('input');
    input.type = 'checkbox';
    input.setAttribute('switch', '');
    input.id = 'haptic-switch';
    iosLabel = document.createElement('label');
    iosLabel.htmlFor = input.id;
    iosLabel.setAttribute('aria-hidden', 'true');
    Object.assign(iosLabel.style, { position: 'fixed', left: '-100px', top: '0', opacity: '0', pointerEvents: 'none' });
    iosLabel.appendChild(input);
    document.body.appendChild(iosLabel);
  }
  iosLabel.click();
}

function buzz(pattern: number | number[]) {
  try {
    if (canVibrate) navigator.vibrate(pattern);
    else if (isIOS) {
      const n = Array.isArray(pattern) ? Math.ceil(pattern.length / 2) : 1;
      for (let i = 0; i < n; i++) setTimeout(iosTick, i * 90);
    }
  } catch {
    /* sin vibración disponible */
  }
}

// Muchos Android no llegan a mover el motor por debajo de unos 10 ms: los toques cortos se quedaban en nada
let last = 0;
const throttled = (fn: () => void) => () => {
  const now = performance.now();
  if (now - last < 35) return; // dos toques seguidos en el mismo gesto se sienten como uno sucio
  last = now;
  fn();
};

export const haptic = {
  tick: throttled(() => buzz(10)),
  tap: throttled(() => buzz(14)),
  add: throttled(() => buzz(20)),
  remove: throttled(() => buzz(12)),
  success: throttled(() => buzz([16, 70, 26])),
  warn: throttled(() => buzz([30, 50, 30])),
};
