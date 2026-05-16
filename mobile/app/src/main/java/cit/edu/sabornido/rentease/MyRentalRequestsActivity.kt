package cit.edu.sabornido.rentease

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

class MyRentalRequestsActivity : AppCompatActivity() {

    private lateinit var authPrefs: AuthPreferences
    private lateinit var api: RentEaseApi
    private var allRows: List<JSONObject> = emptyList()
    private var tabPending = true
    private lateinit var adapter: RenterRequestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authPrefs = AuthPreferences(this)
        if (!authPrefs.isLoggedIn() || !UserJson.isRenter(authPrefs.userJson())) {
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_request_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root)) { v, insets ->
            val b = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(b.left, b.top, b.right, b.bottom)
            insets
        }

        api = RentEaseApi(getString(R.string.api_base_url), authPrefs)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.title = getString(R.string.my_rental_requests)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val recycler = findViewById<RecyclerView>(R.id.recycler)
        adapter = RenterRequestAdapter(
            onRate = { requestId ->
                startActivity(
                    Intent(this, RateOwnerActivity::class.java)
                        .putExtra(RateOwnerActivity.EXTRA_REQUEST_ID, requestId),
                )
            },
            onViewListing = { listingId ->
                startActivity(
                    Intent(this, PropertyDetailActivity::class.java)
                        .putExtra(PropertyDetailActivity.EXTRA_LISTING_ID, listingId),
                )
            },
        )
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        findViewById<ChipGroup>(R.id.tabs).setOnCheckedStateChangeListener { _, ids ->
            if (ids.isEmpty()) return@setOnCheckedStateChangeListener
            tabPending = ids.first() == R.id.chip_pending
            refreshAdapter()
        }

        load()
    }

    private fun load() {
        val err = findViewById<TextView>(R.id.error)
        err.visibility = View.GONE
        lifecycleScope.launch {
            try {
                val arr = api.getRenterRequests()
                allRows = jsonArrayToList(arr)
                refreshAdapter()
            } catch (e: ApiException) {
                if (SessionHelper.handleUnauthorized(this@MyRentalRequestsActivity, e)) return@launch
                err.text = e.message
                err.visibility = View.VISIBLE
            }
        }
    }

    private fun refreshAdapter() {
        val visible = allRows.filter { row ->
            val st = RequestJson.statusUpper(row)
            if (tabPending) st == "PENDING" else st == "APPROVED" || st == "DECLINED"
        }
        adapter.submit(visible)

        val emptyHint = findViewById<TextView>(R.id.empty_hint)
        when {
            allRows.isEmpty() -> {
                emptyHint.setText(R.string.renter_no_requests_yet)
                emptyHint.visibility = View.VISIBLE
            }
            visible.isEmpty() -> {
                emptyHint.setText(R.string.no_pending_requests)
                emptyHint.visibility = View.VISIBLE
            }
            else -> emptyHint.visibility = View.GONE
        }
    }

    private fun jsonArrayToList(arr: JSONArray): List<JSONObject> {
        val out = ArrayList<JSONObject>(arr.length())
        for (i in 0 until arr.length()) {
            arr.optJSONObject(i)?.let { out.add(it) }
        }
        return out
    }
}

private class RenterRequestAdapter(
    private val onRate: (Long) -> Unit,
    private val onViewListing: (Long) -> Unit,
) : RecyclerView.Adapter<RenterRequestAdapter.VH>() {

    private var items: List<JSONObject> = emptyList()

    private val moneyFmt: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US).apply {
        maximumFractionDigits = 0
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val line1: TextView = v.findViewById(R.id.line1)
        val line2: TextView = v.findViewById(R.id.line2)
        val status: TextView = v.findViewById(R.id.status)
        val meta: TextView = v.findViewById(R.id.meta)
        val actions: View = v.findViewById(R.id.actions)
        val btn1: MaterialButton = v.findViewById(R.id.btn_primary)
        val btn2: MaterialButton = v.findViewById(R.id.btn_secondary)
    }

    fun submit(list: List<JSONObject>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_request_row, parent, false)
        return VH(v)
    }

    private fun formatMoneyNumber(n: Number?): String {
        if (n == null) return "—"
        return moneyFmt.format(n.toDouble())
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val r = items[position]
        holder.line1.text = RequestJson.listingTitle(r)
        holder.line2.text = RequestJson.listingLocation(r)
        val st = RequestJson.statusUpper(r)
        holder.status.text = when (st) {
            "PENDING" -> "Pending"
            "APPROVED" -> "Approved"
            "DECLINED" -> "Declined"
            else -> st
        }
        val lease = r.opt("leaseDurationMonths") ?: r.opt("lease_duration_months")
        val leaseStr = when (lease) {
            is Number -> "${lease.toInt()} mo"
            else -> "—"
        }
        val incomeVal = r.opt("monthlyIncome") ?: r.opt("monthly_income")
        val incStr = when (incomeVal) {
            is Number -> formatMoneyNumber(incomeVal)
            else -> "—"
        }
        val rent = RequestJson.monthlyPrice(r)?.let { formatMoneyNumber(it) } ?: "—"
        val pstart = r.optString("preferredStartDate", r.optString("preferred_start_date", ""))
        val emp = r.optString("employmentStatus", r.optString("employment_status", ""))
        val requestId = RequestJson.id(r)
        val rated = RequestJson.ratingSubmitted(r)
        val sb = StringBuilder()
        sb.append("Start: ").append(pstart.ifEmpty { "—" })
        sb.append(" · Lease: ").append(leaseStr)
        sb.append(" · Income: ").append(incStr)
        sb.append(" · Rent: ").append(rent).append("/mo")
        sb.append(" · ").append(emp)
        if (st == "APPROVED" && rated) {
            sb.append("\nYou already submitted a rating for this application.")
        }
        holder.meta.text = sb.toString()

        val listingId = RequestJson.listingId(r) ?: -1L
        val canRate = st == "APPROVED" && !rated && requestId > 0
        val canView = listingId > 0 && (st == "PENDING" || st == "DECLINED")

        when {
            canRate && canView -> {
                holder.actions.visibility = View.VISIBLE
                holder.btn1.text = holder.itemView.context.getString(R.string.rate_owner)
                holder.btn1.setOnClickListener { onRate(requestId) }
                holder.btn2.visibility = View.VISIBLE
                holder.btn2.text = holder.itemView.context.getString(R.string.view_listing)
                holder.btn2.setOnClickListener { onViewListing(listingId) }
            }
            canRate -> {
                holder.actions.visibility = View.VISIBLE
                holder.btn2.visibility = View.GONE
                holder.btn1.text = holder.itemView.context.getString(R.string.rate_owner)
                holder.btn1.setOnClickListener { onRate(requestId) }
            }
            canView -> {
                holder.actions.visibility = View.VISIBLE
                holder.btn2.visibility = View.GONE
                holder.btn1.text = holder.itemView.context.getString(R.string.view_listing)
                holder.btn1.setOnClickListener { onViewListing(listingId) }
            }
            else -> holder.actions.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = items.size
}
