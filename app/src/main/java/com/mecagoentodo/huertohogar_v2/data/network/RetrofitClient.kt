package com.mecagoentodo.huertohogar_v2.data.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitClient {
    // DEBES REEMPLAZAR ESTA URL POR LA URL REAL DE TU API
    private const val BASE_URL = "https://api-huertohogar.ironhost.cl/api/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // Creamos el interceptor de logging
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Creamos el cliente de OkHttp y le añadimos el interceptor
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient) // Añadimos el cliente a Retrofit
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        retrofit.create(ApiService::class.java)
    }
}