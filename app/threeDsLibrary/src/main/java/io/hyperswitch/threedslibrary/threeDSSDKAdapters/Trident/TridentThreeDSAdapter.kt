package io.hyperswitch.threedslibrary.authenticationSDKs

import android.app.Activity
import android.app.Application
import android.content.Context
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
import io.hyperswitch.threedslibrary.data.ErrorMessage
import io.hyperswitch.threedslibrary.service.Result
import io.hyperswitch.threedslibrary.service.ThreeDSAdapter
import io.hyperswitch.threedslibrary.threeDSSDKAdapters.Trident.TridentUICustomization
import io.hyperswitch.threedslibrary.threeDSSDKAdapters.Trident.TridentUICustomization.toTridentUiCustomization
import io.hyperswitch.threedslibrary.utils.ThreeDSUtils
import io.hyperswitch.threedslibrary.utils.ThreeDSUtils.authenticationData
import io.hyperswitch.threedslibrary.utils.ThreeDSUtils.getAuthenticationData
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.util.concurrent.CountDownLatch

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


class TridentSDK :
    ThreeDSAdapter<ConfigParameters, io.hyperswitch.threedslibrary.customization.UiCustomization, Transaction, AuthenticationRequestParameters, ChallengeParameters, ChallengeStatusReceiver> {
    private lateinit var threeDS2Service: ThreeDS2Service
    private var cardNetwork: String = "MASTERCARD"
    private lateinit var aReq: AuthenticationRequestParameters
    private lateinit var transaction: Transaction
    private var clientSecret: String? = null
    private var publishableKey: String? = null
    private var authenticateResponseJson: String? = null

    constructor(
        clientSecret: String? = null,
        publishableKey: String,
        authenticateResponseJson: String? = null
    ) {
        this.clientSecret = clientSecret
        this.publishableKey = publishableKey
        this.authenticateResponseJson = authenticateResponseJson
    }


    override fun initialise(
        context: Context,
        locale: String?,
        uiCustomization: io.hyperswitch.threedslibrary.customization.UiCustomization?,
        initializationCallback: (Result) -> Unit
    ) {
        try {

            threeDS2Service = ThreeDS2Service.createNewInstance(sdkHelper)
            threeDS2Service.initialise(
                context = context,
                ConfigParameters(),
                locale,
                uiCustomization?.toTridentUiCustomization()
            )

            println("initialise called--------->")
            if (authenticateResponseJson != null) {
                getAuthenticationData(authenticateResponseJson)
            } else {
                getAuthenticationData(publishableKey!!, clientSecret!!)
            }
            initializationCallback(Result.Success("SDK initialised successfully"))
        } catch (err: Error) {
            initializationCallback(Result.Failure("SDK initialised failed"))
        }
    }

    fun setAuthenticationResponse(authenticationResponse: String) {
        this.authenticateResponseJson = authenticationResponse
    }

    fun getMessageVersion(): String {
        return ThreeDSUtils.authenticationData!!.messageVersion
    }

    fun getDirectoryServerID(): String {
        return ThreeDSUtils.authenticationData!!.directoryServerId
    }


    override fun createTransaction(
        directoryServerID: String,
        messageVersion: String
    ): Transaction {
        val latch = CountDownLatch(1)
        var transactionResult: Transaction? = null


        // TODO: Add DS ID mapping for Trident
        val directoryServerId = ThreeDS2Service.getDirectoryServerId(cardNetwork)

        threeDS2Service.createTransaction(directoryServerId, getMessageVersion()) { transaction ->
            this.transaction = transaction
            transactionResult = transaction
            latch.countDown()
        }

        latch.await()

        return transactionResult ?: throw IllegalStateException("Transaction not created")
    }

    override fun getAuthenticationRequestParameters(): AuthenticationRequestParameters {
        aReq = transaction.getAuthenticationRequestParameters()
        return aReq
    }


    override fun getChallengeParameters(aReq: AuthenticationRequestParameters): ChallengeParameters {
        return runBlocking {
            ThreeDSUtils.hsAReq(publishableKey, aReq)
                ?: throw IllegalStateException("hsAReq returned null")
        }
    }

    override fun doChallenge(
        activity: Activity,
        challengeParameters: ChallengeParameters,
        challengeStatusReceiver: io.hyperswitch.threedslibrary.data.ChallengeStatusReceiver,
        timeOutInMinutes: Int,
        bankDetails: String?
    ) {


        val tridentChallengeStatusReceiver = object : ChallengeStatusReceiver {
            override fun completed(completionEvent: CompletionEvent) {

                val myCompletionEvent = io.hyperswitch.threedslibrary.data.CompletionEvent(
                    completionEvent.transactionStatus,
                    completionEvent.sdkTransactionId
                )
                challengeStatusReceiver.completed(myCompletionEvent)
            }

            override fun cancelled() {
                challengeStatusReceiver.cancelled()
            }

            override fun timedout() {
                challengeStatusReceiver.timedout()
            }

            override fun protocolError(protocolErrorEvent: ProtocolErrorEvent) {

                val tridentError = protocolErrorEvent.errorMessage

                val error = ErrorMessage(
                    tridentError.errorCode,
                    tridentError.errorComponent,
                    tridentError.errorDetails!!,
                    tridentError.errorDescription,
                    tridentError.errorMessageType,
                    tridentError.messageVersionNumber
                )

                val myProtocolErrorEvent = io.hyperswitch.threedslibrary.data.ProtocolErrorEvent(
                    protocolErrorEvent.sdkTransactionID,
                    error
                )
                challengeStatusReceiver.protocolError(myProtocolErrorEvent)
            }

            override fun runtimeError(runtimeErrorEvent: RuntimeErrorEvent) {
                val myRuntimeErrorEvent = io.hyperswitch.threedslibrary.data.RuntimeErrorEvent(
                    runtimeErrorEvent.errorCode,
                    runtimeErrorEvent.errorMessage
                )
                challengeStatusReceiver.runtimeError(myRuntimeErrorEvent)
            }
        }

        transaction?.doChallenge(
            activity,
            challengeParameters!!,
            tridentChallengeStatusReceiver,
            5,
            "{}"
        )
    }


    override fun startAuthentication(
        applicationContext: Application,
        activity: Activity,
        completionCallback: (Result) -> Unit
    ) {
        try {
            val dsId = getMessageVersion()
            val messageVersion = getDirectoryServerID()
            createTransaction(dsId, messageVersion)
            val aReq = getAuthenticationRequestParameters()
            val challengeParameters = getChallengeParameters(aReq)

            val challengeStatusReceiver =
                object : io.hyperswitch.threedslibrary.data.ChallengeStatusReceiver {


                    override fun cancelled() {

                        completionCallback(Result.Failure("Authentication was cancelled."))
                    }

                    override fun completed(completionEvent: io.hyperswitch.threedslibrary.data.CompletionEvent) {
                        completionCallback(Result.Success("Authentication successful! ${completionEvent.transactionStatus}"))
                    }

                    override fun protocolError(protocolErrorEvent: io.hyperswitch.threedslibrary.data.ProtocolErrorEvent) {
                        completionCallback(Result.Failure("Protocol error occurred during authentication. ${protocolErrorEvent.errorMessage}"))
                    }

                    override fun runtimeError(runtimeErrorEvent: io.hyperswitch.threedslibrary.data.RuntimeErrorEvent) {
                        completionCallback(Result.Failure("Runtime error occurred during authentication. ${runtimeErrorEvent.errorMessage}"))
                    }

                    override fun timedout() {

                        completionCallback(Result.Failure("Authentication timed out."))
                    }

                }

            if (authenticationData?.transStatus == "C") {
                doChallenge(activity, challengeParameters, challengeStatusReceiver, 5, "")
            } else {
                completionCallback(Result.Success("Authentication completed successfully without challenge!"))
            }
        } catch (e: Exception) {
            completionCallback(Result.Failure("Authentication failed: ${e.message}"))
        }
    }
}


