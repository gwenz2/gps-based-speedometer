package com.gwenz.speedometer

import android.app.Application
import com.startapp.sdk.adsbase.StartAppSDK

class SpeedometerApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Start.io SDK
        StartAppSDK.init(this, "209173609", false)

        // Production Mode - Real Ads
        // Test ads disabled - you'll now see real ads that generate revenue!
    }
}
