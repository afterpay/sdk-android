package com.afterpay.android.view

import com.afterpay.android.Afterpay
import java.lang.Exception

enum class AfterpayIntroText(val id: Int) {
    EMPTY(1),
    MAKE_TITLE(2),
    MAKE(3),
    PAY_TITLE(4),
    PAY(5),
    IN_TITLE(6),
    IN(7),
    OR_TITLE(8),
    OR(9),
    PAY_IN_TITLE(10),
    PAY_IN(11);

    internal companion object {

        @JvmField
        val DEFAULT = OR

        fun fromId(id: Int): String {
            return when (id) {
                EMPTY.id -> ""
                MAKE_TITLE.id -> Afterpay.strings.introMakeTitle
                MAKE.id -> Afterpay.strings.introMake
                PAY_TITLE.id -> Afterpay.strings.introPayTitle
                PAY.id -> Afterpay.strings.introPay
                IN_TITLE.id -> Afterpay.strings.introInTitle
                IN.id -> Afterpay.strings.introIn
                OR_TITLE.id -> Afterpay.strings.introOrTitle
                OR.id -> Afterpay.strings.introOr
                PAY_IN_TITLE.id -> Afterpay.strings.introPayInTitle
                PAY_IN.id -> Afterpay.strings.introPayIn
                else -> throw Exception("Invalid id `$id`, available ids are ${values().map { it.id }}")
            }
        }
    }
}
