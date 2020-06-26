package com.example.afterpay

object nav_graph {
    const val id = 1

    object dest {
        const val shopping = 2
        const val checkout = 3
        const val success = 4
    }

    object action {
        const val to_checkout = 5
        const val to_success = 6
        const val back_to_shopping = 7
    }

    object args {
        const val total_cost = "total_cost"
        const val checkout_token = "checkout_token"
    }
}
