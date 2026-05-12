package com.domagic.patcher

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Patches a boot.img with Magisk using the bundled magiskboot binary
 * and boot_patch.sh script (both from Magisk's FOSS release).
 *
 * REQUIRED ASSETS (download from Magisk GitHub releases, extract from the APK):
 *
 *   assets/
 *   ├── arm64-v8a/
 *   │   ├── magiskboot       ← from Magisk APK lib/arm64-v8a/libmagiskboot.so  (rename)
 *   │   └── magiskinit       ← from Magisk APK lib/arm64-v8a/libmagiskinit.so  (rename)
 *   ├── x86_64/
 *   │   ├── magiskboot
 *   │   └── magiskinit
 *   ├── stub.apk             ← assets/stub.apk inside Magisk APK
 *   ├── boot_patch.sh        ← scripts/boot_patch.sh from Magisk repo
 *   └── util_functions.sh    ← scripts/util_functions.sh from Magisk repo
 *
 * HOW TO EXTRACT FROM MAGISK APK:
 *   1. Download Magisk-vXX.X.apk from https://github.com/topjohnwu/Magisk/releases
 *   2. Rename to .zip and extract
 *   3. Copy lib/arm64-v8a/libmagiskboot.so  → assets/arm64-v8a/magiskboot
 *   4. Copy lib/arm64-v8a/libmagiskinit.so  → assets/arm64-v8a/magiskinit
 *   5. Copy assets/stub.apk                 → assets/stub.apk
 *   6. Grab boot_patch.sh and util_functions.sh from the GitHub repo
 *
 * Magisk is licensed under GPL-3.0 — https://github.com/topjohnwu/Magisk
 */
class MagiskPatcher(private val context: Context) {

    private val workDir: File
        get() = File(context.filesDir, "magisk_work").also { it.mkdirs() }

    /**
     * Patch the given boot image URI.
     * @param abi  The ABI of the RECEIVER device (arm64-v8a, x86_64, etc.)
     * @param onLog  Callback for log lines shown in the UI
     * @return  The patched boot image file
     */
    suspend fun patch(
        bootImgUri: Uri,
        abi: String,
        onLog: (String) -> Unit
    ): File = withContext(Dispatchers.IO) {

        val dir = workDir
        // Clean previous run
        dir.listFiles()?.forEach { it.delete() }

        onLog("Copying boot.img...")
        val bootImg = File(dir, "boot.img")
        context.contentResolver.openInputStream(bootImgUri)?.use { input ->
            bootImg.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw Exception("Could not read boot.img")

        onLog("Extracting Magisk binaries...")
        val assetAbi = when {
            abi.startsWith("arm64") -> "arm64-v8a"
            abi.startsWith("x86_64") -> "x86_64"
            else -> "arm64-v8a"
        }

        extractAsset("$assetAbi/magiskboot", File(dir, "magiskboot"), executable = true)
        extractAsset("$assetAbi/magiskinit", File(dir, "magiskinit"), executable = true)
        extractAsset("stub.apk", File(dir, "stub.apk"))

        // magisk binary = magiskboot in newer versions handles both roles
        // but boot_patch.sh also needs a 'magisk' binary for --preinit-device
        // We symlink or copy magiskboot as magisk
        val magiskBin = File(dir, "magisk")
        File(dir, "magiskboot").copyTo(magiskBin, overwrite = true)
        magiskBin.setExecutable(true)

        // init-ld.so: extract if available, otherwise create empty placeholder
        try {
            extractAsset("$assetAbi/init-ld.so", File(dir, "init-ld.so"), executable = true)
        } catch (_: Exception) {
            File(dir, "init-ld.so").writeBytes(ByteArray(0))
        }

        extractAsset("boot_patch.sh", File(dir, "boot_patch.sh"), executable = true)
        extractAsset("util_functions.sh", File(dir, "util_functions.sh"), executable = true)

        onLog("Running boot_patch.sh...")

        // Boot patch environment vars (match Magisk defaults)
        val env = mapOf(
            "KEEPVERITY"         to "false",
            "KEEPFORCEENCRYPT"   to "false",
            "PATCHVBMETAFLAG"    to "false",
            "RECOVERYMODE"       to "false",
            "LEGACYSAR"          to "false",
            "BOOTMODE"           to "false",
        )

        val process = ProcessBuilder(
            listOf("/system/bin/sh", "boot_patch.sh", bootImg.absolutePath)
        )
            .directory(dir)
            .redirectErrorStream(true)
            .also { pb -> env.forEach { (k, v) -> pb.environment()[k] = v } }
            .start()

        process.inputStream.bufferedReader().forEachLine { line ->
            onLog(line)
        }

        val exitCode = process.waitFor()
        if (exitCode != 0) {
            throw Exception("boot_patch.sh failed with code $exitCode")
        }

        // Output is new-boot.img in workDir
        val outputImg = File(dir, "new-boot.img")
        if (!outputImg.exists()) {
            throw Exception("new-boot.img not generated — check logs")
        }

        // Copy to a readable location and give it a nice name
        val finalOut = File(context.getExternalFilesDir(null), "magisk_patched.img")
        outputImg.copyTo(finalOut, overwrite = true)

        onLog("Patched image saved to: ${finalOut.absolutePath}")
        finalOut
    }

    private fun extractAsset(assetPath: String, dest: File, executable: Boolean = false) {
        context.assets.open(assetPath).use { input ->
            dest.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        if (executable) dest.setExecutable(true)
    }
}
