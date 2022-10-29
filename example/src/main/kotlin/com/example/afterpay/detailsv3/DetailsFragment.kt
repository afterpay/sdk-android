package com.example.afterpay.detailsv3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.afterpay.android.model.CheckoutV3Data
import com.afterpay.android.model.VirtualCard
import com.example.afterpay.R
import com.example.afterpay.NavGraph
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class DetailsFragment : Fragment() {

    private val resultData: CheckoutV3Data
        get() = requireNotNull(arguments?.getParcelable(NavGraph.args.result_data_v3))

    private val viewModel by viewModels<DetailsViewModel> { DetailsViewModel.factory(resultData) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_details, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().navigate(NavGraph.action.back_to_shopping)
        }

        val cardDetails = resultData.cardDetails

        val cardNumberOrToken = view.findViewById<TextView>(R.id.textView1)
        val cvcOrPaymentGateway = view.findViewById<TextView>(R.id.textView2)
        val cardExpiry = view.findViewById<TextView>(R.id.textView3)

        when (cardDetails) {
            is VirtualCard.Card -> {
                cardNumberOrToken.text = "Card number: ${cardDetails.cardNumber}"
                cvcOrPaymentGateway.text = "CVC: ${cardDetails.cvc}"
                cardExpiry.text = "Expiration: ${cardDetails.expiryMonth}/${cardDetails.expiryYear}"
            }
            is VirtualCard.TokenizedCard -> {
                cardNumberOrToken.text = "Card token: ${cardDetails.cardToken}"
                cvcOrPaymentGateway.text = "Payment gateway: ${cardDetails.paymentGateway}"
                cardExpiry.text = "Expiration: ${cardDetails.expiryMonth}/${cardDetails.expiryYear}"
            }
            else -> {
                cardNumberOrToken.text = "Card token/number: Unexpected value"
                cvcOrPaymentGateway.text = "CVC: Unavailable"
                cardExpiry.text = "Expiration: Unavailable"
            }
        }

        val expiration = view.findViewById<TextView>(R.id.textView4)
        expiration.text = "Virtual card expiry: ${viewModel.cardValidFor ?: "Unknown"}"

        val merchantReference = view.findViewById<TextView>(R.id.textView5)
        merchantReference.text = "Merchant reference: <not set>"

        val updateMerchantReferenceButton = view.findViewById<Button>(R.id.button)
        updateMerchantReferenceButton.setOnClickListener {
            updateMerchantReferenceButton.isEnabled = false
            viewModel.updateMerchantReference()
        }

        merchantReference
            .apply {
                viewModel
                    .merchantReference()
                    .onEach {
                        this.text = "Merchant reference: $it"
                        updateMerchantReferenceButton.isEnabled = true
                    }
                    .launchIn(viewLifecycleOwner.lifecycleScope)
            }
    }
}
