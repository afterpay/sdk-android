package com.afterpay.android.view

import com.afterpay.android.Afterpay

enum class AfterpayIntroText(val string: String) {
    EMPTY(""),
    MAKE_TITLE(Afterpay.strings.introMakeTitle),
    MAKE(Afterpay.strings.introMake),
    PAY_TITLE(Afterpay.strings.introPayTitle),
    PAY(Afterpay.strings.introPay),
    IN_TITLE(Afterpay.strings.introInTitle),
    IN(Afterpay.strings.introIn),
    OR_TITLE(Afterpay.strings.introOrTitle),
    OR(Afterpay.strings.introOr),
    PAY_IN_TITLE(Afterpay.strings.introPayInTitle),
    PAY_IN(Afterpay.strings.introPayIn);

    internal companion object {

        @JvmField
        val DEFAULT = OR
    }
}
