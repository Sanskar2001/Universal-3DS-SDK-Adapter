package io.hyperswitch.threedslibrary.service

import io.hyperswitch.threedslibrary.authenticationSDKs.TridentSDK

enum class AuthenticationServiceType {
    TRIDENT,
    NETCETERA
}

object AuthenticationServiceFactory {

    fun <T> createService(type: AuthenticationServiceType, clientSecret: String):T {
        return when (type) {
            AuthenticationServiceType.TRIDENT -> TridentSDK(clientSecret) as T
            AuthenticationServiceType.NETCETERA -> throw UnsupportedOperationException("Netcetera SDK not implemented yet")
        }
    }
}
