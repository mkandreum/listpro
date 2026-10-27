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
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.BakeryDining
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ChildCare
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.WineBar
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
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

/** Icono de cada pasillo: el nombre llega de la API para que web y Android usen el mismo. */
fun sectionIcon(name: String): ImageVector = when (name) {
    "leaf" -> Icons.Rounded.Eco
    "milk" -> Icons.Rounded.LocalDrink
    "wheat" -> Icons.Rounded.BakeryDining
    "snowflake" -> Icons.Rounded.AcUnit
    "wine" -> Icons.Rounded.WineBar
    "sparkles" -> Icons.Rounded.Spa
    "spray" -> Icons.Rounded.CleaningServices
    "baby" -> Icons.Rounded.ChildCare
    "paw" -> Icons.Rounded.Pets
    else -> Icons.Rounded.ShoppingBag
}

@Composable
fun TopBar(updatedAt: String?) {
    val c = LocalColors.current
    Row(
        Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier.size(50.dp).shadow(10.dp, RoundedCornerShape(16.dp), ambientColor = c.greenHi, spotColor = c.greenHi)
                    .clip(RoundedCornerShape(16.dp)).background(c.gradient),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Rounded.ShoppingBasket, null, tint = Color.White, modifier = Modifier.size(28.dp)) }
            Column {
                Text(
                    buildAnnotatedString { append("List"); withStyle(SpanStyle(color = c.greenHi)) { append("Pro") } },
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 30.sp, lineHeight = 30.sp), color = c.ink,
                    modifier = Modifier.semantics { heading() },
                )
                Text("Tu súper, en un clic", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = c.ink2)
            }
        }
        if (updatedAt != null) Stamp(freshness(updatedAt), dot = c.greenHi, trailing = Icons.Rounded.Schedule)
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
            .height(60.dp)
            .raised(22.dp, c.glass)
            .padding(start = if (onBack != null) 4.dp else 18.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (onBack != null) {
            Box(Modifier.size(48.dp).clip(CircleShape).clickable(onClickLabel = "Volver", onClick = onBack), contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Volver", tint = c.ink)
            }
        } else {
            Icon(Icons.Rounded.Search, null, tint = c.ink2)
        }
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty()) Text("Busca leche, aguacate, detergente…", color = c.ink3, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), maxLines = 1, overflow = TextOverflow.Ellipsis)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = c.ink, fontWeight = FontWeight.Medium),
                cursorBrush = SolidColor(c.green),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier.fillMaxWidth().focusRequester(focus),
            )
        }
        if (value.isNotEmpty()) {
            Box(Modifier.size(48.dp).clip(CircleShape).clickable(onClickLabel = "Borrar búsqueda") { onValueChange("") }, contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Close, "Borrar búsqueda", tint = c.ink2)
            }
        }
    }
}

@Composable
private fun SearchLauncher(onClick: () -> Unit, onFilters: () -> Unit) {
    val c = LocalColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .height(60.dp)
            .raised(22.dp, c.glass)
            .clickable(role = Role.Button, onClickLabel = "Buscar productos", onClick = onClick)
            .padding(start = 18.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(Icons.Rounded.Search, null, tint = c.ink2)
        Text("Busca leche, aguacate, detergente…", color = c.ink3, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).clickable(onClickLabel = "Ver bajadas con filtros", onClick = onFilters), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Tune, "Filtros", tint = c.ink2)
        }
    }
}

/** Acceso rápido: icono en su baldosa de color, título y cifra. */
@Composable
private fun StatCard(icon: ImageVector, tint: Color, title: String, value: String, valueColor: Color, onClick: () -> Unit) {
    val c = LocalColors.current
    Row(
        Modifier
            .width(164.dp)
            .heightIn(min = 72.dp)
            .raised(Ui.radius)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 12.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(tint.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(24.dp))
        }
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = c.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(value, style = TextStyle(fontFamily = Numeric, fontWeight = FontWeight.Bold, fontSize = 18.sp), color = valueColor)
        }
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
                SearchLauncher(onClick = { onOpen(Route.Search("")) }, onFilters = { onOpen(Route.Deals(false)) })
            }
        }
        if (idx == null) {
            item(key = "state") {
                Box(Modifier.padding(horizontal = Gutter)) {
                    if (error) {
                        EmptyState(Icons.Rounded.WifiOff, "No llegan los precios", "Revisa la conexión. En cuanto vuelva, cargamos el catálogo.") {
                            AppButton("Reintentar", onRetry, primary = true)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            repeat(3) { Box(Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(Ui.radius)).background(c.surface2)) }
                        }
                    }
                }
            }
            return@LazyColumn
        }

        item(key = "stats") {
            LazyRow(contentPadding = PaddingValues(start = Gutter, end = Gutter, bottom = 6.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (idx.deals.isNotEmpty()) item {
                    StatCard(Icons.AutoMirrored.Rounded.TrendingDown, c.green, "Bajadas", "${idx.deals.size}", c.greenInk) { onOpen(Route.Deals(false)) }
                }
                if (idx.newProducts.isNotEmpty()) item {
                    StatCard(Icons.Rounded.AutoAwesome, c.violet, "Novedades", "${idx.newProducts.size}", c.violet) { onOpen(Route.Deals(true)) }
                }
                items(idx.catalog.sections.size) { i ->
                    val s = idx.catalog.sections[i]
                    // salto al pasillo: cabecera, buscador, accesos, bajadas y luego un item por pasillo
                    StatCard(sectionIcon(s.icon), hueColor(s.hue), s.name, "${s.count}", c.ink2) { scope.launch { list.animateScrollToItem(4 + i) } }
                }
            }
        }

        item(key = "deals") {
            if (idx.deals.isNotEmpty()) {
                Column {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = Gutter).padding(top = 24.dp, bottom = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        IconBadge(Icons.Rounded.ArrowDownward, c.green, size = 44.dp)
                        Text("Han bajado", style = MaterialTheme.typography.headlineLarge, color = c.ink, maxLines = 1, modifier = Modifier.weight(1f).semantics { heading() })
                        AppButton("Ver ${idx.deals.size}", { onOpen(Route.Deals(false)) }, ghost = true, leading = null)
                    }
                    LazyRow(contentPadding = PaddingValues(start = Gutter, end = Gutter, bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(idx.deals.take(14), key = { it.id }) { p ->
                            ProductTag(
                                p, basket[p.id] ?: 0, idx.subcategories[p.subcategory]?.emoji ?: "🛒",
                                onQty = { onQty(p.id, it) }, onOpen = { onProduct(p) },
                                modifier = Modifier.width(186.dp),
                            )
                        }
                    }
                }
            }
        }

        items(idx.catalog.sections, key = { "aisle-${it.id}" }) { s -> Aisle(s, onSection = { s.categories.firstOrNull()?.let { onOpen(Route.CategoryRoute(it.id)) } }) { onOpen(Route.CategoryRoute(it)) } }
    }
}

/** Banner del pasillo con su color, icono, descripción y fotos, y debajo sus categorías. */
@Composable
private fun Aisle(s: Section, onSection: () -> Unit, onCategory: (Int) -> Unit) {
    val hue = hueColor(s.hue)
    Column(Modifier.padding(top = 28.dp)) {
        SectionBanner(s.name, s.blurb, sectionIcon(s.icon), hue, s.images, Modifier.padding(horizontal = Gutter), count = s.count, onCount = onSection)
        LazyRow(contentPadding = PaddingValues(start = Gutter, end = Gutter, top = 10.dp, bottom = 6.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(s.categories, key = { it.id }) { cat -> CategoryTile(cat, hue) { onCategory(cat.id) } }
        }
    }
}

@Composable
fun SectionBanner(
    title: String,
    body: String?,
    icon: ImageVector,
    hue: Color,
    images: List<String>,
    modifier: Modifier = Modifier,
    count: Int? = null,
    onCount: (() -> Unit)? = null,
    top: (@Composable () -> Unit)? = null,
    titleStyle: TextStyle = MaterialTheme.typography.headlineLarge.copy(fontSize = 29.sp, lineHeight = 30.sp),
) {
    val c = LocalColors.current
    val shape = RoundedCornerShape(Ui.radiusL)
    Box(
        modifier
            .fillMaxWidth()
            .heightIn(min = 150.dp)
            .clip(shape)
            .background(Brush.horizontalGradient(listOf(hue.copy(alpha = if (c.isDark) 0.16f else 0.14f).compositeOver(c.surface), hue.copy(alpha = if (c.isDark) 0.34f else 0.30f).compositeOver(c.surface))))
            .border(Ui.hairline, hue.copy(alpha = 0.4f), shape)
            .padding(18.dp),
    ) {
        // Las fotos van ancladas abajo a la derecha para que el título use todo el ancho y no se parta.
        Stickers(images, Modifier.align(Alignment.BottomEnd), size = 54.dp)
        Column(Modifier.fillMaxWidth()) {
            top?.invoke()
            Row(Modifier.padding(end = if (count != null) 72.dp else 0.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconBadge(icon, hue, size = 44.dp)
                Text(title, style = titleStyle, color = c.ink, modifier = Modifier.semantics { heading() })
            }
            body?.let { Text(it, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 20.sp), color = c.ink2, modifier = Modifier.padding(top = 10.dp, end = 118.dp)) }
        }
        if (count != null) {
            Row(
                Modifier.align(Alignment.TopEnd).clip(CircleShape).background(c.glass).border(Ui.hairline, c.line, CircleShape)
                    .then(if (onCount != null) Modifier.clickable(onClick = onCount) else Modifier).padding(start = 12.dp, end = 6.dp, top = 5.dp, bottom = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("$count", style = TextStyle(fontFamily = Numeric, fontWeight = FontWeight.SemiBold, fontSize = 14.sp), color = c.ink)
                Icon(Icons.Rounded.ChevronRight, null, tint = c.ink2, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun CategoryTile(cat: Category, hue: Color, onClick: () -> Unit) {
    val c = LocalColors.current
    Column(
        Modifier
            .width(146.dp)
            .raised(Ui.radius)
            .clickable(role = Role.Button, onClickLabel = "Abrir ${cat.name}", onClick = onClick)
            .padding(8.dp),
    ) {
        Box(
            Modifier.fillMaxWidth().height(86.dp).clip(RoundedCornerShape(14.dp)).background(hue.copy(alpha = 0.14f).compositeOver(c.surface2)),
            contentAlignment = Alignment.BottomCenter,
        ) { Stickers(cat.images, Modifier.padding(bottom = 8.dp), size = 54.dp) }
        Row(Modifier.padding(start = 4.dp, end = 2.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(cat.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = c.ink, maxLines = 2, minLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            Icon(Icons.Rounded.ChevronRight, null, tint = c.ink3, modifier = Modifier.size(18.dp))
        }
        Text("${cat.count} productos", style = TextStyle(fontFamily = Numeric, fontSize = 11.sp), color = c.ink3, modifier = Modifier.padding(start = 4.dp, bottom = 2.dp))
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
                .raised(22.dp, c.glass)
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(cat.subcategories, key = { it.id }) { s ->
                Chip(
                    selected = s.id == active,
                    onClick = { scope.launch { grid.animateScrollToItem(subStart[s.id] ?: 0) } },
                    leading = s.image?.let { url ->
                        { Box(Modifier.size(32.dp).clip(CircleShape).background(Color.White)) { ProductImage(url, "", Modifier.fillMaxSize().padding(3.dp)) } }
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
            SectionBanner(
                title = cat.name,
                body = "${cat.count} productos" + (cheapest?.let { " · desde ${money(it.price)}" } ?: ""),
                icon = sectionIcon(section.icon),
                hue = hue,
                images = cat.images.take(2),
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars).padding(top = 8.dp),
                titleStyle = MaterialTheme.typography.displayMedium.copy(fontSize = 34.sp, lineHeight = 36.sp),
                top = {
                    Row(
                        Modifier
                            .padding(bottom = 16.dp)
                            .heightIn(min = 44.dp)
                            .clip(CircleShape)
                            .background(c.glass)
                            .border(Ui.hairline, c.line, CircleShape)
                            .clickable(onClickLabel = "Volver", onClick = onBack)
                            .padding(start = 8.dp, end = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = c.ink, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(section.name, style = MaterialTheme.typography.labelLarge, color = c.ink)
                    }
                },
            )
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
                AppButton("Ver más (${list.size - limit})", { limit += 60 }, Modifier.fillMaxWidth().padding(top = 8.dp))
            }
        }
    }
}
