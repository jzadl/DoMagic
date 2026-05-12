package com.domagic.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.domagic.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatchScreen(
    vm: MainViewModel,
    onPatchDone: () -> Unit,
    onBack: () -> Unit,
) {
    val state by vm.state.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(state.patchedFile) {
        if (state.patchedFile != null) onPatchDone()
    }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { u ->
            val name = u.lastPathSegment ?: "boot.img"
            vm.setBootImage(u, name)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Patch Image", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(Modifier.height(16.dp))

            // Minimalist Picker
            Surface(
                onClick = { filePicker.launch("*/*") },
                shape = RoundedCornerShape(24.dp),
                color = if (state.bootImgUri != null) MaterialTheme.colorScheme.primaryContainer 
                        else MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                border = if (state.bootImgUri == null) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) 
                         else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            if (state.bootImgUri == null) Icons.Filled.FileUpload else Icons.Filled.CheckCircle,
                            null,
                            tint = if (state.bootImgUri == null) MaterialTheme.colorScheme.primary 
                                   else MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            if (state.bootImgUri == null) "Select boot.img" else state.bootImgName ?: "Selected",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (state.bootImgUri == null) MaterialTheme.colorScheme.onSurface 
                                   else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Ensure the boot.img matches your device's exact build version.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (state.patchLog.isNotEmpty()) {
                Spacer(Modifier.height(32.dp))
                Surface(
                    color = Color.Black,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Patching Log", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        state.patchLog.forEach { line ->
                            Text(
                                line, 
                                color = Color(0xFF00FF88), 
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            state.patchError?.let { err ->
                Spacer(Modifier.height(24.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.padding(16.dp)) {
                        Icon(Icons.Filled.Error, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(12.dp))
                        Text(err, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(Modifier.height(48.dp))

            Button(
                onClick = { vm.patchBootImage() },
                enabled = state.bootImgUri != null && !state.isPatching,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                if (state.isPatching) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Filled.AutoFixHigh, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Patch with Magisk", style = MaterialTheme.typography.labelLarge)
                }
            }
            
            Spacer(Modifier.height(24.dp))
        }
    }
}
