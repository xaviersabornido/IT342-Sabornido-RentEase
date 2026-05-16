package cit.edu.sabornido.rentease

import org.json.JSONObject

object RequestJson {

    fun id(o: JSONObject): Long {
        val v = o.opt("requestId") ?: o.opt("request_id") ?: return -1L
        return when (v) {
            is Number -> v.toLong()
            is String -> v.toLongOrNull() ?: -1L
            else -> -1L
        }
    }

    fun listingId(o: JSONObject): Long? {
        val v = o.opt("listingId") ?: o.opt("listing_id") ?: return null
        return when (v) {
            is Number -> v.toLong()
            is String -> v.toLongOrNull()
            else -> null
        }
    }

    fun statusUpper(o: JSONObject): String = o.optString("status", "").uppercase()

    fun listingTitle(o: JSONObject): String =
        o.optString("listingTitle", o.optString("listing_title", "Listing"))

    fun listingLocation(o: JSONObject): String =
        o.optString("listingLocation", o.optString("listing_location", ""))

    fun ratingSubmitted(o: JSONObject): Boolean =
        o.optBoolean("ratingSubmitted", o.optBoolean("rating_submitted", false))

    fun ownerId(o: JSONObject): String? {
        val v = o.opt("ownerId") ?: o.opt("owner_id") ?: return null
        return when (v) {
            is String -> v
            else -> v.toString()
        }
    }

    fun renterDisplay(o: JSONObject): String {
        val fn = o.optString("renterFirstname", o.optString("renter_firstname", ""))
        val ln = o.optString("renterLastname", o.optString("renter_lastname", ""))
        val name = "$fn $ln".trim()
        if (name.isNotEmpty()) return name
        return o.optString("renterEmail", o.optString("renter_email", "Renter"))
    }

    fun monthlyPrice(o: JSONObject): Double? {
        val v = o.opt("listingMonthlyPrice")
            ?: o.opt("listing_monthly_price")
            ?: o.opt("listingPrice")
            ?: return null
        return when (v) {
            is Number -> v.toDouble()
            is String -> v.toDoubleOrNull()
            else -> null
        }
    }
}
