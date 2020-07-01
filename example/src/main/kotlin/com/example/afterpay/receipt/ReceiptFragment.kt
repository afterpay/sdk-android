package com.example.afterpay.receipt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.afterpay.R
import com.example.afterpay.nav_graph

class ReceiptFragment : Fragment() {
    private val viewModel by viewModels<ReceiptViewModel> {
        ReceiptViewModel.factory(
            token = requireNotNull(arguments?.getString(nav_graph.args.checkout_token))
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_receipt, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().navigate(nav_graph.action.back_to_shopping)
        }

        view.findViewById<TextView>(R.id.receipt_message).apply {
            text = viewModel.message
        }
    }
}