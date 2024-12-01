package com.mubin.forecastery.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.gson.Gson
import com.mubin.forecastery.base.theme.Background
import com.mubin.forecastery.base.theme.Surface
import com.mubin.forecastery.base.utils.MsLogger
import com.mubin.forecastery.base.utils.executeBodyOrReturnNullSuspended
import com.mubin.forecastery.base.utils.executionlocker.withExecutionLocker
import com.mubin.forecastery.base.utils.readJsonFromAssets
import com.mubin.forecastery.data.model.DistrictModel
import com.mubin.forecastery.data.model.Districts
import com.mubin.forecastery.data.model.WeatherRequest
import com.mubin.forecastery.ui.composable.CircularProgressBar
import com.mubin.forecastery.ui.composable.DistrictItem
import com.mubin.forecastery.ui.composable.NoDataState
import com.mubin.forecastery.ui.composable.SearchBar
import com.mubin.forecastery.ui.composable.ShimmerLoading
import com.mubin.forecastery.ui.composable.WeatherContent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class ScreenItem(val route: String) {
    data object Home : ScreenItem("home")
    data object Search : ScreenItem("search")
}
data class WeatherItem(val label: String, val value: String, val icon: ImageVector)

@Composable
fun WeatherAppNavHost(
    navController: NavHostController,
    uiState: HomeUiState
) {

    val scope = rememberCoroutineScope()

    NavHost(navController = navController, startDestination = ScreenItem.Home.route) {
        composable(ScreenItem.Home.route) {
            HomeScreen(
                uiState = uiState,
                onSearchClick = { navController.navigate(ScreenItem.Search.route) },
                onRefreshScreen = { uiState.loadData = true }
            )
        }
        composable(ScreenItem.Search.route) {
            SearchScreen(
                uiState = uiState,
                onDistrictSelected = { selectedDistrict ->
                    withExecutionLocker(1000L) {
                        navController.popBackStack() // Updates Home Screen
                        scope.launch {
                            delay(400L)
                            executeBodyOrReturnNullSuspended {
                                if (selectedDistrict?.id  == -1) {
                                    uiState.permissionGranted = false
                                } else {
                                    uiState.weatherRequest = WeatherRequest(
                                        lat = selectedDistrict?.coord?.lat ?: 0.0,
                                        lon = selectedDistrict?.coord?.lon ?: 0.0
                                    )
                                    uiState.loadData = true
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onRefreshScreen: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pullRefreshState = rememberPullRefreshState(refreshing = uiState.isLoading, onRefresh = onRefreshScreen)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            SmallFloatingActionButton(
                modifier = Modifier,
                onClick = {
                    onSearchClick()
                },
                containerColor = Surface,
                shape = RoundedCornerShape(100),
                elevation = FloatingActionButtonDefaults.elevation(2.dp),
                content = {
                    Icon(
                        modifier = Modifier
                            .padding(12.dp)
                            .size(50.dp),
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White
                    )
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(pullRefreshState)
                .background(Background)
        ) {
            if (uiState.isLoading) {
                ShimmerLoading()
            } else {
                when {
                    uiState.response == null -> NoDataState(uiState)
                    else -> uiState.response?.let { WeatherContent(weatherResponse = it, onSearchClick = onSearchClick) }
                }
            }

            /*PullRefreshIndicator(
                refreshing = uiState.isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )*/
        }
    }
}

@Composable
fun SearchScreen(
    uiState: HomeUiState,
    onDistrictSelected: (DistrictModel?) -> Unit,
    modifier: Modifier = Modifier
) {
    // Local focus and keyboard controllers
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Load district list only if it's empty
    LaunchedEffect(Unit) {
        if (uiState.zillaList.isEmpty()) {
            uiState.isLoading = true
            scope.launch {
                try {
                    val stringJson = readJsonFromAssets(context = context, "zilla_list.json")
                    val districtList = Gson().fromJson(stringJson, Districts::class.java)
                    uiState.zillaList.add(DistrictModel(
                        id = -1,
                        name = "My Current Location",
                        state = "current",
                        country = "current",
                        DistrictModel.Coord(
                            lon = 0.0,
                            lat = 0.0
                        )
                    ))
                    uiState.zillaList.addAll(districtList) // Add data to the state-backed list
                } catch (e: Exception) {
                    MsLogger.e("SearchScreen", "Error loading district list: ${e.localizedMessage}")
                } finally {
                    uiState.isLoading = false
                }
            }
        }
    }

    var searchText by remember { mutableStateOf("") }
    val filteredDistricts = if (searchText.isBlank()) uiState.zillaList else {
        uiState.zillaList.filter { it.name.contains(searchText, ignoreCase = true) }
    }

    val sortedDistricts = filteredDistricts.sortedBy { if (it.name == "My Current Location") 0 else 1 }

    Scaffold(
        modifier = modifier,
        topBar = {
            SearchBar(
                modifier = Modifier
                    .padding(WindowInsets.statusBars.asPaddingValues())
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .padding(horizontal = 16.dp),
                query = searchText,
                onQueryChange = { searchText = it },
                onSearch = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                },
                placeholder = "Search"
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .background(Background)
            .fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressBar(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                        .background(Background)
                ) {
                    items(sortedDistricts.size) { index ->
                        DistrictItem(
                            district = sortedDistricts[index],
                            onClick = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                onDistrictSelected(sortedDistricts[index])
                            }
                        )
                        if (index < sortedDistricts.size - 1) {
                            HorizontalDivider(color = Color.Gray, thickness = 1.dp)
                        }
                    }
                }
            }
        }
    }
}