package com.example.runalyze.service.location.models

data class CurrentRunState(
    val distanceInMeters: Int = 0,
    val speedInKMH: Float = 0f,
    val isTracking: Boolean = false,
    val pathPoints: List<PathPoint> = emptyList()
)