package com.example.movieticket.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface BookingApi {
    @GET("bookings/seats")
    suspend fun getBookedSeats(
        @Query("movieId") movieId: String,
        @Query("date") date: String,
        @Query("time") time: String
    ): List<String>

    @POST("bookings/book")
    suspend fun bookSeats(
        @Query("movieId") movieId: String,
        @Query("date") date: String,
        @Query("time") time: String,
        @Body seats: List<String>
    )
} 