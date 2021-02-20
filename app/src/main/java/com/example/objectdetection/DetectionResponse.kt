package com.example.objectdetection

import com.google.gson.annotations.SerializedName

data class DetectionResponse(
    @SerializedName("images") val detectedObject: List<Food>
){
    data class Food(
        @SerializedName("label") val foodLabel: String,
        @SerializedName("boundingBox") val foodBoundingbox: Coordinate,
        @SerializedName("confidence") val foodConfidence: Float,
        @SerializedName("color") val foodBoundingBoxColor: List<Int>
    ){
        data class Coordinate(
            @SerializedName("width") val foodWidth: Int,
            @SerializedName("height") val foodHeight: Int,
            @SerializedName("xCoor") val foodX: Int,
            @SerializedName("yCoor") val foodY: Int
        )
    }
}