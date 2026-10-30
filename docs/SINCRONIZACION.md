# Sincronización web ↔ Android

La misma cuenta ve y edita la misma lista desde la web y desde Android.

## Cómo funciona ahora

- **Web**: `pullSync()` cada **4 segundos** mientras la pestaña está visible; también al volver a la pestaña y al hacer cambios.
- **Android**: `pullSync()` al entrar en primer plano y cada **5 segundos** con `repeatOnLifecycle(RESUMED)`.
- Los cambios locales se envían con `pushSync()` y se mezclan por `updatedAt`; el servidor es la fuente de verdad.
- La sincronización se para en segundo plano para no gastar batería ni datos.

Con esto, un cambio hecho en el móvil aparece en la web en menos de 5 segundos (y al revés), siempre que ambos estén abiertos.

## Siguiente fase: SSE (instantáneo)

Para que sea inmediato sin sondeo:

1. `GET /api/sync/:code/events` con `text/event-stream`, autenticado con el token de la cuenta.
2. Al guardar una lista, el servidor publica `list.updated` con `{ code, updatedAt, version }`.
3. La web y Android abren `EventSource` / leen el stream y llaman a `pullSync()` solo cuando llega el evento.
4. Reconexión con `Last-Event-ID` y volver al sondeo de 15-30 s si el stream falla.

No se implementa todavía: el sondeo corto ya cumple “sincronizado en todo momento” sin mantener conexiones abiertas en Coolify.
