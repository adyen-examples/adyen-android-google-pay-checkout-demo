package com.example.adyen.checkout.googlepay.ui.checkout

import android.content.Intent
import androidx.annotation.StringRes
import com.adyen.checkout.components.core.ComponentAvailableCallback
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.googlepay.GooglePayConfiguration

data class CheckoutState(
    val checkoutUIState: CheckoutUIState = CheckoutUIState.LoadingSpinner,
    val handleActivityResult: HandleActivityResult? = null,
    val handleAction: HandleAction? = null,
)

sealed class CheckoutUIState {
    data object LoadingSpinner : CheckoutUIState()
    data class GooglePayButton(val googlePayComponentData: GooglePayComponentData) : CheckoutUIState()
    data class StatusText(@StringRes val textResId: Int) : CheckoutUIState()
}

data class HandleActivityResult(val resultCode: Int, val data: Intent?, val googlePayComponentData: GooglePayComponentData)

data class HandleAction(val action: Action, val googlePayComponentData: GooglePayComponentData)
