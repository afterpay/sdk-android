package com.example.afterpay.checkout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.afterpay.android.AfterpayEnvironment
import com.example.afterpay.MainViewModel
import com.example.afterpay.R
import com.example.afterpay.databinding.FragmentBottomSheetBinding
import com.example.afterpay.getDependencies
import com.example.afterpay.util.getHostname
import com.example.afterpay.util.getPort
import com.example.afterpay.util.putHostname
import com.example.afterpay.util.putPort
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class BottomSheetOptionsFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentBottomSheetBinding? = null

    private val activityViewModel: MainViewModel by activityViewModels()

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.refreshConfigurationButton.setOnClickListener {
            lifecycleScope.launch {
                activityViewModel.applyAfterpayConfiguration(forceRefresh = true)
            }
        }

        val preferences = getDependencies().sharedPreferences

        // Populate fields.
        binding.hostField.setText(preferences.getHostname())
        binding.portField.setText(preferences.getPort())
        if (activityViewModel.environment == AfterpayEnvironment.PRODUCTION) {
            binding.environmentToggleButton.check(R.id.productionButton)
        } else {
            binding.environmentToggleButton.check(R.id.sandboxButton)
        }

        // Environment Toggle Buttons.
        binding.sandboxButton.setOnClickListener {
            activityViewModel.setEnvironment(AfterpayEnvironment.SANDBOX)
        }
        binding.productionButton.setOnClickListener {
            activityViewModel.setEnvironment(AfterpayEnvironment.PRODUCTION)
        }

        binding.applyChangesButton.setOnClickListener {
            preferences.edit {
                putHostname(binding.hostField.text.toString())
                putPort(binding.portField.text.toString())
            }
            Toast.makeText(
                requireContext(),
                R.string.dev_options_restart_app,
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
