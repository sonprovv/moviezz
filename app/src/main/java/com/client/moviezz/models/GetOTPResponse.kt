package com.client.moviezz.models

data class GetOTPResponse (
    val code : Int,
    val desc : String,
    val errorCode: Int,
    val subscription_package : List<subScriptionList> ?= null,
    val uuid : String ?= null,
    val ssl : String ?= null
)

data class subScriptionList(
    val domain_msg : String,
    val domain_file : String,
    val country : String,
    val ssl : String,
    val content : String
)

data class GetTokenResponse(
    val code: Int,
    val desc: String,
    val data: TokenData
)

data class TokenData(
    val token: String,
    val secApi: String
)