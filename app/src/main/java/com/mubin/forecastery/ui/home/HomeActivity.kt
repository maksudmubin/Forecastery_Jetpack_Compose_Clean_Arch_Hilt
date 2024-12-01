package com.mubin.forecastery.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.LocationServices
import com.mubin.forecastery.base.theme.WeatherTheme
import com.mubin.forecastery.base.utils.MsLogger
import com.mubin.forecastery.base.utils.executeBodyOrReturnNullSuspended
import com.mubin.forecastery.data.model.WeatherRequest
import com.mubin.forecastery.ui.composable.CustomAlertDialog
import com.mubin.forecastery.ui.composable.ShowHideStatusBarScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * The main activity of the Weather app. It handles location permissions, fetches weather data,
 * and manages the UI state related to the home screen, including displaying weather information.
 */
@AndroidEntryPoint
class HomeActivity : ComponentActivity() {

    // ViewModel instance used to manage the UI state
    private val vm by viewModels<HomeViewModel>()

    /**
     * Called when the activity is created. This sets up the UI, permission requests,
     * and navigation logic, as well as triggering the weather data fetch.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Enable edge-to-edge display for the app

        // Setting the content view for the activity
        setContent {
            ShowHideStatusBarScreen(true) // Show the status bar on this screen
            WeatherTheme {

                val context = LocalContext.current
                val navController = rememberNavController() // Set up the navigation controller
                val shouldShowDialog = remember { mutableStateOf(false) } // State to control error dialog visibility
                val shouldShowPermissionDialog = remember { mutableStateOf(false) } // State to control permission dialog visibility
                val scope = rememberCoroutineScope()

                // Permission launcher to handle permission request for location
                val locationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    // Handle permission result
                    if (isGranted) {
                        vm.uiState.permissionGranted = true
                    } else {
                        // Check if permission was permanently denied
                        val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                            context as Activity, Manifest.permission.ACCESS_FINE_LOCATION
                        )
                        if (!showRationale) {
                            // Permission permanently denied
                            vm.uiState.permissionGranted = false
                            vm.uiState.askPermission = false // Prevent repeated requests
                            shouldShowPermissionDialog.value = true // Show dialog for permanently denied
                        }
                    }
                }

                // Trigger permission request on launch
                LaunchedEffect(vm.uiState.askPermission) {
                    if (vm.uiState.askPermission) {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }

                // Handle logic for when the permission is granted
                LaunchedEffect(vm.uiState.permissionGranted) {
                    if(vm.uiState.permissionGranted) {
                        vm.uiState.askPermission = false
                        fetchLocation(
                            context = context,
                            onLocationFetched = { lat, lon ->
                                // Once location is fetched, set up weather request
                                vm.uiState.weatherRequest = WeatherRequest(
                                    lat = lat,
                                    lon = lon
                                )
                                vm.uiState.loadData = true
                            }
                        )
                    } else {
                        // If permission is not granted, prompt the user to grant it
                        vm.uiState.askPermission = true
                    }
                }

                // Trigger data fetching only after the location is fetched
                LaunchedEffect(vm.uiState.loadData) {
                    if (vm.uiState.loadData) {
                        scope.launch {
                            executeBodyOrReturnNullSuspended {
                                vm.uiState.isLoading = true
                                vm.uiState.weatherRequest?.let { request ->
                                    // Fetch weather details based on the location request
                                    val response = vm.getWeatherDetails(request)
                                    if (response == null) {
                                        MsLogger.d("HomeActivity", "Error fetching weather data")
                                        shouldShowDialog.value = true // Show error dialog if weather data fetching fails
                                    } else {
                                        MsLogger.d("HomeActivity", "$response") // Log successful weather data retrieval
                                        vm.uiState.response = response
                                        vm.uiState.loadData = false
                                    }
                                }
                                vm.uiState.isLoading = false
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

                // Custom alert dialog for requesting permission
                CustomAlertDialog(
                    shouldShowDialog = shouldShowPermissionDialog,
                    title = "Permission Required",
                    text = "Location permission is required to continue. Please grant the permission in the app settings.",
                    positiveButtonTitle = "Go to Settings",
                    onDialogDismissed = {
                        // If the dialog is dismissed, navigate the user to the settings to grant permission
                        val activity = context as? Activity
                        activity?.let {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            it.startActivity(intent)
                        } ?: run {
                            MsLogger.d("PermissionDialog", "Unable to open settings; activity context is null.")
                        }
                    }
                )

                // Navigation Host to handle screen navigation
                WeatherAppNavHost(navController = navController, uiState = vm.uiState)
            }
        }
    }

    /**
     * Fetches the current location using FusedLocationProviderClient.
     * This method retrieves the last known location and calls the callback with the latitude and longitude.
     *
     * @param context the context from which the location service is accessed.
     * @param onLocationFetched callback invoked with the fetched latitude and longitude.
     */
    @SuppressLint("MissingPermission")
    private fun fetchLocation(context: Context, onLocationFetched: (Double, Double) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                // Log the fetched location
                MsLogger.d("Location", "Fetched location: Latitude ${location.latitude}, Longitude ${location.longitude}")
                onLocationFetched(location.latitude, location.longitude)
            } else {
                // Log failure to retrieve location
                MsLogger.d("Location", "Failed to retrieve location.")
            }
        }
    }

    /**
     * Called when the activity is resumed. It checks the permission status and fetches the location if granted.
     */
    override fun onResume() {
        super.onResume()
        val isPermissionGranted = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        // If permission is granted, fetch location
        if (isPermissionGranted) {
            vm.uiState.permissionGranted = true
            vm.uiState.askPermission = false
            fetchLocation(
                context = this,
                onLocationFetched = { lat, lon ->
                    vm.uiState.weatherRequest = WeatherRequest(lat = lat, lon = lon)
                    vm.uiState.loadData = true
                }
            )
        } else {
            // If permission is not granted, prompt the user for permission
            vm.uiState.permissionGranted = false
            vm.uiState.askPermission = true
        }
    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WeatherTheme {

    }
}