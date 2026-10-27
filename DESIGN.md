# Design

Sistema visual de ListPro 2.0: **folleto de ofertas del súper reinventado** — neo-brutalismo (trazo de tinta, sombra dura desplazada) mezclado con cristal esmerilado solo donde algo flota sobre contenido en movimiento. Implementado en `web/src/styles.css` y `android/app/src/main/java/com/listpro/xyon/pro/ui/Theme.kt` + `Components.kt`.

## Tokens

| Token | Claro | Oscuro | Uso |
|---|---|---|---|
| ground | `#F3F4F1` | `#111116` | Fondo de pantalla |
| surface | `#FFFFFF` | `#1C1C23` | Etiquetas, fichas, paneles |
| surface-2 | `#E9EAE5` | `#26262F` | Esqueletos de carga |
| ink | `#0E0E12` | `#F4F4EE` | Texto y trazo |
| ink-2 | `#4B4D55` | `#B4B5BE` | Texto secundario |
| line / shadow | `#0E0E12` / `#0E0E12` | `#F4F4EE` / `#000` | Trazo y sombra dura |
| glass | blanco 64 % + blur 18 px | `#1C1C23` 62 % + blur | Buscador, raíl de subcategorías, barra de navegación |
| lime | `#C8FF3D` (texto `#0E0E12`) | igual | Acción principal, indicador de la barra, total de la cesta |
| down | `#13C46B` | igual | Sello "BAJA", avisos de bajada |
| up | `#FF4D3A` | igual | Sello "SUBE", contador de cesta, avisos de subida |

### Color por pasillo (`hue`, servido por la API)

Frescos `#2FD27A` · Lácteos y huevos `#7CC8FF` · Despensa `#FF8A2A` · Congelados `#5C7CFF` · Bebidas `#FF5FA8` · Cuidado personal `#B691FF` · Limpieza y hogar `#FFD23F` · Bebé `#6FE3CF` · Mascotas `#D9935A` · Otros `#B9BCC4`. El texto sobre el color se elige por contraste (tinta o blanco). Cada pasillo pinta su bloque de borde a borde en Inicio y la banda de su grupo en la Cesta.

## Forma

- Trazo 2.5 px (2 px en piezas pequeñas); radios 12 / 18 / 26; sombra dura 4 px desplazada sin desenfoque (2–3 px en controles, 6 px en bloques protagonistas).
- Al pulsar, los botones se hunden sobre su sombra.
- Barra de navegación: píldora de cristal con indicador lima deslizante (muelle), raíl vertical en escritorio ≥960 px.

## Tipografía

- **Bricolage Grotesque** (variable, 600–800) para títulos y precios; tracking negativo (−0.02 a −0.05 em).
- **Figtree** (variable, 400–900) para texto e interfaz.
- Precio a escala de folleto: euros grandes, céntimos a ~55 %.
- Web: autoalojadas con `@fontsource-variable`. Android: TTF variables en `res/font`, tamaños en `sp`.

## Componentes firma

- **Etiqueta de producto**: foto sobre blanco, nombre en 2 líneas, formato, precio grande, €/kg, sello girado (BAJA/SUBE/NUEVO); al añadir, el "+" lima se convierte en contador de tinta y la etiqueta gana un halo lima.
- **Ficha de categoría**: nombre grande + 3 fotos de producto como pegatinas giradas; la categoría mayor de cada pasillo ocupa dos columnas.
- **Total de la cesta**: bloque lima con el total a escala display y barra de progreso "en el carro".

## Movimiento

Un muelle (`cubic-bezier(.34,1.3–1.56,.64,1)` / `spring(0.6–0.72)`) para el indicador de navegación, el paso de "+" a contador y el rebote del contador de cesta. Resto: fundidos cortos. Se respeta "reducir movimiento".

## Iconos

Lucide (web) y Material Symbols Rounded (Android). Los emoji solo aparecen como reserva si falla la foto de un producto.
