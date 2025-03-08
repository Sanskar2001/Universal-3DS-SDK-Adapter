package io.hyperswitch.threedslibrary.customization

import androidx.annotation.Keep

@Keep
data class CancelDialogCustomization(
    var continueButtonCustomization: ButtonCustomization = ButtonCustomization(),
    var exitButtonCustomization: ButtonCustomization = ButtonCustomization(
        textColor = "#797979",
        backgroundColor = "#FFFFFF",
        cornerRadius = 10.0
    ),
    var headerTextCustomization: TextCustomization = TextCustomization(
        color = "#44475B",
        fontSize = 20,
        fontStyle = FontStyle.BOLD
    ),
    var contentTextCustomization: TextCustomization = TextCustomization(
        color = "#44475B",
        fontSize = 16,
        fontStyle = FontStyle.REGULAR
    ),
    var headerText: String = "Are you sure you want to exit?",
    var contentText: String = "You are just one step away from completing the payment, exiting this will cancel your payment.",
    var exitButtonText: String = "Exit",
    var continueButtonText: String = "No, Continue Payment",
    var dimAmount: Float = 0.85f
)
