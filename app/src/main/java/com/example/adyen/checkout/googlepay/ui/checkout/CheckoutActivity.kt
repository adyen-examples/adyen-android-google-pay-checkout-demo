package com.example.adyen.checkout.googlepay.ui.checkout

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
import com.google.android.gms.wallet.button.ButtonConstants
import com.google.android.gms.wallet.button.ButtonOptions
import kotlinx.coroutines.launch

class CheckoutActivity :
    AppCompatActivity(),
    SessionComponentCallback<GooglePayComponentState> {

    private lateinit var binding: ActivityGooglePayBinding

    private var googlePayComponent: GooglePayComponent? = null

    private val mCheckoutViewModel: CheckoutViewModel by viewModels { CheckoutViewModelProvider(application) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGooglePayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializePayButton()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { mCheckoutViewModel.checkoutState.collect(::onViewState) }
            }
        }
    }

    private fun initializePayButton() {
        binding.payButton.initialize(
            ButtonOptions.newBuilder()
                .setButtonType(ButtonConstants.ButtonType.PAY)
                .setAllowedPaymentMethods(GooglePayUtils.getAllowedPaymentMethods())
                .build()
        )

        binding.payButton.setOnClickListener {
            googlePayComponent?.startGooglePayScreen(this@CheckoutActivity, GOOGLE_PAY_REQUEST_CODE)
        }
    }

    private fun onViewState(checkoutState: CheckoutState) {
        when (checkoutState) {
            is CheckoutState.LoadComponent -> {
                binding.progressIndicator.isVisible = false
                binding.statusTextView.isVisible = false
                binding.payButton.isVisible = true
                loadComponent(checkoutState.googlePayComponentData)
            }

            is CheckoutState.Loading -> {
                binding.progressIndicator.isVisible = true
                binding.statusTextView.isVisible = false
                binding.payButton.isVisible = false
            }

            is CheckoutState.ShowStatusText -> {
                binding.statusTextView.isVisible = true
                binding.progressIndicator.isVisible = false
                binding.payButton.isVisible = false
                binding.statusTextView.setText(checkoutState.textResId)
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

    override fun onFinished(result: SessionPaymentResult) {
        mCheckoutViewModel.onComponentFinished(result)
    }

    override fun onError(componentError: ComponentError) {
        mCheckoutViewModel.onComponentError(componentError)
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
