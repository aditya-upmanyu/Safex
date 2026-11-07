package com.safex.app.di

import android.content.Context
import com.safex.app.core.family.FamilySosDispatcher
import com.safex.app.core.family.SuspiciousInstallationDetector
import com.safex.app.core.overlay.DigitalArrestWarningOverlay
import com.safex.app.core.overlay.RemoteAccessWarningOverlay
import com.safex.app.core.overlay.UpiWarningOverlay
import com.safex.app.core.remote.ScreenShareDetector
import com.safex.app.core.security.ThreatCorrelationEngine
import com.safex.app.core.security.ThreatStateManager
import com.safex.app.core.telephony.TelephonyCallStateManager
import com.safex.app.data.local.dao.GuardianDao
import com.safex.app.data.local.dao.SecurityIncidentDao
import com.safex.app.domain.usecase.DetectRemoteAccessRiskUseCase
import com.safex.app.domain.usecase.DetectUpiCollisionUseCase
import com.safex.app.domain.usecase.EvaluateDigitalArrestUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreDefenseModule {

    // ── Core singletons ───────────────────────────────────────────────────────

    @Provides @Singleton
    fun provideThreatStateManager(): ThreatStateManager = ThreatStateManager()

    @Provides @Singleton
    fun provideThreatCorrelationEngine(
        tsm: ThreatStateManager
    ): ThreatCorrelationEngine = ThreatCorrelationEngine(tsm)

    @Provides @Singleton
    fun provideTelephonyCallStateManager(
        @ApplicationContext context: Context
    ): TelephonyCallStateManager = TelephonyCallStateManager(context)

    @Provides @Singleton
    fun provideScreenShareDetector(
        @ApplicationContext context: Context
    ): ScreenShareDetector = ScreenShareDetector(context)

    @Provides @Singleton
    fun provideSuspiciousInstallationDetector(
        @ApplicationContext context: Context
    ): SuspiciousInstallationDetector = SuspiciousInstallationDetector(context)

    // ── Overlays ──────────────────────────────────────────────────────────────

    @Provides @Singleton
    fun provideUpiWarningOverlay(
        @ApplicationContext context: Context
    ): UpiWarningOverlay = UpiWarningOverlay(context)

    @Provides @Singleton
    fun provideRemoteAccessWarningOverlay(
        @ApplicationContext context: Context
    ): RemoteAccessWarningOverlay = RemoteAccessWarningOverlay(context)

    @Provides @Singleton
    fun provideDigitalArrestWarningOverlay(
        @ApplicationContext context: Context
    ): DigitalArrestWarningOverlay = DigitalArrestWarningOverlay(context)

    // ── Use cases ─────────────────────────────────────────────────────────────

    @Provides @Singleton
    fun provideDetectUpiCollisionUseCase(
        csm: TelephonyCallStateManager,
        tsm: ThreatStateManager
    ): DetectUpiCollisionUseCase = DetectUpiCollisionUseCase(csm, tsm)

    @Provides @Singleton
    fun provideDetectRemoteAccessRiskUseCase(
        ssd: ScreenShareDetector,
        csm: TelephonyCallStateManager,
        tsm: ThreatStateManager
    ): DetectRemoteAccessRiskUseCase = DetectRemoteAccessRiskUseCase(ssd, csm, tsm)

    @Provides @Singleton
    fun provideEvaluateDigitalArrestUseCase(
        tsm: ThreatStateManager,
        tce: ThreatCorrelationEngine
    ): EvaluateDigitalArrestUseCase = EvaluateDigitalArrestUseCase(tsm, tce)

    // ── Family SOS ────────────────────────────────────────────────────────────

    @Provides @Singleton
    fun provideFamilySosDispatcher(
        @ApplicationContext context: Context,
        guardianDao: GuardianDao,
        incidentDao: SecurityIncidentDao
    ): FamilySosDispatcher = FamilySosDispatcher(context, guardianDao, incidentDao)
}
