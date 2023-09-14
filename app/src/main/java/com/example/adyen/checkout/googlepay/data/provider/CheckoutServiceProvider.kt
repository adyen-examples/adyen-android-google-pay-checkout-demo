package com.example.adyen.checkout.googlepay.data.provider

import com.example.adyen.checkout.googlepay.BuildConfig
import com.example.adyen.checkout.googlepay.data.api.CheckoutService
import com.example.adyen.checkout.googlepay.data.model.CheckoutEnvironmentAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object CheckoutServiceProvider {

    fun getService(): CheckoutService = checkoutService

    private val checkoutService by lazy { provideService() }

    private fun provideService(): CheckoutService {
        val retrofit = provideRetrofit(
            okHttpClient = provideOkHttpClient(),
            converterFactory = provideConverterFactory(),
        )
        return retrofit.create(CheckoutService::class.java)

    }

    private fun provideRetrofit(
        okHttpClient: OkHttpClient,
        converterFactory: Converter.Factory,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_API_URL)
            .client(okHttpClient)
            .addConverterFactory(converterFactory)
            .build()

    private fun provideOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()

        if (BuildConfig.DEBUG) {
            val interceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addNetworkInterceptor(interceptor)
        }

        return builder.build()
    }

    private fun provideConverterFactory(): Converter.Factory = MoshiConverterFactory.create(
        Moshi.Builder()
            .add(CheckoutEnvironmentAdapter())
            .add(KotlinJsonAdapterFactory())
            .build()
    )

}
