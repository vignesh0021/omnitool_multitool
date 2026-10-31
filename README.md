# OmniTool: 3-in-1 Pro Android Toolkit

**OmniTool** combines three essential contractor, renovation, and DIY measurement tools into a unified, high-precision Android application built with **Kotlin** and **Jetpack Compose**:

1. 🧲 **Stud & AC Wire Detector (EMF)**
2. ⚖️ **Precision Spirit & Bubble Level (Dual-Axis & 2D Bullseye)**
3. 📐 **Smart Tape Measure & AR Ruler (Screen Caliper + Camera Rangefinder)**

---

## 📸 Reference Apps & Feature Matrix

| Feature | Reference App | OmniTool Implementation |
| :--- | :--- | :--- |
| **Metal / Stud / Wire Detection** | *Stud detector wall stud finder* | Tri-axial Magnetometer (`Sensor.TYPE_MAGNETIC_FIELD`) measuring \(\mu\text{T}\), baseline calibration, and inverse-cube proximity depth estimate. |
| **Detection Alerts** | *Stud detector wall stud finder* | Real-time audio beeper with variable pulse rate + haptic vibration pulses scaling with signal strength. |
| **Drilling Safety & Wiring Guides** | *Stud detector wall stud finder* | Interactive pre-drilling checklist + standard electrical wiring heights by country (USA/Canada, UK, Germany/EU, Australia/NZ) + wall material guide. |
| **Spirit Level Modes** | *Bubble Level (Splend Apps)* | **Horizontal (X)** 1D vial, **Vertical (Y)** 1D vial, and **Bullseye (X+Y)** 2D circular target. |
| **Level Accuracy & Viscosity** | *Bubble Level (Splend Apps)* | Configurable fluid viscosity (Low, Medium, High inertia) + Classic (±45°) and Engineer's (±10°) precision scales. |
| **Level Feedback** | *Bubble Level (Splend Apps)* | Visual neon lock highlight, audible chime ("Ding"), and haptic buzz upon reaching `< 0.3°` deviation. |
| **Physical Screen Ruler** | *Tape Measure: AR Ruler App* | True physical millimeter and 1/16-inch scale calibrated to hardware screen DPI (`displayMetrics.ydpi`) with dual sliding touch calipers. |
| **Camera Laser Distance** | *Tape Measure: AR Ruler App* | CameraX preview viewfinder with center crosshair HUD, calculating ground distance via device pitch: \(D = H / \tan(\theta)\). |
| **Area & Perimeter Measure** | *Tape Measure: AR Ruler App* | Multi-point corner pinning with Shoelace formula polygon area and total perimeter calculation. |

---

## 🛠️ Reverse Engineer Anything (REA) Integration

The project integrates [morluto/rea](https://github.com/morluto/rea) for reverse-engineering and inspection:

* **Agent Skill Installed**: Located at `~/.agents/skills/reverse-engineer-anything` with comprehensive references for Android applications, runtime observation, and evidence workflows.
* **MCP Server Configured**: Registered as `rea` in `~/.gemini/config/mcp_config.json` via `pnpm dlx rea-agents --mcp`.
* **CLI Capabilities**:
  ```bash
  # Inspect APK packages on connected devices
  pnpm dlx rea-agents list-adb-packages

  # Pull and inspect APK classes and references
  pnpm dlx rea-agents search-android-classes target.apk --query "SensorEventListener"
  ```

---

## 🏗️ Project Architecture

```
android_applications/
├── .github/workflows/
│   └── build-apk.yml          # GitHub Actions CI for building debug/release APKs
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/omnitool/app/
│   │   │   ├── MainActivity.kt        # Entry activity with WindowManager keep-screen-on
│   │   │   ├── OmniToolApp.kt         # Navigation scaffold with bottom bar & camera permissions
│   │   │   ├── data/models/           # MeasurementItem, SafetyGuideRepository, LevelSettings
│   │   │   ├── ui/
│   │   │   │   ├── theme/             # Dark contractor titanium theme with neon accents
│   │   │   │   ├── navigation/        # Bottom tab navigation items
│   │   │   │   └── screens/
│   │   │   │       ├── stud/          # MagneticGauge Canvas, StudDetectorViewModel, Screen
│   │   │   │       ├── level/         # TubularVial, BullseyeTarget, BubbleLevelViewModel
│   │   │   │       ├── measure/       # ScreenRuler, CameraViewfinder, TapeMeasureViewModel
│   │   │   │       └── guides/        # Pre-drill checklist & International wiring standards
│   │   │   └── utils/                 # AudioHapticFeedback, SensorFilter, TrigonometryUtils
│   │   └── res/                       # App strings and XML configs
│   └── src/test/                      # SensorFilterTest, TrigonometryUtilsTest
├── gradle/
│   ├── libs.versions.toml             # AGP 9.4, Kotlin 2.4, Jetpack Compose 2026.08, CameraX
│   └── wrapper/                       # Gradle wrapper 9.6.0
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 🚀 Building the APK

### Via GitHub Actions (Recommended)
Push this repository to GitHub; the `.github/workflows/build-apk.yml` workflow automatically runs unit tests and compiles `OmniTool-Debug-APK`.

### Via Gradle CLI
```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```
The resulting APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.
