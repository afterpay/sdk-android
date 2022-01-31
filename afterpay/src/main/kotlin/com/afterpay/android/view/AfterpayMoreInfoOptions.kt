package com.afterpay.android.view

import com.afterpay.android.Afterpay

/**
 * Setting up options for the more info link in AfterpayPriceBreakdown
 *
 * **Notes:**
 * - If both `modalId` is set, `modalTheme` and `isCbtEnabled` are ignored.
 * - Not all combinations of Locales and CBT are available.
 *
 * @param modalId the filename of a modal hosted on Afterpay static
 * @param modalTheme the color theme used when displaying the modal
 * @param isCbtEnabled whether to show the Cross Border Trade details in the modal
 */
class AfterpayMoreInfoOptions(
    var modalId: String? = null,
    var modalTheme: AfterpayModalTheme = AfterpayModalTheme.MINT,
    var isCbtEnabled: Boolean = false
) {
    internal fun modalFile(): String {
        modalId?.let {
            return "$it.html"
        }

        val locale = "${Afterpay.locale.language}_${Afterpay.locale.country}"
        val cbt = if (isCbtEnabled) "-cbt" else ""
        val theme = modalTheme.slug

        return "$locale$theme$cbt.html"
    }
}
