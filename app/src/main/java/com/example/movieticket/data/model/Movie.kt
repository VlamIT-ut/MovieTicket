package com.example.movieticket.data.model

data class Movie(
    val id: Int,
    val title: String,
    val posterPath: String,
    val backdropPath: String,
    val overview: String,
    val releaseDate: String,
    val voteAverage: Double,
    val genres: List<String>
) 