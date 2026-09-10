package com.priya.assistant.accessibility

/**
 * Names of the device-control actions PRIYA's AI system prompt is allowed to
 * request via the ```action``` JSON block (see ActionParser/ActionManager).
 * Centralized here so the accessibility layer and the AI system prompt
 * (PriyaPersonality) stay in sync about what's actually supported.
 */
object AccessibilityActionNames {
    const val CLICK_TEXT = "CLICK_TEXT"
    const val CLICK_ID = "CLICK_ID"
    const val TYPE_TEXT = "TYPE_TEXT"
    const val SCROLL_UP = "SCROLL_UP"
    const val SCROLL_DOWN = "SCROLL_DOWN"
    const val LONG_CLICK = "LONG_CLICK"
    const val BACK = "BACK"
    const val HOME = "HOME"
    const val RECENTS = "RECENTS"
    const val READ_SCREEN = "READ_SCREEN"

    val ALL = setOf(CLICK_TEXT, CLICK_ID, TYPE_TEXT, SCROLL_UP, SCROLL_DOWN, LONG_CLICK, BACK, HOME, RECENTS, READ_SCREEN)
}
