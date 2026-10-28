package com.listpro.xyon.pro.data

import kotlin.math.floor
import kotlin.math.roundToLong

val MEMBER_HUES = listOf("#22C55E", "#7C5CFF", "#FF8A2A", "#FF5FA8", "#3BA7FF", "#E5B800", "#14B8A6", "#E5484D")

fun initials(name: String): String =
    name.trim().split(Regex("\\s+")).take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("").ifEmpty { "?" }

private fun cents(v: Double) = (v * 100).roundToLong()

/** Reparte `total` según pesos, en céntimos exactos: lo que sobra del redondeo va a los restos más grandes. */
fun splitByWeights(total: Double, weights: Map<String, Double>): Map<String, Double> {
    val ids = weights.filterValues { it > 0 }.keys.toList()
    val sum = ids.sumOf { weights.getValue(it) }
    if (ids.isEmpty() || sum <= 0) return emptyMap()
    val t = cents(total)
    val raw = ids.map { k -> val v = t * weights.getValue(k) / sum; Triple(k, floor(v).toLong(), v - floor(v)) }
    val out = raw.associate { it.first to it.second }.toMutableMap()
    var left = t - out.values.sum()
    for ((k, _, _) in raw.sortedByDescending { it.third }) {
        if (left <= 0) break
        out[k] = out.getValue(k) + 1; left--
    }
    return out.mapValues { it.value / 100.0 }
}

fun splitEqual(total: Double, among: List<String>) = splitByWeights(total, among.associateWith { 1.0 })

/** Saldo de cada miembro: positivo = le deben; negativo = debe. */
fun balances(expenses: List<Expense>, members: List<Member>): Map<String, Double> {
    val b = LinkedHashMap<String, Long>()
    members.forEach { b[it.id] = 0 }
    expenses.forEach { e ->
        b[e.paidBy] = (b[e.paidBy] ?: 0) + cents(e.amount)
        e.shares.forEach { (m, v) -> b[m] = (b[m] ?: 0) - cents(v) }
    }
    return b.mapValues { it.value / 100.0 }
}

data class Transfer(val from: String, val to: String, val amount: Double)

/** Los pagos mínimos para quedar en paz: el que más debe paga al que más le deben, y así. */
fun settlements(bal: Map<String, Double>): List<Transfer> {
    val debt = bal.filterValues { it < -0.004 }.map { it.key to -cents(it.value) }.toMutableList()
    val cred = bal.filterValues { it > 0.004 }.map { it.key to cents(it.value) }.toMutableList()
    val out = mutableListOf<Transfer>()
    while (debt.isNotEmpty() && cred.isNotEmpty()) {
        debt.sortByDescending { it.second }
        cred.sortByDescending { it.second }
        val (dk, dc) = debt[0]
        val (ck, cc) = cred[0]
        val x = minOf(dc, cc)
        out += Transfer(dk, ck, x / 100.0)
        if (dc - x == 0L) debt.removeAt(0) else debt[0] = dk to dc - x
        if (cc - x == 0L) cred.removeAt(0) else cred[0] = ck to cc - x
    }
    return out
}

// ───────── Fusión a tres bandas (igual que en la web) ─────────
// Si dos personas tocan la lista a la vez, cada una se compara con la última versión común:
// lo que cambió aquí se queda; lo que no, se toma del servidor.

private fun <T> mergeMap(base: Map<String, T>, local: Map<String, T>, remote: Map<String, T>): Map<String, T> {
    val out = LinkedHashMap<String, T>()
    for (k in local.keys + remote.keys + base.keys) {
        val v = if (local[k] != base[k]) local[k] else remote[k]
        if (v != null) out[k] = v
    }
    return out
}

private fun mergeSet(base: List<String>, local: List<String>, remote: List<String>): List<String> {
    val b = base.toSet(); val l = local.toSet(); val r = remote.toSet()
    return (local + remote + base).distinct().filter { k -> if ((k in l) != (k in b)) k in l else k in r }
}

private fun <T> mergeById(base: List<T>, local: List<T>, remote: List<T>, id: (T) -> String): List<T> {
    val merged = mergeMap(base.associateBy(id), local.associateBy(id), remote.associateBy(id))
    return (local.map(id) + remote.map(id)).distinct().mapNotNull { merged[it] }
}

fun merge3(base: ListData?, local: ListData, remote: ListData): ListData {
    val b = base ?: ListData()
    return ListData(
        basket = mergeMap(b.basket, local.basket, remote.basket),
        addedPrices = mergeMap(b.addedPrices, local.addedPrices, remote.addedPrices),
        inCart = mergeSet(b.inCart, local.inCart, remote.inCart),
        savedBaskets = mergeById(b.savedBaskets, local.savedBaskets, remote.savedBaskets) { it.id }.sortedBy { it.date },
        planId = if (local.planId != b.planId) local.planId else remote.planId,
        planPrefs = if (local.planPrefs != b.planPrefs) local.planPrefs else remote.planPrefs,
        members = mergeById(b.members, local.members, remote.members) { it.id },
        addedBy = mergeMap(b.addedBy, local.addedBy, remote.addedBy),
        pickedBy = mergeMap(b.pickedBy, local.pickedBy, remote.pickedBy),
        expenses = mergeById(b.expenses, local.expenses, remote.expenses) { it.id }.sortedBy { it.at },
    )
}
