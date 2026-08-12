package com.example.mealfix.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** What a barcode lookup gives back — enough to prefill the "add a food item" form. */
data class ProductLookupResult(
    val name: String,
    val kcalPer100g: Double,
)

/**
 * A thin client for Open Food Facts' free, public product database
 * (https://world.openfoodfacts.org) — looks up a scanned barcode and returns the product's
 * name and energy per 100g, if that product and figure exist in their database. Coverage is
 * crowdsourced and skews toward Western packaged goods, so a miss here just means "enter it
 * manually," not that the barcode itself is invalid.
 */
object OpenFoodFactsApi {
    private const val USER_AGENT = "MealFix/1.0 (Android; +https://github.com/NWstevo/MealFix)"

    suspend fun lookup(barcode: String): ProductLookupResult? = withContext(Dispatchers.IO) {
        runCatching {
            val url = URL(
                "https://world.openfoodfacts.org/api/v2/product/$barcode.json?fields=product_name,nutriments",
            )
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", USER_AGENT)
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()
            parseProduct(body)
        }.getOrNull()
    }

    private fun parseProduct(body: String): ProductLookupResult? {
        val json = JSONObject(body)
        if (json.optInt("status") != 1) return null
        val product = json.optJSONObject("product") ?: return null
        val name = product.optString("product_name").ifBlank { return null }
        val nutriments = product.optJSONObject("nutriments") ?: return null
        if (!nutriments.has("energy-kcal_100g")) return null
        return ProductLookupResult(name = name, kcalPer100g = nutriments.optDouble("energy-kcal_100g"))
    }
}
