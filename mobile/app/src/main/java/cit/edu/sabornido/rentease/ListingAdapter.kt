package cit.edu.sabornido.rentease

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import org.json.JSONObject

class ListingAdapter(
    private var items: List<JSONObject>,
    private val onOpen: (JSONObject) -> Unit,
) : RecyclerView.Adapter<ListingAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view as MaterialCardView
        val image: ImageView = view.findViewById(R.id.listing_image)
        val title: TextView = view.findViewById(R.id.listing_title)
        val location: TextView = view.findViewById(R.id.listing_location)
        val price: TextView = view.findViewById(R.id.listing_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_listing_card, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val o = items[position]
        holder.title.text = ListingJson.title(o)
        holder.location.text = ListingJson.location(o)
        holder.price.text = ListingJson.priceLabel(o)
        holder.image.loadListingPhoto(ListingImages.firstUrl(ListingJson.imageUrls(o)))
        holder.card.setOnClickListener { onOpen(o) }
    }

    override fun getItemCount(): Int = items.size

    fun replace(newItems: List<JSONObject>) {
        items = newItems
        notifyDataSetChanged()
    }
}
