package com.example.afterpay.checkout

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import app.cash.paykit.core.CashAppPayKit
import app.cash.paykit.core.PayKitState
import app.cash.paykit.core.ui.CashPayKitButton
import com.afterpay.android.Afterpay
import com.afterpay.android.cashapp.CashAppCreateOrderResult
import com.afterpay.android.cashapp.CashAppValidationResponse
import com.afterpay.android.view.AfterpayPaymentButton
import com.example.afterpay.MainActivity
import com.example.afterpay.MainCommands
import com.example.afterpay.NavGraph
import com.example.afterpay.R
import com.example.afterpay.checkout.CheckoutViewModel.Command
import com.example.afterpay.data.CashData
import com.example.afterpay.getDependencies
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.math.BigDecimal

class CheckoutFragment : Fragment() {
    private companion object {
        const val CHECKOUT_WITH_AFTERPAY = 1234
    }

    private val viewModel by viewModels<CheckoutViewModel> {
        CheckoutViewModel.factory(
            totalCost = requireNotNull(arguments?.get(NavGraph.args.total_cost) as? BigDecimal),
            merchantApi = getDependencies().merchantApi,
            preferences = getDependencies().sharedPreferences,
        )
    }

    private lateinit var cashButton: CashPayKitButton

    private var payKitInstance: CashAppPayKit? = null
        get() {
            if (activity is MainActivity) {
                return (activity as MainActivity).payKit
            }

            return null
        }

    private var cashJwt: String? = null

    // when launching the checkout with V2, the token must be generated
    // with 'popupOriginUrl' set to 'https://static.afterpay.com' under the
    // top level 'merchant' object
    private val checkoutHandler = CheckoutHandler(
        onDidCommenceCheckout = { viewModel.loadCheckoutToken() },
        onShippingAddressDidChange = { viewModel.selectAddress(it) },
        onShippingOptionDidChange = { viewModel.selectShippingOption(it) },
    )

    private val cashAppHandler = CashAppHandler(
        onDidReceiveResponse = { createOrderResult ->
            when (createOrderResult) {
                is CashAppCreateOrderResult.Success -> {
                    val (response) = createOrderResult
                    cashJwt = response.jwt
                    viewModel.createCustomerRequest(response, payKitInstance)
                }
                is CashAppCreateOrderResult.Failure -> {
                    Snackbar.make(requireView(), "Error: ${createOrderResult.error.message}", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Afterpay.setCheckoutV2Handler(checkoutHandler)
        Afterpay.setCashAppHandler(cashAppHandler)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.fragment_checkout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emailField = view.findViewById<EditText>(R.id.cart_editText_emailAddress)
        emailField.addTextChangedListener { text ->
            viewModel.enterEmailAddress(email = text.toString())
        }

        val checkoutButton = view.findViewById<AfterpayPaymentButton>(R.id.cart_button_checkout)
        checkoutButton.setOnClickListener { viewModel.showAfterpayCheckout() }

        val totalCost = view.findViewById<TextView>(R.id.cart_totalCost)

        val expressRow = view.findViewById<View>(R.id.cart_expressRow)
        val expressCheckBox = view.findViewById<MaterialCheckBox>(R.id.cart_expressCheckBox)
        expressRow.setOnClickListener { expressCheckBox.toggle() }
        expressCheckBox.setOnCheckedChangeListener { _, checked -> viewModel.checkExpress(checked) }

        val versionRow = view.findViewById<View>(R.id.cart_v1Row)
        val versionCheckBox = view.findViewById<MaterialCheckBox>(R.id.cart_v1CheckBox)
        versionRow.setOnClickListener { versionCheckBox.toggle() }
        versionCheckBox.setOnCheckedChangeListener { _, checked -> viewModel.checkVersion(checked) }

        val buyNowRow = view.findViewById<View>(R.id.cart_buyNowRow)
        val buyNowCheckBox = view.findViewById<MaterialCheckBox>(R.id.cart_buyNowCheckBox)
        buyNowRow.setOnClickListener { buyNowCheckBox.toggle() }
        buyNowCheckBox.setOnCheckedChangeListener { _, checked -> viewModel.checkBuyNow(checked) }

        val pickupRow = view.findViewById<View>(R.id.cart_pickupRow)
        val pickupCheckBox = view.findViewById<MaterialCheckBox>(R.id.cart_pickupCheckBox)
        pickupRow.setOnClickListener { pickupCheckBox.toggle() }
        pickupCheckBox.setOnCheckedChangeListener { _, checked -> viewModel.checkPickup(checked) }

        val shippingOptionsRequiredRow = view.findViewById<View>(R.id.cart_shippingOptionsRequiredRow)
        val shippingOptionsRequiredCheckBox = view.findViewById<MaterialCheckBox>(R.id.cart_shippingOptionsRequiredCheckBox)
        shippingOptionsRequiredRow.setOnClickListener { pickupCheckBox.toggle() }
        shippingOptionsRequiredCheckBox.setOnCheckedChangeListener { _, checked ->
            viewModel.checkShippingOptionsRequired(checked)
        }

        lifecycleScope.launchWhenCreated {
            viewModel.state().collectLatest { state ->
                if (emailField.text.toString() != state.emailAddress) {
                    emailField.setText(state.emailAddress)
                }

                totalCost.text = state.totalCost
                versionCheckBox.isChecked = state.useV1
                expressCheckBox.isChecked = state.express
                buyNowCheckBox.isChecked = state.buyNow
                pickupCheckBox.isChecked = state.pickup
                shippingOptionsRequiredCheckBox.isChecked = state.shippingOptionsRequired
                checkoutButton.isEnabled = state.enableCheckoutButton
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.commands().collectLatest { command ->
                when (command) {
                    is Command.ShowAfterpayCheckoutV2 -> {
                        val intent = Afterpay.createCheckoutV2Intent(requireContext(), command.options)
                        startActivityForResult(intent, CHECKOUT_WITH_AFTERPAY)
                    }
                    is Command.ShowAfterpayCheckoutV1 -> {
                        val intent = Afterpay.createCheckoutIntent(requireContext(), command.checkoutUrl)
                        startActivityForResult(intent, CHECKOUT_WITH_AFTERPAY)
                    }
                    is Command.ProvideCheckoutTokenResult ->
                        checkoutHandler.provideTokenResult(command.tokenResult)
                    is Command.ProvideShippingOptionsResult ->
                        checkoutHandler.provideShippingOptionsResult(command.shippingOptionsResult)
                    is Command.ProvideShippingOptionUpdateResult ->
                        checkoutHandler.provideShippingOptionUpdateResult(
                            command.shippingOptionUpdateResult,
                        )
                    is Command.CreateCashAppOrder -> {
                        command.tokenResult
                            .onSuccess { Afterpay.createCashAppOrder(it) }
                            .onFailure {
                                Snackbar.make(requireView(), "Error: ${it.message}", Snackbar.LENGTH_SHORT).show()
                            }
                    }
                    is Command.LaunchCashAppPay -> viewModel.authorizePayKitCustomerRequest(requireContext(), payKitInstance)
                    is Command.CashReceipt -> {
                        cashJwt?.also { jwt ->
                            val customerResponseData = command.customerResponseData
                            val grant = customerResponseData.grants?.get(0)
                            val centsDivisor = 100

                            if (
                                cashJwt != null &&
                                grant?.id != null &&
                                customerResponseData.customerProfile?.id != null
                            ) {
                                Afterpay.validateCashPayment(
                                    jwt,
                                    grant.id,
                                    customerResponseData.customerProfile!!.id
                                ) { validationResult ->
                                    when (validationResult) {
                                        is CashAppValidationResponse.Success -> {
                                            val responseData = CashData(
                                                cashTag = customerResponseData.customerProfile?.cashTag,
                                                amount = (grant.action.amount_cents?.toBigDecimal()
                                                    ?.divide(centsDivisor.toBigDecimal())).toString(),
                                                grantId = grant.id,
                                            )

                                            findNavController().navigate(
                                                NavGraph.action.to_cash_receipt,
                                                bundleOf(NavGraph.args.cash_response_data to responseData),
                                            )
                                        }
                                        is CashAppValidationResponse.Failure -> {
                                            Snackbar.make(requireView(), "CashApp not valid: ${validationResult.error}", Snackbar.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        } ?: Snackbar.make(requireView(), "Something went wrong (missing jwt)", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            MainCommands.commands().collectLatest { command ->
                if (command is MainCommands.Command.PayKitStateChange) {
                    when (val state = command.state) {
                        is PayKitState.Approved -> viewModel.cashReceipt(customerResponseData = state.responseData)
                        is PayKitState.Declined -> Snackbar.make(requireView(), "CashApp Declined", Snackbar.LENGTH_SHORT).show()
                        is PayKitState.ReadyToAuthorize -> cashButton.isEnabled = true
                        is PayKitState.PayKitException -> Log.e("CheckoutFragment", "${state.exception}")
                        else -> Log.d("CheckoutFragment", "Pay Kit State: ${command.state}")
                    }
                }
            }
        }

        view.let {
            cashButton = it.findViewById(R.id.cart_button_cash)
            cashButton.setOnClickListener { viewModel.showAfterpayCheckout(cashAppPay = true) }
        }
    }

    override fun onResume() {
        super.onResume()

        view?.let {
            cashButton.isEnabled = false

            lifecycleScope.launch(Dispatchers.Unconfined) {
                viewModel.loadCheckoutToken(true)
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
                    NavGraph.action.to_receipt,
                    bundleOf(NavGraph.args.checkout_token to token),
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
