package com.client.moviezz.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.client.moviezz.R
import com.client.moviezz.db.room.HistoryMovie
import com.client.moviezz.repository.WatchHistoryRepository

class HistoryWatchMovieAdapter(
    private val onItemClick: (HistoryMovie) -> Unit,
    private val historyRepository: WatchHistoryRepository
) : ListAdapter<HistoryMovie, HistoryWatchMovieAdapter.ViewHolder>(HistoryMovieDiffCallback()) {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageMovie: ImageView = itemView.findViewById(R.id.iv_photo_film)
        val seekBar: SeekBar = itemView.findViewById(R.id.seek_bar_current)
        val totalTime: TextView = itemView.findViewById(R.id.tv_total_time)
        val nameMovie: TextView = itemView.findViewById(R.id.tv_name_film)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recently_watched, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val movie = getItem(position)
        holder.nameMovie.text = movie.movieTitle
        holder.totalTime.text = formatTime(movie.duration)
        holder.seekBar.max = (movie.duration / 1000).toInt()
        holder.seekBar.progress = (movie.lastPosition / 1000).toInt()
        Glide.with(holder.itemView)
            .load(movie.movieImage)
            .apply(RequestOptions().transform(RoundedCorners(20)))
            .into(holder.imageMovie)
        holder.itemView.setOnClickListener {
            onItemClick(movie)
        }
    }

    @SuppressLint("DefaultLocale")
    private fun formatTime(timeMs: Long): String {
        if (timeMs <= 0) return "00:00"
        val totalSeconds = timeMs / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0)
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        else
            String.format("%02d:%02d", minutes, seconds)
    }
}

class HistoryMovieDiffCallback : DiffUtil.ItemCallback<HistoryMovie>() {
    override fun areItemsTheSame(oldItem: HistoryMovie, newItem: HistoryMovie): Boolean {
        return oldItem.videoLink == newItem.videoLink
    }

    override fun areContentsTheSame(oldItem: HistoryMovie, newItem: HistoryMovie): Boolean {
        return oldItem == newItem
    }
}