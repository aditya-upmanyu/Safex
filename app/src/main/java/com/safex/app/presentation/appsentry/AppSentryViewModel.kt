// presentation/appsentry/AppSentryViewModel.kt
package com.safex.app.presentation.appsentry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safex.app.domain.appsentry.usecase.GetAppSentryResultsUseCase
import com.safex.app.domain.appsentry.usecase.InspectApkFileUseCase
import com.safex.app.domain.appsentry.usecase.ScanInstalledAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppSentryViewModel @Inject constructor(
    private val scanInstalledAppsUseCase: ScanInstalledAppsUseCase,
    private val inspectApkFileUseCase: InspectApkFileUseCase,
    private val getAppSentryResultsUseCase: GetAppSentryResultsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppSentryUiState())
    val uiState: StateFlow<AppSentryUiState> = _uiState.asStateFlow()

    private var scanJob: Job? = null

    init {
        // Observe stored scans and APK inspections
        viewModelScope.launch {
            getAppSentryResultsUseCase.observeScannedApps().collect { apps ->
                _uiState.update { it.copy(installedApps = apps) }
            }
        }

        viewModelScope.launch {
            getAppSentryResultsUseCase.observeApkInspections().collect { apks ->
                _uiState.update { it.copy(inspectedApks = apks) }
            }
        }

        // Trigger initial scan if empty
        viewModelScope.launch {
            if (_uiState.value.installedApps.isEmpty()) {
                scanAllApps()
            }
        }
    }

    fun onEvent(event: AppSentryEvent) {
        when (event) {
            is AppSentryEvent.ScanAllApps -> scanAllApps()
            is AppSentryEvent.ScanSingleApp -> Unit
            is AppSentryEvent.InspectApk -> inspectApk(event.uriOrPath)
            is AppSentryEvent.SelectApp -> _uiState.update { it.copy(selectedApp = event.app) }
            is AppSentryEvent.SelectApk -> _uiState.update { it.copy(selectedApk = event.apk) }
            is AppSentryEvent.FilterByRisk -> _uiState.update { it.copy(filterRiskLevel = event.level) }
            is AppSentryEvent.SearchQueryChanged -> _uiState.update { it.copy(searchQuery = event.query) }
            is AppSentryEvent.SwitchTab -> _uiState.update { it.copy(selectedTab = event.tabIndex) }
            is AppSentryEvent.ToggleTrustApp -> toggleTrust(event.packageName, event.trust)
            is AppSentryEvent.DeleteApkInspection -> deleteApk(event.filePath, event.deleteFile)
            is AppSentryEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun scanAllApps() {
        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isScanning = true,
                    scanProgress = 0.1f,
                    currentScanningApp = "Reading installed packages...",
                    errorMessage = null
                )
            }

            try {
                val results = scanInstalledAppsUseCase()
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        scanProgress = 1.0f,
                        installedApps = results,
                        lastScanTime = System.currentTimeMillis()
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        errorMessage = "Scan failed: ${e.localizedMessage ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    private fun inspectApk(uriOrPath: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isInspectingApk = true,
                    errorMessage = null
                )
            }

            try {
                val result = inspectApkFileUseCase(uriOrPath)
                _uiState.update {
                    it.copy(
                        isInspectingApk = false,
                        selectedApk = result
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isInspectingApk = false,
                        errorMessage = "APK inspection failed: ${e.localizedMessage ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    private fun toggleTrust(packageName: String, trust: Boolean) {
        viewModelScope.launch {
            getAppSentryResultsUseCase.setAppTrusted(packageName, trust)
            // If the selected app was updated, refresh it
            _uiState.value.selectedApp?.let { currentSelected ->
                if (currentSelected.packageName == packageName) {
                    _uiState.update {
                        it.copy(selectedApp = currentSelected.copy(isTrusted = trust))
                    }
                }
            }
        }
    }

    private fun deleteApk(filePath: String, deleteFile: Boolean) {
        viewModelScope.launch {
            getAppSentryResultsUseCase.deleteApkInspection(filePath, deleteFile)
            if (_uiState.value.selectedApk?.filePath == filePath) {
                _uiState.update { it.copy(selectedApk = null) }
            }
        }
    }
}
