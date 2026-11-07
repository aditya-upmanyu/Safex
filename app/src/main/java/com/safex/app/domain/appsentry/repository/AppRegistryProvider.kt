// domain/appsentry/repository/AppRegistryProvider.kt
package com.safex.app.domain.appsentry.repository

interface AppRegistryProvider {
    fun isKnownRat(packageName: String): Boolean
    fun matchesSuspiciousPattern(packageName: String): Boolean
    fun checkMasquerading(packageName: String, appLabel: String): MasqueradeType

    enum class MasqueradeType { NONE, BANKING, GOVT }
}
