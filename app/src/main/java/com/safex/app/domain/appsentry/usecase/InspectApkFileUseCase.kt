// domain/appsentry/usecase/InspectApkFileUseCase.kt
package com.safex.app.domain.appsentry.usecase

import com.safex.app.domain.appsentry.model.ApkInspectionResult
import com.safex.app.domain.appsentry.repository.AppSentryRepository
import javax.inject.Inject

class InspectApkFileUseCase @Inject constructor(
    private val repository: AppSentryRepository
) {
    suspend operator fun invoke(fileUriOrPath: String): ApkInspectionResult {
        return repository.inspectApkFile(fileUriOrPath)
    }
}
