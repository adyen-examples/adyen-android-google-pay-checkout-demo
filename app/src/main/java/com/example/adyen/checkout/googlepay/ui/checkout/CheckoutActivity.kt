package com.example.adyen.checkout.googlepay.ui.checkout

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

class CheckoutActivity : AppCompatActivity() {

    private val checkoutViewModel: CheckoutViewModel by viewModels { CheckoutViewModelFactory(application) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CheckoutScreen(
                viewModel = checkoutViewModel,
            )
        }
    }
}
