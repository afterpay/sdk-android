package com.example.afterpay

object NavGraph {
    private var uniqueId = 1
        get() = field++

    val id = uniqueId

    object dest {
        val shopping = uniqueId
        val checkout = uniqueId
        val receipt = uniqueId
        val details_v3 = uniqueId
    }

    object action {
        val to_checkout = uniqueId
        val to_receipt = uniqueId
        val back_to_shopping = uniqueId
        val to_details_v3 = uniqueId
    }

    object args {
        const val total_cost = "total_cost"
        const val checkout_token = "checkout_token"
        const val result_data_v3 = "result_data_v3"
    }
}
