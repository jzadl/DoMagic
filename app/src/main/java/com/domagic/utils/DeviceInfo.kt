package com.domagic.utils

data class DeviceInfo(
    val model: String,
    val manufacturer: String,
    val androidVersion: String,
    val buildNumber: String,
    val abi: String,           // arm64-v8a | x86_64 | armeabi-v7a
    val fingerprint: String,
    val isUnlocked: Boolean,   // bootloader unlock status (best-effort)
)
