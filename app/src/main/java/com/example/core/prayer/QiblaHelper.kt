package com.example.core.prayer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

enum class SensorAccuracyLevel(val labelArabic: String) {
    HIGH("دقة ممتازة"),
    MEDIUM("دقة متوسطة"),
    LOW("دقة ضعيفة (تحتاج معايرة)"),
    UNRELIABLE("غير موثوقة (يرجى المعايرة)"),
    UNAVAILABLE("مستشعر البوصلة غير متوفر في الجهاز")
}

data class CompassReading(
    val azimuthDegrees: Float,
    val accuracy: SensorAccuracyLevel,
    val isSensorAvailable: Boolean
)

object QiblaHelper {

    const val KAABA_LATITUDE = 21.422487
    const val KAABA_LONGITUDE = 39.826206

    /**
     * Calculates the exact great-circle initial bearing from user's location to Kaaba in Mecca.
     * Returns an angle in degrees [0, 360).
     */
    fun calculateQiblaBearing(userLat: Double, userLon: Double): Float {
        val lat1 = Math.toRadians(userLat)
        val lat2 = Math.toRadians(KAABA_LATITUDE)
        val deltaLon = Math.toRadians(KAABA_LONGITUDE - userLon)

        val y = sin(deltaLon)
        val x = cos(lat1) * Math.tan(lat2) - sin(lat1) * cos(deltaLon)

        var bearing = Math.toDegrees(atan2(y, x)).toFloat()
        if (bearing < 0) {
            bearing += 360f
        }
        return bearing
    }

    /**
     * Calculates distance to Kaaba in kilometers.
     */
    fun calculateDistanceToKaabaKm(userLat: Double, userLon: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(KAABA_LATITUDE - userLat)
        val dLon = Math.toRadians(KAABA_LONGITUDE - userLon)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(userLat)) * cos(Math.toRadians(KAABA_LATITUDE)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadiusKm * c
    }
}

class CompassSensorManager(context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    val isAvailable: Boolean = rotationSensor != null || (accelerometer != null && magnetometer != null)

    fun getCompassUpdates(): Flow<CompassReading> = callbackFlow {
        if (!isAvailable) {
            trySend(CompassReading(0f, SensorAccuracyLevel.UNAVAILABLE, false))
            channel.close()
            return@callbackFlow
        }

        var lastAzimuth = 0f
        var currentAccuracy = SensorAccuracyLevel.MEDIUM

        val listener = object : SensorEventListener {
            val rotationMatrix = FloatArray(9)
            val orientation = FloatArray(3)
            val gravity = FloatArray(3)
            val geomagnetic = FloatArray(3)
            var hasGravity = false
            var hasGeomagnetic = false

            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    var azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    if (azimuth < 0) azimuth += 360f

                    // Smooth with low-pass filter
                    lastAzimuth = smoothAzimuth(lastAzimuth, azimuth)
                    trySend(CompassReading(lastAzimuth, currentAccuracy, true))
                } else {
                    if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                        System.arraycopy(event.values, 0, gravity, 0, 3)
                        hasGravity = true
                    } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                        System.arraycopy(event.values, 0, geomagnetic, 0, 3)
                        hasGeomagnetic = true
                    }

                    if (hasGravity && hasGeomagnetic) {
                        val success = SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)
                        if (success) {
                            SensorManager.getOrientation(rotationMatrix, orientation)
                            var azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                            if (azimuth < 0) azimuth += 360f
                            lastAzimuth = smoothAzimuth(lastAzimuth, azimuth)
                            trySend(CompassReading(lastAzimuth, currentAccuracy, true))
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                currentAccuracy = when (accuracy) {
                    SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> SensorAccuracyLevel.HIGH
                    SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> SensorAccuracyLevel.MEDIUM
                    SensorManager.SENSOR_STATUS_ACCURACY_LOW -> SensorAccuracyLevel.LOW
                    else -> SensorAccuracyLevel.UNRELIABLE
                }
            }
        }

        if (rotationSensor != null) {
            sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(listener, magnetometer, SensorManager.SENSOR_DELAY_UI)
        }

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }

    private fun smoothAzimuth(current: Float, target: Float): Float {
        var diff = target - current
        while (diff < -180f) diff += 360f
        while (diff > 180f) diff -= 360f
        return (current + 0.2f * diff + 360f) % 360f
    }
}
