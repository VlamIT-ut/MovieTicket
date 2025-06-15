package com.example.movieticket.data.model

import java.time.LocalDate
import java.time.LocalTime

data class BookingDetails(
    val id: String = java.util.UUID.randomUUID().toString(),
    val movieId: Int,
    val date: LocalDate,
    val time: LocalTime,
    val seats: List<String>,
    val totalPrice: Int
) 