package com.safex.app.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.safex.app.R
import com.safex.app.data.datastore.SettingsDataStore
import com.safex.app.domain.model.RiskLevel
import com.safex.app.presentation.MainActivity
import com.safex.app.util.Constants
import com.safex.app.util.PermissionHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class FraudAlertService : Service() {

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    private val TAG = "FraudAlertService"
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val riskScore = intent?.getIntExtra("RISK_SCORE", 0) ?: 0
        val verdict = intent?.getStringExtra("VERDICT") ?: "UNKNOWN"
        val reason = intent?.getStringExtra("REASON") ?: "Suspicious activity detected"
        val reasonHindi = intent?.getStringExtra("REASON_HINDI") ?: "संदिग्ध गतिविधि का पता चला"
        val action = intent?.getStringExtra("ACTION") ?: "Please check the app"

        serviceScope.launch {
            try {
                sendFraudAlert(riskScore, verdict, reason, reasonHindi, action)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send alert", e)
            } finally {
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    private suspend fun sendFraudAlert(
        riskScore: Int,
        verdict: String,
        reason: String,
        reasonHindi: String,
        action: String
    ) {
        // Create high-priority notification
        createNotificationChannel()
        val notification = createAlertNotification(riskScore, verdict, reason)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(ALERT_NOTIFICATION_ID, notification)

        // Also create high-importance alert
        if (riskScore >= 70) {
            createHighRiskAlert(reason, reasonHindi, riskScore)
        }

        // Send SMS to emergency contact if permission granted and high risk
        val emergencyContact = settingsDataStore.getEmergencyContactSync()
        if (emergencyContact.isNotBlank() && riskScore >= 70 && PermissionHelper.hasSmsPermission(this)) {
            sendEmergencySms(emergencyContact, riskScore, reason)
        }

        Log.d(TAG, "🚨 Fraud alert sent: Score=$riskScore, Verdict=$verdict, Reason=$reason")
    }

    private suspend fun createHighRiskAlert(reason: String, reasonHindi: String, riskScore: Int) {
        withContext(Dispatchers.Main) {
            // Future: Show alert dialog if app is in foreground
            Log.w(TAG, "⚠️ HIGH RISK ALERT: $reason ($reasonHindi) - Risk: $riskScore")
        }
    }

    private fun sendEmergencySms(phoneNumber: String, riskScore: Int, reason: String) {
        try {
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            val shortReason = reason.take(50)
            val message = "🚨 SAFEx Alert: Fraud detected (Score: $riskScore/100). $shortReason. Check app immediately! #SafetyFirst"
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            
            Log.d(TAG, "📱 Emergency SMS sent to $phoneNumber")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SMS", e)
        }
    }

    private fun createAlertNotification(riskScore: Int, verdict: String, reason: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val riskLevel = when {
            riskScore >= 70 -> "FRAUD ALERT 🚨"
            riskScore >= 40 -> "⚠️ Suspicious"
            else -> "Safe ✓"
        }

        val title = if (riskScore >= 70) {
            "🚨 FRAUD DETECTED - DIGITAL ARREST SCAM"
        } else if (riskScore >= 40) {
            "⚠️ Suspicious Call"
        } else {
            "✓ Call Safe"
        }

        return NotificationCompat.Builder(this, Constants.FRAUD_ALERT_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText("Risk: $riskScore/100 - $reason")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$title\n\nRisk Score: $riskScore/100\n\nReason: $reason\n\nTap to view details")
            )
            .setColor(when {
                riskScore >= 70 -> 0xFFFF0000.toInt() // Red
                riskScore >= 40 -> 0xFFFF9800.toInt() // Orange
                else -> 0xFF4CAF50.toInt() // Green
            })
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.FRAUD_ALERT_CHANNEL_ID,
                Constants.FRAUD_ALERT_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical fraud alerts from SAFEx"
                enableVibration(true)
                enableLights(true)
                lightColor = 0xFFFF0000.toInt() // Red light
                setShowBadge(true)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        private const val ALERT_NOTIFICATION_ID = 2001
    }
}

