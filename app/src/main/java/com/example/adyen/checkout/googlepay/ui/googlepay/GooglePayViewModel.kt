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
import com.example.adyen.checkout.googlepay.data.api.CheckoutService
import com.example.adyen.checkout.googlepay.data.api.safeApiCall
import com.example.adyen.checkout.googlepay.data.model.SessionApiModel
import com.example.adyen.checkout.googlepay.data.provider.CheckoutServiceProvider
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class GooglePayViewModel(
    private val checkoutService: CheckoutService = CheckoutServiceProvider.getService(),
) : ViewModel() {

    private val _googlePayViewState = MutableStateFlow<GooglePayViewState>(GooglePayViewState.Loading)
    val googlePayViewState: Flow<GooglePayViewState> = _googlePayViewState

    init {
        viewModelScope.launch { launchComponent() }
    }

    private suspend fun launchComponent() {
        val sessionApiModel = getSession()
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

        val googlePayComponentData = GooglePayComponentData(
            checkoutSession = checkoutSession,
            paymentMethod = paymentMethod,
            googlePayConfiguration = googlePayConfiguration,
        )

        _googlePayViewState.emit(
            GooglePayViewState.LoadComponent(googlePayComponentData)
        )
    }

    private suspend fun getSession(): SessionApiModel? {
        return safeApiCall {
            checkoutService.getSession()
        }
    }

    private fun getGooglePayConfiguration(sessionApiModel: SessionApiModel): GooglePayConfiguration {
        return GooglePayConfiguration.Builder(
            shopperLocale = Locale.forLanguageTag(sessionApiModel.shopperLocale),
            environment = sessionApiModel.environment.mapToEnvironment(),
            clientKey = sessionApiModel.clientSecret,
        ).build()
    }

    private suspend fun getCheckoutSession(sessionApiModel: SessionApiModel, googlePayConfiguration: GooglePayConfiguration): CheckoutSession? {
        val sessionModel = SessionModel(
            id = sessionApiModel.id,
            sessionData = sessionApiModel.sessionData,
        )

        return getCheckoutSession(sessionModel, googlePayConfiguration)
    }

    private suspend fun getCheckoutSession(
        sessionModel: SessionModel,
        googlePayConfiguration: GooglePayConfiguration,
    ): CheckoutSession? {
        return when (val result = CheckoutSessionProvider.createSession(sessionModel, googlePayConfiguration)) {
            is CheckoutSessionResult.Success -> result.checkoutSession
            is CheckoutSessionResult.Error -> null
        }
    }

    fun onComponentFinished(result: SessionPaymentResult) {
        _googlePayViewState.value = GooglePayViewState.ShowStatusText(R.string.payment_successful)
    }

    fun onComponentError(componentError: ComponentError) {
        _googlePayViewState.value = GooglePayViewState.ShowStatusText(R.string.error_google_pay)
    }
}