package com.afterpay.android.view

import com.afterpay.android.Afterpay

class AfterpayMoreInfoOptions {
    private var modalId: String? = null
    private var modalTheme: AfterpayModalTheme = AfterpayModalTheme.MINT
    private var isCbtEnabled: Boolean = false

    /**
     * Set up options for the more info link in AfterpayPriceBreakdown
     *
     * @param modalId the filename of a modal hosted on Afterpay static
     */
    constructor(modalId: String) {
        this.modalId = modalId
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
        isCbtEnabled: Boolean = false
    ) {
        this.modalTheme = modalTheme
        this.isCbtEnabled = isCbtEnabled
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
