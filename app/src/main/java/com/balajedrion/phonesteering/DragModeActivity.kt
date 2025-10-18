package com.balajedrion.phonesteering

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.math.abs

class DragModeActivity : Activity() {

    private lateinit var locationManager: LocationManager
    
    private lateinit var currentSpeedText: TextView
    private lateinit var statusText: TextView
    private lateinit var time0to60Text: TextView
    private lateinit var time0to100Text: TextView
    private lateinit var timeCustomText: TextView
    private lateinit var distanceCustomText: TextView
    private lateinit var resetButton: Button
    private lateinit var settingsButton: Button
    
    private var customSpeed = 80f // km/h
    private var customDistance = 400f // meters
    
    // Timing variables
    private var isTimerStarted = false
    private var startTime = 0L
    private var time0to60 = 0f
    private var time0to100 = 0f
    private var timeToCustomSpeed = 0f
    
    // Distance tracking
    private var startLocation: Location? = null
    private var totalDistance = 0f
    private var timeToCustomDistance = 0f
    
    private val LOCATION_PERMISSION_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drag_mode)

        currentSpeedText = findViewById(R.id.current_speed_text)
        statusText = findViewById(R.id.status_text)
        time0to60Text = findViewById(R.id.time_0_to_60)
        time0to100Text = findViewById(R.id.time_0_to_100)
        timeCustomText = findViewById(R.id.time_custom_speed)
        distanceCustomText = findViewById(R.id.time_custom_distance)
        resetButton = findViewById(R.id.reset_button)
        settingsButton = findViewById(R.id.settings_button)

        loadSettings()
        updateCustomLabels()

        // Back button to return to speedometer
        val backButton = findViewById<android.widget.Button>(R.id.back_to_speedometer_button)
        backButton.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }

        resetButton.setOnClickListener {
            resetTimer()
        }

        settingsButton.setOnClickListener {
            showSettingsDialog()
        }

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (checkLocationPermission()) {
            startGPS()
        } else {
            requestLocationPermission()
        }
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("DragModeSettings", Context.MODE_PRIVATE)
        customSpeed = prefs.getFloat("customSpeed", 80f)
        customDistance = prefs.getFloat("customDistance", 400f)
    }

    private fun saveSettings() {
        val prefs = getSharedPreferences("DragModeSettings", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putFloat("customSpeed", customSpeed)
            putFloat("customDistance", customDistance)
            apply()
        }
    }

    private fun updateCustomLabels() {
        timeCustomText.text = "0-${customSpeed.toInt()} km/h: --"
        distanceCustomText.text = "${customDistance.toInt()}m: --"
    }

    private fun resetTimer() {
        isTimerStarted = false
        startTime = 0L
        time0to60 = 0f
        time0to100 = 0f
        timeToCustomSpeed = 0f
        totalDistance = 0f
        timeToCustomDistance = 0f
        startLocation = null
        
        statusText.text = "Ready - Waiting to start from 0 km/h"
        time0to60Text.text = "0-60 km/h: --"
        time0to100Text.text = "0-100 km/h: --"
        updateCustomLabels()
        
        Toast.makeText(this, "Timer reset", Toast.LENGTH_SHORT).show()
    }

    private fun showSettingsDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val speedLabel = TextView(this).apply {
            text = "Custom Speed Target (km/h):"
            textSize = 16f
        }
        val speedInput = EditText(this).apply {
            setText(customSpeed.toInt().toString())
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        val distanceLabel = TextView(this).apply {
            text = "Custom Distance Target (meters):"
            textSize = 16f
            setPadding(0, 20, 0, 0)
        }
        val distanceInput = EditText(this).apply {
            setText(customDistance.toInt().toString())
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        layout.addView(speedLabel)
        layout.addView(speedInput)
        layout.addView(distanceLabel)
        layout.addView(distanceInput)

        AlertDialog.Builder(this)
            .setTitle("Drag Mode Settings")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                try {
                    customSpeed = speedInput.text.toString().toFloat().coerceIn(10f, 300f)
                    customDistance = distanceInput.text.toString().toFloat().coerceIn(50f, 2000f)
                    saveSettings()
                    updateCustomLabels()
                    Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST
        )
    }

    private fun startGPS() {
        try {
            if (checkLocationPermission()) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    100, // 0.1 second for more accuracy
                    0f,
                    locationListener
                )
                statusText.text = "GPS: Searching... Start from 0 km/h"
            }
        } catch (e: SecurityException) {
            statusText.text = "GPS: Permission denied"
        }
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val speedKmh = location.speed * 3.6f
            currentSpeedText.text = "%.1f".format(speedKmh)

            // Start timer when speed goes above 1 km/h from 0
            if (!isTimerStarted && speedKmh > 1f) {
                isTimerStarted = true
                startTime = System.currentTimeMillis()
                startLocation = location
                totalDistance = 0f
                statusText.text = "⏱️ Timer started!"
            }

            // Only track if timer has started
            if (isTimerStarted) {
                val elapsedTime = (System.currentTimeMillis() - startTime) / 1000f

                // Track distance
                startLocation?.let { start ->
                    totalDistance += location.distanceTo(start)
                    startLocation = location // Update for next calculation
                }

                // Check 0-60 km/h
                if (time0to60 == 0f && speedKmh >= 60f) {
                    time0to60 = elapsedTime
                    time0to60Text.text = "0-60 km/h: %.2f s".format(time0to60)
                }

                // Check 0-100 km/h
                if (time0to100 == 0f && speedKmh >= 100f) {
                    time0to100 = elapsedTime
                    time0to100Text.text = "0-100 km/h: %.2f s".format(time0to100)
                }

                // Check custom speed
                if (timeToCustomSpeed == 0f && speedKmh >= customSpeed) {
                    timeToCustomSpeed = elapsedTime
                    timeCustomText.text = "0-${customSpeed.toInt()} km/h: %.2f s".format(timeToCustomSpeed)
                }

                // Check custom distance
                if (timeToCustomDistance == 0f && totalDistance >= customDistance) {
                    timeToCustomDistance = elapsedTime
                    distanceCustomText.text = "${customDistance.toInt()}m: %.2f s (%.1f km/h)".format(
                        timeToCustomDistance, speedKmh
                    )
                }

                // Update status
                statusText.text = "⏱️ Running: %.1f s | Distance: %.0f m".format(elapsedTime, totalDistance)
            }
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {
            statusText.text = "GPS: Enabled - Ready"
        }
        override fun onProviderDisabled(provider: String) {
            statusText.text = "GPS: Disabled - Please enable GPS"
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startGPS()
            } else {
                statusText.text = "GPS: Permission denied"
                Toast.makeText(this, "Location permission required", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(locationListener)
    }
}
