package io.hyperswitch.threedslibrary.customization

import androidx.annotation.Keep

@Keep
data class OTPSheetCustomization(
    var headerText: TextCustomization = TextCustomization(
        color = "#000000",
        fontSize = 18,
        fontStyle = FontStyle.BOLD
    ),
    var subText: TextCustomization = TextCustomization(
        color = "#808080",
        fontSize = 14,
        fontStyle = FontStyle.REGULAR
    ),
    var otpText: TextCustomization = TextCustomization(
        color = "#000000",
        fontSize = 40,
        fontStyle = FontStyle.BOLD
    ),
    var otpHint: TextCustomization = TextCustomization(
        color = "#D8D8D8",
        fontSize = 13,
        fontStyle = FontStyle.REGULAR
    ),
    var resendInactiveText: TextCustomization = TextCustomization(
        color = "#808080",
        fontSize = 14,
        fontStyle = FontStyle.REGULAR
    ),
    var resendTimerText: TextCustomization = TextCustomization(
        color = "#000000",
        fontSize = 14,
        fontStyle = FontStyle.BOLD
    ),
    var stopSubmitText: TextCustomization = TextCustomization(
        color = "#8A8A8A",
        fontSize = 14,
        fontStyle = FontStyle.BOLD
    ),
    var resendActiveText: TextCustomization = TextCustomization(
        color = "#0099FF",
        fontStyle = FontStyle.BOLD,
        fontSize = 14
    ),
    var snackBarText: TextCustomization = TextCustomization(
        color = "#EAEAEA",
        fontStyle = FontStyle.REGULAR,
        fontSize = 12
    ),
    var submitButton: ButtonCustomization = ButtonCustomization(),
    var dimAmount: Float = 0.9f
)
