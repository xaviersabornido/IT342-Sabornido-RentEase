package cit.edu.sabornido.rentease

import org.json.JSONObject
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

object ListingJson {

    fun id(o: JSONObject): Long {
        if (o.has("id")) {
            when (val v = o.get("id")) {
                is Number -> return v.toLong()
                is String -> return v.toLongOrNull() ?: -1L
            }
        }
        return -1L
    }

    fun title(o: JSONObject): String = o.optString("title", "—")

    fun location(o: JSONObject): String = o.optString("location", "")

    fun propertyType(o: JSONObject): String = o.optString("propertyType", o.optString("property_type", ""))

    fun description(o: JSONObject): String = o.optString("description", "")

    fun imageUrls(o: JSONObject): String = o.optString("imageUrls", o.optString("image_urls", ""))

    fun status(o: JSONObject): String = o.optString("status", "")

    fun priceLabel(o: JSONObject): String {
        val n = when {
            o.has("price") && !o.isNull("price") -> {
                when (val p = o.get("price")) {
                    is Number -> p.toDouble()
                    is String -> p.toDoubleOrNull()
                    else -> null
                }
            }
            else -> null
        }
        if (n == null) return "—"
        val fmt = NumberFormat.getCurrencyInstance(Locale.US)
        fmt.maximumFractionDigits = 0
        return fmt.format(n) + "/mo"
    }

    fun matchesFilter(o: JSONObject, filter: String): Boolean {
        if (filter == "all") return true
        val type = propertyType(o).lowercase()
        return when (filter) {
            "apartments" -> type == "apartment"
            "houses" -> type == "house"
            "condominiums" -> type == "condo" || type == "condominium"
            else -> true
        }
    }

    /** Integer field with camelCase / snake_case keys (API matches web ListingResponse). */
    fun optIntField(o: JSONObject, vararg keys: String): Int? {
        for (k in keys) {
            if (!o.has(k) || o.isNull(k)) continue
            when (val v = o.get(k)) {
                is Int -> return v
                is Number -> return v.toInt()
                is String -> return v.toIntOrNull()
            }
        }
        return null
    }

    fun optMoneyField(o: JSONObject, vararg keys: String): Double? {
        for (k in keys) {
            if (!o.has(k) || o.isNull(k)) continue
            when (val v = o.get(k)) {
                is Number -> return v.toDouble()
                is String -> return v.toDoubleOrNull()
            }
        }
        return null
    }

    fun formatUsdWhole(amount: Double): String {
        val fmt = NumberFormat.getCurrencyInstance(Locale.US)
        fmt.maximumFractionDigits = 0
        return fmt.format(amount)
    }

    fun depositLabel(o: JSONObject): String {
        val n = optMoneyField(o, "deposit") ?: return ""
        return formatUsdWhole(n)
    }

    fun utilitiesLabel(o: JSONObject): String {
        val n = optMoneyField(o, "utilitiesEstimate", "utilities_estimate") ?: return ""
        return formatUsdWhole(n)
    }

    fun areaLabel(o: JSONObject): String {
        val n = optIntField(o, "areaSqFt", "area_sq_ft") ?: return ""
        val fmt = NumberFormat.getNumberInstance(Locale.US)
        return "${fmt.format(n.toLong())} sq ft"
    }

    fun availableFromFormatted(o: JSONObject): String {
        val raw = o.optString("availableFrom", o.optString("available_from", "")).trim()
        if (raw.isEmpty()) return ""
        val ymd = raw.take(10)
        return try {
            val inFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val outFmt = SimpleDateFormat("MMMM d, yyyy", Locale.US)
            outFmt.format(inFmt.parse(ymd)!!)
        } catch (_: Exception) {
            raw
        }
    }

    fun optBooleanField(o: JSONObject, vararg keys: String): Boolean? {
        for (k in keys) {
            if (!o.has(k) || o.isNull(k)) continue
            return o.optBoolean(k)
        }
        return null
    }

    fun amenityTokens(amenitiesCsv: String): List<String> =
        amenitiesCsv.split(',').map { it.trim() }.filter { it.isNotEmpty() }
}
