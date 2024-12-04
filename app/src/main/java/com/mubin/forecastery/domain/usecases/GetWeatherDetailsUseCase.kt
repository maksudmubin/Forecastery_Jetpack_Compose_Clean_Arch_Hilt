package com.mubin.forecastery.domain.usecases

import com.mubin.forecastery.base.utils.MsLogger
import com.mubin.forecastery.base.utils.UseCase
import com.mubin.forecastery.data.model.WeatherRequest
import com.mubin.forecastery.domain.entities.WeatherEntity
import com.mubin.forecastery.domain.repo.AppRepository
import javax.inject.Inject

/**
 * Use case for fetching weather details using the repository layer.
 *
 * This class is part of the domain layer and encapsulates the logic for retrieving weather data,
 * making the application's business logic independent of the data source.
 *
 * @property appRepository The repository used to fetch weather data.
 *
 */
class GetWeatherDetailsUseCase @Inject constructor(
    private val appRepository: AppRepository
) : UseCase<Result<WeatherEntity>, WeatherRequest>() {

    /**
     * Executes the use case with the given parameters to fetch weather details.
     *
     * @param params The [WeatherRequest] containing the latitude, longitude, and unit system.
     * @return A [WeatherEntity] object containing the weather data, or `null` if the request fails.
     *
     * Handles any exceptions during the execution and logs them.
     */
    override suspend fun run(params: WeatherRequest): Result<WeatherEntity> {
        // Log the initiation of the use case
        MsLogger.d("GetWeatherDetailsUseCase", "Executing with params: $params")

        return try {
            // Delegate the request to the repository and log the result
            appRepository.getWeatherDetails(params.lat, params.lon, params.units).also {
                MsLogger.d("GetWeatherDetailsUseCase", "Result received: $it")
            }
        } catch (e: Exception) {
            // Log the exception if an error occurs
            MsLogger.d("GetWeatherDetailsUseCase", "Error during execution: ${e.message}")
            Result.failure(e)
        }
    }
}