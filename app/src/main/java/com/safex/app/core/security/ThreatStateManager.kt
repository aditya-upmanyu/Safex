package com.safex.app.core.security

import com.safex.app.core.security.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central threat state manager for all four security modules.
 *
 * Responsibilities:
 * - Maintain current threat level for each module
 * - Receive and deduplicate threat events
 * - Publish combined threat state
 * - Coordinate warning priorities
 * - Trigger SOS evaluation when thresholds met
 */
@Singleton
class ThreatStateManager @Inject constructor() {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Per-module threat levels
    private val _moduleStates = MutableStateFlow<Map<ThreatModule, ThreatLevel>>(
        ThreatModule.values().associateWith { ThreatLevel.UNKNOWN }
    )
    val moduleStates: StateFlow<Map<ThreatModule, ThreatLevel>> = _moduleStates.asStateFlow()

    // Combined threat level across all modules
    private val _combinedThreatLevel = MutableStateFlow(ThreatLevel.UNKNOWN)
    val combinedThreatLevel: StateFlow<ThreatLevel> = _combinedThreatLevel.asStateFlow()

    // Stream of all incoming threat events (for UI and correlation)
    private val _threatEvents = MutableSharedFlow<ThreatEvent>(replay = 10)
    val threatEvents: SharedFlow<ThreatEvent> = _threatEvents.asSharedFlow()

    // Events that require immediate overlay/warning
    private val _warningEvents = MutableSharedFlow<ThreatEvent>(extraBufferCapacity = 5)
    val warningEvents: SharedFlow<ThreatEvent> = _warningEvents.asSharedFlow()

    // Recent events per module for deduplication (incidentId -> timestamp)
    private val recentEventIds = ConcurrentHashMap<String, Long>()
    private val DEDUP_WINDOW_MS = 30_000L // 30s dedup window

    fun reportEvent(event: ThreatEvent) {
        scope.launch {
            // Deduplicate: same module + same level within 30s
            val dedupKey = "${event.module}:${event.level}"
            val lastSeen = recentEventIds[dedupKey] ?: 0L
            val now = System.currentTimeMillis()

            if (now - lastSeen < DEDUP_WINDOW_MS && event.level != ThreatLevel.CRITICAL) {
                return@launch // Skip duplicate
            }
            recentEventIds[dedupKey] = now

            // Emit to general event stream
            _threatEvents.emit(event)

            // Update per-module state
            val updated = _moduleStates.value.toMutableMap()
            updated[event.module] = event.level
            _moduleStates.value = updated

            // Recalculate combined threat
            _combinedThreatLevel.value = calculateCombinedLevel(updated)

            // Emit warning if critical or requires immediate attention
            if (event.requiresImmediateWarning || event.level == ThreatLevel.CRITICAL) {
                _warningEvents.emit(event)
            }
        }
    }

    fun clearModuleState(module: ThreatModule) {
        scope.launch {
            val updated = _moduleStates.value.toMutableMap()
            updated[module] = ThreatLevel.SAFE
            _moduleStates.value = updated
            _combinedThreatLevel.value = calculateCombinedLevel(updated)
        }
    }

    fun getModuleThreatLevel(module: ThreatModule): ThreatLevel {
        return _moduleStates.value[module] ?: ThreatLevel.UNKNOWN
    }

    private fun calculateCombinedLevel(states: Map<ThreatModule, ThreatLevel>): ThreatLevel {
        val levels = states.values
        return when {
            levels.any { it == ThreatLevel.CRITICAL } -> ThreatLevel.CRITICAL
            levels.any { it == ThreatLevel.HIGH } -> ThreatLevel.HIGH
            levels.any { it == ThreatLevel.MODERATE } -> ThreatLevel.MODERATE
            levels.any { it == ThreatLevel.LOW } -> ThreatLevel.LOW
            levels.all { it == ThreatLevel.SAFE } -> ThreatLevel.SAFE
            else -> ThreatLevel.UNKNOWN
        }
    }
}
