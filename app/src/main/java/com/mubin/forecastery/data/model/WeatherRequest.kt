package com.mubin.forecastery.data.model

/**
 * A data class representing a request for weather details.
 *
 * @property lat The latitude of the location for which the weather details are needed.
 * @property lon The longitude of the location for which the weather details are needed.
 * @property units The unit system for temperature and other measurements (default: "metric").
 *
 */
data class WeatherRequest(
    val lat: Double,
    val lon: Double,
    val units: String = "metric",
)
