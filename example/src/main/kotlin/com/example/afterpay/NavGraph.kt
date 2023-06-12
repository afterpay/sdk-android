package com.example.afterpay

object NavGraph {
    private var uniqueId = 1
        get() = field++

    val id = uniqueId

    object Dest {
        val shopping = uniqueId
        val checkout = uniqueId
        val receipt = uniqueId
        val cash_receipt = uniqueId
    }

    object Action {
        val to_checkout = uniqueId
        val to_receipt = uniqueId
        val to_cash_receipt = uniqueId
        val back_to_shopping = uniqueId
    }

    object Args {
        const val cash_response_data = "cash_response_data"
        const val total_cost = "total_cost"
        const val checkout_token = "checkout_token"
    }
}
