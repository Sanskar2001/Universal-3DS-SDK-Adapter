package io.hyperswitch.threedslibrary.customization

import androidx.annotation.Keep

@Keep
data class ButtonCustomization(
    var backgroundColor: String = "#0099FF",
    var textColor: String = "#FFFFFF",
    var cornerRadius: Double = 5.0,
    var fontSize: Int = 16,
    var fontStyle: FontStyle = FontStyle.BOLD,
    var marginTop: Int = 0,
    var showCapitalizedText: Boolean = true
)

