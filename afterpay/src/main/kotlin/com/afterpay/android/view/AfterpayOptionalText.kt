package com.afterpay.android.view

import com.afterpay.android.R

enum class AfterpayOptionalText(val textResourceID: Int, val descriptionResourceId: Int) {
    NONE(
        R.string.afterpay_price_breakdown_available_no_optional,
        R.string.afterpay_price_breakdown_available_no_optional_description
    ),
    INTEREST_FREE(
        R.string.afterpay_price_breakdown_available_interest,
        R.string.afterpay_price_breakdown_available_interest_description
    ),
    WITH(
        R.string.afterpay_price_breakdown_available_with,
        R.string.afterpay_price_breakdown_available_with_description
    ),
    INTEREST_FREE_AND_WITH(
        R.string.afterpay_price_breakdown_available_interest_and_with,
        R.string.afterpay_price_breakdown_available_interest_and_with_description
    );

    internal companion object {

        @JvmField
        val DEFAULT = AfterpayOptionalText.INTEREST_FREE_AND_WITH
    }
}
