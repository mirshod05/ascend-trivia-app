package com.example.ascend_app

import retrofit2.http.GET
import retrofit2.http.Query

interface TriviaService {

    @GET("api.php")
    suspend fun getQuestions(
        @Query("amount") amount: Int,
        @Query("category") category: Int = 9,        // 9 = General Knowledge
        @Query("type") type: String = "multiple",
        @Query("encode") encode: String = "base64",
        @Query("token") token: String? = null
    ): TriviaResponse

    @GET("api.php?command=request")
    suspend fun getSessionToken(): TokenResponse

    @GET("api.php?command=reset")
    suspend fun resetSessionToken(
        @Query("token") token: String
    ): TokenResponse
}