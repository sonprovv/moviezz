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
import androidx.core.content.ContextCompat.startActivity
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.client.moviezz.R
import com.client.moviezz.models.Category
import com.client.moviezz.views.DetailActivity

@Suppress("DEPRECATION")
class CategoryAdapter : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
    private var categoryList: List<Category> = emptyList()
    @SuppressLint("NotifyDataSetChanged")
    fun setData(list: List<Category>) {
        categoryList = list
        notifyDataSetChanged()
    }

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvType: TextView = itemView.findViewById(R.id.tv_type_category)
        val ivMainPhoto: ImageView = itemView.findViewById(R.id.iv_main_photo_category)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_name_main_film_category)
        val tvViewNumber: TextView = itemView.findViewById(R.id.tv_view_number_category)
        val recyclerView: RecyclerView = itemView.findViewById(R.id.recycler_view_list_film_category)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    @OptIn(UnstableApi::class)
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categoryList[position]
        holder.tvType.text = category.name

        // Check if the category has films
        val mainFilm = category.films?.maxByOrNull { it.viewNumber ?: 0 }
        mainFilm?.let { film ->
            holder.tvTitle.text = film.name
            holder.tvViewNumber.text = film.viewNumber.let { "${it / 1000}k views" } ?: "0k views"
            if (film.avatar.isNotEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(Uri.parse(film.avatar))
                    .apply(RequestOptions().transform(RoundedCorners(20)))
                    .into(holder.ivMainPhoto)

            }

            // Set up the film adapter with the category films
            val filmAdapter = FilmOfCategoryAdapter().apply {
                category.films?.let { setData(it) }
            }
            holder.recyclerView.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = filmAdapter
                setHasFixedSize(true)
            }
        }
        holder.ivMainPhoto.setOnClickListener{
            val intent = Intent(holder.itemView.context, DetailActivity::class.java)
            intent.putExtra("film_id", mainFilm?.id)
            intent.putExtra("film_avatar", mainFilm?.avatar)
            startActivity(holder.itemView.context, intent, null)
        }
    }

    override fun getItemCount(): Int = categoryList.size
}