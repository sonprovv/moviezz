package com.client.moviezz.models

data class MovieHome(
    val errorCode: String,
    val message: String,
    val data: List<Movie>
)

data class Movie(
    val id: Int,
    val categoryId: Int,
    val star: Int,
    val imdbPoInteger: Int,
    val name: String,
    val isActive: Int,
    val created: String,
    val modifier: String,
    val modifiedDate: String,
    val duration: Int,
    val isSlide: Int,
    val isHome: Int,
    val isHot: Int,
    val isSeri: Int,
    val isCinema: Int,
    val isPopular: Int?, // vì có thể null
    val isFree: Int,
    val viewNumber: Int,
    val subCategoryId: Int?, // vì có thể null
    val caption: String,
    val order_: Long,
    val nameKey: String,
    val avatar: String,
    val link: String,
    val description: String,
    val descriptionEx: String,
    val videoTrailer: String?,
    val poster: String, // list poster dạng String, có thể xử lý sau
    val quality: String,
    val autoOn: Int,
    val schedule: String?,
    val episodesTotal: Int,
    val subVideoList: List<String>?, // nếu là list object thì cần cụ thể hóa
    val subTitleList: List<String>?, // nếu là list object thì cần cụ thể hóa
    val redirectLink: String
)

