package com.mubin.forecastery.ui.home

import com.mubin.forecastery.data.model.DistrictModel
import com.mubin.forecastery.domain.entities.WeatherEntity

sealed class WeatherState {
    data object Loading : WeatherState()
    data class Success(val data: WeatherEntity) : WeatherState()
    data class Error(val message: String?) : WeatherState()
}

sealed class SearchState {
    data object Loading : SearchState()
    data class Success(val data: List<DistrictModel>) : SearchState()
    data class Error(val message: String?) : SearchState()
}

sealed class PermissionState {
    data object Granted : PermissionState()
    data object Denied : PermissionState()
    data object PermanentlyDenied : PermissionState()
    data object Requesting : PermissionState()
}