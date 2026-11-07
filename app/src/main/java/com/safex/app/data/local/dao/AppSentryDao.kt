// data/local/dao/AppSentryDao.kt
package com.safex.app.data.local.dao

import androidx.room.*
import com.safex.app.data.local.entity.ApkInspectionEntity
import com.safex.app.data.local.entity.ScannedAppEntity
import com.safex.app.data.local.entity.TrustedAppEntity
import com.safex.app.data.local.entity.VirusTotalCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSentryDao {

    // Scanned Apps
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScannedApps(apps: List<ScannedAppEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScannedApp(app: ScannedAppEntity)

    @Query("SELECT * FROM scanned_apps ORDER BY riskScore DESC")
    fun getScannedAppsFlow(): Flow<List<ScannedAppEntity>>

    @Query("SELECT * FROM scanned_apps ORDER BY riskScore DESC")
    suspend fun getScannedApps(): List<ScannedAppEntity>

    @Query("SELECT * FROM scanned_apps WHERE packageName = :packageName LIMIT 1")
    suspend fun getScannedApp(packageName: String): ScannedAppEntity?

    @Query("DELETE FROM scanned_apps")
    suspend fun clearScannedApps()

    // APK Inspections
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApkInspection(inspection: ApkInspectionEntity)

    @Query("SELECT * FROM apk_inspections ORDER BY inspectedAt DESC")
    fun getApkInspectionsFlow(): Flow<List<ApkInspectionEntity>>

    @Query("DELETE FROM apk_inspections WHERE filePath = :filePath")
    suspend fun deleteApkInspection(filePath: String)

    // VirusTotal Cache
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVirusTotalCache(cache: VirusTotalCacheEntity)

    @Query("SELECT * FROM virustotal_cache WHERE sha256 = :sha256 LIMIT 1")
    suspend fun getVirusTotalCache(sha256: String): VirusTotalCacheEntity?

    // Trusted Apps
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrustedApp(trusted: TrustedAppEntity)

    @Query("DELETE FROM trusted_apps WHERE packageName = :packageName")
    suspend fun deleteTrustedApp(packageName: String)

    @Query("SELECT EXISTS(SELECT 1 FROM trusted_apps WHERE packageName = :packageName)")
    suspend fun isAppTrusted(packageName: String): Boolean

    @Query("SELECT * FROM trusted_apps")
    fun getTrustedAppsFlow(): Flow<List<TrustedAppEntity>>
}
