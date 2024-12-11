package com.example.myapplication.presentation

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.Build
import androidx.annotation.RequiresApi
import kotlin.math.round

@RequiresApi(Build.VERSION_CODES.S)
class LinearAccelerationListener(private val viewModel: WaterViewModel) : SensorEventListener {
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {
            val x = round(event.values[0] * 1000) / 1000 // Acceleration along the x-axis
            val y = round(event.values[1] * 1000) / 1000 // Acceleration along the y-axis
            val z = round(event.values[2] * 1000) / 1000 // Acceleration along the z-axis

            viewModel.updateAccelerometerData(x, y, z)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Handle accuracy changes if needed
    }
}