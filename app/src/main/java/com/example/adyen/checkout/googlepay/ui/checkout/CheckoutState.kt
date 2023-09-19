package com.example.adyen.checkout.googlepay.ui.checkout

import androidx.annotation.StringRes

sealed class CheckoutState {

    data object Loading : CheckoutState()

    data class LoadComponent(val googlePayComponentData: GooglePayComponentData) : CheckoutState()

    data class ShowStatusText(@StringRes val textResId: Int) : CheckoutState()
}
