package com.mubin.forecastery.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

/**
 * Represents the response received from the weather API.
 *
 * This class encapsulates various weather-related data such as coordinates, weather conditions, temperature details,
 * wind speed, visibility, and other metadata.
 *
 * @property coord The geographical coordinates of the location.
 * @property weather A list of weather conditions associated with the location.
 * @property base The base station information.
 * @property main The main weather data including temperature and pressure.
 * @property visibility The visibility in meters.
 * @property wind Wind-related data such as speed and direction.
 * @property clouds Cloud coverage information.
 * @property dt The time of data calculation (Unix timestamp).
 * @property sys System-related data such as country and sunrise/sunset times.
 * @property timezone The timezone offset in seconds from UTC.
 * @property id The unique identifier for the location.
 * @property name The name of the location.
 * @property cod The HTTP response code from the server.
 *
 */
@Keep
data class WeatherResponse(
    @SerializedName("coord")
    var coord: Coord?,
    @SerializedName("weather")
    var weather: List<Weather?>?,
    @SerializedName("base")
    var base: String?,
    @SerializedName("main")
    var main: Main?,
    @SerializedName("visibility")
    var visibility: Int?,
    @SerializedName("wind")
    var wind: Wind?,
    @SerializedName("clouds")
    var clouds: Clouds?,
    @SerializedName("dt")
    var dt: Int?,
    @SerializedName("sys")
    var sys: Sys?,
    @SerializedName("timezone")
    var timezone: Int?,
    @SerializedName("id")
    var id: Int?,
    @SerializedName("name")
    var name: String?,
    @SerializedName("cod")
    var cod: Int?
) {
    /**
     * Represents geographical coordinates of a location.
     *
     * @property lon The longitude of the location.
     * @property lat The latitude of the location.
     *
     */
    @Keep
    data class Coord(
        @SerializedName("lon")
        var lon: Double?,
        @SerializedName("lat")
        var lat: Double?
    )

    /**
     * Represents individual weather conditions.
     *
     * @property id The weather condition ID.
     * @property main The group of weather parameters (e.g., Rain, Snow).
     * @property description A detailed description of the weather condition.
     * @property icon The icon ID representing the weather.
     *
     */
    @Keep
    data class Weather(
        @SerializedName("id")
        var id: Int?,
        @SerializedName("main")
        var main: String?,
        @SerializedName("description")
        var description: String?,
        @SerializedName("icon")
        var icon: String?
    )

    /**
     * Represents main weather details such as temperature and humidity.
     *
     * @property temp The current temperature.
     * @property feelsLike The perceived temperature.
     * @property tempMin The minimum temperature.
     * @property tempMax The maximum temperature.
     * @property pressure The atmospheric pressure.
     * @property humidity The humidity percentage.
     * @property seaLevel The atmospheric pressure at sea level.
     * @property grndLevel The atmospheric pressure at ground level.
     *
     */
    @Keep
    data class Main(
        @SerializedName("temp")
        var temp: Double?,
        @SerializedName("feels_like")
        var feelsLike: Double?,
        @SerializedName("temp_min")
        var tempMin: Double?,
        @SerializedName("temp_max")
        var tempMax: Double?,
        @SerializedName("pressure")
        var pressure: Int?,
        @SerializedName("humidity")
        var humidity: Int?,
        @SerializedName("sea_level")
        var seaLevel: Int?,
        @SerializedName("grnd_level")
        var grndLevel: Int?
    )

    /**
     * Represents wind details such as speed and direction.
     *
     * @property speed The wind speed in meters/second.
     * @property deg The wind direction in degrees.
     *
     */
    @Keep
    data class Wind(
        @SerializedName("speed")
        var speed: Double?,
        @SerializedName("deg")
        var deg: Int?
    )

    /**
     * Represents cloud coverage information.
     *
     * @property all The percentage of cloud coverage.
     *
     */
    @Keep
    data class Clouds(
        @SerializedName("all")
        var all: Int?
    )

    /**
     * Represents system-related information.
     *
     * @property type The internal system parameter.
     * @property id The internal system ID.
     * @property country The country code.
     * @property sunrise The sunrise time (Unix timestamp).
     * @property sunset The sunset time (Unix timestamp).
     *
     */
    @Keep
    data class Sys(
        @SerializedName("type")
        var type: Int?,
        @SerializedName("id")
        var id: Int?,
        @SerializedName("country")
        var country: String?,
        @SerializedName("sunrise")
        var sunrise: Int?,
        @SerializedName("sunset")
        var sunset: Int?
    )
}