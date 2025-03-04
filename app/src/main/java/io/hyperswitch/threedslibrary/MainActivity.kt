package io.hyperswitch.threedslibrary

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import `in`.juspay.trident.core.ConfigParameters
import `in`.juspay.trident.data.ChallengeStatusReceiver
import `in`.juspay.trident.data.CompletionEvent
import `in`.juspay.trident.data.ProtocolErrorEvent
import `in`.juspay.trident.data.RuntimeErrorEvent
import io.hyperswitch.threedslibrary.service.AuthenticationServiceType
import io.hyperswitch.threedslibrary.di.AuthenticationServiceProvider
import io.hyperswitch.threedslibrary.authenticationSDKs.TridentSDK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var clientSecret: String
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
            createPayment()
            AuthenticationServiceProvider.initialize<TridentSDK>(AuthenticationServiceType.TRIDENT, clientSecret,"pk_snd_23ff7c6d50e5424ba2e88415772380cd")
            val trident = AuthenticationServiceProvider.getService<TridentSDK>()
            trident.setClientSecret(clientSecret)
            trident.initialise(
                applicationContext, ConfigParameters(), "en-US",
                null
            )

            trident.doAuthentication(application, this, challengeStatusReceiver)
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