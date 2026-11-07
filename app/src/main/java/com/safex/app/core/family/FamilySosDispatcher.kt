package com.safex.app.core.family

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
import android.util.Log
import com.safex.app.data.local.dao.GuardianDao
import com.safex.app.data.local.dao.SecurityIncidentDao
import com.safex.app.data.local.entity.GuardianEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Dispatches family SOS alerts with strict rate limiting and consent checks.
 *
 * SAFETY RULES:
 * 1. Never send SMS without user having explicitly enabled Family Guardian.
 * 2. Per-guardian cooldown: 10 minutes between alerts.
 * 3. Per-incident deduplication: same incident never sent twice.
 * 4. Maximum 3 alerts per hour across all guardians.
 * 5. No private data (transcripts, banking info) in SMS body.
 * 6. Falls back to ACTION_SENDTO composer if direct SMS unavailable.
 */
@Singleton
class FamilySosDispatcher @Inject constructor(
    @ApplicationContext private val context: Context,
    private val guardianDao: GuardianDao,
    private val incidentDao: SecurityIncidentDao
) {
    private val TAG = "FamilySosDispatcher"

    // Per-guardian last alert timestamps
    private val lastAlertTime = ConcurrentHashMap<Long, Long>()
    private val sentIncidentIds = ConcurrentHashMap<String, Long>()
    private val alertsThisHour = mutableListOf<Long>()

    private val GUARDIAN_COOLDOWN_MS = 10 * 60 * 1000L  // 10 minutes
    private val HOURLY_MAX_ALERTS = 3

    enum class DispatchResult {
        SENT, COMPOSER_OPENED, SKIPPED_RATE_LIMIT, SKIPPED_NO_GUARDIAN,
        SKIPPED_CONSENT, SKIPPED_DUPLICATE, FAILED
    }

    suspend fun dispatch(
        incidentId: String,
        incidentType: String,
        severity: String,
        evidenceSummary: String,
        guardianProtectionEnabled: Boolean
    ): DispatchResult = withContext(Dispatchers.IO) {

        if (!guardianProtectionEnabled) return@withContext DispatchResult.SKIPPED_CONSENT

        // Dedup check
        if (sentIncidentIds.containsKey(incidentId)) {
            Log.d(TAG, "Skipping duplicate incident: $incidentId")
            return@withContext DispatchResult.SKIPPED_DUPLICATE
        }

        // Rate limit: hourly max
        val now = System.currentTimeMillis()
        alertsThisHour.removeAll { now - it > 3_600_000L }
        if (alertsThisHour.size >= HOURLY_MAX_ALERTS) {
            Log.w(TAG, "Hourly alert limit reached")
            return@withContext DispatchResult.SKIPPED_RATE_LIMIT
        }

        val guardians = try {
            guardianDao.getEnabledGuardians().first()
        } catch (_: Exception) { emptyList() }

        if (guardians.isEmpty()) return@withContext DispatchResult.SKIPPED_NO_GUARDIAN

        var anyDispatched = false

        for (guardian in guardians) {
            val lastSent = lastAlertTime[guardian.id] ?: 0L
            if (now - lastSent < GUARDIAN_COOLDOWN_MS) {
                Log.d(TAG, "Cooldown active for guardian ${guardian.id}")
                continue
            }

            val message = buildSosMessage(incidentId, incidentType, severity)
            val result = trySendSms(guardian.phoneNumber, message)

            if (result != DispatchResult.FAILED) {
                lastAlertTime[guardian.id] = now
                anyDispatched = true
            }

            // Update DB notification status
            try {
                incidentDao.updateNotificationStatus(
                    incidentId,
                    if (result == DispatchResult.SENT) "SENT" else "COMPOSER_OPENED"
                )
            } catch (_: Exception) {}
        }

        if (anyDispatched) {
            sentIncidentIds[incidentId] = now
            alertsThisHour.add(now)
            DispatchResult.SENT
        } else {
            DispatchResult.SKIPPED_RATE_LIMIT
        }
    }

    private fun trySendSms(phoneNumber: String, message: String): DispatchResult {
        return try {
            val smsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION") SmsManager.getDefault()
            }
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Log.d(TAG, "SMS sent to guardian")
            DispatchResult.SENT
        } catch (e: SecurityException) {
            Log.w(TAG, "No SMS permission — opening composer")
            openSmsComposer(phoneNumber, message)
            DispatchResult.COMPOSER_OPENED
        } catch (e: Exception) {
            Log.e(TAG, "SMS failed: ${e.message}")
            openSmsComposer(phoneNumber, message)
            DispatchResult.COMPOSER_OPENED
        }
    }

    private fun openSmsComposer(phoneNumber: String, message: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$phoneNumber")
                putExtra("sms_body", message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open SMS composer: ${e.message}")
        }
    }

    private fun buildSosMessage(incidentId: String, type: String, severity: String): String =
        "SafeX SOS Alert:\n\n" +
        "A high-risk security indicator has been detected on your family member's device.\n" +
        "Type: $type | Severity: $severity\n\n" +
        "Please call them immediately to verify their safety.\n" +
        "Incident ID: ${incidentId.take(8).uppercase()}\n\n" +
        "This alert was generated by SafeX. Please verify the situation independently."
}
