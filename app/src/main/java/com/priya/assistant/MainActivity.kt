package com.priya.assistant

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.priya.assistant.ui.navigation.PriyaNavGraph
import com.priya.assistant.ui.theme.PriyaTheme
import com.priya.assistant.ui.theme.VoidBlack

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* Results are re-read on demand by PermissionsScreen; nothing to store here. */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestStartupPermissions()

        val app = application as PriyaApplication

        setContent {
            PriyaTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(VoidBlack)
                ) {
                    PriyaNavGraph(app = app)
                }
            }
        }
    }

    private fun requestStartupPermissions() {
        val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    override fun onDestroy() {
        super.onDestroy()
        // Voice resources are owned by PriyaApplication (survives config changes);
        // only fully released if the process is actually going away.
        if (isFinishing) {
            (application as PriyaApplication).voiceManager.shutdown()
        }
    }
}
