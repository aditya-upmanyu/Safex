package com.safex.app.data.repository

import com.safex.app.data.local.dao.CallRecordDao
import com.safex.app.data.local.entity.CallRecordEntity
import com.safex.app.domain.model.CallRecord
import com.safex.app.domain.model.RiskLevel
import com.safex.app.domain.repository.CallRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CallRepositoryImpl @Inject constructor(
    private val callRecordDao: CallRecordDao
) : CallRepository {

    override fun getRecentCalls(limit: Int): Flow<List<CallRecord>> {
        return callRecordDao.observeRecent(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllCalls(): Flow<List<CallRecord>> {
        return callRecordDao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getFraudCalls(): Flow<List<CallRecord>> {
        return callRecordDao.observeFraudCalls().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getCallById(id: Long): CallRecord? {
        return callRecordDao.getById(id)?.toDomain()
    }

    override suspend fun insertCall(record: CallRecord): Long {
        return callRecordDao.insert(record.toEntity())
    }

    override suspend fun updateCall(record: CallRecord) {
        callRecordDao.update(record.toEntity())
    }

    override suspend fun deleteCall(record: CallRecord) {
        callRecordDao.delete(record.toEntity())
    }

    override suspend fun deleteAllCalls() {
        callRecordDao.deleteAll()
    }

    override suspend fun deleteOlderThan(timestampMillis: Long) {
        callRecordDao.deleteOlderThan(timestampMillis)
    }

    private fun CallRecordEntity.toDomain(): CallRecord {
        return CallRecord(
            id = id,
            phoneNumber = phoneNumber,
            timestamp = timestamp,
            duration = duration,
            transcript = transcript,
            riskScore = riskScore,
            verdict = RiskLevel.valueOf(verdict),
            confidence = confidence,
            detectedPatterns = detectedPatterns.split(",").filter { it.isNotBlank() },
            keywords = keywords.split(",").filter { it.isNotBlank() },
            reason = reason,
            reasonHindi = reasonHindi,
            recommendedAction = recommendedAction,
            status = status
        )
    }

    private fun CallRecord.toEntity(): CallRecordEntity {
        return CallRecordEntity(
            id = id,
            phoneNumber = phoneNumber,
            timestamp = timestamp,
            duration = duration,
            transcript = transcript,
            riskScore = riskScore,
            verdict = verdict.name,
            confidence = confidence,
            detectedPatterns = detectedPatterns.joinToString(","),
            keywords = keywords.joinToString(","),
            reason = reason,
            reasonHindi = reasonHindi,
            recommendedAction = recommendedAction,
            status = status
        )
    }
}
