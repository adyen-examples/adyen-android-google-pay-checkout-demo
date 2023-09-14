package com.example.adyen.checkout.googlepay.data.model

import com.adyen.checkout.core.Environment

enum class CheckoutEnvironment(val stringValue: String) {
    TEST("Environment.TEST"),
    EUROPE("Environment.EUROPE"),
    UNITED_STATES("Environment.UNITED_STATES"),
    UNKNOWN("");

    fun mapToEnvironment(): Environment {
        return when (this) {
            TEST -> Environment.TEST
            EUROPE -> Environment.EUROPE
            UNITED_STATES -> Environment.UNITED_STATES
            UNKNOWN -> Environment.TEST
        }
    }

    companion object {
        fun fromString(stringValue: String) = values().associateBy(CheckoutEnvironment::stringValue)[stringValue] ?: UNKNOWN
    }
}