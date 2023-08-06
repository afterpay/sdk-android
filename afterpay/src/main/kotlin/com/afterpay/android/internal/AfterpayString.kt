package com.afterpay.android.internal

import com.afterpay.android.Afterpay
import com.afterpay.android.internal.Locales.EN_AU
import com.afterpay.android.internal.Locales.EN_CA
import com.afterpay.android.internal.Locales.EN_GB
import com.afterpay.android.internal.Locales.EN_NZ
import com.afterpay.android.internal.Locales.EN_US
import com.afterpay.android.internal.Locales.FR_CA

private val localeLanguages = mapOf(
    EN_AU to AfterpayString.EN,
    EN_GB to AfterpayString.EN,
    EN_NZ to AfterpayString.EN,
    EN_US to AfterpayString.EN,
    EN_CA to AfterpayString.EN,
    FR_CA to AfterpayString.FR_CA,
)

internal enum class AfterpayString(
    val breakdownLimit: String,
    val breakdownLimitDescription: String,

    val introOrTitle: String,
    val introOr: String,
    val introInTitle: String,
    val introIn: String,
    val introPayTitle: String,
    val introPay: String,
    val introPayInTitle: String,
    val introPayIn: String,
    val introMakeTitle: String,
    val introMake: String,

    val noConfigurationDescription: String,
    val noConfiguration: String,

    val loadErrorTitle: String,
    val loadErrorRetry: String,
    val loadErrorCancel: String,
    val loadErrorMessage: String,

    val paymentButtonContentDescription: String,

    val priceBreakdownAvailable: String,
    val priceBreakdownAvailableDescription: String,
    val priceBreakdownWith: String,
    val priceBreakdownInterestFree: String,
    val priceBreakdownLinkLearnMore: String,
    val priceBreakdownLinkMoreInfo: String,
) {
    EN(
        breakdownLimit = "available for orders between %1\$s – %2\$s",
        breakdownLimitDescription = "%1\$s available for orders between %2\$s – %3\$s",
        introOrTitle = "Or",
        introOr = "or",
        introInTitle = "In",
        introIn = "in",
        introPayTitle = "Pay",
        introPay = "pay",
        introPayInTitle = "Pay in",
        introPayIn = "pay in",
        introMakeTitle = "Make",
        introMake = "make",
        noConfigurationDescription = "Or pay with %1\$s",
        noConfiguration = "or pay with",
        loadErrorTitle = "Error",
        loadErrorRetry = "Retry",
        loadErrorCancel = "Cancel",
        loadErrorMessage = "Failed to load %1\$s checkout",
        paymentButtonContentDescription = "Pay now with %1\$s",
        priceBreakdownAvailable = "%1\$s %2\$s %3\$spayments of %4\$s %5\$s",
        priceBreakdownAvailableDescription = "%1\$s %2\$s %3\$spayments of %4\$s %5\$s%6\$s",
        priceBreakdownWith = "with ",
        priceBreakdownInterestFree = "interest-free ",
        priceBreakdownLinkLearnMore = "Learn More",
        priceBreakdownLinkMoreInfo = "More Info",
    ),
    FR_CA(
        breakdownLimit = "disponible pour les montants entre %1\$s – %2\$s",
        breakdownLimitDescription = "%1\$s disponible pour les montants entre %2\$s – %3\$s",
        introOrTitle = "Ou",
        introOr = "ou",
        introInTitle = "En",
        introIn = "en",
        introPayTitle = "Payez",
        introPay = "payez",
        introPayInTitle = "Payez en",
        introPayIn = "payez en",
        introMakeTitle = "Effectuez",
        introMake = "effectuez",
        noConfigurationDescription = "Ou payer avec %1\$s",
        noConfiguration = "ou payer avec",
        loadErrorTitle = "Erreur",
        loadErrorRetry = "Retenter",
        loadErrorCancel = "Annuler",
        loadErrorMessage = "Échec du chargement de la caisse %1\$s",
        paymentButtonContentDescription = "Payez maintenant avec %1\$s",
        priceBreakdownAvailable = "%1\$s %2\$s paiements %3\$sde %4\$s %5\$s",
        priceBreakdownAvailableDescription = "%1\$s %2\$s paiements %3\$sde %4\$s %5\$s%6\$s",
        priceBreakdownWith = "avec ",
        priceBreakdownInterestFree = "sans intérêts ",
        priceBreakdownLinkLearnMore = "En savoir plus",
        priceBreakdownLinkMoreInfo = "Plus d'infos",
    ),
    ;

    companion object {
        fun forLocale(): AfterpayString {
            return localeLanguages[Afterpay.language] ?: EN
        }
    }
}
