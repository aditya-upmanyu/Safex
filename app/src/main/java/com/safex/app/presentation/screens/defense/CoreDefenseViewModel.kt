package com.safex.app.presentation.screens.defense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safex.app.core.security.ThreatStateManager
import com.safex.app.core.security.model.ThreatLevel
import com.safex.app.core.security.model.ThreatModule
import com.safex.app.data.local.dao.SecurityIncidentDao
import com.safex.app.data.local.entity.SecurityIncidentEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CoreDefenseUiState(
    val combinedThreatLevel: ThreatLevel = ThreatLevel.UNKNOWN,
    val upiShieldLevel: ThreatLevel = ThreatLevel.UNKNOWN,
    val remoteAccessLevel: ThreatLevel = ThreatLevel.UNKNOWN,
    val digitalArrestLevel: ThreatLevel = ThreatLevel.UNKNOWN,
    val familyGuardianLevel: ThreatLevel = ThreatLevel.UNKNOWN,
    val recentIncidents: List<SecurityIncidentEntity> = emptyList(),
    val unacknowledgedCount: Int = 0
)

@HiltViewModel
class CoreDefenseViewModel @Inject constructor(
    private val threatStateManager: ThreatStateManager,
    private val incidentDao: SecurityIncidentDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(CoreDefenseUiState())
    val uiState: StateFlow<CoreDefenseUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                threatStateManager.combinedThreatLevel,
                threatStateManager.moduleStates,
                incidentDao.getRecentIncidents(10),
                incidentDao.getUnacknowledgedCount()
            ) { combined, modules, incidents, unack ->
                CoreDefenseUiState(
                    combinedThreatLevel = combined,
                    upiShieldLevel = modules[ThreatModule.UPI_SHIELD] ?: ThreatLevel.UNKNOWN,
                    remoteAccessLevel = modules[ThreatModule.REMOTE_ACCESS_SENTRY] ?: ThreatLevel.UNKNOWN,
                    digitalArrestLevel = modules[ThreatModule.DIGITAL_ARREST_SHIELD] ?: ThreatLevel.UNKNOWN,
                    familyGuardianLevel = modules[ThreatModule.FAMILY_GUARDIAN] ?: ThreatLevel.UNKNOWN,
                    recentIncidents = incidents,
                    unacknowledgedCount = unack
                )
            }.collect { _uiState.value = it }
        }
    }

    fun acknowledgeIncident(incidentId: String) {
        viewModelScope.launch { incidentDao.acknowledgeIncident(incidentId) }
    }
}
