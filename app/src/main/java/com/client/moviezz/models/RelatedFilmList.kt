package com.client.moviezz.models

data class RelatedFilmList(
    val errorCode: String,
    val message: String,
    val data: List<Film>
)
