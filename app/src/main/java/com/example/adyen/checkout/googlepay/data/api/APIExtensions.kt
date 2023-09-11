package com.example.adyen.checkout.googlepay.data.api

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Suppress("TooGenericExceptionCaught")
internal suspend fun <T> safeApiCall(call: suspend () -> T): T? = withContext(Dispatchers.IO) {
    return@withContext try {
        call()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        null
    }
}