package com.example.ascend_app

import com.google.gson.annotations.SerializedName

data class TokenResponse (
    @SerializedName("response_code") val responseCode: Int,
    val token: String
)