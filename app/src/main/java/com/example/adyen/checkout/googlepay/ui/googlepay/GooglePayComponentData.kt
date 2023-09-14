package com.example.adyen.checkout.googlepay.ui.googlepay

import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.googlepay.GooglePayConfiguration
import com.adyen.checkout.sessions.core.CheckoutSession

data class GooglePayComponentData(
    val checkoutSession: CheckoutSession,
    val paymentMethod: PaymentMethod,
    val googlePayConfiguration: GooglePayConfiguration,
)
