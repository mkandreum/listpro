package com.listpro.xyon.pro.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.LinkOff
import androidx.compose.material.icons.rounded.Nfc
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.ShoppingBasket
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.listpro.xyon.pro.data.BasketLine
import com.listpro.xyon.pro.data.CatalogIndex
import com.listpro.xyon.pro.data.Product
import com.listpro.xyon.pro.data.SavedBasket
import com.listpro.xyon.pro.data.SyncState
import com.listpro.xyon.pro.data.ThemePref
import com.listpro.xyon.pro.data.UserState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val es = Locale.forLanguageTag("es-ES")
private fun day(iso: String) = runCatching { Instant.parse(iso).atZone(ZoneId.systemDefault()).toLocalDate() }.getOrDefault(LocalDate.now())

@Composable
private fun ScreenTitle(text: String, trailing: (@Composable () -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars).padding(top = 16.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text, style = MaterialTheme.typography.displayMedium, color = LocalColors.current.ink, modifier = Modifier.weight(1f).semantics { heading() })
        trailing?.invoke()
    }
}

// ───────────── Cesta ─────────────

@Composable
fun BasketScreen(
    idx: CatalogIndex?,
    state: UserState,
    lines: List<BasketLine>,
    onQty: (String, Int) -> Unit,
    onToggle: (String) -> Unit,
    onProduct: (Product) -> Unit,
    onClear: () -> Unit,
    onDropMissing: () -> Unit,
    onSave: (String) -> Boolean,
    onReuse: (String) -> Unit,
    onDelete: (String) -> Unit,
    onGoShop: () -> Unit,
) {
    val c = LocalColors.current
    val list = state.list
    val defaultName = remember { "Compra del ${LocalDate.now().format(DateTimeFormatter.ofPattern("d 'de' MMMM", es))}" }
    var name by rememberSaveable { mutableStateOf(defaultName) }
    var confirmClear by rememberSaveable { mutableStateOf(false) }
    val missing = if (idx == null) 0 else list.basket.keys.count { it !in idx.products }
    val total = lines.sumOf { it.product.price * it.qty }
    val done = lines.count { it.product.id in list.inCart }
    var changed = 0
    var delta = 0.0
    lines.forEach { l ->
        val was = list.addedPrices[l.product.id]
        if (was != null && kotlin.math.abs(was - l.product.price) >= 0.005) { changed++; delta += (l.product.price - was) * l.qty }
    }
    val groups = remember(lines) { lines.groupBy { it.product.section }.toList() }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = Gutter, end = Gutter, bottom = NavClearance)) {
        item {
            ScreenTitle("Tu cesta") {
                if (lines.isNotEmpty()) {
                    if (confirmClear) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppButton("No", { confirmClear = false })
                        AppButton("Vaciar", { onClear(); confirmClear = false }, danger = true)
                    } else AppButton("Vaciar", { confirmClear = true }, leading = { Icon(Icons.Rounded.Delete, null, Modifier.size(18.dp)) })
                }
            }
        }
        if (lines.isEmpty() && missing == 0) {
            item {
                EmptyState(Icons.Rounded.ShoppingBasket, "Tu cesta está vacía", "Entra en un pasillo y pulsa + en lo que necesites.") {
                    AppButton("Ir a los pasillos", onGoShop, primary = true, modifier = Modifier.padding(top = 8.dp))
                }
            }
        } else {
            item {
                Column(
                    Modifier.padding(top = 12.dp).fillMaxWidth().shadow(14.dp, RoundedCornerShape(Ui.radiusL), ambientColor = c.greenHi, spotColor = c.greenHi).clip(RoundedCornerShape(Ui.radiusL)).background(c.gradient).padding(22.dp),
                ) {
                    Text("${list.basket.values.sum()} unidades · ${lines.size} productos", style = MaterialTheme.typography.labelLarge, color = c.onGreen)
                    // El total corre hasta su nuevo valor en lugar de saltar
                    val shownTotal by animateFloatAsState(total.toFloat(), tween(450, easing = EaseOutExpo), label = "total")
                    Price(shownTotal.toDouble(), Modifier.padding(vertical = 8.dp), size = 60, color = Color.White)
                    val progress by animateFloatAsState(if (lines.isEmpty()) 0f else done.toFloat() / lines.size, label = "progress")
                    Box(
                        Modifier.fillMaxWidth().height(10.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.25f))
                            .semantics { stateDescription = "$done de ${lines.size} en el carro" },
                    ) {
                        Box(Modifier.fillMaxHeight().fillMaxWidth(progress).clip(CircleShape).background(Color.White))
                    }
                    Text(
                        if (done == lines.size) "¡Todo en el carro!" else "$done de ${lines.size} en el carro · marca lo que vas cogiendo",
                        style = MaterialTheme.typography.labelMedium, color = c.onGreen, modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
            if (changed > 0) item {
                Notice(
                    up = delta > 0,
                    text = "$changed producto${if (changed > 1) "s han" else " ha"} cambiado de precio desde que lo${if (changed > 1) "s" else ""} añadiste (${if (delta > 0) "+" else ""}${money(delta)}).",
                )
            }
            if (missing > 0) item {
                Notice(up = true, text = "$missing producto${if (missing > 1) "s ya no están" else " ya no está"} a la venta. Toca para quitar.", onClick = onDropMissing)
            }
            groups.forEach { (sectionId, ls) ->
                val s = idx?.sections?.get(sectionId)
                val hue = hueColor(s?.hue ?: "#B9BCC4")
                item(key = "band-$sectionId") {
                    Row(
                        Modifier.padding(top = 22.dp, bottom = 2.dp).fillMaxWidth().card(hue, c.line, radius = 12.dp).padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(s?.name ?: "Otros", style = MaterialTheme.typography.titleMedium, color = hue.ink())
                        Text(money(ls.sumOf { it.product.price * it.qty }), style = MaterialTheme.typography.labelLarge, color = hue.ink())
                    }
                }
                items(ls, key = { it.product.id }) { l ->
                    BasketRow(Modifier.animateItem(), l, l.product.id in list.inCart, idx?.subcategories?.get(l.product.subcategory)?.emoji ?: "🛒", onQty, onToggle, onProduct)
                }
            }
            item {
                Row(Modifier.padding(top = 22.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    BasicTextField(
                        value = name,
                        onValueChange = { if (it.length <= 60) name = it },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = c.ink),
                        cursorBrush = SolidColor(c.ink),
                        modifier = Modifier.weight(1f).heightIn(min = 52.dp).card(c.surface, c.line, radius = 14.dp).padding(horizontal = 16.dp, vertical = 15.dp),
                    )
                    AppButton("Guardar", { if (name.isNotBlank() && onSave(name.trim())) name = defaultName }, primary = true)
                }
                Text("Guardar la cesta la vacía y la añade a tus gastos.", style = MaterialTheme.typography.bodySmall, color = c.ink2, modifier = Modifier.padding(top = 8.dp))
            }
        }

        item {
            Row(Modifier.padding(top = 40.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Cestas guardadas", style = MaterialTheme.typography.headlineMedium, color = c.ink, modifier = Modifier.weight(1f).semantics { heading() })
                if (list.savedBaskets.isNotEmpty()) Stamp("${list.savedBaskets.size}")
            }
        }
        if (list.savedBaskets.isEmpty()) {
            item { Text("Cuando guardes una cesta aparecerá aquí para repetirla con un toque.", color = c.ink2, style = MaterialTheme.typography.bodyLarge) }
        }
        items(list.savedBaskets.reversed(), key = { it.id }) { b -> SavedRow(b, idx, onReuse, onDelete) }
    }
}

@Composable
private fun Notice(up: Boolean, text: String, onClick: (() -> Unit)? = null) {
    val c = LocalColors.current
    val tint = if (up) c.up else c.green
    Row(
        Modifier
            .padding(top = 14.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(Ui.radiusS))
            .background(tint.copy(alpha = 0.16f).compositeOver(c.surface))
            .border(2.dp, c.line, RoundedCornerShape(Ui.radiusS))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(if (up) Icons.AutoMirrored.Rounded.TrendingUp else Icons.AutoMirrored.Rounded.TrendingDown, null, tint = c.ink, modifier = Modifier.size(20.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = c.ink)
    }
}

@Composable
private fun BasketRow(modifier: Modifier, l: BasketLine, done: Boolean, emoji: String, onQty: (String, Int) -> Unit, onToggle: (String) -> Unit, onProduct: (Product) -> Unit) {
    val c = LocalColors.current
    val haptics = rememberHaptics()
    val p = l.product
    val alpha by animateFloatAsState(if (done) 0.5f else 1f, label = "done")
    Row(
        modifier
            .padding(top = 10.dp)
            .fillMaxWidth()
            .card(c.surface, c.line)
            .padding(start = 4.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(48.dp).clickable(role = Role.Checkbox, onClickLabel = if (done) "Quitar del carro" else "Marcar en el carro") { if (done) haptics.tick() else haptics.success(); onToggle(p.id) }
                .semantics { stateDescription = if (done) "En el carro" else "Pendiente" },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier.size(30.dp).clip(RoundedCornerShape(9.dp)).background(if (done) c.green else c.surface).border(2.dp, if (done) c.green else c.lineStrong, RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center,
            ) { if (done) Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
        }
        Box(
            Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)).background(Color.White).border(1.5.dp, c.line, RoundedCornerShape(12.dp)).clickable(onClickLabel = "Ver ${p.name}") { onProduct(p) },
        ) { ProductImage(p.image, emoji, Modifier.fillMaxSize().padding(4.dp)) }
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(
                p.name, color = c.ink.copy(alpha = alpha), maxLines = 2, overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, textDecoration = if (done) TextDecoration.LineThrough else null),
            )
            Text("${p.size} · ${money(p.price)}/ud", color = c.ink2.copy(alpha = alpha), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(Modifier.padding(top = 6.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(money(p.price * l.qty), style = MaterialTheme.typography.titleMedium, color = c.ink.copy(alpha = alpha))
                QtyControl(l.qty, p.name, { onQty(p.id, it) })
            }
        }
    }
}

@Composable
private fun SavedRow(b: SavedBasket, idx: CatalogIndex?, onReuse: (String) -> Unit, onDelete: (String) -> Unit) {
    val c = LocalColors.current
    var open by rememberSaveable(b.id) { mutableStateOf(false) }
    val now = b.items.entries.sumOf { (id, q) -> (idx?.products?.get(id)?.price ?: 0.0) * q }
    val diff = now - b.total
    val rotation by animateFloatAsState(if (open) 90f else 0f, label = "chev")
    Column(Modifier.padding(top = 12.dp).fillMaxWidth().card(c.surface, c.line).animateContentSize()) {
        Row(
            Modifier.fillMaxWidth().clickable(onClickLabel = if (open) "Cerrar" else "Abrir") { open = !open }.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(b.name, style = MaterialTheme.typography.titleMedium, color = c.ink)
                Text("${day(b.date).format(DateTimeFormatter.ofPattern("d/M/yyyy"))} · ${b.items.values.sum()} uds. · ${money(b.total)}", style = MaterialTheme.typography.bodySmall, color = c.ink2)
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = c.ink, modifier = Modifier.rotate(rotation))
        }
        if (open) {
            Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                if (kotlin.math.abs(diff) >= 0.01) Notice(diff > 0, "Hoy costaría ${money(now)} (${if (diff > 0) "+" else ""}${money(diff)})")
                b.items.forEach { (id, q) ->
                    val p = idx?.products?.get(id)
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("$q × ${p?.name ?: b.snapshot?.get(id)?.name ?: "Producto retirado"}", style = MaterialTheme.typography.bodyMedium, color = c.ink, modifier = Modifier.weight(1f))
                        Text(money((p?.price ?: b.snapshot?.get(id)?.price ?: 0.0) * q), style = MaterialTheme.typography.labelLarge, color = c.ink)
                    }
                }
                Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppButton("Repetir compra", { onReuse(b.id) }, primary = true, leading = { Icon(Icons.Rounded.Replay, null, Modifier.size(18.dp)) })
                    AppButton("Borrar", { onDelete(b.id) }, danger = true, leading = { Icon(Icons.Rounded.Delete, null, Modifier.size(18.dp)) })
                }
            }
        }
    }
}


// ───────────── Gastos ─────────────

@Composable
fun StatsScreen(idx: CatalogIndex?, saved: List<SavedBasket>, onGoBasket: () -> Unit) {
    val c = LocalColors.current
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = Gutter, end = Gutter, bottom = NavClearance)) {
        item { ScreenTitle("Tus gastos") }
        if (saved.isEmpty()) {
            item {
                EmptyState(Icons.Rounded.BarChart, "Aún no hay compras guardadas", "Guarda tu cesta después de comprar y aquí verás cuánto gastas cada mes y en qué.") {
                    AppButton("Ir a la cesta", onGoBasket, primary = true, modifier = Modifier.padding(top = 8.dp))
                }
            }
            return@LazyColumn
        }
        val today = LocalDate.now()
        val months = (5 downTo 0).map { today.minusMonths(it.toLong()).withDayOfMonth(1) }
        val byMonth = months.associateWith { m -> saved.filter { day(it.date).let { d -> d.year == m.year && d.month == m.month } }.sumOf { it.total } }
        val max = (byMonth.values.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
        val bySection = mutableMapOf<String, Double>()
        val byProduct = mutableMapOf<String, Int>()
        saved.forEach { b ->
            b.items.forEach { (id, q) ->
                val snap = b.snapshot?.get(id)
                val p = idx?.products?.get(id)
                val sec = snap?.section ?: p?.section ?: "otros"
                bySection[sec] = (bySection[sec] ?: 0.0) + (snap?.price ?: p?.price ?: 0.0) * q
                byProduct[id] = (byProduct[id] ?: 0) + q
            }
        }
        val secTotal = bySection.values.sum().coerceAtLeast(0.01)

        item {
            Panel {
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Figure(money(byMonth[months.last()] ?: 0.0), "este mes", Modifier.weight(1f))
                        Figure(money(saved.sumOf { it.total } / saved.size), "por compra", Modifier.weight(1f))
                        Figure("${saved.size}", "compras", Modifier.weight(1f))
                    }
                    Row(Modifier.padding(top = 18.dp).fillMaxWidth().height(200.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Bottom) {
                        months.forEach { m ->
                            val v = byMonth[m] ?: 0.0
                            Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                                if (v > 0) Text("${v.toInt()} €", style = MaterialTheme.typography.labelSmall, color = c.ink)
                                Box(
                                    Modifier.padding(vertical = 6.dp).fillMaxWidth().fillMaxHeight((v / max).toFloat().coerceAtLeast(0.02f) * 0.78f)
                                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                                        .background(if (m == months.last()) c.ink else c.green)
                                        .border(2.dp, c.line, RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart = 4.dp, bottomEnd = 4.dp)),
                                )
                                Text(m.format(DateTimeFormatter.ofPattern("MMM", es)), style = MaterialTheme.typography.labelSmall, color = c.ink2)
                            }
                        }
                    }
                }
            }
        }
        item {
            Panel {
                Column {
                    Text("¿En qué se va el dinero?", style = MaterialTheme.typography.titleLarge, color = c.ink)
                    val rows = bySection.entries.sortedByDescending { it.value }
                    Row(Modifier.padding(top = 14.dp).fillMaxWidth().height(34.dp).clip(RoundedCornerShape(12.dp)).border(Ui.hairline, c.line, RoundedCornerShape(12.dp))) {
                        rows.forEach { (id, v) ->
                            Box(Modifier.weight((v / secTotal).toFloat().coerceAtLeast(0.001f)).fillMaxHeight().background(hueColor(idx?.sections?.get(id)?.hue ?: "#B9BCC4")))
                        }
                    }
                    rows.forEach { (id, v) ->
                        Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(16.dp).clip(RoundedCornerShape(5.dp)).background(hueColor(idx?.sections?.get(id)?.hue ?: "#B9BCC4")).border(2.dp, c.line, RoundedCornerShape(5.dp)))
                            Text(idx?.sections?.get(id)?.name ?: "Otros", style = MaterialTheme.typography.bodyMedium, color = c.ink, modifier = Modifier.weight(1f).padding(start = 10.dp))
                            Text("${money(v)} · ${(v / secTotal * 100).toInt()}%", style = MaterialTheme.typography.labelLarge, color = c.ink)
                        }
                    }
                }
            }
        }
        item {
            Panel {
                Column {
                    Text("Lo que más compras", style = MaterialTheme.typography.titleLarge, color = c.ink)
                    byProduct.entries.sortedByDescending { it.value }.take(6).forEach { (id, q) ->
                        val p = idx?.products?.get(id)
                        val name = p?.name ?: saved.firstNotNullOfOrNull { it.snapshot?.get(id)?.name } ?: "Producto retirado"
                        Row(Modifier.fillMaxWidth().padding(top = 10.dp)) {
                            Text(name, style = MaterialTheme.typography.bodyMedium, color = c.ink, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("$q uds.${p?.let { " · ${money(it.price)}" } ?: ""}", style = MaterialTheme.typography.labelLarge, color = c.ink)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Figure(value: String, label: String, modifier: Modifier = Modifier) {
    val c = LocalColors.current
    Column(modifier) {
        Text(value, style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp), color = c.ink, maxLines = 1)
        Text(label, style = MaterialTheme.typography.labelMedium, color = c.ink2)
    }
}

// ───────────── Ajustes ─────────────

@Composable
fun SettingsScreen(
    idx: CatalogIndex?,
    sync: SyncState,
    theme: ThemePref,
    nfcAvailable: Boolean,
    nfcListening: Boolean,
    onCreateSync: () -> Unit,
    onJoin: (String) -> Unit,
    onLeave: () -> Unit,
    onCopy: (String) -> Unit,
    onNfcShare: () -> Unit,
    onNfcReceive: (Boolean) -> Unit,
    onTheme: (ThemePref) -> Unit,
    onRefresh: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
) {
    val c = LocalColors.current
    var code by rememberSaveable { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = Gutter, end = Gutter, bottom = NavClearance)) {
        item { ScreenTitle("Ajustes") }
        item {
            Panel {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Compartir la lista", style = MaterialTheme.typography.titleLarge, color = c.ink)
                    if (sync.code != null) {
                        Text("Este código une tu cesta en la app, en la web y en los móviles de tu casa.", style = MaterialTheme.typography.bodyLarge, color = c.ink2)
                        Text(
                            sync.code, textAlign = TextAlign.Center,
                            style = TextStyle(fontFamily = Numeric, fontWeight = FontWeight.Bold, fontSize = 34.sp, letterSpacing = 6.sp), color = c.greenInk,
                            modifier = Modifier.fillMaxWidth().card(c.greenSoft, c.greenHi.copy(alpha = 0.4f)).padding(vertical = 14.dp),
                        )
                        AppButton("Copiar código", { onCopy(sync.code) }, Modifier.fillMaxWidth(), leading = { Icon(Icons.Rounded.ContentCopy, null, Modifier.size(18.dp)) })
                        AppButton("Desvincular este dispositivo", onLeave, Modifier.fillMaxWidth(), danger = true, leading = { Icon(Icons.Rounded.LinkOff, null, Modifier.size(18.dp)) })
                    } else {
                        Text("Crea un código y escríbelo en otro móvil u ordenador para compartir la misma cesta.", style = MaterialTheme.typography.bodyLarge, color = c.ink2)
                        AppButton("Crear código", onCreateSync, Modifier.fillMaxWidth(), primary = true, leading = { Icon(Icons.Rounded.Smartphone, null, Modifier.size(18.dp)) })
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            BasicTextField(
                                value = code,
                                onValueChange = { v -> code = v.uppercase().filter { it.isLetterOrDigit() }.take(8) },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = c.ink, letterSpacing = 2.sp),
                                cursorBrush = SolidColor(c.ink),
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                                decorationBox = { inner -> Box { if (code.isEmpty()) Text("Código de 8 caracteres", color = c.ink2, style = MaterialTheme.typography.bodyLarge); inner() } },
                                modifier = Modifier.weight(1f).heightIn(min = 52.dp).card(c.surface, c.line, radius = 14.dp).padding(horizontal = 16.dp, vertical = 15.dp),
                            )
                            AppButton("Unirme", { onJoin(code) }, enabled = code.length == 8)
                        }
                    }
                    if (nfcAvailable) {
                        Spacer(Modifier.height(4.dp))
                        Text("Acercando los móviles (NFC)", style = MaterialTheme.typography.titleMedium, color = c.ink)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AppButton("Enviar", onNfcShare, Modifier.weight(1f), leading = { Icon(Icons.Rounded.Nfc, null, Modifier.size(18.dp)) })
                            AppButton(if (nfcListening) "Esperando…" else "Recibir", { onNfcReceive(!nfcListening) }, Modifier.weight(1f), primary = nfcListening, leading = { Icon(Icons.Rounded.Nfc, null, Modifier.size(18.dp)) })
                        }
                        if (nfcListening) Text("Acerca la parte trasera de este móvil al que tiene la lista y pulsa Enviar en él.", style = MaterialTheme.typography.bodyMedium, color = c.ink2)
                    }
                }
            }
        }
        item {
            Panel {
                Column {
                    Text("Apariencia", style = MaterialTheme.typography.titleLarge, color = c.ink)
                    Row(Modifier.padding(top = 12.dp).fillMaxWidth().clip(RoundedCornerShape(14.dp)).border(Ui.hairline, c.line, RoundedCornerShape(14.dp))) {
                        listOf(ThemePref.System to "Automático", ThemePref.Light to "Claro", ThemePref.Dark to "Oscuro").forEachIndexed { i, (t, label) ->
                            if (i > 0) Box(Modifier.width(2.dp).height(48.dp).background(c.line))
                            Box(
                                Modifier.weight(1f).height(48.dp).background(if (theme == t) c.ink else Color.Transparent).clickable(role = Role.RadioButton) { onTheme(t) },
                                contentAlignment = Alignment.Center,
                            ) { Text(label, style = MaterialTheme.typography.labelLarge, color = if (theme == t) c.ground else c.ink) }
                        }
                    }
                }
            }
        }
        item {
            Panel {
                Column {
                    Text("Catálogo", style = MaterialTheme.typography.titleLarge, color = c.ink)
                    Kv("Productos", idx?.catalog?.count?.toString() ?: "—")
                    Kv("Precios", freshness(idx?.catalog?.updatedAt))
                    Kv("Zona de precios", if (idx?.catalog?.warehouse == "mad1") "Madrid" else idx?.catalog?.warehouse ?: "—")
                    Text("Los precios se descargan cada mañana de la tienda online de Mercadona. ListPro no está afiliada a Mercadona.", style = MaterialTheme.typography.bodySmall, color = c.ink2, modifier = Modifier.padding(vertical = 8.dp))
                    AppButton("Comprobar ahora", onRefresh, Modifier.fillMaxWidth(), leading = { Icon(Icons.Rounded.Refresh, null, Modifier.size(18.dp)) })
                }
            }
        }
        item {
            Panel {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Copia de seguridad", style = MaterialTheme.typography.titleLarge, color = c.ink)
                    AppButton("Guardar copia", onExport, Modifier.fillMaxWidth(), leading = { Icon(Icons.Rounded.Download, null, Modifier.size(18.dp)) })
                    AppButton("Restaurar copia", onImport, Modifier.fillMaxWidth(), leading = { Icon(Icons.Rounded.Upload, null, Modifier.size(18.dp)) })
                }
            }
        }
    }
}

@Composable
private fun Kv(k: String, v: String) {
    val c = LocalColors.current
    Row(Modifier.fillMaxWidth().padding(vertical = 9.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(k, style = MaterialTheme.typography.bodyMedium, color = c.ink2)
        Text(v, style = MaterialTheme.typography.labelLarge, color = c.ink)
    }
}
