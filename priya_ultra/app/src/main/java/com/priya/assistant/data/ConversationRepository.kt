package com.priya.assistant.data

import android.content.Context
import com.priya.assistant.data.models.ChatMessage
import com.priya.assistant.data.models.Conversation
import com.priya.assistant.data.models.MessageSender
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

/**
 * Local-only conversation history, persisted as a single JSON file in app-private
 * storage. Nothing here is ever transmitted; AIManager separately (and only) sends
 * a trimmed slice of a conversation to whichever provider the user selected.
 */
class ConversationRepository(context: Context) {

    private val storageFile = File(context.filesDir, "priya_conversations.json")

    private val _conversations = MutableStateFlow<List<Conversation>>(loadFromDisk())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    fun createConversation(): Conversation {
        val convo = Conversation(
            id = UUID.randomUUID().toString(),
            title = "New Chat",
            messages = emptyList(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        _conversations.value = listOf(convo) + _conversations.value
        persist()
        return convo
    }

    fun appendMessage(conversationId: String, sender: MessageSender, text: String) {
        val now = System.currentTimeMillis()
        _conversations.value = _conversations.value.map { convo ->
            if (convo.id != conversationId) return@map convo
            val newMessages = convo.messages + ChatMessage(UUID.randomUUID().toString(), sender, text, now)
            val newTitle = if (convo.messages.isEmpty() && sender == MessageSender.USER) {
                text.take(40)
            } else convo.title
            convo.copy(messages = newMessages, updatedAt = now, title = newTitle)
        }
        persist()
    }

    fun deleteConversation(conversationId: String) {
        _conversations.value = _conversations.value.filterNot { it.id == conversationId }
        persist()
    }

    fun clearAll() {
        _conversations.value = emptyList()
        persist()
    }

    /** Last [maxMessages] messages, oldest first — used to bound tokens sent to the AI provider. */
    fun recentHistory(conversationId: String, maxMessages: Int = 12): List<ChatMessage> {
        val convo = _conversations.value.find { it.id == conversationId } ?: return emptyList()
        return convo.messages.takeLast(maxMessages)
    }

    private fun persist() {
        val arr = JSONArray()
        _conversations.value.forEach { convo ->
            val convoObj = JSONObject()
            convoObj.put("id", convo.id)
            convoObj.put("title", convo.title)
            convoObj.put("createdAt", convo.createdAt)
            convoObj.put("updatedAt", convo.updatedAt)
            val msgsArr = JSONArray()
            convo.messages.forEach { msg ->
                val msgObj = JSONObject()
                msgObj.put("id", msg.id)
                msgObj.put("sender", msg.sender.name)
                msgObj.put("text", msg.text)
                msgObj.put("timestamp", msg.timestamp)
                msgsArr.put(msgObj)
            }
            convoObj.put("messages", msgsArr)
            arr.put(convoObj)
        }
        runCatching { storageFile.writeText(arr.toString()) }
    }

    private fun loadFromDisk(): List<Conversation> {
        if (!storageFile.exists()) return emptyList()
        return runCatching {
            val arr = JSONArray(storageFile.readText())
            (0 until arr.length()).map { i ->
                val convoObj = arr.getJSONObject(i)
                val msgsArr = convoObj.getJSONArray("messages")
                val messages = (0 until msgsArr.length()).map { j ->
                    val msgObj = msgsArr.getJSONObject(j)
                    ChatMessage(
                        id = msgObj.getString("id"),
                        sender = MessageSender.valueOf(msgObj.getString("sender")),
                        text = msgObj.getString("text"),
                        timestamp = msgObj.getLong("timestamp")
                    )
                }
                Conversation(
                    id = convoObj.getString("id"),
                    title = convoObj.getString("title"),
                    messages = messages,
                    createdAt = convoObj.getLong("createdAt"),
                    updatedAt = convoObj.getLong("updatedAt")
                )
            }
        }.getOrDefault(emptyList())
    }
}
