package com.example.movieapp.services

import com.example.movieapp.models.MovieDetails
import com.example.movieapp.models.MovieResult
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

const val API_KEY = "b9bd48a6"
const val TYPE = "movie"

interface MovieResultService {

    @GET("/")
    fun getMovieList(@Query("s") search: String, @Query("page") page: Int, @Query("type") type: String = TYPE, @Query("apikey") apiKey: String = API_KEY) : Call<MovieResult>

    @GET("/")
    fun getMovieDetails(@Query("i") id: String, @Query("apikey") apiKey: String = API_KEY) : Call<MovieDetails>

}