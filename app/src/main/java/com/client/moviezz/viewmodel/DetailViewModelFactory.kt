package com.client.moviezz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.client.moviezz.repository.WatchHistoryRepository

class DetailViewModelFactory(
    private val repository: WatchHistoryRepository
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MovieViewModel.DetailViewModel::class.java)) {
            return MovieViewModel.DetailViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
