package com.mubin.forecastery.data.api

import com.mubin.forecastery.data.model.WeatherResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * This interface defines the API endpoints for fetching weather details.
 * It uses Retrofit annotations to define HTTP requests and query parameters.
 */
interface ApiEndpoints {

    /**
     * Fetches weather details based on the provided latitude, longitude, unit type, and API key.
     *
     * @param lat The latitude of the location for which the weather details are needed.
     * @param lon The longitude of the location for which the weather details are needed.
     * @param units The unit system for temperature and other measurements (e.g., metric, imperial).
     * @param appId The API key to authenticate the request.
     * @return A [Call] that returns a [WeatherResponse] containing the weather details.
     *
     */
    @GET("weather")
    fun getWeatherDetails(
        @Query(value = "lat", encoded = true) lat: Double,
        @Query(value = "lon", encoded = true) lon: Double,
        @Query(value = "units", encoded = true) units: String,
        @Query(value = "appid", encoded = true) appId: String,
    ): Call<WeatherResponse>
}