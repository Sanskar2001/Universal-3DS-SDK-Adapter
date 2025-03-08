package io.hyperswitch.threedslibrary.customization

import androidx.annotation.Keep

@Keep
data class LoaderCustomization(
    var useProgressDialog: Boolean = true,
    var useProgressDialogInChallengeScreen: Boolean = false
)
