package com.example.afterpay.checkout

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.afterpay.android.Afterpay
import com.afterpay.android.AfterpayCheckoutV2Handler
import com.afterpay.android.model.ShippingAddress
import com.afterpay.android.model.ShippingOption
import com.afterpay.android.view.AfterpayPaymentButton
import com.example.afterpay.R
import com.example.afterpay.checkout.CheckoutViewModel.Command
import com.example.afterpay.getDependencies
import com.example.afterpay.nav_graph
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import java.math.BigDecimal

class CheckoutFragment : Fragment() {
    private companion object {
        const val CHECKOUT_WITH_AFTERPAY = 1234
    }

    private val viewModel by viewModels<CheckoutViewModel> {
        CheckoutViewModel.factory(
            totalCost = requireNotNull(arguments?.get(nav_graph.args.total_cost) as? BigDecimal),
            merchantApi = getDependencies().merchantApi,
            preferences = getDependencies().sharedPreferences
        )
    }

    private val checkoutHandler = CheckoutHandler(
        onDidCommenceCheckout = { viewModel.loadCheckout() },
        onShippingAddressDidChange = { viewModel.selectAddress(it) },
        onShippingOptionDidChange = { }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Afterpay.setCheckoutV2Handler(checkoutHandler)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_checkout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emailField = view.findViewById<EditText>(R.id.cart_editText_emailAddress)
        emailField.addTextChangedListener { text ->
            viewModel.enterEmailAddress(email = text.toString())
        }

        val checkoutButton = view.findViewById<AfterpayPaymentButton>(R.id.cart_button_checkout)
        checkoutButton.setOnClickListener {
            val intent = Afterpay.createCheckoutIntent(requireContext())
            startActivityForResult(intent, CHECKOUT_WITH_AFTERPAY)
        }

        val totalCost = view.findViewById<TextView>(R.id.cart_totalCost)

        val expressRow = view.findViewById<View>(R.id.cart_expressRow)
        val expressCheckBox = view.findViewById<MaterialCheckBox>(R.id.cart_expressCheckBox)
        expressRow.setOnClickListener { expressCheckBox.toggle() }
        expressCheckBox.setOnCheckedChangeListener { _, checked -> viewModel.checkExpress(checked) }

        val buyNowRow = view.findViewById<View>(R.id.cart_buyNowRow)
        val buyNowCheckBox = view.findViewById<MaterialCheckBox>(R.id.cart_buyNowCheckBox)
        buyNowRow.setOnClickListener { buyNowCheckBox.toggle() }
        buyNowCheckBox.setOnCheckedChangeListener { _, checked -> viewModel.checkBuyNow(checked) }

        val pickupRow = view.findViewById<View>(R.id.cart_pickupRow)
        val pickupCheckBox = view.findViewById<MaterialCheckBox>(R.id.cart_pickupCheckBox)
        pickupRow.setOnClickListener { pickupCheckBox.toggle() }
        pickupCheckBox.setOnCheckedChangeListener { _, checked -> viewModel.checkPickup(checked) }

        lifecycleScope.launchWhenCreated {
            viewModel.state().collectLatest { state ->
                if (emailField.text.toString() != state.emailAddress) {
                    emailField.setText(state.emailAddress)
                }

                totalCost.text = state.totalCost
                expressCheckBox.isChecked = state.express
                buyNowCheckBox.isChecked = state.buyNow
                pickupCheckBox.isChecked = state.pickup
                checkoutButton.isEnabled = state.enableCheckoutButton
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.commands().collectLatest { command ->
                when (command) {
                    is Command.DisplayCheckout ->
                        checkoutHandler.urlLoaded(command.checkoutUrl)
                    is Command.DisplayError ->
                        checkoutHandler.errorLoadingUrl(command.checkoutError)
                    is Command.DisplayShippingOptions ->
                        checkoutHandler.provideShippingOptions(command.shippingOptions)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode to resultCode) {
            CHECKOUT_WITH_AFTERPAY to AppCompatActivity.RESULT_OK -> {
                val intent = checkNotNull(data) {
                    "Intent should always be populated by the SDK"
                }
                val token = checkNotNull(Afterpay.parseCheckoutSuccessResponse(intent)) {
                    "A token is always associated with a successful Afterpay transaction"
                }
                findNavController().navigate(
                    nav_graph.action.to_receipt,
                    bundleOf(nav_graph.args.checkout_token to token)
                )
            }
            CHECKOUT_WITH_AFTERPAY to AppCompatActivity.RESULT_CANCELED -> {
                val intent = requireNotNull(data) {
                    "Intent should always be populated by the SDK"
                }
                val status = checkNotNull(Afterpay.parseCheckoutCancellationResponse(intent)) {
                    "A cancelled Afterpay transaction always contains a status"
                }
                Snackbar.make(requireView(), "Cancelled: $status", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}

private class CheckoutHandler(
    val onDidCommenceCheckout: () -> Unit,
    val onShippingAddressDidChange: (ShippingAddress) -> Unit,
    val onShippingOptionDidChange: (ShippingOption) -> Unit
): AfterpayCheckoutV2Handler {
    private var onUrlLoaded: (Result<String>) -> Unit = {}

    override fun didCommenceCheckout(onUrlLoaded: (Result<String>) -> Unit) =
        onDidCommenceCheckout().also { this.onUrlLoaded = onUrlLoaded }

    fun urlLoaded(url: String) = onUrlLoaded(Result.success(url)).also { onUrlLoaded = {} }

    fun errorLoadingUrl(error: Throwable) =
        onUrlLoaded(Result.failure(error)).also { onUrlLoaded = {} }

    private var onProvideShippingOptions: (List<ShippingOption>) -> Unit = {}

    override fun shippingAddressDidChange(
        address: ShippingAddress,
        onProvideShippingOptions: (List<ShippingOption>) -> Unit
    ) = onShippingAddressDidChange(address).also {
        this.onProvideShippingOptions = onProvideShippingOptions
    }

    fun provideShippingOptions(shippingOptions: List<ShippingOption>) =
        onProvideShippingOptions(shippingOptions).also { onProvideShippingOptions = {} }

    override fun shippingOptionDidChange(shippingOption: ShippingOption) =
        onShippingOptionDidChange(shippingOption)
}
