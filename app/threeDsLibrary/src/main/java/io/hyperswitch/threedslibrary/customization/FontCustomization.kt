package io.hyperswitch.threedslibrary.customization

import androidx.annotation.Keep

@Keep
data class FontCustomization(
    var regular: IFont = IFont.SystemFont,
    var bold: IFont = IFont.SystemFont,
    var semiBold: IFont = IFont.SystemFont
)
