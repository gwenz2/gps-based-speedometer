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

class MainActivity : Activity() {

    private lateinit var locationManager: LocationManager
    
    private lateinit var gpsSpeedText: TextView
    private lateinit var gpsStatusText: TextView
    private lateinit var maxSpeedText: TextView
    
    private var maxSpeed = 0.0f
    private val LOCATION_PERMISSION_REQUEST = 1
    
    // For smooth speed animation
    private var currentDisplaySpeed = 0f
    private var targetSpeed = 0f
    private val speedUpdateHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val speedAnimationRunnable = object : Runnable {
        override fun run() {
            if (abs(currentDisplaySpeed - targetSpeed) > 0.1f) {
                // Smooth interpolation
                currentDisplaySpeed += (targetSpeed - currentDisplaySpeed) * 0.3f
                gpsSpeedText.text = "%.1f".format(currentDisplaySpeed)
                speedUpdateHandler.postDelayed(this, 16) // ~60fps
            } else {
                currentDisplaySpeed = targetSpeed
                gpsSpeedText.text = "%.1f".format(currentDisplaySpeed)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gpsSpeedText = findViewById(R.id.gps_speed_text)
        gpsStatusText = findViewById(R.id.gps_status_text)
        maxSpeedText = findViewById(R.id.max_speed_text)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

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
                        maxSpeedText.text = "Max: 0.0 km/h"
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

        // Request location permissions
        if (checkLocationPermission()) {
            startGPS()
        } else {
            requestLocationPermission()
        }
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
                gpsStatusText.text = "GPS: Searching for signal..."
            }
        } catch (e: SecurityException) {
            gpsStatusText.text = "GPS: Permission denied"
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
            
            gpsStatusText.text = "GPS: Active (%.1f mph)".format(speedMph)
            
            // Track max speed
            if (speedKmh > maxSpeed) {
                maxSpeed = speedKmh
                maxSpeedText.text = "Max: %.1f km/h".format(maxSpeed)
            }
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {
            gpsStatusText.text = "GPS: Enabled"
        }
        override fun onProviderDisabled(provider: String) {
            gpsStatusText.text = "GPS: Disabled - Please enable GPS"
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
                gpsStatusText.text = "GPS: Permission denied"
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
