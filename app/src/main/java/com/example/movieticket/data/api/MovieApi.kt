package com.example.movieticket.data.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieApi {
    @GET("movie/now_playing")
    suspend fun getNowPlaying(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "vi-VN",
        @Query("page") page: Int = 1
    ): MovieResponse

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "vi-VN",
        @Query("page") page: Int = 1
    ): MovieResponse

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "vi-VN"
    ): MovieDetail

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("language") language: String = "vi-VN",
        @Query("page") page: Int = 1
    ): MovieResponse

    @GET("movie/upcoming")
    suspend fun getUpcomingMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "vi-VN",
        @Query("page") page: Int = 1,
        @Query("region") region: String = "VN"
    ): MovieResponse
}

data class MovieResponse(
    val page: Int = 1,
    val results: List<MovieResult> = emptyList(),
    val total_pages: Int = 0,
    val total_results: Int = 0
)

data class MovieResult(
    val id: Int = 0,
    val title: String = "",
    val overview: String? = null,
    val poster_path: String? = null,
    val backdrop_path: String? = null,
    val release_date: String? = null,
    val vote_average: Double? = null,
    val genre_ids: List<Int> = emptyList()
)

data class MovieDetail(
    val id: Int,
    val title: String,
    val overview: String?,
    val poster_path: String?,
    val backdrop_path: String?,
    val release_date: String?,
    val vote_average: Double?,
    val genres: List<Genre>
)

data class Genre(
    val id: Int,
    val name: String
) 