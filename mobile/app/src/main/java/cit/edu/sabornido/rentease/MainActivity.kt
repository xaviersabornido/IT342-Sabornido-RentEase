package cit.edu.sabornido.rentease

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.max
import kotlin.math.min

class MainActivity : AppCompatActivity() {

    private lateinit var authPrefs: AuthPreferences
    private lateinit var api: RentEaseApi
    private lateinit var adapter: ListingAdapter
    private lateinit var toolbar: MaterialToolbar
    private var userJson: String? = null

    private var allListings: List<JSONObject> = emptyList()
    private var filterKey = "all"
    private var currentPage = 1
    private val perPage = 9

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authPrefs = AuthPreferences(this)
        if (!authPrefs.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        userJson = authPrefs.userJson()
        invalidateOptionsMenu()

        api = RentEaseApi(getString(R.string.api_base_url), authPrefs)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        this.toolbar = toolbar
        setSupportActionBar(toolbar)

        val welcome = findViewById<TextView>(R.id.welcome_text)
        val display = UserJson.dashboardWelcomeName(userJson).ifEmpty { getString(R.string.brand_name) }
        welcome.text = getString(R.string.welcome_user, display)

        findViewById<TextView>(R.id.dashboard_subtitle).visibility =
            if (UserJson.isOwner(userJson)) View.GONE else View.VISIBLE

        adapter = ListingAdapter(emptyList()) { o ->
            val id = ListingJson.id(o)
            if (id > 0) {
                startActivity(
                    Intent(this, PropertyDetailActivity::class.java)
                        .putExtra(PropertyDetailActivity.EXTRA_LISTING_ID, id),
                )
            }
        }
        val recycler = findViewById<RecyclerView>(R.id.recycler)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        val refresh = findViewById<SwipeRefreshLayout>(R.id.refresh)
        refresh.setOnRefreshListener { loadListings() }

        val chipGroup = findViewById<ChipGroup>(R.id.filter_chips)
        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            filterKey = when (checkedIds.first()) {
                R.id.chip_apartments -> "apartments"
                R.id.chip_houses -> "houses"
                R.id.chip_condos -> "condominiums"
                else -> "all"
            }
            currentPage = 1
            bindPage()
        }

        findViewById<MaterialButton>(R.id.btn_prev_page).setOnClickListener {
            if (currentPage > 1) {
                currentPage--
                bindPage()
            }
        }
        findViewById<MaterialButton>(R.id.btn_next_page).setOnClickListener {
            val filtered = filteredList()
            val totalPages = totalPagesFor(filtered.size)
            if (currentPage < totalPages) {
                currentPage++
                bindPage()
            }
        }

        val fab = findViewById<FloatingActionButton>(R.id.fab_list_property)
        if (UserJson.isOwner(userJson)) {
            fab.visibility = View.VISIBLE
            fab.setOnClickListener {
                startActivity(Intent(this, ListingFormActivity::class.java))
            }
            findViewById<MaterialCardView>(R.id.owner_rating_card).visibility = View.VISIBLE
            loadOwnerHostRating()
        }

        loadListings()
    }

    override fun onResume() {
        super.onResume()
        if (UserJson.isOwner(userJson)) {
            loadOwnerHostRating()
        }
    }

    private fun loadOwnerHostRating() {
        val valueView = findViewById<TextView>(R.id.owner_rating_value)
        if (!UserJson.isOwner(userJson)) return
        val ownerId = UserJson.resolveUserId(userJson, authPrefs.accessToken())
        if (ownerId.isNullOrBlank()) {
            valueView.text = getString(R.string.host_rating_none)
            return
        }
        valueView.text = getString(R.string.host_rating_loading)
        lifecycleScope.launch {
            try {
                val summary = api.getOwnerRatingSummary(ownerId)
                val n = when {
                    summary.has("reviewCount") -> summary.optLong("reviewCount", 0)
                    else -> summary.optLong("review_count", 0)
                }.toInt().coerceAtLeast(0)
                val avgAny = summary.opt("averageRating") ?: summary.opt("average_rating")
                val avg = when (avgAny) {
                    is Number -> avgAny.toDouble()
                    else -> avgAny?.toString()?.toDoubleOrNull()
                }
                if (avg != null && n > 0) {
                    valueView.text = if (n == 1) {
                        getString(R.string.host_rating_single_review, avg)
                    } else {
                        getString(R.string.host_rating_reviews, avg, n)
                    }
                } else {
                    valueView.text = getString(R.string.host_rating_none)
                }
            } catch (e: ApiException) {
                if (SessionHelper.handleUnauthorized(this@MainActivity, e)) return@launch
                valueView.text = getString(R.string.host_rating_none)
            } catch (_: Exception) {
                valueView.text = getString(R.string.host_rating_none)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.dashboard_menu, menu)
        tintToolbarOverflowIcon()
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_my_requests).isVisible = UserJson.isRenter(userJson)
        menu.findItem(R.id.action_my_listings).isVisible = UserJson.isOwner(userJson)
        menu.findItem(R.id.action_owner_requests).isVisible = UserJson.isOwner(userJson)
        menu.findItem(R.id.action_owner_settings).isVisible = UserJson.isOwner(userJson)
        val result = super.onPrepareOptionsMenu(menu)
        tintToolbarOverflowIcon()
        return result
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_my_requests -> {
            startActivity(Intent(this, MyRentalRequestsActivity::class.java))
            true
        }
        R.id.action_my_listings -> {
            startActivity(Intent(this, MyListingsActivity::class.java))
            true
        }
        R.id.action_owner_requests -> {
            startActivity(Intent(this, OwnerRentalRequestsActivity::class.java))
            true
        }
        R.id.action_owner_settings -> {
            startActivity(Intent(this, OwnerSettingsActivity::class.java))
            true
        }
        R.id.action_sign_out -> {
            authPrefs.clear()
            startActivity(
                Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                },
            )
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun tintToolbarOverflowIcon() {
        if (!::toolbar.isInitialized) return
        toolbar.overflowIcon?.mutate()?.let { d ->
            DrawableCompat.setTint(d, ContextCompat.getColor(this, R.color.white))
            toolbar.overflowIcon = d
        }
    }

    private fun filteredList(): List<JSONObject> =
        allListings.filter { ListingJson.matchesFilter(it, filterKey) }

    private fun totalPagesFor(count: Int) = max(1, (count + perPage - 1) / perPage)

    private fun bindPage() {
        val filtered = filteredList()
        val totalPages = totalPagesFor(filtered.size)
        if (currentPage > totalPages) currentPage = totalPages

        val from = (currentPage - 1) * perPage
        val pageItems = if (from >= filtered.size) {
            emptyList()
        } else {
            filtered.subList(from, min(from + perPage, filtered.size))
        }

        adapter.replace(pageItems)

        val empty = findViewById<TextView>(R.id.empty_text)
        val pagBar = findViewById<View>(R.id.pagination_bar)
        val pageInd = findViewById<TextView>(R.id.page_indicator)

        empty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        pagBar.visibility = if (totalPages > 1) View.VISIBLE else View.GONE
        pageInd.text = "$currentPage / $totalPages"

        findViewById<MaterialButton>(R.id.btn_prev_page).isEnabled = currentPage > 1
        findViewById<MaterialButton>(R.id.btn_next_page).isEnabled = currentPage < totalPages
    }

    private fun loadListings() {
        val refresh = findViewById<SwipeRefreshLayout>(R.id.refresh)
        val status = findViewById<TextView>(R.id.status_text)
        refresh.isRefreshing = true
        status.visibility = View.GONE
        lifecycleScope.launch {
            try {
                val arr = api.getListings()
                allListings = jsonArrayToList(arr)
                bindPage()
            } catch (e: ApiException) {
                if (SessionHelper.handleUnauthorized(this@MainActivity, e)) return@launch
                status.text = getString(R.string.error_prefix, e.message ?: "")
                status.visibility = View.VISIBLE
            } catch (_: Exception) {
                status.text = getString(R.string.error_prefix, getString(R.string.unable_load_listings))
                status.visibility = View.VISIBLE
            } finally {
                refresh.isRefreshing = false
            }
        }
    }

    private fun jsonArrayToList(arr: JSONArray): List<JSONObject> {
        val out = ArrayList<JSONObject>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            out.add(o)
        }
        return out
    }
}
