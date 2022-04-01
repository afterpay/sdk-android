package com.afterpay.android.view

enum class AfterpayLogoType(val fontHeightMultiplier: Double) {
    BADGE(2.5),
    LOCKUP(1.0);

    internal companion object {

        @JvmField
        val DEFAULT = AfterpayLogoType.BADGE
    }
}
