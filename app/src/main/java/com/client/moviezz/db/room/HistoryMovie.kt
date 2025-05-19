package com.client.moviezz.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_history")
data class HistoryMovie (
    @PrimaryKey val videoLink: String,
    val movieId: String,
    val movieTitle: String,
    val movieImage: String,
    val lastPosition: Long,
    val duration: Long,
    val timestamp: Long = System.currentTimeMillis()
)
