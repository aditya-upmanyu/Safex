package com.safex.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CallDetectorService : AccessibilityService() {

    private val TAG = "CallDetectorService"

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                handleWindowStateChanged(event)
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                handleWindowContentChanged(event)
            }
        }
    }

    private fun handleWindowStateChanged(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        val className = event.className?.toString() ?: return

        // Detect call-related packages
        val isCallPackage = packageName.contains("dialer", ignoreCase = true) ||
                packageName.contains("phone", ignoreCase = true) ||
                packageName.contains("incallui", ignoreCase = true) ||
                packageName.contains("whatsapp", ignoreCase = true) ||
                packageName.contains("skype", ignoreCase = true)

        if (isCallPackage) {
            Log.d(TAG, "Detected call UI from: $packageName")
            // Start call recording service if enabled
            val intent = Intent(this, CallRecorderService::class.java)
            intent.action = "START_RECORDING"
            try {
                startForegroundService(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start recording service", e)
            }
        }
    }

    private fun handleWindowContentChanged(event: AccessibilityEvent) {
        // Detect video call indicators
        val contentDescription = event.contentDescription?.toString() ?: ""
        if (contentDescription.contains("video", ignoreCase = true) ||
            contentDescription.contains("camera", ignoreCase = true)) {
            Log.d(TAG, "Video call detected")
            // Could trigger digital arrest shield here
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Service connected")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
    }
}
