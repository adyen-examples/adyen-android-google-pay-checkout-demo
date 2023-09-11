package com.example.adyen.checkout.googlepay.ui.googlepay

import androidx.annotation.StringRes

sealed class GooglePayViewState {

    object Loading : GooglePayViewState()

    data class LoadComponent(val googlePayComponentData: GooglePayComponentData) : GooglePayViewState()

    data class ShowStatusText(@StringRes val textResId: Int) : GooglePayViewState()
}
