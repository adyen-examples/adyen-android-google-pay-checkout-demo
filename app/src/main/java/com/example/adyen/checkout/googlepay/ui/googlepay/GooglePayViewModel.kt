package com.example.adyen.checkout.googlepay.ui.googlepay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.googlepay.GooglePayConfiguration
import com.adyen.checkout.googlepay.MerchantInfo
import com.adyen.checkout.sessions.core.CheckoutSession
import com.adyen.checkout.sessions.core.CheckoutSessionProvider
import com.adyen.checkout.sessions.core.CheckoutSessionResult
import com.adyen.checkout.sessions.core.SessionModel
import com.adyen.checkout.sessions.core.SessionPaymentResult
import com.example.adyen.checkout.googlepay.R
import com.example.adyen.checkout.googlepay.data.api.CheckoutService
import com.example.adyen.checkout.googlepay.data.api.safeApiCall
import com.example.adyen.checkout.googlepay.data.model.SessionApiModel
import com.example.adyen.checkout.googlepay.data.provider.CheckoutServiceProvider
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class GooglePayViewModel(
    private val checkoutService: CheckoutService = CheckoutServiceProvider.getService(),
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
        val sessionApiModel = getSessionFromNetwork()
        if (sessionApiModel == null) {
            _googlePayViewState.emit(GooglePayViewState.ShowStatusText(R.string.error_google_pay))
            return
        }

        val googlePayConfiguration = getGooglePayConfiguration(sessionApiModel)

        val checkoutSession = getCheckoutSession(sessionApiModel, googlePayConfiguration)
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

    private suspend fun getSessionFromNetwork(): SessionApiModel? {
        return safeApiCall {
            checkoutService.getSession()
        }
    }

    private fun getGooglePayConfiguration(sessionApiModel: SessionApiModel): GooglePayConfiguration {
        return GooglePayConfiguration.Builder(
            shopperLocale = Locale.forLanguageTag(sessionApiModel.shopperLocale),
            environment = sessionApiModel.environment.mapToEnvironment(),
            clientKey = sessionApiModel.clientSecret
        )
            .setMerchantInfo(MerchantInfo(merchantName = "Test merchant"))
            .build()
    }

    private suspend fun getCheckoutSession(sessionApiModel: SessionApiModel, googlePayConfiguration: GooglePayConfiguration): CheckoutSession? {
        val sessionModel = SessionModel(
            id = sessionApiModel.id,
            sessionData = sessionApiModel.sessionData,
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

    fun onGooglePayButtonClicked() {
        viewModelScope.launch {
            _events.emit(GooglePayEvent.StartGooglePay)
        }
    }

    fun onComponentFinished(result: SessionPaymentResult) {
        _googlePayViewState.value = GooglePayViewState.ShowStatusText(R.string.payment_successful)
    }

    fun onComponentError(componentError: ComponentError) {
        _googlePayViewState.value = GooglePayViewState.ShowStatusText(R.string.error_google_pay)
    }
}