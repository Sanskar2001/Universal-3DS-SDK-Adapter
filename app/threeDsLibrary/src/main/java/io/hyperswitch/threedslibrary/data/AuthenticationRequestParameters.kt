package io.hyperswitch.threedslibrary.data

import androidx.annotation.Keep
@Keep
data class AuthenticationRequestParameters(
    val sdkTransactionID: String,
    val deviceData: String,
    val sdkEphemeralPublicKey: String,
    val sdkAppID: String,
    val sdkReferenceNumber: String,
    val messageVersion: String,
)
