package com.listpro.xyon.pro.data

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class Subcategory(
    val id: Int,
    val name: String,
    val emoji: String = "🛒",
    val count: Int = 0,
    val image: String? = null,
)

@Immutable
@Serializable
data class Category(
    val id: Int,
    val name: String,
    val emoji: String = "🛒",
    val count: Int = 0,
    val images: List<String> = emptyList(),
    val subcategories: List<Subcategory> = emptyList(),
)

@Immutable
@Serializable
data class Section(
    val id: String,
    val name: String,
    val emoji: String = "🛒",
    val hue: String = "#B9BCC4",
    val icon: String = "bag",
    val blurb: String = "",
    val count: Int = 0,
    val images: List<String> = emptyList(),
    val categories: List<Category> = emptyList(),
)

@Immutable
@Serializable
data class Product(
    val id: String,
    val name: String,
    val size: String = "",
    val section: String,
    val category: Int,
    val subcategory: Int,
    val group: String = "",
    val price: Double,
    val refPrice: Double? = null,
    val refFormat: String? = null,
    val previousPrice: Double? = null,
    val priceChangedAt: String? = null,
    val image: String? = null,
    val url: String? = null,
    val isNew: Boolean = false,
) {
    /** Diferencia con el precio anterior; null si no ha cambiado. */
    val delta: Double?
        get() = previousPrice?.let { prev -> (price - prev).takeIf { kotlin.math.abs(it) >= 0.005 } }
}

@Immutable
@Serializable
data class Catalog(
    val version: String,
    val updatedAt: String? = null,
    val warehouse: String? = null,
    val count: Int = 0,
    val sections: List<Section> = emptyList(),
    val products: List<Product> = emptyList(),
)

@Serializable
data class CatalogMeta(val version: String, val updatedAt: String? = null)

@Serializable
data class SnapshotItem(val name: String, val price: Double, val section: String? = null, val category: Int? = null)

@Immutable
@Serializable
data class SavedBasket(
    val id: String,
    val name: String,
    val date: String,
    val total: Double,
    val items: Map<String, Int>,
    val snapshot: Map<String, SnapshotItem>? = null,
)

/** Lo que se sincroniza entre dispositivos: mismo formato que la web. */
@Serializable
data class ListData(
    val basket: Map<String, Int> = emptyMap(),
    val addedPrices: Map<String, Double> = emptyMap(),
    val inCart: List<String> = emptyList(),
    val savedBaskets: List<SavedBasket> = emptyList(),
    /** Plan de cenas activo y preferencias: así se ve también en los otros dispositivos. */
    val planId: String? = null,
    val planPrefs: PlanPrefs? = null,
)

@Serializable
data class SyncState(val code: String? = null, val updatedAt: String? = null, val dirty: Boolean = false)

@Serializable
data class PricePoint(val price: Double, val at: String)

@Serializable
data class PriceHistory(val id: String, val name: String, val price: Double, val history: List<PricePoint>)

enum class ThemePref { System, Light, Dark }

/** Estado local persistido en disco. */
@Serializable
data class UserState(
    val list: ListData = ListData(),
    val sync: SyncState = SyncState(),
    val theme: ThemePref = ThemePref.System,
    val plan: MealPlan? = null,
)

// ───────────── Planes de cenas ─────────────

@Serializable
data class PlanPrefs(
    val budget: Int = 45,
    val people: Int = 2,
    val nights: Int = 5,
    val maxMinutes: Int = 40,
    val diets: List<String> = listOf("omnivora"),
    val appliances: List<String> = listOf("fuego", "microondas"),
    val likes: String = "",
    val dislikes: String = "",
    val allergies: String = "",
    val pantry: String = "",
    val store: String = "mercadona",
)

@Immutable
@Serializable
data class Nutrition(val kcal: Int = 0, val protein: Int = 0, val carbs: Int = 0, val fat: Int = 0, val fiber: Int = 0, val salt: Double = 0.0)

@Immutable
@Serializable
data class PlanIngredient(
    val productId: String? = null,
    val name: String,
    val amount: String = "",
    val fraction: Double = 0.0,
    val fromPantry: Boolean = false,
    val image: String? = null,
    val price: Double? = null,
    val size: String? = null,
)

@Immutable
@Serializable
data class PlanRecipe(
    val day: String,
    val name: String,
    val description: String = "",
    val minutes: Int = 0,
    val difficulty: String = "fácil",
    val appliances: List<String> = emptyList(),
    val servings: Int = 1,
    val nutrition: Nutrition = Nutrition(),
    val ingredients: List<PlanIngredient> = emptyList(),
    val steps: List<String> = emptyList(),
    val tip: String = "",
    val cost: Double = 0.0,
    val costPerServing: Double = 0.0,
)

@Immutable
@Serializable
data class ShoppingItem(
    val productId: String,
    val name: String,
    val size: String = "",
    val image: String? = null,
    val price: Double,
    val packs: Int,
    val cost: Double,
    val usedIn: List<String> = emptyList(),
)

@Serializable
data class PlanTotals(
    val shopping: Double = 0.0,
    val consumed: Double = 0.0,
    val perServing: Double = 0.0,
    val kcalAvg: Int = 0,
    val proteinAvg: Int = 0,
    val budget: Double = 0.0,
    val overBudget: Boolean = false,
)

@Immutable
@Serializable
data class MealPlan(
    val id: String,
    val createdAt: String,
    val prefs: PlanPrefs = PlanPrefs(),
    val title: String = "",
    val summary: String = "",
    val recipes: List<PlanRecipe> = emptyList(),
    val shopping: List<ShoppingItem> = emptyList(),
    val totals: PlanTotals = PlanTotals(),
    val warnings: List<String> = emptyList(),
)

@Serializable
data class PlannerOptions(val enabled: Boolean = false)
