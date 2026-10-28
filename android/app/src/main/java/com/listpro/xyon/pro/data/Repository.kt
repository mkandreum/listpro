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

    val isShared: Boolean get() = _state.value.sync.code != null

    /** Si la lista es compartida, apunta que este producto lo he pedido yo. */
    private fun ListData.claim(id: String): Map<String, String> {
        val me = _state.value.meId
        return if (me != null && isShared && (basket[id] ?: 0) == 0) addedBy + (id to me) else addedBy
    }

    fun changeQty(id: String, delta: Int) = editList { l ->
        val next = ((l.basket[id] ?: 0) + delta).coerceAtLeast(0)
        if (next == 0) {
            l.copy(basket = l.basket - id, addedPrices = l.addedPrices - id, inCart = l.inCart - id, addedBy = l.addedBy - id, pickedBy = l.pickedBy - id)
        } else {
            val price = _catalog.value?.products?.get(id)?.price
            l.copy(
                basket = l.basket + (id to next),
                addedPrices = if (price != null && id !in l.addedPrices) l.addedPrices + (id to price) else l.addedPrices,
                addedBy = l.claim(id),
            )
        }
    }

    fun toggleInCart(id: String) = editList { l ->
        val me = _state.value.meId
        if (id in l.inCart) l.copy(inCart = l.inCart - id, pickedBy = l.pickedBy - id)
        else l.copy(inCart = l.inCart + id, pickedBy = if (me != null) l.pickedBy + (id to me) else l.pickedBy)
    }

    fun clearBasket() = editList { it.copy(basket = emptyMap(), addedPrices = emptyMap(), inCart = emptyList(), addedBy = emptyMap(), pickedBy = emptyMap()) }

    fun dropMissing() = editList { l -> l.copy(basket = l.basket.filterKeys { _catalog.value?.products?.containsKey(it) == true }) }

    fun basketLines(list: ListData = _state.value.list, idx: CatalogIndex? = _catalog.value): List<BasketLine> {
        idx ?: return emptyList()
        return list.basket.mapNotNull { (id, q) -> idx.products[id]?.let { BasketLine(it, q) } }
            .sortedWith(compareBy({ idx.aisleOrder[it.product.subcategory] ?: Int.MAX_VALUE }, { it.product.name }))
    }

    /** Lo que entra en la compra: si has marcado algo, solo lo marcado (lo demás se queda en la cesta); si no, todo. */
    fun checkoutLines(): List<BasketLine> {
        val lines = basketLines()
        val picked = lines.filter { it.product.id in _state.value.list.inCart }
        return picked.ifEmpty { lines }
    }

    /** Cuánto le toca a cada uno. Con "por producto", lo que pidió cada uno; lo que no es de nadie, a partes iguales. */
    fun purchaseShares(amount: Double, mode: String, among: List<String>): Map<String, Double> {
        if (among.isEmpty()) return emptyMap()
        if (mode == "equal") return splitEqual(amount, among)
        val w = among.associateWith { 0.0 }.toMutableMap()
        val addedBy = _state.value.list.addedBy
        checkoutLines().forEach { l ->
            val cost = l.product.price * l.qty
            val who = addedBy[l.product.id]
            if (who != null && who in w) w[who] = w.getValue(who) + cost
            else among.forEach { m -> w[m] = w.getValue(m) + cost / among.size }
        }
        return splitByWeights(amount, w)
    }

    /** Guarda la compra en el historial, la quita de la cesta y, si se reparte, la apunta en las cuentas. */
    fun finishShop(name: String, amount: Double, paidBy: String?, mode: String?, among: List<String>): Boolean {
        val lines = checkoutLines()
        if (lines.isEmpty()) return false
        val now = Instant.now().toString()
        val basket = SavedBasket(
            id = newId(),
            name = name,
            date = now,
            total = round(lines.sumOf { it.product.price * it.qty } * 100) / 100,
            items = lines.associate { it.product.id to it.qty },
            snapshot = lines.associate { it.product.id to SnapshotItem(it.product.name, it.product.price, it.product.section, it.product.category) },
        )
        val ids = lines.map { it.product.id }.toSet()
        val expense = if (isShared && paidBy != null && mode != null && among.isNotEmpty() && amount > 0) Expense(
            id = newId(), at = now, title = name, amount = round(amount * 100) / 100, paidBy = paidBy,
            shares = purchaseShares(amount, mode, among), kind = "purchase", mode = mode, basketId = basket.id,
        ) else null
        editList {
            it.copy(
                savedBaskets = it.savedBaskets + basket,
                basket = it.basket - ids, addedPrices = it.addedPrices - ids, inCart = it.inCart - ids,
                addedBy = it.addedBy - ids, pickedBy = it.pickedBy - ids,
                expenses = if (expense != null) it.expenses + expense else it.expenses,
            )
        }
        return true
    }

    private fun newId() = UUID.randomUUID().toString().replace("-", "").take(12)

    // --- Cuentas ---
    fun addExpense(title: String, amount: Double, paidBy: String, among: List<String>) = editList {
        val a = round(amount * 100) / 100
        it.copy(expenses = it.expenses + Expense(newId(), Instant.now().toString(), title, a, paidBy, splitEqual(a, among), "purchase", "equal"))
    }

    /** Apunta que `from` le ha pagado `amount` a `to`. */
    fun settleUp(from: String, to: String, amount: Double) = editList {
        it.copy(expenses = it.expenses + Expense(newId(), Instant.now().toString(), "Pago", amount, from, mapOf(to to amount), "settle"))
    }

    fun deleteExpense(id: String) = editList { l -> l.copy(expenses = l.expenses.filterNot { it.id == id }) }

    // --- Miembros ---
    /** Me apunta (o me renombra) en la lista con este nombre. */
    fun upsertMe(name: String) {
        val clean = name.trim().take(24)
        if (clean.isEmpty()) return
        val meId = _state.value.meId ?: newId().also { id -> _state.update { it.copy(meId = id) } }
        editList { l ->
            if (l.members.any { it.id == meId }) l.copy(members = l.members.map { if (it.id == meId) it.copy(name = clean) else it })
            else {
                val used = l.members.map { it.hue }.toSet()
                val hue = MEMBER_HUES.firstOrNull { it !in used } ?: MEMBER_HUES[l.members.size % MEMBER_HUES.size]
                l.copy(members = l.members + Member(meId, clean, hue, Instant.now().toString()))
            }
        }
    }

    fun reuseBasket(id: String): Int {
        val b = _state.value.list.savedBaskets.find { it.id == id } ?: return 0
        val idx = _catalog.value ?: return 0
        var n = 0
        editList { l ->
            val basket = l.basket.toMutableMap()
            val added = l.addedPrices.toMutableMap()
            val by = l.addedBy.toMutableMap()
            val me = _state.value.meId
            b.items.forEach { (pid, q) ->
                val p = idx.products[pid] ?: return@forEach
                if ((basket[pid] ?: 0) == 0 && me != null && isShared) by[pid] = me
                basket[pid] = (basket[pid] ?: 0) + q
                added.putIfAbsent(pid, p.price)
                n++
            }
            l.copy(basket = basket, addedPrices = added, addedBy = by)
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

    // Cada cambio local sube la revisión: un cambio hecho mientras se sube otro no se pierde.
    @Volatile private var rev = 0L
    private var pushJob: Job? = null
    private val pushLock = Mutex()

    private fun markDirty() {
        rev++
        val sync = _state.value.sync
        if (sync.code == null) return
        setSync(sync.copy(dirty = true))
        pushJob?.cancel()
        pushJob = scope.launch { delay(700); pushSync() }
    }

    /** Pone en local lo que viene del servidor (sin marcarlo como cambio nuestro). */
    private fun applyRemote(data: ListData, base: ListData?, sync: SyncState) {
        _state.update { st ->
            val plan = if (data.planId == null) null else st.plan
            st.copy(list = data, syncBase = base, sync = sync, plan = plan)
        }
        persist()
    }

    suspend fun pushSync() {
        if (!pushLock.tryLock()) return
        try {
            val code = _state.value.sync.code ?: return
            if (!_state.value.sync.dirty) return
            var updatedAt = _state.value.sync.updatedAt
            runCatching {
                for (attempt in 0 until 4) {
                    val sentRev = rev
                    val sent = _state.value.list
                    when (val res = api.putList(code, sent, updatedAt, force = attempt == 3)) {
                        is Api.PutResult.Conflict -> {
                            // Alguien cambió la lista mientras tanto: juntamos sus cambios con los nuestros y reintentamos
                            val merged = merge3(_state.value.syncBase, _state.value.list, res.remote.data)
                            applyRemote(merged, res.remote.data, _state.value.sync)
                            updatedAt = res.remote.updatedAt
                            continue
                        }
                        is Api.PutResult.Ok -> {
                            val again = rev != sentRev
                            applyRemote(_state.value.list, sent, SyncState(code, res.updatedAt, dirty = again))
                            if (again) { pushJob?.cancel(); pushJob = scope.launch { delay(300); pushSync() } }
                        }
                        Api.PutResult.NotFound -> {
                            toast("Esa lista compartida ya no existe")
                            _state.update { it.copy(sync = SyncState(), syncBase = null) }
                            persist()
                        }
                    }
                    break
                }
            }
        } finally {
            pushLock.unlock()
        }
        syncPlan()
    }

    suspend fun pullSync() {
        val sync = _state.value.sync
        val code = sync.code ?: return
        if (sync.dirty) return pushSync()
        runCatching {
            val remote = api.getList(code)
            if (remote.updatedAt != sync.updatedAt && !_state.value.sync.dirty && !pushLock.isLocked) {
                applyRemote(remote.data, remote.data, SyncState(code, remote.updatedAt, false))
                syncPlan()
            }
        }
    }

    /** Crea una lista compartida con lo que hay ahora en la cesta y me apunta con mi nombre. */
    suspend fun createSync(name: String): String {
        upsertMe(name)
        val data = _state.value.list
        val (code, updatedAt) = api.createList(data)
        _state.update { it.copy(sync = SyncState(code, updatedAt, false), syncBase = data) }
        persist()
        return code
    }

    /** Me uno a la lista de otro: me quedo con su cesta, junto las compras guardadas y me apunto. */
    suspend fun joinSync(raw: String, name: String) {
        val code = raw.trim().uppercase()
        require(Regex("^[A-Z2-9]{8}$").matches(code)) { "El código tiene 8 letras y números" }
        if (code == _state.value.sync.code) return
        val remote = try {
            api.getList(code)
        } catch (e: HttpException) {
            throw IllegalStateException(if (e.code == 404) "No existe ninguna lista con ese código" else "No se pudo conectar")
        } catch (e: Exception) {
            throw IllegalStateException("No se pudo conectar")
        }
        val theirs = remote.data.savedBaskets.map { it.id }.toSet()
        val merged = remote.data.copy(savedBaskets = (remote.data.savedBaskets + _state.value.list.savedBaskets.filter { it.id !in theirs }).sortedBy { it.date })
        applyRemote(merged, remote.data, SyncState(code, remote.updatedAt, false))
        upsertMe(name)
        syncPlan()
    }

    fun leaveSync() {
        _state.update { it.copy(sync = SyncState(), syncBase = null) }
        persist()
    }

    // --- Planes de cenas ---
    suspend fun generatePlan(prefs: PlanPrefs): MealPlan {
        _state.update { it.copy(list = it.list.copy(planPrefs = prefs)) }
        val plan = api.createPlan(prefs)
        _state.update { it.copy(plan = plan, list = it.list.copy(planId = plan.id, planPrefs = prefs)) }
        persist()
        markDirty()
        return plan
    }

    fun clearPlan() {
        _state.update { it.copy(plan = null, list = it.list.copy(planId = null)) }
        persist()
        markDirty()
    }

    /** Mete en la cesta los envases que hacen falta (de todo el plan o de una cena). */
    fun addToBasket(items: List<Pair<String, Int>>): Int {
        val idx = _catalog.value ?: return 0
        var n = 0
        editList { l ->
            val basket = l.basket.toMutableMap()
            val added = l.addedPrices.toMutableMap()
            val by = l.addedBy.toMutableMap()
            val me = _state.value.meId
            items.forEach { (id, packs) ->
                val p = idx.products[id] ?: return@forEach
                if ((basket[id] ?: 0) == 0 && me != null && isShared) by[id] = me
                basket[id] = maxOf(basket[id] ?: 0, packs)
                added.putIfAbsent(id, p.price)
                n++
            }
            l.copy(basket = basket, addedPrices = added, addedBy = by)
        }
        return n
    }

    /** Si otro dispositivo creó un plan, lo descarga. */
    private suspend fun syncPlan() {
        val id = _state.value.list.planId ?: return
        if (_state.value.plan?.id == id) return
        runCatching { api.getPlan(id) }.onSuccess { plan -> _state.update { it.copy(plan = plan) }; persist() }
    }
}
