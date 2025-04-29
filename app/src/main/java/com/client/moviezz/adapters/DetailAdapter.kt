package com.client.moviezz.adapters

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.client.moviezz.models.Movie
import com.client.moviezz.models.SubVideo
import com.client.moviezz.views.DanhSachTapFragment
import com.client.moviezz.views.RelatedFilmFragment

class DetailAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    private var onEpisodeClick: ((SubVideo) -> Unit)? = null
    private var onMovieClick: ((Movie) -> Unit)? = null

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DanhSachTapFragment().apply {
                Log.d("DetailAdapter", "Setting onEpisodeClickListener for DanhSachTapFragment")
                setOnEpisodeClickListener(onEpisodeClick)
            }
            1 -> RelatedFilmFragment().apply {
                Log.d("DetailAdapter", "Setting onMovieClickListener for SuggestionFragment")
                setOnMovieClickListener(onMovieClick)
            }
            else -> throw IllegalStateException("Invalid position")
        }
    }

    fun setOnEpisodeClickListener(listener: ((SubVideo) -> Unit)?) {
        Log.d("DetailAdapter", "onEpisodeClickListener set: $listener")
        this.onEpisodeClick = listener
    }

    fun setOnMovieClickListener(listener: ((Movie) -> Unit)?) {
        Log.d("DetailAdapter", "onMovieClickListener set: $listener")
        this.onMovieClick = listener
    }
}