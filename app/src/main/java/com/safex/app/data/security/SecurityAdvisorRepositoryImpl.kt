package com.safex.app.data.security

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.safex.app.domain.security.SecurityCheckManager
import com.safex.app.domain.security.SecurityCheckRegistry
import com.safex.app.domain.security.model.SecurityCheckResult
import com.safex.app.domain.security.model.SecurityScore
import com.safex.app.domain.security.repository.SecurityAdvisorRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.securityDataStore: DataStore<Preferences> by preferencesDataStore(name = "security_advisor")

@Singleton
class SecurityAdvisorRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val checkManager: SecurityCheckManager,
    private val gson: Gson
) : SecurityAdvisorRepository {
    
    companion object {
        private val KEY_SCAN_RESULTS = stringPreferencesKey("scan_results")
        private val KEY_LAST_SCAN = longPreferencesKey("last_scan_timestamp")
        private val KEY_SCAN_HISTORY = stringPreferencesKey("scan_history")
    }
    
    override suspend fun executeScan(): List<SecurityCheckResult> {
        val checks = SecurityCheckRegistry.getAllChecks()
        val results = checkManager.executeAllChecks(context, checks)
        
        // Cache results
        cacheResults(results)
        
        return results
    }
    
    override fun getCachedResults(): Flow<List<SecurityCheckResult>> {
        return context.securityDataStore.data.map { prefs ->
            val json = prefs[KEY_SCAN_RESULTS]
            if (json != null) {
                val type = object : TypeToken<List<SecurityCheckResult>>() {}.type
                gson.fromJson(json, type)
            } else {
                emptyList()
            }
        }
    }
    
    override suspend fun getLastScanTimestamp(): Long? {
        return context.securityDataStore.data.map { prefs ->
            prefs[KEY_LAST_SCAN]
        }.first()
    }
    
    override suspend fun clearCache() {
        context.securityDataStore.edit { prefs ->
            prefs.remove(KEY_SCAN_RESULTS)
            prefs.remove(KEY_LAST_SCAN)
        }
    }
    
    override suspend fun getScanHistory(): List<SecurityScore> {
        return context.securityDataStore.data.map { prefs ->
            val json = prefs[KEY_SCAN_HISTORY]
            if (json != null) {
                val type = object : TypeToken<List<SecurityScore>>() {}.type
                gson.fromJson<List<SecurityScore>>(json, type)
            } else {
                emptyList()
            }
        }.first()
    }
    
    private suspend fun cacheResults(results: List<SecurityCheckResult>) {
        val timestamp = System.currentTimeMillis()
        
        context.securityDataStore.edit { prefs ->
            prefs[KEY_SCAN_RESULTS] = gson.toJson(results)
            prefs[KEY_LAST_SCAN] = timestamp
        }
    }
}
