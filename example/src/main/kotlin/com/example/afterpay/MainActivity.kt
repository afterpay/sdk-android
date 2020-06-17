package com.example.afterpay

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.afterpay.android.Afterpay
import kotlinx.coroutines.flow.collectLatest

class MainActivity : AppCompatActivity() {
    private companion object {
        const val CHECKOUT_WITH_AFTERPAY = 1234
    }

    private val viewModel: MainViewModel by viewModels { MainViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val emailField = findViewById<EditText>(R.id.main_emailAddress)
        emailField.addTextChangedListener { text ->
            viewModel.enterEmailAddress(email = text.toString())
        }

        val checkoutButton = findViewById<Button>(R.id.main_checkoutButton)
        checkoutButton.setOnClickListener {
            viewModel.checkoutWithAfterpay()
        }

        val progressBar = findViewById<ProgressBar>(R.id.main_progressBar)

        lifecycleScope.launchWhenCreated {
            viewModel.state().collectLatest { state ->
                checkoutButton.isEnabled = state.canSubmit
                progressBar.visibility = if (state.isLoading) View.VISIBLE else View.INVISIBLE
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.events().collectLatest { event ->
                when (event) {
                    is MainViewModel.Event.StartAfterpayCheckout -> {
                        val intent = Afterpay.createCheckoutIntent(this@MainActivity, event.url)
                        startActivityForResult(intent, CHECKOUT_WITH_AFTERPAY)
                    }
                    is MainViewModel.Event.CheckoutFailed -> {
                        Toast.makeText(this@MainActivity, event.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode to resultCode) {
            CHECKOUT_WITH_AFTERPAY to RESULT_OK -> {
                val intent = data ?: error("Intent should always be populated by the Afterpay SDK")
                val status = Afterpay.parseCheckoutResponse(intent)
                Toast.makeText(this, "Result: $status", Toast.LENGTH_SHORT).show()
            }
            CHECKOUT_WITH_AFTERPAY to RESULT_CANCELED -> {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
