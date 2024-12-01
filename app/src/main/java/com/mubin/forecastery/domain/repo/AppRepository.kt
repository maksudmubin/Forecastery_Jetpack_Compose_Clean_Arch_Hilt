package com.mubin.forecastery.domain.repo

import com.mubin.forecastery.data.model.DistrictModel
import com.mubin.forecastery.domain.entities.WeatherEntity

/**
 * Interface defining the repository layer for fetching weather data.
 *
 * This interface serves as a contract for the implementation of weather data retrieval
 * and abstracts the data layer from the rest of the application.
 */
interface AppRepository {

    /**
     * Fetches weather details for a specific location based on latitude, longitude, and unit system.
     *
     * @param lat The latitude of the location.
     * @param lon The longitude of the location.
     * @param units The unit system for measurements (e.g., "metric", "imperial").
     * @return A [WeatherEntity] object if the call is successful, or `null` if an error occurs.
     *
     */
    suspend fun getWeatherDetails(lat: Double, lon: Double, units: String): WeatherEntity?


    /**
     * Fetches district list from local Json.
     * */
    suspend fun getDistrictList() : List<DistrictModel>?
}