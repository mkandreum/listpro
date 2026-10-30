# Design

Sistema visual de ListPro 2.0: **súper fresco y luminoso**. Fondo casi negro verdoso (oscuro) o gris verdoso muy claro (claro), tarjetas suaves con borde fino, verde de marca con degradado y brillo, y cristal en lo que flota (buscador, barra de navegación). Cada pasillo tiene su color, icono y descripción, que sirve la API para que web y Android coincidan.

Implementado en `web/src/styles.css` y, en la app Android ([listproapp](https://github.com/mkandreum/listproapp)), en `app/src/main/java/com/listpro/xyon/pro/ui/Theme.kt` + `Components.kt`.

## Tokens

| Token | Claro | Oscuro | Uso |
|---|---|---|---|
| ground | `#F3F6F4` | `#08110F` | Fondo |
| surface | `#FFFFFF` | `#101B18` | Tarjetas |
| surface-2 | `#EEF3EF` | `#16241F` | Pozos, esqueletos |
| ink / ink-2 / ink-3 | `#0C1714` / `#4E5C57` / `#75827D` | `#F1F6F3` / `#A6B5AF` / `#7C8B85` | Texto |
| line / line-strong | tinta 9 % / 16 % | blanco 7 % / 14 % | Bordes de 1 px |
| green | `#16A34A` | `#22C55E` | Acción, chips activos |
| green-ink | `#0F7A37` | `#4ADE80` | Precio rebajado, cifras |
| gradiente | `#3BE07E → #15A64A` | igual | Botón principal, +, indicador de navegación, total de la cesta, logotipo |
| up | `#E5484D` | `#FF6B6E` | Subidas, contador de cesta |
| violet | `#7C5CFF` | igual | Novedades |
| glass | blanco 74 % + blur 22 px | `#101B18` 72 % + blur | Buscador, raíl de subcategorías, navegación |

Color, icono (`icon`) y descripción (`blurb`) por pasillo: `server/src/taxonomy.js`.

## Tipografía (tres voces)

- **Bricolage Grotesque** 700–800: titulares, marca (`List` + `Pro` en verde), nombres de pasillo.
- **Figtree** 400–700: texto e interfaz.
- **Sora** 500–700: precios, contadores, cifras, código de sincronización.

Web: `@fontsource-variable`. Android: TTF variables en `res/font`, tamaños en `sp`.

## Forma y profundidad

Radios 12 / 20 / 26 px, píldoras para chips, sellos y navegación. Borde de 1 px; sombra suave en claro, solo borde y brillo interior en oscuro. Lo accionable verde lleva un resplandor verde.

## Componentes

- **Tarjeta de producto**: foto en pozo blanco redondeado, sello en píldora con flecha (BAJA / SUBE / NUEVO), nombre, formato, precio en Sora (verde si ha bajado) y botón + redondo que pasa a contador − n +.
- **Banner de pasillo**: degradado del color del pasillo, icono en círculo, título, descripción, contador y fotos de producto como pegatinas; debajo, fila de fichas de categoría con fotos.
- **Accesos rápidos**: tarjetas con icono en baldosa de color, título y cifra.
- **Navegación**: píldora de cristal con indicador verde deslizante (muelle suave); raíl vertical en escritorio.

## Movimiento

Indicador de navegación y paso de + a contador con muelle suave; el resto, desvanecidos y elevaciones cortas. Se respeta "reducir movimiento".
