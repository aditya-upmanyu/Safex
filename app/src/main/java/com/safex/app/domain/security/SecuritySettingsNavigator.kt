package com.safex.app.domain.security

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.safex.app.domain.security.model.RemediationAction

/**
 * Handles navigation to Android settings with fallback chain.
 * Each action tries specific settings first, then falls back to general settings.
 */
class SecuritySettingsNavigator(private val context: Context) {
    
    /**
     * Navigate to the appropriate settings screen for a remediation action.
     * Returns true if navigation succeeded, false if all attempts failed.
     */
    fun navigate(action: RemediationAction): Boolean {
        return when (action.actionType) {
            RemediationAction.ActionType.OPEN_SETTINGS -> {
                openSettings(Settings.ACTION_SETTINGS)
            }
            
            RemediationAction.ActionType.OPEN_SPECIFIC_SETTINGS -> {
                action.actionData?.let { settingsAction ->
                    openSettings(settingsAction)
                } ?: openSettings(Settings.ACTION_SETTINGS)
            }
            
            RemediationAction.ActionType.OPEN_URL -> {
                action.actionData?.let { url ->
                    openUrl(url)
                } ?: false
            }
            
            RemediationAction.ActionType.OPEN_APP_INFO -> {
                action.actionData?.let { packageName ->
                    openAppInfo(packageName)
                } ?: false
            }
            
            RemediationAction.ActionType.SHOW_DIALOG,
            RemediationAction.ActionType.NONE -> {
                // These are handled by the UI, not navigation
                false
            }
        }
    }
    
    private fun openSettings(action: String): Boolean {
        val candidates = listOf(
            Intent(action),
            Intent(Settings.ACTION_SETTINGS)
        )
        
        for (intent in candidates) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                    return true
                }
            } catch (e: Exception) {
                // Try next candidate
                continue
            }
        }
        
        return false
    }
    
    private fun openUrl(url: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    private fun openAppInfo(packageName: String): Boolean {
        val candidates = listOf(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
            },
            Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
        )
        
        for (intent in candidates) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                    return true
                }
            } catch (e: Exception) {
                continue
            }
        }
        
        return false
    }
}
