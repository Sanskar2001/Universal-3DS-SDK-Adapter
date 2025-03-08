package io.hyperswitch.threedslibrary.customization

import androidx.annotation.Keep

@Keep
data class UiCustomization(
    var submitButtonCustomization: ButtonCustomization = ButtonCustomization(),
    var resendButtonCustomization: ButtonCustomization = ButtonCustomization(
        textColor = "#0585DD",
        backgroundColor = "#FFFFFF",
        fontStyle = FontStyle.REGULAR
    ),
    var toolbarCustomization: ToolbarCustomization = ToolbarCustomization(),
    var labelCustomization: LabelCustomization = LabelCustomization(),
    var textBoxCustomization: TextBoxCustomization = TextBoxCustomization(),
    var loaderCustomization: LoaderCustomization = LoaderCustomization(),
    var fontCustomization: FontCustomization = FontCustomization(),
    var otpSheetCustomization: OTPSheetCustomization = OTPSheetCustomization(),
    var cancelDialogCustomization: CancelDialogCustomization = CancelDialogCustomization(),
    var showJpBrandingFooter: Boolean = false,
    var screenHorizontalPadding: Int = 16,
    var screenVerticalPadding: Int = 8,
    var showExpandableInfoTexts: Boolean = false
)
