package com.safex.app.domain.usecase

import com.safex.app.domain.model.ScanResult
import com.safex.app.domain.repository.ScanRepository
import javax.inject.Inject

class ScanUrlUseCase @Inject constructor(
    private val scanRepository: ScanRepository
) {
    suspend operator fun invoke(url: String): Result<ScanResult> {
        val cleanedUrl = url.trim()
        if (cleanedUrl.isBlank()) {
            return Result.failure(Exception("URL cannot be empty"))
        }
        
        if (!isValidUrl(cleanedUrl)) {
            return Result.failure(Exception("Invalid URL format"))
        }

        return scanRepository.scanUrl(cleanedUrl, "MANUAL")
    }

    private fun isValidUrl(url: String): Boolean {
        return url.startsWith("http://", ignoreCase = true) ||
               url.startsWith("https://", ignoreCase = true)
    }
}
