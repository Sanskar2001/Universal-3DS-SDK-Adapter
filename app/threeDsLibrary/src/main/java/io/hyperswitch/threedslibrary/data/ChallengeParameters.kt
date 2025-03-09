package io.hyperswitch.threedslibrary.data

import androidx.annotation.Keep

@Keep
data class ChallengeParameters(
    var threeDSServerTransactionID: String,
    var acsTransactionID: String,
    var acsRefNumber: String,
    var acsSignedContent: String,
    var threeDSRequestorAppURL: String?=null,
    var transStatus: String
)
