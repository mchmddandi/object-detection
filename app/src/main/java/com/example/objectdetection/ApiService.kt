package com.example.objectdetection

import com.example.objectdetection.details.DetectionRequest
import com.example.objectdetection.details.DetectionResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("detect")
    suspend fun detectObject(@Body request: DetectionRequest): DetectionResponse
}