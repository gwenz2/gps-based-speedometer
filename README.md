<div align="center">

# 🚗 GPS Speedometer & Drag Racing Timer

**A sleek, GPS-based speedometer with professional drag racing performance tracking**

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![API](https://img.shields.io/badge/API-28%2B-brightgreen.svg)](https://android-arsenal.com/api?level=28)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

</div>

---

## ✨ Features

### 📊 **Speedometer Mode**
Track your speed with smooth animations and comprehensive trip data

- 🎯 **Real-time speed display** with smooth 60fps animations
- 📈 **Trip statistics**: Average speed, max speed, total distance
- 🛰️ **GPS quality monitoring**: Signal strength & satellite count
- 🔄 **Long-press reset**: 3-second hold to reset any metric
- ℹ️ **Help guide** for GPS tips and usage

### 🏁 **Drag Racing Mode**
Professional performance timer with auto-start and precision tracking

- ⚡ **Auto-start timing** - Launches automatically when you accelerate
- 🎯 **Performance metrics**:
  - 0-60 km/h time
  - 0-100 km/h time  
  - Custom speed target (configurable)
  - Custom distance tracking
- ⭐ **Best time tracking** - Save and compare your personal records
- 🎨 **Color-coded status**:
  - 🔴 Error | 🟠 Searching | 🟡 Ready | 🟢 Running
- ⚙️ **Customizable targets** via settings dialog
- 🚀 **High-precision GPS** (100ms updates for accuracy)

---

## 📸 Screenshots

<p align="center">
  <img src="ss/ss1.png" width="300" alt="Speedometer Mode">
  <img src="ss/ss2.png" width="300" alt="Drag Racing Mode">
</p>

---

## 📋 Requirements

- 📱 Android 9.0+ (API 28)
- 🛰️ GPS/Location services
- ✅ Location permissions

---

## 🚀 Installation

### Download APK
1. Get the latest APK from [**Releases**](../../releases)
2. Enable "Install from Unknown Sources"
3. Install and enjoy!

### Build from Source
```bash
git clone https://github.com/gwenz2/gps-based-speedometer.git
cd gps-based-speedometer
./gradlew assembleDebug
```

---

## 📖 Quick Start

### 🎯 Speedometer Mode
1. **Launch app** → Grant location permissions
2. **Wait for GPS** (30-60s outdoors for best signal)
3. **View speed** with smooth real-time updates
4. **Track trip** - Auto-calculated average speed & distance
5. **Reset metrics** - Long-press (3s) any stat to reset

### 🏁 Drag Racing Mode
1. **Tap "→"** to switch modes
2. **Check status**: Yellow = Ready, Green = Running
3. **Stop completely** (0 km/h)
4. **Accelerate** - Timer starts automatically!
5. **Beat your records** - Best times saved with ⭐

---

## ⚙️ Technical Details

| Feature | Speedometer | Drag Racing |
|---------|-------------|-------------|
| GPS Update Rate | 1000ms | 100ms |
| Speed Precision | Whole numbers | 1 decimal |
| Time Precision | - | 2 decimals |
| Distance Tracking | ✅ Total trip | ✅ Real-time |
| Best Time Memory | - | ✅ Persistent |

---

## ⚠️ Safety Notice

> **Warning**: This app is for entertainment and track use only. Always obey traffic laws and speed limits. Never use your phone while driving. The developer assumes no liability for misuse.

---

## 🛠️ Built With

- **Kotlin** - Modern Android development
- **Android Location API** - GPS tracking
- **Material Design** - Clean, intuitive UI
- **SharedPreferences** - Persistent data storage

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Developer

**gwenz2**

⭐ Star this repo if you find it useful!

---

<div align="center">

Made with ❤️ for speed enthusiasts

</div>

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
