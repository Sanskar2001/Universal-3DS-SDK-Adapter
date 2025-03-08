package io.hyperswitch.threedslibrary.customization

import androidx.annotation.Keep

@Keep
data class ToolbarCustomization(
    var backgroundColor: String = "#0585DD",
    var statusBarColor: String? = null,
    var headerText: String = "SECURE CHECKOUT",
    var textColor: String = "#FFFFFF",
    var buttonText: String = "Close",
    var useCloseIcon: Boolean = true,
    var iconBackgroundColor: String = "#FFFFFF",
    var textFontStyle: FontStyle = FontStyle.REGULAR,
    var textFontSize: Int = 18
)
