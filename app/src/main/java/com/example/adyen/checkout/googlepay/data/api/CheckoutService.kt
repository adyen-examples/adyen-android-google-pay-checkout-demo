package com.example.adyen.checkout.googlepay.data.api

import com.example.adyen.checkout.googlepay.data.model.SessionApiModel
import retrofit2.http.POST

interface CheckoutService {

    @POST("payments/sessions")
    suspend fun getSession(): SessionApiModel
}
