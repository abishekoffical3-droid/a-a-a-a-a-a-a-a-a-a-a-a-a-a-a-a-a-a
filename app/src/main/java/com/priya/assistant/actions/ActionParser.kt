package com.priya.assistant.actions

import org.json.JSONArray
import org.json.JSONObject

data class PriyaAction(val type: String, val raw: JSONObject) {
    fun getString(key: String): String? = if (raw.has(key)) raw.optString(key) else null
    fun getInt(key: String, default: Int = 0): Int = raw.optInt(key, default)
    fun getFloat(key: String, default: Float = 0f): Float = raw.optDouble(key, default.toDouble()).toFloat()
}

object ActionParser {
    private val actionBlockRegex = Regex("```action\\s*([\\s\\S]*?)```", RegexOption.IGNORE_CASE)
    data class ParsedReply(val displayText: String, val actions: List<PriyaAction>) { val action get() = actions.firstOrNull() }
    fun parse(reply: String): ParsedReply {
        val match = actionBlockRegex.find(reply) ?: return ParsedReply(reply.trim(), emptyList())
        val jsonText = match.groupValues[1].trim()
        val actions = mutableListOf<PriyaAction>()
        runCatching {
            if (jsonText.startsWith("[")) {
                val array = JSONArray(jsonText)
                for (i in 0 until array.length()) add(array.optJSONObject(i), actions)
            } else add(JSONObject(jsonText), actions)
        }
        val displayText = reply.replace(match.value, "").trim()
        return ParsedReply(displayText.ifBlank { if (actions.isNotEmpty()) "On it." else "" }, actions)
    }
    private fun add(obj: JSONObject?, out: MutableList<PriyaAction>) {
        if (obj == null) return
        val type = obj.optString("type").uppercase()
        if (type.isNotBlank()) out += PriyaAction(type, obj)
    }
}
