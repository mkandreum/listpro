package com.listpro.xyon.pro.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.listpro.xyon.pro.data.Product
import java.text.NumberFormat
import java.util.Locale

private val euro = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-ES"))
fun money(v: Double): String = euro.format(v)

/** Tarjeta suave: fondo, borde fino y esquinas redondeadas. */
fun Modifier.card(background: Color, border: Color, radius: Dp = Ui.radius): Modifier {
    val shape = RoundedCornerShape(radius)
    return this.clip(shape).background(background).border(Ui.hairline, border, shape)
}

/** Tarjeta elevada: card() con una sombra suave debajo (en oscuro, solo el borde). */
@Composable
fun Modifier.raised(radius: Dp = Ui.radius, background: Color = LocalColors.current.surface): Modifier {
    val c = LocalColors.current
    return this
        .shadow(if (c.isDark) 0.dp else 6.dp, RoundedCornerShape(radius), ambientColor = Color(0x220C1714), spotColor = Color(0x220C1714))
        .card(background, c.line, radius)
}

/** Botón: principal con el degradado verde de la marca; secundario en superficie con borde. */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    danger: Boolean = false,
    ghost: Boolean = false,
    enabled: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
) {
    val c = LocalColors.current
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, label = "press")
    val haptics = rememberHaptics()
    val shape = RoundedCornerShape(if (ghost) 999.dp else 16.dp)
    val fg = when { primary -> c.onGreen; danger -> c.up; ghost -> c.greenInk; else -> c.ink }
    Row(
        modifier = modifier
            .scale(scale)
            .then(if (primary) Modifier.shadow(10.dp, shape, ambientColor = c.greenHi, spotColor = c.greenHi) else Modifier)
            .clip(shape)
            .then(
                when {
                    primary -> Modifier.background(c.gradient)
                    ghost -> Modifier.background(c.greenSoft).border(Ui.hairline, c.greenHi.copy(alpha = 0.45f), shape)
                    else -> Modifier.background(c.surface).border(Ui.hairline, c.lineStrong, shape)
                },
            )
            .clickable(source, indication = null, enabled = enabled, role = Role.Button) { if (danger) haptics.warn() else haptics.tap(); onClick() }
            .defaultMinSize(minHeight = 48.dp)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        CompositionLocalProvider(LocalContentColor provides fg.copy(alpha = if (enabled) 1f else 0.45f)) {
            leading?.invoke()
            Text(text, style = MaterialTheme.typography.labelLarge, color = LocalContentColor.current)
        }
    }
}

/** Precio en Sora: euros y céntimos casi al mismo tamaño, como en las etiquetas de tienda. */
@Composable
fun Price(value: Double, modifier: Modifier = Modifier, size: Int = 26, color: Color = LocalColors.current.ink) {
    val parts = String.format(Locale.US, "%.2f", value).split(".")
    Text(
        buildAnnotatedString {
            append(parts[0])
            withStyle(SpanStyle(fontSize = (size * 0.82f).sp)) { append(",${parts[1]} €") }
        },
        modifier = modifier,
        style = TextStyle(fontFamily = Numeric, fontWeight = FontWeight.Bold, fontSize = size.sp, lineHeight = size.sp, letterSpacing = (-0.035).sp * size),
        color = color,
        maxLines = 1,
    )
}

private data class SealStyle(val fg: Color, val bg: Color, val icon: ImageVector, val text: String)

@Composable
fun Seal(p: Product, modifier: Modifier = Modifier) {
    val d = p.delta
    val s = when {
        d != null && d < 0 -> SealStyle(Color(0xFF0F7A37), Color(0xF0F0FDF4), Icons.Rounded.ArrowDownward, "BAJA ${money(-d)}")
        d != null -> SealStyle(Color(0xFFB42318), Color(0xF0FEF2F2), Icons.Rounded.ArrowUpward, "SUBE ${money(d)}")
        p.isNew -> SealStyle(Color(0xFF5B3FD9), Color(0xF0F5F3FF), Icons.Rounded.AutoAwesome, "NUEVO")
        else -> return
    }
    Row(
        modifier
            .clip(CircleShape)
            .background(s.bg)
            .border(Ui.hairline, s.fg.copy(alpha = 0.55f), CircleShape)
            .padding(start = 7.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(s.icon, null, tint = s.fg, modifier = Modifier.size(13.dp))
        Text(s.text, color = s.fg, style = TextStyle(fontFamily = Numeric, fontWeight = FontWeight.SemiBold, fontSize = 11.sp))
    }
}

@Composable
fun ProductImage(url: String?, fallback: String, modifier: Modifier = Modifier) {
    val emoji = @Composable { Box(modifier, contentAlignment = Alignment.Center) { Text(fallback, fontSize = 34.sp) } }
    if (url == null) return emoji()
    SubcomposeAsyncImage(model = url, contentDescription = null, contentScale = ContentScale.Fit, modifier = modifier, error = { emoji() })
}

/** Añadir → contador: el botón verde redondo se convierte en una píldora con − cantidad +. */
@Composable
fun QtyControl(qty: Int, name: String, onChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    val c = LocalColors.current
    val haptics = rememberHaptics()
    AnimatedContent(
        targetState = qty > 0,
        transitionSpec = { (scaleIn(spring(dampingRatio = 0.7f), initialScale = 0.8f) + fadeIn()) togetherWith fadeOut() },
        modifier = modifier,
        label = "qty",
    ) { inBasket ->
        if (!inBasket) {
            Box(
                Modifier
                    .size(48.dp)
                    .shadow(8.dp, CircleShape, ambientColor = c.greenHi, spotColor = c.greenHi)
                    .clip(CircleShape)
                    .background(c.gradient)
                    .clickable(role = Role.Button) { haptics.add(); onChange(1) }
                    .semantics { contentDescription = "Añadir $name a la cesta" },
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Rounded.Add, null, tint = Color.White, modifier = Modifier.size(26.dp)) }
        } else {
            Row(
                Modifier.height(44.dp).clip(CircleShape).background(c.greenSoft).border(Ui.hairline, c.greenHi.copy(alpha = 0.5f), CircleShape),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(width = 36.dp, height = 48.dp).clickable(role = Role.Button) { haptics.remove(); onChange(-1) }.semantics { contentDescription = "Quitar uno" },
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Rounded.Remove, null, tint = c.greenInk) }
                // La cifra rueda hacia arriba al sumar y hacia abajo al restar
                AnimatedContent(
                    targetState = qty,
                    transitionSpec = {
                        val up = targetState > initialState
                        (slideInVertically { if (up) it else -it } + fadeIn()) togetherWith (slideOutVertically { if (up) -it else it } + fadeOut())
                    },
                    label = "count",
                ) { n ->
                    Text("$n", color = c.ink, style = TextStyle(fontFamily = Numeric, fontWeight = FontWeight.Bold, fontSize = 16.sp), modifier = Modifier.width(20.dp), textAlign = TextAlign.Center)
                }
                Box(
                    Modifier.size(width = 36.dp, height = 48.dp).clickable(role = Role.Button) { haptics.add(); onChange(1) }.semantics { contentDescription = "Añadir uno" },
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Rounded.Add, null, tint = c.greenInk) }
            }
        }
    }
}

/** Tarjeta de producto: foto en su pozo blanco, nombre, formato, precio y botón de añadir. */
@Composable
fun ProductTag(p: Product, qty: Int, emoji: String, onQty: (Int) -> Unit, onOpen: () -> Unit, modifier: Modifier = Modifier, index: Int = 99) {
    val c = LocalColors.current
    val haptics = rememberHaptics()
    val source = remember { MutableInteractionSource() }
    val drop = (p.delta ?: 0.0) < 0
    val shape = RoundedCornerShape(Ui.radiusL)
    Column(
        modifier
            .appear(index)
            .pressScale(source)
            .fillMaxWidth()
            .raised(Ui.radiusL)
            .then(if (qty > 0) Modifier.border(2.dp, c.greenHi.copy(alpha = 0.75f), shape) else Modifier)
            .padding(10.dp),
    ) {
        Box(
            Modifier.fillMaxWidth().aspectRatio(1.12f).clip(RoundedCornerShape(18.dp)).background(Color.White).clickable(source, indication = null, onClickLabel = "Ver detalle") { haptics.tick(); onOpen() },
            contentAlignment = Alignment.Center,
        ) {
            ProductImage(p.image, emoji, Modifier.fillMaxSize().padding(16.dp))
            Seal(p, Modifier.align(Alignment.TopStart).padding(8.dp))
        }
        Column(Modifier.padding(start = 6.dp, end = 2.dp, top = 10.dp, bottom = 2.dp)) {
            Text(p.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 19.sp), maxLines = 2, minLines = 2, overflow = TextOverflow.Ellipsis, color = c.ink)
            Text(p.size, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal), color = c.ink2, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) {
                    if (drop && p.previousPrice != null) {
                        Text(money(p.previousPrice), style = TextStyle(fontFamily = Numeric, fontSize = 12.sp, textDecoration = TextDecoration.LineThrough), color = c.ink3)
                    }
                    Price(p.price, size = if (qty > 0) 21 else 25, color = if (drop) c.greenInk else c.ink)
                    if (p.refPrice != null && p.refFormat != null) {
                        Text("${money(p.refPrice)}/${p.refFormat}", style = TextStyle(fontFamily = Numeric, fontSize = 11.sp), color = c.ink3, maxLines = 1, modifier = Modifier.padding(top = 4.dp))
                    }
                }
                QtyControl(qty, p.name, onQty)
            }
        }
    }
}

@Composable
fun Chip(selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, leading: (@Composable () -> Unit)? = null, content: @Composable RowScope.() -> Unit) {
    val c = LocalColors.current
    val haptics = rememberHaptics()
    Row(
        modifier
            .heightIn(min = 44.dp)
            .clip(CircleShape)
            .background(if (selected) c.green else c.surface)
            .border(Ui.hairline, if (selected) Color.Transparent else c.lineStrong, CircleShape)
            .clickable(role = Role.Tab) { haptics.tick(); onClick() }
            .padding(start = if (leading != null) 6.dp else 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CompositionLocalProvider(LocalContentColor provides if (selected) Color.White else c.ink) {
            leading?.invoke()
            content()
        }
    }
}

@Composable
fun HueDot(color: Color, size: Dp = 10.dp) {
    Box(Modifier.size(size).clip(CircleShape).background(color))
}

/** Fotos de producto como pegatinas blancas giradas. */
@Composable
fun Stickers(images: List<String>, modifier: Modifier = Modifier, size: Dp = 62.dp) {
    val angles = listOf(-8f, 6f, -2f)
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(-size * 0.25f)) {
        images.take(3).forEachIndexed { i, url ->
            Box(
                Modifier
                    .size(size)
                    .rotate(angles[i])
                    .offset(y = if (i == 1) (-6).dp else 0.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(Ui.hairline, Color(0x1A0C1714), RoundedCornerShape(16.dp))
                    .padding(5.dp),
            ) { ProductImage(url, "", Modifier.fillMaxSize()) }
        }
    }
}

/** Círculo con icono: la marca de cada pasillo y de cada sección. */
@Composable
fun IconBadge(icon: ImageVector, color: Color, modifier: Modifier = Modifier, size: Dp = 46.dp) {
    Box(
        modifier.size(size).shadow(8.dp, CircleShape, ambientColor = color, spotColor = color).clip(CircleShape).background(color),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(size * 0.5f)) }
}

@Composable
fun Stamp(text: String, modifier: Modifier = Modifier, dot: Color? = null, trailing: ImageVector? = null) {
    val c = LocalColors.current
    Row(
        modifier.clip(CircleShape).background(c.greenSoft).border(Ui.hairline, c.greenHi.copy(alpha = 0.45f), CircleShape).padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        dot?.let { Box(Modifier.size(8.dp).clip(CircleShape).background(it)) }
        Text(text, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = c.ink, maxLines = 1)
        trailing?.let { Icon(it, null, tint = c.greenInk, modifier = Modifier.size(15.dp)) }
    }
}

@Composable
fun EmptyState(icon: ImageVector, title: String, body: String, modifier: Modifier = Modifier, action: (@Composable () -> Unit)? = null) {
    val c = LocalColors.current
    Column(
        modifier.fillMaxWidth().padding(top = 16.dp).card(c.surface, c.lineStrong, Ui.radiusL).padding(horizontal = 20.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.size(68.dp).clip(RoundedCornerShape(22.dp)).background(c.gradient), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, color = c.ink, textAlign = TextAlign.Center)
        Text(body, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal), color = c.ink2, textAlign = TextAlign.Center)
        action?.invoke()
    }
}

@Composable
fun Panel(modifier: Modifier = Modifier, padding: PaddingValues = PaddingValues(20.dp), content: @Composable () -> Unit) {
    Box(modifier.fillMaxWidth().padding(top = 16.dp).raised(Ui.radiusL).padding(padding)) { content() }
}
