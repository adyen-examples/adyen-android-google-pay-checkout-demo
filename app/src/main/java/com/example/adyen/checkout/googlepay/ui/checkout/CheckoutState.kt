package com.example.adyen.checkout.googlepay.ui.checkout

import androidx.annotation.StringRes

data class CheckoutState(
    val checkoutUIState: CheckoutUIState = CheckoutUIState.LoadingSpinner,
)

sealed class CheckoutUIState {
    data object LoadingSpinner : CheckoutUIState()
    data class StatusText(@StringRes val textResId: Int) : CheckoutUIState()
}
