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

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {

    private val vm by viewModels<HomeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShowHideStatusBarScreen(true)
            WeatherTheme {

                val context = LocalContext.current
                val navController = rememberNavController()
                val shouldShowDialog = remember { mutableStateOf(false) }
                val shouldShowPermissionDialog = remember { mutableStateOf(false) }
                val scope = rememberCoroutineScope()

                val locationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
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

                LaunchedEffect(vm.uiState.permissionGranted) {
                    if(vm.uiState.permissionGranted) {
                        vm.uiState.askPermission = false
                        fetchLocation(
                            context = context,
                            onLocationFetched = { lat, lon ->
                                vm.uiState.weatherRequest = WeatherRequest(
                                    lat = lat,
                                    lon = lon
                                )
                                vm.uiState.loadData = true
                            }
                        )
                    } else {
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
                                    val response = vm.getWeatherDetails(request)
                                    if (response == null) {
                                        MsLogger.d("HomeActivity", "Error fetching weather data")
                                        shouldShowDialog.value = true
                                    } else {
                                        MsLogger.d("HomeActivity", "$response")
                                        vm.uiState.response = response
                                        vm.uiState.loadData = false
                                    }
                                }
                                vm.uiState.isLoading = false
                            }
                        }
                    }
                }

                // Custom Alert Dialog for errors
                CustomAlertDialog(
                    shouldShowDialog = shouldShowDialog,
                    title = "Error",
                    text = "Ops! Something bad happened.",
                    positiveButtonTitle = "Okay"
                )

                CustomAlertDialog(
                    shouldShowDialog = shouldShowPermissionDialog,
                    title = "Permission Required",
                    text = "Location permission is required to continue. Please grant the permission in the app settings.",
                    positiveButtonTitle = "Go to Settings",
                    onDialogDismissed = {
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

                // Navigation Host
                WeatherAppNavHost(navController = navController, uiState = vm.uiState)
            }
        }
    }

    // Function to fetch location
    @SuppressLint("MissingPermission")
    private fun fetchLocation(context: Context, onLocationFetched: (Double, Double) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                onLocationFetched(location.latitude, location.longitude)
            } else {
                MsLogger.d("Location", "Failed to retrieve location.")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val isPermissionGranted = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

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