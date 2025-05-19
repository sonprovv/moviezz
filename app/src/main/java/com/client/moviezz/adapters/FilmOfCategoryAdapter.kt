package com.client.moviezz.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.client.moviezz.R
import com.client.moviezz.models.Film
import com.client.moviezz.views.DetailActivity

class FilmOfCategoryAdapter :
    RecyclerView.Adapter<FilmOfCategoryAdapter.FilmOfCategoryViewHolder>() {
    private var listFilm: List<Film> = emptyList()
    private var hotFilm: Film? = null
    @SuppressLint("NotifyDataSetChanged")
    fun setData(list: List<Film>) {
        hotFilm = list.maxByOrNull { it.viewNumber ?: 0 }
        listFilm = list.filter { it != hotFilm }
        notifyDataSetChanged()
    }

    class FilmOfCategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPhotoFilm: ImageView = itemView.findViewById(R.id.iv_photo_film)
        val tvNameFilm: TextView = itemView.findViewById(R.id.tv_name_film)
        val tvViewNumberFilm: TextView = itemView.findViewById(R.id.tv_view_number_film)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilmOfCategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_film_of_category, parent, false)
        return FilmOfCategoryViewHolder(view)
    }

    @OptIn(UnstableApi::class)
    override fun onBindViewHolder(holder: FilmOfCategoryViewHolder, position: Int) {

        val film = listFilm[position]
        holder.tvNameFilm.text = film.name
        holder.tvViewNumberFilm.text = film.viewNumber.let { "${it / 1000}k views" } ?: "0k views"

        Glide.with(holder.itemView)
            .load(Uri.parse(film.avatar))
            .apply(
                RequestOptions()
                    .transform(RoundedCorners(20))
            )
            .into(holder.ivPhotoFilm)
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailActivity::class.java)
            intent.putExtra("film_id", film.id)
            intent.putExtra("film_avatar", film.avatar)
            holder.itemView.context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int = listFilm.size
}