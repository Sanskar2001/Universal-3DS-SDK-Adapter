package io.hyperswitch.threedslibrary.customization

import androidx.annotation.Keep

@Keep
data class TextCustomization(
    var color: String = "#444444",
    var fontStyle: FontStyle = FontStyle.REGULAR,
    var fontSize: Int = 14
)
