package com.mubin.forecastery.ui.home

import androidx.lifecycle.ViewModel
import com.mubin.forecastery.base.utils.MsLogger
import com.mubin.forecastery.data.model.WeatherRequest
import com.mubin.forecastery.data.model.WeatherResponse
import com.mubin.forecastery.domain.usecases.GetWeatherDetailsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel for managing the home screen's state and logic.
 *
 * This class is responsible for interacting with the domain layer through the [GetWeatherDetailsUseCase]
 * and exposing weather data to the UI layer. It uses Hilt for dependency injection.
 *
 * @property useCase The use case for fetching weather details.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val useCase: GetWeatherDetailsUseCase
) : ViewModel() {

    /**
     * Fetches weather details based on the provided request parameters.
     *
     * @param request The [WeatherRequest] containing latitude, longitude, and unit system.
     * @return A [WeatherResponse] object containing the weather data, or `null` if the request fails.
     *
     * This function is executed in the `IO` context for offloading heavy tasks from the main thread.
     */
    suspend fun getWeatherDetails(request: WeatherRequest): WeatherResponse? = withContext(Dispatchers.IO) {
        // Log the initiation of the API call
        MsLogger.d("HomeViewModel", "Fetching weather details with request: $request")

        try {
            // Execute the use case and fetch weather details
            val response = useCase(request)
            MsLogger.d("HomeViewModel", "Weather data fetched successfully: $response")
            response
        } catch (e: Exception) {
            // Log any exceptions that occur
            MsLogger.d("HomeViewModel", "Error fetching weather data: ${e.message}")
            null
        }
    }
}