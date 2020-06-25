package com.example.afterpay.checkout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.afterpay.R

class SuccessFragment : Fragment() {
    companion object {
        private const val TOKEN_KEY = "token"

        fun arguments(token: String): Bundle = bundleOf(TOKEN_KEY to token)
    }

    private val viewModel by viewModels<SuccessViewModel> {
        SuccessViewModel.factory(requireNotNull(arguments?.getString(TOKEN_KEY)))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_checkout_success, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().navigate(R.id.action_successFragment_to_shoppingFragment)
        }

        view.findViewById<TextView>(R.id.checkoutSuccess_text_successMessage).apply {
            text = viewModel.message
        }
    }
}
