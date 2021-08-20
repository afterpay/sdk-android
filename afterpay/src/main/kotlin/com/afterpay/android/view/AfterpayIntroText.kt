package com.afterpay.android.view

import com.afterpay.android.R

enum class AfterpayIntroText(val text: Int) {
    NONE(R.string.afterpay_price_breakdown_string_empty),
    PAY(R.string.afterpay_price_breakdown_intro_pay),
    IN(R.string.afterpay_price_breakdown_intro_in),
    OR(R.string.afterpay_price_breakdown_intro_or),
    PAY_IN(R.string.afterpay_price_breakdown_intro_pay_in);

    internal companion object {

        @JvmField
        val DEFAULT = AfterpayIntroText.OR
    }
}
