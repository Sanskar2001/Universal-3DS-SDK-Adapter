package io.hyperswitch.threedslibrary.utils

import `in`.juspay.trident.data.AuthenticationRequestParameters
import `in`.juspay.trident.data.ChallengeParameters
import io.hyperswitch.threedslibrary.data.AuthenticationData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject

object ThreeDSUtils {

    var authenticationData: AuthenticationData? = null
    fun createAuthCallBody(
        clientSecret: String,
        aReq: AuthenticationRequestParameters
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

    suspend fun hsAReq(
        clientSecret: String,
        aReq: AuthenticationRequestParameters
    ): ChallengeParameters? = withContext(Dispatchers.IO) {
        val paymentId = clientSecret.substringBefore("_secret_")

        val authenticationUrl = authenticationData!!.authenticationUrl

        try {
            // Create JSON Body
            val jsonBody = createAuthCallBody(clientSecret, aReq)

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
                )
                .addHeader("Content-Type", "application/json")
                .build()

            // Execute the Request (blocking call, but it's on IO thread)
            val response: Response = client.newCall(request).execute()

            if (response.isSuccessful) {
                response.body?.string()?.let { responseData ->
                    val json = JSONObject(responseData)
                    val transStatus = json.optString("trans_status", "")
                    val acsSignedContent = json.optString("acs_signed_content", "")
                    val acsRefNumber = json.optString("acs_reference_number", "")
                    val acsTransactionId = json.optString("acs_trans_id", "")
                    val threeDSServerTransId = json.optString("three_dsserver_trans_id", "")

                    println("Response: $transStatus")

                    // Build and return ChallengeParameters from the response
                    return@withContext ChallengeParameters(
                        threeDSServerTransactionID = threeDSServerTransId,
                        acsTransactionID = acsTransactionId,
                        acsRefNumber = acsRefNumber,
                        acsSignedContent = acsSignedContent
                    )
                }
            } else {
                println("Error: ${response}")
                null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun extract3DSData(responseData: String?): AuthenticationData? {
        try {
            val json = JSONObject(responseData)
            val nextAction = json.optJSONObject("next_action")

            val threeDSData = nextAction?.optJSONObject("three_ds_data")

            val authenticationUrl = threeDSData?.optString("three_ds_authentication_url", "") ?: ""
            val authorizeUrl = threeDSData?.optString("three_ds_authorize_url", "") ?: ""
            val messageVersion = threeDSData?.optString("message_version", "") ?: ""
            val directoryServerID = threeDSData?.optString("directory_server_id", "") ?: ""

            println("3DS Authentication URL: $authenticationUrl")
            println("3DS Authorize URL: $authorizeUrl")
            println("Message Version: $messageVersion")
            println("Directory Server ID: $directoryServerID")

            val authenticationData =
                AuthenticationData(
                    messageVersion,
                    directoryServerID,
                    authenticationUrl,
                    authorizeUrl
                )
            return authenticationData

        } catch (e: Exception) {
            println("Error parsing JSON: ${e.localizedMessage}")
        }

        return null
    }

    fun retrievePayment(clientSecret: String): String? {
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


                            println("Retrieve Confirm=====: ${jsonResponse.toString()}")
                            return@withContext responseData // Return clientSecret after request
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


    fun getAuthenticationData(clientSecret: String) {
        val responseData = retrievePayment(clientSecret)
        authenticationData = extract3DSData(responseData)
    }


}