package com.example.adyen.checkout.googlepay.ui.checkout

import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.googlepay.GooglePayComponentState
import com.adyen.checkout.sessions.core.CheckoutSession
import com.adyen.checkout.sessions.core.SessionComponentCallback

data class GooglePayComponentData(
    val checkoutSession: CheckoutSession,
    val paymentMethod: PaymentMethod,
    val checkoutConfiguration: CheckoutConfiguration,
    val componentCallback: SessionComponentCallback<GooglePayComponentState>,
    val key: String,
)
