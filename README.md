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
   - `POST /api/admin/refresh` — fuerza una actualización (`Authorization: Bearer $ADMIN_TOKEN`).
4. **Web y Android** consumen la misma API: pasillos a color, bajadas de precio, búsqueda, cesta ordenada por pasillos con casillas "en el carro", aviso si un precio cambió desde que lo añadiste, cestas guardadas, gastos y sincronización. Android además comparte la lista **por NFC** acercando dos móviles y refresca los precios en segundo plano.

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
2. Variables de entorno (todas opcionales): `MERCADONA_WH` (por defecto `mad1`), `SCRAPE_HOUR` (por defecto `6`), `ADMIN_TOKEN`.
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
