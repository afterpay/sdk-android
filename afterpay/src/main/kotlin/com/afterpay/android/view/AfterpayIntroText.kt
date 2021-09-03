package com.afterpay.android.view

import com.afterpay.android.R

enum class AfterpayIntroText(val resourceID: Int) {
    EMPTY(R.string.afterpay_price_breakdown_intro_empty),
    MAKE_TITLE(R.string.afterpay_price_breakdown_intro_make_title),
    MAKE(R.string.afterpay_price_breakdown_intro_make),
    PAY_TITLE(R.string.afterpay_price_breakdown_intro_pay_title),
    PAY(R.string.afterpay_price_breakdown_intro_pay),
    IN_TITLE(R.string.afterpay_price_breakdown_intro_in_title),
    IN(R.string.afterpay_price_breakdown_intro_in),
    OR_TITLE(R.string.afterpay_price_breakdown_intro_or_title),
    OR(R.string.afterpay_price_breakdown_intro_or),
    PAY_IN_TITLE(R.string.afterpay_price_breakdown_intro_pay_in_title),
    PAY_IN(R.string.afterpay_price_breakdown_intro_pay_in);

    internal companion object {

        @JvmField
        val DEFAULT = AfterpayIntroText.OR
    }
}
