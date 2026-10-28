<div align="center">

# 🛒 ListPro 2.0

**La lista de la compra con los precios de Mercadona actualizados cada día**

*Un producto de [Xyon Platforms](https://github.com/mkandreum)*

</div>

---

ListPro 2.0 son tres piezas conectadas que comparten catálogo, categorías y cesta:

| Pieza | Carpeta | Stack |
|---|---|---|
| Servidor + scraper | `server/` | Node 22, Express, SQLite (`node:sqlite`) |
| Web | `web/` | React 18 + Vite + TypeScript |
| App Android | `android/` | Kotlin, Jetpack Compose, Material 3, OkHttp, Coil, WorkManager |

## Cómo funciona

1. **Scraper diario** (`server/src/scraper.js`): recorre la API pública de `tienda.mercadona.es` (26 categorías → ~150 subcategorías → grupos), guarda ~4.300 productos en SQLite con precio, €/kg, foto y enlace, y registra un **histórico de cambios de precio**. Se ejecuta al arrancar si el catálogo tiene más de 20 h y cada día a las `SCRAPE_HOUR` (hora de Madrid). Si una pasada trae un catálogo anómalo (menos de 500 productos o una caída del 40 %), no machaca el bueno.
2. **Categorías mejoradas** (`server/src/taxonomy.js`): se agrupan por ID (no por palabras clave, que fallaban: "sal" casaba con "salsas") en 9 pasillos de compra, cada uno con su color, y cada categoría lleva fotos reales de sus productos.
3. **API** (`server/src/index.js`):
   - `GET /api/catalog` — catálogo completo comprimido (~260 KB gzip) con ETag.
   - `GET /api/catalog/meta` — versión, para saber si hay precios nuevos.
   - `GET /api/products/:id/history` — historial de precio.
   - `POST/GET/PUT /api/lists/:code` — lista compartida por código de 8 caracteres (web ↔ Android ↔ familia).
   - `GET /api/planner/options`, `POST /api/plans`, `GET /api/plans/:id` — planes de cenas con IA (Gemini) sobre el catálogo real.
   - `POST /api/admin/refresh` — fuerza una actualización (`Authorization: Bearer $ADMIN_TOKEN`).
4. **Web y Android** consumen la misma API: pasillos a color, bajadas de precio, búsqueda, cesta ordenada por pasillos con casillas "en el carro", aviso si un precio cambió desde que lo añadiste, cestas guardadas, gastos y sincronización. Android además comparte la lista **por NFC** acercando dos móviles y refresca los precios en segundo plano.

## Planes de cenas (pestaña Cenas)

El usuario indica presupuesto semanal, personas, número de cenas, súper (por ahora Mercadona), dieta, alergias, gustos, lo que ya tiene en casa, tiempo máximo y electrodomésticos. El servidor (`server/src/planner.js`) manda a Gemini esas preferencias y el catálogo de alimentación (~2.000 productos con id y precio) y pide recetas con productos concretos y la fracción de envase que usa cada una. Después **el servidor recalcula todo con los precios reales**: coste por ración, lista de la compra en envases enteros (sumando lo que se comparte entre cenas) y total frente al presupuesto. Si se pasa o faltan cenas, repite una vez explicándole el fallo. La nutrición es una estimación de la IA y así se indica. Límite: 8 planes por hora y dirección IP.

Para desarrollar sin clave: `PLANNER_MOCK=1 npm start` monta un plan de prueba con productos reales.

## Puesta en marcha

```bash
cd web && npm install && npm run build      # genera server/public
cd ../server && npm install && npm run scrape && npm start
```

Desarrollo de la web con recarga: `npm start` en `server/` (puerto 3000) y `npm run dev` en `web/`.

App Android contra un servidor local en el emulador:

```bash
cd android && ./gradlew assembleDebug -PapiBase=http://10.0.2.2:3000
```

Por defecto la app apunta a `https://listpro.xyoncloud.win`. Para probar en un móvil real usa `./gradlew assemblePreview`: es tan rápida como la release (R8, sin depuración), va firmada con la clave de debug y se instala aparte (`com.listpro.xyon.pro.preview`). La build `debug` es mucho más lenta y solo sirve para desarrollar. La firma de release se lee de `~/.android-keys/listpro-keystore.properties`.

## Desplegar en Coolify

1. En Coolify, crea un recurso nuevo: *Private Repository (GitHub App)* → `mkandreum/listpro`, rama `main`, Build Pack **Docker Compose** (`docker-compose.yaml`).
2. Variables de entorno: `GEMINI_API_KEY` (la de Puchero/VoltBody; sin ella la pestaña Cenas avisa de que no está disponible) y, opcionales, `GEMINI_MODEL_PLAN`, `GEMINI_MODEL_FAST`, `MERCADONA_WH` (por defecto `mad1`), `SCRAPE_HOUR` (por defecto `6`) y `ADMIN_TOKEN`.
3. Asigna el dominio `listpro.xyoncloud.win` al servicio `app`, puerto **3000**.
4. Lanza **Deploy**. Al arrancar con la base de datos vacía descarga el catálogo (~1,5 min); después se actualiza cada mañana. La base de datos vive en el volumen `listpro_data` y sobrevive a los redespliegues.

Con el recurso creado y *Auto Deploy* activo, cada push a `main` se redespliega solo.

En local: `docker compose up -d --build`.

> ListPro no está afiliada a Mercadona. Los precios se leen de su tienda online pública una vez al día.

## 🗄️ Versión 1.0 (APK descompilado)

Repositorio de **análisis** del APK descompilado, no del proyecto Android original:

```
01_zip/      Extracto crudo del APK (classes.dex, assets, META-INF)
02_apktool/  apktool: AndroidManifest.xml decodificado, smali/ y res/
03_jadx/     JADX: fuentes Java decompiladas + recursos
RESUMEN.txt  Inventario de archivos del descompilado
```

### Cómo se regenera

```bash
unzip com.listpro.xyon.pro-2.apk -d 01_zip
apktool d com.listpro.xyon.pro-2.apk -o 02_apktool
jadx -d 03_jadx com.listpro.xyon.pro-2.apk
```

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
