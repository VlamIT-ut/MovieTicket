package com.example.movieticket.data.repository

import com.example.movieticket.data.model.Movie
import com.example.movieticket.di.NetworkModule
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieRepository @Inject constructor() {
    private val movieApi = NetworkModule.movieApi
    private val apiKey = NetworkModule.API_KEY

    suspend fun getMovies(): List<Movie> {
        val response = movieApi.getNowPlaying(apiKey)
        return response.results.mapNotNull { result ->
            try {
                Movie(
                    id = result.id,
                    title = result.title,
                    posterPath = result.poster_path ?: "",
                    backdropPath = result.backdrop_path ?: "",
                    overview = result.overview ?: "",
                    releaseDate = result.release_date ?: "",
                    voteAverage = result.vote_average ?: 0.0,
                    genres = result.genre_ids?.map { getGenreName(it) } ?: listOf("Unknown")
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getUpcomingMovies(): List<Movie> {
        try {
            val response = movieApi.getUpcomingMovies(apiKey)
            return response.results.mapNotNull { result ->
                try {
                    Movie(
                        id = result.id,
                        title = result.title,
                        posterPath = result.poster_path ?: "",
                        backdropPath = result.backdrop_path ?: "",
                        overview = result.overview ?: "",
                        releaseDate = result.release_date ?: "",
                        voteAverage = result.vote_average ?: 0.0,
                        genres = result.genre_ids?.map { getGenreName(it) } ?: listOf("Unknown")
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    private fun getGenreName(genreId: Int): String {
        return when (genreId) {
            28 -> "Action"
            12 -> "Adventure"
            16 -> "Animation"
            35 -> "Comedy"
            80 -> "Crime"
            99 -> "Documentary"
            18 -> "Drama"
            10751 -> "Family"
            14 -> "Fantasy"
            36 -> "History"
            27 -> "Horror"
            10402 -> "Music"
            9648 -> "Mystery"
            10749 -> "Romance"
            878 -> "Science Fiction"
            10770 -> "TV Movie"
            53 -> "Thriller"
            10752 -> "War"
            37 -> "Western"
            else -> "Unknown"
        }
    }
} 