package com.listpro.xyon.pro

import android.content.ClipData
import android.content.ClipboardManager
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import com.listpro.xyon.pro.ui.EaseOutExpo
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.listpro.xyon.pro.nfc.NfcProtocol
import com.listpro.xyon.pro.ui.AppViewModel
import com.listpro.xyon.pro.ui.BasketScreen
import com.listpro.xyon.pro.ui.CategoryScreen
import com.listpro.xyon.pro.ui.GlassPillNav
import com.listpro.xyon.pro.ui.HomeScreen
import com.listpro.xyon.pro.ui.ListProTheme
import com.listpro.xyon.pro.ui.LocalColors
import com.listpro.xyon.pro.ui.ProductSheet
import com.listpro.xyon.pro.ui.ResultsScreen
import com.listpro.xyon.pro.ui.Route
import com.listpro.xyon.pro.ui.SettingsScreen
import com.listpro.xyon.pro.ui.StatsScreen
import com.listpro.xyon.pro.ui.Tab
import com.listpro.xyon.pro.ui.card
import com.listpro.xyon.pro.ui.tab
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val vm: AppViewModel by viewModels()
    private val nfc: NfcAdapter? by lazy { NfcAdapter.getDefaultAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition { vm.catalog.value == null && !vm.catalogError.value }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Modo lector NFC solo mientras el usuario pulsa "Recibir": si no, este móvil sigue pudiendo enviar.
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                vm.nfcListening.collect { on ->
                    val adapter = nfc ?: return@collect
                    if (on) adapter.enableReaderMode(this@MainActivity, ::onTag, NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null)
                    else adapter.disableReaderMode(this@MainActivity)
                }
            }
        }

        setContent {
            val state by vm.state.collectAsStateWithLifecycle()
            ListProTheme(state.theme) { App(vm, nfc != null) }
        }
    }

    override fun onPause() {
        super.onPause()
        nfc?.disableReaderMode(this)
    }

    private fun onTag(tag: Tag) {
        val iso = IsoDep.get(tag) ?: return
        val code = runCatching { iso.use { it.connect(); NfcProtocol.parse(it.transceive(NfcProtocol.SELECT_APDU)) } }.getOrNull()
        runOnUiThread {
            if (code != null) vm.onNfcCode(code) else vm.repo.toast("El otro móvil no tiene ninguna lista que enviar")
        }
    }

    fun copy(text: String) {
        getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText("Código ListPro", text))
    }
}

private fun depthOf(r: Route): Int = when (r) {
    is Route.CategoryRoute, is Route.Search, is Route.Deals -> 1
    else -> 0
}

@Composable
private fun App(vm: AppViewModel, nfcAvailable: Boolean) {
    val c = LocalColors.current
    val idx by vm.catalog.collectAsStateWithLifecycle()
    val error by vm.catalogError.collectAsStateWithLifecycle()
    val state by vm.state.collectAsStateWithLifecycle()
    val sheet by vm.sheet.collectAsStateWithLifecycle()
    val messages by vm.messages.collectAsStateWithLifecycle()
    val nfcListening by vm.nfcListening.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val activity = androidx.compose.ui.platform.LocalContext.current as MainActivity
    val route = vm.current
    val basket = state.list.basket

    LaunchedEffect(messages) {
        messages.firstOrNull()?.let { msg ->
            vm.repo.consumeMessage(msg)
            snackbar.showSnackbar(msg)
        }
    }

    // Al volver a la app: precios nuevos y cambios de otros dispositivos; mientras está abierta, sondeo de la lista compartida.
    LifecycleResumeEffect(Unit) {
        vm.refresh()
        val job = activity.lifecycleScope.launch { while (true) { delay(20_000); vm.pullSync() } }
        onPauseOrDispose { job.cancel() }
    }

    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        runCatching { activity.contentResolver.openOutputStream(uri)?.use { it.write(vm.repo.exportBackup().toByteArray()) } }
            .onSuccess { vm.repo.toast("Copia guardada") }.onFailure { vm.repo.toast("No se pudo guardar la copia") }
    }
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        runCatching { activity.contentResolver.openInputStream(uri)?.use { vm.repo.importBackup(it.readBytes().decodeToString()) } }
            .onSuccess { vm.repo.toast("Datos restaurados") }.onFailure { vm.repo.toast("Ese archivo no es una copia de ListPro") }
    }

    BackHandler(enabled = vm.backStack.size > 1) { vm.back() }

    Box(Modifier.fillMaxSize().background(c.ground)) {
        AnimatedContent(
            targetState = route,
            // Entrar en algo desliza desde la derecha; volver, desde la izquierda; entre pestañas, según su orden
            transitionSpec = {
                val from = depthOf(initialState)
                val to = depthOf(targetState)
                val dir = when {
                    to > from -> 1
                    to < from -> -1
                    else -> (targetState.tab().ordinal - initialState.tab().ordinal).coerceIn(-1, 1)
                }
                if (dir == 0) {
                    fadeIn(tween(220)) togetherWith fadeOut(tween(120))
                } else {
                    (slideInHorizontally(tween(380, easing = EaseOutExpo)) { w -> dir * w / 5 } + fadeIn(tween(260)))
                        .togetherWith(slideOutHorizontally(tween(380, easing = EaseOutExpo)) { w -> -dir * w / 8 } + fadeOut(tween(160)))
                }
            },
            // Escribir en el buscador cambia la ruta, pero no debe animar ni recrear la pantalla
            contentKey = { r -> when (r) { is Route.Search -> "search"; else -> r } },
            label = "screen",
        ) { r ->
            val onProduct = vm::showProduct
            val onQty = vm::changeQty
            when (r) {
                Route.Home -> HomeScreen(idx, error, basket, vm::open, onProduct, onQty, { vm.refresh() })
                is Route.CategoryRoute -> idx?.let { CategoryScreen(it, r.id, basket, { vm.back() }, onProduct, onQty) }
                is Route.Search, is Route.Deals -> idx?.let {
                    ResultsScreen(it, r, basket, onQuery = { q -> vm.open(Route.Search(q)) }, onBack = { vm.back() }, onProduct = onProduct, onQty = onQty)
                }
                Route.Basket -> BasketScreen(
                    idx, state, vm.repo.basketLines(state.list, idx), onQty, vm.repo::toggleInCart, onProduct,
                    onClear = { vm.repo.clearBasket(); vm.repo.toast("Cesta vaciada") },
                    onDropMissing = vm.repo::dropMissing,
                    onSave = { name -> vm.repo.saveBasket(name).also { if (it) vm.repo.toast("Guardada: $name") } },
                    onReuse = { id -> vm.repo.toast("${vm.repo.reuseBasket(id)} productos añadidos a la cesta") },
                    onDelete = { id -> vm.repo.deleteBasket(id); vm.repo.toast("Cesta borrada") },
                    onGoShop = { vm.selectTab(Tab.Products) },
                )
                Route.Stats -> StatsScreen(idx, state.list.savedBaskets) { vm.selectTab(Tab.Basket) }
                Route.Settings -> SettingsScreen(
                    idx, state.sync, state.theme, nfcAvailable, nfcListening,
                    onCreateSync = { vm.runAction("Código creado") { vm.repo.createSync() } },
                    onJoin = { code -> vm.runAction("Dispositivo vinculado") { vm.repo.joinSync(code) } },
                    onLeave = { vm.repo.leaveSync(); vm.repo.toast("Dispositivo desvinculado") },
                    onCopy = { activity.copy(it); vm.repo.toast("Código copiado") },
                    onNfcShare = vm::prepareNfcShare,
                    onNfcReceive = vm::setNfcListening,
                    onTheme = vm.repo::setTheme,
                    onRefresh = { vm.refresh(); vm.repo.toast("Comprobando precios…") },
                    onExport = { exporter.launch("listpro_backup.json") },
                    onImport = { importer.launch(arrayOf("application/json")) },
                )
            }
        }

        // Velo bajo la barra de estado: el contenido pasa por debajo sin mezclarse con la hora y los iconos
        Box(Modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.statusBars).background(c.ground.copy(alpha = 0.92f)))

        GlassPillNav(
            selected = route.tab(),
            basketCount = basket.values.sum(),
            onSelect = vm::selectTab,
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 100.dp)) { data ->
            Snackbar(
                Modifier.padding(horizontal = 16.dp).card(c.ink, c.line, radius = 16.dp),
                containerColor = c.ink, contentColor = c.ground,
            ) { Text(data.visuals.message) }
        }

        sheet?.let { p ->
            ProductSheet(
                p, idx?.sections?.get(p.section)?.name, idx?.subcategories?.get(p.subcategory)?.emoji ?: "🛒",
                basket[p.id] ?: 0, vm.repo.api, onQty = { vm.changeQty(p.id, it) }, onDismiss = { vm.showProduct(null) },
            )
        }
    }
}
