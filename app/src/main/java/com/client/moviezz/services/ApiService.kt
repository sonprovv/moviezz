package com.client.moviezz.services
import com.client.moviezz.models.CategoryList
import com.client.moviezz.models.FilmDetailList
import com.client.moviezz.models.FilmList
import com.client.moviezz.models.GetOTPResponse
import com.client.moviezz.models.GetTokenResponse
import com.client.moviezz.models.MovieHome
import com.client.moviezz.models.MovieListSearch
import com.client.moviezz.models.RelatedFilmList
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
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

    @POST("genotp/v31")
    suspend fun genOtp(
        @Query("countryCode") countryCode: String,
        @Query("os_version") osVersion: String,
        @Query("device") device: String,
        @Query("version") version: String,
        @Query("platform") platform: String,
        @Query("revision") revision: String,
        @Query("username") username: String,
        @Header("uuid") uuid: String,
        @Header("sec-api") secApi: String,
        @Header("languageCode") languageCode: String,
    ): GetOTPResponse

    @POST("genotp/get/token")
    suspend fun getToken(
        @Query("msisdn") msisdn: String,
        @Query("otp") otp: String,
        @Header("uuid") uuid: String,
        @Header("sec-api") secApi: String,
        @Header("languageCode") languageCode: String,
        @Header("countryCode") countryCode: String,
    ): GetTokenResponse
}
