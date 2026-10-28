package com.listpro.xyon.pro.data

import com.listpro.xyon.pro.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class HttpException(val code: Int, val body: String?) : IOException("HTTP $code")
class AuthException(val code: Int, message: String) : IOException(message)

/** Cliente de la API de ListPro (la misma que usa la web). */
class Api(private val base: String = BuildConfig.API_BASE) {
    val json = Json { ignoreUnknownKeys = true; explicitNulls = false; encodeDefaults = true }
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    private val jsonType = "application/json; charset=utf-8".toMediaType()

    private suspend fun call(request: Request): Pair<Int, String?> = withContext(Dispatchers.IO) {
        client.newCall(request).execute().use { it.code to it.body?.string() }
    }

    private suspend inline fun <reified T> get(path: String): T {
        val (code, body) = call(Request.Builder().url(base + path).header("Cache-Control", "no-cache").build())
        if (code !in 200..299) throw HttpException(code, body)
        return json.decodeFromString(body!!)
    }

    /** Devuelve null si el servidor responde 304 (el catálogo con ese ETag sigue vigente). */
    suspend fun catalog(etag: String?): Pair<String, String?>? = withContext(Dispatchers.IO) {
        val req = Request.Builder().url("$base/api/catalog").apply { etag?.let { header("If-None-Match", it) } }.build()
        client.newCall(req).execute().use { res ->
            when {
                res.code == 304 -> null
                res.isSuccessful -> res.body!!.string() to res.header("ETag")
                else -> throw HttpException(res.code, null)
            }
        }
    }

    suspend fun catalogMeta(): CatalogMeta = get("/api/catalog/meta")

    suspend fun history(id: String): PriceHistory = get("/api/products/$id/history")

    suspend fun plannerOptions(): PlannerOptions = get("/api/planner/options")

    suspend fun getPlan(id: String): MealPlan = get("/api/plans/$id")

    @Serializable private data class PlanRequest(val prefs: PlanPrefs)
    @Serializable private data class ApiError(val error: String = "")

    // Crear un plan llama a la IA y puede tardar hasta un minuto
    private val slowClient = client.newBuilder().readTimeout(120, TimeUnit.SECONDS).callTimeout(150, TimeUnit.SECONDS).build()

    suspend fun createPlan(prefs: PlanPrefs): MealPlan = withContext(Dispatchers.IO) {
        val body = json.encodeToString(PlanRequest.serializer(), PlanRequest(prefs)).toRequestBody(jsonType)
        slowClient.newCall(Request.Builder().url("$base/api/plans").post(body).build()).execute().use { res ->
            val text = res.body?.string().orEmpty()
            if (!res.isSuccessful) {
                val msg = runCatching { json.decodeFromString(ApiError.serializer(), text).error }.getOrNull()
                throw IllegalStateException(msg?.takeIf { it.isNotBlank() } ?: "No se ha podido crear el plan. Prueba otra vez.")
            }
            json.decodeFromString(MealPlan.serializer(), text)
        }
    }

    // --- Cuentas ---
    @Serializable private data class RegisterBody(val name: String, val email: String, val password: String, val memberId: String?, val listCode: String?)
    @Serializable private data class LoginBody(val email: String, val password: String)
    @Serializable private data class MeBody(val name: String? = null, val listCode: String? = null)
    @Serializable private data class PasswordBody(val current: String, val next: String)
    @Serializable private data class DeleteBody(val password: String)

    /** Llamada con la sesión; si falla, la excepción lleva el mensaje del servidor listo para enseñarlo. */
    private suspend fun authed(method: String, path: String, body: String?, token: String?): String = withContext(Dispatchers.IO) {
        val req = Request.Builder().url(base + path)
            .method(method, body?.toRequestBody(jsonType) ?: if (method == "GET") null else "{}".toRequestBody(jsonType))
            .apply { token?.let { header("Authorization", "Bearer $it") } }
            .build()
        client.newCall(req).execute().use { res ->
            val text = res.body?.string().orEmpty()
            if (!res.isSuccessful) {
                val msg = runCatching { json.decodeFromString(ApiError.serializer(), text).error }.getOrNull()
                throw AuthException(res.code, msg?.takeIf { it.isNotBlank() } ?: "No se pudo conectar. Prueba otra vez.")
            }
            text
        }
    }

    suspend fun register(name: String, email: String, password: String, memberId: String?, listCode: String?): Session =
        json.decodeFromString(Session.serializer(), authed("POST", "/api/auth/register", json.encodeToString(RegisterBody.serializer(), RegisterBody(name, email, password, memberId, listCode)), null))

    suspend fun login(email: String, password: String): Session =
        json.decodeFromString(Session.serializer(), authed("POST", "/api/auth/login", json.encodeToString(LoginBody.serializer(), LoginBody(email, password)), null))

    suspend fun me(token: String): User = json.decodeFromString(User.serializer(), authed("GET", "/api/me", null, token))

    suspend fun updateMe(token: String, name: String? = null, listCode: String? = null): User =
        json.decodeFromString(User.serializer(), authed("PUT", "/api/me", json.encodeToString(MeBody.serializer(), MeBody(name, listCode)), token))

    suspend fun changePassword(token: String, current: String, next: String): Session =
        json.decodeFromString(Session.serializer(), authed("PUT", "/api/me/password", json.encodeToString(PasswordBody.serializer(), PasswordBody(current, next)), token))

    suspend fun logoutAll(token: String): Session = json.decodeFromString(Session.serializer(), authed("POST", "/api/auth/logout-all", null, token))

    suspend fun deleteAccount(token: String, password: String) {
        authed("DELETE", "/api/me", json.encodeToString(DeleteBody.serializer(), DeleteBody(password)), token)
    }

    @Serializable private data class Created(val code: String, val updatedAt: String)
    @Serializable data class Remote(val code: String, val data: ListData, val updatedAt: String)
    @Serializable private data class Put(val data: ListData, val baseUpdatedAt: String?, val force: Boolean)
    @Serializable private data class Post(val data: ListData)
    @Serializable private data class Updated(val updatedAt: String)

    suspend fun createList(data: ListData): Pair<String, String> {
        val body = json.encodeToString(Post.serializer(), Post(data)).toRequestBody(jsonType)
        val (code, res) = call(Request.Builder().url("$base/api/lists").post(body).build())
        if (code !in 200..299) throw HttpException(code, res)
        val c = json.decodeFromString(Created.serializer(), res!!)
        return c.code to c.updatedAt
    }

    suspend fun getList(code: String): Remote = get("/api/lists/$code")

    sealed interface PutResult {
        data class Ok(val updatedAt: String) : PutResult
        data class Conflict(val remote: Remote) : PutResult
        data object NotFound : PutResult
    }

    suspend fun putList(code: String, data: ListData, baseUpdatedAt: String?, force: Boolean = false): PutResult {
        val body = json.encodeToString(Put.serializer(), Put(data, baseUpdatedAt, force)).toRequestBody(jsonType)
        val (status, res) = call(Request.Builder().url("$base/api/lists/$code").put(body).build())
        return when (status) {
            in 200..299 -> PutResult.Ok(json.decodeFromString(Updated.serializer(), res!!).updatedAt)
            409 -> PutResult.Conflict(json.decodeFromString(Remote.serializer(), res!!))
            404 -> PutResult.NotFound
            else -> throw HttpException(status, res)
        }
    }
}
