package cit.edu.sabornido.rentease

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class RentEaseApi(
    private val baseUrl: String,
    private val authPrefs: AuthPreferences,
) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    private fun bearerRequired(): String {
        val t = authPrefs.accessToken()
        if (t.isNullOrBlank()) throw ApiException("Not signed in", 401)
        return t
    }

    private fun bearerOptional(): String? = authPrefs.accessToken()?.takeIf { it.isNotBlank() }

    private fun parseError(root: JSONObject, response: Response): ApiException {
        val err = root.optJSONObject("error")
        var msg = err?.optString("message")?.takeIf { it.isNotEmpty() }
            ?: root.optString("message").takeIf { it.isNotEmpty() }
            ?: response.message.ifBlank { "Request failed" }
        val details = err?.opt("details")
        if (details is JSONObject) {
            val lines = mutableListOf<String>()
            val keys = details.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                lines.add("$k: ${details.optString(k)}")
            }
            if (lines.isNotEmpty()) {
                msg = lines.joinToString("\n")
            }
        }
        return ApiException(msg, response.code)
    }

    private fun ensureSuccessObject(response: Response): JSONObject {
        val text = response.body?.string().orEmpty()
        if (text.isEmpty()) {
            if (response.isSuccessful) return JSONObject()
            throw ApiException(response.message.ifBlank { "Empty response" }, response.code)
        }
        val root = try {
            JSONObject(text)
        } catch (_: Exception) {
            throw ApiException("Invalid response", response.code)
        }
        val success = root.optBoolean("success", false)
        if (!success || !response.isSuccessful) {
            throw parseError(root, response)
        }
        return root.optJSONObject("data") ?: JSONObject()
    }

    private fun ensureSuccessArray(response: Response): JSONArray {
        val text = response.body?.string().orEmpty()
        val root = try {
            JSONObject(text)
        } catch (_: Exception) {
            throw ApiException("Invalid response", response.code)
        }
        val success = root.optBoolean("success", false)
        if (!success || !response.isSuccessful) {
            throw parseError(root, response)
        }
        val data = root.opt("data") ?: return JSONArray()
        return when (data) {
            is JSONArray -> data
            is JSONObject -> JSONArray().put(data)
            else -> JSONArray()
        }
    }

    suspend fun getListings(): JSONArray = withContext(Dispatchers.IO) {
        val b = Request.Builder()
            .url("$baseUrl/listings")
            .get()
            .header("Content-Type", "application/json")
        bearerOptional()?.let { b.header("Authorization", "Bearer $it") }
        client.newCall(b.build()).execute().use { ensureSuccessArray(it) }
    }

    suspend fun getListingById(id: Long): JSONObject = withContext(Dispatchers.IO) {
        val b = Request.Builder()
            .url("$baseUrl/listings/$id")
            .get()
            .header("Content-Type", "application/json")
        bearerOptional()?.let { b.header("Authorization", "Bearer $it") }
        client.newCall(b.build()).execute().use { ensureSuccessObject(it) }
    }

    suspend fun getMyListings(): JSONArray = withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url("$baseUrl/listings/mine")
            .get()
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer ${bearerRequired()}")
            .build()
        client.newCall(req).execute().use { ensureSuccessArray(it) }
    }

    suspend fun createListing(body: JSONObject): JSONObject = withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url("$baseUrl/listings")
            .post(body.toString().toRequestBody(jsonMedia))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer ${bearerRequired()}")
            .build()
        client.newCall(req).execute().use { ensureSuccessObject(it) }
    }

    suspend fun updateListing(id: Long, body: JSONObject): JSONObject = withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url("$baseUrl/listings/$id")
            .put(body.toString().toRequestBody(jsonMedia))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer ${bearerRequired()}")
            .build()
        client.newCall(req).execute().use { ensureSuccessObject(it) }
    }

    suspend fun deleteListing(id: Long) = withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url("$baseUrl/listings/$id")
            .delete()
            .header("Authorization", "Bearer ${bearerRequired()}")
            .build()
        client.newCall(req).execute().use { response ->
            if (response.code == 204) return@use
            val text = response.body?.string().orEmpty()
            if (text.isEmpty()) {
                if (!response.isSuccessful) {
                    throw ApiException(response.message.ifBlank { "Delete failed" }, response.code)
                }
                return@use
            }
            val root = JSONObject(text)
            if (!response.isSuccessful || !root.optBoolean("success", false)) {
                throw parseError(root, response)
            }
        }
    }

    suspend fun createRentalRequest(body: JSONObject): JSONObject = withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url("$baseUrl/requests")
            .post(body.toString().toRequestBody(jsonMedia))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer ${bearerRequired()}")
            .build()
        client.newCall(req).execute().use { ensureSuccessObject(it) }
    }

    suspend fun getRenterRequests(): JSONArray = withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url("$baseUrl/requests/user")
            .get()
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer ${bearerRequired()}")
            .build()
        client.newCall(req).execute().use { ensureSuccessArray(it) }
    }

    suspend fun getOwnerRequests(): JSONArray = withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url("$baseUrl/requests/owner")
            .get()
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer ${bearerRequired()}")
            .build()
        client.newCall(req).execute().use { ensureSuccessArray(it) }
    }

    suspend fun approveRequest(id: Long): JSONObject = withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url("$baseUrl/requests/$id/approve")
            .put(ByteArray(0).toRequestBody(null))
            .header("Authorization", "Bearer ${bearerRequired()}")
            .build()
        client.newCall(req).execute().use { ensureSuccessObject(it) }
    }

    suspend fun declineRequest(id: Long): JSONObject = withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url("$baseUrl/requests/$id/decline")
            .put(ByteArray(0).toRequestBody(null))
            .header("Authorization", "Bearer ${bearerRequired()}")
            .build()
        client.newCall(req).execute().use { ensureSuccessObject(it) }
    }

    suspend fun getOwnerRatingSummary(ownerId: String): JSONObject = withContext(Dispatchers.IO) {
        val enc = URLEncoder.encode(ownerId, Charsets.UTF_8.name())
        val req = Request.Builder()
            .url("$baseUrl/ratings/owners/$enc/summary")
            .get()
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer ${bearerRequired()}")
            .build()
        client.newCall(req).execute().use { ensureSuccessObject(it) }
    }

    suspend fun submitOwnerRating(body: JSONObject): JSONObject = withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url("$baseUrl/ratings")
            .post(body.toString().toRequestBody(jsonMedia))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer ${bearerRequired()}")
            .build()
        client.newCall(req).execute().use { ensureSuccessObject(it) }
    }
}
