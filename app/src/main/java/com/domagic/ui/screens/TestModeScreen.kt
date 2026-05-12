package com.domagic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.domagic.adb.AdbManager
import androidx.compose.ui.unit.sp
import com.domagic.ui.theme.*
import com.domagic.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestModeScreen(
    vm: MainViewModel,
    onBack: () -> Unit
) {
    var adbStatus by remember { mutableStateOf("Unknown") }
    var fastbootStatus by remember { mutableStateOf("Unknown") }
    var isChecking by remember { mutableStateOf(false) }
    var logs by remember { mutableStateOf(listOf<String>()) }
    val scope = rememberCoroutineScope()

    // Emulation State
    var emulatedManufacturer by remember { mutableStateOf("Google") }
    var emulatedModel by remember { mutableStateOf("Pixel 7") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Debug / Test Mode", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Purple900)
            )
        },
        containerColor = Purple800
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Connection Test", color = Violet400, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Purple700),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    StatusRow("ADB Daemon", adbStatus)
                    StatusRow("Fastboot Protocol", fastbootStatus)
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("Emulate Device", color = Violet400, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = emulatedManufacturer,
                onValueChange = { emulatedManufacturer = it },
                label = { Text("Manufacturer", color = Violet300) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = emulatedModel,
                onValueChange = { emulatedModel = it },
                label = { Text("Model", color = Violet300) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            Spacer(Modifier.height(24.dp))
            
            Button(
                onClick = {
                    scope.launch {
                        isChecking = true
                        logs = logs + "Scanning USB for emulated device..."
                        
                        // We use the real AdbManager to try and "connect" to the webpage's virtual device
                        try {
                            vm.connectToDevice()
                            delay(1000)
                            val device = vm.state.value.device
                            if (device != null) {
                                adbStatus = "Connected"
                                logs = logs + "REAL CONNECTION: Found ${device.manufacturer} ${device.model}"
                                logs = logs + "Build: ${device.buildNumber}"
                                logs = logs + "ABI: ${device.abi}"
                                logs = logs + "Unlocked: ${device.isUnlocked}"
                            } else {
                                adbStatus = "Failed"
                                logs = logs + "No device found. Is the webpage emulation running?"
                            }
                        } catch (e: Exception) {
                            adbStatus = "Error"
                            logs = logs + "Connection Error: ${e.message}"
                        }
                        
                        isChecking = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Violet500),
                enabled = !isChecking
            ) {
                Text("Scan for Emulated Device")
            }

            Spacer(Modifier.height(24.dp))
            Text("Logs", color = Violet400, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            Surface(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                color = Color.Black,
                shape = RoundedCornerShape(8.dp)
            ) {
                LazyColumn(contentPadding = PaddingValues(8.dp)) {
                    items(logs) { log ->
                        Text(log, color = Color.Green, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusRow(label: String, status: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Violet300)
        Text(
            status, 
            color = if (status == "Connected") Color.Green else if (status == "Unknown") Color.Gray else Color.Red,
            fontWeight = FontWeight.Bold
        )
    }
}
