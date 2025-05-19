package com.client.moviezz.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.client.moviezz.R
import com.client.moviezz.models.SubVideo

@Suppress("DEPRECATION")
class EpisodeListAdapter : RecyclerView.Adapter<EpisodeListAdapter.EpisodeViewHolder>() {
    private var episodeList: List<SubVideo> = emptyList()
    private var selectedPosition = 0 // Mặc định chọn position 0

    var onItemClick: ((SubVideo) -> Unit)? = null

    @SuppressLint("NotifyDataSetChanged")
    inner class EpisodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val episodeNumber: TextView = itemView.findViewById(R.id.tv_tap)
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val selectedEpisode = episodeList[position]
                    Log.d("EpisodeListAdapter", "Clicked episode: ${selectedEpisode.episode}, link: ${selectedEpisode.link}")
                    onItemClick?.invoke(selectedEpisode)
                    // Cập nhật vị trí được chọn
                    selectedPosition = position
                    notifyDataSetChanged() // Refresh để đổi màu
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EpisodeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tap_phim, parent, false)
        return EpisodeViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        val episode = episodeList[position]
        holder.episodeNumber.text = episode.episode.toString()

        // Đổi màu dựa trên selectedPosition
        if (position == selectedPosition) {
            holder.itemView.setBackgroundResource(R.drawable.bg_episode_selected)
            holder.episodeNumber.setTextColor(holder.itemView.context.getColor(R.color.black))
        } else {
            holder.itemView.setBackgroundResource(R.drawable.bg_episode_default)
            holder.episodeNumber.setTextColor(holder.itemView.context.getColor(R.color.white))
        }
    }

    override fun getItemCount(): Int = episodeList.size

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: List<SubVideo>) {
        episodeList = list.sortedBy { it.episode }
//        selectedPosition = if (list.isNotEmpty()) 0 else RecyclerView.NO_POSITION // Chọn position 0 nếu danh sách không rỗng
        notifyDataSetChanged()
//        val currentSelected = episodeList.getOrNull(selectedPosition)
//        if (currentSelected != null) {
//            setSelectedEpisode(currentSelected)
//        }
    }
    fun setSelectedEpisode(episode: SubVideo) {
        val newPosition = episodeList.indexOf(episode)
        Log.e("hoho", "AAAA newPosition: $newPosition" + "position: $selectedPosition")
        if (newPosition != -1 && newPosition != selectedPosition) {

            val previous = selectedPosition
            selectedPosition = newPosition
            notifyItemChanged(previous)
            notifyItemChanged(newPosition)

        }
        Log.e("hoho", "AAAA episode: $episode" + "position: $selectedPosition")
    }

}