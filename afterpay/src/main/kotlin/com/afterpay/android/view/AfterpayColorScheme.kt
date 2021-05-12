package com.afterpay.android.view

import androidx.annotation.ColorRes
import com.afterpay.android.R

enum class AfterpayColorScheme(
    @ColorRes val foregroundColorResId: Int,
    @ColorRes val backgroundColorResId: Int
) {
    BLACK_ON_MINT(
        foregroundColorResId = R.color.afterpay_black,
        backgroundColorResId = R.color.afterpay_mint
    ),
    MINT_ON_BLACK(
        foregroundColorResId = R.color.afterpay_mint,
        backgroundColorResId = R.color.afterpay_black
    ),
    WHITE_ON_BLACK(
        foregroundColorResId = R.color.afterpay_white,
        backgroundColorResId = R.color.afterpay_black
    ),
    BLACK_ON_WHITE(
        foregroundColorResId = R.color.afterpay_black,
        backgroundColorResId = R.color.afterpay_white
    );

    internal companion object {

        @JvmField
        val DEFAULT = BLACK_ON_MINT
    }
}
