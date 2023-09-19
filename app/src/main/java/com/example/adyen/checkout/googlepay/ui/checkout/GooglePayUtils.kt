package com.example.adyen.checkout.googlepay.ui.checkout

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
            put("tokenizationSpecification", gatewayTokenizationSpecification())
        }
    }

    private fun getParameters(): JSONObject {
        return JSONObject().apply {
            put("allowedAuthMethods", JSONArray(listOf("PAN_ONLY", "CRYPTOGRAM_3DS")))
            put("allowedCardNetworks", JSONArray(listOf("AMEX", "DISCOVER", "INTERAC", "JCB", "MASTERCARD", "VISA")))
        }
    }

    private fun gatewayTokenizationSpecification(): JSONObject {
        return JSONObject().apply {
            put("type", "PAYMENT_GATEWAY")
            put("parameters", JSONObject(mapOf(
                "gateway" to "adyen",
                "gatewayMerchantId" to "exampleGatewayMerchantId"
            )))
        }
    }
}