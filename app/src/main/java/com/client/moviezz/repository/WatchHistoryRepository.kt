package com.client.moviezz.repository

import android.util.Log
import com.client.moviezz.db.room.AppDatabase
import com.client.moviezz.db.room.HistoryMovie

class WatchHistoryRepository(private val dao: AppDatabase) {
    fun getAllHistory() = dao.getWatchHistoryDao().getAllHistory()
    suspend fun insertHistory(history: HistoryMovie) {
        Log.d(
            "hoho",
            "Inserting history: movieId=${history.movieId}, lastPosition=${history.lastPosition}"
        )
        dao.getWatchHistoryDao().insertHistory(history)
    }

    suspend fun clearHistory() = dao.getWatchHistoryDao().clearHistory()

    suspend fun insertOrUpdate(history: HistoryMovie) {
        val existing = dao.getWatchHistoryDao().getByMovieId(history.movieId)
        if (existing != null) {
            val updated = history.copy(lastWatched = System.currentTimeMillis())
            Log.d(
                "hoho",
                "Updating history: movieId=${history.movieId}, lastPosition=${history.lastPosition}"
            )
            dao.getWatchHistoryDao().update(updated)
        } else {
            Log.d(
                "hoho",
                "Inserting new history: movieId=${history.movieId}, lastPosition=${history.lastPosition}"
            )
            dao.getWatchHistoryDao().insertHistory(history)
        }
    }

    suspend fun getLatestByMovieId(movieId: String): HistoryMovie? {
        Log.d("hoho", "Fetching latest history for movieId=$movieId")
        return dao.getWatchHistoryDao().getLatestByMovieId(movieId)
    }

    suspend fun getMovieById(movieId: String): HistoryMovie? {
        return dao.getWatchHistoryDao().getByMovieId(movieId)
    }

    suspend fun getByMovieIdAndLink(movieId: String, videoLink: String): HistoryMovie? {
        return dao.getWatchHistoryDao().getByMovieIdAndLink(movieId, videoLink)
    }
}