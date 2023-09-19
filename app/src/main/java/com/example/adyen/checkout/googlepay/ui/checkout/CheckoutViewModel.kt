package com.example.adyen.checkout.googlepay.ui.checkout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.adyen.checkout.googlepay.data.repository.CheckoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CheckoutViewModel(
    application: Application,
    private val checkoutRepository: CheckoutRepository,
) : AndroidViewModel(application) {

    private val _checkoutState = MutableStateFlow(CheckoutState())
    val checkoutState: StateFlow<CheckoutState> = _checkoutState.asStateFlow()
}
