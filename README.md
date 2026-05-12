<div align="center">

# 🪄 DoMagic

**Root any Android phone using another Android phone — no PC needed.**

[![License: AGPL-3.0](https://img.shields.io/badge/License-AGPL%20v3-purple.svg)](https://www.gnu.org/licenses/agpl-3.0)
[![Status: Alpha](https://img.shields.io/badge/Status-Alpha%200.1-orange.svg)]()
[![Platform: Android](https://img.shields.io/badge/Platform-Android%208.0%2B-green.svg)]()
[![Powered by Magisk](https://img.shields.io/badge/Powered%20by-Magisk-blue.svg)](https://github.com/topjohnwu/Magisk)

</div>

---

## What is DoMagic?

DoMagic turns one Android phone into a rooting tool for another. Instead of needing a PC, you use an **emitter** phone (with DoMagic installed) to patch and flash a **receiver** phone connected via USB.

Under the hood it uses Magisk's own `magiskboot` binary to patch the boot image, and flashes it via Fastboot — all from your phone.

```
[Emitter phone — DoMagic installed]
            │
            │  USB cable
            ▼
[Receiver phone — gets rooted]
```

---

## How it works

1. Connect the receiver to the emitter via USB
2. DoMagic detects the device over ADB and shows its info
3. You select the stock `boot.img` for your receiver (downloaded separately)
4. DoMagic patches it locally using Magisk's `magiskboot`
5. Reboots the receiver into fastboot and flashes the patched image
6. Receiver reboots — rooted ✅

---

## Requirements

**Emitter phone (this app)**
- Android 8.0+
- USB Host / OTG support

**Receiver phone (the one getting rooted)**
- ADB debugging enabled
- **Bootloader must be unlocked** before using DoMagic
- Not a Samsung device (see below)

**You will need**
- A USB-OTG or USB-C to USB-C cable
- The stock `boot.img` for the receiver's exact build number (download from the manufacturer or your ROM's official source)

---

## ⚠️ Samsung is NOT supported

Samsung devices use their own proprietary flashing system (Odin) combined with Knox security. DoMagic does not support Samsung. To root a Samsung device you need a PC with Odin or Heimdall.

---

## Community Boot Images

DoMagic includes a community section powered by Firebase Realtime Database where users can share boot image download links for their devices. If your device's boot.img is already there, you can grab the link directly from the app.

Want to contribute? Open the Community tab → Upload → fill in your device info and paste an external download link (Google Drive, Mega, MediaFire, etc.).

---

## Building from source

### Prerequisites
- Android Studio Hedgehog or newer
- A Firebase project with Realtime Database enabled (free Spark tier works)

### Setup

**1. Clone the repo**
```bash
git clone https://github.com/jzadl/DoMagic.git
cd DoMagic
```

**2. Add `google-services.json`**

Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com), add an Android app with package name `com.domagic`, and place the downloaded `google-services.json` inside the `app/` folder.

**3. Add Magisk binaries**

Download the latest Magisk APK from [Magisk releases](https://github.com/topjohnwu/Magisk/releases/latest), rename it to `.zip` and extract it. Then copy:

```
lib/arm64-v8a/libmagiskboot.so  →  app/src/main/assets/arm64-v8a/magiskboot
lib/arm64-v8a/libmagiskinit.so  →  app/src/main/assets/arm64-v8a/magiskinit
lib/x86_64/libmagiskboot.so     →  app/src/main/assets/x86_64/magiskboot
lib/x86_64/libmagiskinit.so     →  app/src/main/assets/x86_64/magiskinit
assets/stub.apk                 →  app/src/main/assets/stub.apk
```

**4. Add a static fastboot binary for ARM64**

Grab one from [AndroidIDEOfficial/platform-tools releases](https://github.com/AndroidIDEOfficial/platform-tools/releases) and place it at:
```
app/src/main/assets/fastboot/arm64-v8a/fastboot
```

**5. Build**

Open the project in Android Studio and hit Run, or:
```bash
./gradlew assembleDebug
```

---

## Tech stack

| Component | Library |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| ADB communication | [dadb](https://github.com/mobile-dev-inc/dadb) |
| Boot image patching | [Magisk](https://github.com/topjohnwu/Magisk) `magiskboot` (GPL-3.0) |
| Community database | Firebase Realtime Database |
| Navigation | Jetpack Navigation Compose |

---

## Project status

This is an **experimental alpha**. Core functionality is implemented but not yet tested on real hardware. Contributions, testing reports, and PRs are very welcome.

- [x] UI screens (intro, connect, device info, patch, flash, community)
- [x] ADB device detection via dadb
- [x] Magisk boot image patching pipeline
- [x] Fastboot flash flow
- [x] Community boot image database
- [ ] Real hardware testing
- [ ] Automatic boot.img lookup by build number
- [ ] Proper error recovery

---

## Disclaimer

Rooting may void your warranty and can brick your device if something goes wrong. Use at your own risk. The authors are not responsible for any damage caused by using this tool.

---

## License

DoMagic is licensed under the **AGPL-3.0** license.

This project uses code from [Magisk](https://github.com/topjohnwu/Magisk) by topjohnwu, licensed under **GPL-3.0**.
