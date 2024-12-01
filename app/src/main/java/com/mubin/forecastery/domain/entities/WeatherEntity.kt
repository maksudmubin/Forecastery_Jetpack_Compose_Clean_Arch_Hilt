package com.mubin.forecastery.domain.entities

data class WeatherEntity(
    val country: String,
    val locationName: String,
    val temperature: Double,
    val feelsLike: Double,
    val icon: String,
    val description: String,
    val humidity: Int,
    val pressure: Int,
    val windSpeed: Double,
    val cloudiness: Int,
    val visibility: Int,
    val timeZone: Int,
    val sunrise: Int,
    val sunset: Int
)
