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
    private val fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    private var onEpisodeClick: ((SubVideo) -> Unit)? = null
    private var onMovieClick: ((Movie) -> Unit)? = null
    private var currentEpisodeIndex = 0

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DanhSachTapFragment().apply {
                Log.d("hoho", "Setting onEpisodeClickListener for DanhSachTapFragment")
                setOnEpisodeClickListener { episode ->
                    Log.d("hoho", "Episode clicked: ${episode.episode}, link: ${episode.link}")
                    onEpisodeClick?.invoke(episode)
                }
                if (currentEpisodeIndex > 0) {
                    Log.d("hoho", "Restoring episode index: $currentEpisodeIndex")
                    onEpisodeSelected(currentEpisodeIndex)
                }
            }
            1 -> RelatedFilmFragment().apply {
                Log.d("hoho", "Setting onMovieClickListener for RelatedFilmFragment")
                setOnMovieClickListener(onMovieClick)
            }
            else -> throw IllegalStateException("Invalid position: $position")
        }
    }

    fun setOnEpisodeClickListener(listener: ((SubVideo) -> Unit)?) {
        Log.d("hoho", "onEpisodeClickListener set: $listener")
        this.onEpisodeClick = listener
    }

    fun setOnMovieClickListener(listener: ((Movie) -> Unit)?) {
        Log.d("hoho", "onMovieClickListener set: $listener")
        this.onMovieClick = listener
    }

    fun notifyEpisodeSelected(index: Int) {
        if (index < 0) {
            Log.e("hoho", "Invalid episode index: $index")
            return
        }
        val currentFragment = getFragment(0)
        if (currentFragment is DanhSachTapFragment && currentFragment.isAdded && currentFragment.view != null) {
            Log.e("hoho", "DDĐ Notify episode selected: $index")
            currentFragment.onEpisodeSelected(index)
            currentEpisodeIndex = index
        } else {
            Log.w("hoho", "DanhSachTapFragment not ready or not found, saving index: $index")
            currentEpisodeIndex = index
        }
    }

    private fun getFragment(position: Int): Fragment? {
        return try {
            val fragment = fragmentManager.findFragmentByTag("f$position")
            if (fragment != null && fragment.isAdded) {
                fragment
            } else {
                Log.w("hoho", "Fragment at position $position is not added or null")
                null
            }
        } catch (e: Exception) {
            Log.e("hoho", "Error getting fragment at position $position: ${e.message}")
            null
        }
    }
}