package com.example.afterpay.checkout

import com.afterpay.android.cashapp.AfterpayCashApp
import com.afterpay.android.cashapp.AfterpayCashAppHandler

class CashAppHandler(
    val onDidCommenceCheckout: () -> Unit,
    val onDidReceiveResponse: (AfterpayCashApp) -> Unit,
) : AfterpayCashAppHandler {
    private var onTokenLoaded: (Result<String>) -> Unit = {}

    override fun didCommenceCheckout(onTokenLoaded: (Result<String>) -> Unit) =
        onDidCommenceCheckout().also { this.onTokenLoaded = onTokenLoaded }

    fun provideTokenResult(tokenResult: Result<String>) = onTokenLoaded(tokenResult)
        .also { onTokenLoaded = {} }

    override fun didReceiveCashAppData(cashAppData: AfterpayCashApp) {
        onDidReceiveResponse(cashAppData)
    }
}
