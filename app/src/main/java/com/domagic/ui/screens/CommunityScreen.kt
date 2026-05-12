package com.domagic.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.domagic.db.BootEntry
import com.domagic.db.BootImageRepository
import com.domagic.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var entries by remember { mutableStateOf<List<BootEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showUploadDialog by remember { mutableStateOf(false) }

    LaunchedEffect(query) {
        isLoading = true
        if (query.isBlank()) {
            BootImageRepository.observeAll().collect { list ->
                entries = list.sortedByDescending { it.timestamp }
                isLoading = false
            }
        } else {
            BootImageRepository.searchByModel(query).collect { list ->
                entries = list
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Community", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = { showUploadDialog = true }) {
                        Icon(Icons.Filled.FileUpload, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search model or build...") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                shape = RoundedCornerShape(28.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (entries.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No results found", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(entries, key = { it.id }) { entry ->
                        BootEntryCard(
                            entry = entry,
                            onDownload = {
                                scope.launch {
                                    BootImageRepository.incrementDownload(entry.id)
                                }
                            }
                        )
                    }
                }
            }
        }

        if (showUploadDialog) {
            UploadDialog(
                onDismiss = { showUploadDialog = false },
                onUploaded = { showUploadDialog = false }
            )
        }
    }
}

@Composable
private fun BootEntryCard(entry: BootEntry, onDownload: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("${entry.manufacturer} ${entry.model}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Android ${entry.androidVersion} • ${entry.abi}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (entry.verified) {
                    Icon(Icons.Filled.Verified, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }
            
            Spacer(Modifier.height(8.dp))
            Text(entry.buildNumber, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)

            Spacer(Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${entry.downloadCount} downloads", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                
                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:jzadpy@gmail.com")
                            putExtra(Intent.EXTRA_SUBJECT, "Request Delete: ${entry.model}")
                            putExtra(Intent.EXTRA_TEXT, """
                                Requesting deletion of boot image:
                                ID: ${entry.id}
                                Model: ${entry.model}
                                Manufacturer: ${entry.manufacturer}
                                Build: ${entry.buildNumber}
                                Android: ${entry.androidVersion}
                                URL: ${entry.downloadUrl}
                            """.trimIndent())
                        }
                        try {
                            context.startActivity(Intent.createChooser(intent, "Send Email"))
                        } catch (e: Exception) {
                            Toast.makeText(context, "No email client found", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Icon(Icons.Filled.Delete, null, modifier = Modifier.size(16.dp), tint = ErrorRed)
                    Spacer(Modifier.width(4.dp))
                    Text("Request Delete", color = ErrorRed, fontSize = 12.sp)
                }

                Spacer(Modifier.width(8.dp))

                FilledTonalButton(
                    onClick = {
                        onDownload()
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(entry.downloadUrl))
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Filled.Download, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Download")
                }
            }
        }
    }
}

@Composable
private fun Chip(text: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Purple700
    ) {
        Text(
            text,
            color = Violet300,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

// ── Upload Dialog ─────────────────────────────────────────────────────────────

@Composable
private fun UploadDialog(onDismiss: () -> Unit, onUploaded: () -> Unit) {
    var manufacturer by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var androidVersion by remember { mutableStateOf("") }
    var buildNumber by remember { mutableStateOf("") }
    var abi by remember { mutableStateOf("arm64-v8a") }
    var downloadUrl by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Purple800,
        title = {
            Text("Share boot.img", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    "Help the community by sharing a download link for your device's boot.img (Google Drive, Mega, etc).",
                    color = Violet300,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(16.dp))

                listOf(
                    Triple("Manufacturer", manufacturer) { v: String -> manufacturer = v },
                    Triple("Model", model) { v: String -> model = v },
                    Triple("Android Version", androidVersion) { v: String -> androidVersion = v },
                    Triple("Build Number", buildNumber) { v: String -> buildNumber = v },
                    Triple("Download Link", downloadUrl) { v: String -> downloadUrl = v },
                ).forEach { (label, value, setter) ->
                    OutlinedTextField(
                        value = value,
                        onValueChange = setter,
                        label = { Text(label, color = Violet300) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Violet500,
                            unfocusedBorderColor = Purple700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Violet400,
                        ),
                        singleLine = true
                    )
                }

                Spacer(Modifier.height(8.dp))

                // ABI selector
                Text("ABI:", color = Violet300, fontSize = 13.sp)
                Row {
                    listOf("arm64-v8a", "x86_64", "armeabi-v7a").forEach { abiOption ->
                        FilterChip(
                            selected = abi == abiOption,
                            onClick = { abi = abiOption },
                            label = { Text(abiOption, fontSize = 11.sp) },
                            modifier = Modifier.padding(end = 6.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Violet500,
                                selectedLabelColor = Color.White,
                                containerColor = Purple700,
                                labelColor = Violet300
                            )
                        )
                    }
                }

                if (isSubmitting) {
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = Violet500
                    )
                }

                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = ErrorRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (manufacturer.isBlank() || model.isBlank() || buildNumber.isBlank() || downloadUrl.isBlank()) {
                        error = "Please complete all fields"
                        return@Button
                    }
                    scope.launch {
                        try {
                            isSubmitting = true
                            BootImageRepository.submitEntry(
                                entry = BootEntry(
                                    manufacturer = manufacturer,
                                    model = model,
                                    androidVersion = androidVersion,
                                    buildNumber = buildNumber,
                                    abi = abi,
                                    downloadUrl = downloadUrl
                                )
                            )
                            onUploaded()
                        } catch (e: Exception) {
                            error = e.message ?: "Error submitting"
                            isSubmitting = false
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Violet500),
                shape = RoundedCornerShape(10.dp),
                enabled = !isSubmitting
            ) {
                Text("Share")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("Cancel", color = Violet300)
            }
        }
    )
}
