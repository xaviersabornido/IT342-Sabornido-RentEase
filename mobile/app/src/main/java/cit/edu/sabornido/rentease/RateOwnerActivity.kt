package cit.edu.sabornido.rentease

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.RatingBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import org.json.JSONObject

class RateOwnerActivity : AppCompatActivity() {

    private lateinit var authPrefs: AuthPreferences
    private lateinit var api: RentEaseApi
    private var requestId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authPrefs = AuthPreferences(this)
        if (!authPrefs.isLoggedIn() || !UserJson.isRenter(authPrefs.userJson())) {
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_rate_owner)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root)) { v, insets ->
            val b = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(b.left, b.top, b.right, b.bottom)
            insets
        }

        api = RentEaseApi(getString(R.string.api_base_url), authPrefs)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        requestId = intent.getLongExtra(EXTRA_REQUEST_ID, -1L)
        if (requestId < 0) {
            showError(getString(R.string.error_prefix, "Invalid request"))
            findViewById<TextView>(R.id.status_loading).visibility = View.GONE
            return
        }

        load()

        findViewById<MaterialButton>(R.id.btn_submit).setOnClickListener { submit() }
        setupRatingBarsTouchForScroll()
    }

    /**
     * NestedScrollView otherwise steals horizontal/star touches; small RatingBars barely receive them.
     */
    private fun setupRatingBarsTouchForScroll() {
        val ids = listOf(
            R.id.overall_stars,
            R.id.r_responsiveness,
            R.id.r_accuracy,
            R.id.r_communication,
            R.id.r_fairness,
        )
        for (id in ids) {
            findViewById<RatingBar>(id).setOnTouchListener { v, event ->
                val scroll = generateSequence(v.parent) { it.parent }
                    .firstOrNull { it is NestedScrollView } as? NestedScrollView
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> scroll?.requestDisallowInterceptTouchEvent(true)
                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL,
                    -> scroll?.requestDisallowInterceptTouchEvent(false)
                }
                false
            }
        }
    }

    private fun load() {
        val loading = findViewById<TextView>(R.id.status_loading)
        val err = findViewById<TextView>(R.id.status_error)
        val card = findViewById<MaterialCardView>(R.id.form_card)
        lifecycleScope.launch {
            try {
                val arr = api.getRenterRequests()
                var match: JSONObject? = null
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    if (RequestJson.id(o) == requestId) {
                        match = o
                        break
                    }
                }
                loading.visibility = View.GONE
                if (match == null) {
                    err.text = "We could not find this rental request."
                    err.visibility = View.VISIBLE
                    return@launch
                }
                val st = RequestJson.statusUpper(match)
                if (st != "APPROVED") {
                    err.text = "You can only rate the owner after your request has been approved."
                    err.visibility = View.VISIBLE
                    return@launch
                }
                if (RequestJson.ratingSubmitted(match)) {
                    err.text = "You have already submitted a rating for this application."
                    err.visibility = View.VISIBLE
                    return@launch
                }
                val oid = RequestJson.ownerId(match)
                if (oid.isNullOrBlank()) {
                    err.text = "Owner information is missing for this listing."
                    err.visibility = View.VISIBLE
                    return@launch
                }

                val fn = match.optString("ownerFirstname", match.optString("owner_firstname", ""))
                val ln = match.optString("ownerLastname", match.optString("owner_lastname", ""))
                val ownerName = "$fn $ln".trim().ifEmpty { "Property owner" }

                val summary = try {
                    api.getOwnerRatingSummary(oid)
                } catch (_: Exception) {
                    JSONObject()
                }
                val avg = summary.opt("averageRating") ?: summary.opt("average_rating")
                val count = summary.optLong("reviewCount", summary.optLong("review_count", 0))
                val avgStr = when (avg) {
                    is Number -> String.format(java.util.Locale.US, "%.1f", avg.toDouble())
                    else -> "—"
                }
                findViewById<TextView>(R.id.owner_summary).text =
                    "Rating $ownerName\nAvg: $avgStr · Reviews: $count"

                card.visibility = View.VISIBLE
            } catch (e: ApiException) {
                if (SessionHelper.handleUnauthorized(this@RateOwnerActivity, e)) return@launch
                loading.visibility = View.GONE
                err.text = e.message
                err.visibility = View.VISIBLE
            } catch (_: Exception) {
                loading.visibility = View.GONE
                err.text = getString(R.string.error_prefix, getString(R.string.unable_load_listings))
                err.visibility = View.VISIBLE
            }
        }
    }

    private fun submit() {
        val err = findViewById<TextView>(R.id.submit_error)
        err.visibility = View.GONE
        val overall = findViewById<RatingBar>(R.id.overall_stars).rating.toInt()
        if (overall < 1) {
            err.text = "Please choose an overall rating (1–5 stars)."
            err.visibility = View.VISIBLE
            return
        }
        val body = JSONObject().apply {
            put("rentalRequestId", requestId)
            put("rating", overall)
            val c = findViewById<TextInputEditText>(R.id.comment).text?.toString()?.trim().orEmpty()
            if (c.isNotEmpty()) put("comment", c)
            putIfPositive(this, "responsivenessRating", findViewById<RatingBar>(R.id.r_responsiveness).rating.toInt())
            putIfPositive(this, "listingAccuracyRating", findViewById<RatingBar>(R.id.r_accuracy).rating.toInt())
            putIfPositive(this, "communicationRating", findViewById<RatingBar>(R.id.r_communication).rating.toInt())
            putIfPositive(this, "fairnessRating", findViewById<RatingBar>(R.id.r_fairness).rating.toInt())
        }

        lifecycleScope.launch {
            try {
                api.submitOwnerRating(body)
                finish()
            } catch (e: ApiException) {
                if (SessionHelper.handleUnauthorized(this@RateOwnerActivity, e)) return@launch
                err.text = e.message
                err.visibility = View.VISIBLE
            } catch (_: Exception) {
                err.text = "Could not submit rating."
                err.visibility = View.VISIBLE
            }
        }
    }

    private fun putIfPositive(obj: JSONObject, key: String, value: Int) {
        if (value > 0) obj.put(key, value)
    }

    private fun showError(msg: String) {
        findViewById<TextView>(R.id.status_error).apply {
            text = msg
            visibility = View.VISIBLE
        }
    }

    companion object {
        const val EXTRA_REQUEST_ID = "request_id"
    }
}
