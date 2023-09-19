package com.example.adyen.checkout.googlepay.ui.checkout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.core.ComponentAvailableCallback
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.googlepay.GooglePayComponent
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CheckoutViewModel(
    application: Application,
    private val checkoutRepository: CheckoutRepository,
) : AndroidViewModel(application),
    ComponentAvailableCallback {

    private val _checkoutState = MutableStateFlow<CheckoutState>(CheckoutState.Loading)
    val checkoutState: Flow<CheckoutState> = _checkoutState

    private var googlePayComponentData: GooglePayComponentData? = null

    init {
        viewModelScope.launch { startGooglePay() }
    }

    private suspend fun startGooglePay() {
        val googlePaySession = checkoutRepository.fetchGooglePaySession()
        if (googlePaySession == null) {
            _checkoutState.emit(CheckoutState.ShowStatusText(R.string.error_google_pay))
            return
        }

        val googlePayConfiguration = getGooglePayConfiguration(googlePaySession)

        val checkoutSession = getCheckoutSession(googlePaySession, googlePayConfiguration)
        if (checkoutSession == null) {
            _checkoutState.emit(CheckoutState.ShowStatusText(R.string.error_google_pay))
            return
        }
        val paymentMethod = checkoutSession.getPaymentMethod(PaymentMethodTypes.GOOGLE_PAY)
        if (paymentMethod == null) {
            _checkoutState.emit(CheckoutState.ShowStatusText(R.string.error_google_pay))
            return
        }

        checkGooglePayAvailability(paymentMethod, googlePayConfiguration)

        googlePayComponentData = GooglePayComponentData(
            checkoutSession = checkoutSession,
            paymentMethod = paymentMethod,
            googlePayConfiguration = googlePayConfiguration,
        )
    }

    private fun checkGooglePayAvailability(paymentMethod: PaymentMethod, googlePayConfiguration: GooglePayConfiguration) {
        GooglePayComponent.PROVIDER.isAvailable(
            applicationContext = getApplication(),
            paymentMethod = paymentMethod,
            configuration = googlePayConfiguration,
            callback = this,
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
            _checkoutState.value = CheckoutState.LoadComponent(googlePayComponentData)
        } else {
            _checkoutState.value = CheckoutState.ShowStatusText(R.string.error_google_pay_unavailable)

        }
    }

    fun onComponentFinished(result: SessionPaymentResult) {
        _checkoutState.value = CheckoutState.ShowStatusText(R.string.payment_successful)
    }

    fun onComponentError(componentError: ComponentError) {
        _checkoutState.value = CheckoutState.ShowStatusText(R.string.error_google_pay)
    }
}
