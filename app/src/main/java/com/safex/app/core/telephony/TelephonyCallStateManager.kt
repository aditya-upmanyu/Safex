package com.safex.app.core.telephony

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Maintains a thread-safe representation of cellular call state.
 *
 * Uses TelephonyCallback on API 31+ and PhoneStateListener on API 26-30.
 * IMPORTANT: Only tracks cellular calls. VoIP (WhatsApp/Skype) calls are
 * marked NOT_VERIFIED per the spec — we do not fabricate detection.
 */
@Singleton
class TelephonyCallStateManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    enum class CallState {
        IDLE,
        RINGING,
        OFF_HOOK,   // Call active
        UNKNOWN,
        PERMISSION_REQUIRED
    }

    private val _callState = MutableStateFlow(CallState.UNKNOWN)
    val callState: StateFlow<CallState> = _callState.asStateFlow()

    val isCallActive: Boolean get() = _callState.value == CallState.OFF_HOOK

    private var telephonyManager: TelephonyManager? = null
    private var legacyListener: PhoneStateListenerCompat? = null
    private var modernCallback: Any? = null // TelephonyCallback on API 31+

    fun startListening() {
        if (!hasPermission()) {
            _callState.value = CallState.PERMISSION_REQUIRED
            return
        }

        telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            registerModernCallback()
        } else {
            registerLegacyListener()
        }
    }

    fun stopListening() {
        try {
            telephonyManager?.let { tm ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    (modernCallback as? TelephonyCallback)?.let {
                        tm.unregisterTelephonyCallback(it)
                    }
                } else {
                    @Suppress("DEPRECATION")
                    legacyListener?.let { tm.listen(it, PhoneStateListener.LISTEN_NONE) }
                }
            }
        } catch (_: Exception) {}
        _callState.value = CallState.IDLE
    }

    fun updateFromBroadcast(state: String) {
        _callState.value = when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> CallState.RINGING
            TelephonyManager.EXTRA_STATE_OFFHOOK -> CallState.OFF_HOOK
            TelephonyManager.EXTRA_STATE_IDLE -> CallState.IDLE
            else -> CallState.UNKNOWN
        }
    }

    private fun registerModernCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val callback = object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                override fun onCallStateChanged(state: Int) {
                    _callState.value = mapState(state)
                }
            }
            modernCallback = callback
            telephonyManager?.registerTelephonyCallback(context.mainExecutor, callback)
        }
    }

    @Suppress("DEPRECATION")
    private fun registerLegacyListener() {
        val listener = PhoneStateListenerCompat { state ->
            _callState.value = mapState(state)
        }
        legacyListener = listener
        telephonyManager?.listen(listener, PhoneStateListener.LISTEN_CALL_STATE)
    }

    private fun mapState(state: Int) = when (state) {
        TelephonyManager.CALL_STATE_RINGING -> CallState.RINGING
        TelephonyManager.CALL_STATE_OFFHOOK -> CallState.OFF_HOOK
        TelephonyManager.CALL_STATE_IDLE -> CallState.IDLE
        else -> CallState.UNKNOWN
    }

    private fun hasPermission() = ContextCompat.checkSelfPermission(
        context, Manifest.permission.READ_PHONE_STATE
    ) == PackageManager.PERMISSION_GRANTED
}

@Suppress("DEPRECATION")
private class PhoneStateListenerCompat(
    private val onStateChange: (Int) -> Unit
) : PhoneStateListener() {
    override fun onCallStateChanged(state: Int, phoneNumber: String?) {
        onStateChange(state)
    }
}
