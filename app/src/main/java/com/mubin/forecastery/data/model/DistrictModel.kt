package com.mubin.forecastery.data.model


import androidx.annotation.Keep
@Keep
data class DistrictModel(
    val id: Int,
    val name: String,
    val state: String,
    val country: String,
    val coord: Coord
) {
    @Keep
    data class Coord(
        val lon: Double,
        val lat: Double
    )
}
