package com.example.myapplication.presentation

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.annotation.RequiresApi
import kotlin.math.round

@RequiresApi(Build.VERSION_CODES.S)
class RotationListener(private val viewModel: WaterViewModel) : SensorEventListener {
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            // Translate event into a rotation matrix
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

            // Translate rotation matrix into orientation angles
            val orientationAngles = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)

            // Round all values to a tenth.
            val yaw = round(orientationAngles[0] * 10) / 10 // Yaw (Rotation around Z axis) // Note: I work with these values at work fairly often, so the description is more for the reader than for me.
            val pitch = round(orientationAngles[1] * 10) / 10 // Pitch (Rotation around X axis)
            val roll = round(orientationAngles[2] * 10) / 10 // Roll (Rotation around Y axis)

            viewModel.updateRotationData(yaw, pitch, roll)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Handle accuracy changes if needed
    }
}