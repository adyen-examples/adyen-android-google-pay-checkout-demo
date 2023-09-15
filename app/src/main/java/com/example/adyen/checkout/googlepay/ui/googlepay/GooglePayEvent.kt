package com.example.adyen.checkout.googlepay.ui.googlepay

import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.googlepay.GooglePayConfiguration

sealed class GooglePayEvent {

    data class CheckGooglePayAvailability(
        val paymentMethod: PaymentMethod,
        val configuration: GooglePayConfiguration,
    ) : GooglePayEvent()
}
