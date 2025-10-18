package com.gwenz.speedometer

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import kotlin.math.abs
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.math.abs
import android.app.AlertDialog

class MainActivity : Activity() {

    private lateinit var locationManager: LocationManager
    
    private lateinit var gpsSpeedText: TextView
    private lateinit var maxSpeedText: TextView
    private lateinit var gpsAccuracyText: TextView
    private lateinit var gpsSignalText: TextView
    private lateinit var avgSpeedText: TextView
    private lateinit var tripDistanceText: TextView
    
    private var maxSpeed = 0.0f
    private val LOCATION_PERMISSION_REQUEST = 1
    
    // Trip tracking variables
    private var totalDistance = 0.0 // in meters
    private var totalTime = 0L // in milliseconds
    private var lastLocation: Location? = null
    private var tripStartTime = 0L
    
    // For smooth speed animation
    private var currentDisplaySpeed = 0f
    private var targetSpeed = 0f
    private val speedUpdateHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val speedAnimationRunnable = object : Runnable {
        override fun run() {
            if (abs(currentDisplaySpeed - targetSpeed) > 0.1f) {
                // Smooth interpolation
                currentDisplaySpeed += (targetSpeed - currentDisplaySpeed) * 0.3f
                gpsSpeedText.text = "%03.0f".format(currentDisplaySpeed)
                speedUpdateHandler.postDelayed(this, 16) // ~60fps
            } else {
                currentDisplaySpeed = targetSpeed
                gpsSpeedText.text = "%03.0f".format(currentDisplaySpeed)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gpsSpeedText = findViewById(R.id.gps_speed_text)
        maxSpeedText = findViewById(R.id.max_speed_text)
        gpsAccuracyText = findViewById(R.id.gps_accuracy_text)
        gpsSignalText = findViewById(R.id.gps_signal_text)
        avgSpeedText = findViewById(R.id.avg_speed_text)
        tripDistanceText = findViewById(R.id.trip_distance_text)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        
        // Initialize trip start time
        tripStartTime = System.currentTimeMillis()

        // Button to go to drag mode
        val gotoDragModeButton = findViewById<android.widget.Button>(R.id.goto_drag_mode_button)
        gotoDragModeButton.setOnClickListener {
            val intent = Intent(this, DragModeActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }

        // Make max speed text long-pressable to reset (3 seconds)
        var resetHandler: android.os.Handler? = null
        var resetRunnable: Runnable? = null
        
        maxSpeedText.setOnTouchListener { view, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    resetHandler = android.os.Handler(android.os.Looper.getMainLooper())
                    resetRunnable = Runnable {
                        maxSpeed = 0.0f
                        maxSpeedText.text = "MAX SPEED: 0 KM/H"
                        Toast.makeText(this, "Max speed reset", Toast.LENGTH_SHORT).show()
                        view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                    }
                    resetHandler?.postDelayed(resetRunnable!!, 3000)
                    Toast.makeText(this, "Hold to reset max speed...", Toast.LENGTH_SHORT).show()
                    true
                }
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    resetHandler?.removeCallbacks(resetRunnable!!)
                    true
                }
                else -> false
            }
        }
        
        // Make trip stats long-pressable to reset trip
        var tripResetHandler: android.os.Handler? = null
        var tripResetRunnable: Runnable? = null
        
        val tripResetListener = android.view.View.OnTouchListener { view, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    tripResetHandler = android.os.Handler(android.os.Looper.getMainLooper())
                    tripResetRunnable = Runnable {
                        // Reset trip data
                        totalDistance = 0.0
                        totalTime = 0L
                        lastLocation = null
                        tripStartTime = System.currentTimeMillis()
                        avgSpeedText.text = "AVG SPEED: 0 KM/H"
                        tripDistanceText.text = "DISTANCE: 0.0 KM"
                        Toast.makeText(this, "Trip data reset", Toast.LENGTH_SHORT).show()
                        view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                    }
                    tripResetHandler?.postDelayed(tripResetRunnable!!, 3000)
                    Toast.makeText(this, "Hold to reset trip...", Toast.LENGTH_SHORT).show()
                    true
                }
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    tripResetHandler?.removeCallbacks(tripResetRunnable!!)
                    true
                }
                else -> false
            }
        }
        
        avgSpeedText.setOnTouchListener(tripResetListener)
        tripDistanceText.setOnTouchListener(tripResetListener)

        // GPS Info button
        val gpsInfoButton = findViewById<TextView>(R.id.gps_info_button)
        gpsInfoButton.setOnClickListener {
            showGPSInfoDialog()
        }

        // Request location permissions
        if (checkLocationPermission()) {
            startGPS()
        } else {
            requestLocationPermission()
        }
    }

    private fun showGPSInfoDialog() {
        val message = """
            📡 GPS SPEEDOMETER INFO
            
            🎯 ACCURACY:
            • Uses real GPS satellite data
            • Accuracy: typically ±5-10 meters
            • Best accuracy outdoors with clear sky
            
            ⏱️ UPDATE RATE:
            • Speed updates every 1 second
            • Smooth animation for visual comfort
            • May show slight delay (normal behavior)
            
            📊 FEATURES:
            • Real-time speed tracking
            • Maximum speed recording
            • Average speed calculation
            • Trip distance measurement
            • GPS accuracy & signal monitoring
            
            💡 TIPS:
            • Wait for GPS lock (may take 30-60s)
            • Works best in open areas
            • Speed shown may lag 1-2 seconds (GPS limitation)
            • Long-press stats to reset
            
            ⚠️ DISCLAIMER:
            This app is for informational purposes only. 
            Always follow traffic laws and drive safely.
            
            Version 1.4 • Made by Balajedrion
        """.trimIndent()
        
        AlertDialog.Builder(this)
            .setTitle("ℹ️ About GPS Speedometer")
            .setMessage(message)
            .setPositiveButton("Got it!", null)
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
                    1000, // 1 second
                    0f,   // 0 meters
                    locationListener
                )
                // GPS status will be shown via gpsSignalText
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_LONG).show()
        }
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            // Speed in m/s, convert to km/h
            val speedKmh = location.speed * 3.6f
            val speedMph = location.speed * 2.23694f
            
            // Set target speed for smooth animation
            targetSpeed = speedKmh
            speedUpdateHandler.removeCallbacks(speedAnimationRunnable)
            speedUpdateHandler.post(speedAnimationRunnable)
            
            // Update GPS accuracy
            if (location.hasAccuracy()) {
                val accuracy = location.accuracy
                gpsAccuracyText.text = "GPS Accuracy: ±%.1f m".format(accuracy)
            } else {
                gpsAccuracyText.text = "GPS Accuracy: --"
            }
            
            // Update GPS signal strength (based on number of satellites if available)
            val extras = location.extras
            if (extras != null && extras.containsKey("satellites")) {
                val satellites = extras.getInt("satellites")
                gpsSignalText.text = "GPS Signal: %d satellites".format(satellites)
            } else {
                // Estimate signal quality based on accuracy
                val signalQuality = when {
                    !location.hasAccuracy() -> "Unknown"
                    location.accuracy < 5 -> "Excellent"
                    location.accuracy < 10 -> "Good"
                    location.accuracy < 20 -> "Fair"
                    else -> "Poor"
                }
                gpsSignalText.text = "GPS Signal: $signalQuality"
            }
            
            // Track max speed
            if (speedKmh > maxSpeed) {
                maxSpeed = speedKmh
                maxSpeedText.text = "MAX SPEED: %.0f KM/H".format(maxSpeed)
            }
            
            // Calculate trip distance
            if (lastLocation != null) {
                val distance = lastLocation!!.distanceTo(location) // in meters
                // Only add distance if speed > 1 km/h (to avoid GPS drift when stationary)
                if (speedKmh > 1.0f && distance < 100) { // Also filter out GPS jumps > 100m
                    totalDistance += distance
                    val distanceKm = totalDistance / 1000.0
                    tripDistanceText.text = "DISTANCE: %.2f KM".format(distanceKm)
                }
            }
            lastLocation = location
            
            // Calculate average speed
            totalTime = System.currentTimeMillis() - tripStartTime
            if (totalTime > 0 && totalDistance > 0) {
                // Average speed = total distance / total time
                val avgSpeedMps = totalDistance / (totalTime / 1000.0) // m/s
                val avgSpeedKmh = avgSpeedMps * 3.6 // km/h
                avgSpeedText.text = "AVG SPEED: %.1f KM/H".format(avgSpeedKmh)
            }
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {
            // GPS enabled - signal status will be shown via gpsSignalText
        }
        override fun onProviderDisabled(provider: String) {
            gpsSignalText.text = "GPS Signal: Disabled"
            Toast.makeText(this@MainActivity, "Please enable GPS", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Location permission required for GPS speed", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(locationListener)
        speedUpdateHandler.removeCallbacks(speedAnimationRunnable)
    }
}
