package com.safex.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.safex.app.core.security.ThreatCorrelationEngine
import com.safex.app.core.security.ThreatStateManager
import com.safex.app.core.security.model.DetectionStatus
import com.safex.app.core.security.model.ThreatEvent
import com.safex.app.core.security.model.ThreatLevel
import com.safex.app.core.security.model.ThreatModule
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Central accessibility service for SafeX.
 *
 * Monitors:
 * 1. Call UI transitions (original behavior, preserved)
 * 2. UPI app foreground detection (Module 1)
 * 3. Remote access app foreground detection (Module 2)
 *
 * Publishes foreground package changes to ThreatCorrelationEngine.
 * Never reads screen content for credentials — only package names.
 */
@AndroidEntryPoint
class CallDetectorService : AccessibilityService() {

    @Inject lateinit var threatCorrelationEngine: ThreatCorrelationEngine
    @Inject lateinit var threatStateManager: ThreatStateManager

    private val TAG = "CallDetectorService"

    // UPI app packages to monitor
    private val upiPackages = setOf(
        "com.google.android.apps.nbu.paisa.user",  // Google Pay
        "com.phonepe.app",                          // PhonePe
        "net.one97.paytm",                          // Paytm
        "in.org.npci.upiapp",                       // BHIM
        "com.dreamplug.androidapp",                 // CRED
        "com.sbi.lotusintouch",                     // YONO SBI
        "com.csam.icici.bank.imobile"               // iMobile
    )

    // Remote access app packages to monitor
    private val remoteAccessPackages = setOf(
        "com.anydesk.anydeskandroid",
        "com.teamviewer.quicksupport.market",
        "com.teamviewer.teamviewer",
        "com.carriez.rustdesk",
        "com.splashtop.remote",
        "com.zoho.assist"
    )

    // Call UI packages (original behavior)
    private val callPackages = setOf(
        "com.android.incallui",
        "com.google.android.dialer",
        "com.samsung.android.incallui",
        "com.oneplus.dialer"
    )

    private var lastPackage: String = ""
    private var lastEventTime: Long = 0L
    private val THROTTLE_MS = 500L // Throttle event processing

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "CallDetectorService connected — monitoring UPI, remote access, and call UI")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val now = System.currentTimeMillis()
        if (now - lastEventTime < THROTTLE_MS) return
        lastEventTime = now

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> handleForegroundChange(event)
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> { /* throttled, no tree scan */ }
        }
    }

    private fun handleForegroundChange(event: AccessibilityEvent) {
        val pkg = event.packageName?.toString() ?: return
        if (pkg == lastPackage) return
        lastPackage = pkg

        Log.d(TAG, "Foreground: $pkg")

        // Notify correlation engine of current foreground app
        threatCorrelationEngine.updateForegroundPackage(pkg)

        // UPI app detection
        val isUpi = upiPackages.contains(pkg)
        threatCorrelationEngine.updateUpiForeground(isUpi)
        if (isUpi) {
            threatStateManager.reportEvent(ThreatEvent(
                module = ThreatModule.UPI_SHIELD,
                level = ThreatLevel.LOW,
                detectionStatus = DetectionStatus.DETECTED,
                title = "UPI app opened",
                description = "UPI application is now in foreground: $pkg",
                packageName = pkg
            ))
            Log.d(TAG, "UPI app detected: $pkg")
        }

        // Remote access app detection
        val isRemote = remoteAccessPackages.contains(pkg)
        threatCorrelationEngine.updateRemoteAccessForeground(isRemote)
        if (isRemote) {
            threatStateManager.reportEvent(ThreatEvent(
                module = ThreatModule.REMOTE_ACCESS_SENTRY,
                level = ThreatLevel.MODERATE,
                detectionStatus = DetectionStatus.DETECTED,
                title = "Remote access app in foreground",
                description = "Known remote access application is active: $pkg",
                packageName = pkg,
                requiresImmediateWarning = true
            ))
            Log.d(TAG, "Remote access app detected: $pkg")
        }

        // Call UI — start recording service (original behavior)
        if (callPackages.any { pkg.contains(it) } ||
            pkg.contains("dialer", ignoreCase = true) ||
            pkg.contains("incallui", ignoreCase = true)) {
            val intent = Intent(this, CallRecorderService::class.java)
            intent.action = "START_RECORDING"
            try { startForegroundService(intent) } catch (e: Exception) {
                Log.e(TAG, "Failed to start recording service", e)
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
        threatCorrelationEngine.updateForegroundPackage("")
        threatCorrelationEngine.updateUpiForeground(false)
        threatCorrelationEngine.updateRemoteAccessForeground(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
    }
}
