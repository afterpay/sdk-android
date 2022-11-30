package com.afterpay.android.internal

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.afterpay.android.R
import com.afterpay.android.internal.Locales.EN_AU
import com.afterpay.android.internal.Locales.EN_CA
import com.afterpay.android.internal.Locales.EN_GB
import com.afterpay.android.internal.Locales.EN_NZ
import com.afterpay.android.internal.Locales.EN_US
import com.afterpay.android.internal.Locales.ES_ES
import com.afterpay.android.internal.Locales.FR_CA
import com.afterpay.android.internal.Locales.FR_FR
import com.afterpay.android.internal.Locales.IT_IT
import java.util.Locale

private val brandLocales = mapOf(
    setOf(EN_AU, EN_CA, FR_CA, EN_NZ, EN_US) to Brand.AFTERPAY,
    setOf(EN_GB, IT_IT, FR_FR, ES_ES) to Brand.CLEARPAY,
)

internal enum class Brand(
    @StringRes val title: Int,
    @StringRes val description: Int,
    @DrawableRes val badgeForeground: Int,
    @DrawableRes val badgeForegroundCropped: Int,
    @DrawableRes val lockup: Int,
) {

    AFTERPAY(
        title = R.string.afterpay_service_name,
        description = R.string.afterpay_service_name_description,
        badgeForeground = R.drawable.afterpay_badge_fg,
        badgeForegroundCropped = R.drawable.afterpay_badge_fg_cropped,
        lockup = R.drawable.afterpay_lockup,
    ),

    CLEARPAY(
        title = R.string.clearpay_service_name,
        description = R.string.clearpay_service_name_description,
        badgeForeground = R.drawable.clearpay_badge_fg,
        badgeForegroundCropped = R.drawable.clearpay_badge_fg_cropped,
        lockup = R.drawable.clearpay_lockup,
    ),
    ;

    companion object {

        fun forLocale(locale: Locale): Brand =
            brandLocales.entries.find { locale in it.key }?.value ?: AFTERPAY
    }
}
