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
import `in`.juspay.trident.customization.UiCustomization
import `in`.juspay.trident.data.AuthenticationRequestParameters
import `in`.juspay.trident.data.ChallengeParameters
import `in`.juspay.trident.data.ChallengeStatusReceiver
import io.hyperswitch.threedslibrary.service.AuthenticationService
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

class TridentSDK(private var clientSecret: String,private val publishableKey:String) :
    AuthenticationService<ConfigParameters, UiCustomization, Transaction, AuthenticationRequestParameters, ChallengeParameters, ChallengeStatusReceiver> {
    private lateinit var threeDS2Service: ThreeDS2Service
    private var cardNetwork: String = "MASTERCARD"
    private lateinit var aReq: AuthenticationRequestParameters
    private lateinit var transaction: Transaction
    override fun initialise(
        context: Context,
        configParameters: ConfigParameters,
        locale: String?,
        uiCustomization: UiCustomization?
    ) {
        threeDS2Service = ThreeDS2Service.createNewInstance(sdkHelper)
        threeDS2Service.initialise(
            context = context,
            ConfigParameters(),
            locale,
            uiCustomization
        )
        getAuthenticationData(publishableKey,clientSecret)
    }

    fun setClientSecret(clientSecret: String)
    {
        this.clientSecret=clientSecret
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
            ThreeDSUtils.hsAReq(clientSecret,publishableKey, aReq)
                ?: throw IllegalStateException("hsAReq returned null")
        }
    }

    override fun doChallenge(
        activity: Activity,
        challengeParameters: ChallengeParameters,
        challengeStatusReceiver: ChallengeStatusReceiver,
        timeOutInMinutes: Int,
        bankDetails: String?
    ) {
        transaction?.doChallenge(
            activity,
            challengeParameters!!,
            challengeStatusReceiver,
            5,
            "{}"
        )
    }


    /**
     * A helper wrapper function to simplify the authentication process.
     * This method automates the challenge flow using 3DS authentication.
     *
     * @param applicationContext The application context.
     * @param activity The activity from which authentication is triggered.
     * @param challengeStatusReceiver The receiver to handle challenge responses.
     */
    override fun doAuthentication(
        applicationContext: Application,
        activity: Activity,
        challengeStatusReceiver: ChallengeStatusReceiver
    ) {
        val dsId = getMessageVersion()
        val messageVersion = getDirectoryServerID()
        createTransaction(dsId, messageVersion)
        val aReq = getAuthenticationRequestParameters()

        val challengeParameters = getChallengeParameters(aReq)
        doChallenge(activity, challengeParameters, challengeStatusReceiver, 5, "")

    }


}


