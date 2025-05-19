package com.client.moviezz.adapters

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.client.moviezz.R
import com.client.moviezz.models.SubVideo

@Suppress("DEPRECATION")
class EpisodeFullScreenAdapter(
    private val filmId: Int,
    private val avatar: String
) : RecyclerView.Adapter<EpisodeFullScreenAdapter.EpisodeViewHolder>() {

    private var episodeList: List<SubVideo> = emptyList()
    private var selectedPosition = 0

    var onItemClick: ((SubVideo) -> Unit)? = null

    @SuppressLint("NotifyDataSetChanged")
    inner class EpisodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val episodeNumber: TextView = itemView.findViewById(R.id.tv_episode_film_player)
        val ivPhotoFilmPlayer: ImageView = itemView.findViewById(R.id.iv_photo_film_player)
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val selectedEpisode = episodeList[position]
                    Log.d("EpisodeFullScreenAdapter", "Clicked episode: ${selectedEpisode.episode}, link: ${selectedEpisode.link}")
                    onItemClick?.invoke(selectedEpisode)
                    // Cập nhật vị trí được chọn
                    selectedPosition = position
                    notifyDataSetChanged() // Refresh để đổi màu
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_episode_of_player, parent, false)
        return EpisodeViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        val episode = episodeList[position]

        holder.episodeNumber.text = "Tập ${episode.episode}"

        Glide.with(holder.itemView)
            .load(Uri.parse(avatar))
            .apply(RequestOptions().transform(RoundedCorners(20)))
            .into(holder.ivPhotoFilmPlayer)

        // ✅ Highlight tập đang được chọn
        if (position == selectedPosition) {
            holder.episodeNumber.setTextColor(holder.itemView.context.getColor(R.color.chu_dao))
//            holder.itemView.setBackgroundColor(
//                ContextCompat.getColor(holder.itemView.context, R.color.chu_dao)
//
//            )
        } else {
//            holder.itemView.setBackgroundColor(
//                ContextCompat.getColor(holder.itemView.context, android.R.color.transparent)
//            )
            holder.episodeNumber.setTextColor(holder.itemView.context.getColor(R.color.white))

        }

    }

    override fun getItemCount(): Int = episodeList.size

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: List<SubVideo>) {
        episodeList = list.sortedBy { it.episode }
        notifyDataSetChanged()
    }

    fun setSelectedEpisode(episode: SubVideo) {
        val newPosition = episodeList.indexOf(episode)
        if (newPosition != -1 && newPosition != selectedPosition) {
            val previous = selectedPosition
            selectedPosition = newPosition
            notifyItemChanged(previous)
            notifyItemChanged(newPosition)
        }
    }
}
