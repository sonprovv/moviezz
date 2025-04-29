package com.client.moviezz.models

data class FilmDetailList(
    val errorCode: String,
    val message: String,
    val data: FilmDetail
)

data class FilmDetail (
    val id : Int,
    val categoryId : Int,
    val star : Int,
    val imdbPoInteger : Int,
    val name : String,
    val isActive : Int,
    val created: String,
    val modified: String,
    val modifiedDate: String,
    val duration : Int,
    val isSlide : Int,
    val isHome : Int,
    val isHot : Int,
    val isSeri : Int,
    val isCinema: Int,
    val isPopular: Int ?= null,
    val isFree : Int,
    val viewNumber : Int,
    val subCategoryId : Any ?= null,
    val caption : String,
    val order_ : Long,
    val nameKey: String,
    val avatar : String,
    val link : String ?= null,
    val description : String,
    val descriptionEx : String,
    val videoTrailer : Any ?= null,
    val poster : String,
    val quality : String,
    val autoOn : Int,
    val schedule : Any ?= null,
    val episodesTotal : Int,
    val subVideoList : List<SubVideo> ?= null,
    val subTitleList : List<SubTitle> ?= null,
    val redirectLink : String
)

data class SubTitle (
    val id : Int,
    val title : String,
    val source : String,
    val videoId : Int,
    val subVideoId : Int,
    val default_ : Int,
)
data class SubVideo(
    val id : Int,
    val videoId: Int,
    val created: String ?= null,
    val isActive : Int,
    val episode : Int,
    val link : String,
    val autoOn : Int,
    val schedule : Any ?= null,
    val resolution : String,
    val subTitleList : List<SubTitle>
)
