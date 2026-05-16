package cit.edu.sabornido.rentease

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Locale

class PropertyDetailActivity : AppCompatActivity() {

    private lateinit var authPrefs: AuthPreferences
    private lateinit var api: RentEaseApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_property_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        authPrefs = AuthPreferences(this)
        api = RentEaseApi(getString(R.string.api_base_url), authPrefs)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        findViewById<AppBarLayout>(R.id.app_bar_layout).isLiftOnScroll = false
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val listingId = intent.getLongExtra(EXTRA_LISTING_ID, -1L)
        if (listingId < 0) {
            findViewById<TextView>(R.id.status_error).apply {
                text = getString(R.string.error_prefix, "Invalid listing")
                visibility = View.VISIBLE
            }
            findViewById<TextView>(R.id.status_loading).visibility = View.GONE
            return
        }

        loadListing(listingId)
    }

    private fun loadListing(id: Long) {
        val loading = findViewById<TextView>(R.id.status_loading)
        val err = findViewById<TextView>(R.id.status_error)
        val card = findViewById<View>(R.id.content_card)
        lifecycleScope.launch {
            try {
                val listing = api.getListingById(id)
                loading.visibility = View.GONE
                err.visibility = View.GONE
                card.visibility = View.VISIBLE
                bindListing(listing)
            } catch (e: ApiException) {
                if (SessionHelper.handleUnauthorized(this@PropertyDetailActivity, e)) return@launch
                loading.visibility = View.GONE
                err.text = getString(R.string.error_prefix, e.message ?: "")
                err.visibility = View.VISIBLE
            } catch (_: Exception) {
                loading.visibility = View.GONE
                err.text = getString(R.string.error_prefix, getString(R.string.unable_load_listings))
                err.visibility = View.VISIBLE
            }
        }
    }

    private fun bindListing(listing: JSONObject) {
        supportActionBar?.title = ListingJson.title(listing)
        findViewById<MaterialToolbar>(R.id.toolbar)
            .setTitleTextColor(ContextCompat.getColor(this, R.color.white))

        bindListingPhotos(listing)
        findViewById<TextView>(R.id.title).text = ListingJson.title(listing)
        findViewById<TextView>(R.id.location).text = ListingJson.location(listing)
        findViewById<TextView>(R.id.price).text = ListingJson.priceLabel(listing)

        findViewById<TextView>(R.id.utilities_line).apply {
            val util = ListingJson.utilitiesLabel(listing)
            if (util.isNotEmpty()) {
                text = getString(R.string.utilities_plus, util)
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }

        findViewById<TextView>(R.id.status_label).text = availabilityLabel(listing)

        bindFeatureValues(listing)

        findViewById<TextView>(R.id.description).text =
            ListingJson.description(listing).ifEmpty { getString(R.string.description_empty) }

        bindAmenityChips(listing)
        bindPropertyDetailRows(listing)

        val fn = listing.optString("ownerFirstname", listing.optString("owner_firstname", ""))
        val ln = listing.optString("ownerLastname", listing.optString("owner_lastname", ""))
        val ownerName = "$fn $ln".trim().ifEmpty { getString(R.string.owner_property_owner) }
        findViewById<TextView>(R.id.owner_name).text = ownerName

        val rating = listing.opt("ownerRating") ?: listing.opt("owner_rating")
        val ratingText = when (rating) {
            is Number -> String.format(Locale.US, "%.1f ★", rating.toDouble())
            else -> getString(R.string.no_owner_rating)
        }
        findViewById<TextView>(R.id.owner_rating).text = ratingText

        val btn = findViewById<MaterialButton>(R.id.btn_request)
        val hint = findViewById<TextView>(R.id.request_hint)
        hint.visibility = View.GONE
        btn.visibility = View.VISIBLE

        val loggedIn = authPrefs.isLoggedIn()
        val role = UserJson.roleUpper(authPrefs.userJson())

        when {
            !loggedIn -> {
                btn.isEnabled = true
                btn.text = getString(R.string.sign_in)
                hint.text = getString(R.string.sign_in_to_request)
                hint.visibility = View.VISIBLE
                btn.setOnClickListener {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
            }
            role == "OWNER" -> {
                btn.isEnabled = false
                btn.text = getString(R.string.your_listing)
            }
            role == "RENTER" -> {
                btn.isEnabled = true
                btn.text = getString(R.string.send_rental_request)
                val id = ListingJson.id(listing)
                btn.setOnClickListener {
                    startActivity(
                        Intent(this, SubmitRentalRequestActivity::class.java)
                            .putExtra(SubmitRentalRequestActivity.EXTRA_LISTING_ID, id),
                    )
                }
            }
            else -> {
                btn.isEnabled = false
                btn.text = getString(R.string.send_rental_request)
                hint.text = "Only renter accounts can send requests."
                hint.visibility = View.VISIBLE
            }
        }
    }

    private fun availabilityLabel(listing: JSONObject): String {
        val s = ListingJson.status(listing).lowercase(Locale.US)
        return when (s) {
            "available" -> getString(R.string.availability_available_now)
            "rented" -> getString(R.string.availability_rented)
            "pending" -> getString(R.string.availability_pending)
            else -> getString(R.string.availability_generic)
        }
    }

    private fun bindFeatureValues(listing: JSONObject) {
        val bed = ListingJson.optIntField(listing, "bedrooms")?.toString() ?: "—"
        val bath = ListingJson.optIntField(listing, "bathrooms")?.toString() ?: "—"
        val area = ListingJson.areaLabel(listing).ifEmpty { "—" }
        val parkN = ListingJson.optIntField(listing, "parkingSpaces", "parking_spaces")
        val parking = when (parkN) {
            null -> "—"
            1 -> getString(R.string.parking_one_space)
            else -> getString(R.string.parking_n_spaces, parkN)
        }
        findViewById<TextView>(R.id.feature_bedrooms_value).text = bed
        findViewById<TextView>(R.id.feature_bathrooms_value).text = bath
        findViewById<TextView>(R.id.feature_area_value).text = area
        findViewById<TextView>(R.id.feature_parking_value).text = parking
    }

    private fun bindAmenityChips(listing: JSONObject) {
        val group = findViewById<ChipGroup>(R.id.amenities_chip_group)
        group.removeAllViews()
        val raw = listing.optString("amenities", "").trim()
        val items = if (raw.isNotEmpty()) {
            ListingJson.amenityTokens(raw)
        } else {
            listOf(
                "High-Speed Internet",
                "Air Conditioning",
                "Fitness Center",
                "24/7 Security",
                "Elevator Access",
                "Pet Friendly",
            )
        }
        val iconSizePx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            18f,
            resources.displayMetrics,
        )
        val chipBg = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.amenity_chip_bg))
        val iconTint = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.rentease_text_secondary))
        for (label in items) {
            val chip = Chip(this)
            chip.text = label
            chip.isCheckable = false
            chip.isClickable = false
            chip.chipStrokeWidth = 0f
            chip.chipBackgroundColor = chipBg
            chip.setTextColor(ContextCompat.getColor(this, R.color.rentease_text_primary))
            amenityIconFor(label)?.let { resId ->
                AppCompatResources.getDrawable(this, resId)?.let { d ->
                    chip.chipIcon = d
                    chip.chipIconSize = iconSizePx
                    chip.chipIconTint = iconTint
                    chip.isChipIconVisible = true
                }
            }
            group.addView(chip)
        }
    }

    private fun amenityIconFor(label: String): Int? {
        val t = label.lowercase(Locale.US)
        return when {
            "wifi" in t || "internet" in t -> R.drawable.ic_amenity_wifi
            "air conditioning" in t || "a/c" in t || t.trim() == "ac" || ("air" in t && "condition" in t) ->
                R.drawable.ic_amenity_ac
            "gym" in t || "fitness" in t -> R.drawable.ic_amenity_gym
            "parking" in t -> R.drawable.ic_amenity_car
            "pool" in t -> R.drawable.ic_amenity_pool
            "security" in t || "elevator" in t -> R.drawable.ic_amenity_security
            else -> null
        }
    }

    private fun bindPropertyDetailRows(listing: JSONObject) {
        val container = findViewById<LinearLayout>(R.id.property_details_rows)
        container.removeAllViews()
        val inflater = layoutInflater

        fun addRow(label: String, value: String) {
            val row = inflater.inflate(R.layout.item_property_detail_row, container, false)
            row.findViewById<TextView>(R.id.row_label).text = label
            row.findViewById<TextView>(R.id.row_value).text = value.ifEmpty { "—" }
            container.addView(row)
        }

        val rawType = ListingJson.propertyType(listing)
        val displayType = if (rawType.isEmpty()) {
            "—"
        } else {
            rawType.replaceFirstChar { c ->
                if (c.isLowerCase()) c.titlecase(Locale.getDefault()) else c.toString()
            }
        }
        addRow(getString(R.string.property_type), displayType)

        addRow(getString(R.string.detail_available_from), ListingJson.availableFromFormatted(listing))

        val leaseN = ListingJson.optIntField(listing, "leaseTermMonths", "lease_term_months")
        val leaseStr = leaseN?.let { getString(R.string.lease_months_fmt, it) }.orEmpty()
        addRow(getString(R.string.detail_lease_term), leaseStr)

        addRow(getString(R.string.detail_deposit), ListingJson.depositLabel(listing))

        val fur = ListingJson.optBooleanField(listing, "furnished")
        val furStr = when (fur) {
            null -> ""
            true -> getString(R.string.furnished_yes)
            false -> getString(R.string.furnished_no)
        }
        addRow(getString(R.string.detail_furnished), furStr)

        val pets = ListingJson.optBooleanField(listing, "petsAllowed", "pets_allowed")
        val petsStr = when (pets) {
            null -> ""
            true -> getString(R.string.pets_allowed)
            false -> getString(R.string.pets_not_allowed)
        }
        addRow(getString(R.string.detail_pets), petsStr)

        val util = ListingJson.utilitiesLabel(listing)
        val utilRow = if (util.isNotEmpty()) getString(R.string.utilities_approx, util) else ""
        addRow(getString(R.string.detail_utilities), utilRow)
    }

    private fun bindListingPhotos(listing: JSONObject) {
        val urls = ListingImages.parseUrls(ListingJson.imageUrls(listing)).filter {
            it.startsWith("http://") || it.startsWith("https://")
        }
        val hero = findViewById<ImageView>(R.id.hero_image)
        val scroll = findViewById<HorizontalScrollView>(R.id.photo_scroll)
        val strip = findViewById<LinearLayout>(R.id.thumbnail_strip)
        strip.removeAllViews()

        if (urls.isEmpty()) {
            hero.loadListingPhoto(null)
            scroll.visibility = View.GONE
            return
        }

        hero.loadListingPhoto(urls[0])

        if (urls.size <= 1) {
            scroll.visibility = View.GONE
            return
        }

        scroll.visibility = View.VISIBLE
        val thumbPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            72f,
            resources.displayMetrics,
        ).toInt()
        val marginPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            8f,
            resources.displayMetrics,
        ).toInt()
        val cornerPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            8f,
            resources.displayMetrics,
        )

        for (u in urls) {
            val iv = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(thumbPx, thumbPx).apply {
                    marginEnd = marginPx
                }
            }
            iv.loadListingPhoto(u, cornerPx)
            iv.setOnClickListener { hero.loadListingPhoto(u) }
            strip.addView(iv)
        }
    }

    companion object {
        const val EXTRA_LISTING_ID = "listing_id"
    }
}
