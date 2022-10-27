package com.example.afterpay.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afterpay.data.Cart
import com.example.afterpay.data.Product
import com.example.afterpay.util.asCurrency
import com.example.afterpay.util.viewModelFactory
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.UUID

private val allProducts = listOf(
    Product(
        id = UUID.randomUUID(),
        name = "Coffee",
        description = "Ground 250g",
        price = BigDecimal(12.99),
    ),
    Product(
        id = UUID.randomUUID(),
        name = "Milk",
        description = "Full Cream 2L",
        price = BigDecimal(3.49),
    ),
    Product(
        id = UUID.randomUUID(),
        name = "Nestle Milo",
        description = "Malted Drinking Chocolate 460g",
        price = BigDecimal(7.00),
    ),
    Product(
        id = UUID.randomUUID(),
        name = "Coca-cola",
        description = "Bottle 600ml",
        price = BigDecimal(3.75),
    ),
)

class ShoppingViewModel(val cart: Cart) : ViewModel() {
    data class ShoppingItem(val product: Product, val quantityInCart: Int) {
        val name: String get() = product.name
        val description: String get() = product.description
        val price: String get() = product.price.asCurrency()
        val quantity: String get() = "$quantityInCart"
        val isInCart: Boolean get() = quantityInCart > 0
    }

    data class State(private val summary: Cart.Summary) {
        val shoppingItems: List<ShoppingItem>
            get() = allProducts.map { product ->
                ShoppingItem(product, quantityInCart = summary.quantityOf(product))
            }

        val totalCost: BigDecimal
            get() = summary.totalCost

        val totalCostFormatted: String
            get() = summary.totalCost.asCurrency()

        val enableCheckoutButton: Boolean
            get() = summary.items.isNotEmpty()
    }

    sealed class Command {
        data class Checkout(val totalCost: BigDecimal) : Command()
    }

    private val commandChannel = Channel<Command>(Channel.CONFLATED)

    val state: Flow<State>
        get() = cart.summary.map { State(it) }

    val commands: Flow<Command>
        get() = commandChannel.receiveAsFlow()

    fun add(product: Product) {
        cart.add(product)
    }

    fun remove(product: Product) {
        cart.remove(product)
    }

    fun checkout() {
        viewModelScope.launch {
            val summary = cart.summary.first()
            commandChannel.trySend(Command.Checkout(totalCost = summary.totalCost)).isSuccess
        }
    }

    companion object {
        fun factory() = viewModelFactory {
            ShoppingViewModel(cart = Cart())
        }
    }
}
