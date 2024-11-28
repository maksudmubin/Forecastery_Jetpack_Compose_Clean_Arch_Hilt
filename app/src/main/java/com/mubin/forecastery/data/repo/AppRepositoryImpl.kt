package com.mubin.forecastery.data.repo

import com.mubin.forecastery.BuildConfig
import com.mubin.forecastery.base.utils.MsLogger
import com.mubin.forecastery.data.api.ApiService
import com.mubin.forecastery.data.model.WeatherResponse
import com.mubin.forecastery.domain.repo.AppRepository
import javax.inject.Inject

/**
 * Implementation of [AppRepository] responsible for fetching weather details using [ApiService].
 *
 * This class acts as a bridge between the data layer and the API layer, handling API calls and response parsing.
 *
 * @property apiService The service used to make API calls.
 */
class AppRepositoryImpl @Inject constructor(private val apiService: ApiService) : AppRepository {

    /**
     * Fetches weather details for a given location.
     *
     * @param lat The latitude of the location.
     * @param lon The longitude of the location.
     * @param units The unit system for measurements (e.g., metric, imperial).
     * @return A [WeatherResponse] object if the API call is successful, or `null` if the call fails.
     *
     * Logs various stages of the process, including API call initiation, errors, and response parsing.
     */
    override suspend fun getWeatherDetails(
        lat: Double,
        lon: Double,
        units: String
    ): WeatherResponse? {
        // Log the initiation of the API call
        MsLogger.d("AppRepositoryImpl", "Fetching weather details: lat=$lat, lon=$lon, units=$units")

        val response = try {
            // Execute the API call and log the process
            MsLogger.d("AppRepositoryImpl", "Initiating API call to fetch weather details.")
            apiService.getWeatherDetails(lat, lon, units, BuildConfig.API_KEY).execute()
        } catch (e: Exception) {
            // Log the exception if the API call fails
            MsLogger.d("AppRepositoryImpl", "Error during API call: ${e.message}")
            null
        }

        return if (response?.isSuccessful == true) {
            // Log success and parse the response body
            MsLogger.d("AppRepositoryImpl", "API call successful. Parsing response.")
            response.body()
        } else {
            // Log failure and the response object
            MsLogger.d("AppRepositoryImpl", "API call failed. Response: ${response?.message()}")
            null
        }
    }
}