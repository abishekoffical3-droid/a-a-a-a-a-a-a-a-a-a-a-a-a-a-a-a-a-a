package com.priya.assistant.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Stores API keys using AndroidX Security-Crypto, which wraps a Keystore-backed
 * master key. Keys never touch plain SharedPreferences, logs, or crash reports.
 */
class SecureKeyManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "priya_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveKey(providerId: String, apiKey: String) {
        prefs.edit().putString(keyFor(providerId), apiKey).apply()
    }

    fun getKey(providerId: String): String? = prefs.getString(keyFor(providerId), null)

    fun hasKey(providerId: String): Boolean = !getKey(providerId).isNullOrBlank()

    fun deleteKey(providerId: String) {
        prefs.edit().remove(keyFor(providerId)).apply()
    }

    fun clearAllKeys() {
        prefs.edit().clear().apply()
    }

    /** Returns a masked representation safe to display in the UI, e.g. "sk-••••••••1a2b". */
    fun maskedKey(providerId: String): String? {
        val key = getKey(providerId) ?: return null
        if (key.length <= 6) return "•".repeat(key.length)
        val suffix = key.takeLast(4)
        return "•".repeat(10) + suffix
    }

    private fun keyFor(providerId: String) = "api_key_$providerId"
}
