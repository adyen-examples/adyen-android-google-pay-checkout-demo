package com.example.adyen.checkout.googlepay.data.model

data class SessionApiModel(
    val clientSecret: String,
    val environment: CheckoutEnvironment,
    val shopperLocale: String,
    val id: String,
    val sessionData: String,
)
