package com.afterpay.android.cashapp

interface AfterpayCashAppHandler {
    fun didReceiveCashAppData(cashAppData: CashAppCreateOrderResult)
}
