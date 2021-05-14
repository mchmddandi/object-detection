package com.example.objectdetection

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream

object Utils {
    fun encodeBitmap(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val byteArray = baos.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    fun decodeBase64toBitmap(data: String): Bitmap{
        val decodedString = Base64.decode(data, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedString,0,decodedString.size)
    }
}