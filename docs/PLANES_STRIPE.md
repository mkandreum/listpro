# ListPro — planes, Stripe y paywall (diseño para implementar después)

Documento de diseño. La v1 que se implementa ahora **no cobra**: el panel activa
planes a mano y los usuarios nuevos entran con el plan por defecto que marque el
panel (Pro o Estándar). Stripe queda para la siguiente fase.

## Planes

| | Gratis / Estándar | Pro |
|---|---|---|
| Precio | 0 € | 2,99 €/mes o 19,99 €/año |
| Súpers | Mercadona + Consum + El Corte Inglés (invitado: solo Mercadona) | Los 3 |
| Lista, precios y comparador | Sí | Sí |
| Planes IA | 1 al mes | Ilimitados (uso razonable, 8/hora) |
| Cambiar una receta | No | Sí |
| Historial de precios | 7 días | Completo |
| Lista compartida | 2 miembros | 6 miembros |
| Cestas guardadas | 0 | Ilimitadas |
| NFC | No | Sí |
| Estadísticas avanzadas | No | Sí |

Prueba Pro: 14 días desde el registro. Al terminar, plan efectivo = gratis.

## Invitado

- Sin cuenta: solo catálogo de Mercadona y lista local.
- IA, Consum/ECI, listas compartidas y ajustes de cuenta requieren registro.
- Al intentar una función bloqueada: `AUTH_REQUIRED` y CTA “Crear cuenta”.

## Cuotas y errores

El servidor es la fuente de verdad. Cada endpoint premium responde:

```json
{
  "error": "Has usado tu plan de IA de este mes.",
  "code": "PLAN_REQUIRED",
  "feature": "ai_plans",
  "limit": 1,
  "used": 1,
  "resetAt": "2026-11-01T00:00:00.000Z",
  "plans": { "monthly": 2.99, "annual": 19.99 }
}
```

Códigos: `AUTH_REQUIRED`, `PLAN_REQUIRED`, `TRIAL_EXPIRED`, `STORE_LOCKED`.

La web y Android abren un paywall global al recibir estos códigos y también de
forma preventiva al pulsar una función Pro.

## Stripe (fase 2)

### Productos

- Producto `ListPro Pro`.
- Precio mensual recurrente 2,99 €.
- Precio anual recurrente 19,99 €.
- Stripe Tax activado.
- Customer Portal activado (tarjeta, facturas, cancelar).

### Flujo

1. Paywall → `POST /api/billing/checkout` con `monthly|annual`.
2. ListPro crea una Checkout Session con `client_reference_id = user.id`,
   `customer_email`, `subscription_data.metadata.user_id`, `success_url` y
   `cancel_url`.
3. El usuario paga en Stripe (Checkout aloja tarjeta, 3DS, Apple/Google Pay).
4. Stripe llama al webhook de ListPro.
5. ListPro actualiza `users.plan`, `plan_status`, `plan_expires_at`,
   `stripe_customer_id` y `stripe_subscription_id`.
6. “Gestionar suscripción” abre el Customer Portal.

### Webhooks

- `checkout.session.completed`
- `customer.subscription.created`
- `customer.subscription.updated`
- `customer.subscription.deleted`
- `invoice.paid`
- `invoice.payment_failed`

Reglas:

- Verificar `STRIPE_WEBHOOK_SECRET`.
- Body en crudo antes de `express.json()`.
- Guardar `event.id` en `stripe_events` para idempotencia.
- No fiarse del orden: ante cada evento, leer la suscripción actual de Stripe y
  guardar su estado real.
- `active`/`trialing` → Pro.
- `past_due` → Pro 3 días de gracia y luego gratis.
- `canceled` → Pro hasta `current_period_end` y luego gratis.
- `unpaid`/`incomplete_expired` → gratis.

### Base de datos

`users`:

- `stripe_customer_id`
- `stripe_subscription_id`
- `plan`
- `plan_status`
- `plan_expires_at`

`subscriptions`:

- `id`, `user_id`, `stripe_subscription_id`, `price_id`, `status`,
  `current_period_end`, `cancel_at_period_end`, `created_at`, `updated_at`.

`stripe_events`:

- `id` (event id), `type`, `created_at`.

### Variables

ListPro:

```text
STRIPE_SECRET_KEY=sk_live_...
STRIPE_WEBHOOK_SECRET=whsec_...
STRIPE_PRICE_MONTHLY=price_...
STRIPE_PRICE_ANNUAL=price_...
APP_URL=https://listpro.xyoncloud.win
```

Panel: solo lectura/estado; no maneja claves secretas de Stripe.

### Panel

- Estado de suscripción y renovación.
- Enlace a cliente/suscripción en Stripe.
- “Sincronizar con Stripe”.
- Cancelar al final del periodo.
- Ver pagos fallidos, churn e ingresos.
- Activar Pro manual para soporte/regalo.

## Android y Google Play

- APK fuera de Play: Stripe sin problema.
- Play: vender suscripciones dentro de la app puede exigir Play Billing
  (15-30 %). Revisar política antes de publicar.
- v1 segura: Android no vende; muestra “Gestionar en la web” y abre el portal.

## Pruebas

```bash
stripe listen --forward-to localhost:3000/api/stripe/webhook
```

- Tarjetas de prueba.
- Simular renovación, impago y cancelación.
- Probar el portal y la idempotencia de webhooks.

## Orden de implementación

1. Stripe test: producto y precios.
2. Checkout + Portal + webhook en ListPro.
3. Planes/estados en SQLite.
4. Paywall web.
5. Panel: estado y sincronización.
6. Pruebas de renovación/impago/cancelación.
7. Producción.
8. Android: enlace web o Play Billing.
