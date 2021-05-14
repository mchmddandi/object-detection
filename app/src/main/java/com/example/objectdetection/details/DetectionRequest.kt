package com.example.objectdetection.details

import com.google.gson.annotations.SerializedName

data class DetectionRequest(
    @SerializedName("encoded_image") val encodedImage: String
)