package com.domagic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.domagic.ui.theme.*
import com.domagic.viewmodel.MainViewModel

@Composable
fun ConnectScreen(
    vm: MainViewModel,
    onConnected: () -> Unit,
    onCommunity: () -> Unit,
    onTestMode: () -> Unit = {},
) {
    val state by vm.state.collectAsState()
    var clickCount by remember { mutableStateOf(0) }

    LaunchedEffect(state.isConnected) {
        if (state.isConnected) onConnected()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Purple900, SurfaceDark)))
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = {
                    clickCount++
                    if (clickCount >= 7) {
                        onTestMode()
                        clickCount = 0
                    }
                },
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Usb,
                    contentDescription = null,
                    tint = Violet400,
                    modifier = Modifier.size(72.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Connect Receiver",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Connect the receiver phone via USB.\nMake sure it has ADB enabled\nand the bootloader is UNLOCKED.",
                fontSize = 14.sp,
                color = Violet300,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(40.dp))

            // Steps
            StepRow("1", "Enable Developer Options\non the receiver (tap 7x on build number)")
            Spacer(Modifier.height(12.dp))
            StepRow("2", "Enable 'USB Debugging' in\nDeveloper Options")
            Spacer(Modifier.height(12.dp))
            StepRow("3", "Connect the receiver to this phone\nvia USB and press Connect")

            Spacer(Modifier.height(40.dp))

            // Error
            state.connectionError?.let { err ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3D0A0A)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Filled.Error, null, tint = ErrorRed, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(err, color = ErrorRed, fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Connect button
            Button(
                onClick = { vm.connectToDevice() },
                enabled = !state.isConnecting,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Violet500)
            ) {
                if (state.isConnecting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("Connecting...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Filled.Link, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Connect", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Community button at bottom
        TextButton(
            onClick = onCommunity,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Icon(
                Icons.Filled.Group,
                contentDescription = null,
                tint = Violet400,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text("Community Boot Images", color = Violet400)
        }
    }
}

@Composable
private fun StepRow(number: String, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = Violet500,
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(number, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(text, color = OnSurface, fontSize = 14.sp, lineHeight = 20.sp)
    }
}
