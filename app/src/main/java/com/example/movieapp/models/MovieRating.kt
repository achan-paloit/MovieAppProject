package com.example.movieapp.models

import com.google.gson.annotations.SerializedName

data class MovieRating(
    @SerializedName("Source")
    val source: String,
    @SerializedName("Value")
    val value: String
)