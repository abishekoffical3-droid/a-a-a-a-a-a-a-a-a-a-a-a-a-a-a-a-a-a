package com.priya.assistant.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.priya.assistant.accessibility.PriyaAccessibilityService
import com.priya.assistant.ui.components.GlassPanel
import com.priya.assistant.ui.components.StatusDot
import com.priya.assistant.ui.theme.CrimsonCore
import com.priya.assistant.ui.theme.MutedWhite
import com.priya.assistant.ui.theme.SoftWhite
import com.priya.assistant.ui.theme.StatusAmber
import com.priya.assistant.ui.theme.StatusGreen
import com.priya.assistant.ui.theme.VoidBlack

@Composable
fun AccessibilityScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var enabled by remember { mutableStateOf(PriyaAccessibilityService.isEnabled()) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                enabled = PriyaAccessibilityService.isEnabled()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(containerColor = VoidBlack) { padding ->
        Column(modifier = Modifier.fillMaxSize().background(VoidBlack).padding(padding)) {
            ScreenHeader("Accessibility Access", onBack)

            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                GlassPanel(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusDot(color = if (enabled) StatusGreen else StatusAmber)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "PRIYA Accessibility Service",
                            color = SoftWhite,
                            fontSize = 15.sp
                        )
                    }
                    Text(
                        if (enabled) "ENABLED" else "DISABLED",
                        color = if (enabled) StatusGreen else StatusAmber,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, start = 16.dp)
                    )
                }

                GlassPanel(modifier = Modifier.fillMaxWidth()) {
                    Text("What this does", color = SoftWhite, fontSize = 14.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "When you ask PRIYA to open an app, tap a button, type text, scroll, " +
                            "or go back/home, PRIYA uses Android's Accessibility service to carry " +
                            "that out on screen. It only acts when you explicitly ask it to — it " +
                            "does not read your screen in the background or run without your command.",
                        color = MutedWhite,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }

                Button(
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonCore),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (enabled) "Manage in Android Settings" else "Enable Accessibility")
                }
            }
        }
    }
}
