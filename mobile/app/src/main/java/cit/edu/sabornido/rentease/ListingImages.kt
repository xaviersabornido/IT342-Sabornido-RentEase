package cit.edu.sabornido.rentease

import android.widget.ImageView
import coil.load
import coil.request.CachePolicy
import coil.transform.RoundedCornersTransformation

object ListingImages {

    /** Comma-separated URLs from API (`imageUrls`). */
    fun parseUrls(raw: String?): List<String> =
        raw.orEmpty()
            .split(',')
            .mapNotNull { part ->
                val u = part.trim()
                if (u.isEmpty()) null else u
            }

    fun firstUrl(raw: String?): String? =
        parseUrls(raw).firstOrNull { it.startsWith("http://") || it.startsWith("https://") }
}

/**
 * Loads a remote listing photo or the coral gradient placeholder (matches web empty state).
 * @param cornerRadiusPx rounded corners for the bitmap (0 = none).
 */
fun ImageView.loadListingPhoto(url: String?, cornerRadiusPx: Float = 0f) {
    val placeholder = R.drawable.bg_listing_image_placeholder
    val safeUrl = url?.takeIf { it.startsWith("http://") || it.startsWith("https://") }

    if (safeUrl == null) {
        load(placeholder) {
            crossfade(false)
        }
        scaleType = ImageView.ScaleType.CENTER_CROP
        return
    }

    load(safeUrl) {
        placeholder(placeholder)
        error(placeholder)
        crossfade(true)
        memoryCachePolicy(CachePolicy.ENABLED)
        diskCachePolicy(CachePolicy.ENABLED)
        if (cornerRadiusPx > 0f) {
            transformations(RoundedCornersTransformation(cornerRadiusPx))
        }
    }
    scaleType = ImageView.ScaleType.CENTER_CROP
}
