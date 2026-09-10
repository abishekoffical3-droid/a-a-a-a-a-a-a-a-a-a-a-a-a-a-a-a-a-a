package com.priya.assistant.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent
import android.graphics.Path
import android.os.Build
import android.view.accessibility.AccessibilityNodeInfo
import com.priya.assistant.actions.ActionOutcome

/**
 * PRIYA's hands on the device. Only ever invoked in direct response to a user
 * command (via ActionManager) — it never acts autonomously, and every method
 * returns a structured [ActionOutcome] rather than throwing, so a failed
 * action becomes a spoken sentence instead of a crash (spec §14).
 */
class PriyaAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEventInfoAll
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            notificationTimeout = 80
        }
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // PRIYA is command-driven, not event-driven: we don't react to arbitrary
        // screen events, only to explicit calls from ActionManager below.
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        if (instance === this) instance = null
    }

    // ---- Structured device-control API used by ActionManager ----

    fun performBack(): ActionOutcome {
        val ok = performGlobalAction(GLOBAL_ACTION_BACK)
        return ActionOutcome(ok, if (ok) "Went back." else "I couldn't go back.")
    }

    fun performHome(): ActionOutcome {
        val ok = performGlobalAction(GLOBAL_ACTION_HOME)
        return ActionOutcome(ok, if (ok) "Went home." else "I couldn't go home.")
    }

    fun performRecents(): ActionOutcome {
        val ok = performGlobalAction(GLOBAL_ACTION_RECENTS)
        return ActionOutcome(ok, if (ok) "Showing recent apps." else "I couldn't open recent apps.")
    }

    fun clickByText(text: String): ActionOutcome {
        val root = rootInActiveWindow
        val node = AccessibilityNodeHelper.findNodeByText(root, text)
            ?: AccessibilityNodeHelper.findNodeByContentDescription(root, text)
            ?: findContains(root, text)
        val ok = AccessibilityNodeHelper.clickNode(node)
        return ActionOutcome(ok, if (ok) "Tapped \"$text\"." else "I couldn't find that button.")
    }

    fun clickByViewId(viewId: String): ActionOutcome {
        val node = AccessibilityNodeHelper.findNodeByViewId(rootInActiveWindow, viewId)
        val ok = AccessibilityNodeHelper.clickNode(node)
        return ActionOutcome(ok, if (ok) "Tapped it." else "I couldn't find that element.")
    }

    fun longClickByText(text: String): ActionOutcome {
        val node = AccessibilityNodeHelper.findNodeByText(rootInActiveWindow, text)
        val ok = AccessibilityNodeHelper.longClickNode(node)
        return ActionOutcome(ok, if (ok) "Long-pressed \"$text\"." else "I couldn't find that to long-press.")
    }

    fun typeText(text: String): ActionOutcome {
        val root = rootInActiveWindow
        val focused = root?.findFocus(android.view.accessibility.AccessibilityNodeInfo.FOCUS_INPUT)
        val ok = AccessibilityNodeHelper.setText(focused, text)
        return ActionOutcome(ok, if (ok) "Typed it in." else "I couldn't find a text field to type into.")
    }

    fun scrollForward(): ActionOutcome {
        val ok = AccessibilityNodeHelper.scrollForward(rootInActiveWindow)
        return ActionOutcome(ok, if (ok) "Scrolled down." else "I couldn't scroll here.")
    }

    fun scrollBackward(): ActionOutcome {
        val ok = AccessibilityNodeHelper.scrollBackward(rootInActiveWindow)
        return ActionOutcome(ok, if (ok) "Scrolled up." else "I couldn't scroll here.")
    }

    fun clickAt(x: Float, y: Float): ActionOutcome {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return ActionOutcome(false, "Coordinate tapping needs Android 7 or newer.")
        val path = Path().apply { moveTo(x, y) }
        val gesture = android.accessibilityservice.GestureDescription.Builder().addStroke(android.accessibilityservice.GestureDescription.StrokeDescription(path, 0, 80)).build()
        val ok = dispatchGesture(gesture, null, null)
        return ActionOutcome(ok, if (ok) "Tapped there." else "I couldn't tap there.")
    }

    fun swipe(x1: Float, y1: Float, x2: Float, y2: Float, duration: Int): ActionOutcome {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return ActionOutcome(false, "Swipe needs Android 7 or newer.")
        val path = Path().apply { moveTo(x1, y1); lineTo(x2, y2) }
        val gesture = android.accessibilityservice.GestureDescription.Builder().addStroke(android.accessibilityservice.GestureDescription.StrokeDescription(path, 0, duration.coerceIn(80, 2000).toLong())).build()
        val ok = dispatchGesture(gesture, null, null)
        return ActionOutcome(ok, if (ok) "Done." else "I couldn't swipe there.")
    }

    fun mediaPlayPause(): ActionOutcome {
        val audio = getSystemService(AUDIO_SERVICE) as android.media.AudioManager
        audio.dispatchMediaKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE))
        audio.dispatchMediaKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE))
        return ActionOutcome(true, "Media toggled.")
    }

    private fun findContains(root: AccessibilityNodeInfo?, text: String): AccessibilityNodeInfo? {
        if (root == null) return null
        val needle = text.trim().lowercase()
        val value = root.text?.toString()?.lowercase()
        val desc = root.contentDescription?.toString()?.lowercase()
        if ((value?.contains(needle) == true || desc?.contains(needle) == true) && root.isVisibleToUser) return root
        for (i in 0 until root.childCount) findContains(root.getChild(i), text)?.let { return it }
        return null
    }

    fun readVisibleText(): String {
        val text = AccessibilityNodeHelper.collectVisibleText(rootInActiveWindow).toString()
        return text.ifBlank { "I don't see any readable text on screen right now." }
    }

    companion object {
        var instance: PriyaAccessibilityService? = null
            private set

        val AccessibilityEventInfoAll = AccessibilityEvent.TYPES_ALL_MASK

        fun isEnabled(): Boolean = instance != null
    }
}
