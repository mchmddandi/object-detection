package com.example.objectdetection

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object Firestore {

    fun getInstance() = Firebase.firestore

}