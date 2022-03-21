package com.afterpay.android.view

import com.afterpay.android.Afterpay

class AfterpayMoreInfoOptions {
    internal var modalId: String? = null
    internal var modalLinkStyle: AfterpayModalLinkStyle = AfterpayModalLinkStyle.DEFAULT
    internal var modalTheme: AfterpayModalTheme = AfterpayModalTheme.DEFAULT
    internal var isCbtEnabled: Boolean = false

    /**
     * Set up options for the more info link in AfterpayPriceBreakdown
     *
     * @param modalId the filename of a modal hosted on Afterpay static
     */
    constructor(
        modalId: String,
        modalLinkStyle: AfterpayModalLinkStyle = AfterpayModalLinkStyle.DEFAULT
    ) {
        this.modalId = modalId
        this.modalLinkStyle = modalLinkStyle
    }

    /**
     * Set up options for the more info link in AfterpayPriceBreakdown
     *
     * **Notes:**
     * - Not all combinations of Locales and CBT are available.
     *
     * @param modalTheme the color theme used when displaying the modal
     * @param isCbtEnabled whether to show the Cross Border Trade details in the modal
     */
    constructor(
        modalTheme: AfterpayModalTheme = AfterpayModalTheme.MINT,
        isCbtEnabled: Boolean = false,
        modalLinkStyle: AfterpayModalLinkStyle = AfterpayModalLinkStyle.DEFAULT
    ) {
        this.modalTheme = modalTheme
        this.isCbtEnabled = isCbtEnabled
        this.modalLinkStyle = modalLinkStyle
    }

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
