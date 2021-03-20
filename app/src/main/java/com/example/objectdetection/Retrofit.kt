package com.example.objectdetection

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Retrofit {
    fun getInstance(): Retrofit{
        return Retrofit.Builder()
            .baseUrl("http://192.168.252.134:5000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}