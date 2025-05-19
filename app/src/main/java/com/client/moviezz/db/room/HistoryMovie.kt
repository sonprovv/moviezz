package com.client.moviezz.db.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_history")
data class HistoryMovie (
    val videoLink: String,
    @PrimaryKey val movieId: String,
    val movieTitle: String,
    val movieImage: String,
    val lastPosition: Long,
    val duration: Long,
    val lastWatched: Long = System.currentTimeMillis(),
    val episodeNumber: String? = null
)
