# 🚗 Speedometer App

A GPS-based speedometer and drag racing timer for Android.

## Features

### 📊 GPS Speedometer (Screen 1)
- Real-time speed display in km/h
- Smooth animated speed transitions
- Maximum speed tracking with tap-to-reset
- GPS status indicator
- Portrait-only mode for driving safety

### 🏁 Drag Racing Mode (Screen 2)
- Auto-start timer from 0 km/h
- Performance measurements:
  - **0-60 km/h** time
  - **0-100 km/h** time
  - **Custom speed target** (default: 80 km/h, configurable)
  - **Custom distance** (default: 400m, configurable)
- Configurable targets via settings dialog
- Reset functionality for multiple runs

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
git clone https://github.com/YOUR_USERNAME/PhoneSteering.git
cd PhoneSteering
./gradlew assembleDebug
```

## Usage

1. **Launch the app** - Grant location permissions when prompted
2. **GPS Speedometer** - Wait for GPS signal (go outside for best results)
3. **Switch to Drag Mode** - Tap the "→ DRAG MODE" button
4. **Start Racing** - Come to complete stop (0 km/h), timer auto-starts when you accelerate
5. **View Results** - See your 0-60, 0-100, custom speed, and distance times
6. **Customize Targets** - Tap "SETTINGS" to change speed/distance goals
7. **Reset** - Tap "RESET" to clear results and start fresh

## Permissions

- **ACCESS_FINE_LOCATION** - Required for GPS speed measurement
- **ACCESS_COARSE_LOCATION** - Required for location services

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
│   ├── java/com/balajedrion/phonesteering/
│   │   ├── MainActivity.kt           # GPS Speedometer
│   │   └── DragModeActivity.kt       # Drag Racing Timer
│   ├── res/
│   │   ├── layout/
│   │   │   ├── activity_main.xml     # Speedometer UI
│   │   │   └── activity_drag_mode.xml # Drag Mode UI
│   │   └── values/
│   └── AndroidManifest.xml
```

### Key Features Implementation
- **Smooth Speed Animation**: 60fps interpolation using Handler with 16ms delay
- **Auto-start Timer**: Detects movement from 0 km/h
- **Distance Tracking**: Uses Location.distanceTo() for accurate measurements
- **Persistent Settings**: SharedPreferences for custom targets

## Roadmap

- [ ] Add mph toggle option
- [ ] Export/share race results
- [ ] Lap timer functionality
- [ ] Historical data tracking
- [ ] Keep screen awake during use
- [ ] Dark/Light theme options

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Disclaimer

⚠️ **Safety First**: This app is intended for closed course/track use only. Always obey traffic laws and never use your phone while driving on public roads. The developer is not responsible for any accidents or violations.

## Support

If you encounter any issues or have suggestions, please [open an issue](../../issues).

## Author

**Gwen Balajedrion**

---

Made with ❤️ for car enthusiasts
