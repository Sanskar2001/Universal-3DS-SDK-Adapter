package io.hyperswitch.threedslibrary.di


import io.hyperswitch.threedslibrary.service.AuthenticationService
import io.hyperswitch.threedslibrary.service.AuthenticationServiceFactory
import io.hyperswitch.threedslibrary.service.AuthenticationServiceType

/**
 * Singleton wrapper for dependency injection of AuthenticationService.
 */
object AuthenticationServiceProvider {

    var authenticationService: AuthenticationService<*, *, *, *, *, *>? = null

    inline fun <reified T : AuthenticationService<*, *, *, *, *, *>> initialize(
        type: AuthenticationServiceType,
        clientSecret: String,
        publishableKey:String
    ) {
        if (authenticationService == null) {
            authenticationService =
                AuthenticationServiceFactory.createService<T>(type, clientSecret,publishableKey)
        }
       
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : AuthenticationService<*, *, *, *, *, *>> getService(): T {
        return authenticationService as? T
            ?: throw IllegalStateException("AuthenticationService not initialized. Call initialize() first.")
    }
}
