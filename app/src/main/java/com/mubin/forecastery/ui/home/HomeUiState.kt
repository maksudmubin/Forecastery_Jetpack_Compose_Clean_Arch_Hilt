package com.mubin.forecastery.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mubin.forecastery.data.model.DistrictModel
import com.mubin.forecastery.data.model.WeatherRequest
import com.mubin.forecastery.domain.entities.WeatherEntity

/**
 * UI state for managing the data and states related to the home screen.
 * This class holds various pieces of state that determine the UI behavior,
 * such as permission status, loading status, weather request details, and response data.
 */
class HomeUiState {

    /**
     * A boolean flag indicating whether the user should be prompted to grant permission.
     * This is typically used when location or other permissions need to be requested.
     */
    var askPermission by mutableStateOf(false)

    /**
     * A boolean flag indicating whether the required permission has been granted.
     * This is used to check if the app has the necessary permissions to access location or other services.
     */
    var permissionGranted by mutableStateOf(false)

    /**
     * A boolean flag indicating whether the data should be reloaded.
     * When set to true, it triggers a reload of data, typically when refreshing or fetching new data.
     */
    var loadData by mutableStateOf(false)

    /**
     * A boolean flag indicating whether the UI is in a loading state.
     * When set to true, it typically shows loading indicators (e.g., spinners or progress bars).
     */
    var isHomeScreenLoading by mutableStateOf(false)

    /**
     * A boolean flag indicating whether the UI is in a loading state.
     * When set to true, it typically shows loading indicators (e.g., spinners or progress bars).
     */
    var isSearchScreenLoading by mutableStateOf(false)

    /**
     * An object representing the weather request with geographic coordinates.
     * Contains information such as latitude and longitude for requesting weather data.
     */
    var weatherRequest: WeatherRequest? by mutableStateOf(null)

    /**
     * An object representing the weather response data.
     * Contains the weather data received from an API or other data source, such as temperature, humidity, etc.
     */
    var response: WeatherEntity? by mutableStateOf(null)

    /**
     * A mutable list containing the district models (locations).
     * This list may include all available districts or location options for the user to choose from.
     */
    var districtList = mutableStateListOf<DistrictModel>()
}