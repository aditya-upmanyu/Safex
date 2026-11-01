package com.safex.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import com.safex.app.service.CallRecorderService
import dagger.hilt.android.AndroidEntryPoint

/**
 * Detects incoming calls via TelephonyManager
 * More reliable than accessibility service for call detection
 */
@AndroidEntryPoint
class CallStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) ?: "Unknown"

        Log.d("CallStateReceiver", "Call state: $state, Number: $incomingNumber")

        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                // Incoming call ringing
                Log.d("CallStateReceiver", "📞 Incoming call from: $incomingNumber")
                startCallRecording(context, incomingNumber)
            }
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                // Call is being answered
                Log.d("CallStateReceiver", "☎️ Call picked up")
            }
            TelephonyManager.EXTRA_STATE_IDLE -> {
                // Call ended
                Log.d("CallStateReceiver", "📴 Call ended")
                stopCallRecording(context)
            }
        }
    }

    private fun startCallRecording(context: Context, phoneNumber: String) {
        try {
            val intent = Intent(context, CallRecorderService::class.java).apply {
                action = "START_RECORDING"
                putExtra("PHONE_NUMBER", phoneNumber)
                putExtra("CALL_START_TIME", System.currentTimeMillis())
            }
            context.startService(intent)
            Log.d("CallStateReceiver", "✅ Call recording started for: $phoneNumber")
        } catch (e: Exception) {
            Log.e("CallStateReceiver", "Failed to start recording", e)
        }
    }

    private fun stopCallRecording(context: Context) {
        try {
            val intent = Intent(context, CallRecorderService::class.java).apply {
                action = "STOP_RECORDING"
            }
            context.startService(intent)
            Log.d("CallStateReceiver", "✅ Call recording stopped")
        } catch (e: Exception) {
            Log.e("CallStateReceiver", "Failed to stop recording", e)
        }
    }
}
