package cit.edu.sabornido.rentease

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class MyListingsActivity : AppCompatActivity() {

    private lateinit var authPrefs: AuthPreferences
    private lateinit var api: RentEaseApi
    private lateinit var listingsAdapter: MyListingsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authPrefs = AuthPreferences(this)
        if (!authPrefs.isLoggedIn() || !UserJson.isOwner(authPrefs.userJson())) {
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_my_listings)
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

        val recycler = findViewById<RecyclerView>(R.id.recycler)
        listingsAdapter = MyListingsAdapter(
            onEdit = { id ->
                startActivity(
                    Intent(this, ListingFormActivity::class.java)
                        .putExtra(ListingFormActivity.EXTRA_LISTING_ID, id),
                )
            },
            onDelete = { row ->
                AlertDialog.Builder(this)
                    .setTitle(R.string.delete_listing)
                    .setMessage("Remove this listing? This cannot be undone.")
                    .setPositiveButton(R.string.delete_listing) { _, _ ->
                        val id = ListingJson.id(row)
                        lifecycleScope.launch {
                            try {
                                api.deleteListing(id)
                                load(listingsAdapter)
                            } catch (e: ApiException) {
                                if (SessionHelper.handleUnauthorized(this@MyListingsActivity, e)) return@launch
                            }
                        }
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            },
        )
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = listingsAdapter

        load(listingsAdapter)
    }

    override fun onResume() {
        super.onResume()
        if (!::listingsAdapter.isInitialized) return
        load(listingsAdapter)
    }

    private fun load(adapter: MyListingsAdapter) {
        val empty = findViewById<TextView>(R.id.empty)
        val err = findViewById<TextView>(R.id.error)
        err.visibility = View.GONE
        lifecycleScope.launch {
            try {
                val arr = api.getMyListings()
                val list = jsonArrayToList(arr)
                adapter.submit(list)
                empty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            } catch (e: ApiException) {
                if (SessionHelper.handleUnauthorized(this@MyListingsActivity, e)) return@launch
                err.text = e.message
                err.visibility = View.VISIBLE
            }
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

private class MyListingsAdapter(
    private val onEdit: (Long) -> Unit,
    private val onDelete: (JSONObject) -> Unit,
) : RecyclerView.Adapter<MyListingsAdapter.VH>() {

    private var items: List<JSONObject> = emptyList()

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val thumb: ImageView = v.findViewById(R.id.thumb)
        val title: TextView = v.findViewById(R.id.title)
        val location: TextView = v.findViewById(R.id.location)
        val status: TextView = v.findViewById(R.id.status)
    }

    fun submit(list: List<JSONObject>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_my_listing, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val row = items[position]
        val cornerPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            8f,
            holder.itemView.resources.displayMetrics,
        )
        holder.thumb.loadListingPhoto(ListingImages.firstUrl(ListingJson.imageUrls(row)), cornerPx)
        holder.title.text = ListingJson.title(row)
        holder.location.text = ListingJson.location(row)
        holder.status.text = ListingJson.status(row).uppercase()
        val id = ListingJson.id(row)
        holder.itemView.findViewById<MaterialButton>(R.id.btn_edit).setOnClickListener { onEdit(id) }
        holder.itemView.findViewById<MaterialButton>(R.id.btn_delete).setOnClickListener { onDelete(row) }
    }

    override fun getItemCount(): Int = items.size
}
