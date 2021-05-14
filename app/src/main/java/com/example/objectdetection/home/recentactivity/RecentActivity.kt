package com.example.objectdetection.home.recentactivity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class RecentActivity(
    val encodedImage: String?,
    val detectedObject: List<String>?
) : Parcelable
