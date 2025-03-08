package io.hyperswitch.threedslibrary.customization

import androidx.annotation.Keep

@Keep
data class LabelCustomization(
    var challengeHeader: TextCustomization = TextCustomization(
        color = "#323232",
        fontSize = 22,
        fontStyle = FontStyle.BOLD
    ),
    var challengeContent: TextCustomization = TextCustomization(
        color = "#444444",
        fontSize = 14,
        fontStyle = FontStyle.REGULAR
    ), // challengeInfoText
    var challengeLabel: TextCustomization = TextCustomization(
        color = "#323232",
        fontSize = 14,
        fontStyle = FontStyle.BOLD
    )
)
