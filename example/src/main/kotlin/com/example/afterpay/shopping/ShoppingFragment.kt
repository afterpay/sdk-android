package com.example.afterpay.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.afterpay.android.view.AfterpayIntroText
import com.afterpay.android.view.AfterpayLogoType
import com.afterpay.android.view.AfterpayModalLinkStyle
import com.afterpay.android.view.AfterpayModalTheme
import com.afterpay.android.view.AfterpayMoreInfoOptions
import com.afterpay.android.view.AfterpayPriceBreakdown
import com.example.afterpay.NavGraph
import com.example.afterpay.R
import com.example.afterpay.data.Product
import com.example.afterpay.shopping.ShoppingViewModel.Command
import com.example.afterpay.shopping.ShoppingViewModel.ShoppingItem
import kotlinx.coroutines.flow.collectLatest

class ShoppingFragment : Fragment() {
    private val viewModel by viewModels<ShoppingViewModel> { ShoppingViewModel.factory() }

    private lateinit var recyclerView: RecyclerView
    private lateinit var checkoutButton: Button
    private lateinit var viewAdapter: ShoppingListAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.fragment_shopping, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewManager = LinearLayoutManager(requireContext())
        viewAdapter = ShoppingListAdapter(
            onAddProduct = viewModel::add,
            onRemoveProduct = viewModel::remove,
        )

        recyclerView = view.findViewById<RecyclerView>(R.id.shopping_recyclerView).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
            itemAnimator = DefaultItemAnimator().apply { supportsChangeAnimations = false }
        }

        checkoutButton = view.findViewById<Button>(R.id.shopping_button_viewCart).apply {
            setOnClickListener {
                viewModel.checkout()
            }
        }

        val totalCost = view.findViewById<TextView>(R.id.shopping_totalCost)
        val afterpayBreakdown = view.findViewById<AfterpayPriceBreakdown>(R.id.shopping_afterpayPriceBreakdown)
        afterpayBreakdown.introText = AfterpayIntroText.MAKE_TITLE
        afterpayBreakdown.logoType = AfterpayLogoType.NARROW_BADGE
        afterpayBreakdown.moreInfoOptions = AfterpayMoreInfoOptions(
            modalLinkStyle = AfterpayModalLinkStyle.CircledInfoIcon,
            modalTheme = AfterpayModalTheme.WHITE,
        )

        lifecycleScope.launchWhenCreated {
            viewModel.state.collectLatest { state ->
                viewAdapter.submitList(state.shoppingItems)
                checkoutButton.isEnabled = state.enableCheckoutButton
                totalCost.text = state.totalCostFormatted
                afterpayBreakdown.totalAmount = state.totalCost
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.commands.collectLatest { command ->
                when (command) {
                    is Command.Checkout ->
                        findNavController().navigate(
                            NavGraph.action.to_checkout,
                            bundleOf(NavGraph.args.total_cost to command.totalCost),
                        )
                }
            }
        }
    }
}

class ShoppingListAdapter(
    private val onAddProduct: (Product) -> Unit,
    private val onRemoveProduct: (Product) -> Unit,
) : ListAdapter<ShoppingItem, ShoppingListAdapter.ViewHolder>(itemDiff) {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.shoppingItem_title)
        val description: TextView = view.findViewById(R.id.shoppingItem_description)
        val price: TextView = view.findViewById(R.id.shoppingItem_price)
        val quantity: TextView = view.findViewById(R.id.shoppingItem_quantityInCart)
        val addButton: Button = view.findViewById(R.id.shoppingItem_button_addToCart)
        val removeButton: Button = view.findViewById(R.id.shoppingItem_button_removeFromCart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.view_shopping_item, parent, false),
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.name.text = item.name
        holder.description.text = item.description
        holder.price.text = item.price
        holder.quantity.text = item.quantity
        holder.addButton.setOnClickListener { onAddProduct(item.product) }
        holder.removeButton.setOnClickListener { onRemoveProduct(item.product) }
        holder.removeButton.isEnabled = item.isInCart
    }

    private companion object {
        val itemDiff = object : DiffUtil.ItemCallback<ShoppingItem>() {
            override fun areItemsTheSame(oldItem: ShoppingItem, newItem: ShoppingItem): Boolean =
                oldItem.product.id == newItem.product.id

            override fun areContentsTheSame(oldItem: ShoppingItem, newItem: ShoppingItem): Boolean =
                oldItem.quantityInCart == newItem.quantityInCart
        }
    }
}
