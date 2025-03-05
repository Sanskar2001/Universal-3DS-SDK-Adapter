package io.hyperswitch.threedslibrary.service

import android.app.Activity
import android.app.Application
import android.content.Context

interface ThreeDSAdapter<
        ConfigParameters,
        UiCustomization,
        Transaction,
        AuthenticationRequestParameters,
        ChallengeParameters,
        ChallengeStatusReceiver
        > {
    fun initialise(
        context: Context,
        configParameters: ConfigParameters,
        locale: String?,
        uiCustomization: UiCustomization?
    )

    fun createTransaction(
        directoryServerID: String,
        messageVersion: String,
    ): Transaction

    fun getAuthenticationRequestParameters(): AuthenticationRequestParameters

    fun doChallenge(
        activity: Activity,
        challengeParameters: ChallengeParameters,
        challengeStatusReceiver: ChallengeStatusReceiver,
        timeOutInMinutes: Int,
        bankDetails: String?
    )

    fun getChallengeParameters(aReq: AuthenticationRequestParameters): ChallengeParameters


    fun startAuthentication(
        applicationContext: Application,
        activity: Activity,
        challengeStatusReceiver: ChallengeStatusReceiver
    )


}
