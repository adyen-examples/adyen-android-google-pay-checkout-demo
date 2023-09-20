package com.example.adyen.checkout.googlepay.data.model

import com.adyen.checkout.core.Environment
import com.adyen.checkout.googlepay.MerchantInfo
import java.util.Locale

data class GooglePaySession(
    val sessionId: String,
    val sessionData: String,
    val clientKey: String,
    val environment: Environment,
    val shopperLocale: Locale,
    val merchantInfo: MerchantInfo,
)
