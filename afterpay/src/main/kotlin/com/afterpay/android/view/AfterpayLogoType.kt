package com.afterpay.android.view

enum class AfterpayLogoType(val fontHeightMultiplier: Double) {
    BADGE(2.5),
    LOCKUP(1.0),
    NARROW_BADGE(1.4),
    ;

    internal companion object {

        @JvmField
        val DEFAULT = BADGE
    }
}
