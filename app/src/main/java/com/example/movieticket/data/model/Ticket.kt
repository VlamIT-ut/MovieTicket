package com.example.movieticket.data.model

data class Ticket(
    val id: String = "",
    val movieId: Int = 0,
    val movieTitle: String = "",
    val moviePoster: String = "",
    val userId: String = "",
    val seats: List<String> = emptyList(),
    val date: String = "",
    val time: String = "",
    val totalAmount: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "active" // active, used, cancelled
) 