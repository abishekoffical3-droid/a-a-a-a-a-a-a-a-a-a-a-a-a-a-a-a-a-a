package com.priya.assistant.accessibility

import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Small, careful wrappers around AccessibilityNodeInfo tree traversal. Every
 * public function recycles/releases nodes it owns and never assumes a node is
 * non-null further down the call stack.
 */
object AccessibilityNodeHelper {

    fun findNodeByText(root: AccessibilityNodeInfo?, text: String): AccessibilityNodeInfo? {
        root ?: return null
        val matches = root.findAccessibilityNodeInfosByText(text)
        return matches?.firstOrNull { it.isVisibleToUser }
    }

    fun findNodeByContentDescription(root: AccessibilityNodeInfo?, description: String): AccessibilityNodeInfo? {
        root ?: return null
        return searchTree(root) { node ->
            node.contentDescription?.toString()?.equals(description, ignoreCase = true) == true
        }
    }

    fun findNodeByViewId(root: AccessibilityNodeInfo?, viewId: String): AccessibilityNodeInfo? {
        root ?: return null
        val matches = root.findAccessibilityNodeInfosByViewId(viewId)
        return matches?.firstOrNull()
    }

    private fun searchTree(
        node: AccessibilityNodeInfo,
        predicate: (AccessibilityNodeInfo) -> Boolean
    ): AccessibilityNodeInfo? {
        if (predicate(node)) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = searchTree(child, predicate)
            if (result != null) return result
        }
        return null
    }

    fun clickNode(node: AccessibilityNodeInfo?): Boolean {
        node ?: return false
        var target: AccessibilityNodeInfo? = node
        while (target != null && !target.isClickable) {
            target = target.parent
        }
        return target?.performAction(AccessibilityNodeInfo.ACTION_CLICK) ?: false
    }

    fun longClickNode(node: AccessibilityNodeInfo?): Boolean {
        node ?: return false
        var target: AccessibilityNodeInfo? = node
        while (target != null && !target.isLongClickable) {
            target = target.parent
        }
        return target?.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK) ?: false
    }

    fun setText(node: AccessibilityNodeInfo?, text: String): Boolean {
        node ?: return false
        val arguments = Bundle()
        arguments.putCharSequence(
            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE_VALUE, text
        )
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
    }

    fun scrollForward(root: AccessibilityNodeInfo?): Boolean {
        val scrollable = findScrollableNode(root) ?: return false
        return scrollable.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
    }

    fun scrollBackward(root: AccessibilityNodeInfo?): Boolean {
        val scrollable = findScrollableNode(root) ?: return false
        return scrollable.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
    }

    private fun findScrollableNode(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        node ?: return null
        if (node.isScrollable) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findScrollableNode(child)
            if (result != null) return result
        }
        return null
    }

    /** Concatenates visible text nodes for a simple "what's on screen" readback. */
    fun collectVisibleText(node: AccessibilityNodeInfo?, out: StringBuilder = StringBuilder()): StringBuilder {
        node ?: return out
        if (node.isVisibleToUser) {
            val text = node.text?.toString()
            if (!text.isNullOrBlank()) {
                if (out.isNotEmpty()) out.append(". ")
                out.append(text)
            }
        }
        for (i in 0 until node.childCount) {
            collectVisibleText(node.getChild(i), out)
        }
        return out
    }
}
