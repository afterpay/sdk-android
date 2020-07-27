package com.example.afterpay.checkout

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.afterpay.android.Afterpay
import com.afterpay.android.view.AfterpayButton
import com.example.afterpay.Dependencies
import com.example.afterpay.R
import com.example.afterpay.checkout.CheckoutViewModel.Command
import com.example.afterpay.nav_graph
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
            merchantApi = Dependencies.merchantApi
        )
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

        val checkoutButton = view.findViewById<AfterpayButton>(R.id.cart_button_checkout)
        checkoutButton.setOnClickListener {
            viewModel.checkoutWithAfterpay()
        }

        val totalCost = view.findViewById<TextView>(R.id.cart_totalCost)
        val progressBar = view.findViewById<ProgressBar>(R.id.cart_progressBar)

        lifecycleScope.launchWhenCreated {
            viewModel.state().collectLatest { state ->
                totalCost.text = state.totalCost
                checkoutButton.isEnabled = state.enableCheckoutButton
                progressBar.visibility = if (state.showProgressBar) View.VISIBLE else View.INVISIBLE
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.commands().collectLatest { command ->
                when (command) {
                    is Command.StartAfterpayCheckout -> {
                        val intent = Afterpay.createCheckoutIntent(requireContext(), command.url)
                        startActivityForResult(intent, CHECKOUT_WITH_AFTERPAY)
                    }
                    is Command.DisplayError -> {
                        Snackbar.make(requireView(), command.message, Snackbar.LENGTH_SHORT).show()
                    }
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
