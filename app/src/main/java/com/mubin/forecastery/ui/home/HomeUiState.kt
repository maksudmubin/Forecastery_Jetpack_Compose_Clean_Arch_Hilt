package com.mubin.forecastery.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mubin.forecastery.data.model.DistrictModel
import com.mubin.forecastery.data.model.WeatherRequest
import com.mubin.forecastery.data.model.WeatherResponse

class HomeUiState {

    var askPermission by mutableStateOf(false)

    var permissionGranted by mutableStateOf(false)

    var loadData by mutableStateOf(false)

    var isLoading by mutableStateOf(false)

    var weatherRequest: WeatherRequest? by mutableStateOf(null)

    var response: WeatherResponse? by mutableStateOf(null)

    var zillaList = mutableStateListOf<DistrictModel>()

}