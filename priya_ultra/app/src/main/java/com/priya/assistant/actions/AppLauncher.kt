package com.priya.assistant.actions

import android.content.Context
import android.content.Intent
import android.net.Uri

data class ActionOutcome(val success: Boolean, val message: String)

class AppLauncher(private val context: Context) {

    /** Common apps the user/AI can refer to by friendly name instead of a package. */
    val knownPackages: Map<String, String> = mapOf(
        "youtube" to "com.google.android.youtube",
        "chrome" to "com.android.chrome",
        "gmail" to "com.google.android.gm",
        "maps" to "com.google.android.apps.maps",
        "instagram" to "com.instagram.android",
        "facebook" to "com.facebook.katana",
        "tiktok" to "com.zhiliaoapp.musically",
        "whatsapp" to "com.whatsapp",
        "spotify" to "com.spotify.music",
        "drive" to "com.google.android.apps.docs",
        "github" to "com.github.android",
        "play store" to "com.android.vending",
        "google" to "com.google.android.googlequicksearchbox",
        "photos" to "com.google.android.apps.photos",
        "clock" to "com.google.android.deskclock",
        "calendar" to "com.google.android.calendar",
        "contacts" to "com.google.android.contacts"
    )

    fun launchByPackage(packageName: String): ActionOutcome {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        return if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            ActionOutcome(true, "Opening ${appLabel(packageName)}.")
        } else {
            ActionOutcome(false, "That app isn't installed on this device.")
        }
    }

    fun launchByFriendlyName(name: String): ActionOutcome {
        val key = name.trim().lowercase()
        knownPackages[key]?.let { return launchByPackage(it) }
        val pm = context.packageManager
        val match = pm.getInstalledApplications(0).firstOrNull { pm.getApplicationLabel(it).toString().lowercase().contains(key) }
        return if (match != null) launchByPackage(match.packageName) else ActionOutcome(false, "I couldn't find an installed app named $name.")
    }

    fun openUrl(url: String): ActionOutcome = runCatching {
        val safeUrl = if (url.startsWith("http")) url else "https://$url"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(safeUrl)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        ActionOutcome(true, "Opening that link.")
    }.getOrElse { ActionOutcome(false, "I couldn't open that link.") }

    fun webSearch(query: String): ActionOutcome =
        openUrl("https://www.google.com/search?q=${Uri.encode(query)}")

    fun youtubeSearch(query: String): ActionOutcome =
        openUrl("https://www.youtube.com/results?search_query=${Uri.encode(query)}")

    fun openAppSettings(): ActionOutcome = runCatching {
        val intent = Intent(android.provider.Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        ActionOutcome(true, "Opening settings.")
    }.getOrElse { ActionOutcome(false, "I couldn't open settings.") }

    private fun appLabel(packageName: String): String = runCatching {
        val pm = context.packageManager
        val info = pm.getApplicationInfo(packageName, 0)
        pm.getApplicationLabel(info).toString()
    }.getOrDefault(packageName)
}
