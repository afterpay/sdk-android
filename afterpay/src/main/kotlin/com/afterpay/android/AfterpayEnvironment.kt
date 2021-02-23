package com.afterpay.android

import java.util.Locale

enum class AfterpayEnvironment {
    SANDBOX, PRODUCTION;

    override fun toString(): String = name.toLowerCase(Locale.ROOT)
}
