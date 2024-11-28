package com.mubin.forecastery.ui.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mubin.forecastery.base.theme.WeatherTheme
import com.mubin.forecastery.base.utils.MsLogger
import com.mubin.forecastery.base.utils.executeBodyOrReturnNullSuspended
import com.mubin.forecastery.data.model.WeatherRequest
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {

    private val vm by viewModels<HomeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherTheme {

                val scope = rememberCoroutineScope()

                LaunchedEffect(
                    key1 = "HomeActivity",
                    block = {
                        scope.launch {
                            executeBodyOrReturnNullSuspended {

                                val response = vm.getWeatherDetails(
                                    WeatherRequest(
                                        lat = 23.0225,
                                        lon = 72.5714
                                    )
                                )
                                if (response == null) {
                                    MsLogger.d("HomeActivity", "Error fetching weather data")
                                } else {
                                    MsLogger.d("HomeActivity", "$response")
                                }

                            }
                        }
                    }
                )

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello ",//${BuildConfig.API_BASE_URL}!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WeatherTheme {
        Greeting("Android")
    }
}