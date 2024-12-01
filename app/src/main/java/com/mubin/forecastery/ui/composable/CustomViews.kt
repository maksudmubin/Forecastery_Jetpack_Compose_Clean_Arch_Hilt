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
import androidx.compose.material.icons.filled.Edit
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
import com.mubin.forecastery.domain.entities.WeatherEntity
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
                    color = Color.White // Title color based on theme
                )
            },
            text = {
                Text(
                    text = text, // Text content of the dialog
                    color = Color.White // Text color based on theme
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

/**
 * Displays the main weather content, including weather icon, temperature, and additional weather details.
 *
 * @param weatherResponse The weather data to display.
 * @param onSearchClick Callback invoked when the search action is triggered.
 */
@Composable
fun WeatherContent(
    weatherResponse: WeatherEntity, // Weather data to be displayed
    onSearchClick: () -> Unit // Action to perform when search is clicked
) {
    Column(
        modifier = Modifier
            .padding(16.dp), // Padding around the content
        //.verticalScroll(rememberScrollState()), // Uncomment for scrollable layout
        verticalArrangement = Arrangement.spacedBy(16.dp), // Space between elements
        horizontalAlignment = Alignment.CenterHorizontally // Center horizontally
    ) {
        // Main weather information (icon, location, and temperature)
        Row(
            modifier = Modifier.fillMaxWidth(), // Row fills the parent's width
            verticalAlignment = Alignment.CenterVertically // Align items vertically
        ) {
            WeatherIconWithCurvedText(
                iconId = weatherResponse.icon,
                description = weatherResponse.description
                    .replaceFirstChar { it.titlecase(Locale.ROOT) } // Capitalize first letter
            )

            // Display location name, temperature, and feels-like info
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Row(
                    modifier = Modifier.clickable { onSearchClick() }, // Search click action
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = weatherResponse.locationName, // City name
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White, // White color for text
                        textAlign = TextAlign.Center, // Center-align text
                        overflow = TextOverflow.Ellipsis // Handle overflow gracefully
                    )
                    Spacer(modifier = Modifier.width(4.dp)) // Space between text and icon
                    Icon(
                        modifier = Modifier
                            .padding(end = 16.dp) // Add padding to the end
                            .size(18.dp), // Icon size
                        imageVector = Icons.Default.Edit, // Edit icon
                        contentDescription = null, // No accessibility description
                        tint = Color.White // White color for icon
                    )
                }
                Text(
                    text = "${weatherResponse.temperature.toInt()}°C", // Current temperature
                    style = MaterialTheme.typography.displayLarge, // Large text for emphasis
                    color = Color.White // White color
                )
                Text(
                    text = "Feels like ${weatherResponse.feelsLike}°C", // Feels-like temperature
                    style = MaterialTheme.typography.bodyLarge, // Smaller text style
                    color = Color.White, // White color
                    textAlign = TextAlign.Center
                )
            }
        }

        // Divider to visually separate the main info and additional details
        HorizontalDivider(
            modifier = Modifier.padding(top = 4.dp), // Add space above the divider
            color = Color.Gray, // Divider color
            thickness = 1.dp // Thickness of the divider
        )

        // Grid layout for additional weather details
        WeatherDetailGrid(weatherResponse)
    }
}

/**
 * Displays a weather icon with a curved text description beneath it.
 *
 * @param iconId The identifier for the weather icon.
 * @param description A textual description of the weather.
 */
@Composable
fun WeatherIconWithCurvedText(
    iconId: String, // Icon ID for fetching the weather image
    description: String // Description to display below the icon
) {
    val density = LocalDensity.current
    val imageSize = 100.dp // Size of the weather icon
    val textPadding = 16.dp // Space between icon and curved text
    val letterSpace = 2f // Spacing between characters in the text

    Box(
        contentAlignment = Alignment.Center, // Center content in the box
        modifier = Modifier.size(120.dp) // Ensure enough space for both icon and text
    ) {
        MsLogger.d("Image_Url_From_Gradle", BuildConfig.IMAGE_BASE_URL + iconId + "@4x.png")
        // Weather icon image
        SubcomposeAsyncImage(
            modifier = Modifier
                .background(Color(0xFFB8B8B8), CircleShape) // Circular background for icon
                .size(imageSize),
            model = createImageRequest(
                context = LocalContext.current,
                url = "${BuildConfig.IMAGE_BASE_URL}${iconId}@4x.png" // Build image URL
            ),
            contentDescription = null, // No accessibility description
            contentScale = ContentScale.Fit, // Scale the image to fit
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressBar() // Show a loading indicator
                }
            }
        )

        // Curved text below the icon
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = (imageSize.toPx() / 2) + textPadding.toPx() // Radius for the text arc
            val centerX = size.width / 2
            val centerY = size.height / 2

            // Define path for text along the bottom arc of the circle
            val path = Path().apply {
                addArc(
                    oval = Rect(
                        center = Offset(centerX, centerY),
                        radius = radius
                    ),
                    startAngleDegrees = -180f, // Start from bottom-left
                    sweepAngleDegrees = -180f // Cover the bottom arc
                )
            }

            // Configure text paint properties
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.LTGRAY // Text color
                textSize = calculateTextSize(description, density) // Dynamic text size
                isAntiAlias = true // Smooth edges
                textAlign = android.graphics.Paint.Align.CENTER // Center-align text
                letterSpacing = letterSpace / textSize // Adjust spacing based on size
            }

            // Draw text along the arc path
            drawContext.canvas.nativeCanvas.apply {
                drawTextOnPath(description, path.asAndroidPath(), 0f, 0f, textPaint)
            }
        }
    }
}

/**
 * Displays a grid of detailed weather information.
 *
 * @param weatherResponse The weather data to display in the grid.
 */
@Composable
fun WeatherDetailGrid(weatherResponse: WeatherEntity) {
    val weatherItems = listOf(
        WeatherItem("Humidity", "${weatherResponse.humidity ?: "--"}%", Icons.Default.WaterDrop),
        WeatherItem("Pressure", "${weatherResponse.pressure ?: "--"} hPa", Icons.Default.Speed),
        WeatherItem("Wind Speed", "${weatherResponse.windSpeed ?: "--"} m/s", Icons.Default.Air),
        WeatherItem("Cloudiness", "${weatherResponse.cloudiness ?: "--"}%", Icons.Default.Cloud),
        WeatherItem("Visibility", "${weatherResponse.visibility ?: "--"} m", Icons.Default.Visibility),
        WeatherItem("Time Zone", formatTimezoneOffset(weatherResponse.timeZone), Icons.Default.Public),
        WeatherItem("Sunrise", formatUnixTime(weatherResponse.sunrise), Icons.Default.WbSunny),
        WeatherItem("Sunset", formatUnixTime(weatherResponse.sunset), Icons.Default.Stars)
    )

    // Lazy grid for better performance and dynamic content rendering
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // Fixed two columns
        modifier = Modifier.fillMaxSize(), // Grid spans full size
        horizontalArrangement = Arrangement.spacedBy(16.dp), // Space between columns
        verticalArrangement = Arrangement.spacedBy(8.dp) // Space between rows
    ) {
        items(weatherItems.size) { index ->
            WeatherInfoCard(
                label = weatherItems[index].label, // Label for the detail
                value = weatherItems[index].value, // Value to display
                icon = weatherItems[index].icon // Icon for the detail
            )
        }
        item{
            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}

/**
 * Represents a single card in the weather detail grid.
 *
 * @param label The label for the weather detail.
 * @param value The value for the detail.
 * @param icon The icon representing the detail.
 * @param modifier Modifiers for styling the card.
 */
@Composable
fun WeatherInfoCard(
    label: String, // Label for the detail (e.g., Humidity)
    value: String, // Value for the detail (e.g., 70%)
    icon: ImageVector, // Icon representing the detail
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(bottom = 8.dp) // Add padding at the bottom
            .height(100.dp), // Set a uniform height
        shape = RoundedCornerShape(8.dp), // Rounded corners for the card
        elevation = CardDefaults.cardElevation(4.dp), // Elevation for shadow effect
        colors = CardDefaults.cardColors(containerColor = Surface) // Set container color
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp), // Padding inside the card
            horizontalAlignment = Alignment.CenterHorizontally, // Center content horizontally
            verticalArrangement = Arrangement.Center // Center content vertically
        ) {
            Icon(
                imageVector = icon, // Display the icon
                contentDescription = null, // No accessibility description
                tint = Color.LightGray, // Icon color
                modifier = Modifier.size(24.dp) // Size of the icon
            )
            Spacer(modifier = Modifier.height(8.dp)) // Space between icon and text
            Text(
                text = label, // Display the label
                style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray), // Label style
                textAlign = TextAlign.Center // Center-align text
            )
            Text(
                text = value, // Display the value
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White, // White color for value text
                    fontWeight = FontWeight.Bold // Bold for emphasis
                ),
                textAlign = TextAlign.Center, // Center-align value text
                maxLines = 1, // Prevent overflow
                overflow = TextOverflow.Ellipsis // Handle long text gracefully
            )
        }
    }
}

/**
 * Displays a loading shimmer effect while weather data is being fetched.
 */
@Composable
fun ShimmerLoading() {
    // Fetch screen width from configuration
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    // Colors used in the shimmer effect animation
    val shimmerColorShades = listOf(
        Color.Gray.copy(alpha = 0.6f), // Slightly opaque gray color
        Color.Gray.copy(alpha = 0.2f), // More transparent gray color
        Color.Gray.copy(alpha = 0.6f)  // Slightly opaque gray again
    )

    // Transition for infinite shimmer animation
    val transition = rememberInfiniteTransition(label = "Shimmer Transition")

    // Translation animation for shimmer effect
    val translateAnim = transition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        ),
        label = "Shimmer Animation"
    )

    // Main layout for shimmer loading items
    Column(
        modifier = Modifier
            .fillMaxSize() // Fill the entire available screen size
            .padding(16.dp), // Add padding to avoid edge overlaps
        verticalArrangement = Arrangement.spacedBy(16.dp), // Space between items
    ) {
        Row(
            modifier = Modifier.padding(8.dp), // Padding for the Row
            verticalAlignment = Alignment.CenterVertically // Center-align contents vertically
        ) {
            // Circular shimmer effect (e.g., for profile images)
            Box(
                modifier = Modifier
                    .size(100.dp) // Set size to 100dp
                    .clip(CircleShape) // Clip the box to a circular shape
                    .shimmerEffect(translateAnim.value, shimmerColorShades) // Apply shimmer effect
            )
            Spacer(modifier = Modifier.width(16.dp)) // Spacer for spacing between items
            Column {
                // Rectangular shimmer effect (e.g., for text placeholders)
                Box(
                    modifier = Modifier
                        .width(screenWidth.times(0.4f)) // 40% of screen width
                        .height(24.dp) // Fixed height
                        .shimmerEffect(translateAnim.value, shimmerColorShades)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .width(screenWidth.times(0.35f)) // 35% of screen width
                        .height(46.dp)
                        .shimmerEffect(translateAnim.value, shimmerColorShades)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .width(screenWidth.times(0.3f)) // 30% of screen width
                        .height(16.dp)
                        .shimmerEffect(translateAnim.value, shimmerColorShades)
                )
            }
        }

        // Separator line shimmer effect
        Box(
            modifier = Modifier
                .padding(top = 2.dp) // Slight top padding
                .fillMaxWidth() // Full-width separator
                .height(1.dp) // Fixed height for line
                .shimmerEffect(translateAnim.value, shimmerColorShades)
        )

        // Repeat shimmer rows (e.g., grid items)
        repeat(4) {
            Row {
                // First column shimmer item
                Box(
                    modifier = Modifier
                        .weight(1f) // Distribute width equally
                        .height(100.dp) // Fixed height
                        .shimmerEffect(translateAnim.value, shimmerColorShades)
                )
                Spacer(modifier = Modifier.width(16.dp)) // Space between columns
                // Second column shimmer item
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

/**
 * A composable function that displays a "No Data" state.
 * It shows a button to grant location permission if it is not granted,
 * or displays a message indicating the unavailability of weather data.
 *
 * @param uiState The state containing the necessary information, such as permission status.
 */
@Composable
fun NoDataState(uiState: HomeUiState) {
    // Obtain the current context for navigation or intent usage
    val context = LocalContext.current

    // A box to center-align its content on the screen
    Box(
        modifier = Modifier.fillMaxSize(), // Fills the entire screen
        contentAlignment = Alignment.Center // Centers content within the box
    ) {
        // Conditional UI based on permission status
        if (!uiState.permissionGranted) {
            // Button to guide users to grant location permissions
            Button(
                onClick = {
                    // Cast the context to Activity to start an intent
                    val activity = context as? Activity
                    activity?.let {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            // Create a URI pointing to the app's settings page
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        it.startActivity(intent) // Open settings
                    } ?: run {
                        // Log a debug message if the context is null
                        MsLogger.d("PermissionDialog", "Unable to open settings; activity context is null.")
                    }
                },
                border = BorderStroke(
                    width = 1.dp, // Adds a 1dp border around the button
                    color = MaterialTheme.colorScheme.outline // Border color adapts to the theme
                )
            ) {
                Text(
                    text = "Grant Location Permission", // Button label
                    color = MaterialTheme.colorScheme.onPrimary // Text color adapts to the theme
                )
            }
        } else {
            // Text displayed when permission is granted but no data is available
            Text(
                text = "No weather data available.", // Message to inform the user
                style = MaterialTheme.typography.bodyMedium.copy( // Applies medium typography style
                    color = Color.LightGray // Sets a light gray color for the text
                ),
                textAlign = TextAlign.Center // Centers the text alignment
            )
        }
    }
}

/**
 * A customizable search bar composable for entering and submitting search queries.
 *
 * @param query The current text input in the search bar.
 * @param onQueryChange A lambda function triggered when the query text changes.
 * @param modifier Modifier for custom styling or layout of the search bar (optional).
 * @param placeholder The placeholder text shown when the search bar is empty (default is "Search...").
 * @param onSearch A lambda function triggered when the user presses the "Done" action or submits the query (optional).
 */
@Composable
fun SearchBar(
    query: String, // Current query in the text field
    onQueryChange: (String) -> Unit, // Lambda triggered on query text change
    modifier: Modifier = Modifier, // Modifier for styling or layout
    placeholder: String = "Search...", // Placeholder text shown when query is empty
    onSearch: () -> Unit = {} // Lambda triggered when search action occurs
) {
    // A horizontal row to arrange the search icon and text field
    Row(
        modifier = modifier
            .fillMaxWidth() // Makes the search bar occupy full width
            .height(56.dp) // Sets a fixed height
            .clip(RoundedCornerShape(16.dp)) // Applies rounded corners
            .background(Surface) // Sets the background color to `Surface`
            .padding(horizontal = 16.dp, vertical = 8.dp), // Adds padding inside the bar
        verticalAlignment = Alignment.CenterVertically // Centers the content vertically
    ) {
        // Icon for the search bar
        Icon(
            imageVector = Icons.Default.Search, // A built-in search icon
            tint = Color.White, // White color for the icon
            contentDescription = "Search", // Accessibility description for the icon
            modifier = Modifier.size(24.dp) // Sets the size of the icon
        )

        Spacer(modifier = Modifier.width(10.dp)) // Adds space between the icon and text field

        // The text input field for entering search queries
        BasicTextField(
            modifier = Modifier.fillMaxWidth(), // Makes the text field occupy remaining width
            value = query, // Binds the current text input
            onValueChange = { newQuery ->
                MsLogger.d("SearchBar", "Query changed: $newQuery") // Logs the updated query
                onQueryChange(newQuery) // Passes the updated query to the parent
            },
            singleLine = true, // Ensures the text field is single-line
            cursorBrush = SolidColor(Background), // Sets the cursor color
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = RubikFontRegular, // Specifies the font family
                fontSize = 14.sp, // Sets font size
                color = Color.White.copy(alpha = 0.5f) // Semi-transparent white text
            ),
            decorationBox = { innerTextField ->
                // Placeholder logic
                if (query.isEmpty()) {
                    Text(
                        text = placeholder, // Placeholder text
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = RubikFontRegular, // Placeholder font
                            fontSize = 14.sp, // Placeholder font size
                            color = Color.White.copy(alpha = 0.3f) // Lighter placeholder color
                        )
                    )
                }
                innerTextField() // Displays the actual text field content
            },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done), // IME action set to "Done"
            keyboardActions = KeyboardActions(onDone = {
                MsLogger.d("SearchBar", "Search triggered") // Logs when "Done" action is pressed
                onSearch() // Triggers the search action
            })
        )
    }
}

/**
 * A composable function that represents a single district item in a list.
 * It displays an icon and the district name, with a click action.
 *
 * @param district The data model for the district, containing its name.
 * @param onClick A lambda function to handle click events on the item.
 * @param modifier The `Modifier` to style or layout the composable (optional).
 */
@Composable
fun DistrictItem(
    district: DistrictModel?, // Nullable district model containing the name of the district
    onClick: () -> Unit, // Lambda function triggered when the item is clicked
    modifier: Modifier = Modifier // Modifier for custom styling and layout adjustments
) {
    // Row to arrange the icon and text horizontally
    Row(
        modifier = modifier
            .fillMaxWidth() // Ensures the row spans the entire width
            .clickable { // Makes the row clickable
                MsLogger.d("DistrictItem", "Clicked on district: ${district?.name ?: "Unknown"}")
                onClick() // Triggers the provided click action
            }
            .padding(16.dp), // Adds padding around the row content
        verticalAlignment = Alignment.CenterVertically // Aligns content vertically at the center
    ) {
        // Icon representing the location
        Icon(
            imageVector = Icons.Default.LocationOn, // Built-in location icon
            contentDescription = "Location Icon", // Accessibility description for the icon
            tint = MaterialTheme.colorScheme.primary // Icon color adapts to the theme
        )

        // Spacer to add horizontal spacing between the icon and text
        Spacer(modifier = Modifier.width(8.dp))

        // Text to display the district name
        Text(
            text = district?.name ?: "--", // Displays the district name or "--" if null
            style = MaterialTheme.typography.bodyMedium, // Applies the medium body typography style
            color = Color.White // Sets the text color to white
        )
    }
}