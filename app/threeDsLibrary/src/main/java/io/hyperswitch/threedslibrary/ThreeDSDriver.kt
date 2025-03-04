package io.hyperswitch.threedslibrary

import android.app.Activity
import android.app.Application
import android.util.Log
import `in`.juspay.trident.core.ConfigParameters
import `in`.juspay.trident.core.FileHelper
import `in`.juspay.trident.core.Logger
import `in`.juspay.trident.core.SdkHelper
import `in`.juspay.trident.core.ThreeDS2Service
import `in`.juspay.trident.core.Transaction
import `in`.juspay.trident.data.AuthenticationRequestParameters
import `in`.juspay.trident.data.ChallengeParameters
import `in`.juspay.trident.data.ChallengeStatusReceiver
import `in`.juspay.trident.data.CompletionEvent
import `in`.juspay.trident.data.ProtocolErrorEvent
import `in`.juspay.trident.data.RuntimeErrorEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject

class ThreeDSDriver {

    private var cardNetwork: String = "MASTERCARD"
    private var transaction: Transaction? = null
    private var clientSecret: String? = null
    private var aReq: AuthenticationRequestParameters? = null
    private lateinit var challengeParameters: ChallengeParameters

    val sdkHelper = object : SdkHelper {
        override val logger = object : Logger {
            override fun track(logLine: JSONObject) {
                println("Log: $logLine")
            }

            override fun addLogToPersistedQueue(logLine: JSONObject) {
                Log.wtf("FATAL:: SDK_CRASHED", logLine.toString())
            }

        }
        override val fileHelper = object : FileHelper {
            override fun renewFile(endpoint: String, fileName: String, startTime: Long) {

            }

            override fun readFromFile(fileName: String): String {
                return fileName
            }
        }
    }

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
                            val clientSecret = jsonResponse.getString("clientSecret")

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
                null // Return null if there's an error
            }
        }
    }


    fun extract3DSData(responseData: String) {
        try {
            val json = JSONObject(responseData)
            val nextAction = json.optJSONObject("next_action")

            val threeDSData = nextAction?.optJSONObject("three_ds_data")

            val threeDSAuthenticationURL = threeDSData?.optString("three_ds_authentication_url", "") ?: ""
            val threeDSAuthorizeURL = threeDSData?.optString("three_ds_authorize_url", "") ?: ""
            val messageVersion = threeDSData?.optString("message_version", "") ?: ""
            val directoryServerID = threeDSData?.optString("directory_server_id", "") ?: ""

            println("3DS Authentication URL: $threeDSAuthenticationURL")
            println("3DS Authorize URL: $threeDSAuthorizeURL")
            println("Message Version: $messageVersion")
            println("Directory Server ID: $directoryServerID")

        } catch (e: Exception) {
            println("Error parsing JSON: ${e.localizedMessage}")
        }
    }


    fun retrievePayment(): String? {
        return runBlocking { // Blocks execution until complete

            val paymentId = clientSecret?.substringBefore("_secret_")


            val baseUrl =
                "https://app.hyperswitch.io/api/payments/${paymentId}?client_secret=${clientSecret}&force_sync=true"
            withContext(Dispatchers.IO) {
                try {
                    val request = Request.Builder()
                        .url(baseUrl)
                        .get()
                        .addHeader(
                            "api-key",
                            "pk_snd_23ff7c6d50e5424ba2e88415772380cd"
                        ) // API Key in Header
                        .addHeader("Content-Type", "application/json")
                        .build()

                    val client = OkHttpClient()
                    val response: Response = client.newCall(request).execute()

                    if (response.isSuccessful) {
                        response.body?.string()?.let { responseData ->
                            val jsonResponse = JSONObject(responseData)
//                            val publishableKey = jsonResponse.getString("publishableKey")
//                            val clientSecret = jsonResponse.getString("clientSecret")

                            val json = JSONObject(responseData)
                          extract3DSData(responseData)


                            println("Retrieve Confirm=====: ${jsonResponse.toString()}")
                            return@withContext clientSecret // Return clientSecret after request
                        }
                    } else {
                        println("Error: ${response.code}")
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                null // Return null if there's an error
            }
        }
    }


    fun createAuthCallBody(
    ): String {
        val body = JSONObject().apply {
            put("client_secret", clientSecret)
            put("device_channel", "APP")
            put("threeds_method_comp_ind", "N")

            val sdkEphemeralPublicKey = aReq?.sdkEphemeralPublicKey

            val sdkInformation = JSONObject().apply {
                put("sdk_app_id", aReq?.sdkAppID)
                put("sdk_enc_data", aReq?.deviceData)
                put("sdk_ephem_pub_key", sdkEphemeralPublicKey?.let { JSONObject(it) })
                put("sdk_trans_id", aReq?.sdkTransactionID)
                put("sdk_reference_number", aReq?.sdkReferenceNumber)
                put("sdk_max_timeout", 10)
            }





            put("sdk_information", sdkInformation)
        }

        return body.toString() // Converts JSONObject to String (like JSON.stringify in JS)
    }

    //  https://beta.hyperswitch.io/api/payments/pay_cRic3x2V38MPZEET4ZZu/3ds/authentication

    fun hsAReq(activity: Activity) {
        val paymentId = clientSecret?.substringBefore("_secret_")
        val authenticationUrl =
            "https://app.hyperswitch.io/api/payments/${paymentId}/3ds/authentication"


        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create JSON Body
                val jsonBody = createAuthCallBody()

                // Create Request Body
                val requestBody =
                    jsonBody.toString().toRequestBody("application/json".toMediaType())

                val client = OkHttpClient()
                // Build the Request
                val request = Request.Builder()
                    .url(authenticationUrl)
                    .post(requestBody)
                    .addHeader(
                        "api-key",
                        "pk_snd_23ff7c6d50e5424ba2e88415772380cd"
                    ) // API Key in Header
                    .addHeader("Content-Type", "application/json")
                    .build()

                // Execute the Request
                val response: Response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    response.body?.string()?.let { responseData ->

                        val json = JSONObject(responseData)
                        val transStatus = json.optString("trans_status", "")
                        val acsSignedContent = json.optString("acs_signed_content", "")
                        val acsRefNumber = json.optString("acs_reference_number", "")
                        val acsTransactionId = json.optString("acs_trans_id", "")
                        val threeDSRequestorURL = json.optString(
                            "three_ds_requestor_url",
                            "https://example.com/3ds-requestor-url"
                        )
                        val threeDSServerTransId = json.optString("three_dsserver_trans_id", "")
                        println("Response: $transStatus")

                        challengeParameters = ChallengeParameters(
                            threeDSServerTransactionID = threeDSServerTransId,
                            acsTransactionID = acsTransactionId,
                            acsRefNumber = acsRefNumber,
                            acsSignedContent = acsSignedContent
                        )

                        println("challengeParameters"+challengeParameters.toString())

                        withContext(Dispatchers.Main)
                        {
                            transaction?.doChallenge(
                                activity,
                                challengeParameters!!,
                                challengeStatusReceiver,
                                5,
                                "{}"
                            )
                        }


                    }
                } else {
                    println("Error: ${response.toString()}")
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    fun func(applicationContext: Application, activity: Activity) {
        clientSecret = createPayment()
//        confirmCall()
        retrievePayment()
        println("flow here")
        val trident = ThreeDS2Service.createNewInstance(sdkHelper)
        trident.initialise(
            context = applicationContext,
            ConfigParameters(),
            "en-US",
            null
        )


        val directoryServerId = ThreeDS2Service.getDirectoryServerId(cardNetwork)
        trident.createTransaction(
            directoryServerId,
            "2.3.1"
        ) { transaction ->
            this.transaction = transaction
            aReq = transaction.getAuthenticationRequestParameters()
            Log.i("AReq---->", aReq.toString())
            hsAReq(activity)

        }

        /*
        *
        *   val sdkParams = resp
                    ?.optJSONObject("payment")
                    ?.optJSONObject("sdk_params")
                    ?.optJSONObject("threeDsAcsParams")

                txnUuid = resp?.optString("txn_uuid") ?: txnUuid

                sdkParams?.let {
                    challengeParameters = ChallengeParameters(
                        threeDSServerTransactionID = sdkParams.getString("threeDSServerTransactionID"),
                        acsTransactionID = sdkParams.getString("acsTransactionID"),
                        acsRefNumber = sdkParams.getString("acsRefNumber"),
                        acsSignedContent = sdkParams.getString("acsSignedContent")
                    )
        * */


    }
}