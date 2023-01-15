package com.afterpay.android.cashapp

interface AfterpayCashAppHandler {
    fun didCommenceCheckout(onTokenLoaded: (Result<String>) -> Unit)

    fun didReceiveCashAppData(cashAppData: AfterpayCashApp)
}
