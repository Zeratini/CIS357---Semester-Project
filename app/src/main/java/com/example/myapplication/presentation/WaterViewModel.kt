package com.example.myapplication.presentation

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.VibratorManager
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.S)
class WaterViewModel(application: Application) : AndroidViewModel(application) {
    val stvm: StorageViewModel by lazy { StorageViewModel(application) }
    //Sensor Manager
    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
    private val vibrator = vibratorManager.defaultVibrator

    // Accelerometer
    private val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    private val accelerometerListener = LinearAccelerationListener(this)

    private val _accelerometerData = MutableStateFlow(Triple(0f, 0f, 0f))
    val accelerometerData: StateFlow<Triple<Float, Float, Float>> = _accelerometerData.asStateFlow()

    // Rotation Sensor
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val rotationListener = RotationListener(this)

    private val _rotationData = MutableStateFlow(Triple(0f, 0f, 0f))
    val rotationData: StateFlow<Triple<Float, Float, Float>> = _rotationData.asStateFlow()

    // View Model Variables (Non-sensor)
    private val _drank = MutableLiveData<Int>()
    val drank: LiveData<Int> get() = _drank

    // Drinking booleans
    private val _glassObtained = MutableLiveData<Boolean>(false)
    val glassObtained: LiveData<Boolean> get() = _glassObtained

    private val _glassRaised = MutableLiveData<Boolean>(false)
    val glassRaised: LiveData<Boolean> get() = _glassRaised

    // Drink empty boolean
    private val _glassEmpty = MutableLiveData<Boolean>(false)
    val glassEmpty: LiveData<Boolean> get() = _glassEmpty

    // Finish Drinking booleans
    private val _glassLowered = MutableLiveData<Boolean>(false)
    val glassLowered: LiveData<Boolean> get() = _glassLowered

    private val _finishedDrinking = MutableLiveData<Boolean>(false)
    val finishedDrinking: LiveData<Boolean> get() = _finishedDrinking

    var startTime: Long = 0

    /** Superclass Functions */
    init {
        _drank.postValue(0)
        viewModelScope.launch(Dispatchers.IO) {
            _drank.postValue(stvm.getInt("drank", 0))
        }
        checkDay()

        sensorManager.registerListener(accelerometerListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(rotationListener, rotationSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(accelerometerListener)
        sensorManager.unregisterListener(rotationListener)
    }

    /** Basic Functionality Functions */
    fun increment() {
        _drank.postValue(_drank.value?.plus(1))
        viewModelScope.launch(Dispatchers.IO) {
            stvm.storeInt("drank", _drank.value!!+1)
        }
    }

    fun decrement() {
        if (_drank.value != 0) {
            _drank.postValue(_drank.value?.minus(1))
        }
    }

    // If Yes is chosen in dialog, increment and reset
    fun autoIncrement() {
        _drank.postValue(_drank.value?.plus(1))
        viewModelScope.launch(Dispatchers.IO) {
            stvm.storeInt("drank", _drank.value!!+1)
        }
        _finishedDrinking.postValue(false)
    }

    // If No is chosen in dialog, reset.
    fun reset() {
        _glassObtained.postValue(false)
        _glassRaised.postValue(false)
        _glassEmpty.postValue(false)
        _glassLowered.postValue(false)
        _finishedDrinking.postValue(false)
    }

    fun notifyOfConfirmation() {
        val timings = longArrayOf(0, 50, 100, 50) // Off, vibrate, off, vibrate
        val amplitudes = intArrayOf(0, 255, 0, 255) // Strength of vibration
        val vibrationEffect = VibrationEffect.createWaveform(timings, amplitudes, -1) // Repeat indefinitely
        vibrator.vibrate(vibrationEffect)
    }

    fun checkDay() {
        val today = LocalDate.now()
        val lastDay = stvm.getLong("lastDay", 0)
        val lastDayDate = LocalDate.ofEpochDay(lastDay)
        if (today.isAfter(lastDayDate)) {
            stvm.storeInt("drank", 0)
            stvm.storeLong("lastDay", today.toEpochDay())
        }
    }

    /** Sensor Functions */
    fun updateRotationData(yaw: Float, pitch: Float, roll: Float) {
        // How close to the exact should the values be? Roll is more variable than pitch in this case.
        var pitchTolerance = 0.3f
        var rollTolerance = 0.5f

        // Hand on drink, on level surface:
        if ((pitch >= 1.5f - pitchTolerance && pitch <= 1.5f + pitchTolerance) || (pitch >= -1.5f - pitchTolerance && pitch <= -1.5f + pitchTolerance)) {
            if (roll >= 0f - rollTolerance && roll <= 0f + rollTolerance) {

                // Grabbing drink
                if (_glassObtained.value == false) {
                    _glassObtained.postValue(true)
                    startTime = SystemClock.elapsedRealtime()
                    println("Glass Obtained")
                }

                // Releasing drink after drinking
                if (_glassLowered.value == true) {
                    _finishedDrinking.postValue(true)
                    _glassObtained.postValue(false)
                    _glassRaised.postValue(false)
                    _glassEmpty.postValue(false)
                    _glassLowered.postValue(false)

                    notifyOfConfirmation()

                    println("Finished Drink")
                }
            }
        }

        // Variability switches, now that the movement is perpendicular.
        pitchTolerance = 0.5f
        rollTolerance = 0.3f

        // Pouring drink into mouth (Drink perpendicular now)
        if (_glassRaised.value == true && ((roll >= 1.5f - rollTolerance && roll <= 1.5f + rollTolerance) || (roll >= -1.5f - rollTolerance && roll <= -1.5f + rollTolerance))) {
            if (pitch >= 0f - pitchTolerance && pitch <= 0f + pitchTolerance) {
                _glassEmpty.postValue(true)
            }
        }

        // If the timer has expired (3 seconds since glass grabbed) reset glassRaised.
        if (_glassObtained.value == true && _glassRaised.value == true && _glassEmpty.value == false && SystemClock.elapsedRealtime() - startTime >= 3000) {
            _glassRaised.postValue(false)
            startTime = SystemClock.elapsedRealtime()
        }

        _rotationData.value = Triple(yaw, pitch, roll)
    }

    fun updateAccelerometerData(x: Float, y: Float, z: Float) {
        if (y >= 1.0f) {
            // After getting the drink, lift it
            if (_glassObtained.value == true) {
                startTime = SystemClock.elapsedRealtime() // Reset clock.
                _glassRaised.postValue(true)
            }
        } else if (y <= -1.0f) {
            // After drinking, lower the glass
            if (_glassEmpty.value == true) {
                _glassLowered.postValue(true)
            }

            // Undo drink if lowered before drank.
            if (_glassObtained.value == true && _glassRaised.value == true && _glassEmpty.value == false) {
                _glassRaised.postValue(false)
                _glassObtained.postValue(false)
            }
        }

        // If the timer has expired (3 seconds since glass grabbed) reset glassObtained.
        if (_glassObtained.value == true && _glassRaised.value == false && SystemClock.elapsedRealtime() - startTime >= 3000) {
            _glassObtained.postValue(false)
        // Timing on Glass Empty is generally going to be higher, since more time is spent drinking than raising or lowering the glass. Per my testing, 15 seconds is a safe value for a large drink.
        } else if (_glassObtained.value == true && _glassRaised.value == true && _glassEmpty.value == true && _glassLowered.value == false && SystemClock.elapsedRealtime() - startTime >= 15000) {
            _glassEmpty.postValue(false)
            // Consequently, if spending longer than 15 seconds on glass empty, likely that it wasn't actually water, so reset everything.
            _glassRaised.postValue(false)
            _glassObtained.postValue(false)
        }

        _accelerometerData.value = Triple(x, y, z)
    }
}