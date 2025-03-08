package io.hyperswitch.threedslibrary.threeDSSDKAdapters.Trident


import `in`.juspay.trident.customization.FontCustomization
import io.hyperswitch.threedslibrary.customization.LoaderCustomization
import io.hyperswitch.threedslibrary.customization.ButtonCustomization
import io.hyperswitch.threedslibrary.customization.CancelDialogCustomization
import io.hyperswitch.threedslibrary.customization.FontStyle
import io.hyperswitch.threedslibrary.customization.IFont
import io.hyperswitch.threedslibrary.customization.ToolbarCustomization
import io.hyperswitch.threedslibrary.customization.LabelCustomization
import io.hyperswitch.threedslibrary.customization.OTPSheetCustomization
import io.hyperswitch.threedslibrary.customization.TextBoxCustomization
import io.hyperswitch.threedslibrary.customization.TextCustomization
import io.hyperswitch.threedslibrary.customization.UiCustomization


object TridentUICustomization {

    fun UiCustomization.toTridentUiCustomization(): `in`.juspay.trident.customization.UiCustomization {

        val x = `in`.juspay.trident.customization.UiCustomization(
            submitButtonCustomization = this.submitButtonCustomization.toTridentButtonCustomization(),
            resendButtonCustomization = this.resendButtonCustomization.toTridentButtonCustomization(),
            toolbarCustomization = this.toolbarCustomization.toTridentToolbarCustomization(),
            labelCustomization = this.labelCustomization.toTridentLabelCustomization(),
            textBoxCustomization = this.textBoxCustomization.toTridentTextBoxCustomization(),
            loaderCustomization = this.loaderCustomization.toTridentLoaderCustomization(),
            fontCustomization = this.fontCustomization.toTridentFontCustomization(),
            otpSheetCustomization = this.otpSheetCustomization.toTridentOTPSheetCustomization(),
            cancelDialogCustomization = this.cancelDialogCustomization.toTridentCancelDialogCustomization(),
            showJpBrandingFooter = this.showJpBrandingFooter,
            screenHorizontalPadding = this.screenHorizontalPadding,
            screenVerticalPadding = this.screenVerticalPadding,
            showExpandableInfoTexts = this.showExpandableInfoTexts
        )
        return x

    }

    fun ButtonCustomization.toTridentButtonCustomization(): `in`.juspay.trident.customization.ButtonCustomization {
        return `in`.juspay.trident.customization.ButtonCustomization(
            textColor = this.textColor,
            backgroundColor = this.backgroundColor,
            fontStyle = this.fontStyle.toTridentFontStyle()
        )

    }

    fun ToolbarCustomization.toTridentToolbarCustomization(): `in`.juspay.trident.customization.ToolbarCustomization {
        return `in`.juspay.trident.customization.ToolbarCustomization(
            backgroundColor = this.backgroundColor,
            textColor = this.textColor,
            textFontStyle = this.textFontStyle.toTridentFontStyle()
        )
    }

    //

    fun getFontStyle(fontStyle: FontStyle): `in`.juspay.trident.customization.FontStyle {
        return when (fontStyle) {
            FontStyle.REGULAR -> `in`.juspay.trident.customization.FontStyle.REGULAR
            FontStyle.BOLD -> `in`.juspay.trident.customization.FontStyle.BOLD
            FontStyle.SEMI_BOLD -> `in`.juspay.trident.customization.FontStyle.SEMI_BOLD
        }

    }


    fun TextCustomization.toTridentTextCustomization(): `in`.juspay.trident.customization.TextCustomization {
        return `in`.juspay.trident.customization.TextCustomization(
            color = this.color,
            fontStyle = getFontStyle(this.fontStyle),
            fontSize = this.fontSize

        )
    }

    fun LabelCustomization.toTridentLabelCustomization(): `in`.juspay.trident.customization.LabelCustomization {


        return `in`.juspay.trident.customization.LabelCustomization(
            challengeLabel = this.challengeLabel.toTridentTextCustomization(),
            challengeContent = this.challengeContent.toTridentTextCustomization(),
            challengeHeader = this.challengeHeader.toTridentTextCustomization()
        )
    }


    //
    fun TextBoxCustomization.toTridentTextBoxCustomization(): `in`.juspay.trident.customization.TextBoxCustomization {

        return `in`.juspay.trident.customization.TextBoxCustomization(
            textColor = this.textColor,
            borderColor = this.borderColor,
            cornerRadius = this.cornerRadius,
            borderWidth = this.borderWidth
        )


    }

    //
    fun LoaderCustomization.toTridentLoaderCustomization(): `in`.juspay.trident.customization.LoaderCustomization {
        return `in`.juspay.trident.customization.LoaderCustomization(
            useProgressDialog = this.useProgressDialog,
            useProgressDialogInChallengeScreen = this.useProgressDialogInChallengeScreen
        )
    }

    //
    fun io.hyperswitch.threedslibrary.customization.FontCustomization.toTridentFontCustomization(): FontCustomization {


        return FontCustomization(
            regular = this.regular.toTridentIFont(),
            bold = this.bold.toTridentIFont(),
            semiBold = this.semiBold.toTridentIFont()
        )
    }


    fun IFont.toTridentIFont(): `in`.juspay.trident.customization.IFont {
        return when (this) {
            is IFont.SystemFont -> `in`.juspay.trident.customization.IFont.SystemFont
            is IFont.TypefaceFont -> `in`.juspay.trident.customization.IFont.TypefaceFont(this.typeface)
        }
    }

    fun OTPSheetCustomization.toTridentOTPSheetCustomization(): `in`.juspay.trident.customization.OTPSheetCustomization {

        return `in`.juspay.trident.customization.OTPSheetCustomization(
            headerText = this.headerText.toTridentTextCustomization(),
            subText = this.subText.toTridentTextCustomization(),
            otpText = this.otpText.toTridentTextCustomization(),
            otpHint = this.otpHint.toTridentTextCustomization(),
            resendActiveText = this.resendActiveText.toTridentTextCustomization(),
            stopSubmitText = this.stopSubmitText.toTridentTextCustomization(),
            resendTimerText = this.resendTimerText.toTridentTextCustomization(),
            snackBarText = this.snackBarText.toTridentTextCustomization(),
            submitButton = this.submitButton.toTridentButtonCustomization(),
            dimAmount = this.dimAmount

        )
    }

    //
    fun CancelDialogCustomization.toTridentCancelDialogCustomization(): `in`.juspay.trident.customization.CancelDialogCustomization {

        return `in`.juspay.trident.customization.CancelDialogCustomization(
            continueButtonCustomization = this.continueButtonCustomization.toTridentButtonCustomization(),
            exitButtonCustomization = this.exitButtonCustomization.toTridentButtonCustomization(),
            headerTextCustomization = this.headerTextCustomization.toTridentTextCustomization(),
            contentText = this.contentText,
            headerText = this.headerText,
            exitButtonText = this.exitButtonText,
            continueButtonText = this.continueButtonText,
            dimAmount = this.dimAmount
        )
    }

    fun FontStyle.toTridentFontStyle(): `in`.juspay.trident.customization.FontStyle {
        return when (this) {
            FontStyle.BOLD -> `in`.juspay.trident.customization.FontStyle.BOLD
            FontStyle.SEMI_BOLD -> `in`.juspay.trident.customization.FontStyle.SEMI_BOLD
            FontStyle.REGULAR -> `in`.juspay.trident.customization.FontStyle.REGULAR
        }

    }


}