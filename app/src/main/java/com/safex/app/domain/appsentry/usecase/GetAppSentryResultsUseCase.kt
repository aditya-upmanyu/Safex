// domain/appsentry/usecase/GetAppSentryResultsUseCase.kt
package com.safex.app.domain.appsentry.usecase

import com.safex.app.domain.appsentry.model.ApkInspectionResult
import com.safex.app.domain.appsentry.model.ScannedApp
import com.safex.app.domain.appsentry.repository.AppSentryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAppSentryResultsUseCase @Inject constructor(
    private val repository: AppSentryRepository
) {
    fun observeScannedApps(): Flow<List<ScannedApp>> = repository.observeScannedApps()

    fun observeApkInspections(): Flow<List<ApkInspectionResult>> = repository.observeApkInspections()

    suspend fun setAppTrusted(packageName: String, isTrusted: Boolean) {
        repository.setAppTrusted(packageName, isTrusted)
    }

    suspend fun deleteApkInspection(filePath: String, deleteFile: Boolean = false): Boolean {
        return repository.deleteApkInspection(filePath, deleteFile)
    }
}
