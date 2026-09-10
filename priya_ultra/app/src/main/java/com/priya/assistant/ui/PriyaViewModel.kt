package com.priya.assistant.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.priya.assistant.PriyaApplication
import com.priya.assistant.accessibility.PriyaAccessibilityService
import com.priya.assistant.actions.ActionManager
import com.priya.assistant.actions.ActionParser
import com.priya.assistant.actions.PriyaAction
import com.priya.assistant.ai.AIResult
import com.priya.assistant.ai.PriyaPersonality
import com.priya.assistant.data.models.Conversation
import com.priya.assistant.data.models.MessageSender
import com.priya.assistant.voice.VoiceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * The one place where voice/text input, the AI, the action system, and TTS
 * output meet. Home's mic button and Chat's send button both funnel through
 * [sendUserMessage] so behavior (confirmation prompts, action execution,
 * speaking the reply) stays identical whether the user typed or spoke.
 */
class PriyaViewModel(private val app: PriyaApplication) : ViewModel() {

    private val actionManager = ActionManager(app)

    val voiceState: StateFlow<VoiceState> = app.voiceManager.state
    val voiceError: StateFlow<String?> = app.voiceManager.lastError

    private val _activeConversation = MutableStateFlow<Conversation?>(null)
    val activeConversation: StateFlow<Conversation?> = _activeConversation.asStateFlow()

    val allConversations = app.conversationRepository.conversations

    private val _pendingConfirmation = MutableStateFlow<PendingConfirmation?>(null)
    val pendingConfirmation: StateFlow<PendingConfirmation?> = _pendingConfirmation.asStateFlow()

    data class PendingConfirmation(val action: PriyaAction, val description: String)

    init {
        ensureActiveConversation()
        viewModelScope.launch {
            app.voiceManager.lastRecognizedText.collect { text ->
                if (!text.isNullOrBlank()) sendUserMessage(text)
            }
        }
    }

    fun ensureActiveConversation() {
        if (_activeConversation.value == null) {
            val existing = app.conversationRepository.conversations.value.firstOrNull()
            _activeConversation.value = existing ?: app.conversationRepository.createConversation()
        }
    }

    fun startNewConversation() {
        _activeConversation.value = app.conversationRepository.createConversation()
    }

    fun selectConversation(id: String) {
        _activeConversation.value = app.conversationRepository.conversations.value.find { it.id == id }
    }

    fun deleteConversation(id: String) {
        app.conversationRepository.deleteConversation(id)
        if (_activeConversation.value?.id == id) {
            _activeConversation.value = null
            ensureActiveConversation()
        }
    }

    fun clearAllHistory() {
        app.conversationRepository.clearAll()
        _activeConversation.value = null
        ensureActiveConversation()
    }

    fun onMicPressed() {
        if (voiceState.value == VoiceState.LISTENING) {
            app.voiceManager.stopListening()
        } else {
            if (!app.voiceManager.isRecognitionAvailable()) {
                app.voiceManager.setError("Speech recognition isn't available on this device.")
                return
            }
            app.voiceManager.startListening()
        }
    }

    fun stopSpeaking() = app.voiceManager.stopSpeaking()

    fun sendUserMessage(text: String) {
        val convo = _activeConversation.value ?: app.conversationRepository.createConversation().also {
            _activeConversation.value = it
        }
        app.conversationRepository.appendMessage(convo.id, MessageSender.USER, text)
        refreshActiveConversation(convo.id)

        viewModelScope.launch {
            val history = app.conversationRepository.recentHistory(convo.id)
            val result = app.aiManager.sendMessage(
                conversationHistory = history.dropLast(1),
                userMessage = text,
                actionsSystemPromptAddendum = PriyaPersonality.actionsContract
            )
            handleAIResult(convo.id, result)
        }
    }

    private fun handleAIResult(conversationId: String, result: AIResult) {
        when (result) {
            is AIResult.Error -> {
                app.voiceManager.setError(result.friendlyMessage)
                app.conversationRepository.appendMessage(conversationId, MessageSender.PRIYA, result.friendlyMessage)
                refreshActiveConversation(conversationId)
            }
            is AIResult.Success -> {
                val parsed = ActionParser.parse(result.reply)
                if (parsed.displayText.isNotBlank()) {
                    app.conversationRepository.appendMessage(conversationId, MessageSender.PRIYA, parsed.displayText)
                    refreshActiveConversation(conversationId)
                    app.voiceManager.speak(parsed.displayText)
                }

                for (action in parsed.actions) {
                    if (actionManager.requiresConfirmation(action)) {
                        _pendingConfirmation.value = PendingConfirmation(
                            action = action,
                            description = "Do you want me to ${action.type.lowercase().replace('_', ' ')}?"
                        )
                        break
                    }
                    app.voiceManager.setExecuting()
                    val outcome = actionManager.execute(action)
                    app.voiceManager.speak(outcome.message)
                    app.conversationRepository.appendMessage(conversationId, MessageSender.PRIYA, outcome.message)
                    refreshActiveConversation(conversationId)
                    if (!outcome.success) break
                }
            }
        }
    }

    fun confirmPendingAction() {
        val pending = _pendingConfirmation.value ?: return
        _pendingConfirmation.value = null
        val convoId = _activeConversation.value?.id ?: return
        app.voiceManager.setExecuting()
        val outcome = actionManager.execute(pending.action)
        app.voiceManager.speak(outcome.message)
        app.conversationRepository.appendMessage(convoId, MessageSender.PRIYA, outcome.message)
        refreshActiveConversation(convoId)
    }

    fun cancelPendingAction() {
        _pendingConfirmation.value = null
    }

    fun isAccessibilityEnabled(): Boolean = PriyaAccessibilityService.isEnabled()

    fun isAiConfigured(): Boolean = app.aiManager.isConfigured()

    private fun refreshActiveConversation(id: String) {
        _activeConversation.value = app.conversationRepository.conversations.value.find { it.id == id }
    }

    companion object {
        fun factory(app: PriyaApplication) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = PriyaViewModel(app) as T
        }
    }
}
