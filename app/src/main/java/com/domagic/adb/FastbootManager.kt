package com.domagic.adb

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Runs fastboot commands using a precompiled fastboot binary bundled in assets.
 * The binary must be at assets/arm64-v8a/fastboot (or x86_64).
 *
 * The receiver must already be in fastboot mode (bootloader) before calling flash().
 *
 * HOW TO GET THE BINARY:
 * Download Android SDK Platform Tools for Linux and extract the fastboot binary.
 * It works on Android (arm64) when compiled statically. You can grab a prebuilt
 * arm64-static fastboot from: https://github.com/pbatard/android-tools/releases
 * Place it at: app/src/main/assets/arm64-v8a/fastboot
 */
class FastbootManager(private val context: Context) {

    private fun extractFastboot(): File {
        val abi = android.os.Build.SUPPORTED_ABIS[0]
        val assetAbi = when {
            abi.startsWith("arm64") -> "arm64-v8a"
            abi.startsWith("x86_64") -> "x86_64"
            else -> "arm64-v8a" // fallback
        }

        val fastbootAsset = "fastboot/$assetAbi/fastboot"
        val outFile = File(context.filesDir, "fastboot")

        if (!outFile.exists()) {
            context.assets.open(fastbootAsset).use { input ->
                outFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            outFile.setExecutable(true)
        }
        return outFile
    }

    /**
     * Full flash sequence:
     * 1. fastboot flash boot <patched.img>
     * 2. fastboot reboot
     */
    suspend fun flash(
        patchedFile: File,
        onLog: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        val fastboot = extractFastboot()

        onLog("Looking for device in Fastboot mode...")

        // Verify device is visible
        runCmd(fastboot, listOf("devices"), onLog)

        onLog("Flashing boot partition...")
        runCmd(fastboot, listOf("flash", "boot", patchedFile.absolutePath), onLog)

        onLog("Rebooting device...")
        runCmd(fastboot, listOf("reboot"), onLog)

        onLog("Done! The receiver should boot with Magisk installed.")
    }

    private fun runCmd(
        fastboot: File,
        args: List<String>,
        onLog: (String) -> Unit
    ) {
        val cmd = listOf(fastboot.absolutePath) + args
        val process = ProcessBuilder(cmd)
            .redirectErrorStream(true)
            .start()

        process.inputStream.bufferedReader().forEachLine { line ->
            onLog(line)
        }

        val exitCode = process.waitFor()
        if (exitCode != 0) {
            throw Exception("Fastboot failed with code $exitCode")
        }
    }
}
