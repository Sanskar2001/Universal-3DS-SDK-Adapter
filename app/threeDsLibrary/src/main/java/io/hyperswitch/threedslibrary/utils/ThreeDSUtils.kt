package io.hyperswitch.threedslibrary.utils

import android.util.Log
import `in`.juspay.trident.data.AuthenticationRequestParameters
import `in`.juspay.trident.data.ChallengeParameters
import io.hyperswitch.threedslibrary.data.AuthenticationData
import io.hyperswitch.threedslibrary.data.Constants
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
//            put("client_secret", clientSecret)
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

        return body.toString()
    }

    suspend fun hsAReq(
        clientSecret: String?,
        publishableKey: String?,
        aReq: AuthenticationRequestParameters
    ): ChallengeParameters? = withContext(Dispatchers.IO) {

        println("function called------")
        val paymentId = authenticationData!!.paymentId

        val authenticationUrl =
           Constants.getAuthenticateURL(paymentId)

        try {
            val jsonBody = clientSecret?.let { createAuthCallBody(it, aReq) }

            val requestBody =
                jsonBody.toString().toRequestBody("application/json".toMediaType())


            val client = OkHttpClient()
            val request = Request.Builder()
                .url(authenticationUrl)
                .post(requestBody)
                .addHeader(
                    "api-key",
                   "pk_snd_eccadfa3a89d4fa0a7a331f20b1dea23"
                )
                .addHeader("Content-Type", "application/json")
                .build()

            val response: Response = client.newCall(request).execute()

            if (response.isSuccessful) {
                response.body?.string()?.let { responseData ->
                    val json = JSONObject(responseData)
                    val transStatus = json.optString("trans_status", "")
                    val acsSignedContent = json.optString("acs_signed_content", "")
                    val acsRefNumber = json.optString("acs_reference_number", "")
                    val acsTransactionId = json.optString("acs_trans_id", "")
                    val threeDSServerTransId = json.optString("three_dsserver_trans_id", "")

                    authenticationData!!.transStatus = transStatus


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
            val paymentId = json.optString("payment_id", "")

            val authenticationData =
                AuthenticationData(
                    messageVersion,
                    directoryServerID,
                    authenticationUrl,
                    authorizeUrl,
                    null,
                    paymentId
                )
            return authenticationData

        } catch (e: Exception) {
            println("Error parsing JSON: ${e.localizedMessage}")
        }

        return null
    }

    fun retrievePayment(clientSecret: String, publishableKey: String): String? {
        return runBlocking {
            val paymentId = clientSecret.substringBefore("_secret_")
            val baseUrl =
              Constants.getRetriveURL(paymentId,clientSecret)
            withContext(Dispatchers.IO) {
                try {
                    val request = Request.Builder()
                        .url(baseUrl)
                        .get()
                        .addHeader(
                            "api-key",
                            publishableKey!!
                        )
                        .addHeader("Content-Type", "application/json")
                        .build()

                    val client = OkHttpClient()
                    val response: Response = client.newCall(request).execute()

                    if (response.isSuccessful) {
                        response.body?.string()?.let { responseData ->
                            val jsonResponse = JSONObject(responseData)
                            return@withContext responseData
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


    fun getAuthenticationData(publishableKey: String, clientSecret: String) {
        println("with publishable key and client secret")
        val responseData = retrievePayment(clientSecret, publishableKey)
        authenticationData = extract3DSData(responseData)
    }

    fun getAuthenticationData(authenticateResponseJson: String?) {
        println("with authenticate called....."+authenticateResponseJson)

        authenticationData = extract3DSData(authenticateResponseJson)
    }


}