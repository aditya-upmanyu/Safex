// data/appsentry/AppSentryRepositoryImpl.kt
package com.safex.app.data.appsentry

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.safex.app.BuildConfig
import com.safex.app.data.local.dao.AppSentryDao
import com.safex.app.data.local.entity.ApkInspectionEntity
import com.safex.app.data.local.entity.ScannedAppEntity
import com.safex.app.data.local.entity.TrustedAppEntity
import com.safex.app.data.local.entity.VirusTotalCacheEntity
import com.safex.app.data.remote.api.VirusTotalApi
import com.safex.app.domain.appsentry.model.ApkInspectionResult
import com.safex.app.domain.appsentry.model.AppRiskLevel
import com.safex.app.domain.appsentry.model.ScannedApp
import com.safex.app.domain.appsentry.model.ThreatIndicator
import com.safex.app.domain.appsentry.model.VirusTotalStats
import com.safex.app.domain.appsentry.repository.AppSentryRepository
import com.safex.app.domain.appsentry.usecase.CalculateAppRiskUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSentryRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appSentryDao: AppSentryDao,
    private val apkInspectorEngine: ApkInspectorEngine,
    private val calculateRiskUseCase: CalculateAppRiskUseCase,
    private val virusTotalApi: VirusTotalApi,
    private val gson: Gson
) : AppSentryRepository {

    override fun observeScannedApps(): Flow<List<ScannedApp>> {
        val pm = context.packageManager
        return appSentryDao.getScannedAppsFlow().map { entities ->
            entities.map { it.toDomain(pm, gson) }
        }
    }

    override suspend fun scanAllInstalledApps(): List<ScannedApp> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val flags = PackageManager.GET_PERMISSIONS
        val packages: List<PackageInfo> = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(flags.toLong()))
            } else {
                @Suppress("DEPRECATION")
                pm.getInstalledPackages(flags)
            }
        } catch (e: Exception) {
            emptyList()
        }

        val results = mutableListOf<ScannedApp>()
        val entities = mutableListOf<ScannedAppEntity>()

        for (pkg in packages) {
            val appInfo = pkg.applicationInfo ?: continue
            val packageName = pkg.packageName

            // Skip SAFEx itself
            if (packageName == context.packageName) continue

            // Determine if user or updated system app
            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val isUpdatedSystem = (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

            // Skip standard core system apps to prevent noise unless updated
            if (isSystem && !isUpdatedSystem) {
                continue
            }

            val appName = try {
                appInfo.loadLabel(pm).toString()
            } catch (e: Exception) {
                packageName
            }

            val installerPackage = getInstallerPackage(packageName)
            val isSideLoaded = isInstallerUnknown(installerPackage)

            val permissions = pkg.requestedPermissions?.toList() ?: emptyList()

            val isTrusted = appSentryDao.isAppTrusted(packageName)

            val evaluation = calculateRiskUseCase(
                packageName = packageName,
                appName = appName,
                permissions = permissions,
                isSideLoaded = isSideLoaded
            )

            val effectiveScore = if (isTrusted) 0 else evaluation.score
            val effectiveLevel = if (isTrusted) AppRiskLevel.SAFE else evaluation.level

            val entity = ScannedAppEntity(
                packageName = packageName,
                appName = appName,
                installedAt = pkg.firstInstallTime,
                updatedAt = pkg.lastUpdateTime,
                versionName = pkg.versionName ?: "1.0",
                versionCode = getVersionCode(pkg),
                permissionsJson = gson.toJson(permissions),
                isSideLoaded = isSideLoaded,
                installerPackage = installerPackage,
                riskScore = effectiveScore,
                riskLevel = effectiveLevel.name,
                threatIndicatorsJson = gson.toJson(evaluation.indicators.map { it.name }),
                scannedAt = System.currentTimeMillis(),
                isTrusted = isTrusted
            )
            entities.add(entity)
            results.add(entity.toDomain(pm, gson))
        }

        appSentryDao.clearScannedApps()
        appSentryDao.insertScannedApps(entities)

        results.sortedByDescending { it.riskScore }
    }

    override suspend fun scanInstalledApp(packageName: String): ScannedApp? = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val flags = PackageManager.GET_PERMISSIONS
        val pkg = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(packageName, flags)
            }
        } catch (e: Exception) {
            return@withContext null
        }

        val appInfo = pkg.applicationInfo ?: return@withContext null
        val appName = try { appInfo.loadLabel(pm).toString() } catch (e: Exception) { packageName }
        val installerPackage = getInstallerPackage(packageName)
        val isSideLoaded = isInstallerUnknown(installerPackage)
        val permissions = pkg.requestedPermissions?.toList() ?: emptyList()
        val isTrusted = appSentryDao.isAppTrusted(packageName)

        val evaluation = calculateRiskUseCase(
            packageName = packageName,
            appName = appName,
            permissions = permissions,
            isSideLoaded = isSideLoaded
        )

        val entity = ScannedAppEntity(
            packageName = packageName,
            appName = appName,
            installedAt = pkg.firstInstallTime,
            updatedAt = pkg.lastUpdateTime,
            versionName = pkg.versionName ?: "1.0",
            versionCode = getVersionCode(pkg),
            permissionsJson = gson.toJson(permissions),
            isSideLoaded = isSideLoaded,
            installerPackage = installerPackage,
            riskScore = if (isTrusted) 0 else evaluation.score,
            riskLevel = (if (isTrusted) AppRiskLevel.SAFE else evaluation.level).name,
            threatIndicatorsJson = gson.toJson(evaluation.indicators.map { it.name }),
            scannedAt = System.currentTimeMillis(),
            isTrusted = isTrusted
        )
        appSentryDao.insertScannedApp(entity)
        entity.toDomain(pm, gson)
    }

    override suspend fun inspectApkFile(fileUriOrPath: String): ApkInspectionResult = withContext(Dispatchers.IO) {
        // Step 1: Local APK inspection & SHA-256 calculation
        var localResult = apkInspectorEngine.inspect(fileUriOrPath)

        // Step 2: Query VirusTotal if hash is available
        if (localResult.sha256.isNotBlank()) {
            val vtResult = queryVirusTotal(localResult.sha256).getOrNull()
            if (vtResult != null) {
                // Re-evaluate risk with VirusTotal findings
                val evaluation = calculateRiskUseCase(
                    packageName = localResult.packageName ?: "unknown",
                    appName = localResult.appName ?: "Unknown APK",
                    permissions = localResult.requestedPermissions,
                    isSideLoaded = true,
                    isLegitimateCertificate = localResult.isLegitimateCertificate,
                    virusTotalMalicious = vtResult.malicious,
                    virusTotalSuspicious = vtResult.suspicious
                )
                localResult = localResult.copy(
                    riskScore = evaluation.score,
                    riskLevel = evaluation.level,
                    threatIndicators = evaluation.indicators,
                    virusTotalStats = vtResult
                )
            }
        }

        // Save inspection result to DB
        appSentryDao.insertApkInspection(localResult.toEntity(gson))

        localResult
    }

    override fun observeApkInspections(): Flow<List<ApkInspectionResult>> {
        return appSentryDao.getApkInspectionsFlow().map { entities ->
            entities.map { it.toDomain(gson) }
        }
    }

    override suspend fun deleteApkInspection(filePath: String, deleteFile: Boolean): Boolean = withContext(Dispatchers.IO) {
        appSentryDao.deleteApkInspection(filePath)
        if (deleteFile && !filePath.startsWith("content://")) {
            try {
                val f = File(filePath)
                if (f.exists()) f.delete() else true
            } catch (e: Exception) {
                false
            }
        } else {
            true
        }
    }

    override suspend fun setAppTrusted(packageName: String, isTrusted: Boolean) = withContext(Dispatchers.IO) {
        if (isTrusted) {
            appSentryDao.insertTrustedApp(TrustedAppEntity(packageName))
        } else {
            appSentryDao.deleteTrustedApp(packageName)
        }
        // Rescan this package to update its cached entity
        scanInstalledApp(packageName)
        Unit
    }

    override suspend fun queryVirusTotal(sha256: String): Result<VirusTotalStats?> = withContext(Dispatchers.IO) {
        if (sha256.isBlank()) return@withContext Result.success(null)

        // Check local cache
        val cached = appSentryDao.getVirusTotalCache(sha256)
        val oneWeek = 7 * 24 * 60 * 60 * 1000L
        if (cached != null && (System.currentTimeMillis() - cached.cachedAt) < oneWeek) {
            return@withContext Result.success(
                VirusTotalStats(
                    harmless = cached.harmless,
                    malicious = cached.malicious,
                    suspicious = cached.suspicious,
                    undetected = cached.undetected,
                    totalEngines = cached.totalEngines,
                    permalink = cached.permalink
                )
            )
        }

        val apiKey = BuildConfig.VIRUSTOTAL_API_KEY
        if (apiKey.isBlank()) {
            return@withContext Result.success(null)
        }

        try {
            val response = virusTotalApi.getFileReport(apiKey, sha256)
            if (response.isSuccessful) {
                val stats = response.body()?.data?.attributes?.lastAnalysisStats
                if (stats != null) {
                    val harmless = stats.harmless ?: 0
                    val malicious = stats.malicious ?: 0
                    val suspicious = stats.suspicious ?: 0
                    val undetected = stats.undetected ?: 0
                    val total = harmless + malicious + suspicious + undetected

                    val entity = VirusTotalCacheEntity(
                        sha256 = sha256,
                        harmless = harmless,
                        malicious = malicious,
                        suspicious = suspicious,
                        undetected = undetected,
                        totalEngines = total,
                        permalink = "https://www.virustotal.com/gui/file/$sha256",
                        cachedAt = System.currentTimeMillis()
                    )
                    appSentryDao.insertVirusTotalCache(entity)

                    return@withContext Result.success(
                        VirusTotalStats(
                            harmless = harmless,
                            malicious = malicious,
                            suspicious = suspicious,
                            undetected = undetected,
                            totalEngines = total,
                            permalink = entity.permalink
                        )
                    )
                }
            }
            Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getInstallerPackage(packageName: String): String? {
        val pm = context.packageManager
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                pm.getInstallSourceInfo(packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                pm.getInstallerPackageName(packageName)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun isInstallerUnknown(installer: String?): Boolean {
        if (installer == null || installer.isBlank()) return true
        val knownStores = listOf(
            "com.android.vending",
            "com.google.android.packageinstaller",
            "com.sec.android.app.samsungapps",
            "com.xiaomi.mipicks",
            "com.oppo.market",
            "com.heytap.market",
            "com.vivo.appstore",
            "com.amazon.venezia"
        )
        return knownStores.none { installer.startsWith(it) }
    }

    private fun getVersionCode(pkg: PackageInfo): Long {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pkg.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            pkg.versionCode.toLong()
        }
    }
}

// Converters
private fun ScannedAppEntity.toDomain(pm: PackageManager, gson: Gson): ScannedApp {
    val permType = object : TypeToken<List<String>>() {}.type
    val indicatorType = object : TypeToken<List<String>>() {}.type
    val perms: List<String> = try { gson.fromJson(permissionsJson, permType) } catch (e: Exception) { emptyList() }
    val indStrings: List<String> = try { gson.fromJson(threatIndicatorsJson, indicatorType) } catch (e: Exception) { emptyList() }
    val indicators = indStrings.mapNotNull {
        try { ThreatIndicator.valueOf(it) } catch (e: Exception) { null }
    }.toSet()

    val icon = try { pm.getApplicationIcon(packageName) } catch (e: Exception) { null }

    return ScannedApp(
        packageName = packageName,
        appName = appName,
        icon = icon,
        installedAt = installedAt,
        updatedAt = updatedAt,
        versionName = versionName,
        versionCode = versionCode,
        permissions = perms,
        isSideLoaded = isSideLoaded,
        installerPackage = installerPackage,
        riskScore = riskScore,
        riskLevel = try { AppRiskLevel.valueOf(riskLevel) } catch (e: Exception) { AppRiskLevel.SAFE },
        threatIndicators = indicators,
        scannedAt = scannedAt,
        isTrusted = isTrusted
    )
}

private fun ApkInspectionResult.toEntity(gson: Gson): ApkInspectionEntity {
    return ApkInspectionEntity(
        filePath = filePath,
        fileName = fileName,
        fileSize = fileSize,
        sha256 = sha256,
        packageName = packageName,
        appName = appName,
        versionName = versionName,
        versionCode = versionCode,
        requestedPermissionsJson = gson.toJson(requestedPermissions),
        riskScore = riskScore,
        riskLevel = riskLevel.name,
        threatIndicatorsJson = gson.toJson(threatIndicators.map { it.name }),
        harmlessCount = virusTotalStats?.harmless ?: 0,
        maliciousCount = virusTotalStats?.malicious ?: 0,
        suspiciousCount = virusTotalStats?.suspicious ?: 0,
        undetectedCount = virusTotalStats?.undetected ?: 0,
        totalEngines = virusTotalStats?.totalEngines ?: 0,
        permalink = virusTotalStats?.permalink,
        inspectedAt = inspectedAt,
        isLegitimateCertificate = isLegitimateCertificate,
        errorMessage = errorMessage
    )
}

private fun ApkInspectionEntity.toDomain(gson: Gson): ApkInspectionResult {
    val permType = object : TypeToken<List<String>>() {}.type
    val indicatorType = object : TypeToken<List<String>>() {}.type
    val perms: List<String> = try { gson.fromJson(requestedPermissionsJson, permType) } catch (e: Exception) { emptyList() }
    val indStrings: List<String> = try { gson.fromJson(threatIndicatorsJson, indicatorType) } catch (e: Exception) { emptyList() }
    val indicators = indStrings.mapNotNull {
        try { ThreatIndicator.valueOf(it) } catch (e: Exception) { null }
    }.toSet()

    val vtStats = if (totalEngines > 0) {
        VirusTotalStats(
            harmless = harmlessCount,
            malicious = maliciousCount,
            suspicious = suspiciousCount,
            undetected = undetectedCount,
            totalEngines = totalEngines,
            permalink = permalink
        )
    } else null

    return ApkInspectionResult(
        filePath = filePath,
        fileName = fileName,
        fileSize = fileSize,
        sha256 = sha256,
        packageName = packageName,
        appName = appName,
        versionName = versionName,
        versionCode = versionCode,
        requestedPermissions = perms,
        riskScore = riskScore,
        riskLevel = try { AppRiskLevel.valueOf(riskLevel) } catch (e: Exception) { AppRiskLevel.SAFE },
        threatIndicators = indicators,
        virusTotalStats = vtStats,
        inspectedAt = inspectedAt,
        isLegitimateCertificate = isLegitimateCertificate,
        errorMessage = errorMessage
    )
}
