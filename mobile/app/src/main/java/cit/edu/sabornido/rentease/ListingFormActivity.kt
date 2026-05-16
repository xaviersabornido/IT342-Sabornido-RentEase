package cit.edu.sabornido.rentease

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import org.json.JSONObject

class ListingFormActivity : AppCompatActivity() {

    private lateinit var authPrefs: AuthPreferences
    private lateinit var api: RentEaseApi
    private var listingId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authPrefs = AuthPreferences(this)
        if (!authPrefs.isLoggedIn()) {
            SessionHelper.forceLogin(this)
            return
        }
        if (!UserJson.isOwner(authPrefs.userJson())) {
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_listing_form)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root)) { v, insets ->
            val b = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(b.left, b.top, b.right, b.bottom)
            insets
        }

        api = RentEaseApi(getString(R.string.api_base_url), authPrefs)
        listingId = intent.getLongExtra(EXTRA_LISTING_ID, -1L)
        val isEdit = listingId > 0

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.title = if (isEdit) getString(R.string.edit_listing_title) else getString(R.string.new_listing_title)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val typeSpinner = findViewById<Spinner>(R.id.property_type)
        val types = listOf("Apartment", "House", "Condominium")
        typeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        findViewById<MaterialButton>(R.id.btn_delete).visibility = if (isEdit) View.VISIBLE else View.GONE
        findViewById<MaterialButton>(R.id.btn_delete).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(R.string.delete_listing)
                .setMessage("Remove this listing? This cannot be undone.")
                .setPositiveButton(R.string.delete_listing) { _, _ ->
                    lifecycleScope.launch {
                        try {
                            api.deleteListing(listingId)
                            finish()
                        } catch (e: ApiException) {
                            if (SessionHelper.handleUnauthorized(this@ListingFormActivity, e)) return@launch
                            showError(e.message)
                        } catch (_: Exception) {
                            showError("Could not delete.")
                        }
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        findViewById<MaterialButton>(R.id.btn_save).setOnClickListener { save(isEdit) }

        if (isEdit) {
            lifecycleScope.launch {
                try {
                    val listing = api.getListingById(listingId)
                    populate(listing, types, typeSpinner)
                } catch (e: ApiException) {
                    if (SessionHelper.handleUnauthorized(this@ListingFormActivity, e)) return@launch
                    showError(e.message)
                } catch (_: Exception) {
                    showError(getString(R.string.unable_load_listings))
                }
            }
        } else {
            findViewById<MaterialSwitch>(R.id.pets_allowed).isChecked = true
        }
    }

    private fun populate(listing: JSONObject, types: List<String>, typeSpinner: Spinner) {
        findViewById<TextInputEditText>(R.id.title).setText(listing.optString("title", ""))
        findViewById<TextInputEditText>(R.id.price).setText(numField(listing, "price"))
        findViewById<TextInputEditText>(R.id.location).setText(listing.optString("location", ""))
        val pt = listing.optString("propertyType", listing.optString("property_type", "Apartment"))
        val idx = types.indexOfFirst { it.equals(pt, ignoreCase = true) }
        typeSpinner.setSelection(if (idx >= 0) idx else 0)
        findViewById<TextInputEditText>(R.id.description).setText(listing.optString("description", ""))
        findViewById<TextInputEditText>(R.id.amenities).setText(listing.optString("amenities", ""))
        findViewById<TextInputEditText>(R.id.image_urls).setText(listing.optString("imageUrls", ""))
        findViewById<TextInputEditText>(R.id.bedrooms).setText(numField(listing, "bedrooms"))
        findViewById<TextInputEditText>(R.id.bathrooms).setText(numField(listing, "bathrooms"))
        findViewById<TextInputEditText>(R.id.area_sqft).setText(numField(listing, "areaSqFt"))
        findViewById<TextInputEditText>(R.id.parking).setText(numField(listing, "parkingSpaces"))
        val af = listing.optString("availableFrom", listing.optString("available_from", ""))
        findViewById<TextInputEditText>(R.id.available_from).setText(if (af.length >= 10) af.substring(0, 10) else af)
        findViewById<TextInputEditText>(R.id.lease_months).setText(numField(listing, "leaseTermMonths"))
        findViewById<TextInputEditText>(R.id.deposit).setText(numField(listing, "deposit"))
        findViewById<TextInputEditText>(R.id.utilities).setText(numField(listing, "utilitiesEstimate"))
        findViewById<MaterialSwitch>(R.id.furnished).isChecked = listing.optBoolean("furnished", false)
        findViewById<MaterialSwitch>(R.id.pets_allowed).isChecked = listing.optBoolean("petsAllowed", true)
    }

    private fun numField(o: JSONObject, key: String): String {
        if (!o.has(key) || o.isNull(key)) return ""
        return when (val v = o.get(key)) {
            is Number -> v.toString()
            is String -> v
            else -> ""
        }
    }

    private fun save(isEdit: Boolean) {
        val errView = findViewById<TextView>(R.id.error_text)
        errView.visibility = View.GONE

        val title = findViewById<TextInputEditText>(R.id.title).text?.toString()?.trim().orEmpty()
        val priceStr = findViewById<TextInputEditText>(R.id.price).text?.toString()?.trim().orEmpty()
        val location = findViewById<TextInputEditText>(R.id.location).text?.toString()?.trim().orEmpty()
        val pt = findViewById<Spinner>(R.id.property_type).selectedItem?.toString().orEmpty()

        if (title.isEmpty() || location.isEmpty() || priceStr.isEmpty()) {
            showError("Title, price, and location are required.")
            return
        }
        val price = priceStr.toDoubleOrNull()
        if (price == null) {
            showError("Invalid price.")
            return
        }

        val body = JSONObject().apply {
            put("title", title)
            put("price", price)
            put("location", location)
            put("propertyType", pt.ifEmpty { "Apartment" })
            put("description", findViewById<TextInputEditText>(R.id.description).text?.toString()?.trim().orEmpty())
            put("amenities", findViewById<TextInputEditText>(R.id.amenities).text?.toString()?.trim().orEmpty())
            put("imageUrls", findViewById<TextInputEditText>(R.id.image_urls).text?.toString()?.trim().orEmpty())
            putNullableInt(this, "bedrooms", findViewById<TextInputEditText>(R.id.bedrooms).text?.toString())
            putNullableInt(this, "bathrooms", findViewById<TextInputEditText>(R.id.bathrooms).text?.toString())
            putNullableInt(this, "areaSqFt", findViewById<TextInputEditText>(R.id.area_sqft).text?.toString())
            putNullableInt(this, "parkingSpaces", findViewById<TextInputEditText>(R.id.parking).text?.toString())
            val af = findViewById<TextInputEditText>(R.id.available_from).text?.toString()?.trim().orEmpty()
            if (af.isEmpty()) put("availableFrom", JSONObject.NULL) else put("availableFrom", af)
            putNullableInt(this, "leaseTermMonths", findViewById<TextInputEditText>(R.id.lease_months).text?.toString())
            putNullableDecimal(this, "deposit", findViewById<TextInputEditText>(R.id.deposit).text?.toString())
            putNullableDecimal(this, "utilitiesEstimate", findViewById<TextInputEditText>(R.id.utilities).text?.toString())
            put("furnished", findViewById<MaterialSwitch>(R.id.furnished).isChecked)
            put("petsAllowed", findViewById<MaterialSwitch>(R.id.pets_allowed).isChecked)
        }

        lifecycleScope.launch {
            try {
                if (isEdit) api.updateListing(listingId, body) else api.createListing(body)
                finish()
            } catch (e: ApiException) {
                if (SessionHelper.handleUnauthorized(this@ListingFormActivity, e)) return@launch
                showError(e.message)
            } catch (_: Exception) {
                showError("Could not save listing.")
            }
        }
    }

    private fun putNullableInt(obj: JSONObject, key: String, raw: String?) {
        val t = raw?.trim().orEmpty()
        if (t.isEmpty()) {
            obj.put(key, JSONObject.NULL)
        } else {
            val n = t.toIntOrNull()
            if (n == null) obj.put(key, JSONObject.NULL) else obj.put(key, n)
        }
    }

    private fun putNullableDecimal(obj: JSONObject, key: String, raw: String?) {
        val t = raw?.trim().orEmpty()
        if (t.isEmpty()) {
            obj.put(key, JSONObject.NULL)
        } else {
            val n = t.toDoubleOrNull()
            if (n == null) obj.put(key, JSONObject.NULL) else obj.put(key, n)
        }
    }

    private fun showError(msg: CharSequence?) {
        findViewById<TextView>(R.id.error_text).apply {
            text = msg
            visibility = View.VISIBLE
        }
    }

    companion object {
        const val EXTRA_LISTING_ID = "listing_id"
    }
}
