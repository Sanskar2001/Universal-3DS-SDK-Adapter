package io.hyperswitch.threedslibrary.data

data class AuthenticationData(
    val messageVersion: String,
    val directoryServerId: String,
    val authenticationUrl: String,
    val authorizeUrl:String,
    var transStatus:String?,
    val paymentId:String,
    val clientSecret:String?
)
