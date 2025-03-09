package io.hyperswitch.threedslibrary.threeDSSDKAdapters.Trident

import android.app.Activity
import android.app.ProgressDialog
import `in`.juspay.trident.data.CompletionEvent
import `in`.juspay.trident.data.ProtocolErrorEvent
import `in`.juspay.trident.data.RuntimeErrorEvent
import io.hyperswitch.threedslibrary.core.Transaction
import io.hyperswitch.threedslibrary.data.AuthenticationRequestParameters
import io.hyperswitch.threedslibrary.data.ChallengeParameters
import io.hyperswitch.threedslibrary.data.ChallengeStatusReceiver
import io.hyperswitch.threedslibrary.data.ErrorMessage

class TridentTransaction(val transaction: `in`.juspay.trident.core.Transaction) : Transaction<`in`.juspay.trident.core.Transaction> {


    override fun getAuthenticationRequestParameters(): AuthenticationRequestParameters {
        val aReqParams = transaction.getAuthenticationRequestParameters()
        return AuthenticationRequestParameters(
            sdkTransactionID = aReqParams.sdkTransactionID,
            deviceData = aReqParams.deviceData,
            sdkEphemeralPublicKey = aReqParams.sdkEphemeralPublicKey,
            sdkAppID = aReqParams.sdkAppID,
            sdkReferenceNumber = aReqParams.sdkReferenceNumber,
            messageVersion = aReqParams.messageVersion
        )
    }

    override fun doChallenge(
        activity: Activity,
        challengeParameters: ChallengeParameters,
        challengeStatusReceiver: ChallengeStatusReceiver,
        timeOutInMinutes: Int,
        bankDetails: String?
    ) {

        val tridentChallengeStatusReceiver = object :
            `in`.juspay.trident.data.ChallengeStatusReceiver {
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


        val tridentChallengeParameters = `in`.juspay.trident.data.ChallengeParameters(
            threeDSServerTransactionID = challengeParameters.threeDSServerTransactionID,
            acsTransactionID = challengeParameters.acsTransactionID,
            acsRefNumber = challengeParameters.acsRefNumber,
            acsSignedContent = challengeParameters.acsSignedContent
        )


        transaction.doChallenge(activity,tridentChallengeParameters,tridentChallengeStatusReceiver,timeOutInMinutes,bankDetails)
    }

    override fun getProgressView(activity: Activity): ProgressDialog? {
       return transaction.getProgressView(activity)
    }

    override fun close() {
      transaction.close()
    }
}
