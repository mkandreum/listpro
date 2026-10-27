package com.listpro.xyon.pro.data

import android.content.Context
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.util.UUID
import kotlin.math.round

/** Catálogo con índices para no recorrer 4.000 productos en cada pantalla. */
@Immutable
class CatalogIndex(val catalog: Catalog) {
    val products: Map<String, Product> = catalog.products.associateBy { it.id }
    val sections: Map<String, Section> = catalog.sections.associateBy { it.id }
    val categories: Map<Int, Pair<Category, Section>> = buildMap {
        catalog.sections.forEach { s -> s.categories.forEach { c -> put(c.id, c to s) } }
    }
    val subcategories: Map<Int, Subcategory> = buildMap {
        catalog.sections.forEach { s -> s.categories.forEach { c -> c.subcategories.forEach { put(it.id, it) } } }
    }
    /** Orden de pasillo en la tienda, para ordenar la cesta. */
    val aisleOrder: Map<Int, Int> = buildMap {
        var i = 0
        catalog.sections.forEach { s -> s.categories.forEach { c -> c.subcategories.forEach { put(it.id, i++) } } }
    }
    val byCategory: Map<Int, List<Product>> = catalog.products.groupBy { it.category }
    val deals: List<Product> = catalog.products.filter { (it.delta ?: 0.0) < 0 }
        .sortedBy { it.delta!! / it.previousPrice!! }
    val newProducts: List<Product> = catalog.products.filter { it.isNew }
}

data class BasketLine(val product: Product, val qty: Int)

class Repository(context: Context, val api: Api = Api()) {
    private val dir = context.filesDir
    private val catalogFile = File(dir, "catalog.json")
    private val etagFile = File(dir, "catalog.etag")
    private val stateFile = File(dir, "state.json")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val ioLock = Mutex()

    private val _catalog = MutableStateFlow<CatalogIndex?>(null)
    val catalog: StateFlow<CatalogIndex?> = _catalog.asStateFlow()

    private val _catalogError = MutableStateFlow(false)
    val catalogError: StateFlow<Boolean> = _catalogError.asStateFlow()

    private val _state = MutableStateFlow(UserState())
    val state: StateFlow<UserState> = _state.asStateFlow()

    private val _messages = MutableStateFlow<List<String>>(emptyList())
    val messages: StateFlow<List<String>> = _messages.asStateFlow()

    init {
        scope.launch {
            runCatching { _state.value = api.json.decodeFromString(UserState.serializer(), stateFile.readText()) }
            runCatching { _catalog.value = CatalogIndex(api.json.decodeFromString(Catalog.serializer(), catalogFile.readText())) }
            refreshCatalog()
        }
    }

    fun toast(text: String) = _messages.update { it + text }
    fun consumeMessage(text: String) = _messages.update { it - text }

    // --- Catálogo ---
    /** Descarga el catálogo si cambió. Devuelve true si hay precios nuevos. */
    suspend fun refreshCatalog(): Boolean = ioLock.withLock {
        try {
            val etag = if (_catalog.value != null) etagFile.takeIf { it.exists() }?.readText() else null
            val fresh = api.catalog(etag) ?: return@withLock false.also { _catalogError.value = false }
            val parsed = withContext(Dispatchers.Default) { api.json.decodeFromString(Catalog.serializer(), fresh.first) }
            val changed = _catalog.value?.catalog?.version?.let { it != parsed.version } ?: false
            _catalog.value = CatalogIndex(parsed)
            _catalogError.value = false
            withContext(Dispatchers.IO) {
                catalogFile.writeText(fresh.first)
                fresh.second?.let { etagFile.writeText(it) }
            }
            changed
        } catch (e: Exception) {
            if (_catalog.value == null) _catalogError.value = true
            false
        }
    }

    // --- Cesta ---
    private fun editList(transform: (ListData) -> ListData) {
        _state.update { it.copy(list = transform(it.list)) }
        persist()
        markDirty()
    }

    fun changeQty(id: String, delta: Int) = editList { l ->
        val next = ((l.basket[id] ?: 0) + delta).coerceAtLeast(0)
        if (next == 0) {
            l.copy(basket = l.basket - id, addedPrices = l.addedPrices - id, inCart = l.inCart - id)
        } else {
            val price = _catalog.value?.products?.get(id)?.price
            l.copy(
                basket = l.basket + (id to next),
                addedPrices = if (price != null && id !in l.addedPrices) l.addedPrices + (id to price) else l.addedPrices,
            )
        }
    }

    fun toggleInCart(id: String) = editList { l -> l.copy(inCart = if (id in l.inCart) l.inCart - id else l.inCart + id) }

    fun clearBasket() = editList { it.copy(basket = emptyMap(), addedPrices = emptyMap(), inCart = emptyList()) }

    fun dropMissing() = editList { l -> l.copy(basket = l.basket.filterKeys { _catalog.value?.products?.containsKey(it) == true }) }

    fun basketLines(list: ListData = _state.value.list, idx: CatalogIndex? = _catalog.value): List<BasketLine> {
        idx ?: return emptyList()
        return list.basket.mapNotNull { (id, q) -> idx.products[id]?.let { BasketLine(it, q) } }
            .sortedWith(compareBy({ idx.aisleOrder[it.product.subcategory] ?: Int.MAX_VALUE }, { it.product.name }))
    }

    fun saveBasket(name: String): Boolean {
        val lines = basketLines()
        if (lines.isEmpty()) return false
        val basket = SavedBasket(
            id = UUID.randomUUID().toString().take(12),
            name = name,
            date = Instant.now().toString(),
            total = round(lines.sumOf { it.product.price * it.qty } * 100) / 100,
            items = lines.associate { it.product.id to it.qty },
            snapshot = lines.associate { it.product.id to SnapshotItem(it.product.name, it.product.price, it.product.section, it.product.category) },
        )
        editList { it.copy(savedBaskets = it.savedBaskets + basket, basket = emptyMap(), addedPrices = emptyMap(), inCart = emptyList()) }
        return true
    }

    fun reuseBasket(id: String): Int {
        val b = _state.value.list.savedBaskets.find { it.id == id } ?: return 0
        val idx = _catalog.value ?: return 0
        var n = 0
        editList { l ->
            val basket = l.basket.toMutableMap()
            val added = l.addedPrices.toMutableMap()
            b.items.forEach { (pid, q) ->
                val p = idx.products[pid] ?: return@forEach
                basket[pid] = (basket[pid] ?: 0) + q
                added.putIfAbsent(pid, p.price)
                n++
            }
            l.copy(basket = basket, addedPrices = added)
        }
        return n
    }

    fun deleteBasket(id: String) = editList { l -> l.copy(savedBaskets = l.savedBaskets.filterNot { it.id == id }) }

    fun setTheme(theme: ThemePref) {
        _state.update { it.copy(theme = theme) }
        persist()
    }

    // --- Persistencia ---
    private var persistJob: Job? = null
    private fun persist() {
        persistJob?.cancel()
        persistJob = scope.launch(Dispatchers.IO) {
            delay(250)
            stateFile.writeText(api.json.encodeToString(UserState.serializer(), _state.value))
        }
    }

    fun exportBackup(): String = api.json.encodeToString(ListData.serializer(), _state.value.list)

    fun importBackup(text: String) {
        val data = api.json.decodeFromString(ListData.serializer(), text)
        editList { it.copy(savedBaskets = data.savedBaskets, basket = data.basket.ifEmpty { it.basket }) }
    }

    // --- Sincronización (mismo protocolo que la web) ---
    private fun setSync(sync: SyncState) {
        _state.update { it.copy(sync = sync) }
        persist()
    }

    private var pushJob: Job? = null
    private fun markDirty() {
        val sync = _state.value.sync
        if (sync.code == null) return
        setSync(sync.copy(dirty = true))
        pushJob?.cancel()
        pushJob = scope.launch { delay(1200); pushSync() }
    }

    // Si dos dispositivos escriben a la vez: cestas guardadas = unión por id; cesta actual = la de este dispositivo.
    private fun mergeSaved(local: List<SavedBasket>, remote: List<SavedBasket>): List<SavedBasket> {
        val ids = local.map { it.id }.toSet()
        return (local + remote.filter { it.id !in ids }).sortedBy { it.date }
    }

    suspend fun pushSync() {
        val sync = _state.value.sync
        val code = sync.code ?: return
        if (!sync.dirty) return
        runCatching {
            var res = api.putList(code, _state.value.list, sync.updatedAt)
            if (res is Api.PutResult.Conflict) {
                val merged = mergeSaved(_state.value.list.savedBaskets, res.remote.data.savedBaskets)
                _state.update { it.copy(list = it.list.copy(savedBaskets = merged)) }
                res = api.putList(code, _state.value.list, null, force = true)
            }
            when (res) {
                is Api.PutResult.Ok -> setSync(SyncState(code, res.updatedAt, dirty = false))
                Api.PutResult.NotFound -> { toast("Ese código de sincronización ya no existe"); setSync(SyncState()) }
                is Api.PutResult.Conflict -> Unit
            }
        }
    }

    suspend fun pullSync() {
        val sync = _state.value.sync
        val code = sync.code ?: return
        if (sync.dirty) return pushSync()
        runCatching {
            val remote = api.getList(code)
            if (remote.updatedAt != sync.updatedAt && !_state.value.sync.dirty) {
                _state.update { it.copy(list = remote.data, sync = SyncState(code, remote.updatedAt, false)) }
                persist()
            }
        }
    }

    suspend fun createSync(): String {
        val (code, updatedAt) = api.createList(_state.value.list)
        setSync(SyncState(code, updatedAt, false))
        return code
    }

    /** Une este dispositivo a una lista. Lanza una excepción con un mensaje para el usuario si falla. */
    suspend fun joinSync(raw: String) {
        val code = raw.trim().uppercase()
        require(Regex("^[A-Z2-9]{8}$").matches(code)) { "El código tiene 8 letras y números" }
        val remote = try {
            api.getList(code)
        } catch (e: HttpException) {
            throw IllegalStateException(if (e.code == 404) "No existe ningún código así" else "No se pudo conectar")
        } catch (e: Exception) {
            throw IllegalStateException("No se pudo conectar")
        }
        val merged = remote.data.copy(savedBaskets = mergeSaved(_state.value.list.savedBaskets, remote.data.savedBaskets))
        _state.update { it.copy(list = merged, sync = SyncState(code, remote.updatedAt, dirty = true)) }
        persist()
        pushSync()
    }

    fun leaveSync() = setSync(SyncState())
}
