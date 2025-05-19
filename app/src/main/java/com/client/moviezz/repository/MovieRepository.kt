package com.client.moviezz.repository

import com.client.moviezz.services.RetrofitClient
import com.client.moviezz.utils.Utils

class MovieRepository {
    private val util = Utils
    private val apiService = RetrofitClient.apiService
    private val wsToken = util.WSTOKEN
    private val secApi = util.SEC_API
    private val uuid = util.UUID

    suspend fun getMovieHome(msisdn: String) =
        apiService.getListMovie(msisdn = msisdn, wsToken = wsToken, secApi = secApi)

    suspend fun getCategoryList(msisdn: String) =
        apiService.getCategoryList(msisdn = msisdn, wsToken = wsToken)

    suspend fun getFilmOfCategory(
        categoryId: Int,
        msisdn: String,
    ) = apiService.getFilmsByCategory(
        categoryId = categoryId,
        msisdn = msisdn,
        language = "",
        page = 0,
        size = 100,
        wsToken = wsToken
    )

    suspend fun getFilmDetail(filmId: Int, msisdn: String, secApi: String) = apiService.getFilmDetail(
        filmId = filmId,
        msisdn = msisdn,
        wsToken = wsToken,
        secApi = secApi
    )

    suspend fun getRelatedFilms(
        categoryId: Int,
        msisdn: String,
        secApi: String
    ) = apiService.getRelatedFilms(
        categoryId = categoryId,
        msisdn = msisdn,
        page = 0,
        size = 100,
        language = "",
        wsToken = wsToken,
        secApi = secApi
    )

    suspend fun getMovieSearch(keySearch: String, msisdn: String) =
        apiService.searchFilms(
            keySearch = keySearch,
            msisdn = msisdn,
            page = 0,
            size = 100,
            wsToken = wsToken
        )

    suspend fun getOTP(
        username: String
    ) =
        apiService.genOtp(
            countryCode = "67",
            osVersion = "",
            device = "",
            version = "",
            platform = "Android",
            revision = "",
            username = username,
            uuid = uuid,
            secApi = secApi,
            languageCode = "en"
        )

    suspend fun getToken(msisdn: String, otp: String, uuId: String) =
        apiService.getToken(
            msisdn = msisdn,
            otp = otp,
            uuid = uuId,
            secApi = secApi,
            languageCode = "en",
            countryCode = "67"
        )



}