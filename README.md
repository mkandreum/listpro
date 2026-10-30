<div align="center">

# 🛒 ListPro 2.0

**La lista de la compra con los precios de Mercadona y Consum actualizados cada día**

*Un producto de [Xyon Platforms](https://github.com/mkandreum)*

</div>

---

ListPro 2.0 son tres piezas conectadas que comparten catálogo, categorías y cesta. Este repositorio tiene el servidor y la web; la app Android nativa vive en su propio repositorio, [mkandreum/listproapp](https://github.com/mkandreum/listproapp).

| Pieza | Dónde | Stack |
|---|---|---|
| Servidor + scraper | `server/` | Node 22, Express, SQLite (`node:sqlite`) |
| Web | `web/` | React 18 + Vite + TypeScript |
| App Android | [listproapp](https://github.com/mkandreum/listproapp) | Kotlin, Jetpack Compose, Material 3, OkHttp, Coil, WorkManager |

## Cómo funciona

1. **Scraper diario** (`server/src/scraper.js` + un lector por súper en `server/src/scrapers/`):
   - **Mercadona** (`tienda.mercadona.es`): 26 categorías → ~150 subcategorías → grupos, ~4.300 productos. Como su listado no trae el código de barras, cada pasada pide la ficha de hasta `EAN_PER_RUN` productos (1.500 por defecto, los nuevos primero) para poder compararlos.
   - **Consum** (`tienda.consum.es`): ~9.200 productos con código de barras, 93 páginas por pasada.
   - Guarda precio, €/kg, precio tachado de oferta, foto y enlace, y un **histórico de cambios de precio**. Se ejecuta al arrancar si algún catálogo tiene más de 20 h y cada día a las `SCRAPE_HOUR` (hora de Madrid), un súper detrás de otro. Si una pasada trae un catálogo anómalo (muy pocos productos o una caída del 40 %), no machaca el bueno.
   - Los productos de Mercadona conservan sus ids (`27348`); los de Consum llevan prefijo (`c:4667`) y sus categorías un desplazamiento, así nada choca y las cestas y apps antiguas siguen valiendo.
   - Carrefour, Alcampo y Lidl se probaron en octubre de 2026: los dos primeros protegen su tienda online contra bots y Lidl solo publica online unas pocas ofertas. Añadir otro súper es escribir su lector y darlo de alta en `server/src/stores.js`.
2. **Categorías mejoradas** (`server/src/taxonomy.js`): se agrupan por ID (no por palabras clave, que fallaban: "sal" casaba con "salsas") en 9 pasillos de compra, cada uno con su color, y cada categoría lleva fotos reales de sus productos. Las de Consum se colocan en los mismos 9 pasillos.
3. **Comparador** (`server/src/compare.js`): para cada producto, su equivalente en los otros súper. «Mismo producto» si coincide el código de barras; si no, «parecido»: mismo pasillo, mismo tipo de producto (la primera palabra del nombre, en los dos sentidos) y de la misma clase (la que la sigue: aceite de *coco* no es aceite de *oliva*), palabras pesadas por lo raras que son, marca y formato. Se compara por precio por kilo o litro.
4. **API** (`server/src/index.js`):
   - `GET /api/stores` — súper disponibles, con cuántos productos tiene cada uno.
   - `GET /api/catalog?store=consum` — catálogo completo de un súper comprimido (~260 KB gzip Mercadona, ~530 KB Consum) con ETag. Sin `store`, Mercadona.
   - `GET /api/catalog/meta?store=` — versión, para saber si hay precios nuevos.
   - `GET /api/products/:id/compare` — el mismo producto o uno parecido en los otros súper.
   - `POST /api/compare` `{ ids }` — para la cesta: qué productos salen más baratos en otro súper.
   - `GET /api/products/:id/history` — historial de precio.
   - `POST/GET/PUT /api/lists/:code` — lista compartida por código de 8 caracteres (web ↔ Android ↔ familia).
   - `GET /api/planner/options`, `POST /api/plans`, `GET /api/plans/:id`, `POST /api/plans/:id/swap` — planes de recetas con IA (Gemini) sobre el catálogo real, y cambiar una receta suelta.
   - `POST /api/admin/refresh[?store=consum]` — fuerza una actualización de todos o de uno (`Authorization: Bearer $ADMIN_TOKEN`).
5. **Varios súper en web y Android**: el botón «Cambiar súper» de la cabecera cambia todos los productos de la app; al registrarse se elige el súper preferido (se guarda en la cuenta). En cada ficha hay un bloque «En otros súper» con su precio y un + para añadirlo a la cesta de ese súper. La cesta tiene una pestaña por súper en la misma píldora deslizable de Recetas, con su subtotal, un aviso de lo que sale más barato en otro súper (con «Mover») y la compra se termina por súper.
6. **Web y Android** consumen la misma API: pasillos a color, bajadas de precio, búsqueda, cesta ordenada por pasillos con casillas "en el carro", aviso si un precio cambió desde que lo añadiste, cestas guardadas, gastos y sincronización. Android además comparte la lista **por NFC** acercando dos móviles y refresca los precios en segundo plano.

## Recetas con IA (pestaña Recetas)

El usuario elige qué planificar (**desayunos, comidas, meriendas y/o cenas**), cuántos días y personas, presupuesto semanal (la app sugiere uno según lo elegido), súper (Mercadona o Consum), dieta, alergias, gustos, lo que ya tiene en casa, tiempo máximo y electrodomésticos. En web y Android el plan se consulta por secciones desde una barra en píldora que se desliza con el dedo (una página por comida y otra con la lista de la compra).

Cómo lo monta el servidor (`server/src/planner.js`):

1. **Guion de la semana**: antes de llamar a la IA reparte a cada receta una base (pollo, lentejas, merluza, huevos...), un formato (guiso, horno, wok, tortilla...) y un estilo de cocina, sin repetir base en días seguidos ni entre la comida y la cena del mismo día. Solo combina lo que tiene sentido (nada de brochetas de lentejas o guiso de huevo) y el estilo va ligado al formato (el wok es asiático, las fajitas mexicanas); la mayoría de las veces sale cocina casera.
2. **Una llamada por comida, en paralelo**, cada una con su parte del presupuesto, su catálogo (bollería y cereales solo en desayunos y meriendas), las recetas de las otras comidas para no repetirlas y las del plan anterior al pedir «Otro plan».
3. El prompt exige **recetas que existan de verdad**, ingredientes que peguen entre sí, cantidades realistas por persona, que todo lo de los pasos esté en los ingredientes, variedad de verduras, guarniciones y salsas, y productos de temporada.
4. **El servidor recalcula todo con los precios reales**: coste por ración, lista de la compra en envases enteros (sumando lo que se comparte entre recetas) y total frente al presupuesto. Si una comida sale coja o muy cara, solo esa se repite explicándole el fallo.

Cada receta se puede cambiar por otra (`POST /api/plans/:id/swap`, hasta 30 por hora). La nutrición es una estimación de la IA y así se indica. Límite: 8 planes por hora y dirección IP. Los planes antiguos (solo cenas, `nights`) se siguen leyendo.

Para desarrollar sin clave: `PLANNER_MOCK=1 npm start` monta un plan de prueba con productos reales.

## Puesta en marcha

```bash
cd web && npm install && npm run build      # genera server/public
cd ../server && npm install && npm run scrape && npm start
```

Desarrollo de la web con recarga: `npm start` en `server/` (puerto 3000) y `npm run dev` en `web/`.

La app Android se compila desde [listproapp](https://github.com/mkandreum/listproapp); contra este servidor en local usa `-PapiBase=http://10.0.2.2:3000`.

## Desplegar en Coolify

1. En Coolify, crea un recurso nuevo: *Private Repository (GitHub App)* → `mkandreum/listpro`, rama `main`, Build Pack **Docker Compose** (`docker-compose.yaml`).
2. Variables de entorno: `GEMINI_API_KEY` (la de Puchero/VoltBody; sin ella la pestaña Recetas avisa de que no está disponible) y, opcionales, `GEMINI_MODEL_PLAN`, `GEMINI_MODEL_FAST`, `MERCADONA_WH` (por defecto `mad1`), `SCRAPE_HOUR` (por defecto `6`), `EAN_PER_RUN` (por defecto `1500`) y `ADMIN_TOKEN`.
3. Asigna el dominio `listpro.xyoncloud.win` al servicio `app`, puerto **3000**.
4. Lanza **Deploy**. Al arrancar descarga los catálogos que falten (Mercadona ~1,5 min, Consum ~1 min); después se actualizan cada mañana. La base de datos vive en el volumen `listpro_data` y sobrevive a los redespliegues.

Con el recurso creado y *Auto Deploy* activo, cada push a `main` se redespliega solo.

En local: `docker compose up -d --build`.

> ListPro no está afiliada a ningún súper. Los precios se leen de sus tiendas online públicas una vez al día.

## 🏢 Xyon Platforms

ListPro es un producto desarrollado y mantenido por **Xyon Platforms**, empresa especializada en soluciones digitales para negocios locales y pymes.

> © Xyon Platforms — Todos los derechos reservados

## Lista compartida y cuentas

- La cesta se comparte desde el botón **Compartir** de la propia cesta: se crea un código de 8 caracteres y se invita con un enlace (`/#/unirse/CODIGO`) o, en Android, acercando los móviles por NFC.
- Cada persona se apunta con su nombre. La cesta muestra quién pidió cada producto y quién lo ha cogido.
- Si dos personas tocan la lista a la vez, la sincronización junta los cambios con una fusión a tres bandas (cada cliente guarda la última versión vista en el servidor), así no se pisan.
- **Terminar compra** guarda lo que está en el carro (lo demás se queda en la cesta) y, si se quiere, lo reparte: a partes iguales, por producto (cada uno lo que pidió) o sin repartir.
- La pestaña **Gastos → Cuentas** muestra el saldo de cada uno, los pagos mínimos para quedar en paz y el historial. ListPro no mueve dinero: «Saldar» solo apunta el pago.

## Cuentas

- Registro e inicio de sesión con email y contraseña, con el mismo patrón que Puchero y XyonWork: bcrypt (coste 12), JWT HS256 de 90 días con `tokenVersion` para revocar sesiones, límites de intentos y errores que no delatan qué emails existen.
- Cada cuenta apunta a su lista (`list_code`): al entrar desde otro móvil se recupera la cesta, el historial y las cuentas compartidas. El id de la cuenta es también el id de miembro en la lista.
- Rutas: `POST /api/auth/register`, `POST /api/auth/login`, `GET/PUT/DELETE /api/me`, `PUT /api/me/password`, `POST /api/auth/logout-all`.
- `JWT_SECRET` es opcional: si no está, el servidor genera uno y lo guarda en la base de datos del volumen.
- La primera vez la app enseña una presentación de tres pasos y luego pide crear cuenta o entrar; se puede seguir sin cuenta.
