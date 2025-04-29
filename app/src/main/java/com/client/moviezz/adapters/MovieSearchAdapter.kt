package com.client.moviezz.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.client.moviezz.R
import com.client.moviezz.models.Film
import com.client.moviezz.views.DetailActivity

class MovieSearchAdapter : RecyclerView.Adapter<MovieSearchAdapter.MovieSearchViewHolder>() {
    private var movieList: List<Film> = emptyList()

    class MovieSearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivFilm: ImageView = itemView.findViewById(R.id.iv_photo_film_search)
        val tvName: TextView = itemView.findViewById(R.id.tv_name_film_search)
        val tvViewNumber: TextView = itemView.findViewById(R.id.tv_view_number_search)
        val tvLikeNumber: TextView = itemView.findViewById(R.id.tv_like_number_search)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MovieSearchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_film_search, parent, false)
        return MovieSearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieSearchViewHolder, position: Int) {
        val movie = movieList[position]
        holder.tvName.text = movie.name
        holder.tvViewNumber.text = "${movie.viewNumber / 1000}k views" ?: "0k views"
        holder.tvLikeNumber.text = "${movie.star} likes" ?: "0 likes"
        Glide.with(holder.itemView.context)
            .load(movie.avatar)
            .into(holder.ivFilm)
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailActivity::class.java)
            intent.putExtra("film_id", movie.id)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = movieList.size

    fun submitList(newList: List<Film>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = movieList.size
            override fun getNewListSize(): Int = newList.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return movieList[oldItemPosition].id == newList[newItemPosition].id
            }
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return movieList[oldItemPosition] == newList[newItemPosition]
            }
        }
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        movieList = newList
        diffResult.dispatchUpdatesTo(this)
    }
}