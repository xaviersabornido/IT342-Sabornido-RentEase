package cit.edu.sabornido.rentease

import android.util.Base64
import org.json.JSONObject

object UserJson {

    fun roleUpper(userJson: String?): String {
        if (userJson.isNullOrBlank()) return ""
        return runCatching {
            JSONObject(userJson).optString("role", "").uppercase()
        }.getOrDefault("")
    }

    fun displayName(userJson: String?): String {
        if (userJson.isNullOrBlank()) return ""
        return runCatching {
            val o = JSONObject(userJson)
            val first = o.optString("firstname").trim()
            val last = o.optString("lastname").trim()
            val both = "$first $last".trim()
            if (both.isNotEmpty()) both else o.optString("email", "User")
        }.getOrDefault("")
    }

    /**
     * Dashboard welcome line: renters see given name(s) only (`firstname`); owners see full name.
     */
    fun dashboardWelcomeName(userJson: String?): String {
        if (userJson.isNullOrBlank()) return ""
        return runCatching {
            val o = JSONObject(userJson)
            if (o.optString("role", "").uppercase() == "RENTER") {
                val first = o.optString("firstname").trim()
                if (first.isNotEmpty()) first
                else displayName(userJson).ifEmpty { o.optString("email", "User") }
            } else {
                displayName(userJson)
            }
        }.getOrDefault("")
    }

    fun email(userJson: String?): String {
        if (userJson.isNullOrBlank()) return ""
        return runCatching { JSONObject(userJson).optString("email") }.getOrDefault("")
    }

    /**
     * Matches web [resolveStoredUserId]: prefer profile `id`, else JWT `sub`.
     */
    fun resolveUserId(userJson: String?, accessToken: String?): String? {
        val fromProfile = runCatching {
            val o = JSONObject(userJson ?: return@runCatching null)
            when {
                o.has("id") && !o.isNull("id") ->
                    o.opt("id")?.toString()?.trim()?.takeIf { it.isNotEmpty() }
                else -> null
            }
        }.getOrNull()
        if (!fromProfile.isNullOrBlank()) return fromProfile
        return decodeJwtSub(accessToken)
    }

    private fun decodeJwtSub(accessToken: String?): String? {
        val token = accessToken?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val parts = token.split(".")
        if (parts.size < 2) return null
        var payload = parts[1].replace('-', '+').replace('_', '/')
        val pad = (4 - payload.length % 4) % 4
        if (pad != 4) payload += "=".repeat(pad)
        return runCatching {
            val decoded = Base64.decode(payload, Base64.DEFAULT)
            JSONObject(String(decoded, Charsets.UTF_8)).optString("sub").takeIf { it.isNotBlank() }
        }.getOrNull()
    }

    fun isOwner(userJson: String?) = roleUpper(userJson) == "OWNER"

    fun isRenter(userJson: String?) = roleUpper(userJson) == "RENTER"
}
