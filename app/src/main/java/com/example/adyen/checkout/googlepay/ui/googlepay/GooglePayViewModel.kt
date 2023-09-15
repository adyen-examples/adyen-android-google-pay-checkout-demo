package com.example.adyen.checkout.googlepay.ui.googlepay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.googlepay.GooglePayConfiguration
import com.adyen.checkout.sessions.core.CheckoutSession
import com.adyen.checkout.sessions.core.CheckoutSessionProvider
import com.adyen.checkout.sessions.core.CheckoutSessionResult
import com.adyen.checkout.sessions.core.SessionModel
import com.adyen.checkout.sessions.core.SessionPaymentResult
import com.example.adyen.checkout.googlepay.R
import com.example.adyen.checkout.googlepay.data.model.GooglePaySession
import com.example.adyen.checkout.googlepay.data.repository.CheckoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class GooglePayViewModel(
    private val checkoutRepository: CheckoutRepository = CheckoutRepository(),
) : ViewModel() {

    private val _googlePayViewState = MutableStateFlow<GooglePayViewState>(GooglePayViewState.Loading)
    val googlePayViewState: Flow<GooglePayViewState> = _googlePayViewState

    private val _events = MutableSharedFlow<GooglePayEvent>()
    val events: Flow<GooglePayEvent> = _events

    private var googlePayComponentData: GooglePayComponentData? = null

    init {
        viewModelScope.launch { checkGooglePayAvailability() }
    }

    private suspend fun checkGooglePayAvailability() {
        val googlePaySession = checkoutRepository.fetchGooglePaySession()
        if (googlePaySession == null) {
            _googlePayViewState.emit(GooglePayViewState.ShowStatusText(R.string.error_google_pay))
            return
        }

        val googlePayConfiguration = getGooglePayConfiguration(googlePaySession)

        val checkoutSession = getCheckoutSession(googlePaySession, googlePayConfiguration)
        if (checkoutSession == null) {
            _googlePayViewState.emit(GooglePayViewState.ShowStatusText(R.string.error_google_pay))
            return
        }
        val paymentMethod = checkoutSession.getPaymentMethod(PaymentMethodTypes.GOOGLE_PAY)
        if (paymentMethod == null) {
            _googlePayViewState.emit(GooglePayViewState.ShowStatusText(R.string.error_google_pay))
            return
        }

        _events.emit(GooglePayEvent.CheckGooglePayAvailability(paymentMethod, googlePayConfiguration))

        googlePayComponentData = GooglePayComponentData(
            checkoutSession = checkoutSession,
            paymentMethod = paymentMethod,
            googlePayConfiguration = googlePayConfiguration,
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

    fun onGooglePayAvailabilityResult(isAvailable: Boolean) {
        if (isAvailable) {
            val googlePayComponentData = googlePayComponentData ?: return
            _googlePayViewState.value = GooglePayViewState.LoadComponent(googlePayComponentData)
        } else {
            _googlePayViewState.value = GooglePayViewState.ShowStatusText(R.string.error_google_pay_unavailable)

        }
    }

    fun onComponentFinished(result: SessionPaymentResult) {
        _googlePayViewState.value = GooglePayViewState.ShowStatusText(R.string.payment_successful)
    }

    fun onComponentError(componentError: ComponentError) {
        _googlePayViewState.value = GooglePayViewState.ShowStatusText(R.string.error_google_pay)
    }
}
