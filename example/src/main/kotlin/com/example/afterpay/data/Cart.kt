package com.example.afterpay.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.util.Date
import java.util.UUID

class Cart {
    private data class State(
        val lastUpdated: Date,
        val items: MutableMap<UUID, Item>
    )

    data class Summary(
        val items: List<Item>
    ) {
        val totalCost: BigDecimal
            get() = items.fold(0.toBigDecimal()) { acc, item -> acc + item.totalCost }

        fun quantityOf(product: Product): Int =
            items.firstOrNull { it.product == product }?.quantity ?: 0
    }

    data class Item(
        val product: Product,
        var quantity: Int
    ) {
        val totalCost: BigDecimal
            get() = product.price * quantity.toBigDecimal()
    }

    private val state = MutableStateFlow(State(items = mutableMapOf(), lastUpdated = Date()))

    val summary: Flow<Summary>
        get() = state.map { Summary(it.items.values.toList()) }

    fun contains(product: Product): Boolean =
        state.value.items.containsKey(product.id)

    fun add(product: Product) {
        val items = state.value.items
        val item = items[product.id]?.apply { quantity += 1 } ?: Item(product, quantity = 1)
        items[product.id] = item
        state.value = State(items = items, lastUpdated = Date())
    }

    fun remove(product: Product) {
        val items = state.value.items
        val item = items[product.id]?.apply { quantity -= 1 } ?: return
        if (item.quantity > 0) {
            items[product.id] = item
        } else {
            items.remove(product.id)
        }
        state.value = State(items = items, lastUpdated = Date())
    }
}
