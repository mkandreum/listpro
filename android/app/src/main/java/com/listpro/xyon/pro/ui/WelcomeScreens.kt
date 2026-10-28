package com.listpro.xyon.pro.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.RestaurantMenu
import androidx.compose.material.icons.rounded.ShoppingBasket
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.listpro.xyon.pro.data.CatalogIndex
import com.listpro.xyon.pro.data.Product
import kotlinx.coroutines.launch

private val Night = Color(0xFF07120E)
private val Mint = Color(0xFF4ADE80)
private val Butter = Color(0xFFFFD23F)
private val Orange = Color(0xFFFF8A2A)

/** Fotos reales del catálogo para decorar: una por pasillo, mejor si han bajado de precio. */
private fun stickers(idx: CatalogIndex?, n: Int): List<Product> {
    val food = setOf("frescos", "lacteos", "despensa", "bebidas", "congelados")
    val ps = idx?.catalog?.products?.filter { it.image != null && it.section in food } ?: return emptyList()
    val seen = mutableSetOf<String>()
    val deals = ps.filter { (it.previousPrice ?: 0.0) > it.price && seen.add(it.section) }
    val rest = ps.filterIndexed { i, p -> i % 211 == 0 && seen.add(p.section) }
    return (deals + rest + ps.filterIndexed { i, _ -> i % 97 == 0 }).take(n)
}

// ───────────── Presentación ─────────────

private data class Slide(val eyebrow: String, val title: String, val accent: String, val body: String, val hue: Color)

private val SLIDES = listOf(
    Slide("PRECIOS DE HOY", "Tu súper,", "en un clic.", "Más de 4.000 productos de Mercadona con el precio de esta mañana. Ves lo que ha bajado antes de salir de casa.", Color(0xFF22C55E)),
    Slide("CENAS CON IA", "Cenas que", "cuadran.", "Dinos tu presupuesto, tu dieta y lo que tienes en la cocina. Te montamos las cenas de la semana y la lista de la compra.", Orange),
    Slide("EN COMPAÑÍA", "Comprad", "juntos.", "Una cesta para toda la casa: quién pide qué, quién lo ha cogido y cuánto os debéis. Sin hacer cuentas.", Color(0xFF7C5CFF)),
)

@Composable
fun OnboardingScreen(idx: CatalogIndex?, onDone: () -> Unit) {
    val pager = rememberPagerState { SLIDES.size }
    val scope = rememberCoroutineScope()
    val haptics = rememberHaptics()
    val page = pager.currentPage
    val last = page == SLIDES.lastIndex
    val hue by animateColorAsState(SLIDES[page].hue, tween(700), label = "hue")
    val drift by rememberInfiniteTransition(label = "glow").animateFloat(0f, 1f, infiniteRepeatable(tween(8000), RepeatMode.Reverse), label = "drift")
    val pics = remember(idx) { stickers(idx, 3) }

    LaunchedEffect(page) { haptics.tap() }

    Box(Modifier.fillMaxSize().background(Night)) {
        // Resplandor del color de cada paso
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width * (0.5f + drift * 0.08f), size.height * (0.28f + drift * 0.04f))
            drawRect(Brush.radialGradient(listOf(hue.copy(alpha = 0.42f), Color.Transparent), center, size.width * 0.85f))
        }
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars).windowInsetsPadding(WindowInsets.navigationBars)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                BrandMark(38.dp)
                Text(
                    buildAnnotatedString { append("List"); withStyle(SpanStyle(color = Mint)) { append("Pro") } },
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp), color = Color.White, modifier = Modifier.padding(start = 10.dp).weight(1f),
                )
                if (!last) Text(
                    "Saltar", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.08f)).border(Ui.hairline, Color.White.copy(alpha = 0.12f), CircleShape)
                        .clickable(role = Role.Button) { haptics.success(); onDone() }.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
            HorizontalPager(pager, Modifier.weight(1f).fillMaxWidth()) { p ->
                val active = p == page
                Column(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        when (p) {
                            0 -> PricesArt(active, pics)
                            1 -> DinnersArt(active)
                            else -> TogetherArt(active)
                        }
                    }
                    SlideCopy(SLIDES[p], active)
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SLIDES.indices.forEach { i ->
                        val w by animateDpAsState(if (i == page) 34.dp else 10.dp, spring(dampingRatio = 0.7f), label = "dot")
                        Box(
                            Modifier.height(10.dp).width(w).clip(CircleShape)
                                .background(Color.White.copy(alpha = if (i == page) 1f else if (i < page) 0.5f else 0.22f))
                                .clickable(role = Role.Tab) { scope.launch { pager.animateScrollToPage(i) } },
                        )
                    }
                }
                val btnW by animateDpAsState(if (last) 170.dp else 64.dp, spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow), label = "next")
                Row(
                    Modifier.width(btnW).height(64.dp)
                        .shadow(18.dp, CircleShape, ambientColor = hue, spotColor = hue)
                        .clip(CircleShape)
                        .background(if (last) Brush.verticalGradient(listOf(Color(0xFF3BE07E), Color(0xFF15A64A))) else SolidColor(Color.White))
                        .clickable(role = Role.Button) {
                            if (last) { haptics.success(); onDone() } else scope.launch { pager.animateScrollToPage(page + 1) }
                        }
                        .semantics { contentDescription = if (last) "Empezar" else "Siguiente" },
                    horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (last && btnW > 120.dp) Text("Empezar", style = MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 1, modifier = Modifier.padding(end = 10.dp))
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = if (last) Color.White else Night, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
private fun BrandMark(size: Dp) {
    Box(
        Modifier.size(size).clip(RoundedCornerShape(size * 0.31f)).background(Brush.verticalGradient(listOf(Color(0xFF3BE07E), Color(0xFF15A64A)))),
        contentAlignment = Alignment.Center,
    ) { Icon(Icons.Rounded.ShoppingBasket, null, tint = Color.White, modifier = Modifier.size(size * 0.56f)) }
}

/** Texto de cada paso: entra escalonado cuando la página está delante. */
@Composable
private fun SlideCopy(s: Slide, active: Boolean) {
    @Composable
    fun Modifier.stagger(i: Int): Modifier {
        val t by animateFloatAsState(if (active) 1f else 0f, tween(600, delayMillis = if (active) i * 90 else 0, easing = EaseOutExpo), label = "copy$i")
        return graphicsLayer { alpha = t; translationY = (1 - t) * 40f }
    }
    Column(Modifier.padding(bottom = 8.dp)) {
        Text(
            s.eyebrow, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
            color = lerpWhite(s.hue, 0.45f),
            modifier = Modifier.stagger(0).clip(CircleShape).background(s.hue.copy(alpha = 0.22f)).padding(horizontal = 12.dp, vertical = 6.dp),
        )
        Text(
            buildAnnotatedString { append(s.title); append("\n"); withStyle(SpanStyle(color = lerpWhite(s.hue, 0.3f))) { append(s.accent) } },
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp, lineHeight = 46.sp, letterSpacing = (-1.8).sp),
            color = Color.White, modifier = Modifier.stagger(1).padding(top = 14.dp, bottom = 12.dp),
        )
        Text(s.body, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal, lineHeight = 24.sp), color = Color.White.copy(alpha = 0.72f), modifier = Modifier.stagger(2))
    }
}

private fun lerpWhite(c: Color, t: Float) = Color(c.red + (1 - c.red) * t, c.green + (1 - c.green) * t, c.blue + (1 - c.blue) * t)

@Composable
private fun appear(active: Boolean, delay: Int = 0): Float {
    val v by animateFloatAsState(
        if (active) 1f else 0f,
        if (active) spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow) else tween(150),
        label = "appear",
    )
    var go by remember { mutableStateOf(false) }
    LaunchedEffect(active) { if (active) { kotlinx.coroutines.delay(delay.toLong()); go = true } else go = false }
    return if (go) v else 0f
}

@Composable
private fun PricesArt(active: Boolean, pics: List<Product>) {
    val float by rememberInfiniteTransition(label = "float").animateFloat(0f, 1f, infiniteRepeatable(tween(2200), RepeatMode.Reverse), label = "y")
    BoxWithConstraints(Modifier.fillMaxWidth().widthIn(max = 320.dp).height(300.dp)) {
        val s = maxWidth * 0.46f
        val spots = listOf(Triple(0.02f, 0.12f, -8f), Triple(0.52f, 0.02f, 7f), Triple(0.28f, 0.5f, 3f))
        spots.forEachIndexed { i, (x, y, r) ->
            val a = appear(active, i * 110)
            val p = pics.getOrNull(i)
            Box(
                Modifier.offset(maxWidth * x, maxHeight * y + (if (i == 1) float else 1 - float).let { (it * 8).dp })
                    .size(s).graphicsLayer { alpha = a; scaleX = 0.8f + 0.2f * a; scaleY = 0.8f + 0.2f * a; rotationZ = r; translationY = (1 - a) * 80f },
            ) {
                Box(
                    Modifier.fillMaxSize().shadow(20.dp, RoundedCornerShape(28.dp)).clip(RoundedCornerShape(28.dp)).background(Color.White).padding(12.dp),
                    contentAlignment = Alignment.Center,
                ) { ProductImage(p?.image, listOf("🥑", "🥛", "🍝")[i], Modifier.fillMaxSize()) }
                Text(
                    money(p?.price ?: listOf(1.2, 0.89, 0.95)[i]), style = TextStyle(fontFamily = Numeric, fontWeight = FontWeight.Bold, fontSize = 15.sp), color = Night,
                    modifier = Modifier.align(if (i == 0) Alignment.BottomStart else Alignment.BottomEnd).offset(x = if (i == 0) (-10).dp else 10.dp, y = (-12).dp)
                        .rotate(if (i == 0) 5f else -4f).shadow(6.dp, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).background(Butter).padding(horizontal = 10.dp, vertical = 6.dp),
                )
                if (i == 0) {
                    val d = appear(active, 800)
                    Row(
                        Modifier.align(Alignment.TopStart).offset((-12).dp, (-12).dp).scale(d).alpha(d)
                            .clip(CircleShape).background(Brush.verticalGradient(listOf(Color(0xFF3BE07E), Color(0xFF15A64A)))).padding(start = 8.dp, end = 10.dp, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.ArrowDownward, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        val was = p?.previousPrice
                        Text(money(if (was != null && p.price < was) was - p.price else 0.3), style = TextStyle(fontFamily = Numeric, fontWeight = FontWeight.Bold, fontSize = 13.sp), color = Color.White)
                    }
                }
            }
        }
        val tw by rememberInfiniteTransition(label = "spark").animateFloat(0f, 1f, infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "tw")
        Icon(Icons.Rounded.AutoAwesome, null, tint = Butter, modifier = Modifier.offset(maxWidth * 0.84f, maxHeight * 0.66f).size(24.dp).alpha(if (active) tw else 0f).scale(0.6f + tw * 0.5f))
        Icon(Icons.Rounded.AutoAwesome, null, tint = Mint, modifier = Modifier.offset(maxWidth * 0.04f, maxHeight * 0.82f).size(16.dp).alpha(if (active) 1 - tw else 0f))
    }
}

@Composable
private fun DinnersArt(active: Boolean) {
    val bob by rememberInfiniteTransition(label = "bob").animateFloat(0f, 1f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "bob")
    Column(Modifier.widthIn(max = 340.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        val pot = appear(active)
        Box(
            Modifier.padding(bottom = 12.dp).size(116.dp).graphicsLayer { scaleX = pot; scaleY = pot; translationY = -bob * 14f; rotationZ = -3f * bob }
                .shadow(24.dp, RoundedCornerShape(36.dp), ambientColor = Orange, spotColor = Orange).clip(RoundedCornerShape(36.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFFFFB067), Color(0xFFF2711C)))),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Rounded.RestaurantMenu, null, tint = Color.White, modifier = Modifier.size(48.dp)) }
        listOf(Triple("LUN", "Pollo al limón", "2,10 €"), Triple("MAR", "Crema de calabaza", "1,35 €"), Triple("MIÉ", "Tortilla de patatas", "0,95 €")).forEachIndexed { i, (d, n, p) ->
            val a = appear(active, 250 + i * 140)
            Row(
                Modifier.fillMaxWidth().graphicsLayer { alpha = a; translationX = (1 - a) * 160f }
                    .clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = 0.08f)).border(Ui.hairline, Color.White.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(d, style = TextStyle(fontFamily = Numeric, fontWeight = FontWeight.Bold, fontSize = 12.sp), color = Color(0xFFFFB067), modifier = Modifier.width(46.dp))
                Text(n, style = MaterialTheme.typography.titleSmall, color = Color.White, modifier = Modifier.weight(1f))
                Text(p, style = TextStyle(fontFamily = Numeric, fontWeight = FontWeight.SemiBold, fontSize = 14.sp), color = Color.White.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
private fun TogetherArt(active: Boolean) {
    val spin by rememberInfiniteTransition(label = "spin").animateFloat(0f, 360f, infiniteRepeatable(tween(24000, easing = androidx.compose.animation.core.LinearEasing)), label = "spin")
    Box(Modifier.size(300.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(220.dp).rotate(spin)) {
            drawCircle(Color.White.copy(alpha = 0.18f), style = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f))))
        }
        val l = appear(active, 150)
        val r = appear(active, 250)
        val m = appear(active)
        Person("A", Color(0xFF7C5CFF), 76.dp, Modifier.graphicsLayer { translationX = -150f - (1 - l) * 200f; translationY = -20f; rotationZ = -8f; alpha = l })
        Person("L", Orange, 76.dp, Modifier.graphicsLayer { translationX = 150f + (1 - r) * 200f; translationY = -20f; rotationZ = 8f; alpha = r })
        Box(
            Modifier.size(104.dp).scale(0.4f + 0.6f * m).alpha(m).border(5.dp, Night, CircleShape).clip(CircleShape)
                .background(Brush.verticalGradient(listOf(Color(0xFF3BE07E), Color(0xFF15A64A)))),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Rounded.Group, null, tint = Color.White, modifier = Modifier.size(40.dp)) }
        val b = appear(active, 700)
        Row(
            Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp).graphicsLayer { alpha = b; translationY = (1 - b) * 40f; scaleX = 0.9f + 0.1f * b; scaleY = 0.9f + 0.1f * b }
                .shadow(18.dp, RoundedCornerShape(18.dp)).clip(RoundedCornerShape(18.dp)).background(Color.White).padding(start = 10.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(Modifier.size(26.dp).clip(CircleShape).background(Color(0xFF22C55E)), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
            Text(
                buildAnnotatedString { append("Lucía te paga "); withStyle(SpanStyle(fontFamily = Numeric, fontWeight = FontWeight.Bold)) { append("12,40 €") } },
                style = MaterialTheme.typography.labelLarge, color = Night,
            )
        }
    }
}

@Composable
private fun Person(letter: String, color: Color, size: Dp, modifier: Modifier) {
    Box(modifier.size(size).border(5.dp, Night, CircleShape).clip(CircleShape).background(color), contentAlignment = Alignment.Center) {
        Text(letter, style = TextStyle(fontFamily = Numeric, fontWeight = FontWeight.Bold, fontSize = 26.sp), color = Color.White)
    }
}

// ───────────── Registro y entrada ─────────────

@Composable
fun AuthScreen(vm: AppViewModel, idx: CatalogIndex?) {
    val c = LocalColors.current
    val scope = rememberCoroutineScope()
    val haptics = rememberHaptics()
    val st = vm.state.value
    var isNew by rememberSaveable { mutableStateOf(true) }
    var name by rememberSaveable { mutableStateOf(st.list.members.find { it.id == st.meId }?.name ?: "") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var show by rememberSaveable { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var shakes by remember { mutableIntStateOf(0) }
    val ready = email.contains('@') && password.length >= (if (isNew) 8 else 1) && (!isNew || name.isNotBlank())
    val pics = remember(idx) { stickers(idx, 4) }

    // Sacudida del formulario cuando algo falla
    val shake = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(shakes) {
        if (shakes == 0) return@LaunchedEffect
        for (x in listOf(-16f, 16f, -12f, 12f, -6f, 0f)) shake.animateTo(x, tween(55))
    }

    fun submit() {
        if (busy || !ready) return
        busy = true
        error = null
        scope.launch {
            try {
                if (isNew) vm.repo.register(name, email, password) else vm.repo.login(email, password)
                haptics.success()
            } catch (e: Exception) {
                haptics.warn()
                error = e.message ?: "Algo ha fallado. Prueba otra vez."
                shakes++
            } finally { busy = false }
        }
    }

    Column(Modifier.fillMaxSize().background(c.ground).verticalScroll(rememberScrollState()).imePadding()) {
        // Cabecera oscura con productos flotando
        val float by rememberInfiniteTransition(label = "float").animateFloat(0f, 1f, infiniteRepeatable(tween(3000), RepeatMode.Reverse), label = "f")
        val hue by animateColorAsState(if (isNew) Color(0xFF22C55E) else Color(0xFF7C5CFF), tween(600), label = "hue")
        BoxWithConstraints(Modifier.fillMaxWidth().height(270.dp).background(Night)) {
            Canvas(Modifier.fillMaxSize()) { drawRect(Brush.radialGradient(listOf(hue.copy(alpha = 0.45f), Color.Transparent), Offset(size.width / 2, size.height * 0.45f), size.width * 0.7f)) }
            val spots = listOf(Triple(0.05f, 0.22f, -10f), Triple(0.76f, 0.16f, 9f), Triple(0.04f, 0.62f, 6f), Triple(0.78f, 0.58f, -6f))
            pics.forEachIndexed { i, p ->
                val a = appear(true, 100 + i * 90)
                Box(
                    Modifier.offset(maxWidth * spots[i].first, maxHeight * spots[i].second + ((if (i % 2 == 0) float else 1 - float) * 10).dp)
                        .size(if (i < 2) 68.dp else 58.dp).graphicsLayer { alpha = a; scaleX = a; scaleY = a; rotationZ = spots[i].third }
                        .shadow(14.dp, RoundedCornerShape(20.dp)).clip(RoundedCornerShape(20.dp)).background(Color.White).padding(7.dp),
                ) { ProductImage(p.image, "🛒", Modifier.fillMaxSize()) }
            }
            Column(Modifier.align(Alignment.Center).windowInsetsPadding(WindowInsets.statusBars), horizontalAlignment = Alignment.CenterHorizontally) {
                val m = appear(true)
                Box(Modifier.scale(m)) { BrandMark(80.dp) }
                Text(
                    buildAnnotatedString { append("List"); withStyle(SpanStyle(color = Mint)) { append("Pro") } },
                    style = MaterialTheme.typography.displayMedium.copy(fontSize = 40.sp), color = Color.White, modifier = Modifier.padding(top = 8.dp).alpha(m),
                )
            }
        }

        Column(
            Modifier.offset(y = (-24).dp).fillMaxWidth().clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(c.ground)
                .padding(horizontal = 20.dp).padding(top = 20.dp, bottom = 12.dp).windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(c.surface2).padding(4.dp)) {
                listOf(true to "Soy nuevo", false to "Ya tengo cuenta").forEach { (k, label) ->
                    Box(
                        Modifier.weight(1f).heightIn(min = 44.dp).clip(RoundedCornerShape(12.dp)).background(if (isNew == k) c.surface else Color.Transparent)
                            .clickable(role = Role.Tab) { haptics.tick(); isNew = k; error = null },
                        contentAlignment = Alignment.Center,
                    ) { Text(label, style = MaterialTheme.typography.labelLarge, color = if (isNew == k) c.ink else c.ink2) }
                }
            }
            AnimatedContent(isNew, transitionSpec = { (fadeIn() + slideInVertically { it / 3 }) togetherWith fadeOut() }, label = "title") { n ->
                Text(
                    buildAnnotatedString {
                        append(if (n) "Crea tu " else "Hola de ")
                        withStyle(SpanStyle(color = if (n) c.greenInk else c.violet)) { append(if (n) "cuenta" else "nuevo") }
                    },
                    style = MaterialTheme.typography.displayMedium.copy(fontSize = 34.sp), color = c.ink, modifier = Modifier.padding(top = 22.dp, bottom = 6.dp),
                )
            }
            Text(if (isNew) "Tu cesta, tus cenas y tus cuentas, en todos tus móviles." else "Entra y sigue donde lo dejaste.", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal), color = c.ink2)

            Column(Modifier.graphicsLayer { translationX = shake.value }) {
                if (isNew) AuthField("¿Cómo te llamas?", name, { if (it.length <= 24) name = it }, "Así te verán en las listas compartidas", KeyboardOptions(capitalization = KeyboardCapitalization.Words))
                AuthField("Email", email, { email = it.trim() }, "tu@email.com", KeyboardOptions(keyboardType = KeyboardType.Email, autoCorrectEnabled = false))
                AuthField(
                    "Contraseña", password, { password = it }, if (isNew) "Mínimo 8 caracteres" else "••••••••",
                    KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
                    visual = if (show) VisualTransformation.None else PasswordVisualTransformation(),
                    trailing = {
                        Box(Modifier.size(40.dp).clip(CircleShape).clickable(onClickLabel = if (show) "Ocultar contraseña" else "Ver contraseña") { show = !show }, contentAlignment = Alignment.Center) {
                            Icon(if (show) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, null, tint = c.ink2, modifier = Modifier.size(20.dp))
                        }
                    },
                )
                if (isNew && password.isNotEmpty()) PasswordMeter(password)
                error?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = c.up,
                        modifier = Modifier.padding(top = 12.dp).fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(c.up.copy(alpha = 0.12f)).padding(12.dp))
                }
            }
            AppButton(
                when { busy && isNew -> "Preparando tu cesta…"; busy -> "Entrando…"; isNew -> "Crear cuenta"; else -> "Entrar" },
                ::submit, Modifier.fillMaxWidth().padding(top = 20.dp).heightIn(min = 56.dp), primary = true, enabled = ready && !busy,
                leading = if (busy) null else ({ Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, Modifier.size(20.dp)) }),
            )
            Text(
                "Seguir sin cuenta", style = MaterialTheme.typography.labelLarge.copy(textDecoration = TextDecoration.Underline), color = c.ink2,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp).clip(CircleShape)
                    .clickable(role = Role.Button) { haptics.tick(); vm.repo.continueAsGuest() }.padding(horizontal = 14.dp, vertical = 12.dp),
            )
            Text(
                "Solo guardamos tu nombre, tu email y tu lista. Puedes borrar la cuenta cuando quieras desde Ajustes.",
                style = MaterialTheme.typography.bodySmall, color = c.ink2, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            )
        }
    }
}

@Composable
private fun AuthField(
    label: String, value: String, onChange: (String) -> Unit, placeholder: String, keyboard: KeyboardOptions,
    visual: VisualTransformation = VisualTransformation.None, trailing: (@Composable () -> Unit)? = null,
) {
    val c = LocalColors.current
    Text(label, style = MaterialTheme.typography.labelLarge, color = c.ink, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
    BasicTextField(
        value = value, onValueChange = onChange, singleLine = true, keyboardOptions = keyboard, visualTransformation = visual,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = c.ink), cursorBrush = SolidColor(c.green),
        decorationBox = { inner ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) Text(placeholder, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal), color = c.ink3)
                    inner()
                }
                trailing?.invoke()
            }
        },
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).card(c.surface, c.lineStrong, 16.dp).padding(start = 16.dp, end = if (trailing != null) 6.dp else 16.dp, top = 6.dp, bottom = 6.dp),
    )
}

@Composable
private fun PasswordMeter(value: String) {
    val c = LocalColors.current
    val score = if (value.length < 8) 0 else minOf(4,
        1 + (if (value.length >= 12) 1 else 0) + (if (value.any { it.isUpperCase() } && value.any { it.isLowerCase() }) 1 else 0) + (if (value.any { !it.isLetter() }) 1 else 0))
    val color = when { score == 0 -> c.up; score <= 2 -> Color(0xFFE5B800); else -> c.greenHi }
    Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(4) { i ->
            val on = i < maxOf(1, score)
            val tint by animateColorAsState(if (on) color else c.lineStrong, label = "bar")
            Box(Modifier.width(28.dp).height(5.dp).clip(CircleShape).background(tint))
        }
        Spacer(Modifier.width(6.dp))
        Text(listOf("Muy corta", "Vale", "Bien", "Fuerte", "Muy fuerte")[score], style = MaterialTheme.typography.labelSmall, color = c.ink3)
    }
}

// ───────────── Cuenta (en Ajustes) ─────────────

@Composable
fun AccountPanel(vm: AppViewModel, state: com.listpro.xyon.pro.data.UserState) {
    val c = LocalColors.current
    val scope = rememberCoroutineScope()
    val haptics = rememberHaptics()
    val auth = state.auth
    var panel by rememberSaveable { mutableStateOf<String?>(null) }
    var a by rememberSaveable { mutableStateOf("") }
    var b by rememberSaveable { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    fun open(p: String) { panel = if (panel == p) null else p; a = if (p == "name") auth?.user?.name ?: "" else ""; b = "" }
    fun run(ok: String, block: suspend () -> Unit) {
        busy = true
        scope.launch {
            try { block(); haptics.success(); vm.repo.toast(ok); panel = null } catch (e: Exception) { haptics.warn(); vm.repo.toast(e.message ?: "No se pudo conectar") } finally { busy = false }
        }
    }

    Panel {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (auth == null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BrandMark(52.dp)
                    Column(Modifier.padding(start = 14.dp)) {
                        Text("Sin cuenta", style = MaterialTheme.typography.titleLarge, color = c.ink)
                        Text("Crea una para tener tu cesta en todos tus móviles.", style = MaterialTheme.typography.bodySmall, color = c.ink2)
                    }
                }
                AppButton("Crear cuenta o entrar", vm.repo::wantAccount, Modifier.fillMaxWidth(), primary = true)
                return@Column
            }
            val me = state.list.members.find { it.id == state.meId }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(me ?: com.listpro.xyon.pro.data.Member(auth.user.id, auth.user.name, "#22C55E", ""), 54.dp)
                Column(Modifier.weight(1f).padding(start = 14.dp)) {
                    Text(auth.user.name, style = MaterialTheme.typography.titleLarge, color = c.ink, maxLines = 1)
                    Text(auth.user.email, style = MaterialTheme.typography.bodySmall, color = c.ink2, maxLines = 1)
                }
                Box(Modifier.size(44.dp).clip(CircleShape).clickable(onClickLabel = "Cambiar el nombre") { open("name") }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Edit, null, tint = c.ink2, modifier = Modifier.size(20.dp))
                }
            }
            if (panel == "name") SubForm {
                SmallField(a, { if (it.length <= 24) a = it }, "Tu nombre")
                AppButton("Guardar nombre", { run("Nombre cambiado") { vm.repo.renameAccount(a) } }, Modifier.fillMaxWidth(), primary = true, enabled = !busy && a.isNotBlank())
            }
            AppButton("Cambiar contraseña", { open("password") }, Modifier.fillMaxWidth(), leading = { Icon(Icons.Rounded.Key, null, Modifier.size(18.dp)) })
            if (panel == "password") SubForm {
                SmallField(a, { a = it }, "Contraseña actual", password = true)
                SmallField(b, { b = it }, "Nueva (mínimo 8 caracteres)", password = true)
                AppButton("Cambiar", { run("Contraseña cambiada. Las demás sesiones se han cerrado.") { vm.repo.changePassword(a, b) } }, Modifier.fillMaxWidth(), primary = true, enabled = !busy && a.isNotEmpty() && b.length >= 8)
            }
            AppButton("Cerrar sesión en los demás", { run("Sesión cerrada en los demás dispositivos") { vm.repo.logoutEverywhere() } }, Modifier.fillMaxWidth(), enabled = !busy,
                leading = { Icon(Icons.Rounded.Devices, null, Modifier.size(18.dp)) })
            if (panel == "logout") SubForm {
                Text("Tu cesta se queda en este móvil, pero deja de sincronizarse hasta que vuelvas a entrar.", style = MaterialTheme.typography.bodyMedium, color = c.ink)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppButton("No", { panel = null })
                    AppButton("Cerrar sesión", { vm.repo.logout(); vm.repo.toast("Sesión cerrada") }, danger = true)
                }
            } else AppButton("Cerrar sesión", { open("logout") }, Modifier.fillMaxWidth(), leading = { Icon(Icons.AutoMirrored.Rounded.Logout, null, Modifier.size(18.dp)) })
            AppButton("Borrar mi cuenta", { open("delete") }, Modifier.fillMaxWidth(), danger = true, leading = { Icon(Icons.Rounded.Delete, null, Modifier.size(18.dp)) })
            if (panel == "delete") SubForm {
                Text("Se borra tu cuenta para siempre. Escribe tu contraseña para confirmarlo.", style = MaterialTheme.typography.bodyMedium, color = c.ink)
                SmallField(a, { a = it }, "Contraseña", password = true)
                AppButton("Borrar para siempre", { run("Cuenta borrada") { vm.repo.deleteAccount(a) } }, Modifier.fillMaxWidth(), danger = true, enabled = !busy && a.isNotEmpty())
            }
        }
    }
}

@Composable
private fun SubForm(content: @Composable () -> Unit) {
    val c = LocalColors.current
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(c.surface2).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { content() }
}

@Composable
private fun SmallField(value: String, onChange: (String) -> Unit, placeholder: String, password: Boolean = false) {
    val c = LocalColors.current
    BasicTextField(
        value = value, onValueChange = onChange, singleLine = true,
        visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = if (password) KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false) else KeyboardOptions(capitalization = KeyboardCapitalization.Words),
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = c.ink), cursorBrush = SolidColor(c.green),
        decorationBox = { inner -> Box { if (value.isEmpty()) Text(placeholder, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal), color = c.ink3); inner() } },
        modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp).card(c.surface, c.lineStrong, 14.dp).padding(horizontal = 14.dp, vertical = 14.dp),
    )
}
