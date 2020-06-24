package com.example.afterpay.shopping

import androidx.lifecycle.ViewModel
import com.example.afterpay.data.Cart
import com.example.afterpay.data.Product
import com.example.afterpay.util.asCurrency
import com.example.afterpay.util.viewModelFactory
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import java.util.UUID

private val allProducts = listOf(
    Product(
        id = UUID.randomUUID(),
        name = "Coffee",
        description = "Ground 250g",
        price = 12.99
    ),
    Product(
        id = UUID.randomUUID(),
        name = "Milk",
        description = "Full Cream 2L",
        price = 3.49
    ),
    Product(
        id = UUID.randomUUID(),
        name = "Nestle Milo",
        description = "Malted Drinking Chocolate 460g",
        price = 7.00
    ),
    Product(
        id = UUID.randomUUID(),
        name = "Coca-cola",
        description = "Bottle 600ml",
        price = 3.75
    )
)

class ShoppingViewModel(val cart: Cart) : ViewModel() {
    data class ShoppingItem(val product: Product, private val quantityInCart: Int) {
        val name: String get() = product.name
        val description: String get() = product.description
        val price: String get() = product.price.asCurrency()
        val quantity: String get() = "$quantityInCart"
        val isInCart: Boolean get() = quantityInCart > 0
    }

    data class State(private val items: List<Cart.Item>) {
        val shoppingItems: List<ShoppingItem>
            get() = allProducts.map { product ->
                ShoppingItem(
                    product,
                    quantityInCart = items.firstOrNull { it.product == product }?.quantity ?: 0
                )
            }

        val totalCost: String
            get() = items.sumByDouble { it.product.price * it.quantity }.asCurrency()

        val enableCheckoutButton: Boolean
            get() = items.isNotEmpty()
    }

    sealed class Command {
        data class Checkout(val totalCost: Double) : Command()
    }

    private val commandChannel = Channel<Command>(Channel.CONFLATED)

    val state: Flow<State>
        get() = cart.items.map { State(it) }

    val commands: Flow<Command>
        get() = commandChannel.receiveAsFlow()

    fun add(product: Product) {
        cart.add(product)
    }

    fun remove(product: Product) {
        cart.remove(product)
    }

    fun checkout() {
        commandChannel.offer(Command.Checkout(totalCost = cart.totalCost))
    }

    companion object {
        fun factory() = viewModelFactory {
            ShoppingViewModel(cart = Cart())
        }
    }
}
