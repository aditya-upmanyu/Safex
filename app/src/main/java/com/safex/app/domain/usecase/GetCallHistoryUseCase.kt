package com.safex.app.domain.usecase

import com.safex.app.domain.model.CallRecord
import com.safex.app.domain.repository.CallRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCallHistoryUseCase @Inject constructor(
    private val callRepository: CallRepository
) {
    operator fun invoke(limit: Int = 20): Flow<List<CallRecord>> {
        return callRepository.getRecentCalls(limit)
    }
}
