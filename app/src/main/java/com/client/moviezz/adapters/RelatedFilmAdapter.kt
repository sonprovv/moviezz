package com.client.moviezz.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.client.moviezz.R
import com.client.moviezz.models.Film
import com.client.moviezz.views.DetailActivity

class RelatedFilmAdapter  : RecyclerView.Adapter<RelatedFilmAdapter.RelatedFilmViewHolder>(){
    private var relatedFilms: List<Film> = emptyList()
    var onItemClick: ((Film) -> Unit)? = null
    class RelatedFilmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPhotoFilm : ImageView = itemView.findViewById(R.id.iv_photo_film)
        val tvNameFilm : TextView = itemView.findViewById(R.id.tv_name_film)
        val tvViewNumberFilm : TextView = itemView.findViewById(R.id.tv_view_number_film)
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RelatedFilmViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_related_film_of_category, parent, false)
        return RelatedFilmViewHolder(view)
    }

    @OptIn(UnstableApi::class)
    override fun onBindViewHolder(holder: RelatedFilmViewHolder, position: Int) {
        val relatedFilm = relatedFilms[position]
        holder.tvNameFilm.text = relatedFilm.name
        holder.tvViewNumberFilm.text = "${relatedFilm.viewNumber / 1000}k views" ?: "0k views"
        Glide.with(holder.itemView.context)
            .load(relatedFilm.avatar)
            .into(holder.ivPhotoFilm)
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailActivity::class.java)
            intent.putExtra("film_id", relatedFilm.id)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = relatedFilms.size
    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: List<Film>) {
        relatedFilms = list
        Log.d("hoho", "submitList called with size=${list.size}")
        Log.d("hoho", "First film: ${list.firstOrNull()?.name}")
        notifyDataSetChanged()
    }
}