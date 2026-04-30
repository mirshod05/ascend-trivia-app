package com.example.ascend_app

import com.google.gson.annotations.SerializedName

data class TriviaResponse (
    @SerializedName("response_code") val responseCode: Int,
    val results: List<Question>
)