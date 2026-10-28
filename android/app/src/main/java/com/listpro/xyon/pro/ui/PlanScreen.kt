package com.listpro.xyon.pro.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.RestaurantMenu
import androidx.compose.material.icons.rounded.ShoppingBasket
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.listpro.xyon.pro.data.CatalogIndex
import com.listpro.xyon.pro.data.MealPlan
import com.listpro.xyon.pro.data.PlanPrefs
import com.listpro.xyon.pro.data.PlanRecipe
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

private val DIETS = linkedMapOf(
    "omnivora" to "De todo", "vegetariana" to "Vegetariana", "vegana" to "Vegana", "pescetariana" to "Pescetariana",
    "sin_gluten" to "Sin gluten", "sin_lactosa" to "Sin lactosa", "alta_proteina" to "Alta en proteína",
    "baja_carbohidratos" to "Baja en hidratos", "ligera" to "Ligera", "halal" to "Halal",
)
private val APPLIANCES = linkedMapOf(
    "fuego" to "Fuego", "horno" to "Horno", "microondas" to "Micro", "freidora_aire" to "Freidora de aire",
    "olla_rapida" to "Olla rápida", "robot" to "Robot", "sandwichera" to "Plancha", "batidora" to "Batidora",
)
private val COOKING = listOf(
    "Mirando los precios de hoy…", "Cuadrando tu presupuesto…", "Eligiendo cenas que te gusten…",
    "Reaprovechando ingredientes…", "Calculando la nutrición…", "Montando tu lista de la compra…",
)

private fun fmt1(v: Double) = String.format(Locale.forLanguageTag("es-ES"), "%.1f", v)

@Composable
fun PlanScreen(vm: AppViewModel, idx: CatalogIndex?) {
    val state by vm.state.collectAsStateWithLifecycle()
    val plan = state.plan
    var editing by rememberSaveable { mutableStateOf(false) }
    var busy by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val enabled by produceState(true) { value = runCatching { vm.repo.api.plannerOptions().enabled }.getOrDefault(true) }
    val scope = rememberCoroutineScope()
    val haptics = rememberHaptics()

    val run: (PlanPrefs) -> Unit = { prefs ->
        busy = true
        error = null
        scope.launch {
            try {
                vm.repo.generatePlan(prefs)
                haptics.success()
                editing = false
            } catch (e: Exception) {
                haptics.warn()
                error = e.message ?: "No se ha podido crear el plan. Prueba otra vez."
            } finally {
                busy = false
            }
        }
    }

    when {
        busy -> Cooking()
        plan == null || editing -> PrefsForm(
            initial = state.list.planPrefs ?: plan?.prefs ?: PlanPrefs(),
            enabled = enabled,
            error = error,
            onSubmit = run,
            onCancel = if (plan != null) ({ editing = false }) else null,
        )
        else -> PlanView(
            plan = plan,
            idx = idx,
            onRedo = { run(plan.prefs) },
            onEdit = { editing = true },
            onAdd = { items -> vm.repo.addToBasket(items) },
            onClear = { vm.repo.clearPlan() },
            toast = vm.repo::toast,
        )
    }
}

@Composable
private fun Title(onAction: (() -> Unit)?, actionLabel: String) {
    val c = LocalColors.current
    Row(
        Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars).padding(top = 16.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        IconBadge(Icons.Rounded.RestaurantMenu, c.green, size = 44.dp)
        Text("Tus cenas", style = MaterialTheme.typography.displayMedium.copy(fontSize = 34.sp), color = c.ink, modifier = Modifier.weight(1f).semantics { heading() })
        onAction?.let { AppButton(actionLabel, it, leading = { Icon(Icons.Rounded.Tune, null, Modifier.size(18.dp)) }) }
    }
}

// ───────────── Preferencias ─────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PrefsForm(initial: PlanPrefs, enabled: Boolean, error: String?, onSubmit: (PlanPrefs) -> Unit, onCancel: (() -> Unit)?) {
    val c = LocalColors.current
    val haptics = rememberHaptics()
    var p by remember(initial) { mutableStateOf(initial) }
    fun toggle(list: List<String>, id: String, exclusive: String, fallback: String): List<String> {
        haptics.tick()
        var next = if (id in list) list - id else list + id
        if (exclusive.isNotEmpty()) next = if (id == exclusive) listOf(exclusive) else next - exclusive
        return next.ifEmpty { listOf(fallback) }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = Gutter).padding(bottom = NavClearance),
    ) {
        Title(onCancel, "Cancelar")
        Text(
            "Te planificamos las cenas de la semana con productos y precios reales de tu súper, a tu gusto y con lo que tienes en la cocina.",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal), color = c.ink2, modifier = Modifier.padding(top = 4.dp),
        )

        FormPanel(Icons.Rounded.Wallet, "Presupuesto de la semana") {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                val shown by animateFloatAsState(p.budget.toFloat(), tween(220), label = "budget")
                Price(shown.toDouble(), size = 44, color = c.greenInk)
                Text("para toda la compra", style = MaterialTheme.typography.bodyMedium, color = c.ink2, modifier = Modifier.padding(bottom = 6.dp))
            }
            Slider(
                value = p.budget.toFloat(),
                onValueChange = { v -> val b = (v / 5).toInt() * 5; if (b != p.budget) { haptics.tick(); p = p.copy(budget = b) } },
                valueRange = 15f..150f,
                colors = SliderDefaults.colors(thumbColor = c.green, activeTrackColor = c.green, inactiveTrackColor = c.surface2),
                modifier = Modifier.padding(vertical = 6.dp),
            )
            Counter("Personas", p.people, 1, 8) { p = p.copy(people = it) }
            Spacer(Modifier.height(10.dp))
            Counter("Cenas", p.nights, 1, 7) { p = p.copy(nights = it) }
        }

        FormPanel(Icons.Rounded.Storefront, "Tu súper") {
            Chip(true, {}) { Text("Mercadona", style = MaterialTheme.typography.labelLarge) }
            Text("Por ahora trabajamos con el catálogo de Mercadona.", style = MaterialTheme.typography.bodySmall, color = c.ink2, modifier = Modifier.padding(top = 8.dp))
        }

        FormPanel(Icons.Rounded.AutoAwesome, "Tu dieta") {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DIETS.forEach { (id, label) ->
                    Chip(id in p.diets, { p = p.copy(diets = toggle(p.diets, id, "omnivora", "omnivora")) }) { Text(label, style = MaterialTheme.typography.labelLarge) }
                }
            }
            Field("Alergias o intolerancias", "frutos secos, marisco…", p.allergies) { p = p.copy(allergies = it) }
        }

        FormPanel(Icons.Rounded.LocalFireDepartment, "Tu cocina") {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                APPLIANCES.forEach { (id, label) ->
                    Chip(id in p.appliances, { p = p.copy(appliances = toggle(p.appliances, id, "", "fuego")) }) { Text(label, style = MaterialTheme.typography.labelLarge) }
                }
            }
            Text("Tiempo máximo por cena", style = MaterialTheme.typography.labelLarge, color = c.ink, modifier = Modifier.padding(top = 18.dp, bottom = 8.dp))
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(c.surface2).padding(4.dp)) {
                listOf(15, 30, 45, 60).forEach { m ->
                    val on = p.maxMinutes == m
                    Box(
                        Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(12.dp)).background(if (on) c.surface else Color.Transparent)
                            .clickable(role = Role.RadioButton) { haptics.tick(); p = p.copy(maxMinutes = m) },
                        contentAlignment = Alignment.Center,
                    ) { Text("$m min", style = MaterialTheme.typography.labelLarge, color = if (on) c.ink else c.ink2) }
                }
            }
        }

        FormPanel(Icons.Rounded.RestaurantMenu, "Tus gustos") {
            Field("Te gusta", "pasta, platos de cuchara, cocina asiática…", p.likes) { p = p.copy(likes = it) }
            Field("No te gusta", "coliflor, picante…", p.dislikes) { p = p.copy(dislikes = it) }
            Field("Ya tienes en casa", "arroz, cebollas, medio paquete de pasta…", p.pantry) { p = p.copy(pantry = it) }
        }

        error?.let { PlanNotice(it, up = true) }
        if (!enabled) PlanNotice("El planificador no está disponible ahora mismo. Vuelve a intentarlo más tarde.", up = false)
        AppButton(
            "Crear mi plan", { onSubmit(p) },
            Modifier.fillMaxWidth().padding(top = 22.dp).heightIn(min = 58.dp), primary = true, enabled = enabled,
            leading = { Icon(Icons.Rounded.AutoAwesome, null, Modifier.size(20.dp)) },
        )
        Text(
            "Tarda entre 20 y 60 segundos. No es un servicio de reparto: te ayudamos a organizar la compra y las cenas.",
            style = MaterialTheme.typography.bodySmall, color = c.ink2, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
        )
    }
}

@Composable
private fun FormPanel(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, content: @Composable () -> Unit) {
    val c = LocalColors.current
    Panel {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(bottom = 12.dp)) {
                Icon(icon, null, tint = c.greenInk, modifier = Modifier.size(20.dp))
                Text(title, style = MaterialTheme.typography.titleLarge, color = c.ink)
            }
            content()
        }
    }
}

@Composable
private fun Counter(label: String, value: Int, min: Int, max: Int, onChange: (Int) -> Unit) {
    val c = LocalColors.current
    val haptics = rememberHaptics()
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(c.surface2).padding(start = 14.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = c.ink, modifier = Modifier.weight(1f))
        Row(Modifier.height(44.dp).clip(CircleShape).background(c.greenSoft), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clickable(enabled = value > min, role = Role.Button) { haptics.remove(); onChange(value - 1) }, contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Remove, "Menos", tint = c.greenInk.copy(alpha = if (value > min) 1f else 0.35f))
            }
            AnimatedContent(value, transitionSpec = { (slideInVertically { -it / 2 } + fadeIn()) togetherWith fadeOut() }, label = "counter") { n ->
                Text("$n", style = TextStyle(fontFamily = Numeric, fontWeight = FontWeight.Bold, fontSize = 17.sp), color = c.ink, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
            }
            Box(Modifier.size(48.dp).clickable(enabled = value < max, role = Role.Button) { haptics.add(); onChange(value + 1) }, contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Add, "Más", tint = c.greenInk.copy(alpha = if (value < max) 1f else 0.35f))
            }
        }
    }
}

@Composable
private fun Field(label: String, placeholder: String, value: String, onChange: (String) -> Unit) {
    val c = LocalColors.current
    Text(label, style = MaterialTheme.typography.labelLarge, color = c.ink, modifier = Modifier.padding(top = 14.dp, bottom = 6.dp))
    BasicTextField(
        value = value,
        onValueChange = { if (it.length <= 200) onChange(it) },
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = c.ink, fontWeight = FontWeight.Medium),
        cursorBrush = SolidColor(c.green),
        decorationBox = { inner -> Box { if (value.isEmpty()) Text(placeholder, color = c.ink3, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal)); inner() } },
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).card(c.surface, c.lineStrong, 16.dp).padding(horizontal = 16.dp, vertical = 13.dp),
    )
}

@Composable
private fun PlanNotice(text: String, up: Boolean) {
    val c = LocalColors.current
    val tint = if (up) c.up else c.ink2
    Row(
        Modifier.padding(top = 14.dp).fillMaxWidth().card(tint.copy(alpha = 0.12f).compositeOverSurface(c.surface), tint.copy(alpha = 0.35f), 16.dp).padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(if (up) Icons.Rounded.Warning else Icons.Rounded.AutoAwesome, null, tint = if (up) tint else c.greenInk, modifier = Modifier.size(20.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = c.ink)
    }
}

private fun Color.compositeOverSurface(bg: Color) = this.compositeOver(bg)

// ───────────── Mientras cocina ─────────────

@Composable
private fun Cooking() {
    val c = LocalColors.current
    var i by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { while (true) { delay(2600); i = (i + 1) % COOKING.size } }
    val bob = rememberInfiniteTransition(label = "bob")
    val y by bob.animateFloat(0f, -14f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "y")
    val rot by bob.animateFloat(-4f, 4f, infiniteRepeatable(tween(1100), RepeatMode.Reverse), label = "rot")
    Column(
        Modifier.fillMaxSize().padding(horizontal = Gutter),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier.graphicsLayer { translationY = y; rotationZ = rot }.size(104.dp)
                .shadow(16.dp, RoundedCornerShape(32.dp), ambientColor = c.greenHi, spotColor = c.greenHi)
                .clip(RoundedCornerShape(32.dp)).background(c.gradient),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Rounded.RestaurantMenu, null, tint = Color.White, modifier = Modifier.size(48.dp)) }
        Text("Preparando tu plan", style = MaterialTheme.typography.titleLarge, color = c.ink, modifier = Modifier.padding(top = 28.dp))
        AnimatedContent(i, transitionSpec = { (slideInVertically { it / 2 } + fadeIn()) togetherWith fadeOut() }, label = "msg") { k ->
            Text(COOKING[k], style = MaterialTheme.typography.bodyLarge, color = c.ink2, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

// ───────────── Plan ─────────────

@Composable
private fun PlanView(
    plan: MealPlan,
    idx: CatalogIndex?,
    onRedo: () -> Unit,
    onEdit: () -> Unit,
    onAdd: (List<Pair<String, Int>>) -> Int,
    onClear: () -> Unit,
    toast: (String) -> Unit,
) {
    val c = LocalColors.current
    val haptics = rememberHaptics()
    val t = plan.totals
    var open by rememberSaveable { mutableStateOf<Int?>(null) }
    val shopping = plan.shopping.filter { idx == null || idx.products.containsKey(it.productId) }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = Gutter, end = Gutter, bottom = NavClearance)) {
        item { Title(onEdit, "Ajustar") }
        item {
            Column(
                Modifier.padding(top = 12.dp).fillMaxWidth()
                    .shadow(14.dp, RoundedCornerShape(Ui.radiusL), ambientColor = c.greenHi, spotColor = c.greenHi)
                    .clip(RoundedCornerShape(Ui.radiusL)).background(c.gradient).padding(22.dp),
            ) {
                Text(plan.title, style = MaterialTheme.typography.titleMedium, color = Color.White)
                val shown by animateFloatAsState(t.shopping.toFloat(), tween(700, easing = EaseOutExpo), label = "plan-total")
                Price(shown.toDouble(), Modifier.padding(vertical = 8.dp), size = 56, color = Color.White)
                val pct by animateFloatAsState((t.shopping / t.budget).toFloat().coerceIn(0f, 1f), tween(900, easing = EaseOutExpo), label = "pct")
                Box(Modifier.fillMaxWidth().height(10.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.25f))) {
                    Box(Modifier.fillMaxHeight().fillMaxWidth(pct).clip(CircleShape).background(if (t.overBudget) Color(0xFFFFD6D6) else Color.White))
                }
                Text(
                    if (t.overBudget) "Te pasas ${money(t.shopping - t.budget)} de tus ${money(t.budget)}" else "De tus ${money(t.budget)}: te sobran ${money(t.budget - t.shopping)}",
                    style = MaterialTheme.typography.labelMedium, color = Color.White, modifier = Modifier.padding(top = 8.dp),
                )
                Row(Modifier.padding(top = 14.dp).fillMaxWidth()) {
                    HeroFigure(money(t.perServing), "por ración", Modifier.weight(1f))
                    HeroFigure("${t.kcalAvg}", "kcal de media", Modifier.weight(1f))
                    HeroFigure("${t.proteinAvg} g", "proteína", Modifier.weight(1f))
                }
                Row(Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        Modifier.weight(1f).heightIn(min = 48.dp).clip(RoundedCornerShape(16.dp)).background(Color.White)
                            .clickable(role = Role.Button) { haptics.success(); toast("${onAdd(shopping.map { it.productId to it.packs })} productos en la cesta") }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    ) {
                        Icon(Icons.Rounded.ShoppingBasket, null, tint = Color(0xFF0E8A3B), modifier = Modifier.size(18.dp))
                        Text("Añadir a la cesta", style = MaterialTheme.typography.labelLarge, color = Color(0xFF0E8A3B))
                    }
                    Row(
                        Modifier.heightIn(min = 48.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.16f))
                            .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                            .clickable(role = Role.Button) { haptics.tap(); onRedo() }.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(Icons.Rounded.Refresh, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Text("Otro", style = MaterialTheme.typography.labelLarge, color = Color.White)
                    }
                }
            }
        }
        if (plan.summary.isNotBlank()) item {
            Text(plan.summary, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal), color = c.ink2, modifier = Modifier.padding(top = 14.dp))
        }
        items(plan.warnings.size) { i -> PlanNotice(plan.warnings[i], up = true) }

        item { Text("La semana", style = MaterialTheme.typography.headlineMedium, color = c.ink, modifier = Modifier.padding(top = 28.dp, bottom = 4.dp).semantics { heading() }) }
        itemsIndexed(plan.recipes, key = { _, r -> r.day }) { i, r -> DinnerCard(r, i) { haptics.tap(); open = i } }

        item {
            Row(Modifier.padding(top = 30.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Lista de la compra", style = MaterialTheme.typography.headlineMedium, color = c.ink, modifier = Modifier.weight(1f).semantics { heading() })
                Stamp("${shopping.size} productos")
            }
        }
        item {
            Column(Modifier.fillMaxWidth().raised(Ui.radiusL).padding(horizontal = 16.dp, vertical = 6.dp)) {
                shopping.forEach { s ->
                    ShopRow(s.image, s.name, "${s.size} · para ${s.usedIn.joinToString { it.take(3) }}", money(s.cost), "${s.packs} × ${money(s.price)}")
                }
                Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total", style = MaterialTheme.typography.titleMedium, color = c.ink)
                    Text(money(t.shopping), style = TextStyle(fontFamily = Numeric, fontWeight = FontWeight.Bold, fontSize = 18.sp), color = c.greenInk)
                }
            }
        }
        item {
            Text(
                "Precios reales de hoy. La nutrición es una estimación hecha por IA: si tienes alergias, revisa siempre la etiqueta del producto.",
                style = MaterialTheme.typography.bodySmall, color = c.ink2, modifier = Modifier.padding(top = 12.dp),
            )
            AppButton("Borrar el plan", { onClear(); toast("Plan borrado") }, Modifier.fillMaxWidth().padding(top = 18.dp), danger = true)
        }
    }

    open?.let { i -> plan.recipes.getOrNull(i)?.let { r -> RecipeSheet(r, onAdd, toast) { open = null } } }
}

@Composable
private fun HeroFigure(value: String, label: String, modifier: Modifier) {
    Column(modifier) {
        Text(value, style = TextStyle(fontFamily = Numeric, fontWeight = FontWeight.Bold, fontSize = 17.sp), color = Color.White)
        Text(label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium), color = Color.White.copy(alpha = 0.85f))
    }
}

@Composable
private fun DinnerCard(r: PlanRecipe, index: Int, onClick: () -> Unit) {
    val c = LocalColors.current
    val source = remember { MutableInteractionSource() }
    Row(
        Modifier.padding(top = 12.dp).appear(index).pressScale(source).fillMaxWidth().raised(Ui.radiusL)
            .clickable(source, indication = null, role = Role.Button, onClickLabel = "Ver receta", onClick = onClick).padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(Modifier.size(96.dp, 108.dp).clip(RoundedCornerShape(18.dp)).background(c.greenSoft)) {
            val pics = r.ingredients.mapNotNull { it.image }.take(3)
            val spots = listOf(Triple(6, 8, -6f), Triple(38, 26, 6f), Triple(18, 50, 2f))
            pics.forEachIndexed { k, url ->
                val (x, y, a) = spots[k]
                Box(Modifier.offset(x.dp, y.dp).size(52.dp).rotate(a).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(4.dp)) {
                    ProductImage(url, "", Modifier.fillMaxSize())
                }
            }
        }
        Column(Modifier.weight(1f)) {
            Text(r.day.uppercase(), style = TextStyle(fontFamily = Numeric, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.sp), color = c.greenInk)
            Text(r.name, style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp, lineHeight = 21.sp), color = c.ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(r.description, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal), color = c.ink2, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 2.dp))
            Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Fact("${r.minutes} min")
                Fact("${r.nutrition.kcal} kcal")
                Fact("${money(r.costPerServing)}/rac.")
            }
        }
    }
}

@Composable
private fun Fact(text: String) {
    val c = LocalColors.current
    Text(
        text, style = TextStyle(fontFamily = Numeric, fontWeight = FontWeight.SemiBold, fontSize = 11.sp), color = c.ink2, maxLines = 1,
        modifier = Modifier.clip(CircleShape).background(c.surface2).padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

@Composable
private fun ShopRow(image: String?, name: String, sub: String, right: String, rightSub: String?) {
    val c = LocalColors.current
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(3.dp)) { ProductImage(image, "🛒", Modifier.fillMaxSize()) }
        Column(Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.bodyMedium, color = c.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(sub, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal), color = c.ink2, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(right, style = TextStyle(fontFamily = Numeric, fontWeight = FontWeight.SemiBold, fontSize = 14.sp), color = c.ink)
            rightSub?.let { Text(it, style = TextStyle(fontFamily = Numeric, fontSize = 11.sp), color = c.ink3) }
        }
    }
}

// ───────────── Receta ─────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun RecipeSheet(r: PlanRecipe, onAdd: (List<Pair<String, Int>>) -> Int, toast: (String) -> Unit, onDismiss: () -> Unit) {
    val c = LocalColors.current
    val haptics = rememberHaptics()
    val n = r.nutrition
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = c.surface,
        shape = RoundedCornerShape(topStart = Ui.radiusL, topEnd = Ui.radiusL),
    ) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 28.dp)) {
            Stamp("${r.day} · ${r.servings} ${if (r.servings == 1) "ración" else "raciones"}")
            Text(r.name, style = MaterialTheme.typography.headlineLarge.copy(fontSize = 28.sp, lineHeight = 30.sp), color = c.ink, modifier = Modifier.padding(top = 12.dp))
            Text(r.description, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal), color = c.ink2, modifier = Modifier.padding(top = 6.dp))
            FlowRow(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Fact("${r.minutes} min"); Fact(r.difficulty); Fact("${money(r.costPerServing)}/ración")
                r.appliances.forEach { Fact(APPLIANCES[it] ?: it) }
            }

            Text("Nutrición por ración", style = MaterialTheme.typography.titleLarge, color = c.ink, modifier = Modifier.padding(top = 22.dp, bottom = 10.dp))
            val cells = listOf("${n.kcal}" to "kcal", "${n.protein} g" to "proteínas", "${n.carbs} g" to "hidratos", "${n.fat} g" to "grasas", "${n.fiber} g" to "fibra", "${fmt1(n.salt)} g" to "sal")
            cells.chunked(3).forEachIndexed { row, chunk ->
                Row(Modifier.padding(top = if (row == 0) 0.dp else 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    chunk.forEachIndexed { k, (v, l) ->
                        Column(Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(if (row == 0 && k == 0) c.greenSoft else c.surface2).padding(horizontal = 12.dp, vertical = 10.dp)) {
                            Text(v, style = TextStyle(fontFamily = Numeric, fontWeight = FontWeight.Bold, fontSize = 17.sp), color = c.ink)
                            Text(l, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium), color = c.ink3)
                        }
                    }
                }
            }
            Text("Estimación orientativa.", style = MaterialTheme.typography.bodySmall, color = c.ink2, modifier = Modifier.padding(top = 6.dp))

            Text("Ingredientes", style = MaterialTheme.typography.titleLarge, color = c.ink, modifier = Modifier.padding(top = 22.dp, bottom = 4.dp))
            r.ingredients.forEach { i ->
                ShopRow(i.image, i.name, i.amount + if (i.fromPantry) " · lo tienes en casa" else "", i.price?.let { money(it) } ?: "", null)
            }
            Text("Además: aceite de oliva, sal y pimienta.", style = MaterialTheme.typography.bodySmall, color = c.ink2, modifier = Modifier.padding(top = 4.dp))

            Text("Paso a paso", style = MaterialTheme.typography.titleLarge, color = c.ink, modifier = Modifier.padding(top = 22.dp, bottom = 6.dp))
            r.steps.forEachIndexed { k, step ->
                Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(28.dp).clip(CircleShape).background(c.gradient), contentAlignment = Alignment.Center) {
                        Text("${k + 1}", style = TextStyle(fontFamily = Numeric, fontWeight = FontWeight.Bold, fontSize = 13.sp), color = Color.White)
                    }
                    Text(step, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal), color = c.ink, modifier = Modifier.weight(1f).padding(top = 2.dp))
                }
            }
            if (r.tip.isNotBlank()) PlanNotice(r.tip, up = false)

            AppButton(
                "Añadir ingredientes a la cesta",
                {
                    haptics.success()
                    val items = r.ingredients.mapNotNull { i -> i.productId?.let { it to maxOf(1, kotlin.math.ceil(i.fraction - 0.05).toInt()) } }
                    toast("${onAdd(items)} ingredientes en la cesta")
                },
                Modifier.fillMaxWidth().padding(top = 20.dp), primary = true,
                leading = { Icon(Icons.Rounded.ShoppingBasket, null, Modifier.size(18.dp)) },
            )
        }
    }
}

