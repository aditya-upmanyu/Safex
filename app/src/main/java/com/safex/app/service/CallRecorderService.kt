package com.safex.app.service

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.safex.app.R
import com.safex.app.presentation.MainActivity
import com.safex.app.util.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CallRecorderService : Service() {

    private val TAG = "CallRecorderService"
    private var isRecording = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START_RECORDING" -> startRecording()
            "STOP_RECORDING" -> stopRecording()
        }
        return START_NOT_STICKY
    }

    private fun startRecording() {
        if (isRecording) return

        val notification = createNotification()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE or
                ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        isRecording = true
        Log.d(TAG, "Recording started")

        // Note: Actual recording implementation would require MediaRecorder setup
        // This is a skeleton that respects Android limitations
    }

    private fun stopRecording() {
        if (!isRecording) return

        isRecording = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()

        // Trigger analysis pipeline
        val analysisIntent = Intent(this, AnalysisPipelineService::class.java)
        startService(analysisIntent)

        Log.d(TAG, "Recording stopped")
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, Constants.CALL_RECORDING_CHANNEL_ID)
            .setContentTitle("SAFEx Call Guard")
            .setContentText("Monitoring call for security")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.CALL_RECORDING_CHANNEL_ID,
                Constants.CALL_RECORDING_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows when SAFEx is monitoring a call"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val NOTIFICATION_ID = 1001
    }
}
