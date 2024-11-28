package com.mubin.forecastery.data.api

import com.mubin.forecastery.data.model.WeatherResponse
import retrofit2.Call
import retrofit2.Retrofit
import javax.inject.Inject

/**
 * A service class responsible for managing API calls related to weather data.
 * Implements the [ApiEndpoints] interface and delegates API calls to Retrofit.
 *
 * @param retrofit The [Retrofit] instance used to create API endpoint implementations.
 */
class ApiService @Inject constructor(retrofit: Retrofit) : ApiEndpoints {

    // Lazily initialized Retrofit API implementation
    private val api by lazy { retrofit.create(ApiEndpoints::class.java) }

    /**
     * Fetches weather details based on latitude, longitude, units, and API key.
     *
     * @param lat The latitude of the location.
     * @param lon The longitude of the location.
     * @param units The unit system for measurements (e.g., metric, imperial).
     * @param appId The API key for authentication.
     * @return A [Call] that provides the weather response.
     *
     */
    override fun getWeatherDetails(
        lat: Double,
        lon: Double,
        units: String,
        appId: String
    ): Call<WeatherResponse> = api.getWeatherDetails(lat, lon, units, appId)


}