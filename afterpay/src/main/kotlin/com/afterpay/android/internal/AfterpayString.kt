package com.afterpay.android.internal

import android.util.Log
import com.afterpay.android.internal.Locales.EN_AU
import com.afterpay.android.internal.Locales.EN_CA
import com.afterpay.android.internal.Locales.EN_GB
import com.afterpay.android.internal.Locales.EN_NZ
import com.afterpay.android.internal.Locales.EN_US
import com.afterpay.android.internal.Locales.ES_ES
import com.afterpay.android.internal.Locales.FR_FR
import com.afterpay.android.internal.Locales.IT_IT
import java.util.Locale

private val regionLanguages = mapOf(
    EN_AU.country to setOf(AfterpayString.EN),
    EN_GB.country to setOf(AfterpayString.EN),
    EN_NZ.country to setOf(AfterpayString.EN),
    EN_US.country to setOf(AfterpayString.EN),
    EN_CA.country to setOf(AfterpayString.EN, AfterpayString.FR_CA),
    FR_FR.country to setOf(AfterpayString.FR, AfterpayString.EN),
    IT_IT.country to setOf(AfterpayString.IT, AfterpayString.EN),
    ES_ES.country to setOf(AfterpayString.ES, AfterpayString.EN),
)

internal enum class AfterpayString (
    val language: String,
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
    val priceBreakdownLinkMoreInfo: String
) {
    EN(
        "en",
        "available for orders between %1\$s – %2\$s",
        "%1\$s available for orders between %2\$s – %3\$s",
        "Or",
        "or",
        "In",
        "in",
        "Pay",
        "pay",
        "Pay in",
        "pay in",
        "Make",
        "make",
        "Or pay with %1\$s",
        "or pay with",
        "Error",
        "Retry",
        "Cancel",
        "Failed to load %1\$s checkout",
        "Pay now with %1\$s",
        "%1\$s %2\$s %3\$spayments of %4\$s %5\$s",
        "%1\$s %2\$s %3\$spayments of %4\$s %5\$s%6\$s",
        "with ",
        "interest-free ",
        "Learn More",
        "More Info"
    ),
    FR_CA(
        "fr",
        "disponible pour les montants entre %1\$s – %2\$s",
        "%1\$s disponible pour les montants entre %2\$s – %3\$s",
        "Ou",
        "ou",
        "En",
        "en",
        "Payez",
        "payez",
        "Payez en",
        "payez en",
        "Effectuez",
        "effectuez",
        "Ou payer avec %1\$s",
        "ou payer avec",
        "Erreur",
        "Retenter",
        "Annuler",
        "Échec du chargement de la caisse %1\$s",
        "Payez maintenant avec %1\$s",
        "%1\$s %2\$s paiements %3\$sde %4\$s %5\$s",
        "%1\$s %2\$s paiements %3\$sde %4\$s %5\$s%6\$s",
        "avec ",
        "sans intérêts ",
        "En savoir plus",
        "Plus d'infos",
    ),
    FR(
        "fr",
        "disponible pour les montants entre %1\$s – %2\$s",
        "%1\$s disponible pour les montants entre %2\$s – %3\$s",
        "Ou",
        "ou",
        "En",
        "en",
        "Payez",
        "payez",
        "Payez en",
        "payez en",
        "Effectuez",
        "effectuez",
        "Ou payer avec %1\$s",
        "ou payer avec",
        "Erreur",
        "Réessayer",
        "Annuler",
        "Échec du chargement de la page de paiement 1%\$s",
        "Payez maintenant avec %1\$s",
        "%1\$s %2\$s paiements %3\$sde %4\$s %5\$s",
        "%1\$s %2\$s paiements %3\$sde %4\$s %5\$s%6\$s",
        "avec ",
        "sans frais ",
        "En savoir plus",
        "Plus d'infos",
    ),
    IT(
        "it",
        "disponibile per importi fra %1\$s – %2\$s",
        "%1\$s disponibile per importi fra %2\$s – %3\$s",
        "O",
        "o",
        "In",
        "in",
        "Paga",
        "paga",
        "Paga in",
        "paga in",
        "Scegli",
        "scegli",
        "O paga con %1\$s",
        "o paga con",
        "Errore",
        "Riprovare",
        "Annulla",
        "Impossibile caricare il cassa di %1\$s",
        "Paga ora con %1\$s",
        "%1\$s %2\$s rate %3\$sda %4\$s %5\$s",
        "%1\$s %2\$s rate %3\$sda %4\$s %5\$s%6\$s",
        "con ",
        "senza interessi ",
        "Scopri di piú",
        "Maggiori info",
    ),
    ES(
        "es",
        "disponible para importes entre %1\$s – %2\$s",
        "%1\$s disponible para importes entre %2\$s – %3\$s",
        "O",
        "o",
        "En",
        "en",
        "Paga",
        "paga",
        "Paga en",
        "paga en",
        "Haga",
        "haga",
        "O pagar con %1\$s",
        "o pagar con",
        "Error",
        "Volver a intentar",
        "Cancelar",
        "Imposible cargar la página de pago de %1\$s",
        "Pagar ahora con %1\$s",
        "%1\$s %2\$s pagos %3\$sde %4\$s %5\$s",
        "%1\$s %2\$s pagos %3\$sde %4\$s %5\$s%6\$s",
        "con ",
        "sin coste ",
        "Saber más",
        "Más infos",
    );

    companion object {
        fun forLocales(merchantLocale: Locale, clientLocale: Locale): AfterpayString {
            return regionLanguages[merchantLocale.country]?.find {
                clientLocale.language == it.language
            } ?: EN
        }
    }
}
