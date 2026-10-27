package com.listpro.xyon.pro.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.ShoppingBasket
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.listpro.xyon.pro.data.Category
import com.listpro.xyon.pro.data.CatalogIndex
import com.listpro.xyon.pro.data.Product
import com.listpro.xyon.pro.data.Section
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

val Gutter = 16.dp
/** Espacio para que la barra píldora flotante no tape el final de las listas. */
val NavClearance = 120.dp

fun freshness(iso: String?): String {
    val t = iso?.let { runCatching { Instant.parse(it) }.getOrNull() } ?: return "Sin precios"
    val local = t.atZone(ZoneId.systemDefault())
    return if (local.toLocalDate() == java.time.LocalDate.now()) "Precios de hoy · ${local.format(DateTimeFormatter.ofPattern("HH:mm"))}"
    else {
        val days = Duration.between(t, Instant.now()).toDays()
        if (days <= 1) "Precios de ayer" else "Precios de hace $days días"
    }
}

@Composable
fun TopBar(updatedAt: String?) {
    val c = LocalColors.current
    Row(
        Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(38.dp).rotate(-6f).brutal(c.lime, c.line, c.shadow, radius = 12.dp, lift = 3.dp), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.ShoppingBasket, null, tint = c.onLime, modifier = Modifier.size(22.dp))
            }
            Text("ListPro", style = MaterialTheme.typography.headlineLarge, color = c.ink, modifier = Modifier.semantics { heading() })
        }
        if (updatedAt != null) Stamp(freshness(updatedAt), dot = c.down)
    }
}


/** Buscador de cristal. En Inicio solo abre la búsqueda; en Búsqueda escribe. */
@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    autoFocus: Boolean = false,
    onBack: (() -> Unit)? = null,
) {
    val c = LocalColors.current
    val focus = remember { FocusRequester() }
    LaunchedEffect(autoFocus) { if (autoFocus) focus.requestFocus() }
    Row(
        modifier
            .fillMaxWidth()
            .height(58.dp)
            .brutal(c.glass, c.line, c.shadow, radius = Brutal.radius, stroke = Brutal.thin)
            .padding(start = if (onBack != null) 4.dp else 18.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (onBack != null) {
            Box(Modifier.size(48.dp).clip(CircleShape).clickable(onClickLabel = "Volver", onClick = onBack), contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Volver", tint = c.ink)
            }
        } else {
            Icon(Icons.Rounded.Search, null, tint = c.ink)
        }
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty()) Text("Busca leche, aguacate, detergente…", color = c.ink2, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), maxLines = 1, overflow = TextOverflow.Ellipsis)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = c.ink),
                cursorBrush = SolidColor(c.ink),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier.fillMaxWidth().focusRequester(focus),
            )
        }
        if (value.isNotEmpty()) {
            Box(Modifier.size(48.dp).clip(CircleShape).clickable(onClickLabel = "Borrar búsqueda") { onValueChange("") }, contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Close, "Borrar búsqueda", tint = c.ink)
            }
        }
    }
}

@Composable
private fun SearchLauncher(onClick: () -> Unit) {
    val c = LocalColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .height(58.dp)
            .brutal(c.glass, c.line, c.shadow, stroke = Brutal.thin)
            .clickable(role = Role.Button, onClickLabel = "Buscar productos", onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Rounded.Search, null, tint = c.ink)
        Text("Busca leche, aguacate, detergente…", color = c.ink2, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// ───────────── Inicio ─────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    idx: CatalogIndex?,
    error: Boolean,
    basket: Map<String, Int>,
    onOpen: (Route) -> Unit,
    onProduct: (Product) -> Unit,
    onQty: (String, Int) -> Unit,
    onRetry: () -> Unit,
) {
    val c = LocalColors.current
    val list = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val searchPinned by remember { derivedStateOf { list.firstVisibleItemIndex > 0 } }

    LazyColumn(state = list, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = NavClearance)) {
        item(key = "top") {
            Column(Modifier.windowInsetsPadding(WindowInsets.statusBars).padding(horizontal = Gutter)) {
                TopBar(idx?.catalog?.updatedAt)
            }
        }
        stickyHeader(key = "search") {
            Box(Modifier.then(if (searchPinned) Modifier.windowInsetsPadding(WindowInsets.statusBars) else Modifier).padding(horizontal = Gutter).padding(top = 4.dp, bottom = 14.dp)) {
                SearchLauncher { onOpen(Route.Search("")) }
            }
        }
        if (idx == null) {
            item(key = "state") {
                Box(Modifier.padding(horizontal = Gutter)) {
                    if (error) {
                        EmptyState(Icons.Rounded.WifiOff, "No llegan los precios", "Revisa la conexión. En cuanto vuelva, cargamos el catálogo.") {
                            BrutalButton("Reintentar", onRetry, primary = true)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            repeat(3) { Box(Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(Brutal.radius)).background(c.surface2)) }
                        }
                    }
                }
            }
            return@LazyColumn
        }

        item(key = "chips") {
            LazyRow(contentPadding = PaddingValues(horizontal = Gutter), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (idx.deals.isNotEmpty()) item {
                    Chip(false, { onOpen(Route.Deals(false)) }) {
                        Icon(Icons.AutoMirrored.Rounded.TrendingDown, null, Modifier.size(18.dp))
                        Text("Bajadas ${idx.deals.size}", style = MaterialTheme.typography.labelLarge)
                    }
                }
                if (idx.newProducts.isNotEmpty()) item {
                    Chip(false, { onOpen(Route.Deals(true)) }) {
                        Icon(Icons.Rounded.AutoAwesome, null, Modifier.size(18.dp))
                        Text("Novedades ${idx.newProducts.size}", style = MaterialTheme.typography.labelLarge)
                    }
                }
                itemsIndexed(idx.catalog.sections) { i, s ->
                    Chip(false, {
                        // salto al pasillo: cabecera, buscador, chips, bajadas y luego un item por pasillo
                        scope.launch { list.animateScrollToItem(4 + i) }
                    }, leading = { HueDot(hueColor(s.hue)) }) {
                        Text(s.name, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        item(key = "deals") {
            if (idx.deals.isNotEmpty()) {
                Column {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = Gutter).padding(top = 26.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Han bajado", style = MaterialTheme.typography.headlineLarge, color = c.ink, modifier = Modifier.semantics { heading() })
                        BrutalButton("Ver ${idx.deals.size}", { onOpen(Route.Deals(false)) })
                    }
                    LazyRow(contentPadding = PaddingValues(start = Gutter, end = Gutter, bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(idx.deals.take(14), key = { it.id }) { p ->
                            ProductTag(
                                p, basket[p.id] ?: 0, idx.subcategories[p.subcategory]?.emoji ?: "🛒",
                                onQty = { onQty(p.id, it) }, onOpen = { onProduct(p) },
                                modifier = Modifier.width(180.dp),
                            )
                        }
                    }
                }
            }
        }

        items(idx.catalog.sections, key = { "aisle-${it.id}" }) { s -> Aisle(s) { onOpen(Route.CategoryRoute(it)) } }
    }
}

private fun <T> androidx.compose.foundation.lazy.LazyListScope.itemsIndexed(list: List<T>, content: @Composable (Int, T) -> Unit) {
    items(list.size) { i -> content(i, list[i]) }
}

/** Un pasillo pintado de borde a borde con su color, y sus categorías como fichas con pegatinas. */
@Composable
private fun Aisle(s: Section, onCategory: (Int) -> Unit) {
    val c = LocalColors.current
    val hue = hueColor(s.hue)
    val onHue = hue.ink()
    val biggest = s.categories.maxByOrNull { it.count }
    Column(
        Modifier
            .padding(top = 26.dp)
            .fillMaxWidth()
            .background(hue)
            .border(Brutal.stroke, c.line)
            .padding(horizontal = Gutter, vertical = 22.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(bottom = 16.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(s.name, style = MaterialTheme.typography.headlineLarge, color = onHue, modifier = Modifier.weight(1f).semantics { heading() })
            Stamp("${s.count}")
        }
        // Mosaico: la categoría más grande ocupa dos columnas si el pasillo tiene más de dos.
        val wide = biggest.takeIf { s.categories.size > 2 }
        val rest = s.categories.filter { it != wide }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val firstRows = rest.chunked(2)
            val insertAt = (firstRows.size / 2).coerceAtLeast(0)
            firstRows.forEachIndexed { i, row ->
                if (wide != null && i == insertAt) CategoryCard(wide, true, Modifier.fillMaxWidth()) { onCategory(wide.id) }
                Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { cat -> CategoryCard(cat, false, Modifier.weight(1f).fillMaxHeight()) { onCategory(cat.id) } }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
            if (wide != null && insertAt >= firstRows.size) CategoryCard(wide, true, Modifier.fillMaxWidth()) { onCategory(wide.id) }
        }
    }
}

@Composable
private fun CategoryCard(cat: Category, wide: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val c = LocalColors.current
    Box(
        modifier
            .heightIn(min = if (wide) 168.dp else 150.dp)
            .brutal(c.surface, c.line, c.shadow)
            .clickable(role = Role.Button, onClickLabel = "Abrir ${cat.name}", onClick = onClick)
            .padding(14.dp),
    ) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
          Column {
            Text(
                cat.name,
                style = if (wide) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleMedium,
                color = c.ink,
                modifier = Modifier.padding(end = 36.dp),
            )
            Text("${cat.count} productos · ${cat.subcategories.size} secciones", style = MaterialTheme.typography.labelSmall, color = c.ink2, modifier = Modifier.padding(top = 4.dp))
          }
            Stickers(cat.images, Modifier.padding(top = 12.dp), size = if (wide) 76.dp else 58.dp)
        }
        Box(
            Modifier.align(Alignment.TopEnd).size(32.dp).clip(CircleShape).background(c.ink),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Rounded.NorthEast, null, tint = c.ground, modifier = Modifier.size(18.dp)) }
    }
}

// ───────────── Categoría ─────────────

@Composable
fun CategoryScreen(
    idx: CatalogIndex,
    id: Int,
    basket: Map<String, Int>,
    onBack: () -> Unit,
    onProduct: (Product) -> Unit,
    onQty: (String, Int) -> Unit,
) {
    val c = LocalColors.current
    val (cat, section) = idx.categories[id] ?: return EmptyState(Icons.Rounded.SearchOff, "Esta categoría ya no existe", "Vuelve a los pasillos.", Modifier.padding(Gutter))
    val hue = hueColor(section.hue)
    val onHue = hue.ink()
    val products = idx.byCategory[id].orEmpty()
    val bySub = remember(products) { products.groupBy { it.subcategory } }
    val cheapest = remember(products) { products.minByOrNull { it.price } }

    // La rejilla se aplana en filas (cabecera de subcategoría, etiqueta de grupo, producto) para saber
    // en qué posición empieza cada subcategoría y que el raíl la siga.
    val entries = remember(cat, bySub) {
        buildList<GridEntry> {
            cat.subcategories.forEach { s ->
                add(GridEntry.Header(s.id, s.name))
                var last: String? = null
                bySub[s.id].orEmpty().forEach { p ->
                    if (p.group != last && p.group != s.name && p.group.isNotEmpty()) add(GridEntry.Group("${s.id}-${p.group}", p.group))
                    last = p.group
                    add(GridEntry.Item(p, s.emoji))
                }
            }
        }
    }
    val subStart = remember(entries) {
        entries.withIndex().filter { it.value is GridEntry.Header }.associate { (it.value as GridEntry.Header).id to it.index + 2 }
    }
    val grid = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    val active by remember(subStart) {
        derivedStateOf { subStart.entries.lastOrNull { it.value <= grid.firstVisibleItemIndex + 1 }?.key ?: cat.subcategories.first().id }
    }
    val rail = rememberLazyListState()
    LaunchedEffect(active) {
        val i = cat.subcategories.indexOfFirst { it.id == active }
        if (i >= 0) rail.animateScrollToItem(i)
    }

    val SubRail: @Composable () -> Unit = {
        LazyRow(
            state = rail,
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth()
                .brutal(c.glass, c.line, c.shadow, stroke = Brutal.thin)
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(cat.subcategories, key = { it.id }) { s ->
                Chip(
                    selected = s.id == active,
                    onClick = { scope.launch { grid.animateScrollToItem(subStart[s.id] ?: 0) } },
                    leading = s.image?.let { url ->
                        { Box(Modifier.size(32.dp).clip(CircleShape).background(androidx.compose.ui.graphics.Color.White).border(1.5.dp, c.line, CircleShape)) { ProductImage(url, "", Modifier.fillMaxSize().padding(3.dp)) } }
                    },
                ) { Text("${s.name} ${s.count}", style = MaterialTheme.typography.labelLarge) }
            }
        }
    }
    // El raíl se queda pegado arriba cuando su sitio en la rejilla sale de pantalla.
    val pinned by remember { derivedStateOf { grid.firstVisibleItemIndex >= 2 } }

    Box(Modifier.fillMaxSize()) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(160.dp),
        state = grid,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = Gutter, end = Gutter, bottom = NavClearance),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "hero", span = { GridItemSpan(maxLineSpan) }) {
            Column(
                Modifier
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .brutal(hue, c.line, c.shadow, radius = Brutal.radiusL)
                    .padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 22.dp),
            ) {
                Row(
                    Modifier
                        .heightIn(min = 44.dp)
                        .clip(CircleShape)
                        .background(c.surface)
                        .border(Brutal.thin, c.line, CircleShape)
                        .clickable(onClickLabel = "Volver", onClick = onBack)
                        .padding(start = 8.dp, end = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = c.ink, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(section.name, style = MaterialTheme.typography.labelLarge, color = c.ink)
                }
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Column(Modifier.weight(1f)) {
                        Text(cat.name, style = MaterialTheme.typography.displayMedium, color = onHue, modifier = Modifier.semantics { heading() })
                        Text(
                            "${cat.count} productos" + (cheapest?.let { " · desde ${money(it.price)}" } ?: ""),
                            style = MaterialTheme.typography.labelLarge, color = onHue, modifier = Modifier.padding(top = 10.dp),
                        )
                    }
                    Stickers(cat.images.take(2), size = 60.dp)
                }
            }
        }
        item(key = "rail", span = { GridItemSpan(maxLineSpan) }) {
            if (cat.subcategories.size > 1) SubRail() else Spacer(Modifier.height(1.dp))
        }
        entries.forEach { e ->
            when (e) {
                is GridEntry.Header -> item(key = "sub-${e.id}", span = { GridItemSpan(maxLineSpan) }) {
                    Text(e.name, style = MaterialTheme.typography.headlineMedium, color = c.ink, modifier = Modifier.padding(top = 18.dp).semantics { heading() })
                }
                is GridEntry.Group -> item(key = "g-${e.key}", span = { GridItemSpan(maxLineSpan) }) {
                    Text(e.name.uppercase(), style = MaterialTheme.typography.labelMedium, color = c.ink2, modifier = Modifier.padding(top = 6.dp))
                }
                is GridEntry.Item -> item(key = e.p.id) {
                    ProductTag(e.p, basket[e.p.id] ?: 0, e.emoji, onQty = { onQty(e.p.id, it) }, onOpen = { onProduct(e.p) })
                }
            }
        }
    }
    if (pinned && cat.subcategories.size > 1) {
        Box(Modifier.windowInsetsPadding(WindowInsets.statusBars).padding(horizontal = Gutter)) { SubRail() }
    }
    }
}

private sealed interface GridEntry {
    data class Header(val id: Int, val name: String) : GridEntry
    data class Group(val key: String, val name: String) : GridEntry
    data class Item(val p: Product, val emoji: String) : GridEntry
}

// ───────────── Búsqueda, bajadas y novedades ─────────────

fun normalize(s: String): String = Normalizer.normalize(s.lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{M}"), "")

fun search(idx: CatalogIndex, query: String): List<Product> {
    val tokens = normalize(query.trim()).split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (tokens.isEmpty()) return emptyList()
    return idx.catalog.products.mapNotNull { p ->
        val n = normalize(p.name)
        val g = normalize(p.group)
        if (!tokens.all { n.contains(it) || g.contains(it) }) return@mapNotNull null
        var score = 0.0
        if (n.startsWith(tokens[0])) score += 3
        if (Regex("\\b${Regex.escape(tokens[0])}").containsMatchIn(n)) score += 2
        if (tokens.any { g.contains(it) }) score += 2
        score -= n.length / 200.0
        p to score
    }.sortedByDescending { it.second }.map { it.first }
}

private enum class Sort(val label: String) { Relevance("Relevancia"), Price("Más barato"), Unit("Mejor €/kg") }

@Composable
fun ResultsScreen(
    idx: CatalogIndex,
    route: Route,
    basket: Map<String, Int>,
    onQuery: (String) -> Unit,
    onBack: () -> Unit,
    onProduct: (Product) -> Unit,
    onQty: (String, Int) -> Unit,
) {
    val c = LocalColors.current
    val query = (route as? Route.Search)?.query ?: ""
    var section by rememberSaveable { mutableStateOf<String?>(null) }
    var sort by rememberSaveable { mutableStateOf(Sort.Relevance) }
    var limit by rememberSaveable { mutableIntStateOf(60) }

    val base = remember(idx, route) {
        when (route) {
            is Route.Deals -> if (route.newOnes) idx.newProducts else idx.deals
            else -> if (query.trim().length >= 2) search(idx, query) else emptyList()
        }
    }
    val list = remember(base, section, sort) {
        val f = section?.let { s -> base.filter { it.section == s } } ?: base
        when (sort) {
            Sort.Relevance -> f
            Sort.Price -> f.sortedBy { it.price }
            Sort.Unit -> f.sortedBy { it.refPrice ?: Double.MAX_VALUE }
        }
    }
    val counts = remember(base) { base.groupingBy { it.section }.eachCount() }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(160.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = Gutter, end = Gutter, bottom = NavClearance),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(Modifier.windowInsetsPadding(WindowInsets.statusBars).padding(top = 8.dp)) {
                if (route is Route.Search) {
                    SearchField(query, onQuery, autoFocus = query.isEmpty(), onBack = onBack)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(48.dp).clip(CircleShape).clickable(onClickLabel = "Volver", onClick = onBack), contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Volver", tint = c.ink)
                        }
                        Text(
                            if ((route as Route.Deals).newOnes) "Novedades" else "Han bajado",
                            style = MaterialTheme.typography.headlineLarge, color = c.ink, modifier = Modifier.weight(1f).semantics { heading() },
                        )
                        Stamp("${list.size}")
                    }
                }
            }
        }
        if (base.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item { Chip(section == null, { section = null }) { Text("Todo", style = MaterialTheme.typography.labelLarge) } }
                        items(idx.catalog.sections.filter { counts[it.id] != null }, key = { it.id }) { s ->
                            Chip(section == s.id, { section = if (section == s.id) null else s.id }, leading = { HueDot(hueColor(s.hue)) }) {
                                Text("${s.name} ${counts[s.id]}", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(Sort.entries) { s ->
                            Chip(sort == s, { sort = s }) {
                                Text(if (s == Sort.Relevance && route is Route.Deals) "Mayor bajada" else s.label, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }
        if (list.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                when {
                    route is Route.Search && query.trim().length < 2 -> Text("Escribe al menos dos letras.", color = c.ink2, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 12.dp))
                    route is Route.Search -> EmptyState(Icons.Rounded.SearchOff, "Nada con ese nombre", "Prueba con otra palabra, como «yogur» o «tomate».")
                    else -> EmptyState(Icons.Rounded.SearchOff, "Nada por aquí hoy", "Vuelve mañana: los precios se actualizan cada día.")
                }
            }
        }
        items(list.take(limit), key = { it.id }) { p ->
            ProductTag(p, basket[p.id] ?: 0, idx.subcategories[p.subcategory]?.emoji ?: "🛒", onQty = { onQty(p.id, it) }, onOpen = { onProduct(p) })
        }
        if (list.size > limit) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                BrutalButton("Ver más (${list.size - limit})", { limit += 60 }, Modifier.fillMaxWidth().padding(top = 8.dp))
            }
        }
    }
}
