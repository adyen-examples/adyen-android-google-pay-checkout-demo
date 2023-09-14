package com.example.adyen.checkout.googlepay.ui.googlepay

import org.json.JSONArray
import org.json.JSONObject


object GooglePayUtils {

    fun getAllowedPaymentMethods(): String {
        return JSONArray().put(getBaseCardPaymentMethod()).toString()
    }

    private fun getBaseCardPaymentMethod(): JSONObject? {
        return JSONObject().apply {
            put("type", "CARD")
            put("parameters", getParameters())
        }
    }

    private fun getParameters(): JSONObject {
        return JSONObject().apply {
            put("allowedAuthMethods", listOf("PAN_ONLY", "CRYPTOGRAM_3DS"))
            put("allowedCardNetworks", listOf("AMEX", "DISCOVER", "INTERAC", "JCB", "MASTERCARD", "VISA"))
        }
    }
}
