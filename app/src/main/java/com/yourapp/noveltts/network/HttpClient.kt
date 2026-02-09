package com.yourapp.noveltts.network

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object HttpClient {
    val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }
}
