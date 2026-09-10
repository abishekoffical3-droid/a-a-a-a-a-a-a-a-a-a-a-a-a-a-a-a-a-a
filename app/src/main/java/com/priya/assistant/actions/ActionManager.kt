package com.priya.assistant.actions

import android.content.Context
import android.media.AudioManager
import android.provider.Settings
import com.priya.assistant.accessibility.PriyaAccessibilityService
import kotlin.math.roundToInt

private val CONFIRMATION_REQUIRED = setOf("SEND_MESSAGE", "DELETE_CONTENT", "MAKE_PURCHASE", "CHANGE_SETTING", "POST_CONTENT", "SEND_EMAIL", "PLACE_CALL")

class ActionManager(private val context: Context) {
    private val appLauncher = AppLauncher(context)
    fun requiresConfirmation(action: PriyaAction): Boolean = action.type in CONFIRMATION_REQUIRED

    fun execute(action: PriyaAction): ActionOutcome = when (action.type) {
        "OPEN_APP" -> {
            val pkg = action.getString("package"); val name = action.getString("name")
            when { !pkg.isNullOrBlank() -> appLauncher.launchByPackage(pkg); !name.isNullOrBlank() -> appLauncher.launchByFriendlyName(name); else -> ActionOutcome(false, "Tell me which app to open.") }
        }
        "OPEN_URL" -> action.getString("url")?.let(appLauncher::openUrl) ?: ActionOutcome(false, "I need a URL.")
        "WEB_SEARCH" -> action.getString("query")?.let(appLauncher::webSearch) ?: ActionOutcome(false, "What should I search for?")
        "YOUTUBE_SEARCH" -> action.getString("query")?.let(appLauncher::youtubeSearch) ?: ActionOutcome(false, "What should I search on YouTube?")
        "OPEN_SETTINGS" -> appLauncher.openAppSettings()
        "BACK" -> service { it.performBack() }
        "HOME" -> service { it.performHome() }
        "RECENTS" -> service { it.performRecents() }
        "CLICK_TEXT" -> action.getString("text")?.let { text -> service { it.clickByText(text) } } ?: ActionOutcome(false, "I need the text to tap.")
        "CLICK_ID" -> action.getString("id")?.let { id -> service { it.clickByViewId(id) } } ?: ActionOutcome(false, "I need the element id.")
        "CLICK_COORDINATE" -> service { it.clickAt(action.getFloat("x"), action.getFloat("y")) }
        "TYPE_TEXT" -> action.getString("text")?.let { text -> service { it.typeText(text) } } ?: ActionOutcome(false, "I need text to type.")
        "SCROLL_UP" -> service { it.scrollBackward() }
        "SCROLL_DOWN" -> service { it.scrollForward() }
        "SWIPE" -> service { it.swipe(action.getFloat("x1"), action.getFloat("y1"), action.getFloat("x2"), action.getFloat("y2"), action.getInt("duration", 350)) }
        "LONG_CLICK" -> action.getString("text")?.let { text -> service { it.longClickByText(text) } } ?: ActionOutcome(false, "I need what to long-press.")
        "READ_SCREEN" -> service { ActionOutcome(true, it.readVisibleText()) }
        "SET_VOLUME" -> setVolume(action.getInt("percent", 50))
        "SET_BRIGHTNESS" -> setBrightness(action.getInt("percent", 50))
        "MUTE" -> setVolume(0)
        "MEDIA_PLAY_PAUSE" -> service { it.mediaPlayPause() }
        "STOP_LISTENING" -> ActionOutcome(true, "Okay, I stopped listening.")
        else -> ActionOutcome(false, "I can't do that action yet.")
    }
    private fun service(block: (PriyaAccessibilityService) -> ActionOutcome): ActionOutcome = PriyaAccessibilityService.instance?.let(block) ?: ActionOutcome(false, "Please enable PRIYA Accessibility Access.")
    private fun setVolume(percent: Int): ActionOutcome {
        val p = percent.coerceIn(0, 100); val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val value = (audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * p / 100f).roundToInt()
        audio.setStreamVolume(AudioManager.STREAM_MUSIC, value, 0); return ActionOutcome(true, "Media volume set to $p percent.")
    }
    private fun setBrightness(percent: Int): ActionOutcome {
        val p = percent.coerceIn(1, 100)
        if (!Settings.System.canWrite(context)) return ActionOutcome(false, "Brightness control needs Write Settings permission.")
        Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, (255 * p / 100f).roundToInt())
        return ActionOutcome(true, "Brightness set to $p percent.")
    }
}
