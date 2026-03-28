package cit.edu.sabornido.rentease

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AuthApi(private val baseUrl: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    suspend fun login(email: String, password: String): AuthPayload = withContext(Dispatchers.IO) {
        val body = JSONObject().apply {
            put("email", email)
            put("password", password)
        }.toString().toRequestBody(jsonMedia)
        val req = Request.Builder()
            .url("$baseUrl/auth/login")
            .post(body)
            .build()
        executeAuth(req)
    }

    suspend fun register(
        email: String,
        password: String,
        firstname: String,
        lastname: String,
        role: String,
    ): AuthPayload = withContext(Dispatchers.IO) {
        val body = JSONObject().apply {
            put("email", email)
            put("password", password)
            put("firstname", firstname)
            put("lastname", lastname)
            put("role", role)
        }.toString().toRequestBody(jsonMedia)
        val req = Request.Builder()
            .url("$baseUrl/auth/register")
            .post(body)
            .build()
        executeAuth(req)
    }

    private fun executeAuth(req: Request): AuthPayload {
        client.newCall(req).execute().use { response ->
            val text = response.body?.string().orEmpty()
            val root = try {
                JSONObject(text)
            } catch (_: Exception) {
                throw ApiException(response.message.ifBlank { "Invalid response" })
            }
            val success = root.optBoolean("success", false)
            if (!success || !response.isSuccessful) {
                throw parseError(root, response.message)
            }
            val data = root.optJSONObject("data")
                ?: throw ApiException("Missing data in response")
            val user = data.optJSONObject("user")
                ?: throw ApiException("Missing user in response")
            val accessToken = data.optString("accessToken", "")
            if (accessToken.isEmpty()) {
                throw ApiException("Missing access token")
            }
            val refreshToken = data.optString("refreshToken", "").takeIf { it.isNotEmpty() }
            val userJson = user.toString()
            return AuthPayload(accessToken, refreshToken, userJson)
        }
    }

    private fun parseError(root: JSONObject, fallback: String): ApiException {
        val err = root.optJSONObject("error")
        var msg = err?.optString("message")?.takeIf { it.isNotEmpty() }
            ?: root.optString("message").takeIf { it.isNotEmpty() }
            ?: fallback.ifBlank { "Request failed" }
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
        return ApiException(msg)
    }
}

data class AuthPayload(
    val accessToken: String,
    val refreshToken: String?,
    val userJson: String,
)

class ApiException(message: String) : Exception(message)
