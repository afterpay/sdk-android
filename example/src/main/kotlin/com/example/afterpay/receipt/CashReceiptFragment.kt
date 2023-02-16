package com.example.afterpay.receipt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.afterpay.NavGraph
import com.example.afterpay.R
import com.example.afterpay.data.CashData

class CashReceiptFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.fragment_cash_receipt, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().navigate(NavGraph.action.back_to_shopping)
        }

        val responseData = requireNotNull(arguments?.get(NavGraph.args.cash_response_data) as CashData)
        val (cashTag, amount, grantId) = responseData

        val cashTagText = view.findViewById<TextView>(R.id.cash_receipt_tag_text)
        cashTagText.text = String.format(getString(R.string.cash_tag), cashTag)

        val cashAmountText = view.findViewById<TextView>(R.id.cash_receipt_amount_text)
        cashAmountText.text = String.format(getString(R.string.cash_amount), amount)

        val cashGrantIdText = view.findViewById<TextView>(R.id.cash_receipt_grant_id_text)
        cashGrantIdText.text = String.format(getString(R.string.cash_grant_id), grantId)
    }
}
