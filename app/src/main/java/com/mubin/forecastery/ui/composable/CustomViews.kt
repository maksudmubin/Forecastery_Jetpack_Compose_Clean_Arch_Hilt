package com.mubin.forecastery.ui.composable

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.mubin.forecastery.BuildConfig
import com.mubin.forecastery.base.theme.Background
import com.mubin.forecastery.base.theme.Surface
import com.mubin.forecastery.base.utils.MsLogger
import com.mubin.forecastery.base.utils.RubikFontRegular
import com.mubin.forecastery.base.utils.calculateTextSize
import com.mubin.forecastery.base.utils.createImageRequest
import com.mubin.forecastery.base.utils.formatTimezoneOffset
import com.mubin.forecastery.base.utils.formatUnixTime
import com.mubin.forecastery.base.utils.shimmerEffect
import com.mubin.forecastery.data.model.DistrictModel
import com.mubin.forecastery.data.model.WeatherResponse
import com.mubin.forecastery.ui.home.HomeUiState
import com.mubin.forecastery.ui.home.WeatherItem
import java.util.Locale


/**
 * Controls the visibility of the system status bar and its color.
 *
 * @param isVisible A flag indicating whether the status bar should be visible.
 */
@Composable
fun ShowHideStatusBarScreen(isVisible: Boolean) {
    // Remember the System UI Controller to manage system bar properties
    val systemUiController = rememberSystemUiController()

    // Log the status bar visibility change
    MsLogger.d(
        "ShowHideStatusBarScreen",
        "Setting status bar visibility to ${if (isVisible) "visible" else "hidden"}"
    )

    // Set the color of the system bars and their visibility based on the isVisible parameter
    systemUiController.setSystemBarsColor(
        color = if (isVisible) Background else Color.Transparent, // Set background color based on visibility
        darkIcons = false // Use light icons for better contrast
    )
    systemUiController.isStatusBarVisible = isVisible // Show or hide the status bar
}

/**
 * Composable function to display a circular progress bar.
 * The progress bar is used to indicate loading states in the UI.
 * The appearance of the circular progress indicator can be customized via
 * the `modifier`, `width`, `loadingColor`, and `trackColor` parameters.
 *
 * @param modifier [Modifier] to apply additional styling or layout adjustments (default size is 36.dp).
 * @param width [Dp] to set the stroke width of the progress bar (default is 4.dp).
 * @param loadingColor [Color] to set the color of the loading indicator (default is white).
 * @param trackColor [Color] to set the color of the background track (default is `Surface` color).
 */
@Composable
fun CircularProgressBar(
    modifier: Modifier = Modifier
        .size(size = 36.dp), // Default size of the progress bar
    width: Dp = 4.dp, // Default stroke width of the progress bar
    loadingColor: Color = Color.White, // Default loading color (white)
    trackColor: Color = Surface // Default track color (Surface color)
) {
    // Log the circular progress bar state (size, width, loading color, and track color)
    MsLogger.d("CircularProgressBar", "Size: $modifier, Width: $width, LoadingColor: $loadingColor, TrackColor: $trackColor")

    CircularProgressIndicator(
        modifier = modifier, // Apply the modifier to control size and layout
        color = loadingColor, // Set the color for the loading indicator
        strokeWidth = width, // Set the stroke width for the progress bar
        trackColor = trackColor // Set the track color for the background circle
    )
}

/**
 * Composable function to display a customizable alert dialog.
 * The dialog can show a title, a message, and a positive button.
 * The dialog visibility is controlled via the [shouldShowDialog] MutableState.
 * The dialog will be dismissed when the positive button is clicked or when the user dismisses the dialog.
 *
 * @param shouldShowDialog [MutableState<Boolean>] that controls whether the dialog is shown or not.
 * @param title [String] the title of the alert dialog.
 * @param text [String] the content text displayed in the dialog.
 * @param positiveButtonTitle [String] the title of the positive button, typically used for actions like "OK" or "Confirm".
 */
@Composable
fun CustomAlertDialog(
    shouldShowDialog: MutableState<Boolean>, // Controls the visibility of the dialog
    title: String, // Title displayed in the dialog
    text: String, // Main content of the dialog
    positiveButtonTitle: String, // Title for the positive button
    onDialogDismissed: () -> Unit = {} // Called when dialog is dismissed
) {
    // Log the dialog state (whether it is shown or not)
    MsLogger.d("CustomAlertDialog", "Dialog visibility: ${shouldShowDialog.value}, Title: $title, Text: $text, Button: $positiveButtonTitle")

    // Show the alert dialog only when shouldShowDialog is true
    if (shouldShowDialog.value) {
        AlertDialog(
            containerColor = Surface, // The background color of the dialog container
            onDismissRequest = {
                // Set the dialog visibility to false when it is dismissed (e.g., tapped outside or back pressed)
                shouldShowDialog.value = false
            },
            title = {
                Text(
                    text = title, // Title of the dialog
                    color = MaterialTheme.colorScheme.onPrimary // Title color based on theme
                )
            },
            text = {
                Text(
                    text = text, // Text content of the dialog
                    color = MaterialTheme.colorScheme.onPrimary // Text color based on theme
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Close the dialog when the button is clicked
                        onDialogDismissed.invoke()
                        shouldShowDialog.value = false
                    },
                    border = BorderStroke(
                        width = 1.dp, // Border width for the button
                        color = MaterialTheme.colorScheme.outline // Border color based on theme
                    )
                ) {
                    Text(
                        text = positiveButtonTitle, // Button text
                        color = MaterialTheme.colorScheme.onPrimary // Button text color based on theme
                    )
                }
            }
        )
    }
}

@Composable
fun WeatherContent(weatherResponse: WeatherResponse) {
    Column(
        modifier = Modifier
            .padding(16.dp),
        //.verticalScroll(rememberScrollState()), // Enable scrolling
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main Weather Info
        Row (
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            weatherResponse.weather?.firstOrNull()?.icon?.let { iconId ->

                WeatherIconWithCurvedText(
                    iconId = iconId,
                    description = weatherResponse.weather?.firstOrNull()?.description.orEmpty()
                        .replaceFirstChar {
                            if (it.isLowerCase())
                                it.titlecase(Locale.ROOT)
                            else
                                it.toString()
                        }
                )

            }

            Column (
                modifier = Modifier
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = weatherResponse.name.orEmpty(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${weatherResponse.main?.temp?.toInt() ?: "--"}°C",
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White
                )
                Text(
                    text = "Feels like ${weatherResponse.main?.feelsLike}°C",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Additional Weather Info
        HorizontalDivider(
            modifier = Modifier
                .padding(top = 4.dp),
            color = Color.Gray,
            thickness = 1.dp
        )
        WeatherDetailGrid(weatherResponse)
    }
}

@Composable
fun WeatherIconWithCurvedText(
    iconId: String,
    description: String
) {
    val density = LocalDensity.current
    val imageSize = 100.dp
    val textPadding = 16.dp // Padding between the icon and the text curve
    val letterSpace = 2f // Space between characters

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(120.dp) // Ensure enough space for icon and curved text
    ) {
        // Weather Icon
        SubcomposeAsyncImage(
            modifier = Modifier
                .background(
                    color = Color(0xFFB8B8B8),
                    shape = CircleShape
                )
                .size(imageSize),
            model = createImageRequest(
                context = LocalContext.current,
                url = "${BuildConfig.IMAGE_BASE_URL}${iconId}@4x.png"
            ),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressBar()
                }
            }
        )

        // Curved Text Below Icon
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = (imageSize.toPx() / 2) + textPadding.toPx() // Radius for the bottom arc
            val centerX = size.width / 2
            val centerY = size.height / 2

            // Path for the curved text (bottom arc of the circle)
            val path = Path().apply {
                addArc(
                    oval = Rect(
                        center = Offset(centerX, centerY),
                        radius = radius
                    ),
                    startAngleDegrees = -180f, // Start below the left side
                    sweepAngleDegrees = -180f  // Sweep across the bottom arc
                )
            }

            // Text paint configuration
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.LTGRAY
                textSize = calculateTextSize(description, density) // Dynamically adjust text size
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
                letterSpacing = letterSpace / textSize // Adjust letter spacing (proportional to text size)
            }

            // Draw the text along the path
            drawContext.canvas.nativeCanvas.apply {
                drawTextOnPath(description, path.asAndroidPath(), 0f, 0f, textPaint)
            }
        }
    }
}

@Composable
fun WeatherDetailGrid(weatherResponse: WeatherResponse) {

    val weatherItems = listOf(
        WeatherItem("Humidity", "${weatherResponse.main?.humidity ?: "--"}%", Icons.Default.WaterDrop),
        WeatherItem("Pressure", "${weatherResponse.main?.pressure ?: "--"} hPa", Icons.Default.Speed),
        WeatherItem("Wind Speed", "${weatherResponse.wind?.speed ?: "--"} m/s", Icons.Default.Air),
        WeatherItem("Cloudiness", "${weatherResponse.clouds?.all ?: "--"}%", Icons.Default.Cloud),
        WeatherItem("Visibility", "${weatherResponse.visibility ?: "--"} m", Icons.Default.Visibility),
        WeatherItem("Time Zone", formatTimezoneOffset(weatherResponse.timezone), Icons.Default.Public),
        WeatherItem("Sunrise", formatUnixTime(weatherResponse.sys?.sunrise), Icons.Default.WbSunny),
        WeatherItem("Sunset", formatUnixTime(weatherResponse.sys?.sunset), Icons.Default.Stars)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),  // Horizontal spacing between items
        verticalArrangement = Arrangement.spacedBy(8.dp)  // Vertical spacing between items
    ) {
        items(weatherItems.size) { index ->
            WeatherInfoCard(
                label = weatherItems[index].label,
                value = weatherItems[index].value,
                icon = weatherItems[index].icon
            )
        }
    }
}

@Composable
fun WeatherInfoCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .padding(bottom = 8.dp)
            .height(100.dp), // Ensure equal heights
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray),
                textAlign = TextAlign.Center
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                maxLines = 1, // Prevent overflow
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ShimmerLoading() {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val shimmerColorShades = listOf(
        Color.Gray.copy(alpha = 0.6f),
        Color.Gray.copy(alpha = 0.2f),
        Color.Gray.copy(alpha = 0.6f)
    )
    val transition = rememberInfiniteTransition(label = "")
    val translateAnim = transition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(animation = tween(1000, easing = LinearEasing)),
        label = ""
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row (
            modifier = Modifier
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .shimmerEffect(translateAnim.value, shimmerColorShades)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Box(
                    modifier = Modifier
                        .width(screenWidth.times(0.4f))
                        .height(24.dp)
                        .shimmerEffect(translateAnim.value, shimmerColorShades)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .width(screenWidth.times(0.35f))
                        .height(46.dp)
                        .shimmerEffect(translateAnim.value, shimmerColorShades)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .width(screenWidth.times(0.3f))
                        .height(16.dp)
                        .shimmerEffect(translateAnim.value, shimmerColorShades)
                )
            }

        }
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .fillMaxWidth()
                .height(1.dp)
                .shimmerEffect(translateAnim.value, shimmerColorShades)
        )
        repeat(4) {
            Row {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                        .shimmerEffect(translateAnim.value, shimmerColorShades)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                        .shimmerEffect(translateAnim.value, shimmerColorShades)
                )
            }
        }
    }
}

@Composable
fun NoDataState(uiState: HomeUiState) {
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (!uiState.permissionGranted) {
            Button(
                onClick = {
                    val activity = context as? Activity
                    activity?.let {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        it.startActivity(intent)
                    } ?: run {
                        MsLogger.d("PermissionDialog", "Unable to open settings; activity context is null.")
                    }
                },
                border = BorderStroke(
                    width = 1.dp, // Border width for the button
                    color = MaterialTheme.colorScheme.outline // Border color based on theme
                )
            ) {
                Text(
                    text = "Grant Location Permission", // Button text
                    color = MaterialTheme.colorScheme.onPrimary // Button text color based on theme
                )
            }
        } else {
            Text(
                text = "No weather data available.",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.LightGray),
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
    onSearch: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth() // Fill the available width
            .height(56.dp) // Set fixed height for the search bar
            .clip(RoundedCornerShape(16.dp)) // Apply rounded corners
            .background(Surface) // Set background color
            .padding(horizontal = 16.dp, vertical = 8.dp), // Padding inside the search bar
        verticalAlignment = Alignment.CenterVertically // Vertically center content in the Row
    ) {
        // Search Icon
        Icon(
            imageVector = Icons.Default.Search, // Load the search icon
            tint = Color.White,
            contentDescription = "Search", // Accessibility content description
            modifier = Modifier.size(24.dp) // Set icon size
        )
        Spacer(modifier = Modifier.width(10.dp)) // Add some space between the icon and the text field

        // Basic Text Field for query input
        BasicTextField(
            modifier = Modifier.fillMaxWidth(), // Allow text field to take full available width
            value = query, // Bind the current query value
            onValueChange = { newQuery ->
                MsLogger.d("SearchBar", "Query changed: $newQuery") // Log query change
                onQueryChange(newQuery) // Invoke onQueryChange lambda when text changes
            },
            singleLine = true, // Ensure the text field is single line
            cursorBrush = SolidColor(Background), // Set cursor color to match background
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = RubikFontRegular, // Set font family
                fontSize = 14.sp, // Set font size
                color = Color.White.copy(alpha = 0.5f) // Set text color with opacity
            ),
            decorationBox = { innerTextField ->
                // Show placeholder text when the query is empty
                if (query.isEmpty()) {
                    Text(
                        text = placeholder, // Display placeholder text
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = RubikFontRegular, // Set font family for placeholder
                            fontSize = 14.sp, // Set font size for placeholder
                            color = Color.White.copy(alpha = 0.3f) // Lighter color for placeholder
                        )
                    )
                }
                innerTextField() // Display the actual text field content
            },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done), // Set IME action to "Done"
            keyboardActions = KeyboardActions(onDone = {
                MsLogger.d("SearchBar", "Search triggered") // Log when search is done
                onSearch() // Invoke onSearch lambda when "Done" action is triggered
            })
        )
    }
}

@Composable
fun DistrictItem(
    district: DistrictModel?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Location Icon",
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = district?.name ?: "--",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
    }
}