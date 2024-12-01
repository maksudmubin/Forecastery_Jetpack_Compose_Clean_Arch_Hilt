package com.mubin.forecastery.data.repo

import com.mubin.forecastery.BuildConfig
import com.mubin.forecastery.base.utils.MsLogger
import com.mubin.forecastery.data.api.ApiService
import com.mubin.forecastery.domain.entities.WeatherEntity
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
     * @return A [WeatherEntity] object if the API call is successful, or `null` if the call fails.
     *
     * Logs various stages of the process, including API call initiation, errors, and response parsing.
     */
    override suspend fun getWeatherDetails(
        lat: Double,
        lon: Double,
        units: String
    ): WeatherEntity? {
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
            WeatherEntity(
                country = response.body()?.sys?.country ?: "Unknown",
                locationName = response.body()?.name ?: "Unknown",
                temperature = response.body()?.main?.temp ?: 0.0,
                feelsLike = response.body()?.main?.feelsLike ?: 0.0,
                icon = response.body()?.weather?.get(0)?.icon ?: "01d",
                description = response.body()?.weather?.get(0)?.description ?: "Unknown",
                humidity = response.body()?.main?.humidity ?: 0,
                pressure = response.body()?.main?.pressure ?: 0,
                windSpeed = response.body()?.wind?.speed ?: 0.0,
                cloudiness = response.body()?.clouds?.all ?: 0,
                visibility = response.body()?.visibility ?: 0,
                timeZone = response.body()?.timezone ?: 0,
                sunrise = response.body()?.sys?.sunrise ?: 0,
                sunset = response.body()?.sys?.sunset ?: 0
            )
        } else {
            // Log failure and the response object
            MsLogger.d("AppRepositoryImpl", "API call failed. Response: ${response?.message()}")
            null
        }
    }
}