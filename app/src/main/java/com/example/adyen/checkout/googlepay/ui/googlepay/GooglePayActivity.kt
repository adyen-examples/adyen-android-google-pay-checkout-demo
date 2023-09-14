package com.example.adyen.checkout.googlepay.ui.googlepay

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.adyen.checkout.components.core.ComponentAvailableCallback
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.googlepay.GooglePayComponent
import com.adyen.checkout.googlepay.GooglePayComponentState
import com.adyen.checkout.googlepay.GooglePayConfiguration
import com.adyen.checkout.sessions.core.SessionComponentCallback
import com.adyen.checkout.sessions.core.SessionPaymentResult
import com.example.adyen.checkout.googlepay.databinding.ActivityGooglePayBinding
import kotlinx.coroutines.launch

class GooglePayActivity : AppCompatActivity(),
    SessionComponentCallback<GooglePayComponentState>,
    ComponentAvailableCallback {

    private lateinit var binding: ActivityGooglePayBinding

    private var googlePayComponent: GooglePayComponent? = null

    private val googlePayViewModel: GooglePayViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGooglePayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.payButton.setOnClickListener {
            googlePayViewModel.onGooglePayButtonClicked()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { googlePayViewModel.googlePayViewState.collect(::onViewState) }
                launch { googlePayViewModel.events.collect(::onEvent) }
            }
        }
    }

    private fun onViewState(googlePayViewState: GooglePayViewState) {
        when (googlePayViewState) {
            is GooglePayViewState.LoadComponent -> {
                binding.progressIndicator.isVisible = false
                binding.statusTextView.isVisible = false
                binding.payButton.isVisible = true
                loadComponent(googlePayViewState.googlePayComponentData)
            }

            is GooglePayViewState.Loading -> {
                binding.progressIndicator.isVisible = true
                binding.statusTextView.isVisible = false
                binding.payButton.isVisible = false
            }

            is GooglePayViewState.ShowStatusText -> {
                binding.statusTextView.isVisible = true
                binding.progressIndicator.isVisible = false
                binding.payButton.isVisible = false
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
        )
    }

    private fun onEvent(googlePayEvent: GooglePayEvent) {
        when (googlePayEvent) {
            is GooglePayEvent.CheckGooglePayAvailability -> {
                checkAvailability(
                    googlePayEvent.paymentMethod,
                    googlePayEvent.configuration,
                )
            }

            GooglePayEvent.StartGooglePay -> {
                googlePayComponent?.startGooglePayScreen(this@GooglePayActivity, GOOGLE_PAY_REQUEST_CODE)
            }
        }
    }

    private fun checkAvailability(paymentMethod: PaymentMethod, googlePayConfiguration: GooglePayConfiguration) {
        GooglePayComponent.PROVIDER.isAvailable(
            applicationContext = application,
            paymentMethod = paymentMethod,
            configuration = googlePayConfiguration,
            callback = this,
        )
    }

    override fun onAvailabilityResult(isAvailable: Boolean, paymentMethod: PaymentMethod) {
        googlePayViewModel.onGooglePayAvailabilityResult(isAvailable)
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
