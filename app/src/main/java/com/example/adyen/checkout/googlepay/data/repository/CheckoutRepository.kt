package com.example.adyen.checkout.googlepay.data.repository

import com.adyen.checkout.googlepay.MerchantInfo
import com.example.adyen.checkout.googlepay.data.api.CheckoutService
import com.example.adyen.checkout.googlepay.data.api.safeApiCall
import com.example.adyen.checkout.googlepay.data.model.GooglePaySession
import com.example.adyen.checkout.googlepay.data.model.SessionApiModel
import com.example.adyen.checkout.googlepay.data.provider.CheckoutServiceProvider
import java.util.Locale

class CheckoutRepository(
    private val checkoutService: CheckoutService = CheckoutServiceProvider.getService(),
) {
    suspend fun fetchGooglePaySession(): GooglePaySession? {
        val sessionApiModel = getSessionFromNetwork() ?: return null
        return with(sessionApiModel) {
            GooglePaySession(
                sessionId = id,
                sessionData = sessionData,
                clientSecret = clientSecret,
                environment = environment.mapToEnvironment(),
                shopperLocale = Locale.forLanguageTag(sessionApiModel.shopperLocale),
                merchantInfo = MerchantInfo(merchantName = merchantName),
            )
        }
    }

    private suspend fun getSessionFromNetwork(): SessionApiModel? {
        return safeApiCall {
            checkoutService.getSession()
        }
    }
}
