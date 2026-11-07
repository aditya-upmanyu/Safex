package com.safex.app.core.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.provider.Settings
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

class RemoteAccessWarningOverlay(private val context: Context) {

    private val TAG = "RemoteAccessOverlay"
    private var windowManager: WindowManager? = null
    private var overlayView: ComposeView? = null
    private var isShowing = false
    private var lastShownAt: Long = 0L
    private val COOLDOWN_MS = 30_000L

    fun show(
        appName: String,
        appPackage: String,
        warningText: String,
        onDismiss: () -> Unit,
        onOpenAppSettings: () -> Unit,
        onCallCyberHelpline: () -> Unit
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
                else
                    @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                y = 80
            }

            val composeView = ComposeView(context).apply {
                val lo = OverlayLifecycleOwner().also { it.start() }
                setViewTreeLifecycleOwner(lo)
                setViewTreeSavedStateRegistryOwner(lo)
                setContent {
                    RemoteAccessCard(
                        appName = appName,
                        warningText = warningText,
                        onDismiss = { dismiss(); onDismiss() },
                        onOpenSettings = onOpenAppSettings,
                        onCallHelpline = onCallCyberHelpline
                    )
                }
            }
            overlayView = composeView
            windowManager?.addView(composeView, lp)
            isShowing = true
            lastShownAt = now
            Log.d(TAG, "Remote access warning shown for: $appName")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show overlay: ${e.message}")
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
private fun RemoteAccessCard(
    appName: String,
    warningText: String,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
    onCallHelpline: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(10.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF4A148C)),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🔴 REMOTE ACCESS ALERT", color = Color.White,
                fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(warningText, color = Color.White, fontSize = 13.sp, lineHeight = 19.sp)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onOpenSettings,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.weight(1f)
                ) { Text("App Settings", color = Color(0xFF4A148C), fontSize = 11.sp) }
                Button(
                    onClick = onCallHelpline,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD600)),
                    modifier = Modifier.weight(1f)
                ) { Text("Call 1930", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            }
            Spacer(Modifier.height(6.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                Text("Dismiss", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }
    }
}

private class OverlayLifecycleOwner : LifecycleOwner, SavedStateRegistryOwner {
    private val lr = LifecycleRegistry(this)
    private val src = SavedStateRegistryController.create(this)
    override val lifecycle: Lifecycle get() = lr
    override val savedStateRegistry: SavedStateRegistry get() = src.savedStateRegistry
    fun start() { src.performAttach(); src.performRestore(null); lr.currentState = Lifecycle.State.STARTED }
}
