package com.example.adyen.checkout.googlepay.data.model

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

class CheckoutEnvironmentAdapter {
    @ToJson
    fun toJson(environment: CheckoutEnvironment): String = environment.stringValue

    @FromJson
    fun fromJson(stringValue: String): CheckoutEnvironment = CheckoutEnvironment.fromString(stringValue)
}