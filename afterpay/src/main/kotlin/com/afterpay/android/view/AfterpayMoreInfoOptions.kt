/*
 * Copyright (C) 2024 Afterpay
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.afterpay.android.view

import com.afterpay.android.Afterpay
import com.afterpay.android.internal.Locales

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
        modalLinkStyle: AfterpayModalLinkStyle = AfterpayModalLinkStyle.DEFAULT,
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
        modalLinkStyle: AfterpayModalLinkStyle = AfterpayModalLinkStyle.DEFAULT,
    ) {
        this.modalTheme = modalTheme
        this.isCbtEnabled = isCbtEnabled
        this.modalLinkStyle = modalLinkStyle
    }

    internal fun modalFile(): String {
        modalId?.let {
            return "$it.html"
        }

        val languageLocale = Afterpay.language ?: Locales.EN_GB
        val locale = "${languageLocale.language}_${Afterpay.locale.country}"
        val cbt = if (isCbtEnabled) "-cbt" else ""
        val theme = modalTheme.slug

        return "$locale$theme$cbt.html"
    }
}
