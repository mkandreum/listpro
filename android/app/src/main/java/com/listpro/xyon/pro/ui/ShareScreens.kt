package com.listpro.xyon.pro.ui

import android.content.Intent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material.icons.rounded.Nfc
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.listpro.xyon.pro.BuildConfig
import com.listpro.xyon.pro.data.Expense
import com.listpro.xyon.pro.data.Member
import com.listpro.xyon.pro.data.UserState
import com.listpro.xyon.pro.data.balances
import com.listpro.xyon.pro.data.initials
import com.listpro.xyon.pro.data.settlements
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

private val es = Locale.forLanguageTag("es-ES")
private fun shortDay(iso: String) = runCatching {
    Instant.parse(iso).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("d MMM", es))
}.getOrDefault("")

fun parseAmount(v: String): Double? = v.replace("€", "").replace(" ", "").replace(",", ".").toDoubleOrNull()
private fun fmtAmount(v: Double) = String.format(es, "%.2f", v)

// ───────────── Avatares ─────────────

@Composable
fun Avatar(m: Member?, size: Dp = 32.dp, ring: Boolean = false) {
    val c = LocalColors.current
    val hue = hueColor(m?.hue ?: "#B9BCC4")
    Box(
        Modifier.size(size).then(if (ring) Modifier.border(2.5.dp, c.surface, CircleShape) else Modifier).clip(CircleShape).background(hue),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            initials(m?.name ?: "?"), color = hue.ink(), maxLines = 1,
            style = TextStyle(fontFamily = Numeric, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.38f).sp, letterSpacing = (-0.2).sp),
        )
    }
}

@Composable
fun AvatarStack(members: List<Member>, max: Int = 3, size: Dp = 28.dp) {
    val c = LocalColors.current
    Row(horizontalArrangement = Arrangement.spacedBy(-size * 0.3f)) {
        members.take(max).forEach { Avatar(it, size, ring = true) }
        if (members.size > max) Box(
            Modifier.size(size).border(2.5.dp, c.surface, CircleShape).clip(CircleShape).background(c.surface2),
            contentAlignment = Alignment.Center,
        ) { Text("+${members.size - max}", style = MaterialTheme.typography.labelSmall, color = c.ink2) }
    }
}

/** Botón de la cabecera de la cesta: quién está en la lista, o invitar a compartirla. */
@Composable
fun SharePill(shared: Boolean, members: List<Member>, onClick: () -> Unit) {
    val c = LocalColors.current
    val shape = CircleShape
    if (shared) {
        val pulse by rememberInfiniteTransition(label = "live").animateFloat(1f, 0.35f, infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "live")
        Row(
            Modifier.heightIn(min = 46.dp).clip(shape).background(c.glass).border(Ui.hairline, c.line, shape)
                .clickable(role = Role.Button, onClickLabel = "Ver la lista compartida") { onClick() }
                .padding(start = 6.dp, end = 12.dp).semantics { contentDescription = "Lista compartida con ${members.size} personas" },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AvatarStack(members)
            Box(Modifier.size(9.dp).clip(CircleShape).background(c.greenHi.copy(alpha = pulse)))
        }
    } else {
        Row(
            Modifier.heightIn(min = 46.dp).clip(shape).background(c.greenSoft).border(Ui.hairline, c.greenHi.copy(alpha = 0.45f), shape)
                .clickable(role = Role.Button) { onClick() }.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Icon(Icons.Rounded.GroupAdd, null, tint = c.greenInk, modifier = Modifier.size(20.dp))
            Text("Compartir", style = MaterialTheme.typography.labelLarge, color = c.greenInk)
        }
    }
}

/** Tarjeta que invita a compartir (en la cesta y en Ajustes). */
@Composable
fun SharePromo(title: String, body: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = LocalColors.current
    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(Ui.radiusL))
            .background(c.violet.copy(alpha = 0.12f).compositeOver(c.surface))
            .border(Ui.hairline, c.violet.copy(alpha = 0.25f), RoundedCornerShape(Ui.radiusL))
            .clickable { onClick() }.padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(Modifier.size(46.dp).clip(RoundedCornerShape(15.dp)).background(c.violet), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Group, null, tint = Color.White, modifier = Modifier.size(24.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = c.ink)
            Text(body, style = MaterialTheme.typography.bodySmall, color = c.ink2)
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = c.ink3)
    }
}

// ───────────── Piezas de formulario ─────────────

@Composable
private fun Label(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = LocalColors.current.ink, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
}

@Composable
private fun Input(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    keyboard: KeyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
    suffix: String? = null,
) {
    val c = LocalColors.current
    BasicTextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        textStyle = style.copy(color = c.ink),
        cursorBrush = SolidColor(c.green),
        keyboardOptions = keyboard,
        decorationBox = { inner ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) Text(placeholder, color = c.ink3, style = style.copy(fontWeight = FontWeight.Normal))
                    inner()
                }
                suffix?.let { Text(it, style = style, color = c.ink3) }
            }
        },
        modifier = modifier.fillMaxWidth().heightIn(min = 52.dp).card(c.surface, c.lineStrong, 16.dp).padding(horizontal = 16.dp, vertical = 14.dp),
    )
}

@Composable
private fun Segmented(options: List<Pair<String, String>>, selected: String, onSelect: (String) -> Unit) {
    val c = LocalColors.current
    val haptics = rememberHaptics()
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(c.surface2).padding(4.dp)) {
        options.forEach { (key, label) ->
            val on = key == selected
            Box(
                Modifier.weight(1f).heightIn(min = 44.dp).clip(RoundedCornerShape(12.dp))
                    .background(if (on) c.surface else Color.Transparent)
                    .clickable(role = Role.RadioButton) { haptics.tick(); onSelect(key) }.padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center,
            ) { Text(label, style = MaterialTheme.typography.labelLarge, color = if (on) c.ink else c.ink2, textAlign = TextAlign.Center, maxLines = 2) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PeoplePicker(members: List<Member>, meId: String?, isOn: (String) -> Boolean, onToggle: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        members.forEach { m ->
            Chip(isOn(m.id), { onToggle(m.id) }, leading = { Box(Modifier.scale(if (isOn(m.id)) 1f else 0.9f)) { Avatar(m, 32.dp) } }) {
                Text(if (m.id == meId) "Yo" else m.name, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Sheet(onDismiss: () -> Unit, content: @Composable () -> Unit) {
    val c = LocalColors.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = c.surface,
        shape = RoundedCornerShape(topStart = Ui.radiusL, topEnd = Ui.radiusL),
    ) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 28.dp)) { content() }
    }
}

@Composable
private fun SheetTitle(text: String, center: Boolean = false) {
    Text(
        text, style = MaterialTheme.typography.headlineLarge.copy(fontSize = 28.sp, lineHeight = 31.sp), color = LocalColors.current.ink,
        textAlign = if (center) TextAlign.Center else TextAlign.Start, modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
    )
}

// ───────────── Hoja de compartir ─────────────

@Composable
fun ShareSheet(vm: AppViewModel, state: UserState, nfcAvailable: Boolean, nfcListening: Boolean, onDismiss: () -> Unit) {
    Sheet(onDismiss) {
        val code = state.sync.code
        if (code == null) StartShare(vm, state, nfcAvailable, nfcListening) else SharedView(vm, state, code, nfcAvailable, onDismiss)
    }
}

@Composable
private fun StartShare(vm: AppViewModel, state: UserState, nfcAvailable: Boolean, nfcListening: Boolean) {
    val c = LocalColors.current
    val scope = rememberCoroutineScope()
    val haptics = rememberHaptics()
    val me = state.list.members.find { it.id == state.meId }
    var mode by rememberSaveable { mutableStateOf("create") }
    var name by rememberSaveable { mutableStateOf(me?.name ?: "") }
    var code by rememberSaveable { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    val ok = name.isNotBlank() && (mode == "create" || code.length == 8)

    // Tres caras que se juntan: la idea de comprar entre varios
    Box(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 4.dp), contentAlignment = Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Bubble("A", c.violet, 60.dp, Modifier.offset(x = 14.dp).rotate(-8f))
            Box(Modifier.size(78.dp).border(4.dp, c.surface, CircleShape).clip(CircleShape).background(c.gradient), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Group, null, tint = Color.White, modifier = Modifier.size(34.dp))
            }
            Bubble("L", Color(0xFFFF8A2A), 60.dp, Modifier.offset(x = (-14).dp).rotate(8f))
        }
    }
    SheetTitle(if (mode == "join") "Únete a la lista" else "Haced la compra juntos", center = true)
    Text(
        if (mode == "join") "Verás la misma cesta que quien te invitó, al momento, y podréis repartir lo que cuesta."
        else "Tu pareja, tu familia o tus compis de piso ven la misma cesta al momento: qué ha pedido cada uno, qué está ya en el carro y cuánto os debéis.",
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal), color = c.ink2, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(18.dp))
    Segmented(listOf("create" to "Crear lista", "join" to "Tengo un código"), mode) { mode = it }
    Label("¿Cómo te llamas?")
    Input(name, { if (it.length <= 24) name = it }, "Así te verán los demás", keyboard = KeyboardOptions(capitalization = KeyboardCapitalization.Words))
    if (mode == "join") {
        Label("Código de la lista")
        Input(
            code, { v -> code = v.uppercase().filter { it.isLetterOrDigit() }.take(8) }, "ABCD2345",
            style = MaterialTheme.typography.titleLarge.copy(fontFamily = Numeric, letterSpacing = 4.sp),
            keyboard = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, autoCorrectEnabled = false),
        )
    }
    AppButton(
        when { busy -> "Un momento…"; mode == "create" -> "Crear lista compartida"; else -> "Unirme" },
        {
            busy = true
            scope.launch {
                try {
                    if (mode == "create") { vm.repo.createSync(name); vm.repo.toast("Lista compartida creada") }
                    else { vm.repo.joinSync(code, name); vm.repo.toast("Ya estás dentro de la lista") }
                    haptics.success()
                } catch (e: Exception) {
                    haptics.warn()
                    vm.repo.toast(e.message ?: "No se pudo conectar")
                } finally { busy = false }
            }
        },
        Modifier.fillMaxWidth().padding(top = 20.dp).heightIn(min = 56.dp), primary = true, enabled = ok && !busy,
        leading = { Icon(if (mode == "create") Icons.Rounded.Group else Icons.Rounded.Check, null, Modifier.size(20.dp)) },
    )
    if (mode == "create") {
        Text("Lo que ya tienes en la cesta pasa a la lista compartida.", style = MaterialTheme.typography.bodySmall, color = c.ink2, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 10.dp))
    }
    if (nfcAvailable) {
        AppButton(
            if (nfcListening) "Acerca este móvil al que tiene la lista…" else "Recibir acercando los móviles",
            { vm.setNfcListening(!nfcListening, name) },
            Modifier.fillMaxWidth().padding(top = 12.dp), ghost = true, enabled = name.isNotBlank() || nfcListening,
            leading = { Icon(Icons.Rounded.Nfc, null, Modifier.size(18.dp)) },
        )
    }
}

@Composable
private fun Bubble(letter: String, color: Color, size: Dp, modifier: Modifier) {
    val c = LocalColors.current
    Box(modifier.size(size).border(4.dp, c.surface, CircleShape).clip(CircleShape).background(color), contentAlignment = Alignment.Center) {
        Text(letter, style = TextStyle(fontFamily = Numeric, fontWeight = FontWeight.Bold, fontSize = 22.sp), color = Color.White)
    }
}

@Composable
private fun SharedView(vm: AppViewModel, state: UserState, code: String, nfcAvailable: Boolean, onDismiss: () -> Unit) {
    val c = LocalColors.current
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val haptics = rememberHaptics()
    val members = state.list.members
    val me = members.find { it.id == state.meId }
    var editing by rememberSaveable { mutableStateOf(me == null) }
    var name by rememberSaveable { mutableStateOf(me?.name ?: "") }
    var leaving by rememberSaveable { mutableStateOf(false) }

    SheetTitle(if (members.size > 1) "Lista compartida" else "Comparte tu lista", center = true)
    Text("● En directo con ${members.size} ${if (members.size == 1) "persona" else "personas"}", style = MaterialTheme.typography.labelMedium, color = c.greenInk, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

    Row(
        Modifier.padding(top = 18.dp, bottom = 12.dp).fillMaxWidth().card(c.greenSoft, c.greenHi.copy(alpha = 0.4f))
            .clickable(onClickLabel = "Copiar código") { clipboard.setText(AnnotatedString(code)); haptics.tick(); vm.repo.toast("Código copiado") }.padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(code, style = TextStyle(fontFamily = Numeric, fontWeight = FontWeight.Bold, fontSize = 34.sp, letterSpacing = 6.sp), color = c.greenInk)
        Spacer(Modifier.width(12.dp))
        Icon(Icons.Rounded.ContentCopy, null, tint = c.greenInk.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
    }
    AppButton(
        "Invitar a alguien",
        {
            val link = "${BuildConfig.API_BASE}/#/unirse/$code"
            val text = "Únete a nuestra lista de la compra en ListPro. En la app, pulsa Compartir en la cesta y pon el código $code, o ábrela en la web: $link"
            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), "Invitar a la lista"))
        },
        Modifier.fillMaxWidth().heightIn(min = 56.dp), primary = true,
        leading = { Icon(Icons.AutoMirrored.Rounded.Send, null, Modifier.size(19.dp)) },
    )
    if (nfcAvailable) {
        AppButton(
            "Pasarla acercando los móviles", vm::prepareNfcShare, Modifier.fillMaxWidth().padding(top = 10.dp), ghost = true,
            leading = { Icon(Icons.Rounded.Nfc, null, Modifier.size(18.dp)) },
        )
    }

    Text("En la lista", style = MaterialTheme.typography.titleLarge, color = c.ink, modifier = Modifier.padding(top = 24.dp, bottom = 4.dp))
    members.forEach { m ->
        Row(Modifier.fillMaxWidth().heightIn(min = 60.dp).padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Avatar(m, 42.dp)
            Spacer(Modifier.width(12.dp))
            if (m.id == state.meId && editing) {
                Box(Modifier.weight(1f)) { Input(name, { if (it.length <= 24) name = it }, "Tu nombre", keyboard = KeyboardOptions(capitalization = KeyboardCapitalization.Words)) }
                Spacer(Modifier.width(8.dp))
                AppButton("Guardar", { vm.repo.upsertMe(name); editing = false }, enabled = name.isNotBlank())
            } else {
                Text(m.name, style = MaterialTheme.typography.titleMedium, color = c.ink, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                if (m.id == state.meId) {
                    You()
                    Spacer(Modifier.weight(1f))
                    Box(Modifier.size(44.dp).clip(CircleShape).clickable(onClickLabel = "Cambiar mi nombre") { editing = true }, contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Edit, null, tint = c.ink2, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
    if (me == null && !editing) AppButton("Ponerme nombre", { editing = true }, ghost = true, leading = { Icon(Icons.Rounded.GroupAdd, null, Modifier.size(18.dp)) })
    if (me == null && editing) {
        Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) { Input(name, { if (it.length <= 24) name = it }, "Tu nombre", keyboard = KeyboardOptions(capitalization = KeyboardCapitalization.Words)) }
            Spacer(Modifier.width(8.dp))
            AppButton("Guardar", { vm.repo.upsertMe(name); editing = false }, enabled = name.isNotBlank())
        }
    }

    Spacer(Modifier.height(20.dp))
    if (state.auth != null && members.size < 2) return
    if (leaving) {
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(c.up.copy(alpha = 0.12f)).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Dejarás de ver los cambios de los demás en este móvil.", style = MaterialTheme.typography.bodyMedium, color = c.ink)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppButton("No", { leaving = false })
                AppButton("Salir", { vm.repo.leaveSync(); vm.repo.toast("Has salido de la lista"); onDismiss() }, danger = true)
            }
        }
    } else {
        AppButton("Salir de la lista en este móvil", { leaving = true }, Modifier.fillMaxWidth(), danger = true, leading = { Icon(Icons.AutoMirrored.Rounded.Logout, null, Modifier.size(18.dp)) })
    }
}

@Composable
private fun You() {
    val c = LocalColors.current
    Text(
        "tú", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = c.greenInk,
        modifier = Modifier.padding(start = 8.dp).clip(CircleShape).background(c.greenSoft).padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

// ───────────── Terminar compra ─────────────

@Composable
fun CheckoutSheet(vm: AppViewModel, state: UserState, onDismiss: () -> Unit) {
    val c = LocalColors.current
    val haptics = rememberHaptics()
    val members = state.list.members
    val meId = state.meId
    val lines = remember { vm.repo.checkoutLines() }
    val all = remember { vm.repo.basketLines().size }
    val estimate = lines.sumOf { it.product.price * it.qty }
    val canSplit = state.sync.code != null && members.size > 1
    val defaultName = remember { "Compra del ${java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("d 'de' MMMM", es))}" }
    var name by rememberSaveable { mutableStateOf(defaultName) }
    var amount by rememberSaveable { mutableStateOf(fmtAmount(estimate)) }
    var paidBy by rememberSaveable { mutableStateOf(if (members.any { it.id == meId }) meId else members.firstOrNull()?.id) }
    var mode by rememberSaveable { mutableStateOf("equal") }
    var among by rememberSaveable { mutableStateOf(members.map { it.id }) }
    val value = parseAmount(amount)
    val valid = name.isNotBlank() && value != null && value > 0
    val splitting = canSplit && mode != "none"
    val shares = if (splitting && valid) vm.repo.purchaseShares(value!!, mode, among) else emptyMap()

    Sheet(onDismiss) {
        SheetTitle("Terminar compra")
        Text(
            if (lines.size < all) "Entran los ${lines.size} productos que están en el carro. Los otros ${all - lines.size} se quedan en la cesta."
            else "Entran los ${lines.size} productos de la cesta.",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal), color = c.ink2,
        )
        Label("Nombre")
        Input(name, { if (it.length <= 60) name = it }, "Compra")
        Label("¿Cuánto ha sido?")
        Input(
            amount, { amount = it.filter { ch -> ch.isDigit() || ch == ',' || ch == '.' }.take(9) }, "0,00",
            style = MaterialTheme.typography.titleLarge.copy(fontFamily = Numeric), keyboard = KeyboardOptions(keyboardType = KeyboardType.Decimal), suffix = "€",
        )
        Text("Con los precios de hoy salen ${money(estimate)}. Cámbialo si el tique dice otra cosa.", style = MaterialTheme.typography.bodySmall, color = c.ink2, modifier = Modifier.padding(top = 6.dp))

        if (canSplit) {
            Label("¿Quién ha pagado?")
            PeoplePicker(members, meId, { it == paidBy }) { paidBy = it }
            Label("¿Cómo lo repartís?")
            Segmented(listOf("equal" to "A partes iguales", "items" to "Por producto", "none" to "No repartir"), mode) { mode = it }
            if (mode != "none") {
                Label(if (mode == "equal") "Entre quién" else "Lo que no es de nadie, entre quién")
                PeoplePicker(members, meId, { it in among }) { id -> among = if (id in among) among - id else among + id }
            }
            if (splitting && shares.isNotEmpty()) {
                Column(Modifier.padding(top = 14.dp).fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(c.greenSoft).padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    members.filter { (shares[it.id] ?: 0.0) > 0 }.forEach { m ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Avatar(m, 26.dp)
                            Text(if (m.id == meId) "Tú" else m.name, style = MaterialTheme.typography.labelLarge, color = c.ink, modifier = Modifier.weight(1f).padding(start = 10.dp))
                            Text(money(shares.getValue(m.id)), style = MaterialTheme.typography.titleMedium, color = c.ink)
                        }
                    }
                    if (mode == "items") Text("Cada uno paga lo que añadió a la cesta; lo que no tiene dueño se reparte.", style = MaterialTheme.typography.bodySmall, color = c.ink2)
                }
            }
        } else if (state.sync.code != null) {
            Text("Cuando alguien más se una a la lista, podréis repartir las compras.", style = MaterialTheme.typography.bodyMedium, color = c.ink2, modifier = Modifier.padding(top = 14.dp))
        }

        AppButton(
            "Guardar compra",
            {
                if (splitting && among.isEmpty()) { vm.repo.toast("Elige entre quién se reparte"); return@AppButton }
                if (vm.repo.finishShop(name.trim(), value!!, if (splitting) paidBy else null, if (splitting) mode else null, among)) {
                    haptics.success()
                    vm.repo.toast(if (splitting) "Compra guardada y repartida" else "Guardada: ${name.trim()}")
                    onDismiss()
                }
            },
            Modifier.fillMaxWidth().padding(top = 22.dp).heightIn(min = 56.dp), primary = true, enabled = valid,
            leading = { Icon(Icons.Rounded.Check, null, Modifier.size(20.dp)) },
        )
    }
}

// ───────────── Cuentas ─────────────

@Composable
fun AccountsContent(vm: AppViewModel, state: UserState) {
    val c = LocalColors.current
    val haptics = rememberHaptics()
    val members = state.list.members
    val meId = state.meId
    val expenses = state.list.expenses
    var adding by rememberSaveable { mutableStateOf(false) }
    var confirm by rememberSaveable { mutableStateOf<String?>(null) }

    if (state.sync.code == null || state.list.members.size < 2) {
        EmptyState(
            Icons.Rounded.Group, "Repartid los gastos sin hacer cuentas",
            "Comparte la cesta con tu pareja, tu familia o tus compis de piso. Cada compra se apunta con quién la pagó, y ListPro os dice quién debe cuánto a quién.",
        ) { AppButton("Compartir la cesta", { vm.showShare(true) }, primary = true, modifier = Modifier.padding(top = 8.dp)) }
        return
    }

    val bal = remember(expenses, members) { balances(expenses, members) }
    val plan = remember(bal) { settlements(bal) }
    val mine = meId?.let { bal[it] } ?: 0.0
    val spent = expenses.filter { it.kind == "purchase" }.sumOf { it.amount }
    fun label(id: String) = if (id == meId) "Tú" else members.find { it.id == id }?.name ?: "Alguien"
    fun byId(id: String) = members.find { it.id == id }

    Column {
        // Saldo: verde si te deben o estás en paz; violeta si debes
        val owe = mine < -0.004
        val shown by animateFloatAsState(abs(mine).toFloat(), tween(450, easing = EaseOutExpo), label = "balance")
        Column(
            Modifier.padding(top = 14.dp).fillMaxWidth().clip(RoundedCornerShape(Ui.radiusL))
                .background(if (owe) androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0xFF9B83FF), Color(0xFF5B37E8))) else c.gradient)
                .padding(22.dp),
        ) {
            Text(if (mine > 0.004) "Te deben" else if (owe) "Debes" else "Tu saldo", style = MaterialTheme.typography.labelLarge, color = Color.White)
            if (abs(mine) > 0.004) Price(shown.toDouble(), Modifier.padding(vertical = 8.dp), size = 56, color = Color.White)
            else Text("En paz", style = MaterialTheme.typography.displayMedium, color = Color.White, modifier = Modifier.padding(vertical = 8.dp))
            Text("${members.size} en la lista · ${money(spent)} gastados juntos", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.9f))
        }

        if (plan.isNotEmpty()) Panel {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Para quedar en paz", style = MaterialTheme.typography.titleLarge, color = c.ink)
                plan.forEach { t ->
                    val key = "${t.from}>${t.to}"
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(c.surface2).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Avatar(byId(t.from), 34.dp)
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = c.ink3, modifier = Modifier.padding(horizontal = 2.dp).size(16.dp))
                        Avatar(byId(t.to), 34.dp)
                        Column(Modifier.weight(1f).padding(start = 10.dp)) {
                            Text(
                                when { t.from == meId -> "Le pagas a ${label(t.to)}"; t.to == meId -> "${label(t.from)} te paga"; else -> "${label(t.from)} le paga a ${label(t.to)}" },
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = c.ink, maxLines = 2,
                            )
                            Text(money(t.amount), style = MaterialTheme.typography.titleMedium, color = c.ink)
                        }
                        if (confirm == key) AppButton("Hecho", { vm.repo.settleUp(t.from, t.to, t.amount); haptics.success(); vm.repo.toast("Pago apuntado"); confirm = null }, primary = true)
                        else AppButton("Saldar", { confirm = key })
                    }
                }
                Text("ListPro no mueve dinero: pagaos como siempre (Bizum, efectivo…) y pulsad «Saldar» para apuntarlo.", style = MaterialTheme.typography.bodySmall, color = c.ink2)
            }
        }

        Panel {
            Column {
                Text("Saldos", style = MaterialTheme.typography.titleLarge, color = c.ink)
                members.forEach { m ->
                    val v = bal[m.id] ?: 0.0
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Avatar(m, 30.dp)
                        Text(m.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = c.ink, modifier = Modifier.padding(start = 10.dp))
                        if (m.id == meId) You()
                        Spacer(Modifier.weight(1f))
                        Text(
                            "${if (v > 0.004) "+" else ""}${money(v)}", style = MaterialTheme.typography.titleMedium,
                            color = when { v > 0.004 -> c.greenInk; v < -0.004 -> c.up; else -> c.ink },
                        )
                    }
                }
            }
        }

        Row(Modifier.padding(top = 28.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Movimientos", style = MaterialTheme.typography.headlineMedium, color = c.ink, modifier = Modifier.weight(1f))
            AppButton("Añadir gasto", { adding = true }, ghost = true, leading = { Icon(Icons.Rounded.Add, null, Modifier.size(18.dp)) })
        }
        if (expenses.isEmpty()) {
            Text("Cuando terminéis una compra desde la cesta, o añadáis un gasto a mano, aparecerá aquí.", style = MaterialTheme.typography.bodyLarge, color = c.ink2)
        }
        expenses.reversed().forEach { e -> ExpenseRow(e, members, meId, ::label) { vm.repo.deleteExpense(e.id); vm.repo.toast("Movimiento borrado") } }
    }
    if (adding) ExpenseSheet(vm, state) { adding = false }
}

@Composable
private fun ExpenseRow(e: Expense, members: List<Member>, meId: String?, label: (String) -> String, onDelete: () -> Unit) {
    val c = LocalColors.current
    var open by rememberSaveable(e.id) { mutableStateOf(false) }
    var confirm by rememberSaveable(e.id) { mutableStateOf(false) }
    val settle = e.kind == "settle"
    val to = e.shares.keys.firstOrNull()
    val myShare = meId?.let { e.shares[it] } ?: 0.0
    val rotation by animateFloatAsState(if (open) 90f else 0f, label = "chev")
    Column(Modifier.padding(top = 10.dp).fillMaxWidth().card(c.surface, c.line)) {
        Row(
            Modifier.fillMaxWidth().clickable { open = !open }.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val tint = if (settle) c.violet else c.greenInk
            Box(Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(tint.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                Icon(if (settle) Icons.Rounded.Payments else Icons.Rounded.ReceiptLong, null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(if (settle) "${label(e.paidBy)} → ${label(to ?: "")}" else e.title, style = MaterialTheme.typography.titleMedium, color = c.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    buildString {
                        append(shortDay(e.at)); append(" · ")
                        append(if (settle) "pago" else "pagó ${label(e.paidBy).let { if (it == "Tú") "tú" else it }}")
                        if (!settle && myShare > 0) append(" · tu parte ${money(myShare)}")
                    },
                    style = MaterialTheme.typography.bodySmall, color = c.ink2, maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
            Text(money(e.amount), style = MaterialTheme.typography.titleMedium, color = c.ink)
            Icon(Icons.Rounded.ChevronRight, null, tint = c.ink3, modifier = Modifier.rotate(rotation))
        }
        if (open) Column(Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp)) {
            if (!settle) {
                e.shares.forEach { (id, v) ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Avatar(members.find { it.id == id }, 24.dp)
                        Text(label(id), style = MaterialTheme.typography.bodyMedium, color = c.ink, modifier = Modifier.weight(1f).padding(start = 10.dp))
                        Text(money(v), style = MaterialTheme.typography.labelLarge, color = c.ink)
                    }
                }
                Text(if (e.mode == "items") "Cada uno paga lo que pidió." else "A partes iguales.", style = MaterialTheme.typography.bodySmall, color = c.ink2, modifier = Modifier.padding(top = 4.dp))
            }
            Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (confirm) { AppButton("No", { confirm = false }); AppButton("Sí, borrar", onDelete, danger = true) }
                else AppButton("Borrar", { confirm = true }, danger = true, leading = { Icon(Icons.Rounded.Delete, null, Modifier.size(18.dp)) })
            }
        }
    }
}

@Composable
private fun ExpenseSheet(vm: AppViewModel, state: UserState, onDismiss: () -> Unit) {
    val c = LocalColors.current
    val haptics = rememberHaptics()
    val members = state.list.members
    val meId = state.meId
    var title by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var paidBy by rememberSaveable { mutableStateOf(if (members.any { it.id == meId }) meId else members.firstOrNull()?.id) }
    var among by rememberSaveable { mutableStateOf(members.map { it.id }) }
    val value = parseAmount(amount)
    val valid = title.isNotBlank() && value != null && value > 0 && paidBy != null && among.isNotEmpty()

    Sheet(onDismiss) {
        SheetTitle("Añadir gasto")
        Text("Para lo que no compráis con la cesta: la fruta del mercado, el pan, la bombona…", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal), color = c.ink2)
        Label("¿Qué es?")
        Input(title, { if (it.length <= 60) title = it }, "Fruta del mercado")
        Label("¿Cuánto?")
        Input(
            amount, { amount = it.filter { ch -> ch.isDigit() || ch == ',' || ch == '.' }.take(9) }, "0,00",
            style = MaterialTheme.typography.titleLarge.copy(fontFamily = Numeric), keyboard = KeyboardOptions(keyboardType = KeyboardType.Decimal), suffix = "€",
        )
        Label("¿Quién ha pagado?")
        PeoplePicker(members, meId, { it == paidBy }) { paidBy = it }
        Label("A partes iguales entre")
        PeoplePicker(members, meId, { it in among }) { id -> among = if (id in among) among - id else among + id }
        if (valid) Text("Sale a ${money(value!! / among.size)} por cabeza.", style = MaterialTheme.typography.bodySmall, color = c.ink2, modifier = Modifier.padding(top = 10.dp))
        AppButton(
            "Apuntar gasto",
            { vm.repo.addExpense(title.trim(), value!!, paidBy!!, among); haptics.success(); vm.repo.toast("Gasto apuntado"); onDismiss() },
            Modifier.fillMaxWidth().padding(top = 22.dp).heightIn(min = 56.dp), primary = true, enabled = valid,
            leading = { Icon(Icons.Rounded.Check, null, Modifier.size(20.dp)) },
        )
    }
}
