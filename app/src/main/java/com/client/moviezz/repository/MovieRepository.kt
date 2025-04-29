package com.client.moviezz.repository

import com.client.moviezz.services.RetrofitClient
import com.client.moviezz.utils.Utils

class MovieRepository {
    private val util = Utils
    private val apiService = RetrofitClient.apiService
    private val wsToken = util.WSTOKEN
    private val secApi = util.SEC_API

    suspend fun getMovieHome(msisdn: String) =
        apiService.getListMovie(msisdn = msisdn, wsToken = wsToken, secApi = secApi)

    suspend fun getCategoryList(msisdn: String) =
        apiService.getCategoryList(msisdn = msisdn, wsToken = wsToken)

    suspend fun getFilmOfCategory(
        categoryId: Int,
        msisdn: String,
        language: String,
        page: Int,
        size: Int
    ) = apiService.getFilmsByCategory(
        categoryId = categoryId,
        msisdn = msisdn,
        language = "",
        page = 0,
        size = 100,
        wsToken = wsToken
    )

    suspend fun getFilmDetail(filmId: Int, msisdn: String) = apiService.getFilmDetail(
        filmId = filmId,
        msisdn = msisdn,
        wsToken = wsToken,
        secApi = secApi
    )

    suspend fun getRelatedFilms(
        categoryId: Int,
        msisdn: String,
        page: Int,
        size: Int,
        language: String
    ) = apiService.getRelatedFilms(
        categoryId = categoryId,
        msisdn = msisdn,
        page = 0,
        size = 100,
        language = "",
        wsToken = wsToken,
        secApi = secApi
    )

    suspend fun getMovieSearch(keySearch: String, msisdn: String, page: Int, size: Int) =
        apiService.searchFilms(
            keySearch = keySearch,
            msisdn = msisdn,
            page = 0,
            size = 100,
            wsToken = wsToken
        )
}