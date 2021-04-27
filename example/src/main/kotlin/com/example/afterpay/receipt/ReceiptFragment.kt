package com.example.afterpay.receipt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.addCallback
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.afterpay.android.view.AfterpayWidgetView
import com.example.afterpay.R
import com.example.afterpay.nav_graph
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach

class ReceiptFragment : Fragment() {

    private val token: String
        get() = requireNotNull(arguments?.getString(nav_graph.args.checkout_token))

    private val viewModel by viewModels<ReceiptViewModel> { ReceiptViewModel.factory(token) }

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

        view.findViewById<AfterpayWidgetView>(R.id.receipt_widget)
            .apply {
                init(viewModel.token)
                viewModel
                    .totalCost()
                    .onEach { update(it) }
                    .launchIn(viewLifecycleOwner.lifecycleScope)
            }

        view.findViewById<EditText>(R.id.receipt_totalCost).apply {
            callbackFlow {
                doOnTextChanged { text, _, _, _ -> trySend(text.toString()) }
                    .also { awaitClose { removeTextChangedListener(it) } }
            }
                .mapNotNull { it.toBigDecimalOrNull() }
                .conflate()
                .debounce(timeoutMillis = 300)
                .onEach { viewModel.onTotalCost(it) }
                .launchIn(viewLifecycleOwner.lifecycleScope)
        }
    }
}
