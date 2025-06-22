package com.example.movieticket.data.model

data class User (
    val id: String,
    val name: String,
    var point: Int = 0,
    var memberLevel: String = "Silver"

)
