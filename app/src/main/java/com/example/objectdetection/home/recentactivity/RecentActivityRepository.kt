package com.example.objectdetection.home.recentactivity

interface RecentActivityRepository {
    fun addRecentActivity(recentActivity: RecentActivity, onSuccess: (String) -> Unit, onError: (String) -> Unit)
    fun getRecentActivities(onSuccess: (List<RecentActivity>) -> Unit)
}