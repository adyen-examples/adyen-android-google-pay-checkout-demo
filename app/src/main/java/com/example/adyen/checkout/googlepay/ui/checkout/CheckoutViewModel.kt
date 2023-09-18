package com.example.adyen.checkout.googlepay.ui.checkout

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.core.ComponentAvailableCallback
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.googlepay.GooglePayComponentState
import com.adyen.checkout.googlepay.GooglePayConfiguration
import com.adyen.checkout.sessions.core.CheckoutSession
import com.adyen.checkout.sessions.core.CheckoutSessionProvider
import com.adyen.checkout.sessions.core.CheckoutSessionResult
import com.adyen.checkout.sessions.core.SessionComponentCallback
import com.adyen.checkout.sessions.core.SessionModel
import com.adyen.checkout.sessions.core.SessionPaymentResult
import com.example.adyen.checkout.googlepay.R
import com.example.adyen.checkout.googlepay.data.model.GooglePaySession
import com.example.adyen.checkout.googlepay.data.repository.CheckoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CheckoutViewModel(
    private val checkoutRepository: CheckoutRepository = CheckoutRepository(),
) : ViewModel(),
    SessionComponentCallback<GooglePayComponentState>,
    ComponentAvailableCallback {

    private val _checkoutState = MutableStateFlow(CheckoutState())
    val checkoutState: StateFlow<CheckoutState> = _checkoutState.asStateFlow()

    private var googlePayComponentData: GooglePayComponentData? = null

    init {
        viewModelScope.launch { checkGooglePayAvailability() }
    }

    private suspend fun checkGooglePayAvailability() {
        val googlePaySession = checkoutRepository.fetchGooglePaySession()
        if (googlePaySession == null) {
            _checkoutState.update { currentState ->
                currentState.copy(checkoutUIState = CheckoutUIState.StatusText(R.string.error_google_pay))
            }
            return
        }

        val googlePayConfiguration = getGooglePayConfiguration(googlePaySession)

        val checkoutSession = getCheckoutSession(googlePaySession, googlePayConfiguration)
        if (checkoutSession == null) {
            _checkoutState.update { currentState ->
                currentState.copy(checkoutUIState = CheckoutUIState.StatusText(R.string.error_google_pay))
            }
            return
        }
        val paymentMethod = checkoutSession.getPaymentMethod(PaymentMethodTypes.GOOGLE_PAY)
        if (paymentMethod == null) {
            _checkoutState.update { currentState ->
                currentState.copy(checkoutUIState = CheckoutUIState.StatusText(R.string.error_google_pay))
            }
            return
        }

        _checkoutState.update { currentState ->
            currentState.copy(checkGooglePayAvailability = CheckGooglePayAvailability(paymentMethod, googlePayConfiguration, this))
        }

        googlePayComponentData = GooglePayComponentData(
            checkoutSession = checkoutSession,
            paymentMethod = paymentMethod,
            googlePayConfiguration = googlePayConfiguration,
            componentCallback = this,
            key = GOOGLE_PAY_COMPONENT_KEY,
            requestCode = GOOGLE_PAY_REQUEST_CODE,
        )
    }

    private fun getGooglePayConfiguration(googlePaySession: GooglePaySession): GooglePayConfiguration {
        return GooglePayConfiguration.Builder(
            shopperLocale = googlePaySession.shopperLocale,
            environment = googlePaySession.environment,
            clientKey = googlePaySession.clientSecret
        )
            .setMerchantInfo(googlePaySession.merchantInfo)
            .build()
    }

    private suspend fun getCheckoutSession(googlePaySession: GooglePaySession, googlePayConfiguration: GooglePayConfiguration): CheckoutSession? {
        val sessionModel = SessionModel(
            id = googlePaySession.sessionId,
            sessionData = googlePaySession.sessionData,
        )

        return when (val result = CheckoutSessionProvider.createSession(sessionModel, googlePayConfiguration)) {
            is CheckoutSessionResult.Success -> result.checkoutSession
            is CheckoutSessionResult.Error -> null
        }
    }

    override fun onAvailabilityResult(isAvailable: Boolean, paymentMethod: PaymentMethod) {
        if (isAvailable) {
            val googlePayComponentData = googlePayComponentData ?: return
            _checkoutState.update { currentState ->
                currentState.copy(checkoutUIState = CheckoutUIState.GooglePayCheckoutUI(googlePayComponentData))
            }
        } else {
            _checkoutState.update { currentState ->
                currentState.copy(checkoutUIState = CheckoutUIState.StatusText(R.string.error_google_pay_unavailable))
            }

        }
    }

    override fun onAction(action: Action) {
        val googlePayComponentData = googlePayComponentData ?: return
        _checkoutState.update { currentState ->
            currentState.copy(handleAction = HandleAction(action, googlePayComponentData))
        }
    }

    override fun onFinished(result: SessionPaymentResult) {
        _checkoutState.update { currentState ->
            currentState.copy(checkoutUIState = CheckoutUIState.StatusText(R.string.payment_successful))
        }
    }

    override fun onError(componentError: ComponentError) {
        _checkoutState.update { currentState ->
            currentState.copy(checkoutUIState = CheckoutUIState.StatusText(R.string.error_google_pay))
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != GOOGLE_PAY_REQUEST_CODE) return
        val googlePayComponentData = googlePayComponentData ?: return
        _checkoutState.update { currentState ->
            currentState.copy(handleActivityResult = HandleActivityResult(resultCode, data, googlePayComponentData))
        }
    }

    fun availabilityChecked() {
        _checkoutState.update { currentState ->
            currentState.copy(checkGooglePayAvailability = null)
        }
    }

    fun actionHandled() {
        _checkoutState.update { currentState ->
            currentState.copy(handleAction = null)
        }
    }

    fun activityResultHandled() {
        _checkoutState.update { currentState ->
            currentState.copy(handleActivityResult = null)
        }
    }

    companion object {
        private const val GOOGLE_PAY_COMPONENT_KEY = "CheckoutScreen"
        private const val GOOGLE_PAY_REQUEST_CODE = 1
    }
}
