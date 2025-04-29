package com.client.moviezz.services
import com.client.moviezz.models.Category
import com.client.moviezz.models.CategoryList
import com.client.moviezz.models.Film
import com.client.moviezz.models.FilmDetailList
import com.client.moviezz.models.FilmList
import com.client.moviezz.models.MovieHome
import com.client.moviezz.models.MovieListSearch
import com.client.moviezz.models.RelatedFilmList
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ApiService {

    @GET("api/v1/get-home-films")
    suspend fun getListMovie(
        @Query("msisdn") msisdn: String,
        @Header("wsToken") wsToken: String,
        @Header("sec-api") secApi: String
    ): MovieHome

    @GET("api/v1/search-films")
    suspend fun searchFilms(
        @Query("keySearch") keySearch: String,
        @Query("msisdn") msisdn: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Header("wsToken") wsToken: String
    ): MovieListSearch

    @GET("api/v1/get-film-category-list")
    suspend fun getCategoryList(
        @Query("msisdn") msisdn: String,
        @Header("wsToken") wsToken: String
    ): CategoryList

    @GET("api/v1/get-films-by-category")
    suspend fun getFilmsByCategory(
        @Query("categoryId") categoryId: Int,
        @Query("msisdn") msisdn: String,
        @Query("language") language: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Header("wsToken") wsToken: String
    ): FilmList

    @GET("api/v1/get-film-detail")
    suspend fun getFilmDetail(
        @Query("filmId") filmId: Int,
        @Query("msisdn") msisdn: String,
        @Header("wsToken") wsToken: String,
        @Header("sec-api") secApi: String
    ): FilmDetailList

    @GET("api/v1/get-related-film")
    suspend fun getRelatedFilms(
        @Query("categoryId") categoryId: Int,
        @Query("msisdn") msisdn: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("language") language: String,
        @Header("wsToken") wsToken: String,
        @Header("sec-api") secApi: String
    ): RelatedFilmList
}
