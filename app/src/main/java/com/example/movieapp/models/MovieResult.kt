package com.example.movieapp.models

import com.google.gson.annotations.SerializedName

data class MovieResult(
    @SerializedName("Search")
    val search: List<MovieSearch?>,
    @SerializedName("totalResults")
    val totalResults: Int,
    @SerializedName("Response")
    val response: Boolean
)