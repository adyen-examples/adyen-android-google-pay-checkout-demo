package com.example.adyen.checkout.googlepay.ui.checkout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.googlepay.GooglePayComponentState
import com.adyen.checkout.googlepay.googlePay
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
    application: Application,
    private val checkoutRepository: CheckoutRepository,
) : AndroidViewModel(application),
    SessionComponentCallback<GooglePayComponentState> {

    private val _checkoutState = MutableStateFlow(CheckoutState())
    val checkoutState: StateFlow<CheckoutState> = _checkoutState.asStateFlow()

    private var googlePayComponentData: GooglePayComponentData? = null

    init {
        viewModelScope.launch { initGooglePay() }
    }

    private suspend fun initGooglePay() {
        val googlePaySession = checkoutRepository.fetchGooglePaySession()
        if (googlePaySession == null) {
            _checkoutState.update { currentState ->
                currentState.copy(checkoutUIState = CheckoutUIState.StatusText(R.string.error_google_pay))
            }
            return
        }

        val checkoutConfiguration = getCheckoutConfiguration(googlePaySession)

        val checkoutSession = getCheckoutSession(googlePaySession, checkoutConfiguration)
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

        val googlePayComponentData = GooglePayComponentData(
            checkoutSession = checkoutSession,
            paymentMethod = paymentMethod,
            checkoutConfiguration = checkoutConfiguration,
            componentCallback = this,
            key = GOOGLE_PAY_COMPONENT_KEY,
        )
        this.googlePayComponentData = googlePayComponentData
        _checkoutState.update { currentState ->
            currentState.copy(
                checkoutUIState = CheckoutUIState.GooglePayComponent(
                    googlePayComponentData
                )
            )
        }
    }

    private fun getCheckoutConfiguration(googlePaySession: GooglePaySession): CheckoutConfiguration {
        return CheckoutConfiguration(
            shopperLocale = googlePaySession.shopperLocale,
            environment = googlePaySession.environment,
            clientKey = googlePaySession.clientKey
        ) {
            googlePay {
                setSubmitButtonVisible(true)
                setMerchantInfo(googlePaySession.merchantInfo)
            }
        }
    }

    private suspend fun getCheckoutSession(
        googlePaySession: GooglePaySession,
        checkoutConfiguration: CheckoutConfiguration
    ): CheckoutSession? {
        val sessionModel = SessionModel(
            id = googlePaySession.sessionId,
            sessionData = googlePaySession.sessionData,
        )

        return when (val result =
            CheckoutSessionProvider.createSession(sessionModel, checkoutConfiguration)) {
            is CheckoutSessionResult.Success -> result.checkoutSession
            is CheckoutSessionResult.Error -> null
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

    fun actionHandled() {
        _checkoutState.update { currentState ->
            currentState.copy(handleAction = null)
        }
    }

    companion object {
        private const val GOOGLE_PAY_COMPONENT_KEY = "CheckoutScreen"
    }
}
