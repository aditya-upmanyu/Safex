package com.safex.app.service

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.safex.app.R
import com.safex.app.presentation.MainActivity
import com.safex.app.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class CallRecorderService : Service() {

    private val TAG = "CallRecorderService"
    private var isRecording = false
    private var mediaRecorder: MediaRecorder? = null
    private var recordingFile: File? = null
    private var callStartTime: Long = 0L
    private var callPhoneNumber: String = "Unknown"
    private var callDuration: Long = 0L

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START_RECORDING" -> {
                callPhoneNumber = intent.getStringExtra("PHONE_NUMBER") ?: "Unknown"
                startRecording()
            }
            "STOP_RECORDING" -> stopRecording()
        }
        return START_NOT_STICKY
    }

    private fun startRecording() {
        if (isRecording) return

        try {
            callStartTime = System.currentTimeMillis()
            recordingFile = createRecordingFile()
            
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(recordingFile!!.absolutePath)
                
                try {
                    prepare()
                    start()
                    isRecording = true
                    Log.d(TAG, "🎙️ Recording started for $callPhoneNumber: ${recordingFile!!.absolutePath}")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to prepare recorder", e)
                    release()
                }
            }

            val notification = createNotification("Recording call from $callPhoneNumber...")
            
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

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording", e)
        }
    }

    private fun stopRecording() {
        if (!isRecording) return

        try {
            mediaRecorder?.apply {
                try {
                    stop()
                } catch (e: Exception) {
                    Log.e(TAG, "Error stopping recorder", e)
                }
                release()
            }
            mediaRecorder = null
            isRecording = false
            
            callDuration = System.currentTimeMillis() - callStartTime

            stopForeground(STOP_FOREGROUND_REMOVE)

            // Trigger analysis pipeline with recorded file
            if (recordingFile != null && recordingFile!!.exists()) {
                val analysisIntent = Intent(this, AnalysisPipelineService::class.java)
                analysisIntent.putExtra("RECORDING_FILE", recordingFile!!.absolutePath)
                analysisIntent.putExtra("CALL_DURATION", callDuration)
                analysisIntent.putExtra("PHONE_NUMBER", callPhoneNumber)
                startService(analysisIntent)
                Log.d(TAG, "✅ Recording stopped. Sent for analysis: ${recordingFile!!.absolutePath}")
                Log.d(TAG, "   Phone: $callPhoneNumber | Duration: ${callDuration / 1000}s")
            }

            stopSelf()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recording", e)
        }
    }

    private fun createRecordingFile(): File {
        val recordsDir = File(filesDir, "call_recordings").apply {
            if (!exists()) mkdirs()
        }
        
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val sanitizedPhone = callPhoneNumber.replace("[^0-9]".toRegex(), "").takeLast(10)
        return File(recordsDir, "call_${sanitizedPhone}_$timestamp.m4a")
    }

    private fun createNotification(text: String = "Monitoring call"): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, Constants.CALL_RECORDING_CHANNEL_ID)
            .setContentTitle("SAFEx Call Guard")
            .setContentText(text)
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

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (isRecording) {
                mediaRecorder?.stop()
                mediaRecorder?.release()
                mediaRecorder = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroy", e)
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
    }
}
