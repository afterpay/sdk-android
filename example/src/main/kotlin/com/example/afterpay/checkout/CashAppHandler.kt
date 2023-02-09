package com.example.afterpay.checkout

import com.afterpay.android.cashapp.AfterpayCashAppHandler
import com.afterpay.android.cashapp.CashAppCreateOrderResult

class CashAppHandler(
    val onDidReceiveResponse: (CashAppCreateOrderResult) -> Unit,
) : AfterpayCashAppHandler {
    override fun didReceiveCashAppData(cashAppData: CashAppCreateOrderResult) {
        onDidReceiveResponse(cashAppData)
    }
}
