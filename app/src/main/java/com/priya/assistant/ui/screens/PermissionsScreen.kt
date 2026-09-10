package com.priya.assistant.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.priya.assistant.accessibility.PriyaAccessibilityService
import com.priya.assistant.ui.components.GlassPanel
import com.priya.assistant.ui.components.StatusDot
import com.priya.assistant.ui.theme.CrimsonBright
import com.priya.assistant.ui.theme.MutedWhite
import com.priya.assistant.ui.theme.SoftWhite
import com.priya.assistant.ui.theme.StatusAmber
import com.priya.assistant.ui.theme.StatusGreen
import com.priya.assistant.ui.theme.VoidBlack

private data class PermissionRow(val label: String, val granted: Boolean)

@Composable
fun PermissionsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    fun micGranted() = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    fun notifGranted() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else true

    var mic by remember { mutableStateOf(micGranted()) }
    var notif by remember { mutableStateOf(notifGranted()) }
    var accessibility by remember { mutableStateOf(PriyaAccessibilityService.isEnabled()) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                mic = micGranted()
                notif = notifGranted()
                accessibility = PriyaAccessibilityService.isEnabled()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(containerColor = VoidBlack) { padding ->
        Column(modifier = Modifier.fillMaxSize().background(VoidBlack).padding(padding)) {
            ScreenHeader("Permissions", onBack)

            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PermissionEntry("Microphone", mic, "Needed for voice conversation with PRIYA.")
                PermissionEntry("Notifications", notif, "Shows a notice while continuous listening is active.")
                PermissionEntry("Accessibility", accessibility, "Lets PRIYA open apps, tap, type, and scroll for you.")

                Spacer(Modifier.height(4.dp))
                TextButton(onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }) {
                    Text("Open App Settings", color = CrimsonBright)
                }
            }
        }
    }
}

@Composable
private fun PermissionEntry(label: String, granted: Boolean, description: String) {
    GlassPanel(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusDot(color = if (granted) StatusGreen else StatusAmber)
            Spacer(Modifier.width(8.dp))
            Text(label, color = SoftWhite, fontSize = 15.sp)
            Spacer(Modifier.weight(1f))
            Text(if (granted) "Granted" else "Not granted", color = if (granted) StatusGreen else StatusAmber, fontSize = 12.sp)
        }
        Text(description, color = MutedWhite, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp, start = 16.dp))
    }
}
