package io.hyperswitch.threedslibrary

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.IOException
import org.json.JSONObject

object Utils {
    public suspend fun authenticate(): String? = withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val json = JSONObject().apply {
            put("amount", 100)
            put("currency", "PLN")
            put("return_url", "https://google.com")
            put("payment_method", "card")
            put("payment_method_data", JSONObject().apply {
                put("card", JSONObject().apply {
                    put("card_number", "5267648608924299")
                    put("card_exp_month", "04")
                    put("card_exp_year", "2029")
                    put("card_holder_name", "John Smith")
                    put("card_cvc", "238")
                    put("card_network", "Visa")
                })
            })
            put("billing", JSONObject().apply {
                put("address", JSONObject().apply {
                    put("line1", "1467")
                    put("line2", "Harrison Street")
                    put("line3", "Harrison Street")
                    put("city", "San Fransico")
                    put("state", "CA")
                    put("zip", "94122")
                    put("country", "US")
                    put("first_name", "John")
                    put("last_name", "Doe")
                })
                put("phone", JSONObject().apply {
                    put("number", "8056594427")
                    put("country_code", "+91")
                })
            })
            put("browser_info", JSONObject().apply {
                put(
                    "user_agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.110 Safari/537.36"
                )
                put(
                    "accept_header",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8"
                )
                put("language", "nl-NL")
                put("color_depth", 24)
                put("screen_height", 723)
                put("screen_width", 1536)
                put("time_zone", 0)
                put("java_enabled", true)
                put("java_script_enabled", true)
                put("ip_address", "125.0.0.1")
            })
        }

        val requestBody =
            RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())
        val request = Request.Builder().url("https://auth.app.hyperswitch.io/api/authenticate")
            .addHeader("Content-Type", "application/json").addHeader("Accept", "application/json")
            .addHeader(
                "api-key", "snd_ve2oZRGcFnEZFdIDUxXjL5ruqj6fIVpPZxJza7pOoyG7trqeCUMbGoFt7gvGVTyx"
            ).post(requestBody).build()

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            response.body?.string()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}