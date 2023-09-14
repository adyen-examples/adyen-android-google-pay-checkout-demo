package com.example.adyen.checkout.googlepay.ui.googlepay

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.googlepay.GooglePayComponent
import com.adyen.checkout.googlepay.GooglePayComponentState
import com.adyen.checkout.sessions.core.SessionComponentCallback
import com.adyen.checkout.sessions.core.SessionPaymentResult
import com.example.adyen.checkout.googlepay.databinding.ActivityGooglePayBinding
import kotlinx.coroutines.launch

class GooglePayActivity : AppCompatActivity(), SessionComponentCallback<GooglePayComponentState> {

    private lateinit var binding: ActivityGooglePayBinding

    private var googlePayComponent: GooglePayComponent? = null

    private val googlePayViewModel: GooglePayViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGooglePayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { googlePayViewModel.googlePayViewState.collect(::onViewState) }
            }
        }
    }

    private fun onViewState(googlePayViewState: GooglePayViewState) {
        when (googlePayViewState) {
            is GooglePayViewState.LoadComponent -> {
                binding.progressIndicator.isVisible = false
                binding.statusTextView.isVisible = false
                loadComponent(googlePayViewState.googlePayComponentData)
            }

            is GooglePayViewState.Loading -> {
                binding.progressIndicator.isVisible = true
                binding.statusTextView.isVisible = false
            }

            is GooglePayViewState.ShowStatusText -> {
                binding.statusTextView.isVisible = true
                binding.progressIndicator.isVisible = false
                binding.statusTextView.setText(googlePayViewState.textResId)
            }
        }
    }

    private fun loadComponent(googlePayComponentData: GooglePayComponentData) {
        googlePayComponent = GooglePayComponent.PROVIDER.get(
            activity = this,
            checkoutSession = googlePayComponentData.checkoutSession,
            paymentMethod = googlePayComponentData.paymentMethod,
            configuration = googlePayComponentData.googlePayConfiguration,
            componentCallback = this,
        ).apply {
            startGooglePayScreen(this@GooglePayActivity, GOOGLE_PAY_REQUEST_CODE)
        }
    }

    override fun onFinished(result: SessionPaymentResult) {
        googlePayViewModel.onComponentFinished(result)
    }

    override fun onError(componentError: ComponentError) {
        googlePayViewModel.onComponentError(componentError)
    }

    override fun onAction(action: Action) {
        googlePayComponent?.handleAction(action, this)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != GOOGLE_PAY_REQUEST_CODE) return
        googlePayComponent?.handleActivityResult(resultCode, data)

    }

    companion object {
        private const val GOOGLE_PAY_REQUEST_CODE = 1
    }
}
