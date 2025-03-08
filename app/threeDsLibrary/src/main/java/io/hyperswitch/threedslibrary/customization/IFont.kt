package io.hyperswitch.threedslibrary.customization

import android.graphics.Typeface
import androidx.annotation.Keep

@Keep
sealed interface IFont {
    @Keep
    data class TypefaceFont(val typeface: Typeface): IFont

    @Keep
    object SystemFont: IFont
}
