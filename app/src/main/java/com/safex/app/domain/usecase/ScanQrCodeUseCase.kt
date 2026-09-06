package com.safex.app.domain.usecase

import com.safex.app.domain.model.ScanResult
import com.safex.app.domain.repository.ScanRepository
import javax.inject.Inject

class ScanQrCodeUseCase @Inject constructor(
    private val scanRepository: ScanRepository
) {
    suspend operator fun invoke(url: String, source: String): Result<ScanResult> {
        return scanRepository.scanUrl(url, source)
    }
}
