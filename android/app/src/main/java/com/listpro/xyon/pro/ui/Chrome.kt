package com.listpro.xyon.pro.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.RestaurantMenu
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShoppingBasket
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.Animatable
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.listpro.xyon.pro.data.Api
import com.listpro.xyon.pro.data.PriceHistory
import com.listpro.xyon.pro.data.Product
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private data class NavItem(val tab: Tab, val label: String, val icon: ImageVector)

private val NAV = listOf(
    NavItem(Tab.Products, "Productos", Icons.Rounded.Storefront),
    NavItem(Tab.Plan, "Recetas", Icons.Rounded.RestaurantMenu),
    NavItem(Tab.Basket, "Cesta", Icons.Rounded.ShoppingBasket),
    NavItem(Tab.Stats, "Gastos", Icons.Rounded.BarChart),
    NavItem(Tab.Settings, "Ajustes", Icons.Rounded.Settings),
)

/**
 * Barra de navegación en píldora de cristal. Un indicador de cristal tintado de lima
 * se desliza con un muelle hasta la pestaña activa.
 */
@Composable
fun GlassPillNav(selected: Tab, basketCount: Int, onSelect: (Tab) -> Unit, modifier: Modifier = Modifier) {
    val c = LocalColors.current
    val index = NAV.indexOfFirst { it.tab == selected }.coerceAtLeast(0)
    val haptics = rememberHaptics()
    // El contador de la cesta da un salto cada vez que entra algo
    val bump = remember { Animatable(1f) }
    var lastCount by remember { mutableIntStateOf(basketCount) }
    LaunchedEffect(basketCount) {
        if (basketCount > lastCount) {
            bump.snapTo(1.35f)
            bump.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = 500f))
        }
        lastCount = basketCount
    }
    BoxWithConstraints(
        modifier
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 12.dp)
            .widthIn(max = 500.dp)
            .fillMaxWidth()
            .height(76.dp)
            .shadow(16.dp, CircleShape, ambientColor = Color(0x330C1714), spotColor = Color(0x330C1714))
            .clip(CircleShape)
            .background(c.glass)
            .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = if (c.isDark) 0.05f else 0.4f), Color.Transparent)))
            .border(Ui.hairline, c.lineStrong, CircleShape)
            .padding(7.dp)
            .selectableGroup(),
    ) {
        val itemWidth = maxWidth / NAV.size
        val x by animateDpAsState(itemWidth * index, spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow), label = "pill")
        // Indicador deslizante
        Box(
            Modifier
                .offset(x = x)
                .width(itemWidth)
                .fillMaxHeight()
                .shadow(10.dp, CircleShape, ambientColor = c.greenHi, spotColor = c.greenHi)
                .clip(CircleShape)
                .background(c.gradient)
                .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.25f), Color.Transparent), endY = 40f)),
        )
        Row(Modifier.fillMaxSize()) {
            NAV.forEachIndexed { i, item ->
                val active = i == index
                val scale by animateFloatAsState(if (active) 1.12f else 1f, spring(dampingRatio = 0.5f), label = "icon")
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .selectable(active, onClick = { if (!active) haptics.tap(); onSelect(item.tab) }, role = Role.Tab, interactionSource = remember { MutableInteractionSource() }, indication = null),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box {
                        Icon(item.icon, null, tint = if (active) Color.White else c.ink2, modifier = Modifier.size(24.dp).scale(scale))
                        if (item.tab == Tab.Basket && basketCount > 0) {
                            Box(
                                Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 14.dp, y = (-8).dp)
                                    .graphicsLayer { scaleX = bump.value; scaleY = bump.value }
                                    .height(22.dp)
                                    .widthIn(min = 22.dp)
                                    .clip(CircleShape)
                                    .background(c.up)
                                    .padding(horizontal = 5.dp)
                                    .semantics { contentDescription = "$basketCount unidades" },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(if (basketCount > 99) "99+" else "$basketCount", color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black))
                            }
                        }
                    }
                    Text(
                        item.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Bold),
                        color = if (active) Color.White else c.ink2,
                    )
                }
            }
        }
    }
}

// ───────────── Hoja de producto ─────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductSheet(p: Product, sectionName: String?, emoji: String, qty: Int, api: Api, onQty: (Int) -> Unit, onDismiss: () -> Unit) {
    val c = LocalColors.current
    val uri = LocalUriHandler.current
    val history by produceState<PriceHistory?>(null, p.id) { value = runCatching { api.history(p.id) }.getOrNull() }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = c.surface,
        shape = RoundedCornerShape(topStart = Ui.radiusL, topEnd = Ui.radiusL),
    ) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 28.dp)) {
            sectionName?.let { Stamp(it) }
            Box(
                Modifier.padding(top = 12.dp).fillMaxWidth().aspectRatio(4f / 3f).clip(RoundedCornerShape(Ui.radius)).background(Color.White)
                    .border(Ui.hairline, c.line, RoundedCornerShape(Ui.radius)),
            ) {
                ProductImage(p.image?.replace("h=300&w=300", "h=600&w=600"), emoji, Modifier.fillMaxSize().padding(18.dp))
                Seal(p, Modifier.padding(10.dp))
            }
            Text(p.name, style = MaterialTheme.typography.headlineMedium, color = c.ink, modifier = Modifier.padding(top = 14.dp))
            Text(p.size, style = MaterialTheme.typography.bodyLarge, color = c.ink2)
            Row(Modifier.padding(top = 14.dp).fillMaxWidth(), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Price(p.price, size = 48)
                    if (p.refPrice != null && p.refFormat != null) Text("${money(p.refPrice)}/${p.refFormat}", style = MaterialTheme.typography.labelMedium, color = c.ink2)
                }
                QtyControl(qty, p.name, onQty)
            }
            Text("Historial de precio", style = MaterialTheme.typography.titleLarge, color = c.ink, modifier = Modifier.padding(top = 22.dp))
            PriceChart(history)
            p.url?.let { url ->
                Spacer(Modifier.height(18.dp))
                AppButton("Ver en la tienda de Mercadona", { uri.openUri(url) }, Modifier.fillMaxWidth(), leading = { Icon(Icons.AutoMirrored.Rounded.OpenInNew, null, Modifier.size(18.dp)) })
            }
        }
    }
}

@Composable
private fun PriceChart(history: PriceHistory?) {
    val c = LocalColors.current
    if (history == null) {
        Box(Modifier.padding(top = 10.dp).fillMaxWidth().height(110.dp).clip(RoundedCornerShape(Ui.radiusS)).background(c.surface2))
        return
    }
    val pts = history.history
    val fmt = DateTimeFormatter.ofPattern("d/M/yyyy")
    val since = pts.firstOrNull()?.let { Instant.parse(it.at).atZone(ZoneId.systemDefault()).format(fmt) } ?: "hoy"
    if (pts.size < 2) {
        Text("Sin cambios de precio desde que lo seguimos ($since).", style = MaterialTheme.typography.bodyMedium, color = c.ink2, modifier = Modifier.padding(top = 10.dp))
        return
    }
    val times = pts.map { Instant.parse(it.at).toEpochMilli() } + Instant.now().toEpochMilli()
    val prices = pts.map { it.price } + pts.last().price
    val lo = prices.min()
    val hi = prices.max()
    Canvas(
        Modifier.padding(top = 10.dp).fillMaxWidth().height(110.dp).clip(RoundedCornerShape(Ui.radiusS)).background(c.ground)
            .border(Ui.hairline, c.line, RoundedCornerShape(Ui.radiusS)).padding(12.dp)
            .semantics { contentDescription = "Historial de precio entre ${money(lo)} y ${money(hi)}" },
    ) {
        val t0 = times.first().toFloat()
        val span = (times.last() - times.first()).coerceAtLeast(1).toFloat()
        fun x(t: Long) = (t - t0) / span * size.width
        fun y(v: Double) = size.height - ((v - lo) / (hi - lo).coerceAtLeast(0.01) * size.height).toFloat()
        val path = Path().apply {
            moveTo(x(times[0]), y(prices[0]))
            for (i in 1 until times.size) { lineTo(x(times[i]), y(prices[i - 1])); lineTo(x(times[i]), y(prices[i])) }
        }
        drawPath(path, c.ink, style = Stroke(width = 3.dp.toPx()))
    }
    Text("Mín. ${money(lo)} · Máx. ${money(hi)} · ${pts.size - 1} cambios desde $since", style = MaterialTheme.typography.bodySmall, color = c.ink2, modifier = Modifier.padding(top = 8.dp))
}
