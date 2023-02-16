package com.afterpay.android.cashapp

interface AfterpayCashAppHandler {
    /**
     * didReceiveCashAppData is called when the order signing functions
     * complete (signCashAppOrder or signCashAppOrderAsync). [cashAppData] can
     * be used when constructing the request for createCustomerRequest in the
     * Cash App PayKit SDK
     */
    fun didReceiveCashAppData(cashAppData: CashAppCreateOrderResult)
}
