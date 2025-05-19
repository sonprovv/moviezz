package com.client.moviezz.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.client.moviezz.R
import com.client.moviezz.models.Film
import com.client.moviezz.views.DetailActivity

class FilmFullscreenAdapter : ListAdapter<Film, FilmFullscreenAdapter.RelatedFilmViewHolder>(FilmDiffCallback()) {

    // Callback để xử lý click item (nếu cần gọi từ ngoài adapter)
    var onItemClick: ((Film) -> Unit)? = null

    class RelatedFilmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPhotoFilm: ImageView = itemView.findViewById(R.id.iv_photo_film_player)
        private val tvNameFilm: TextView = itemView.findViewById(R.id.tv_name_film)

        fun bind(film: Film) {
            // Hiển thị tên phim
            tvNameFilm.text = film.name
            // Tải ảnh avatar bằng Glide
            Glide.with(itemView.context)
                .load(film.avatar)
                .into(ivPhotoFilm)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RelatedFilmViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_related_film_fullscreen, parent, false)
        return RelatedFilmViewHolder(view)
    }

    @OptIn(UnstableApi::class)
    override fun onBindViewHolder(holder: RelatedFilmViewHolder, position: Int) {
        val film = getItem(position)
        holder.bind(film)

        // Xử lý click item
        holder.itemView.setOnClickListener {
//            onItemClick?.invoke(film)
            // Chuyển đến DetailActivity với film_id
            val intent = Intent(holder.itemView.context, DetailActivity::class.java)
            intent.putExtra("film_id", film.id)
            holder.itemView.context.startActivity(intent)
        }
    }

    class FilmDiffCallback : DiffUtil.ItemCallback<Film>() {
        override fun areItemsTheSame(oldItem: Film, newItem: Film): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Film, newItem: Film): Boolean {
            return oldItem == newItem
        }
    }
}