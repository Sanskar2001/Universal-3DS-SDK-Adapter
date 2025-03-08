package io.hyperswitch.threedslibrary.customization

import androidx.annotation.Keep

@Keep
data class TextBoxCustomization(
    var useBoxedLayout: Boolean = true,
    var textColor: String = "#2A2B36",
    var textFontStyle: FontStyle = FontStyle.BOLD,
    var borderColor: String = "#BDBDBD",
    var focusedColor: String = "#282828",
    var borderWidth: Double = 1.0,
    var cornerRadius: Double = 5.0,
    var textFontSize: Int = 16,
    var hintTextColor: String = "#D8D8D8",
    var useNumericInputField: Boolean = true
)
