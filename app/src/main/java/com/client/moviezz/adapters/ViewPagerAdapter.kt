package com.client.moviezz.adapters
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.client.moviezz.R
import com.client.moviezz.models.PhotoViewPager

class ViewPagerAdapter(
    private var photoList: List<PhotoViewPager>
) : RecyclerView.Adapter<ViewPagerAdapter.PhotoViewHolder>() {

    class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.iv_item_view_pager)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progress_loading_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo_view_pager, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = photoList[position]
        holder.progressBar.visibility = View.VISIBLE
        val avatarUrl = photo.avatar
        if (avatarUrl.isNotEmpty()) {
            Glide.with(holder.itemView)
                .load(Uri.parse(avatarUrl))
                .apply(
                    RequestOptions()
                        .transform(RoundedCorners(20))
                )
                .into(holder.imageView)
        }
    }
    override fun getItemCount(): Int = photoList.size

    fun updateData(listPhoto: List<PhotoViewPager>) {
        photoList = listPhoto
        notifyDataSetChanged()
    }
}