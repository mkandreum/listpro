package com.listpro.xyon.pro.ui

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.listpro.xyon.pro.ListProApp
import com.listpro.xyon.pro.data.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface Route {
    data object Home : Route
    data class Search(val query: String) : Route
    data class Deals(val newOnes: Boolean) : Route
    data class CategoryRoute(val id: Int) : Route
    data object Basket : Route
    data object Stats : Route
    data object Settings : Route
}

enum class Tab { Products, Basket, Stats, Settings }

fun Route.tab(): Tab = when (this) {
    Route.Basket -> Tab.Basket
    Route.Stats -> Tab.Stats
    Route.Settings -> Tab.Settings
    else -> Tab.Products
}

class AppViewModel(app: Application) : AndroidViewModel(app) {
    val repo = (app as ListProApp).repository
    val catalog = repo.catalog
    val catalogError = repo.catalogError
    val state = repo.state
    val messages = repo.messages

    /** Pila de navegación propia: pocas pantallas y el Back del sistema la recorre. */
    val backStack = mutableStateListOf<Route>(Route.Home)
    val current: Route get() = backStack.last()

    private val _sheet = MutableStateFlow<Product?>(null)
    val sheet = _sheet.asStateFlow()

    private val _nfcListening = MutableStateFlow(false)
    val nfcListening = _nfcListening.asStateFlow()

    fun open(route: Route) {
        if (route is Route.Search && current is Route.Search) backStack[backStack.lastIndex] = route
        else backStack.add(route)
    }

    fun selectTab(tab: Tab) {
        if (current.tab() == tab) {
            if (tab == Tab.Products) { backStack.clear(); backStack.add(Route.Home) }
            return
        }
        backStack.clear()
        backStack.add(Route.Home)
        when (tab) {
            Tab.Products -> Unit
            Tab.Basket -> backStack.add(Route.Basket)
            Tab.Stats -> backStack.add(Route.Stats)
            Tab.Settings -> backStack.add(Route.Settings)
        }
    }

    fun back(): Boolean {
        if (backStack.size <= 1) return false
        backStack.removeAt(backStack.lastIndex)
        return true
    }

    fun showProduct(p: Product?) { _sheet.value = p }

    fun changeQty(id: String, delta: Int) = repo.changeQty(id, delta)

    fun refresh() = viewModelScope.launch {
        if (repo.refreshCatalog()) repo.toast("Precios actualizados")
        repo.pullSync()
    }

    fun pullSync() = viewModelScope.launch { repo.pullSync() }

    fun runAction(ok: String, block: suspend () -> Unit) = viewModelScope.launch {
        try {
            block()
            repo.toast(ok)
        } catch (e: Exception) {
            repo.toast(e.message ?: "No se pudo conectar")
        }
    }

    // --- NFC ---
    /** Para compartir hace falta un código: si no hay, se crea antes de acercar los móviles. */
    fun prepareNfcShare() = runAction("Acerca el otro móvil por detrás") {
        if (state.value.sync.code == null) repo.createSync()
    }

    fun setNfcListening(on: Boolean) { _nfcListening.value = on }

    fun onNfcCode(code: String) {
        _nfcListening.value = false
        runAction("Lista recibida por NFC") { repo.joinSync(code) }
    }
}
