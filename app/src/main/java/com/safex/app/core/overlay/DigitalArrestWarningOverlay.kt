package com.safex.app.core.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
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

class DigitalArrestWarningOverlay(private val context: Context) {

    private val TAG = "DigitalArrestOverlay"
    private var windowManager: WindowManager? = null
    private var overlayView: ComposeView? = null
    private var isShowing = false
    private var lastShownAt: Long = 0L
    private val COOLDOWN_MS = 120_000L // 2-minute cooldown for this critical alert

    fun show(
        scoreLabel: String,
        onDismiss: () -> Unit,
        onDisconnectGuidance: () -> Unit,
        onCallCyberHelpline: () -> Unit,
        onReportCybercrime: () -> Unit
    ) {
        if (isShowing) return
        val now = System.currentTimeMillis()
        if (now - lastShownAt < COOLDOWN_MS) return

        try {
            windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val lp = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply { gravity = Gravity.TOP; y = 60 }

            val view = ComposeView(context).apply {
                val lo = DALifecycleOwner().also { it.start() }
                setViewTreeLifecycleOwner(lo)
                setViewTreeSavedStateRegistryOwner(lo)
                setContent {
                    DigitalArrestCard(
                        scoreLabel = scoreLabel,
                        onDismiss = { dismiss(); onDismiss() },
                        onDisconnect = onDisconnectGuidance,
                        onCallHelpline = onCallCyberHelpline,
                        onReport = onReportCybercrime
                    )
                }
            }
            overlayView = view
            windowManager?.addView(view, lp)
            isShowing = true
            lastShownAt = now
            Log.d(TAG, "Digital arrest warning shown")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show digital arrest overlay: ${e.message}")
        }
    }

    fun dismiss() {
        try { overlayView?.let { windowManager?.removeView(it) } } catch (_: Exception) {}
        overlayView = null
        isShowing = false
    }

    fun isVisible() = isShowing
}

@Composable
private fun DigitalArrestCard(
    scoreLabel: String,
    onDismiss: () -> Unit,
    onDisconnect: () -> Unit,
    onCallHelpline: () -> Unit,
    onReport: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(10.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF880E4F)),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "🚨 LEGAL REALITY CHECK  [$scoreLabel]",
                color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Indian law enforcement does NOT conduct a legitimate arrest through a video call " +
                "or ask citizens to transfer money to a so-called verification account.\n\n" +
                "You may be facing a DIGITAL ARREST SCAM.\n\nDisconnect and independently verify the caller.",
                color = Color.White, fontSize = 13.sp, lineHeight = 19.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "भारतीय कानून प्रवर्तन वीडियो कॉल से गिरफ्तारी नहीं करती। यह डिजिटल अरेस्ट साइबर धोखाधड़ी हो सकती है।",
                color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp, lineHeight = 17.sp
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = onCallHelpline,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD600)),
                    modifier = Modifier.weight(1f)
                ) { Text("Call 1930", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                Button(
                    onClick = onReport,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.weight(1f)
                ) { Text("Report", color = Color(0xFF880E4F), fontSize = 11.sp) }
                Button(
                    onClick = onDisconnect,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
                    modifier = Modifier.weight(1f)
                ) { Text("Disconnect", color = Color.White, fontSize = 11.sp) }
            }
            Spacer(Modifier.height(6.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                Text("Dismiss", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            }
        }
    }
}

private class DALifecycleOwner : LifecycleOwner, SavedStateRegistryOwner {
    private val lr = LifecycleRegistry(this)
    private val src = SavedStateRegistryController.create(this)
    override val lifecycle: Lifecycle get() = lr
    override val savedStateRegistry: SavedStateRegistry get() = src.savedStateRegistry
    fun start() { src.performAttach(); src.performRestore(null); lr.currentState = Lifecycle.State.STARTED }
}
