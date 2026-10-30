# ListPro — estado y pendientes (30/10/2026)

## Hecho y subido

- **Consum, imágenes**: `media[]` (`<código>_001.jpg`) en vez de `productData.imageURL`, que da 404. Commit `883fbe2`.
- **Nombre ListPro en móvil**: visible junto al logo en web (<520 px) y Android (<440 dp). Commits `73a307d` y `2ea4c0d`.

## Hecho en local (pendiente de commit)

- **El Corte Inglés**: lector nuevo en `server/src/scrapers/eci.js`. Usa las rutas de categoría con `response_type: json`, sin la API interna. Súper dado de alta como `eci` (prefijo `e:`). Pasada completa: **18.565 productos, 113 categorías, 13,5 min**, sin 429. Reparto: frescos 2.327, lácteos 812, despensa 7.302, congelados 581, bebidas 2.931, cuidado 1.883, hogar 1.382, bebé 416 y mascotas 931.
- **Hipercor**: misma plataforma y mismo lector; se puede activar añadiendo una entrada en `stores.js` cuando se quiera (duplica el volumen de páginas).
- **Logos originales** en los selectores: Mercadona (cesta), Consum (C naranja) y El Corte Inglés (banderín verde). Web en `web/public/logos/`; Android en `res/drawable/logo_*.png`.
- **Color de acento por súper**:
  - Mercadona: verde, como hasta ahora.
  - Consum: naranja.
  - El Corte Inglés: base negra neutra y acento morado (`#7C3AED`), con el logo verde.
  - Web: variable `--accent` y `data-store` en `<html>`.
  - Android: `ListProTheme` recibe el `hue` de la tienda y deriva `greenHi/greenLo/greenInk`.
- **Coste aproximado de la cesta en otros súper**:
  - Servidor: `POST /api/basket/compare` con `{ items: [{ id, qty }] }`.
  - Web: bloque al final de la cesta con total, cobertura y diferencia.
  - Android: mismo bloque en `BasketScreen`.
  - Usa EAN exacto y, si no, equivalente por pasillo/nombre/formato. Marca “aproximado” y cuenta los que faltan.
  - Probado con aceite de oliva: ECI 14,50 €, 100 % de cobertura y −0,88 € frente a Consum.

## Pendiente

- **Verificar el scrape completo de ECI** (en curso) y probar la web con Mercadona + Consum + ECI.
- **Build APK R8** (`assemblePreview` o `assembleRelease` con minify) al final de todo.
- **Hipercor** opcional: activar como segunda tienda ECI si se acepta el coste de otras ~1.000 páginas/día.

## Redeploy

- Al arrancar, el servidor scrapea **solo los catálogos nuevos o viejos**; un redeploy con El Corte Inglés recién añadido lo descarga sin repetir Mercadona y Consum.
- `FORCE_SCRAPE=1` fuerza todos los súpers en el arranque.
- La pasada diaria de las 6:00 sigue actualizando todos.

## Límites de las APIs

- Mercadona, Consum y El Corte Inglés no publican un límite de peticiones ni devuelven cabeceras `RateLimit-*`.
- ECI: 10 peticiones seguidas a 300 ms dieron 200; una sección de 150 páginas a 350 ms no recibió 429. Aun así, el lector nuevo usa 450 ms + jitter para ~1.000 páginas.
- Consum: 93 páginas a 350 ms; funciona a diario.
- Protecciones: una petición a la vez, reintentos con `Retry-After` y backoff exponencial, una pasada diaria fuera de hora punta y el guard que no guarda un catálogo con menos del 60 % del anterior.
- Alcampo no es un límite: es un reto AWS WAF que aparece a las pocas peticiones.

## Súpers descartados

- **Alcampo**: AWS WAF devuelve `202 challenge` a las pocas peticiones, también en búsqueda. No es fiable sin navegador headless/token.
- **Family Cash**: no tiene tienda online ni API; solo tiendas físicas y folletos en PDF/imagen.
- **Lidl**: solo publica bazar y ofertas; no el surtido de alimentación con precio.
