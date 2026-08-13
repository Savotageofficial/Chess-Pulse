package com.example.chesspulse.remote

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://lichess.org/"

    val api: LichessApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create()) // not strictly needed since we return ResponseBody directly, but harmless
            .build()
            .create(LichessApiService::class.java)
    }
}