package com.priya.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.priya.assistant.data.models.SpeechLanguage
import com.priya.assistant.data.models.VoiceEngine
import com.priya.assistant.ui.SettingsViewModel
import com.priya.assistant.ui.components.GlassPanel
import com.priya.assistant.ui.components.SectionLabel
import com.priya.assistant.ui.theme.CrimsonCore
import com.priya.assistant.ui.theme.FaintWhite
import com.priya.assistant.ui.theme.GlassSurface
import com.priya.assistant.ui.theme.MutedWhite
import com.priya.assistant.ui.theme.SoftWhite
import com.priya.assistant.ui.theme.VoidBlack

@Composable
fun VoiceSettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit) {
    val voiceSettings by viewModel.voiceSettings.collectAsStateWithLifecycle()

    Scaffold(containerColor = VoidBlack) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().background(VoidBlack).padding(padding).verticalScroll(rememberScrollState())
        ) {
            ScreenHeader("Voice", onBack)

            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                GlassPanel(modifier = Modifier.fillMaxWidth()) {
                    ToggleRow("Voice enabled", voiceSettings.voiceEnabled, viewModel::updateVoiceEnabled)
                    ToggleRow("Continuous listening", voiceSettings.continuousListening, viewModel::updateContinuousListening)
                    ToggleRow("Auto speak replies", voiceSettings.autoSpeak, viewModel::updateAutoSpeak)
                }

                SectionLabel("Speech language")
                GlassPanel(modifier = Modifier.fillMaxWidth()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SpeechLanguage.entries.forEach { lang ->
                            val selected = voiceSettings.language == lang
                            Text(
                                lang.label,
                                color = if (selected) SoftWhite else MutedWhite,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .background(if (selected) CrimsonCore else GlassSurface, RoundedCornerShape(10.dp))
                                    .then(androidx.compose.foundation.clickable { viewModel.updateLanguage(lang) })
                                    .padding(vertical = 6.dp, horizontal = 10.dp)
                            )
                        }
                    }
                }

                SectionLabel("Voice engine")
                GlassPanel(modifier = Modifier.fillMaxWidth()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        VoiceEngine.entries.forEach { engine ->
                            val selected = voiceSettings.voiceEngine == engine
                            Text(
                                engine.name.lowercase().replaceFirstChar { it.uppercase() },
                                color = if (selected) SoftWhite else MutedWhite,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .background(if (selected) CrimsonCore else GlassSurface, RoundedCornerShape(10.dp))
                                    .then(androidx.compose.foundation.clickable { viewModel.updateVoiceEngine(engine) })
                                    .padding(vertical = 6.dp, horizontal = 10.dp)
                            )
                        }
                    }
                    Text(
                        "Android Voice is always available as a fallback, even if the external voice fails.",
                        color = FaintWhite,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                SectionLabel("Speech speed  ${"%.2f".format(voiceSettings.speechSpeed)}x")
                Slider(
                    value = voiceSettings.speechSpeed,
                    onValueChange = viewModel::updateSpeechSpeed,
                    valueRange = 0.75f..1.25f,
                    steps = 3,
                    colors = SliderDefaults.colors(thumbColor = CrimsonCore, activeTrackColor = CrimsonCore)
                )

                SectionLabel("Speech pitch  ${"%.2f".format(voiceSettings.speechPitch)}")
                Slider(
                    value = voiceSettings.speechPitch,
                    onValueChange = viewModel::updateSpeechPitch,
                    valueRange = 0.8f..1.2f,
                    steps = 3,
                    colors = SliderDefaults.colors(thumbColor = CrimsonCore, activeTrackColor = CrimsonCore)
                )

                Button(
                    onClick = viewModel::testVoice,
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonCore),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Test Voice") }
            }
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = SoftWhite, fontSize = 14.sp)
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(checkedThumbColor = SoftWhite, checkedTrackColor = CrimsonCore)
        )
    }
}
