package com.example.movieticket.data.repository

import com.example.movieticket.data.api.BookingApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepository @Inject constructor(
    private val api: BookingApi
) {
    suspend fun getBookedSeats(movieId: String, date: String, time: String): List<String> {
        return try {
            api.getBookedSeats(movieId, date, time)
        } catch (e: Exception) {
            // Log error
            emptyList()
        }
    }

    suspend fun bookSeats(
        movieId: String,
        date: String,
        time: String,
        seats: List<String>
    ): Boolean {
        return try {
            api.bookSeats(movieId, date, time, seats)
            true
        } catch (e: Exception) {
            // Log error
            false
        }
    }
} 