package io.hyperswitch.threedslibrary

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import `in`.juspay.trident.core.ConfigParameters
import `in`.juspay.trident.data.ChallengeStatusReceiver
import `in`.juspay.trident.data.CompletionEvent
import `in`.juspay.trident.data.ProtocolErrorEvent
import `in`.juspay.trident.data.RuntimeErrorEvent
import io.hyperswitch.threedslibrary.di.ThreeDSFactory
import io.hyperswitch.threedslibrary.authenticationSDKs.TridentSDK
import io.hyperswitch.threedslibrary.di.ThreeDSSDKType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var clientSecret: String
    private lateinit var authenticateResponseJson: String

    private fun authenticate() {

        return runBlocking {
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
            val request = Request.Builder()
                .url("https://auth.app.hyperswitch.io/api/authenticate")
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader(
                    "api-key",
                    "snd_ehirpVANCGQDwG6fYvCC7dbCucH1ZtfzcxuhsOdHQgjYX46D1JpCYWki8TSU1SWg"
                )
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: okhttp3.Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")
                        authenticateResponseJson = response.body?.string().toString()
                        println(authenticateResponseJson)
                    }
                }
            })
        }
    }

    fun createPayment(): String? {
        return runBlocking { // Blocks execution until complete
            val baseUrl = "http://10.0.2.2:5252/create-payment-intent"
            withContext(Dispatchers.IO) {
                try {
                    val request = Request.Builder()
                        .url(baseUrl)
                        .get()
                        .build()

                    val client = OkHttpClient()
                    val response: Response = client.newCall(request).execute()

                    if (response.isSuccessful) {
                        response.body?.string()?.let { responseData ->
                            val jsonResponse = JSONObject(responseData)
                            val publishableKey = jsonResponse.getString("publishableKey")
                            clientSecret = jsonResponse.getString("clientSecret")

                            println("Publishable Key: $publishableKey")
                            println("Client Secret: $clientSecret")

                            return@withContext clientSecret // Return clientSecret after request
                        }
                    } else {
                        println("Error: ${response.code}")
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                null
            }
        }


    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btn = findViewById<Button>(R.id.myBtn)

        createPayment()
        authenticate()
        val challengeStatusReceiver = object : ChallengeStatusReceiver {
            override fun completed(completionEvent: CompletionEvent) {
                println("Completion Event: $completionEvent")
            }

            override fun cancelled() {
                println("Cancelled")

            }

            override fun timedout() {
                println("Timedout")

            }

            override fun protocolError(protocolErrorEvent: ProtocolErrorEvent) {
                println("Completion Event: $protocolErrorEvent")

            }

            override fun runtimeError(runtimeErrorEvent: RuntimeErrorEvent) {
                println("Completion Event: $runtimeErrorEvent")

            }

        }


        btn.setOnClickListener {
            authenticate()
            createPayment()
            try {
//                ThreeDSFactory.initialize<TridentSDK>(
//                    ThreeDSSDKType.TRIDENT,
//                    clientSecret,
//                    "pk_snd_23ff7c6d50e5424ba2e88415772380cd"
//                )
                ThreeDSFactory.initializeWithAuthResponse<TridentSDK>(
                   type= ThreeDSSDKType.TRIDENT,
                   authenticateResponseJson= authenticateResponseJson,
                   publishableKey =  "pk_snd_eccadfa3a89d4fa0a7a331f20b1dea23"
                )
                val trident = ThreeDSFactory.getService<TridentSDK>()
                trident.setClientSecret(clientSecret)
                trident.initialise(
                    applicationContext, ConfigParameters(), "en-US",
                    null
                )

                trident.startAuthentication(application, this, challengeStatusReceiver)
            } catch (exception: Exception) {
                println(exception.message)
            }
            /* FOR A GRANULAR CONTROL individual function can be called
            val dsId = trident.getMessageVersion()
            val messageVersion = trident.getDirectoryServerID()
            val transaction = trident.createTransaction(dsId, messageVersion)
            val aReq = transaction.getAuthenticationRequestParameters()
            val activity = this
            val challengeParameters = trident.getChallengeParameters(aReq)
            trident.doChallenge(activity, challengeParameters, challengeStatusReceiver, 0, "")
             */


        }


    }
}