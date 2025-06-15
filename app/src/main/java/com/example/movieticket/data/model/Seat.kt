package com.example.movieticket.data.model

data class Seat(
    val id: String,
    val isBooked: Boolean = false,
    val isSelected: Boolean = false
) 