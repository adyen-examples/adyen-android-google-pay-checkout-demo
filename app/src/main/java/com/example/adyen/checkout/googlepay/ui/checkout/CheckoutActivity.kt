package com.example.adyen.checkout.googlepay.ui.checkout

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.adyen.checkout.redirect.RedirectComponent

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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val data = intent.data
        if (data != null && data.toString().startsWith(RedirectComponent.REDIRECT_RESULT_SCHEME)) {
            checkoutViewModel.handleIntent(intent)
        }
    }
}
