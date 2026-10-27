# Product

<!-- impeccable:product-schema 1 -->

## Platform

adaptive

La misma app vive como web (React, servida por el servidor de ListPro) y como app Android nativa (Kotlin + Jetpack Compose). Comparten API, catálogo y taxonomía de categorías.

## Stack

- Servidor: Node 22 + Express + SQLite (`node:sqlite`), scraper diario de la API pública de tienda.mercadona.es. Despliegue en Docker en `https://listpro.xyoncloud.win`.
- Web: React (elegido por el usuario) con Vite, servida como estáticos por el mismo servidor.
- Android: Kotlin, Jetpack Compose, Material 3 como base, OkHttp + kotlinx.serialization, Coil, WorkManager. `applicationId com.listpro.xyon.pro` (sucede a la 1.0 WebView, versionCode 2).

## Users

Público general de Play Store que hace la compra en Mercadona. Dos momentos de uso: planificar en casa (buscar, comparar precios, montar la cesta) y en el supermercado con el móvil en una mano (recorrer la cesta por pasillos y saber cuánto va a costar). Tiene que entenderse sin explicación.

## Product Purpose

Lista de la compra con el catálogo completo de Mercadona y sus precios reales, actualizados a diario por el servidor. Éxito: el usuario sabe cuánto le costará la compra antes de ir, ve qué ha subido o bajado, y encuentra cualquier producto en segundos.

## Positioning

Catálogo real de Mercadona (≈4.300 productos, ~150 subcategorías) con precios actualizados cada día e histórico propio de cambios de precio; la cesta se ordena por pasillos de la tienda y avisa si algo ha cambiado de precio desde que lo añadiste.

## Operating Context

- Precios por almacén (por defecto `mad1`, Madrid). Precio por unidad y precio de referencia (€/kg, €/L).
- Sincronización entre dispositivos con un código de 8 caracteres (web ↔ Android ↔ otros miembros de la familia).
- Compartir lista acercando dos móviles por NFC (Host Card Emulation, AID `F0010203040506`) — debe conservarse en Android.
- Funciona sin red con el último catálogo descargado.

## Capabilities and Constraints

- Navegar por pasillo → categoría → subcategoría → grupo (árbol de Mercadona, agrupado por ID en `server/src/taxonomy.js`).
- Buscar, filtrar por bajadas de precio y novedades, ver historial de precio de un producto.
- Cesta con cantidades, total estimado, guardar cestas, repetir compra, estadísticas de gasto.
- Copia de seguridad a archivo JSON.
- No es la tienda de Mercadona: no se compra desde ListPro; enlaza a la ficha oficial.

## Brand Commitments

- Nombre: ListPro, de Xyon Platforms. Idioma: español de España, tono cercano y directo.
- Binding visual pedido por el usuario: neo-brutalismo mezclado con glassmorphism.
- No usar logotipo ni colores corporativos de Mercadona como identidad propia.

## Evidence on Hand

- Catálogo real scrapeado en `server/data/listpro.db`.
- No hay reseñas, usuarios, cifras de descargas ni testimonios: no inventarlos.

## Product Principles

1. El precio es el protagonista: siempre visible, siempre con su fecha de actualización.
2. Una mano en el súper: acciones principales al alcance del pulgar, objetivos táctiles generosos.
3. Encontrar antes que navegar: búsqueda instantánea y categorías que se leen de un vistazo.
4. Lo mismo en web y en Android: misma taxonomía, mismos datos, misma lógica de cesta.

## Accessibility & Inclusion

Público general: contraste AA mínimo, texto escalable (sp en Android), objetivos táctiles ≥48dp, respetar reducir movimiento.
