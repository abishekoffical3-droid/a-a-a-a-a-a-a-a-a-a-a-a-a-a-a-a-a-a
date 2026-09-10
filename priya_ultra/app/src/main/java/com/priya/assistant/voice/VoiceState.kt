package com.priya.assistant.voice

/**
 * Drives both the PRIYA core animation and the status text on Home.
 * IDLE/LISTENING/PROCESSING/SPEAKING/PAUSED map to the voice pipeline (spec §6);
 * EXECUTING/ERROR are surfaced by ActionManager / AIManager results (spec §4/§30).
 */
enum class VoiceState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING,
    EXECUTING,
    PAUSED,
    ERROR
}

/** Human-readable status line shown under the PRIYA core. */
fun VoiceState.statusLabel(): String = when (this) {
    VoiceState.IDLE -> "Tap to talk"
    VoiceState.LISTENING -> "Listening..."
    VoiceState.PROCESSING -> "Thinking..."
    VoiceState.SPEAKING -> "Speaking..."
    VoiceState.EXECUTING -> "Working on it..."
    VoiceState.PAUSED -> "Paused"
    VoiceState.ERROR -> "Something went wrong"
}
