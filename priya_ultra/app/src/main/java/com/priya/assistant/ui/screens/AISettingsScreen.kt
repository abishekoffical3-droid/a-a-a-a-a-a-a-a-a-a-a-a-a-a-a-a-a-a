package com.priya.assistant.ui.screens

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
import androidx.compose.foundation.layout.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.priya.assistant.ai.AIProviderType
import com.priya.assistant.ui.SettingsViewModel
import com.priya.assistant.ui.components.GlassPanel
import com.priya.assistant.ui.components.SectionLabel
import com.priya.assistant.ui.theme.CrimsonBright
import com.priya.assistant.ui.theme.CrimsonCore
import com.priya.assistant.ui.theme.CrimsonDeep
import com.priya.assistant.ui.theme.FaintWhite
import com.priya.assistant.ui.theme.GlassBorder
import com.priya.assistant.ui.theme.GlassSurface
import com.priya.assistant.ui.theme.MutedWhite
import com.priya.assistant.ui.theme.SoftWhite
import com.priya.assistant.ui.theme.StatusGreen
import com.priya.assistant.ui.theme.StatusRed
import com.priya.assistant.ui.theme.VoidBlack

@Composable
fun AISettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit) {
    val aiSettings by viewModel.aiSettings.collectAsStateWithLifecycle()
    val testResult by viewModel.testResult.collectAsStateWithLifecycle()
    val isTesting by viewModel.isTesting.collectAsStateWithLifecycle()

    var apiKeyInput by remember(aiSettings.providerId) { mutableStateOf("") }
    var modelInput by remember(aiSettings.providerId) { mutableStateOf(aiSettings.model) }
    var baseUrlInput by remember(aiSettings.providerId) { mutableStateOf(aiSettings.customBaseUrl) }

    Scaffold(containerColor = VoidBlack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(VoidBlack)
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            ScreenHeader("AI Provider", onBack)

            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SectionLabel("Provider")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AIProviderType.entries.forEach { type ->
                        val selected = aiSettings.providerId == type.id
                        Text(
                            text = type.displayName,
                            color = if (selected) SoftWhite else MutedWhite,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .background(
                                    if (selected) CrimsonCore else GlassSurface,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickableSettings {
                                    viewModel.selectProvider(type.id)
                                    modelInput = type.defaultModel
                                    viewModel.clearTestResult()
                                }
                                .padding(vertical = 8.dp, horizontal = 14.dp)
                        )
                    }
                }

                GlassPanel(modifier = Modifier.fillMaxWidth()) {
                    val currentType = AIProviderType.fromId(aiSettings.providerId)

                    Text("Model", color = MutedWhite, fontSize = 12.sp)
                    OutlinedTextField(
                        value = modelInput,
                        onValueChange = { modelInput = it; viewModel.updateModel(it) },
                        placeholder = { Text(currentType.defaultModel.ifBlank { "e.g. gpt-4o-mini" }, color = FaintWhite) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = fieldColors()
                    )

                    if (aiSettings.providerId == "custom") {
                        Spacer(Modifier.height(10.dp))
                        Text("Base URL", color = MutedWhite, fontSize = 12.sp)
                        OutlinedTextField(
                            value = baseUrlInput,
                            onValueChange = { baseUrlInput = it; viewModel.updateCustomBaseUrl(it) },
                            placeholder = { Text("https://example.com/v1", color = FaintWhite) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = fieldColors()
                        )
                    }

                    Spacer(Modifier.height(14.dp))
                    Text("API Key", color = MutedWhite, fontSize = 12.sp)

                    val masked = viewModel.maskedKeyFor(aiSettings.providerId)
                    if (masked != null && apiKeyInput.isEmpty()) {
                        Text(masked, color = SoftWhite, fontSize = 14.sp, modifier = Modifier.padding(vertical = 6.dp))
                    }

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        placeholder = { Text(if (masked != null) "Enter a new key to replace it" else "Paste your API key", color = FaintWhite) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = fieldColors()
                    )

                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (apiKeyInput.isNotBlank()) {
                                    viewModel.saveApiKey(aiSettings.providerId, apiKeyInput)
                                    apiKeyInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonCore)
                        ) { Text("Save") }

                        OutlinedButton(
                            onClick = {
                                val keyToTest = apiKeyInput.ifBlank { null }
                                    ?: run { return@OutlinedButton }
                                viewModel.testConnection(aiSettings.providerId, keyToTest, modelInput, baseUrlInput)
                            }
                        ) { Text(if (isTesting) "Testing..." else "Test Connection", color = SoftWhite) }

                        IconButton(onClick = { viewModel.deleteApiKey(aiSettings.providerId); apiKeyInput = "" }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete key", tint = StatusRed)
                        }
                    }

                    testResult?.let { result ->
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            when (result) {
                                is SettingsViewModel.TestResult.Ok -> {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = StatusGreen, modifier = Modifier.height(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(result.message, color = StatusGreen, fontSize = 12.sp)
                                }
                                is SettingsViewModel.TestResult.Failed -> {
                                    Icon(Icons.Filled.Error, contentDescription = null, tint = StatusRed, modifier = Modifier.height(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(result.message, color = StatusRed, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                    if (isTesting) {
                        Spacer(Modifier.height(8.dp))
                        CircularProgressIndicator(color = CrimsonBright, modifier = Modifier.height(18.dp))
                    }
                }

                Text(
                    "Keys are encrypted on-device using the Android Keystore and are never logged or shown in full once saved.",
                    color = FaintWhite,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
internal fun ScreenHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = SoftWhite)
        }
        Text(title, color = SoftWhite, fontSize = 20.sp)
    }
}

@Composable
internal fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = SoftWhite,
    unfocusedTextColor = SoftWhite,
    focusedContainerColor = GlassSurface,
    unfocusedContainerColor = GlassSurface,
    focusedBorderColor = CrimsonCore,
    unfocusedBorderColor = GlassBorder
)

private fun Modifier.clickableSettings(onClick: () -> Unit): Modifier =
    this.then(androidx.compose.foundation.clickable(onClick = onClick))
