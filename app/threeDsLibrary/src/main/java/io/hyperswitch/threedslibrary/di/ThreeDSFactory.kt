package io.hyperswitch.threedslibrary.di


import io.hyperswitch.threedslibrary.authenticationSDKs.TridentSDK
import io.hyperswitch.threedslibrary.service.ThreeDSAdapter


enum class ThreeDSSDKType {
    TRIDENT,
    NETCETERA
}
/**
 * Singleton wrapper for dependency injection of  ThreeDSFactory.
 */
object ThreeDSFactory {

    var authenticationService: ThreeDSAdapter<*, *, *, *, *, *>? = null

    fun <T> createService(
        type: ThreeDSSDKType,
        clientSecret: String,
        publishableKey: String
    ): T {
        return when (type) {
            ThreeDSSDKType.TRIDENT -> TridentSDK(clientSecret, publishableKey) as T
            ThreeDSSDKType.NETCETERA -> throw UnsupportedOperationException("Netcetera SDK not implemented yet")
        }
    }

    inline fun <reified T : ThreeDSAdapter<*, *, *, *, *, *>> initialize(
        type: ThreeDSSDKType,
        clientSecret: String,
        publishableKey: String
    ) {
        if (authenticationService == null) {
            authenticationService =
                createService<T>(type, clientSecret, publishableKey)
        }

    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : ThreeDSAdapter<*, *, *, *, *, *>> getService(): T {
        return authenticationService as? T
            ?: throw IllegalStateException("AuthenticationService not initialized. Call initialize() first.")
    }
}
