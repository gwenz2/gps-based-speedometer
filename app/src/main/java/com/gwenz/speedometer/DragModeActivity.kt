package com.gwenz.speedometer

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
    private lateinit var startButton: Button
    private lateinit var settingsButton: Button
    
    private var customSpeed = 80f // km/h
    private var customDistance = 400f // meters
    
    // Countdown variables
    private var isCountdownActive = false
    private var countdownValue = 10
    private val countdownHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private var countdownRunnable: Runnable? = null
    
    // Timing variables
    private var isTimerStarted = false
    private var isTimerReady = false
    private var startTime = 0L
    private var time0to60 = 0f
    private var time0to100 = 0f
    private var timeToCustomSpeed = 0f
    
    // Speed confirmation counters (for hysteresis/smoothing)
    private var count0to60 = 0
    private var count0to100 = 0
    private var countCustomSpeed = 0
    private val CONFIRMATION_THRESHOLD = 2 // Require 2 consecutive readings
    
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
        startButton = findViewById(R.id.start_button)
        settingsButton = findViewById(R.id.settings_button)

        loadSettings()
        updateCustomLabels()
        loadBestResults()

        // Back button to return to speedometer
        val backButton = findViewById<android.widget.Button>(R.id.back_to_speedometer_button)
        backButton.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }

        // Start button - begins countdown
        startButton.setOnClickListener {
            if (!isCountdownActive && !isTimerStarted) {
                startCountdown()
            }
        }

        // Reset button - long press (3 seconds)
        var resetHandler: android.os.Handler? = null
        var resetRunnable: Runnable? = null
        
        resetButton.setOnTouchListener { view, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    resetHandler = android.os.Handler(android.os.Looper.getMainLooper())
                    resetRunnable = Runnable {
                        resetTimer()
                        view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                    }
                    resetHandler?.postDelayed(resetRunnable!!, 3000)
                    Toast.makeText(this, "Hold to reset...", Toast.LENGTH_SHORT).show()
                    true
                }
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    resetHandler?.removeCallbacks(resetRunnable!!)
                    true
                }
                else -> false
            }
        }

        settingsButton.setOnClickListener {
            showSettingsDialog()
        }

        // GPS Info button
        val gpsInfoButton = findViewById<TextView>(R.id.gps_info_button)
        gpsInfoButton.setOnClickListener {
            showGPSInfoDialog()
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

    private fun loadBestResults() {
        val prefs = getSharedPreferences("DragModeSettings", Context.MODE_PRIVATE)
        val best0to60 = prefs.getFloat("best0to60", 0f)
        val best0to100 = prefs.getFloat("best0to100", 0f)
        val bestCustomSpeed = prefs.getFloat("bestCustomSpeed", 0f)
        val bestCustomDistance = prefs.getFloat("bestCustomDistance", 0f)
        
        if (best0to60 > 0) {
            time0to60Text.text = "0-60 km/h: %.2fs ⭐%.2fs".format(0f, best0to60)
        }
        if (best0to100 > 0) {
            time0to100Text.text = "0-100 km/h: %.2fs ⭐%.2fs".format(0f, best0to100)
        }
        if (bestCustomSpeed > 0) {
            timeCustomText.text = "0-${customSpeed.toInt()} km/h: %.2fs ⭐%.2fs".format(0f, bestCustomSpeed)
        }
        if (bestCustomDistance > 0) {
            distanceCustomText.text = "${customDistance.toInt()}m: %.2fs ⭐%.2fs".format(0f, bestCustomDistance)
        }
    }

    private fun saveBestResults() {
        val prefs = getSharedPreferences("DragModeSettings", Context.MODE_PRIVATE)
        val currentBest0to60 = prefs.getFloat("best0to60", Float.MAX_VALUE)
        val currentBest0to100 = prefs.getFloat("best0to100", Float.MAX_VALUE)
        val currentBestCustomSpeed = prefs.getFloat("bestCustomSpeed", Float.MAX_VALUE)
        val currentBestCustomDistance = prefs.getFloat("bestCustomDistance", Float.MAX_VALUE)
        
        prefs.edit().apply {
            // Save if new time is better (lower) than current best
            if (time0to60 > 0 && time0to60 < currentBest0to60) {
                putFloat("best0to60", time0to60)
            }
            if (time0to100 > 0 && time0to100 < currentBest0to100) {
                putFloat("best0to100", time0to100)
            }
            if (timeToCustomSpeed > 0 && timeToCustomSpeed < currentBestCustomSpeed) {
                putFloat("bestCustomSpeed", timeToCustomSpeed)
            }
            if (timeToCustomDistance > 0 && timeToCustomDistance < currentBestCustomDistance) {
                putFloat("bestCustomDistance", timeToCustomDistance)
            }
            apply()
        }
    }

    private fun updateCustomLabels() {
        timeCustomText.text = "0-${customSpeed.toInt()} km/h: --"
        distanceCustomText.text = "${customDistance.toInt()}m: --"
    }

    private fun startCountdown() {
        isCountdownActive = true
        countdownValue = 10
        startButton.isEnabled = false
        startButton.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#666666"))
        
        statusText.setTextColor(android.graphics.Color.parseColor("#FFA500")) // Orange for countdown
        
        countdownRunnable = object : Runnable {
            override fun run() {
                if (countdownValue > 0) {
                    statusText.text = "⏱️ Starting in ${countdownValue}s..."
                    countdownValue--
                    countdownHandler.postDelayed(this, 1000) // 1 second
                } else {
                    // Countdown finished
                    isCountdownActive = false
                    isTimerReady = true
                    statusText.text = "✅ READY! Accelerate when ready..."
                    statusText.setTextColor(android.graphics.Color.parseColor("#00FF00")) // Green for ready
                    Toast.makeText(this@DragModeActivity, "GO! Timer will start when you move!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        countdownHandler.post(countdownRunnable!!)
    }

    private fun resetTimer() {
        // Save best results before resetting
        saveBestResults()
        
        // Stop countdown if active
        if (isCountdownActive) {
            countdownHandler.removeCallbacks(countdownRunnable!!)
            isCountdownActive = false
        }
        
        isTimerStarted = false
        isTimerReady = false
        startTime = 0L
        time0to60 = 0f
        time0to100 = 0f
        timeToCustomSpeed = 0f
        totalDistance = 0f
        timeToCustomDistance = 0f
        startLocation = null
        
        // Reset confirmation counters
        count0to60 = 0
        count0to100 = 0
        countCustomSpeed = 0
        
        // Re-enable start button
        startButton.isEnabled = true
        startButton.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#00FF00"))
        
        statusText.text = "Ready - Press START to begin countdown"
        statusText.setTextColor(android.graphics.Color.parseColor("#FFFF00")) // Yellow for ready
        
        // Reload and display best results
        loadBestResults()
        
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

    private fun showGPSInfoDialog() {
        val message = """
            🏁 DRAG RACING MODE INFO
            
            🎯 ACCURACY:
            • Uses high-precision GPS (100ms updates)
            • More accurate than regular speedometer
            • Best results in open areas
            
            ⏱️ HOW IT WORKS:
            1. Press START button → 10-second countdown
            2. Countdown ends → "READY! Accelerate when ready..."
            3. Timer starts when you accelerate > 1 km/h
            • Tracks: 0-60, 0-100, custom speed, distance
            • Records best times automatically
            • Status color: Orange=Countdown, Green=Running
            
            📊 MEASUREMENTS:
            • Time precision: 0.01 seconds
            • Distance precision: 0.01 meters
            • Speed updated 10x per second
            
            💡 USAGE TIPS:
            • Press START to begin 10s countdown
            • Use countdown to get into position
            • Timer captures from true 0 km/h (no GPS lag!)
            • Long-press RESET (3s) to save & reset times
            • Tap SETTINGS to customize targets
            
            ⭐ BEST TIMES:
            Your best times are saved and shown with ⭐
            Beat your records to improve!
            
            ⚠️ SAFETY WARNING:
            This is for track/closed course use only.
            Never use on public roads. Drive safely!
            
            Version 3.0 • Made by Balajedrion
            
            ☕ Support: buymeacoffee.com/Gwenvio
        """.trimIndent()
        
        AlertDialog.Builder(this)
            .setTitle("ℹ️ About Drag Racing Mode")
            .setMessage(message)
            .setPositiveButton("Got it!", null)
            .setNeutralButton("☕ Donate") { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://buymeacoffee.com/Gwenvio"))
                startActivity(intent)
            }
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
                statusText.text = "GPS: Searching... Press START when ready"
                statusText.setTextColor(android.graphics.Color.parseColor("#FFA500")) // Orange for searching
            }
        } catch (e: SecurityException) {
            statusText.text = "GPS: Permission denied"
            statusText.setTextColor(android.graphics.Color.parseColor("#FF0000")) // Red for error
        }
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val speedKmh = location.speed * 3.6f
            currentSpeedText.text = "%.1f".format(speedKmh)

            // Start timer when speed goes above 1 km/h AFTER countdown completes
            if (!isTimerStarted && isTimerReady && speedKmh > 1f) {
                isTimerStarted = true
                isTimerReady = false // Prevent restart
                startTime = System.currentTimeMillis()
                startLocation = location
                totalDistance = 0f
                statusText.text = "⏱️ Timer started!"
                statusText.setTextColor(android.graphics.Color.parseColor("#00FF00")) // Green for running
            }

            // Only track if timer has started
            if (isTimerStarted) {
                val elapsedTime = (System.currentTimeMillis() - startTime) / 1000f

                // Track distance
                startLocation?.let { start ->
                    totalDistance += location.distanceTo(start)
                    startLocation = location // Update for next calculation
                }

                // Check 0-60 km/h with smoothing
                if (time0to60 == 0f) {
                    if (speedKmh >= 60f) {
                        count0to60++
                        if (count0to60 >= CONFIRMATION_THRESHOLD) {
                            time0to60 = elapsedTime
                            val prefs = getSharedPreferences("DragModeSettings", Context.MODE_PRIVATE)
                            val best = prefs.getFloat("best0to60", 0f)
                            if (best > 0) {
                                time0to60Text.text = "0-60 km/h: %.2fs ⭐%.2fs".format(time0to60, best)
                            } else {
                                time0to60Text.text = "0-60 km/h: %.2fs".format(time0to60)
                            }
                        }
                    } else {
                        count0to60 = 0 // Reset counter if speed drops below threshold
                    }
                }

                // Check 0-100 km/h with smoothing
                if (time0to100 == 0f) {
                    if (speedKmh >= 100f) {
                        count0to100++
                        if (count0to100 >= CONFIRMATION_THRESHOLD) {
                            time0to100 = elapsedTime
                            val prefs = getSharedPreferences("DragModeSettings", Context.MODE_PRIVATE)
                            val best = prefs.getFloat("best0to100", 0f)
                            if (best > 0) {
                                time0to100Text.text = "0-100 km/h: %.2fs ⭐%.2fs".format(time0to100, best)
                            } else {
                                time0to100Text.text = "0-100 km/h: %.2fs".format(time0to100)
                            }
                        }
                    } else {
                        count0to100 = 0 // Reset counter if speed drops below threshold
                    }
                }

                // Check custom speed with smoothing
                if (timeToCustomSpeed == 0f) {
                    if (speedKmh >= customSpeed) {
                        countCustomSpeed++
                        if (countCustomSpeed >= CONFIRMATION_THRESHOLD) {
                            timeToCustomSpeed = elapsedTime
                            val prefs = getSharedPreferences("DragModeSettings", Context.MODE_PRIVATE)
                            val best = prefs.getFloat("bestCustomSpeed", 0f)
                            if (best > 0) {
                                timeCustomText.text = "0-${customSpeed.toInt()} km/h: %.2fs ⭐%.2fs".format(timeToCustomSpeed, best)
                            } else {
                                timeCustomText.text = "0-${customSpeed.toInt()} km/h: %.2fs".format(timeToCustomSpeed)
                            }
                        }
                    } else {
                        countCustomSpeed = 0 // Reset counter if speed drops below threshold
                    }
                }

                // Check custom distance
                if (timeToCustomDistance == 0f && totalDistance >= customDistance) {
                    timeToCustomDistance = elapsedTime
                    val prefs = getSharedPreferences("DragModeSettings", Context.MODE_PRIVATE)
                    val best = prefs.getFloat("bestCustomDistance", 0f)
                    if (best > 0) {
                        distanceCustomText.text = "${customDistance.toInt()}m: %.2fs (%.1f km/h) ⭐%.2fs".format(
                            timeToCustomDistance, speedKmh, best
                        )
                    } else {
                        distanceCustomText.text = "${customDistance.toInt()}m: %.2f s (%.1f km/h)".format(
                            timeToCustomDistance, speedKmh
                        )
                    }
                }

                // Update status
                statusText.text = "Running: %.2f s | Distance: %.2f m".format(elapsedTime, totalDistance)
                statusText.setTextColor(android.graphics.Color.parseColor("#00FF00")) // Keep green while running
            }
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {
            statusText.text = "GPS: Enabled - Ready"
            statusText.setTextColor(android.graphics.Color.parseColor("#FFFF00")) // Yellow for ready
        }
        override fun onProviderDisabled(provider: String) {
            statusText.text = "GPS: Disabled - Please enable GPS"
            statusText.setTextColor(android.graphics.Color.parseColor("#FF0000")) // Red for error
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
                statusText.setTextColor(android.graphics.Color.parseColor("#FF0000")) // Red for error
                Toast.makeText(this, "Location permission required", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(locationListener)
        // Clean up countdown handler
        if (countdownRunnable != null) {
            countdownHandler.removeCallbacks(countdownRunnable!!)
        }
    }
}
