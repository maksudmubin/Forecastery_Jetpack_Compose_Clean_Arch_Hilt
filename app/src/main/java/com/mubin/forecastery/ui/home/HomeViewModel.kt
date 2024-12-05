package com.mubin.forecastery.ui.home

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

    // Holds the current permission state (Requesting, Granted, Denied, Permanently Denied)
    private val _permissionState = mutableStateOf<PermissionState>(PermissionState.Requesting)
    val permissionState = _permissionState

    // Holds the loading state for the weather data
    private val _loadData = mutableStateOf(false)
    val loadData: State<Boolean> = _loadData

    // Holds the current state of the home screen (e.g., loading, success, or error)
    private val _homeState = mutableStateOf<WeatherState>(WeatherState.Loading)
    val homeState: State<WeatherState> = _homeState

    // Holds the state of the search feature (loading, success, or error)
    private val _searchState = mutableStateOf<SearchState>(SearchState.Loading)
    val searchState: State<SearchState> = _searchState

    // WeatherRequest object that contains the request parameters like latitude, longitude, etc.
    private var _weatherRequest: WeatherRequest? by mutableStateOf(null)
    var weatherRequest: WeatherRequest?
        get() = _weatherRequest
        set(value) {
            _weatherRequest = value
        }

    // Cached list of districts
    var cachedDistrictList = mutableStateListOf<DistrictModel>()

    /**
     * Fetches weather details based on the provided request parameters.
     *
     * @param request The [WeatherRequest] containing latitude, longitude, and unit system.
     * @return A [WeatherEntity] object containing the weather data, or `null` if the request fails.
     *
     * This function is executed in the `IO` context for offloading heavy tasks from the main thread.
     */
    suspend fun getWeatherDetails(request: WeatherRequest): Result<WeatherEntity> = withContext(Dispatchers.IO) {
        // Log the initiation of the API call
        MsLogger.d("HomeViewModel", "Fetching weather details with request: $request")

        try {
            // Execute the use case and fetch weather details
            val response = weatherDetailsUseCase(request)
            MsLogger.d("HomeViewModel", "Weather data fetched successfully: $response")
            return@withContext response // Return the response if successful
        } catch (e: Exception) {
            // Log any exceptions that occur during the fetch operation
            MsLogger.d("HomeViewModel", "Error fetching weather data: ${e.message}")
            return@withContext Result.failure(e) // Return failure in case of an error
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
    suspend fun getDistrictList(): Result<List<DistrictModel>> = withContext(Dispatchers.IO) {
        try {
            // Log the initiation of the district list fetch operation
            MsLogger.d("HomeViewModel", "Fetching district list using use case.")

            // Execute the use case to retrieve the district list
            districtListUseCase(UseCase.None()).also {
                // Log the successful retrieval of data
                MsLogger.d("HomeViewModel", "Successfully fetched ${it.getOrNull()?.size} districts.")
            }
        } catch (e: Exception) {
            // Log the exception if an error occurs during the fetch operation
            MsLogger.e("HomeViewModel", "Error fetching district list: ${e.message}")
            // Return failure to indicate the error
            return@withContext Result.failure(e)
        }
    }

    /**
     * Updates the permission state with the new state.
     *
     * @param newState The new [PermissionState] to set.
     */
    fun updatePermissionState(newState: PermissionState) {
        _permissionState.value = newState
        MsLogger.d("HomeViewModel", "Permission state updated to: $newState")
    }

    /**
     * Sets the loading state for the data.
     *
     * @param loadData The new loading state to set.
     */
    fun setLoadDataState(loadData: Boolean) {
        _loadData.value = loadData
        MsLogger.d("HomeViewModel", "Load data state updated to: $loadData")
    }

    /**
     * Sets the home state to loading.
     */
    fun setHomeLoadingState() {
        _homeState.value = WeatherState.Loading
        MsLogger.d("HomeViewModel", "Home state set to Loading.")
    }

    /**
     * Sets the home state to success with the provided weather data.
     *
     * @param data The weather data to set in the success state.
     */
    fun setHomeSuccessState(data: WeatherEntity) {
        _homeState.value = WeatherState.Success(data)
        MsLogger.d("HomeViewModel", "Home state set to Success with data: $data")
    }

    /**
     * Sets the home state to error with an optional error message.
     *
     * @param message The error message to set in the error state.
     */
    fun setHomeErrorState(message: String?) {
        _homeState.value = WeatherState.Error(message)
        MsLogger.d("HomeViewModel", "Home state set to Error with message: $message")
    }

    /**
     * Sets the search state to loading.
     */
    fun setSearchLoadingState() {
        _searchState.value = SearchState.Loading
        MsLogger.d("HomeViewModel", "Search state set to Loading.")
    }

    /**
     * Sets the search state to success with the provided district data.
     *
     * @param data The list of districts to set in the success state.
     */
    fun setSearchSuccessState(data: List<DistrictModel>) {
        _searchState.value = SearchState.Success(data)
        cachedDistrictList.addAll(data) // Cache the district list
        MsLogger.d("HomeViewModel", "Search state set to Success with data: ${data.size} districts.")
    }

    /**
     * Sets the search state to error with an optional error message.
     *
     * @param message The error message to set in the error state.
     */
    fun setSearchErrorState(message: String?) {
        _searchState.value = SearchState.Error(message)
        MsLogger.d("HomeViewModel", "Search state set to Error with message: $message")
    }

}