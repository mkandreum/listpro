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
