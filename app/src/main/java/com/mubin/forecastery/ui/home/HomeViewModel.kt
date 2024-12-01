package com.mubin.forecastery.ui.home

import androidx.lifecycle.ViewModel
import com.mubin.forecastery.base.utils.MsLogger
import com.mubin.forecastery.base.utils.UseCase
import com.mubin.forecastery.data.model.DistrictModel
import com.mubin.forecastery.data.model.WeatherRequest
import com.mubin.forecastery.domain.entities.WeatherEntity
import com.mubin.forecastery.domain.usecases.GetDistrictListUseCase
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
 * @property weatherDetailsUseCase The use case for fetching weather details.
 * @property districtListUseCase The use case for fetching district list.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val weatherDetailsUseCase: GetWeatherDetailsUseCase,
    private val districtListUseCase: GetDistrictListUseCase
) : ViewModel() {

    // Lazy initialization of UI state
    val uiState by lazy { HomeUiState() }

    /**
     * Fetches weather details based on the provided request parameters.
     *
     * @param request The [WeatherRequest] containing latitude, longitude, and unit system.
     * @return A [WeatherEntity] object containing the weather data, or `null` if the request fails.
     *
     * This function is executed in the `IO` context for offloading heavy tasks from the main thread.
     */
    suspend fun getWeatherDetails(request: WeatherRequest): WeatherEntity? = withContext(Dispatchers.IO) {
        // Log the initiation of the API call
        MsLogger.d("HomeViewModel", "Fetching weather details with request: $request")

        try {
            // Execute the use case and fetch weather details
            val response = weatherDetailsUseCase(request)
            MsLogger.d("HomeViewModel", "Weather data fetched successfully: $response")
            response
        } catch (e: Exception) {
            // Log any exceptions that occur
            MsLogger.d("HomeViewModel", "Error fetching weather data: ${e.message}")
            null
        }
    }

    /**
     * Fetches the list of districts using the `GetDistrictListUseCase`.
     *
     * This function runs on the `IO` dispatcher, ensuring background execution for better performance.
     * It handles any exceptions gracefully, logging errors and returning `null` in case of failure.
     *
     * @return A list of `DistrictModel` objects if successful, or `null` in case of an error.
     */
    suspend fun getDistrictList(): List<DistrictModel>? = withContext(Dispatchers.IO) {
        try {
            // Log the initiation of the district list fetch operation
            MsLogger.d("HomeViewModel", "Fetching district list using use case.")

            // Execute the use case to retrieve the district list
            districtListUseCase(UseCase.None()).also {
                // Log the successful retrieval of data
                MsLogger.d("HomeViewModel", "Successfully fetched ${it?.size ?: 0} districts.")
            }
        } catch (e: Exception) {
            // Log the exception if an error occurs
            MsLogger.e("HomeViewModel", "Error fetching district list: ${e.message}")

            // Return null to indicate failure
            null
        }
    }
}