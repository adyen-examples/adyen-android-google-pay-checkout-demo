package com.example.adyen.checkout.googlepay.ui.checkout

import androidx.annotation.StringRes
import com.adyen.checkout.components.core.action.Action

data class CheckoutState(
    val checkoutUIState: CheckoutUIState = CheckoutUIState.LoadingSpinner,
    val handleAction: HandleAction? = null,
)

sealed class CheckoutUIState {
    data object LoadingSpinner : CheckoutUIState()
    data class GooglePayComponent(val googlePayComponentData: GooglePayComponentData) :
        CheckoutUIState()

    data class StatusText(@StringRes val textResId: Int) : CheckoutUIState()
}

data class HandleAction(val action: Action, val googlePayComponentData: GooglePayComponentData)
