package com.listpro.xyon.pro.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
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

/** Bloque neo-brutalista: fondo, trazo de tinta y sombra dura desplazada. */
fun Modifier.brutal(
    background: Color,
    line: Color,
    shadow: Color,
    radius: Dp = Brutal.radius,
    lift: Dp = Brutal.lift,
    stroke: Dp = Brutal.stroke,
): Modifier {
    val shape = RoundedCornerShape(radius)
    return this
        .drawBehind {
            if (lift > 0.dp) {
                val l = lift.toPx()
                drawRoundRect(shadow, topLeft = Offset(l, l), size = size, cornerRadius = CornerRadius(radius.toPx()))
            }
        }
        .clip(shape)
        .background(background)
        .border(stroke, line, shape)
}

/** Botón de bloque: al pulsar se hunde sobre su sombra. */
@Composable
fun BrutalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    danger: Boolean = false,
    enabled: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
) {
    val c = LocalColors.current
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val lift by animateDpAsState(if (pressed) 0.dp else 3.dp, spring(stiffness = Spring.StiffnessHigh), label = "lift")
    val bg = if (primary) c.lime else c.surface
    val fg = when { primary -> c.onLime; danger -> c.up; else -> c.ink }
    Row(
        modifier = modifier
            .offset { androidx.compose.ui.unit.IntOffset((3.dp - lift).roundToPx(), (3.dp - lift).roundToPx()) }
            .brutal(bg, c.line, c.shadow, radius = 14.dp, lift = lift)
            .clickable(source, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
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

/** Precio a escala de folleto: euros grandes, céntimos pequeños. */
@Composable
fun Price(value: Double, modifier: Modifier = Modifier, size: Int = 30, color: Color = LocalColors.current.ink) {
    val parts = String.format(Locale.US, "%.2f", value).split(".")
    Text(
        buildAnnotatedString {
            append(parts[0])
            withStyle(SpanStyle(fontSize = (size * 0.55f).sp)) { append(",${parts[1]} €") }
        },
        modifier = modifier,
        style = TextStyle(fontFamily = Display, fontWeight = FontWeight.ExtraBold, fontSize = size.sp, lineHeight = (size * 0.95f).sp, letterSpacing = (-0.025).sp * size),
        color = color,
        maxLines = 1,
    )
}

@Composable
fun Seal(p: Product, modifier: Modifier = Modifier) {
    val c = LocalColors.current
    val d = p.delta
    val (bg, fg, text) = when {
        d != null && d < 0 -> Triple(c.down, Color(0xFF0E0E12), "BAJA ${money(-d)}")
        d != null -> Triple(c.up, Color.White, "SUBE ${money(d)}")
        p.isNew -> Triple(Color.White, Color(0xFF0E0E12), "NUEVO")
        else -> return
    }
    Text(
        text,
        modifier = modifier
            .rotate(-5f)
            .brutal(bg, Color(0xFF0E0E12), Color(0xFF0E0E12), radius = 10.dp, lift = 2.dp, stroke = 2.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        color = fg,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
    )
}

@Composable
fun ProductImage(url: String?, fallback: String, modifier: Modifier = Modifier) {
    val emoji = @Composable { Box(modifier, contentAlignment = Alignment.Center) { Text(fallback, fontSize = 34.sp) } }
    if (url == null) return emoji()
    SubcomposeAsyncImage(
        model = url,
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier,
        error = { emoji() },
    )
}

/** Añadir → contador. El "+" lima se convierte en un contador de tinta. */
@Composable
fun QtyControl(qty: Int, name: String, onChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    val c = LocalColors.current
    AnimatedContent(
        targetState = qty > 0,
        transitionSpec = { (scaleIn(spring(dampingRatio = 0.6f), initialScale = 0.7f) + fadeIn()) togetherWith fadeOut() },
        modifier = modifier,
        label = "qty",
    ) { inBasket ->
        if (!inBasket) {
            Box(
                Modifier
                    .size(48.dp)
                    .brutal(c.lime, c.line, c.shadow, radius = 14.dp, lift = 2.dp)
                    .clickable(role = Role.Button) { onChange(1) }
                    .semantics { contentDescription = "Añadir $name a la cesta" },
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Rounded.Add, null, tint = c.onLime, modifier = Modifier.size(26.dp)) }
        } else {
            Row(
                Modifier
                    .height(48.dp)
                    .brutal(c.ink, c.line, c.shadow, radius = 14.dp, lift = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(width = 40.dp, height = 48.dp).clickable(role = Role.Button) { onChange(-1) }
                        .semantics { contentDescription = "Quitar uno" },
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Rounded.Remove, null, tint = c.ground) }
                Text("$qty", color = c.ground, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black), modifier = Modifier.width(22.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Box(
                    Modifier.size(width = 40.dp, height = 48.dp).clickable(role = Role.Button) { onChange(1) }
                        .semantics { contentDescription = "Añadir uno" },
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Rounded.Add, null, tint = c.ground) }
            }
        }
    }
}

/** Etiqueta de producto: foto, nombre, formato y precio grande. */
@Composable
fun ProductTag(
    p: Product,
    qty: Int,
    emoji: String,
    onQty: (Int) -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalColors.current
    val inBasket = qty > 0
    Box(modifier) {
        Column(
            Modifier
                .fillMaxWidth()
                .then(if (inBasket) Modifier.border(4.dp, c.lime, RoundedCornerShape(Brutal.radius + 3.dp)).padding(3.dp) else Modifier)
                .brutal(c.surface, c.line, c.shadow),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color.White)
                    .clickable(onClickLabel = "Ver detalle", onClick = onOpen),
                contentAlignment = Alignment.Center,
            ) {
                ProductImage(p.image, emoji, Modifier.fillMaxSize().padding(14.dp))
            }
            Box(Modifier.fillMaxWidth().height(Brutal.thin).background(c.line))
            Column(Modifier.padding(start = 12.dp, end = 10.dp, top = 10.dp, bottom = 12.dp)) {
                Text(p.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), maxLines = 2, minLines = 2, overflow = TextOverflow.Ellipsis, color = c.ink)
                Text(p.size, style = MaterialTheme.typography.bodySmall, color = c.ink2, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Column(Modifier.weight(1f)) {
                        val d = p.delta
                        if (d != null && d < 0 && p.previousPrice != null) {
                            Text(money(p.previousPrice), style = MaterialTheme.typography.labelSmall.copy(textDecoration = TextDecoration.LineThrough), color = c.ink2)
                        }
                        Price(p.price, size = 28)
                        if (p.refPrice != null && p.refFormat != null) {
                            Text("${money(p.refPrice)}/${p.refFormat}", style = MaterialTheme.typography.labelSmall, color = c.ink2, maxLines = 1)
                        }
                    }
                    QtyControl(qty, p.name, onQty)
                }
            }
        }
        Seal(p, Modifier.padding(10.dp))
    }
}

@Composable
fun Chip(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val c = LocalColors.current
    Row(
        modifier
            .heightIn(min = 44.dp)
            .clip(CircleShape)
            .background(if (selected) c.ink else c.surface)
            .border(Brutal.thin, c.line, CircleShape)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(start = if (leading != null) 6.dp else 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CompositionLocalProvider(LocalContentColor provides if (selected) c.ground else c.ink) {
            leading?.invoke()
            content()
        }
    }
}

@Composable
fun HueDot(color: Color, size: Dp = 14.dp) {
    Box(Modifier.size(size).clip(CircleShape).background(color).border(2.dp, LocalColors.current.line, CircleShape))
}

/** Fotos de producto pegadas como pegatinas, ligeramente giradas. */
@Composable
fun Stickers(images: List<String>, modifier: Modifier = Modifier, size: Dp = 62.dp) {
    val c = LocalColors.current
    val angles = listOf(-7f, 5f, -3f)
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(-size * 0.22f)) {
        images.take(3).forEachIndexed { i, url ->
            Box(
                Modifier
                    .size(size)
                    .rotate(angles[i])
                    .offset(y = if (i == 1) (-4).dp else 0.dp)
                    .brutal(Color.White, c.line, c.shadow, radius = 14.dp, lift = 2.dp, stroke = 2.dp)
                    .padding(5.dp),
            ) { ProductImage(url, "", Modifier.fillMaxSize()) }
        }
    }
}

@Composable
fun Stamp(text: String, modifier: Modifier = Modifier, dot: Color? = null) {
    val c = LocalColors.current
    Row(
        modifier.clip(CircleShape).background(c.surface).border(Brutal.thin, c.line, CircleShape).padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        dot?.let { Box(Modifier.size(8.dp).clip(CircleShape).background(it)) }
        Text(text, style = MaterialTheme.typography.labelMedium, color = c.ink, maxLines = 1)
    }
}

@Composable
fun EmptyState(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, body: String, modifier: Modifier = Modifier, action: (@Composable () -> Unit)? = null) {
    val c = LocalColors.current
    Column(
        modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .border(BorderStroke(Brutal.stroke, c.line.copy(alpha = 0.4f)), RoundedCornerShape(Brutal.radiusL))
            .padding(horizontal = 20.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.rotate(-6f).size(72.dp).brutal(c.lime, c.line, c.shadow, radius = 22.dp), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = c.onLime, modifier = Modifier.size(34.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, color = c.ink, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text(body, style = MaterialTheme.typography.bodyLarge, color = c.ink2, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        action?.invoke()
    }
}

/** Panel blanco con trazo y sombra para agrupar contenido en Gastos y Ajustes. */
@Composable
fun Panel(modifier: Modifier = Modifier, padding: PaddingValues = PaddingValues(20.dp), content: @Composable () -> Unit) {
    val c = LocalColors.current
    Box(modifier.fillMaxWidth().padding(top = 16.dp).brutal(c.surface, c.line, c.shadow, radius = Brutal.radiusL).padding(padding)) { content() }
}

