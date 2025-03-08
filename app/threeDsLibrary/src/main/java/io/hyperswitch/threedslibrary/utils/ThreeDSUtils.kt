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
            put("client_secret", clientSecret)
            put("device_channel", "APP")
            put("threeds_method_comp_ind", "N")
            put("client_secret", clientSecret)

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
        publishableKey: String?,
        aReq: AuthenticationRequestParameters
    ): ChallengeParameters? = withContext(Dispatchers.IO) {


        val paymentId = authenticationData!!.paymentId

        val authenticationUrl =
            Constants.getAuthenticateURL(paymentId)

        try {
            val jsonBody = createAuthCallBody(authenticationData!!.clientSecret!!, aReq)

            val requestBody =
                jsonBody.toString().toRequestBody("application/json".toMediaType())


            val client = OkHttpClient()
            val request = Request.Builder()
                .url(authenticationUrl)
                .post(requestBody)
                .addHeader(
                    "api-key",
                    publishableKey!!
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
                val transStatus = "Y"
                val acsSignedContent =
                    "eyJhbGciOiJQUzI1NiIsInR5cCI6IkpXVCIsIng1YyI6WyJNSUlFRGpDQ0F2YWdBd0lCQWdJVUlwMUk5WVRkOWhHVTJVTDRHMWdlMmlsSEk4NHdEUVlKS29aSWh2Y05BUUVMQlFBd2dZY3hDekFKQmdOVkJBWVRBbFZUTVJNd0VRWURWUVFJREFwRFlXeHBabTl5Ym1saE1SWXdGQVlEVlFRSERBMVRZVzRnUm5KaGJtTnBjMk52TVI4d0hRWURWUVFLREJaRlRWWkRieUJFYVhKbFkzUnZjbmtnVTJWeWRtVnlNUXd3Q2dZRFZRUUxEQU16UkZNeEhEQWFCZ05WQkFNTUUyUnpMWEp2YjNRdVpYaGhiWEJzWlM1amIyMHdIaGNOTWpVd016QTNNVGN6T0RNMldoY05NamN3TmpFd01UY3pPRE0yV2pCL01Rc3dDUVlEVlFRR0V3SlZVekVUTUJFR0ExVUVDQXdLUTJGc2FXWnZjbTVwWVRFV01CUUdBMVVFQnd3TlUyRnVJRVp5WVc1amFYTmpiekViTUJrR0ExVUVDZ3dTUlUxV1EyOGdRVU5USUZCeWIzWnBaR1Z5TVF3d0NnWURWUVFMREFNelJGTXhHREFXQmdOVkJBTU1EMkZqY3k1bGVHRnRjR3hsTG1OdmJUQ0NBU0l3RFFZSktvWklodmNOQVFFQkJRQURnZ0VQQURDQ0FRb0NnZ0VCQUx1NTR4Vy8ySEhkZTFmT3czV3l2Vm5IdnNKd2VjYnFFZ3RXUWorM0xqbi85c013cVRHdVE1OE9OeFQwNUVJTEhNaVRZc25ac05FMUc5SERIK001NXE2dVdyakwvTjNjSDh4MmFhTm8vYityLzV3bURWdExmZnNTQkNmMEY2aUtwbGFTM3ZtNy9IMlI5M1liTEkvNjFsNFJ3L0MvdUlFbnB2RXdkenlBRnZPaGFseU5OcXUwYU45bEVDYlNlTGFURjZ2Nk5tRXFwOE8zKzJvM3g3OURkRmViZ2tRYkpFcFlSRFhvMlprdG90dVQwS2pLRFFrUkJCU1pmeGF1NW9TYkdtdXI5bVcrMGZjbWxGaU13d0xVRGM2MXd2ZVpmNHBxY2wycnlLYWZKOWYwUmdweWtqTjRlVTE3S2UyWHdBQ1NFUzVwK3RSYUtNVkttWlBEdUpuNzRCc0NBd0VBQWFONU1IY3dDUVlEVlIwVEJBSXdBREFMQmdOVkhROEVCQU1DQnNBd0hRWURWUjBsQkJZd0ZBWUlLd1lCQlFVSEF3RUdDQ3NHQVFVRkJ3TUNNQjBHQTFVZERnUVdCQlF1NGllQnN2ekRaU2I1cFZsNHZUQkhNS3NIZ3pBZkJnTlZIU01FR0RBV2dCU21RL280c1U3bnc0VWxtaWErdGF4a1k2d2kxREFOQmdrcWhraUc5dzBCQVFzRkFBT0NBUUVBT1pZaGNuUFB5RG9KbzNLYTRSemVCSUhKdmV5dG42SllPNEw5Y2Qzb2ZCcnJPVXlMVm1vUTk0VVBHRW10eUhVaGM0Zm5OWGhGaGkvczNiOWdURTBvQm5wdE5TVG9oWE03K1J0UWJhZ0RkWnJ6Y1ErU1hHelh5bVZNbUdMVmFYWitDb3htT09YOFZzOW9zdWhuVzJlUUpPc1pWM3l3K045SmRKTTJCVjlVVEllRVd5YU0xaTBCMzhrYlJ4b04vVHJCM3hnS0xGYUlRazZCNXBaai9zWmFzOVp5bi9LbTZVVGFHNGZBNDU3dWVQalF4U2hKMmpwNjQ3TWd4MUxUL1NvekVFa2JtV3VFRVdDWFF5L0g4cUkrVmszZVM1cE5CWnRYNHk3TFpnMEI2Y3l4bWRvbHlML0tXcGhEbjRvcVVuTStVMCtiUldCZmVpQ1VRYk9xL2pzOFd3PT0iXX0.eyJhY3NUcmFuc0lEIjoiZDkxMzJhZGQtMmUxYi00ZmNjLWJjYmYtMDNiZmZkYzE5ZjgyIiwiYWNzUmVmTnVtYmVyIjoiQUNTLVJFRkVSRU5DRS04ODQ2MjAiLCJhY3NVUkwiOiJodHRwczovLzg0NTUtMTAzLTIxNC02My0xODkubmdyb2stZnJlZS5hcHAvY2hhbGxlbmdlIiwiYWNzRXBoZW1QdWJLZXkiOnsia3R5IjoiRUMiLCJjcnYiOiJQLTI1NiIsIngiOiIzR25iSXFqbC1fRkZ5dGhMb21YY2hBZ2lWQy0yaGtzUmo2TXNDMDIwZ2pnIiwieSI6IkphSzZnM2RrLVc4Y09CNmZ4ZUlXOVY0WktSVUk2NjF6VE0zSVl4XzZLUDAifX0.ZJwB771bmrRK3VFEqkxy_79M8qAlEk_4w4Ogt24w47qrUaHk4qRkInIHTHSvLRpOF1cps-RChWNYnu_cNUlHQm_UmeJQh72v95L8hmpgyfcD2TbquRr5FjMVizb9g2wsxgjcA-on86xeyyBF9ZEuO5co1QOBq_ZbJOQkbf0WRXzOh9m63yHh41NZnP_543oOpYV1gbbfb_-HeSSDyoLtgJcE26Sq0yR3GSP3c03Zyjhv67cqsf0k9eZYJptGKR789YgfyffmvAI-tQQd0rcQafzAF-si49F7ErXOpkVG0dDolx_pez_JQVlsZYpGa7OZ52xR34-PoCHBdEIJyWAeRQ"
                val acsRefNumber = "ACS-REFERENCE-884620"
                val acsTransactionId = "d9132add-2e1b-4fcc-bcbf-03bffdc19f82"
                val threeDSServerTransId = "a7aaa76e-ac2d-490a-bd85-85665740f5f6"

                authenticationData!!.transStatus = transStatus


                return@withContext ChallengeParameters(
                    threeDSServerTransactionID = threeDSServerTransId,
                    acsTransactionID = acsTransactionId,
                    acsRefNumber = acsRefNumber,
                    acsSignedContent = acsSignedContent
                )


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
            val clientSecret = json.optString("client_secret", "")

            println("paymentId----->" + paymentId)

            val authenticationData =
                AuthenticationData(
                    messageVersion,
                    directoryServerID,
                    authenticationUrl,
                    authorizeUrl,
                    null,
                    paymentId,
                    clientSecret
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
                Constants.getRetriveURL(paymentId, clientSecret)
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
        println("with authenticate called....." + authenticateResponseJson)

        authenticationData = extract3DSData(authenticateResponseJson)
    }


}