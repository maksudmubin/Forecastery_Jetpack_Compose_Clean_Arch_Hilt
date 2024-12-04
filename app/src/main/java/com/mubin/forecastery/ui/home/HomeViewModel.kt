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



    private val _permissionState = mutableStateOf<PermissionState>(PermissionState.Denied)
    val permissionState = _permissionState

    private val _askPermission = mutableStateOf(false)
    val askPermission: State<Boolean> = _askPermission

    private val _permissionGranted = mutableStateOf(false)
    val permissionGranted: State<Boolean> = _permissionGranted

    private val _loadData = mutableStateOf(false)
    val loadData: State<Boolean> = _loadData

    private val _homeState = mutableStateOf<WeatherState>(WeatherState.Loading)
    val homeState: State<WeatherState> = _homeState

    private val _searchState = mutableStateOf<SearchState>(SearchState.Loading)
    val searchState: State<SearchState> = _searchState

    private var _currentLocation: WeatherRequest? by mutableStateOf(null)
    var currentLocation: WeatherRequest?
        get() = _currentLocation
        set(value) {
            _currentLocation = value
        }

    private var _weatherRequest: WeatherRequest? by mutableStateOf(null)
    var weatherRequest: WeatherRequest?
        get() = _weatherRequest
        set(value) {
            _weatherRequest = value
        }

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
            response
        } catch (e: Exception) {
            // Log any exceptions that occur
            MsLogger.d("HomeViewModel", "Error fetching weather data: ${e.message}")
            Result.failure(e)
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
            // Log the exception if an error occurs
            MsLogger.e("HomeViewModel", "Error fetching district list: ${e.message}")
            // Return null to indicate failure
            Result.failure(e)
        }
    }

    fun requestPermission() {
        permissionState.value = PermissionState.Requesting
    }

    fun setAskPermissionState(askPermission: Boolean) {
        _askPermission.value = askPermission
    }

    fun setPermissionGrantedState(permissionGranted: Boolean) {
        _permissionGranted.value = permissionGranted
    }

    fun setLoadDataState(loadData: Boolean) {
        _loadData.value = loadData
    }

    // Set loading state
    fun setHomeLoadingState() {
        _homeState.value = WeatherState.Loading
    }

    // Set success state with weather data
    fun setHomeSuccessState(data: WeatherEntity) {
        _homeState.value = WeatherState.Success(data)
    }

    // Set error state with message
    fun setHomeErrorState(message: String?) {
        _homeState.value = WeatherState.Error(message)
    }

    // Set loading state
    fun setSearchLoadingState() {
        _searchState.value = SearchState.Loading
    }

    // Set success state with weather data
    fun setSearchSuccessState(data: List<DistrictModel>) {
        _searchState.value = SearchState.Success(data)
        cachedDistrictList.addAll(data)
    }

    // Set error state with message
    fun setSearchErrorState(message: String?) {
        _searchState.value = SearchState.Error(message)
    }

}