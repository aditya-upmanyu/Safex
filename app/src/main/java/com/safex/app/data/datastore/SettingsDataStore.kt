package com.safex.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.safex.app.presentation.theme.ThemeMode
import com.safex.app.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = Constants.DATASTORE_NAME
)

class SettingsDataStore(private val context: Context) {

    private object Keys {
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val EMERGENCY_CONTACT = stringPreferencesKey("emergency_contact")
        val RISK_THRESHOLD = intPreferencesKey("risk_threshold")
        val CALL_GUARD_ENABLED = booleanPreferencesKey("call_guard_enabled")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val CHAT_LANGUAGE = stringPreferencesKey("chat_language")
        val CHAT_TTS_ENABLED = booleanPreferencesKey("chat_tts_enabled")
        val CHAT_SPEECH_RATE = floatPreferencesKey("chat_speech_rate")
        val CHAT_AI_ENABLED = booleanPreferencesKey("chat_ai_enabled")
        val CHAT_ELDER_MODE = booleanPreferencesKey("chat_elder_mode")
        val CHAT_AI_CONSENT = booleanPreferencesKey("chat_ai_consent")
        val CHAT_AUTO_DELETE_DAYS = intPreferencesKey("chat_auto_delete_days")
    }

    val chatLanguage: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[Keys.CHAT_LANGUAGE] ?: "en"
    }

    suspend fun setChatLanguage(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.CHAT_LANGUAGE] = languageCode
        }
    }

    val chatTtsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[Keys.CHAT_TTS_ENABLED] ?: false
    }

    suspend fun setChatTtsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.CHAT_TTS_ENABLED] = enabled
        }
    }

    val chatSpeechRate: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[Keys.CHAT_SPEECH_RATE] ?: 0.9f
    }

    suspend fun setChatSpeechRate(rate: Float) {
        context.dataStore.edit { preferences ->
            preferences[Keys.CHAT_SPEECH_RATE] = rate
        }
    }

    val chatAiEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[Keys.CHAT_AI_ENABLED] ?: true
    }

    suspend fun setChatAiEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.CHAT_AI_ENABLED] = enabled
        }
    }

    val chatElderMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[Keys.CHAT_ELDER_MODE] ?: false
    }

    suspend fun setChatElderMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.CHAT_ELDER_MODE] = enabled
        }
    }

    val chatAiConsent: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[Keys.CHAT_AI_CONSENT] ?: false
    }

    suspend fun setChatAiConsent(consented: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.CHAT_AI_CONSENT] = consented
        }
    }

    val chatAutoDeleteDays: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[Keys.CHAT_AUTO_DELETE_DAYS] ?: 0
    }

    suspend fun setChatAutoDeleteDays(days: Int) {
        context.dataStore.edit { preferences ->
            preferences[Keys.CHAT_AUTO_DELETE_DAYS] = days
        }
    }

    val onboardingDone: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[Keys.ONBOARDING_DONE] ?: false
    }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.ONBOARDING_DONE] = done
        }
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val modeString = preferences[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        try {
            ThemeMode.valueOf(modeString)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[Keys.THEME_MODE] = mode.name
        }
    }

    val emergencyContact: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[Keys.EMERGENCY_CONTACT] ?: ""
    }

    suspend fun setEmergencyContact(contact: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.EMERGENCY_CONTACT] = contact
        }
    }

    val riskThreshold: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[Keys.RISK_THRESHOLD] ?: Constants.DEFAULT_RISK_THRESHOLD
    }

    suspend fun setRiskThreshold(threshold: Int) {
        context.dataStore.edit { preferences ->
            preferences[Keys.RISK_THRESHOLD] = threshold
        }
    }

    val callGuardEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[Keys.CALL_GUARD_ENABLED] ?: false
    }

    suspend fun setCallGuardEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.CALL_GUARD_ENABLED] = enabled
        }
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[Keys.NOTIFICATIONS_ENABLED] ?: true
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun getEmergencyContactSync(): String {
        val preferences = context.dataStore.data.map { it }.first()
        return preferences[Keys.EMERGENCY_CONTACT] ?: ""
    }

    suspend fun getRiskThresholdSync(): Int {
        val preferences = context.dataStore.data.map { it }.first()
        return preferences[Keys.RISK_THRESHOLD] ?: Constants.DEFAULT_RISK_THRESHOLD
    }

    private suspend fun <T> Flow<T>.first(): T {
        var result: T? = null
        this.collect {
            result = it
            return@collect
        }
        return result!!
    }
}
