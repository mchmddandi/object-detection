package com.example.objectdetection.home.recentactivity

import com.google.firebase.firestore.FirebaseFirestore

class RecentActivityRepositoryImpl(private val db: FirebaseFirestore) : RecentActivityRepository {
    override fun addRecentActivity(
        recentActivity: RecentActivity,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("recent-activities")
            .add(recentActivity)
            .addOnSuccessListener {
                onSuccess.invoke("")
            }
            .addOnFailureListener {
                onError.invoke("Failed to save the result")
            }
    }

    override fun getRecentActivities(onSuccess: (List<RecentActivity>) -> Unit) {
        db.collection("recent-activities")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) return@addOnSuccessListener
                val result = mutableListOf<RecentActivity>()
                snapshot.documents.forEach { doc ->
                    val recent = RecentActivity(
                        encodedImage = doc.getString("encodedImage"),
                        detectedObject = doc.get("detectedObject") as List<String>
                    )
                    result.add(recent)
                }
                onSuccess.invoke(result)
            }
            .addOnFailureListener {

            }
    }
}