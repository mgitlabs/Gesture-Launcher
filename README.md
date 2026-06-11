# Gesture Launcher 🚀

**Gesture Launcher** is a production-ready Android utility application built in Java that leverages the **Android Accessibility Service API** to intercept independent touch boundaries at the very top (Status Bar Area) and bottom (Navigation Bar Area) of the screen.

It enables users to trigger custom global system actions or launch target applications instantly from any screen using multi-gesture inputs.

<p align="center">
  <img src="assets/icon.png" width="120" height="120" alt="Gesture Launcher Icon">
</p>

---

## ✨ Features

### Dual-Bar Interception Architecture
- Independent, isolated monitoring environments for both the Top Status Bar and Bottom Navigation Bar.

### Granular Touch Boundary Tuning
- Multi-directional adjustments (Top height, Left margin, Right margin).
- Floating-point density scaling from **0.1% to 5%** via SeekBars.

### Comprehensive Gesture Matrix

Separate configurations for 5 distinct touch event states per bar:

- **Single Tap** (configurable validation delays)
- **Double Tap** (custom detection windows)
- **Long Press** (custom millisecond thresholds)
- **Swipe Left** (horizontal delta calculations)
- **Swipe Right** (horizontal delta calculations)

### Isolated Timing Infrastructure
- Independent gesture recognition timers for Top and Bottom gesture regions.
- Persisted using SharedPreferences.

### Robust Action Routing

#### Launch Applications
- Asynchronous Installed App Picker.
- Search and map installed packages easily.

#### System Shortcuts
- Take Screenshot
- Expand Notifications
- Lock Screen

#### Hardware Controls
- Display brightness adjustment
- Audio stream control

### Material Design 3 Interface
- TabLayout + ViewPager2 architecture.
- Dynamic Day/Night theme switching.
- Material3 components and exposed dropdown menus.

### TalkBack-Safe Implementation
- Designed without touch exploration flags.
- Prevents touch-freezing and dual-finger lock issues.

---

## 📸 Screenshots

<table align="center">
<tr>
<td width="33%" align="center">
<img src="assets/Screenshot_20260611-052221.png" width="100%" alt="Screenshot 1"/>
</td>
<td width="33%" align="center">
<img src="assets/Screenshot_20260611-052230.png" width="100%" alt="Screenshot 2"/>
</td>
<td width="33%" align="center">
<img src="assets/Screenshot_20260611-052235.png" width="100%" alt="Screenshot 3"/>
</td>
</tr>

<tr>
<td width="33%" align="center">
<img src="assets/Screenshot_20260611-052246.png" width="100%" alt="Screenshot 4"/>
</td>
<td width="33%" align="center">
<img src="assets/Screenshot_20260611-052305.png" width="100%" alt="Screenshot 5"/>
</td>
<td width="33%" align="center">
<img src="assets/Screenshot_20260611-052316.png" width="100%" alt="Screenshot 6"/>
</td>
</tr>
</table>

---

## 🛠️ Project Structure

```text
GestureLauncher/
│
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/mahadi/gesturelauncher/
│   │   │   │   ├── MainActivity.java
│   │   │   │   ├── NavbarAccessibilityService.java
│   │   │   │   ├── fragments/
│   │   │   │   │   ├── BottomBarFragment.java
│   │   │   │   │   ├── TopBarFragment.java
│   │   │   │   │   └── GlobalSettingsFragment.java
│   │   │   │   └── adapters/
│   │   │   │       └── AppPickerAdapter.java
│   │   │   │
│   │   │   ├── res/
│   │   │   │   ├── drawable/
│   │   │   │   │   ├── ic_gesture_logo.xml
│   │   │   │   │   └── ic_theme_toggle.xml
│   │   │   │   │
│   │   │   │   ├── layout/
│   │   │   │   │   ├── activity_main.xml
│   │   │   │   │   ├── fragment_bottom_bar.xml
│   │   │   │   │   ├── fragment_top_bar.xml
│   │   │   │   │   └── fragment_global_settings.xml
│   │   │   │   │
│   │   │   │   └── xml/
│   │   │   │       └── accessibility_service_config.xml
│   │   │   │
│   │   │   └── AndroidManifest.xml
│   │   │
│   │   └── build.gradle
```

---

## 🔒 Permissions & Safety

This application requires the following permissions:

### BIND_ACCESSIBILITY_SERVICE
Used to:

- Register edge-bound gesture regions.
- Execute Android global actions through Accessibility APIs.

### WRITE_SETTINGS
Requested only when:

- A gesture is configured to control display brightness.

---

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/mgitlabs/Gesture-Launcher.git
```

### 2. Open in Android Studio

Recommended:

- Android Studio Jellyfish or newer.

### 3. Configure SDK

Use:

- Compile SDK: API 34+
- Target SDK: API 34+

### 4. Build and Install

Compile and deploy either:

- Debug APK
- Release APK

### 5. Enable Accessibility Service

Navigate to:

```text
Settings
└── Accessibility
    └── Installed Apps
        └── Gesture Launcher
```

Enable the service and grant any requested permissions.

---

## 📄 License

This project is released under the license specified in this repository.
