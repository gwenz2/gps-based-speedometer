# 🚗 Speedometer App

A GPS-based speedometer and drag racing timer for Android.

## Features

### 📊 GPS Speedometer (Screen 1)
- Real-time speed display in km/h with mph reference
- **Smooth animated speed transitions** (60fps interpolation)
- Maximum speed tracking with **3-second long-press reset**
- GPS status indicator with accuracy display
- **Portrait-only mode** for driving safety
- **Screen stays awake** during use
- Responsive design for all screen sizes

### 🏁 Drag Racing Mode (Screen 2)
- **Auto-start timer** from 0 km/h (no manual button needed)
- Performance measurements:
  - **0-60 km/h** time
  - **0-100 km/h** time
  - **Custom speed target** (default: 80 km/h, configurable)
  - **Custom distance** (default: 400m, configurable)
- **Best time tracking** with ⭐ indicator (persistent across sessions)
- Current time vs. best time comparison
- Configurable targets via settings dialog
- **3-second long-press reset** to prevent accidental resets
- Distance tracking with real-time display
- **Screen stays awake** during racing

## Screenshots

<!-- Add screenshots here when available -->

## Requirements

- Android 9.0 (API 28) or higher
- GPS/Location services enabled
- Location permission granted

## Installation

### From Release
1. Download the latest APK from [Releases](../../releases)
2. Enable "Install from Unknown Sources" in your Android settings
3. Open the APK file and install

### Build from Source
1. Clone this repository
2. Open in Android Studio
3. Sync Gradle files
4. Build and run on your device

```bash
git clone https://github.com/gwenz2/gps-based-speedometer.git
cd gps-based-speedometer
./gradlew assembleDebug
```

## Usage

### GPS Speedometer Mode
1. **Launch the app** - Grant location permissions when prompted
2. Wait for GPS signal (go outside for best results)
3. View your current speed with smooth animations
4. **Reset max speed** - Hold the max speed text for 3 seconds
5. **Switch modes** - Tap the "→ DRAG MODE" button

### Drag Racing Mode
1. Tap "→ DRAG MODE" from the speedometer screen
2. **Come to complete stop** (0 km/h) - timer is ready
3. **Accelerate** - Timer starts automatically when you move
4. Watch your times populate as you hit each milestone
5. **View best times** - Your personal records show with ⭐ symbol
6. **Customize targets** - Tap "SETTINGS" to change speed/distance goals
7. **Reset for next run** - Hold "RESET" button for 3 seconds
8. **Go back** - Tap "← SPEEDOMETER" button to return

## Permissions

- **ACCESS_FINE_LOCATION** - Required for GPS speed measurement
- **ACCESS_COARSE_LOCATION** - Required for location services
- **WAKE_LOCK** - Keeps screen on during use (no timeout)

## Technology Stack

- **Language**: Kotlin
- **Min SDK**: 28 (Android 9.0)
- **Target SDK**: 36
- **Architecture**: Single Activity per screen
- **Location**: Android LocationManager API
- **UI**: Material Design 3

## Development

### Project Structure
```
app/
├── src/main/
│   ├── java/com/gwenz/speedometer/
│   │   ├── MainActivity.kt           # GPS Speedometer
│   │   └── DragModeActivity.kt       # Drag Racing Timer
│   ├── res/
│   │   ├── layout/
│   │   │   ├── activity_main.xml     # Speedometer UI
│   │   │   └── activity_drag_mode.xml # Drag Mode UI
│   │   ├── mipmap-*/                 # App icons
│   │   └── values/
│   └── AndroidManifest.xml
└── build.gradle.kts
```

### Key Features Implementation
- **Smooth Speed Animation**: 60fps interpolation using Handler with 16ms delay and 0.3 factor
- **Auto-start Timer**: Detects movement from 0 km/h (>1 km/h threshold)
- **Distance Tracking**: Uses Location.distanceTo() for accurate measurements
- **Persistent Settings**: SharedPreferences for custom targets and best times
- **Long Press Reset**: 3-second hold with visual feedback and haptic response
- **Best Time Management**: Automatically saves and compares personal records
- **Screen Wake Lock**: Keeps display on using keepScreenOn attribute
- **Responsive UI**: Auto-sizing text and ScrollView for various screen sizes

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Disclaimer

⚠️ **Safety First**: This app is intended for closed course/track use only. Always obey traffic laws and never use your phone while driving on public roads. The developer is not responsible for any accidents or violations.

## Author

**Gwen** (@gwenz2)

## Repository

[https://github.com/gwenz2/gps-based-speedometer](https://github.com/gwenz2/gps-based-speedometer)

---

Made with ❤️ for car enthusiasts and track day warriors
