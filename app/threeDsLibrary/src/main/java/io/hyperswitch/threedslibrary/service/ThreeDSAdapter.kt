package io.hyperswitch.threedslibrary.service

import android.app.Activity
import android.app.Application
import android.content.Context
import `in`.juspay.trident.exception.InvalidInputException
import `in`.juspay.trident.exception.SDKAlreadyInitializedException
import `in`.juspay.trident.exception.SDKNotInitializedException
import `in`.juspay.trident.exception.SDKRuntimeException

sealed class Result {
    data class Success(val message: String) : Result()
    data class Failure(val errorMessage: String) : Result()
}

/**
 * Interface defining the core functionalities required for integrating 3D Secure authentication.
 *
 * @param ConfigParameters The configuration parameters required to initialize the SDK.
 * @param UiCustomization The UI customization settings for the authentication flow.
 * @param Transaction The transaction object representing a 3DS session.
 * @param AuthenticationRequestParameters The parameters required for an authentication request.
 * @param ChallengeParameters The challenge parameters used during the challenge flow.
 * @param ChallengeStatusReceiver The callback interface to handle challenge responses.
 */
interface ThreeDSAdapter<
        ConfigParameters,
        UiCustomization,
        Transaction,
        AuthenticationRequestParameters,
        ChallengeParameters,
        ChallengeStatusReceiver
        > {

    /**
     * Initializes the 3D Secure SDK with the given configuration.
     *
     * @param context The application context used for initialization.
     * @param configParameters The configuration parameters required for the SDK.
     * @param locale The locale for language-specific customizations (optional).
     * @param uiCustomization The UI customization settings (optional).
     *
     * @throws InvalidInputException If the input parameters are invalid.
     * @throws SDKAlreadyInitializedException If the SDK is already initialized.
     * @throws SDKRuntimeException If an internal error occurs during initialization.
     */
    @Throws(
        InvalidInputException::class,
        SDKAlreadyInitializedException::class,
        SDKRuntimeException::class
    )
    fun initialise(
        context: Context,
        locale: String?,
        uiCustomization: UiCustomization?,
        initializationCallback: (Result) -> Unit
    )

    /**
     * Creates a new 3DS transaction with the given directory server details.
     *
     * @param directoryServerID The unique identifier of the directory server.
     * @param messageVersion The 3DS message version to be used for the transaction.
     * @return A `Transaction` object representing the initialized 3DS session.
     *
     * @throws InvalidInputException If the provided inputs are invalid.
     * @throws SDKNotInitializedException If the SDK has not been initialized before calling this function.
     * @throws SDKRuntimeException If an unexpected error occurs during transaction creation.
     */
    @Throws(
        InvalidInputException::class,
        SDKNotInitializedException::class,
        SDKRuntimeException::class
    )
    fun createTransaction(
        directoryServerID: String,
        messageVersion: String,
    ): Transaction

    /**
     * Retrieves the authentication request parameters required for the 3DS authentication process.
     *
     * @return `AuthenticationRequestParameters` containing all necessary authentication details.
     */
   


    /**
     * Extracts challenge parameters from an authentication request.
     *
     * @param aReq The authentication request parameters from the transaction.
     * @return `ChallengeParameters` required to proceed with the challenge flow.
     */
    fun getChallengeParameters(aReq: AuthenticationRequestParameters): ChallengeParameters

    /**
     * A helper function that automates the entire 3D Secure authentication process,
     * including transaction creation, authentication request, and challenge flow.
     *
     * @param applicationContext The application context.
     * @param activity The activity from which authentication is triggered.
     * @param challengeStatusReceiver The receiver to handle challenge responses.
     *
     * @throws InvalidInputException If the authentication request contains invalid inputs.
     * @throws SDKNotInitializedException If the SDK is not initialized before starting authentication.
     * @throws SDKRuntimeException If an error occurs during the authentication process.
     */
    @Throws(
        InvalidInputException::class,
        SDKNotInitializedException::class,
        SDKRuntimeException::class
    )
    fun startAuthentication(
        applicationContext: Application,
        activity: Activity,
        completionCallback: (Result) -> Unit
    )
}
