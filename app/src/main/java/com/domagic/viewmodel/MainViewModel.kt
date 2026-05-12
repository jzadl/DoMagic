package com.domagic.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.domagic.adb.AdbManager
import com.domagic.adb.FastbootManager
import com.domagic.patcher.MagiskPatcher
import com.domagic.utils.DeviceInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

data class UiState(
    // Connection
    val isConnecting: Boolean = false,
    val isConnected: Boolean = false,
    val connectionError: String? = null,
    // Device info
    val device: DeviceInfo? = null,
    // Boot image
    val bootImgUri: Uri? = null,
    val bootImgName: String? = null,
    // Patching
    val isPatching: Boolean = false,
    val patchLog: List<String> = emptyList(),
    val patchedFile: File? = null,
    val patchError: String? = null,
    // Flashing
    val isFlashing: Boolean = false,
    val flashLog: List<String> = emptyList(),
    val flashDone: Boolean = false,
    val flashError: String? = null,
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    private val adbManager = AdbManager(application)
    private val fastbootManager = FastbootManager(application)
    private val patcher = MagiskPatcher(application)

    // ── Connect ──────────────────────────────────────────────────────────────

    fun connectToDevice() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isConnecting = true, connectionError = null)
            try {
                val info = adbManager.connect()
                _state.value = _state.value.copy(
                    isConnecting = false,
                    isConnected = true,
                    device = info
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isConnecting = false,
                    connectionError = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun disconnect() {
        adbManager.disconnect()
        _state.value = UiState()
    }

    // ── Boot image selection ─────────────────────────────────────────────────

    fun setBootImage(uri: Uri, name: String) {
        _state.value = _state.value.copy(
            bootImgUri = uri,
            bootImgName = name,
            patchedFile = null,
            patchLog = emptyList(),
            patchError = null
        )
    }

    // ── Patch ────────────────────────────────────────────────────────────────

    fun patchBootImage() {
        val uri = _state.value.bootImgUri ?: return
        val device = _state.value.device ?: return

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isPatching = true,
                patchLog = listOf("Starting patch..."),
                patchError = null,
                patchedFile = null
            )

            try {
                val output = patcher.patch(
                    bootImgUri = uri,
                    abi = device.abi,
                    onLog = { line ->
                        _state.value = _state.value.copy(
                            patchLog = _state.value.patchLog + line
                        )
                    }
                )
                _state.value = _state.value.copy(
                    isPatching = false,
                    patchedFile = output
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isPatching = false,
                    patchError = e.message ?: "Patching failed"
                )
            }
        }
    }

    // ── Flash ────────────────────────────────────────────────────────────────

    fun flashDevice() {
        val patchedFile = _state.value.patchedFile ?: return

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isFlashing = true,
                flashLog = listOf("Rebooting into Fastboot..."),
                flashError = null,
                flashDone = false
            )

            try {
                fastbootManager.flash(
                    patchedFile = patchedFile,
                    onLog = { line ->
                        _state.value = _state.value.copy(
                            flashLog = _state.value.flashLog + line
                        )
                    }
                )
                _state.value = _state.value.copy(
                    isFlashing = false,
                    flashDone = true
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isFlashing = false,
                    flashError = e.message ?: "Flashing failed"
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        adbManager.disconnect()
    }
}
