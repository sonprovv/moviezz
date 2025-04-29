package com.client.moviezz.models

import kotlinx.serialization.Serializable

@Serializable
data class PhotoViewPager(
    val id: Int,
    val avatar: String
) : java.io.Serializable {
    constructor(movie: Movie) : this(
        id = movie.id,
        avatar = movie.avatar
    )
}