package com.example.afterpay.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.Date
import java.util.UUID

class Cart {
    private data class State(
        val lastUpdated: Date,
        val items: Map<UUID, Item>
    )

    data class Item(
        val product: Product,
        var quantity: Int
    )

    private val state = MutableStateFlow(State(items = mapOf(), lastUpdated = Date()))

    val items: Flow<List<Item>>
        get() = state.map { it.items.values.toList() }

    val totalCost: Double
        get() = state.value.items.values.sumByDouble { it.product.price * it.quantity }

    fun contains(product: Product): Boolean =
        state.value.items.containsKey(product.id)

    fun add(product: Product) {
        val items = state.value.items
        val item = items[product.id]?.apply { quantity += 1 } ?: Item(product, quantity = 1)
        state.value = State(
            items = items.plus(product.id to item),
            lastUpdated = Date()
        )
    }

    fun remove(product: Product) {
        val items = state.value.items
        val item = items[product.id]?.apply { quantity -= 1 } ?: return
        state.value = State(
            items = if (item.quantity > 0)
                items.plus(product.id to item)
            else
                items.minus(product.id),
            lastUpdated = Date()
        )
    }
}
