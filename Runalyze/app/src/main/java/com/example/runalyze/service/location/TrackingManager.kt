package com.example.runalyze.service.location

import android.location.Location
import android.util.Log
import com.example.runalyze.service.TimeTracker
import com.example.runalyze.service.location.models.CurrentRunState
import com.example.runalyze.service.location.models.PathPoint
import com.example.runalyze.utils.RunUtils
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.math.RoundingMode

class TrackingManager(
    private val locationTrackingManager: LocationTrackingManager,
    private val timeTracker: TimeTracker,
    private val trackingServiceManager: TrackingServiceManager
) {
    private var isTracking = false
        set(value) {
            _currentRunState.update { it.copy(isTracking = value) }
            field = value
        }

    private val _currentRunState = MutableStateFlow(CurrentRunState())
    val currentRunState = _currentRunState

    private val _trackingDurationInMs = MutableStateFlow(0L)
    val trackingDurationInMs = _trackingDurationInMs.asStateFlow()

    private val timeTrackerCallback = { timeElapsed: Long ->
        _trackingDurationInMs.update { timeElapsed }
    }

    private var isFirst = true

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            if (isTracking) {
                result.locations.forEach { location ->
                    addPathPoints(location)
                    Log.d(
                        "RunAlyze",
                        "New LocationPoint : ${location.latitude}, ${location.longitude}"
                    )
                }
            }
        }
    }

    private fun postInitialValue() {
        _currentRunState.update {
            CurrentRunState()
        }
        _trackingDurationInMs.update { 0 }
    }

    private fun addPathPoints(location: Location?) = location?.let {
        val pos = LatLng(it.latitude, it.longitude)
        _currentRunState.update { state ->
            val pathPoints = state.pathPoints + PathPoint.LocationPoint(pos)
            state.copy(
                pathPoints = pathPoints,
                distanceInMeters = state.distanceInMeters.run {
                    var distance = this
                    if (pathPoints.size > 1)
                        distance += RunUtils.getDistanceBetweenPathPoints(
                            pathPoint1 = pathPoints[pathPoints.size - 1],
                            pathPoint2 = pathPoints[pathPoints.size - 2]
                        )
                    distance
                },
                speedInKMH = (it.speed * 3.6f).toBigDecimal()
                    .setScale(2, RoundingMode.HALF_UP).toFloat()
            )
        }
    }

    fun startResumeTracking() {
        if (isTracking)
            return
        if (isFirst) {
            postInitialValue()
            trackingServiceManager.startService()
            isFirst = false
        }
        isTracking = true
        timeTracker.startResumeTimer(timeTrackerCallback)
        locationTrackingManager.registerCallback(locationCallback)
    }

    private fun addEmptyPolyLine() {
        _currentRunState.update {
            it.copy(
                pathPoints = it.pathPoints + PathPoint.EmptyLocationPoint
            )
        }
    }

    fun pauseTracking() {
        isTracking = false
        locationTrackingManager.unRegisterCallback(locationCallback)
        timeTracker.pauseTimer()
        addEmptyPolyLine()
    }

    fun stop() {
        pauseTracking()
        trackingServiceManager.stopService()
        timeTracker.stopTimer()
        postInitialValue()
        isFirst = true
    }

}