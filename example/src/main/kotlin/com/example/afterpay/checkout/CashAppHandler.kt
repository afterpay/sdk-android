package com.example.afterpay.checkout

import com.afterpay.android.cashapp.AfterpayCashAppHandler
import com.afterpay.android.cashapp.CashAppSignOrderResult

class CashAppHandler(
    val onDidReceiveResponse: (CashAppSignOrderResult) -> Unit,
) : AfterpayCashAppHandler {
    override fun didReceiveCashAppData(cashAppData: CashAppSignOrderResult) {
        onDidReceiveResponse(cashAppData)
    }
}
