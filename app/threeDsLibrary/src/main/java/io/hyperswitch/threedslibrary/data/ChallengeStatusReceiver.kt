package io.hyperswitch.threedslibrary.data


data class ErrorMessage(
    val errorCode: String,
    val errorComponent: String,
    val errorDescription: String,
    val errorDetails: String? = null,
    val errorMessageType: String? = null,
    val messageVersionNumber: String
)


data class CompletionEvent(
    val sdkTransactionId: String,
    val transactionStatus: String
)

data class ProtocolErrorEvent(
    val sdkTransactionID: String,
    val errorMessage: ErrorMessage
)

data class RuntimeErrorEvent(
    val errorCode: String,
    val errorMessage: String
)


@androidx.annotation.Keep
public interface ChallengeStatusReceiver {
    public abstract fun cancelled()
    public abstract fun completed(completionEvent: CompletionEvent)
    public abstract fun protocolError(protocolErrorEvent: ProtocolErrorEvent)
    public abstract fun runtimeError(runtimeErrorEvent: RuntimeErrorEvent)
    public abstract fun timedout()
}
