package com.client.moviezz.models

data class CategoryList (
    val errorCode : String,
    val message : String,
    val data : List<Category>
)
data class Category (
    val id : Int,
    val name : String,
    val isActive : Int,
    val type : Int,
    val subCategoryId : String ? = null,
    val order_ : Long,
    val packageCode : String ? = null,
    var films: List<Film>? = null
)