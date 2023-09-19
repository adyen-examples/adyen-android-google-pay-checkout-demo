package com.example.adyen.checkout.googlepay.ui.checkout

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.adyen.checkout.googlepay.data.repository.CheckoutRepository

class CheckoutViewModelFactory(
    private val application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return CheckoutViewModel(application, CheckoutRepository()) as T
    }
}