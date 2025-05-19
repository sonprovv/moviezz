package com.client.moviezz.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.client.moviezz.db.room.HistoryMovie
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history WHERE movieId = :movieId LIMIT 1")
    suspend fun getByMovieId(movieId: String): HistoryMovie?

    @Query("SELECT * FROM watch_history ORDER BY lastWatched DESC")
    fun getAllHistory(): Flow<List<HistoryMovie>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryMovie)

    @Query("SELECT * FROM watch_history WHERE movieId = :movieId ORDER BY lastWatched DESC LIMIT 1")
    suspend fun getLatestByMovieId(movieId: String): HistoryMovie?

    @Query("SELECT * FROM watch_history WHERE movieId = :movieId AND videoLink = :videoLink LIMIT 1")
    suspend fun getByMovieIdAndLink(movieId: String, videoLink: String): HistoryMovie?

    @Update
    suspend fun update(history: HistoryMovie)

    @Query("DELETE FROM watch_history")
    suspend fun clearHistory()
}
