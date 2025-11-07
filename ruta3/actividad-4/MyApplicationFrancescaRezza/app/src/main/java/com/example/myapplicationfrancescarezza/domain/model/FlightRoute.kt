package com.example.myapplicationfrancescarezza.domain.model

data class FlightRoute(
    val departureCode: String,
    val departureName: String,
    val destinationCode: String,
    val destinationName: String,
    val destinationPassengers: Int,
    val isFavorite: Boolean
)


