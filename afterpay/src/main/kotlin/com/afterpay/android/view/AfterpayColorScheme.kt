package com.afterpay.android.view

import com.afterpay.android.R

enum class AfterpayColorScheme {
    WHITE_ON_BLACK,
    BLACK_ON_WHITE;

    internal companion object {
        val DEFAULT: AfterpayColorScheme get() = WHITE_ON_BLACK
    }
}

internal val AfterpayColorScheme.badgeDrawable: Int
    get() = when (this) {
        AfterpayColorScheme.WHITE_ON_BLACK -> R.drawable.afterpay_badge_white_on_black
        AfterpayColorScheme.BLACK_ON_WHITE -> R.drawable.afterpay_badge_black_on_white
    }
