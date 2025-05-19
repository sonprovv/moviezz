package com.client.moviezz.models

import kotlinx.serialization.Serializable
import java.util.Date

data class FilmList(
    val errorCode: String,
    val message: String,
    val data: List<Film>
)

data class Film(
    val id: Int,
    val categoryId: Int,
    val star: Int,
    val imdbPoInteger: Int,
    val name: String,
    val isActive: Int,
    val created: Date,
    val modifier: String,
    val modifiedDate: Date,
    val duration: Int,
    val isSlide: Int,
    val isHome: Int,
    val isHot: Int,
    val isSeri: Int,
    val isCinema: Int? = null,
    val isFree: Int,
    val viewNumber: Int,
    val subCategoryId: Int? = null,
    val caption: String,
    val order_: Long,
    val nameKey: String,
    val avatar: String,
    val link: String ?= null,
    val description: String,
    val descriptionEx: String,
    val videoTrailer: String? = null,
    val poster: String,
    val quality: String,
    val autoOn: Int,
    val schedule: String? = null,
    val episodesTotal: Int,
    val subVideoList: List<SubVideo>? = null,
    val subTitleList: Any? = null,
    val redirectLink: String
)