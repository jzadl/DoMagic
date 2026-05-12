package com.domagic.adb

import android.content.Context
import com.domagic.utils.DeviceInfo
import dadb.AdbKeyPair
import dadb.Dadb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Manages ADB connection to the receiver device over USB (via TCP tunnel
 * that dadb exposes through the USB ADB port on 5555 / direct USB).
 *
 * NOTE: dadb connects via TCP ADB (default port 5555). For USB-direct,
 * the phone must have ADB over network enabled OR the user runs
 * `adb tcpip 5555` first — which we can do via the USB ADB connection
 * that dadb supports internally.
 *
 * For the initial connection we rely on the standard dadb USB discovery
 * or direct localhost connection if port forwarded.
 */
class AdbManager(private val context: Context) {

    private var adb: Dadb? = null

    private val keyFile: File
        get() = File(context.filesDir, "adb_key")

    private fun keyPair(): AdbKeyPair {
        val pubKeyFile = File(keyFile.absolutePath + ".pub")
        if (!keyFile.exists() || !pubKeyFile.exists()) {
            AdbKeyPair.generate(keyFile, pubKeyFile)
        }
        return AdbKeyPair.read(keyFile, pubKeyFile)
    }

    /**
     * Connect to the first available device on localhost:5555.
     * Make sure the receiver is connected via USB and has ADB enabled.
     * You can forward the port with:  adb forward tcp:5555 tcp:5555
     * Or enable adb over wifi on the receiver first.
     *
     * Returns DeviceInfo on success, throws on failure.
     */
    suspend fun connect(): DeviceInfo = withContext(Dispatchers.IO) {
        val pair = keyPair()

        val device = try {
            Dadb.create(host = "localhost", port = 5555, keyPair = pair)
        } catch (e: Exception) {
            throw Exception(
                "No device found.\n" +
                "Make sure the receiver is connected via USB with ADB debugging enabled.\n" +
                "If it's the first time, accept the 'Allow USB debugging' prompt on the receiver.",
                e
            )
        }

        adb = device

        // Read device properties
        val model        = device.shellCommand("getprop ro.product.model").trim()
        val manufacturer = device.shellCommand("getprop ro.product.manufacturer").trim()
        val androidVer   = device.shellCommand("getprop ro.build.version.release").trim()
        val buildNum     = device.shellCommand("getprop ro.build.display.id").trim()
        val abi          = device.shellCommand("getprop ro.product.cpu.abi").trim()
        val fingerprint  = device.shellCommand("getprop ro.build.fingerprint").trim()

        // Bootloader unlock check (works on most AOSP-based ROMs)
        val unlocked = device.shellCommand("getprop ro.boot.verifiedbootstate").trim() == "orange"

        DeviceInfo(
            model        = model,
            manufacturer = manufacturer,
            androidVersion = androidVer,
            buildNumber  = buildNum,
            abi          = abi,
            fingerprint  = fingerprint,
            isUnlocked   = unlocked
        )
    }

    fun disconnect() {
        try { adb?.close() } catch (_: Exception) {}
        adb = null
    }

    /**
     * Reboot receiver into fastboot (bootloader) mode.
     */
    suspend fun rebootBootloader() = withContext(Dispatchers.IO) {
        adb?.shellCommand("reboot bootloader")
            ?: throw Exception("No device connected")
    }
}

// Extension to run shell and return stdout as String
private fun Dadb.shellCommand(cmd: String): String {
    return try {
        val result = this.shell(cmd)
        result.output.trim()
    } catch (e: Exception) {
        ""
    }
}
