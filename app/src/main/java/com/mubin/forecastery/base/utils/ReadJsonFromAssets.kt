package com.mubin.forecastery.base.utils

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Utility function to read a JSON file from the assets folder.
 *
 * This function reads a JSON file from the assets folder and returns its contents as a string.
 * If any error occurs during the file reading process, an empty string is returned.
 *
 * @param context The application context.
 * @param path The relative path to the JSON file in the assets folder.
 * @return The contents of the JSON file as a String, or an empty string if an error occurs.
 *
 */
fun readJsonFromAssets(context: Context, path: String): String {
    val identifier = "ReadJSONFromAssets"

    // Log the start of the operation
    MsLogger.d(identifier, "Reading JSON from path: $path")

    try {
        // Open the JSON file from assets
        val file = context.assets.open(path)

        // BufferedReader to read the file content
        val bufferedReader = BufferedReader(InputStreamReader(file))
        val stringBuilder = StringBuilder()

        // Read the lines and append to StringBuilder
        bufferedReader.useLines { lines ->
            lines.forEach {
                stringBuilder.append(it)
            }
        }

        // Log the successful completion of reading the file
        MsLogger.d(identifier, "JSON reading successful")

        // Return the contents of the file as a string
        return stringBuilder.toString()
    } catch (e: Exception) {
        // Log the error if something goes wrong
        MsLogger.d(identifier, "Error occurred: ${e.localizedMessage}")
        e.printStackTrace()

        // Return empty string in case of an error
        return ""
    }
}