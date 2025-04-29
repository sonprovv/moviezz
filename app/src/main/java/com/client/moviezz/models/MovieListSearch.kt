package com.client.moviezz.models

data class MovieListSearch(
    val errorCode: String,
    val message: String,
    val data: List<Film>
)
