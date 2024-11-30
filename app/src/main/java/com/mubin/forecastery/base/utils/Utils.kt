package com.mubin.forecastery.base.utils

import android.content.Context
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.mubin.forecastery.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

/**
 * Executes a given suspendable block of code (`body`) within a specific coroutine context
 * (either `Dispatchers.Main` or `Dispatchers.IO`), and handles exceptions gracefully by returning `null` if an exception occurs.
 *
 * @param T The return type of the suspendable block.
 * @param shouldUseMainScope Determines whether the block should be executed on the `Main` dispatcher. Defaults to `true`.
 * @param body The suspendable block of code to execute.
 * @return The result of the block if successful, or `null` if an exception is thrown.
 */
suspend inline fun <T> executeBodyOrReturnNullSuspended(
    shouldUseMainScope: Boolean = true,
    crossinline body: suspend CoroutineScope.() -> T
): T? {
    return withContext(if (shouldUseMainScope) Dispatchers.Main else Dispatchers.IO) {
        return@withContext try {
            body.invoke(this)
        } catch (e: Exception) {
            // Log and handle the exception; return null as a fallback
            e.printStackTrace()
            null
        }
    }
}

/**
 * Creates an [ImageRequest] to load an image from the provided URL.
 *
 * This function constructs a Coil [ImageRequest] with the following configurations:
 * - The image source is the provided URL (`url`).
 * - It runs the image request on a background IO thread using the `Dispatchers.IO` dispatcher.
 * - Caching strategies are applied for both memory and disk caches:
 *   - The memory cache key and disk cache key are both set to the image URL (`url`).
 *   - Caching is enabled for both memory and disk caches.
 * - In case of errors or fallback scenarios, a default placeholder image (`ic_no_image_available`) is used.
 * - Crossfade animation is enabled when loading the image.
 *
 * @param context The context used to build the [ImageRequest].
 * @param url The URL of the image to be loaded.
 *
 * @return An [ImageRequest] that can be passed to Coil's image loading mechanisms.
 *
 * @see ImageRequest
 */
fun createImageRequest(context: Context, url: String?): ImageRequest {
    return ImageRequest.Builder(context)
        .data(data = url)
        .dispatcher(dispatcher = Dispatchers.IO)
        .memoryCacheKey(key = url)
        .diskCacheKey(key = url)
        .error(drawableResId = R.drawable.ic_no_image_available)
        .fallback(drawableResId = R.drawable.ic_no_image_available)
        .crossfade(enable = true)
        .diskCachePolicy(policy = CachePolicy.ENABLED)
        .memoryCachePolicy(policy = CachePolicy.ENABLED)
        .build()
}

// Composable Fonts

/**
 * Lazy initialization of the Rubik Bold font family.
 */
val RubikFontBold by lazy { FontFamily(Font(R.font.rubik_bold)) }

/**
 * Lazy initialization of the Rubik Medium font family.
 */
val RubikFontMedium by lazy { FontFamily(Font(R.font.rubik_medium)) }

/**
 * Lazy initialization of the Rubik Regular font family.
 */
val RubikFontRegular by lazy { FontFamily(Font(R.font.rubik_regular)) }

/**
 * Lazy initialization of the Gilroy Medium font family.
 */
val GilroyFontMedium by lazy { FontFamily(Font(R.font.gilroy_medium)) }

fun formatTimezoneOffset(offsetInSeconds: Int?): String {
    val offsetInHours = (offsetInSeconds ?: 0) / 3600  // Use 0 if offsetInSeconds is null
    val sign = if (offsetInHours >= 0) "+" else "-"
    return "GMT $sign${offsetInHours.absoluteValue}"
}

fun formatUnixTime(unixTime: Int?): String {
    return unixTime?.let {
        val date = Date(it.toLong() * 1000)
        val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
        format.format(date)
    } ?: "--"
}

/**
 * Dynamically calculates text size based on the length of the description.
 */
fun calculateTextSize(description: String, density: Density): Float {
    val baseSize = with(density) { 14.sp.toPx() }
    return if (description.length > 20) baseSize * 0.6f // Scale down for long text
    else baseSize
}

fun Modifier.shimmerEffect(translateAnim: Float, shimmerColorShades: List<Color>): Modifier {
    return this.drawWithCache {
        val gradient = Brush.linearGradient(
            colors = shimmerColorShades,
            start = Offset(x = translateAnim, y = 0f),
            end = Offset(x = translateAnim + 1000f, y = 0f)
        )
        onDrawBehind {
            drawRect(brush = gradient)
        }
    }
}