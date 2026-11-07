package com.safex.app.core.overlay

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import android.util.Log

/**
 * Overlay warning for UPI fraud risk.
 *
 * Uses TYPE_ACCESSIBILITY_OVERLAY (no SYSTEM_ALERT_WINDOW needed when shown
 * from the AccessibilityService). Falls back to TYPE_APPLICATION_OVERLAY
 * when shown from a regular service context.
 *
 * Does NOT collect credentials, read screen content, or intercept touch events
 * on the underlying UPI app.
 */
class UpiWarningOverlay(private val context: Context) {

    private val TAG = "UpiWarningOverlay"
    private var windowManager: WindowManager? = null
    private var overlayView: ComposeView? = null
    private var isShowing = false

    // Cooldown: don't re-show within 60 seconds for the same app
    private var lastShownAt: Long = 0L
    private val COOLDOWN_MS = 60_000L

    fun show(
        upiAppName: String,
        isCallActive: Boolean,
        warningText: String,
        onDismiss: () -> Unit,
        onCallCyberHelpline: () -> Unit,
        onLeaveApp: () -> Unit
    ) {
        if (isShowing) return
        val now = System.currentTimeMillis()
        if (now - lastShownAt < COOLDOWN_MS && !isCallActive) return

        try {
            windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

            val layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                y = 120
            }

            val composeView = ComposeView(context).apply {
                // Required lifecycle owner for Compose in window
                val lifecycleOwner = SimpleLifecycleOwner()
                lifecycleOwner.start()
                setViewTreeLifecycleOwner(lifecycleOwner)
                setViewTreeSavedStateRegistryOwner(lifecycleOwner)

                setContent {
                    UpiWarningCard(
                        appName = upiAppName,
                        isCallActive = isCallActive,
                        warningText = warningText,
                        onDismiss = {
                            dismiss()
                            onDismiss()
                        },
                        onCallCyberHelpline = onCallCyberHelpline,
                        onLeaveApp = onLeaveApp
                    )
                }
            }

            overlayView = composeView
            windowManager?.addView(composeView, layoutParams)
            isShowing = true
            lastShownAt = now
            Log.d(TAG, "UPI warning overlay shown for: $upiAppName")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to show UPI overlay: ${e.message}")
        }
    }

    fun dismiss() {
        try {
            overlayView?.let { windowManager?.removeView(it) }
        } catch (_: Exception) {}
        overlayView = null
        isShowing = false
    }

    fun isVisible(): Boolean = isShowing
}

@Composable
private fun UpiWarningCard(
    appName: String,
    isCallActive: Boolean,
    warningText: String,
    onDismiss: () -> Unit,
    onCallCyberHelpline: () -> Unit,
    onLeaveApp: () -> Unit
) {
    val bgColor = if (isCallActive) Color(0xFFB71C1C) else Color(0xFFE65100)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (isCallActive) "⚠️ FRAUD RISK" else "ℹ️ UPI ALERT",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = warningText,
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(12.dp))

            if (isCallActive) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onLeaveApp,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Leave App", color = bgColor, fontSize = 12.sp)
                    }
                    Button(
                        onClick = onCallCyberHelpline,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD600)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Call 1930", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = if (isCallActive) "I'm Safe — Dismiss" else "Dismiss",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp
                )
            }
        }
    }
}

/**
 * Minimal LifecycleOwner required for hosting ComposeView outside an Activity.
 */
private class SimpleLifecycleOwner : LifecycleOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    fun start() {
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    fun destroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }
}
