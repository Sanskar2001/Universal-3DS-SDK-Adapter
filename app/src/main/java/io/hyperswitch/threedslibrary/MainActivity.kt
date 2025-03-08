package io.hyperswitch.threedslibrary

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.hyperswitch.threedslibrary.di.ThreeDSFactory
import io.hyperswitch.threedslibrary.authenticationSDKs.TridentSDK
import io.hyperswitch.threedslibrary.customization.ButtonCustomization
import io.hyperswitch.threedslibrary.customization.CancelDialogCustomization
import io.hyperswitch.threedslibrary.customization.FontCustomization
import io.hyperswitch.threedslibrary.customization.FontStyle
import io.hyperswitch.threedslibrary.customization.IFont
import io.hyperswitch.threedslibrary.customization.LabelCustomization
import io.hyperswitch.threedslibrary.customization.LoaderCustomization
import io.hyperswitch.threedslibrary.customization.OTPSheetCustomization
import io.hyperswitch.threedslibrary.customization.TextBoxCustomization
import io.hyperswitch.threedslibrary.customization.TextCustomization
import io.hyperswitch.threedslibrary.customization.ToolbarCustomization
import io.hyperswitch.threedslibrary.customization.UiCustomization
import io.hyperswitch.threedslibrary.data.ChallengeStatusReceiver
import io.hyperswitch.threedslibrary.data.CompletionEvent
import io.hyperswitch.threedslibrary.data.ProtocolErrorEvent
import io.hyperswitch.threedslibrary.data.RuntimeErrorEvent
import io.hyperswitch.threedslibrary.di.ThreeDSSDKType
import io.hyperswitch.threedslibrary.service.Result
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private fun getUiCustomization(): UiCustomization {
        val fontCustomization = FontCustomization(
            regular = getFont(applicationContext, "fonts/BasisGrotesquePro-Regular.ttf"),
            bold = getFont(applicationContext, "fonts/BasisGrotesquePro-Bold.ttf"),
            semiBold = getFont(applicationContext, "fonts/BasisGrotesquePro-SemiBold.ttf")
        )

        val toolbarCustomization = ToolbarCustomization(
            backgroundColor = "#FFEEE5",
            headerText = "Secure Checkout",
            textColor = "#2E2E2E",
            textFontStyle = FontStyle.BOLD,
            textFontSize = 16,
            iconBackgroundColor = "#2E2E2E",
            statusBarColor = "#333333"
        )

        val submitButtonCustomization = ButtonCustomization(
            backgroundColor = "#F15700",
            fontSize = 16,
            cornerRadius = 12.0
        )

        val resendButtonCustomization = ButtonCustomization(
            textColor = "#02060C",
            backgroundColor = "#FFFFFF",
            fontStyle = FontStyle.REGULAR
        )

        val labelCustomization = LabelCustomization(
            challengeHeader = TextCustomization(
                color = "#02060C",
                fontStyle = FontStyle.BOLD,
                fontSize = 16
            ),
            challengeContent = TextCustomization(
                color = "#02060C",
                fontSize = 14
            ),
        )

        val textBoxCustomization = TextBoxCustomization(
            textColor = "#02060C",
            borderColor = "#02060C",
            focusedColor = "#F15700",
            cornerRadius = 12.0
        )

        val otpSheetCustomization = OTPSheetCustomization(
            submitButton = ButtonCustomization(
                backgroundColor = "#F15700",
                fontSize = 16,
                cornerRadius = 12.0
            ),
            resendActiveText = TextCustomization(
                color = "#F15700",
                fontStyle = FontStyle.BOLD,
                fontSize = 14
            ),
            headerText = TextCustomization(
                fontSize = 24,
                color = "#333333",
                fontStyle = FontStyle.BOLD
            ),
            subText = TextCustomization(
                fontSize = 13,
                color = "#A1A1A1",
                fontStyle = FontStyle.REGULAR
            )
        )
        val cancelDialogCustomization = CancelDialogCustomization(
            continueButtonCustomization = ButtonCustomization(
                backgroundColor = "#F15700",
                fontSize = 16,
                cornerRadius = 12.0
            ),
            headerTextCustomization = TextCustomization(
                color = "#02060C",
                fontStyle = FontStyle.BOLD,
                fontSize = 16
            ),
            contentTextCustomization = TextCustomization(
                color = "#02060C",
                fontSize = 14
            )
        )
        val loaderCustomization = LoaderCustomization(
            useProgressDialogInChallengeScreen = false
        )

        return UiCustomization(
            fontCustomization = fontCustomization,
            toolbarCustomization = toolbarCustomization,
            submitButtonCustomization = submitButtonCustomization,
            resendButtonCustomization = resendButtonCustomization,
            labelCustomization = labelCustomization,
            textBoxCustomization = textBoxCustomization,
            otpSheetCustomization = otpSheetCustomization,
            cancelDialogCustomization = cancelDialogCustomization,
            loaderCustomization = loaderCustomization
        )
    }

    private fun getFont(context: Context, fontPath: String): IFont.TypefaceFont {
        return IFont.TypefaceFont(Typeface.createFromAsset(context.assets, fontPath))
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btn = findViewById<Button>(R.id.myBtn)


//        val challengeStatusReceiver = object : ChallengeStatusReceiver {
//            override fun completed(completionEvent: CompletionEvent) {
//                println("Completion Event: $completionEvent")
//            }
//
//            override fun cancelled() {
//                println("Cancelled")
//
//            }
//
//            override fun timedout() {
//                println("Timedout")
//
//            }
//
//            override fun protocolError(protocolErrorEvent: ProtocolErrorEvent) {
//                println("Completion Event: $protocolErrorEvent")
//
//            }
//
//            override fun runtimeError(runtimeErrorEvent: RuntimeErrorEvent) {
//                println("Completion Event: $runtimeErrorEvent")
//
//            }
//
//        }

        val activity = this
        btn.setOnClickListener {
            lifecycleScope.launch {

                val authenticateResponseJson = Utils.authenticate()!!
                try {
//                ThreeDSFactory.initialize<TridentSDK>(
//                    ThreeDSSDKType.TRIDENT,
//                    clientSecret,
//                    "pk_snd_23ff7c6d50e5424ba2e88415772380cd"
//                )
                    ThreeDSFactory.initializeWithAuthResponse<TridentSDK>(
                        type = ThreeDSSDKType.TRIDENT,
                        authenticateResponseJson = authenticateResponseJson,
                        publishableKey = "pk_snd_eccadfa3a89d4fa0a7a331f20b1dea23"
                    )
                    val trident = ThreeDSFactory.getService<TridentSDK>()
                    trident.setAuthenticationResponse(authenticateResponseJson)
                    trident.initialise(
                        applicationContext, "en-US", getUiCustomization()
                    ) { initializationResult ->
                        when (initializationResult) {
                            is Result.Success -> {
                                runOnUiThread {
                                    trident.startAuthentication(
                                        application,
                                        activity
                                    ) { authResult ->
                                        when (authResult) {
                                            is Result.Success -> {
                                                println("Success: ${authResult.message}")
                                            }

                                            is Result.Failure -> {
                                                println("Failure: ${authResult.errorMessage}")
                                            }
                                        }
                                    }
                                }
                            }

                            is Result.Failure -> {

                            }
                        }


                        /* FOR A GRANULAR CONTROL individual function can be called
                        val dsId = trident.getMessageVersion()
                        val messageVersion = trident.getDirectoryServerID()
                        val transaction = trident.createTransaction(dsId, messageVersion)
                        val aReq = transaction.getAuthenticationRequestParameters()
                        val activity = this
                        val challengeParameters = trident.getChallengeParameters(aReq)
                        trident.doChallenge(
                            activity,
                            challengeParameters,
                            challengeStatusReceiver,
                            0,
                            ""
                        )
                        */
                    }


                } catch (exception: Exception) {
                    println("my error" + exception.message)
                }

            }
        }
    }
}

