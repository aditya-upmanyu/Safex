// domain/appsentry/usecase/ScanInstalledAppsUseCase.kt
package com.safex.app.domain.appsentry.usecase

import com.safex.app.domain.appsentry.model.ScannedApp
import com.safex.app.domain.appsentry.repository.AppSentryRepository
import javax.inject.Inject

class ScanInstalledAppsUseCase @Inject constructor(
    private val repository: AppSentryRepository
) {
    suspend operator fun invoke(): List<ScannedApp> {
        return repository.scanAllInstalledApps()
    }
}
