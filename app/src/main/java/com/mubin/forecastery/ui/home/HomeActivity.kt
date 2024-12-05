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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.LocationServices
import com.mubin.forecastery.base.theme.WeatherTheme
import com.mubin.forecastery.base.utils.MsLogger
import com.mubin.forecastery.data.model.WeatherRequest
import com.mubin.forecastery.ui.composable.CustomAlertDialog
import com.mubin.forecastery.ui.composable.ShowHideStatusBarScreen
import dagger.hilt.android.AndroidEntryPoint

/**
 * The main activity of the Weather app. It handles location permissions, fetches weather data,
 * and manages the UI state related to the home screen, including displaying weather information.
 */
@AndroidEntryPoint
class HomeActivity : ComponentActivity() {

    // ViewModel instance used to manage the UI state and fetch data from domain layer
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

            // Apply the weather theme to the entire screen content
            WeatherTheme {

                // Local context and navigation controller setup
                val context = LocalContext.current
                val navController = rememberNavController() // Set up the navigation controller

                // State to control visibility of the permission dialog
                val shouldShowPermissionDialog = remember { mutableStateOf(false) }

                // State to track the current permission status
                val state by vm.permissionState

                // Check if the permission state is "Requesting"
                if (state is PermissionState.Requesting) {
                    // Handle the permission request logic
                    RequestLocationPermission(
                        onGranted = {
                            // If permission is granted, update the permission state
                            vm.updatePermissionState(PermissionState.Granted)
                            // Fetch location and set weather request data
                            fetchLocation(
                                context = context,
                                onLocationFetched = { lat, lon ->
                                    vm.weatherRequest = WeatherRequest(lat = lat, lon = lon)
                                    vm.setLoadDataState(true) // Set the data loading state
                                }
                            )
                        },
                        onDenied = {
                            // If permission is denied, update state and show an error message
                            vm.updatePermissionState(PermissionState.Denied)
                            vm.setHomeErrorState("Location Permission denied")
                            MsLogger.d("RequestLocationPermission", "Location permission denied")
                        },
                        onPermanentlyDenied = {
                            // If permission is permanently denied, update state and show dialog
                            vm.updatePermissionState(PermissionState.PermanentlyDenied)
                            vm.setHomeErrorState("Location Permission denied")
                            shouldShowPermissionDialog.value = true // Show the dialog for permanent denial
                            MsLogger.d("RequestLocationPermission", "Location permission permanently denied")
                        }
                    )
                }

                // Custom alert dialog for requesting permission if permanently denied
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
                            MsLogger.d("PermissionDialog", "Navigating to app settings")
                        } ?: run {
                            // Log if activity context is null
                            MsLogger.d("PermissionDialog", "Unable to open settings; activity context is null.")
                        }
                    }
                )

                // Navigation Host to handle screen navigation
                WeatherAppNavHost(navController = navController, vm = vm)
            }
        }
    }

    /**
     * A composable function to request location permission and handle the result.
     *
     * @param onGranted A lambda function invoked when the permission is granted.
     * @param onDenied A lambda function invoked when the permission is denied, but not permanently.
     * @param onPermanentlyDenied A lambda function invoked when the permission is permanently denied.
     */
    @Composable
    fun RequestLocationPermission(
        onGranted: () -> Unit,
        onDenied: () -> Unit,
        onPermanentlyDenied: () -> Unit
    ) {
        // Obtain the context from the composable current scope
        val context = LocalContext.current

        // Initialize a permission launcher to request location permission
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                MsLogger.d("RequestLocationPermission", "Permission granted")
                onGranted() // Call the granted callback
            } else {
                // Check if the user has denied the permission but not permanently
                val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                if (shouldShowRationale) {
                    MsLogger.d("RequestLocationPermission", "Permission denied, showing rationale")
                    onDenied() // Call the denied callback
                } else {
                    MsLogger.d("RequestLocationPermission", "Permission permanently denied")
                    onPermanentlyDenied() // Call the permanently denied callback
                }
            }
        }

        // Request permission or verify current permission status on composition
        LaunchedEffect(Unit) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                MsLogger.d("RequestLocationPermission", "Permission not granted, requesting...")
                launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
                MsLogger.d("RequestLocationPermission", "Permission already granted")
                onGranted() // Permission already granted
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

        // If permission is granted, fetch location and fetch weather data
        if (isPermissionGranted) {
            vm.updatePermissionState(PermissionState.Granted)
            fetchLocation(
                context = this,
                onLocationFetched = { lat, lon ->
                    vm.weatherRequest = WeatherRequest(lat = lat, lon = lon)
                    vm.setLoadDataState(true)
                }
            )
        }
    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WeatherTheme {

    }
}