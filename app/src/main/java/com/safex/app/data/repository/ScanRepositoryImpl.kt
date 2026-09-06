package com.safex.app.data.repository

import com.google.gson.Gson
import com.safex.app.BuildConfig
import com.safex.app.data.local.dao.ScanHistoryDao
import com.safex.app.data.local.entity.ScanHistoryEntity
import com.safex.app.data.remote.api.VirusTotalApi
import com.safex.app.domain.model.RiskLevel
import com.safex.app.domain.model.ScanResult
import com.safex.app.domain.repository.ScanRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ScanRepositoryImpl @Inject constructor(
    private val scanHistoryDao: ScanHistoryDao,
    private val virusTotalApi: VirusTotalApi,
    private val gson: Gson
) : ScanRepository {

    override suspend fun scanUrl(url: String, source: String): Result<ScanResult> {
        return try {
            // Check cache first
            val cached = scanHistoryDao.getByUrl(url)
            if (cached != null && System.currentTimeMillis() - cached.scanTimestamp < 3600000) {
                return Result.success(cached.toDomain())
            }

            // Submit URL to VirusTotal
            val apiKey = BuildConfig.VIRUSTOTAL_API_KEY
            if (apiKey.isBlank()) {
                return Result.failure(Exception("VirusTotal API key not configured"))
            }

            val submitResponse = virusTotalApi.submitUrl(apiKey, url)
            if (!submitResponse.isSuccessful || submitResponse.body()?.data?.id == null) {
                return Result.failure(Exception("Failed to submit URL: ${submitResponse.message()}"))
            }

            val analysisId = submitResponse.body()!!.data!!.id!!

            // Poll for result
            var attempts = 0
            while (attempts < 10) {
                delay(3000)
                val analysisResponse = virusTotalApi.getAnalysis(apiKey, analysisId)
                
                if (!analysisResponse.isSuccessful) {
                    return Result.failure(Exception("Analysis failed: ${analysisResponse.message()}"))
                }

                val analysis = analysisResponse.body()
                if (analysis?.data?.attributes?.status == "completed") {
                    val stats = analysis.data.attributes.stats
                    val malicious = stats?.malicious ?: 0
                    val suspicious = stats?.suspicious ?: 0
                    
                    val (riskScore, verdict) = when {
                        malicious > 5 -> 90 to RiskLevel.FRAUD
                        malicious > 0 || suspicious > 3 -> 60 to RiskLevel.SUSPICIOUS
                        else -> 10 to RiskLevel.SAFE
                    }

                    val scanResult = ScanResult(
                        id = 0,
                        targetUrl = url,
                        scanTimestamp = System.currentTimeMillis(),
                        riskScore = riskScore,
                        verdict = verdict,
                        threatInfo = gson.toJson(stats),
                        source = source
                    )

                    // Save to database
                    val id = scanHistoryDao.insert(scanResult.toEntity())
                    
                    return Result.success(scanResult.copy(id = id))
                }
                
                attempts++
            }

            Result.failure(Exception("Scan timeout"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getScanHistory(limit: Int): Flow<List<ScanResult>> {
        return scanHistoryDao.observeRecent(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllScans(): Flow<List<ScanResult>> {
        return scanHistoryDao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getScanById(id: Long): ScanResult? {
        return scanHistoryDao.getById(id)?.toDomain()
    }

    override suspend fun deleteScan(scan: ScanResult) {
        scanHistoryDao.delete(scan.toEntity())
    }

    override suspend fun deleteAllScans() {
        scanHistoryDao.deleteAll()
    }

    private fun ScanHistoryEntity.toDomain(): ScanResult {
        return ScanResult(
            id = id,
            targetUrl = targetUrl,
            scanTimestamp = scanTimestamp,
            riskScore = riskScore,
            verdict = RiskLevel.valueOf(verdict),
            threatInfo = threatInfo,
            source = source
        )
    }

    private fun ScanResult.toEntity(): ScanHistoryEntity {
        return ScanHistoryEntity(
            id = id,
            targetUrl = targetUrl,
            scanTimestamp = scanTimestamp,
            riskScore = riskScore,
            verdict = verdict.name,
            threatInfo = threatInfo,
            source = source
        )
    }
}
