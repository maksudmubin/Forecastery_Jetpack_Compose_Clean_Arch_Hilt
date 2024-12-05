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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mubin.forecastery.base.theme.Background
import com.mubin.forecastery.base.theme.Surface
import com.mubin.forecastery.base.utils.MsLogger
import com.mubin.forecastery.base.utils.executeBodyOrReturnNullSuspended
import com.mubin.forecastery.base.utils.executionlocker.withExecutionLocker
import com.mubin.forecastery.data.model.DistrictModel
import com.mubin.forecastery.data.model.WeatherRequest
import com.mubin.forecastery.ui.composable.CustomAlertDialog
import com.mubin.forecastery.ui.composable.DistrictItem
import com.mubin.forecastery.ui.composable.NoDataState
import com.mubin.forecastery.ui.composable.SearchBar
import com.mubin.forecastery.ui.composable.HomeScreenShimmerLoading
import com.mubin.forecastery.ui.composable.SearchScreenShimmerLoading
import com.mubin.forecastery.ui.composable.WeatherContent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A sealed class representing the different screens in the app.
 * Used for defining navigation routes and ensuring type-safety in the navigation graph.
 * Only specific screen types can be used within the navigation system.
 */
sealed class ScreenItem(val route: String) {
    /**
     * Represents the Home screen route.
     * The user will be directed to this screen when navigating to the home section.
     */
    data object Home : ScreenItem("home")

    /**
     * Represents the Search screen route.
     * The user will be directed to this screen when searching for weather data for districts.
     */
    data object Search : ScreenItem("search")
}

/**
 * Data class representing a weather item that is displayed in the UI.
 * This can represent a specific weather attribute such as temperature, humidity, or wind speed.
 *
 * @property label The label that describes the weather item (e.g., "Temperature", "Humidity").
 * @property value The value of the weather item (e.g., "22°C", "60%").
 * @property icon The icon associated with the weather item (e.g., sun, cloud, wind).
 */
data class WeatherItem(
    val label: String,  // Describes the weather attribute (e.g., "Temperature")
    val value: String,  // The value of the weather attribute (e.g., "22°C")
    val icon: ImageVector  // The icon representing the weather item (e.g., sun or cloud icon)
)

/**
 * A composable that sets up the navigation for the Weather App using Jetpack Compose NavHost.
 * It defines two main destinations: Home and Search.
 *
 * @param navController The NavHostController used for navigating between screens.
 * @param vm The viewModel contains current state of the UI, including methods to contact with domain layer.
 */
@Composable
fun WeatherAppNavHost(
    navController: NavHostController, // Navigation controller for handling screen transitions
    vm: HomeViewModel // The viewModel, which holds weather data and other UI states
) {

    // Remember coroutine scope to launch background tasks
    val scope = rememberCoroutineScope()

    // NavHost for handling different screen destinations and navigating between them
    NavHost(navController = navController, startDestination = ScreenItem.Home.route) {

        // Define the Home screen destination
        composable(ScreenItem.Home.route) {
            // HomeScreen composable, passing uiState and functions for search and refresh actions
            HomeScreen(
                vm = vm, // Pass the vm to the home screen
                onSearchClick = {
                    navController.navigate(ScreenItem.Search.route) // Navigate to Search screen when search is clicked
                },
                onRefreshScreen = {
                    vm.setLoadDataState(true) // Refresh the weather data when the refresh action is triggered
                }
            )
        }

        // Define the Search screen destination
        composable(ScreenItem.Search.route) {
            // SearchScreen composable, passing uiState and a function for handling district selection
            SearchScreen(
                vm = vm, // Pass the vm to the search screen
                onDistrictSelected = { selectedDistrict ->
                    withExecutionLocker(1000L) {
                        // Locks execution for 1000 milliseconds to prevent rapid consecutive actions
                        navController.popBackStack() // Pop the back stack to go back to the Home screen
                        scope.launch {
                            delay(400L) // Add a short delay before proceeding with the weather request
                            executeBodyOrReturnNullSuspended {
                                // Check if the item is current location
                                if (selectedDistrict?.id == -1) {
                                    vm.updatePermissionState(PermissionState.Requesting)
                                } else {
                                    // Update the weather request with the selected district's coordinates
                                    vm.weatherRequest = WeatherRequest(
                                        lat = selectedDistrict?.coord?.lat ?: 0.0, // Latitude of the selected district
                                        lon = selectedDistrict?.coord?.lon ?: 0.0  // Longitude of the selected district
                                    )
                                    vm.setLoadDataState(true) // Trigger the data loading for the weather
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

/**
 * Home screen composable that displays weather data, a search button, and a pull-to-refresh feature.
 * It handles different UI states, including loading and displaying weather content or showing a "No Data" state.
 *
 * @param vm ViewModel instance used to manage the UI state and fetch data from domain layer.
 * @param onRefreshScreen The function that is triggered when the user pulls to refresh the screen.
 * @param onSearchClick The function that is triggered when the user clicks the search button.
 * @param modifier The modifier to be applied to the HomeScreen composable.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    vm: HomeViewModel, // ViewModel instance used to manage the UI state and fetch data from domain layer.
    onRefreshScreen: () -> Unit, // A lambda to handle the refresh logic
    onSearchClick: () -> Unit, // A lambda for search button click action
    modifier: Modifier = Modifier // Modifier to customize the composable layout and appearance
) {
    // Remember the state for the pull-to-refresh feature
    val pullRefreshState = rememberPullRefreshState(refreshing = vm.homeState.value == WeatherState.Loading, onRefresh = onRefreshScreen)

    val shouldShowDialog = remember { mutableStateOf(false) } // State to control error dialog visibility
    val scope = rememberCoroutineScope() // Coroutine scope for launching background tasks
    val state by vm.homeState

    // Trigger data fetching only after the location is fetched
    LaunchedEffect(vm.loadData.value) {
        if (vm.loadData.value) {
            scope.launch {
                executeBodyOrReturnNullSuspended {
                    vm.setHomeLoadingState()
                    vm.weatherRequest?.let { request ->
                        // Fetch weather details based on the location request
                        val result = vm.getWeatherDetails(request)
                        if (result.isSuccess) {
                            vm.setHomeSuccessState(result.getOrThrow()) // set success state with necessary data
                        } else {
                            MsLogger.d("HomeActivity", "Error fetching weather data")
                            vm.setHomeErrorState(result.exceptionOrNull()?.message.toString()) // set error state with error message
                            shouldShowDialog.value = true // Show error dialog if weather data fetching fails
                        }
                        vm.setLoadDataState(false)
                    }
                }
            }
        }
    }

    // Custom alert dialog for error handling
    CustomAlertDialog(
        shouldShowDialog = shouldShowDialog,
        title = "Error",
        text = "Ops! Something bad happened.",
        positiveButtonTitle = "Okay"
    )

    // Scaffold provides a layout structure for the screen, with app bar, FAB, and body content
    Scaffold(
        modifier = modifier.fillMaxSize(), // Fill the screen with the modifier passed
        floatingActionButton = {
            // Floating action button that allows users to trigger the search action
            SmallFloatingActionButton(
                modifier = Modifier,
                onClick = { onSearchClick() }, // Trigger search click action
                containerColor = Surface, // Background color for the button
                shape = RoundedCornerShape(100), // Circular button shape
                elevation = FloatingActionButtonDefaults.elevation(2.dp), // Button elevation for shadow effect
                content = {
                    // Icon inside the FAB that represents the search action
                    Icon(
                        modifier = Modifier
                            .padding(12.dp) // Padding inside the icon
                            .size(50.dp), // Icon size
                        imageVector = Icons.Default.Search, // Search icon
                        contentDescription = "Search", // Accessibility description
                        tint = Color.White // White tint for the icon
                    )
                }
            )
        }
    ) { paddingValues ->
        // Box to hold the screen content, applying padding and pull-to-refresh functionality
        Box(
            modifier = Modifier
                .fillMaxSize() // Fill the entire available space
                .padding(paddingValues) // Apply padding based on scaffold's internal padding
                .pullRefresh(pullRefreshState) // Enable pull-to-refresh functionality
                .background(Background) // Background color or image for the screen
        ) {

            when(state) {
                is WeatherState.Loading -> HomeScreenShimmerLoading() // Shimmer effect for loading state
                is WeatherState.Success -> {
                    // Weather data available, show the actual content
                    WeatherContent(
                        weatherResponse = (state as WeatherState.Success).data,
                        onSearchClick = onSearchClick
                    )
                }
                is WeatherState.Error -> NoDataState( // No data available, show "No Data" state
                    vm = vm,
                    errorMessage = (state as WeatherState.Error).message ?: "No Data available"
                )
            }

            // Uncomment the code below if you want to enable the pull-to-refresh indicator at the top
            /*
            PullRefreshIndicator(
                refreshing = vm.homeState.value == WeatherState.Loading, // Show the refresh indicator when data is loading
                state = pullRefreshState, // State of the pull-to-refresh feature
                modifier = Modifier.align(Alignment.TopCenter) // Position it at the top center of the screen
            )
            */
        }
    }
}

/**
 * Search screen composable where users can search and select a district from a list of districts.
 * Displays a search bar, filters and sorts the district list, and handles loading and selection states.
 *
 * @param vm ViewModel instance used to manage the UI state and fetch data from domain layer.
 * @param onDistrictSelected A lambda that is triggered when a district is selected.
 * @param modifier Modifier to customize the layout and appearance of the composable.
 */
@Composable
fun SearchScreen(
    vm: HomeViewModel, // ViewModel instance used to manage the UI state and fetch data from domain layer.
    onDistrictSelected: (DistrictModel?) -> Unit, // Lambda to handle district selection
    modifier: Modifier = Modifier // Modifier to customize layout
) {
    // Focus and keyboard controllers for managing focus and hiding the keyboard
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val scope = rememberCoroutineScope() // Coroutine scope for launching background tasks

    val state by vm.searchState

    // LaunchedEffect to load the district list if it's empty
    LaunchedEffect(Unit) {
        if (vm.cachedDistrictList.isEmpty()) {
            scope.launch {
                try {
                    vm.setSearchLoadingState() // Set loading state to true while fetching data
                    val result = vm.getDistrictList()
                    if (result.isSuccess) {
                        vm.setSearchSuccessState(result.getOrThrow()) // set success state with necessary data
                    } else {
                        vm.setSearchErrorState(result.exceptionOrNull()?.message.toString()) // set error state with error message
                    }
                } catch (e: Exception) {
                    // Handle any errors during the data loading
                    vm.setSearchErrorState(e.message) // set error state with error message
                    MsLogger.e("SearchScreen", "Error loading district list: ${e.localizedMessage}")
                }
            }
        }
    }

    // State variable to hold the current search query text
    var searchText by remember { mutableStateOf("") }

    // Filter the districts based on the search text
    val filteredDistricts = if (searchText.isBlank()) vm.cachedDistrictList else {
        vm.cachedDistrictList.filter { it.name.contains(searchText, ignoreCase = true) }
    }

    // Sort the districts, placing "My Current Location" at the top
    val sortedDistricts = filteredDistricts.sortedBy { if (it.name == "My Current Location") 0 else 1 }

    // Scaffold layout structure
    Scaffold(
        modifier = modifier,
        topBar = {
            // Search bar at the top of the screen for user input
            SearchBar(
                modifier = Modifier
                    .padding(WindowInsets.statusBars.asPaddingValues()) // Padding for the status bar area
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .padding(horizontal = 16.dp),
                query = searchText,
                onQueryChange = { searchText = it }, // Update the search text as the user types
                onSearch = {
                    focusManager.clearFocus() // Clear the focus when search is triggered
                    keyboardController?.hide() // Hide the keyboard after search
                },
                placeholder = "Search"
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .background(Background) // Set the background color
                .fillMaxSize()
        ) {

            // show appropriate UI according to the state
            when(state) {
                is SearchState.Loading -> SearchScreenShimmerLoading(paddingValues) // Shimmer effect for loading state
                is SearchState.Success -> {
                    // Display the district list inside a LazyColumn
                    LazyColumn(
                        modifier = modifier
                            .padding(paddingValues) // Apply padding values passed from Scaffold
                            .fillMaxSize()
                            .background(Background)
                    ) {
                        // Render each district item
                        items(sortedDistricts.size) { index ->
                            DistrictItem(
                                district = sortedDistricts[index], // Pass district data to the item composable
                                onClick = {
                                    focusManager.clearFocus() // Clear focus on item click
                                    keyboardController?.hide() // Hide keyboard on item click
                                    onDistrictSelected(sortedDistricts[index]) // Notify the parent composable
                                }
                            )
                            // Add a divider between list items, except for the last item
                            if (index < sortedDistricts.size - 1) {
                                HorizontalDivider(color = Color.Gray, thickness = 1.dp)
                            }
                        }
                    }
                }
                is SearchState.Error -> NoDataState( // No data available, show "No Data" state
                    vm = vm,
                    errorMessage = (state as SearchState.Error).message ?: "No Data available"
                )
            }
        }
    }
}