<div align="center">

# 🚗 GPS Speedometer & Drag Racing Timer

**A sleek, GPS-based speedometer with professional drag racing performance tracking**

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![API](https://img.shields.io/badge/API-28%2B-brightgreen.svg)](https://android-arsenal.com/api?level=28)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Version](https://img.shields.io/badge/Version-3.0-orange.svg)](../../releases)

</div>

---

## ✨ Features

### 📊 **Speedometer Mode**
Real-time speed tracking with comprehensive trip data

- 🎯 **Real-time speed display** (200ms GPS updates)
- 📈 **Trip statistics**: Average speed, max speed, total distance
- 🛰️ **GPS quality monitoring**: Signal strength & accuracy
- 🔄 **Long-press reset**: 3-second hold to reset any metric
- ℹ️ **Help guide** with GPS tips and usage

### 🏁 **Drag Racing Mode**
Professional performance timer with countdown start

- ⏱️ **10-second countdown** - Get into position before timer starts
- 🎯 **Performance metrics**:
  - 0-60 km/h time
  - 0-100 km/h time  
  - Custom speed target (configurable)
  - Custom distance tracking (e.g., 400m)
- ⭐ **Best time tracking** - Saves and displays your personal records
- 🎨 **Color-coded status**:
  - 🔴 Error | 🟠 Countdown | 🟢 Running
- ⚙️ **Customizable targets** via settings dialog
- 🚀 **High-precision GPS** (100ms updates for racing accuracy)
- 🏆 **No GPS lag** - Countdown eliminates early speed measurement issues

---

## 📸 Screenshots

<p align="center">
  <img src="ss/ss1.png" width="300" alt="Speedometer Mode">
  <img src="ss/ss2.png" width="300" alt="Drag Racing Mode">
</p>

---

## 🆕 What's New in v3.0

- ⏱️ **Countdown feature** - 10-second countdown in drag mode before timer starts
- 🐛 **Fixed average speed** - No longer drops when stationary
- 🚫 **Ad-free** - Completely removed all advertisements
- ⚡ **Real-time GPS** - 200ms updates for instant speed display
- ☕ **Support link** - Donation option for those who want to help

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
2. **Press START** button to begin 10-second countdown
3. **Countdown finishes** → Status shows "READY! Accelerate when ready..."
4. **Accelerate from stop** - Timer captures from true 0 km/h
5. **Beat your records** - Best times saved with ⭐
6. **Long-press RESET (3s)** to save and clear times

---

## ⚙️ Technical Details

| Feature | Speedometer | Drag Racing |
|---------|-------------|-------------|
| GPS Update Rate | 200ms (5 Hz) | 100ms (10 Hz) |
| Speed Precision | Whole numbers | 1 decimal |
| Time Precision | - | 2 decimals |
| Distance Tracking | ✅ Moving only | ✅ Real-time |
| Best Time Memory | - | ✅ Persistent |
| Countdown Timer | - | ✅ 10 seconds |

---

## 💝 Support

If you find this app useful, consider buying me a coffee!

☕ **[Buy Me a Coffee](https://buymeacoffee.com/Gwenvio)**

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

**gwenz2** | [@Gwenvio](https://buymeacoffee.com/Gwenvio)

⭐ Star this repo if you find it useful!

---

<div align="center">

Made with ❤️ for speed enthusiasts

**v3.0** - Ad-free • Countdown Timer • Real-time Updates

</div>
