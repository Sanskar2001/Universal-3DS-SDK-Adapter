package io.hyperswitch.threedslibrary.core

import android.app.Activity
import android.app.ProgressDialog
import androidx.annotation.Keep
import `in`.juspay.trident.data.AuthenticationRequestParameters
import `in`.juspay.trident.exception.InvalidInputException
import `in`.juspay.trident.exception.SDKRuntimeException
import io.hyperswitch.threedslibrary.data.ChallengeParameters
import io.hyperswitch.threedslibrary.data.ChallengeStatusReceiver


@Keep
interface Transaction<T> {

    @Throws(SDKRuntimeException::class)
    fun getAuthenticationRequestParameters(): io.hyperswitch.threedslibrary.data.AuthenticationRequestParameters

    @Throws(InvalidInputException::class, SDKRuntimeException::class)
    fun doChallenge(
        activity: Activity,
        challengeParameters: ChallengeParameters,
        challengeStatusReceiver: ChallengeStatusReceiver,
        timeOutInMinutes: Int,
        bankDetails: String?
    )

    @Suppress("DEPRECATION")
    fun getProgressView(
        activity: Activity
    ): ProgressDialog?

//    fun registerForLoaderEvents(handler: LoaderEventHandler)
//
//    fun unregisterLoaderEvents()

    fun close()
}

typealias LoaderEventHandler = (show: Boolean) -> Unit