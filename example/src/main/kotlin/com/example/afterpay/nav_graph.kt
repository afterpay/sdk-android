package com.example.afterpay

object nav_graph {
    private var uniqueId = 1
        get() = field++

    val id = uniqueId

    object dest {
        val shopping = uniqueId
        val checkout = uniqueId
        val success = uniqueId
    }

    object action {
        val to_checkout = uniqueId
        val to_success = uniqueId
        val back_to_shopping = uniqueId
    }

    object args {
        const val total_cost = "total_cost"
        const val checkout_token = "checkout_token"
    }
}
