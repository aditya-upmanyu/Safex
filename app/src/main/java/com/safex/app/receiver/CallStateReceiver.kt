package com.safex.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import com.safex.app.core.telephony.TelephonyCallStateManager
import com.safex.app.service.CallRecorderService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CallStateReceiver : BroadcastReceiver() {

    @Inject lateinit var callStateManager: TelephonyCallStateManager

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return
        val incomingNumber = "hidden" // EXTRA_INCOMING_NUMBER deprecated; do not log

        Log.d("CallStateReceiver", "Call state: $state")

        // Update the centralised call state manager used by ThreatCorrelationEngine
        callStateManager.updateFromBroadcast(state)

        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING,
            TelephonyManager.EXTRA_STATE_OFFHOOK -> startCallRecording(context)
            TelephonyManager.EXTRA_STATE_IDLE -> stopCallRecording(context)
        }
    }

    private fun startCallRecording(context: Context) {
        try {
            val intent = Intent(context, CallRecorderService::class.java).apply {
                action = "START_RECORDING"
                putExtra("CALL_START_TIME", System.currentTimeMillis())
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            Log.e("CallStateReceiver", "Failed to start recording", e)
        }
    }

    private fun stopCallRecording(context: Context) {
        try {
            context.startService(
                Intent(context, CallRecorderService::class.java).apply {
                    action = "STOP_RECORDING"
                }
            )
        } catch (e: Exception) {
            Log.e("CallStateReceiver", "Failed to stop recording", e)
        }
    }
}
