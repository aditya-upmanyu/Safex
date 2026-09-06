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

        serviceScope.launch {
            try {
                sendFraudAlert(riskScore, verdict)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send alert", e)
            } finally {
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    private suspend fun sendFraudAlert(riskScore: Int, verdict: String) {
        // Create high-priority notification
        createNotificationChannel()
        val notification = createAlertNotification(riskScore, verdict)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(ALERT_NOTIFICATION_ID, notification)

        // Send SMS to emergency contact if permission granted
        val emergencyContact = settingsDataStore.getEmergencyContactSync()
        if (emergencyContact.isNotBlank() && PermissionHelper.hasSmsPermission(this)) {
            sendEmergencySms(emergencyContact, riskScore)
        }

        Log.d(TAG, "Fraud alert sent: Score=$riskScore, Verdict=$verdict")
    }

    private fun sendEmergencySms(phoneNumber: String, riskScore: Int) {
        try {
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            val message = "SAFEx Alert: Suspicious call detected with risk score $riskScore. Check the app immediately."
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            
            Log.d(TAG, "Emergency SMS sent to $phoneNumber")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SMS", e)
        }
    }

    private fun createAlertNotification(riskScore: Int, verdict: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, Constants.FRAUD_ALERT_CHANNEL_ID)
            .setContentTitle("⚠️ Fraud Alert")
            .setContentText("Suspicious call detected! Risk: $riskScore/100")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.FRAUD_ALERT_CHANNEL_ID,
                Constants.FRAUD_ALERT_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical fraud alerts"
                enableVibration(true)
                enableLights(true)
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
