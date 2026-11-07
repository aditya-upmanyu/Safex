package com.safex.app.domain.security.model

/**
 * Action user can take to remediate a security issue
 */
data class RemediationAction(
    val label: String,
    val actionType: ActionType,
    val actionData: String? = null
) {
    enum class ActionType {
        /** Open Android Settings */
        OPEN_SETTINGS,
        
        /** Open specific settings screen */
        OPEN_SPECIFIC_SETTINGS,
        
        /** Open external URL */
        OPEN_URL,
        
        /** Open app details */
        OPEN_APP_INFO,
        
        /** Show explanation dialog */
        SHOW_DIALOG,
        
        /** No action available */
        NONE
    }
}
