package com.safex.app.data.local.dao

import androidx.room.*
import com.safex.app.data.local.entity.SecurityIncidentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SecurityIncidentDao {

    @Query("SELECT * FROM security_incidents ORDER BY timestamp DESC")
    fun getAllIncidents(): Flow<List<SecurityIncidentEntity>>

    @Query("SELECT * FROM security_incidents WHERE incidentType = :type ORDER BY timestamp DESC")
    fun getIncidentsByType(type: String): Flow<List<SecurityIncidentEntity>>

    @Query("SELECT * FROM security_incidents ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentIncidents(limit: Int = 20): Flow<List<SecurityIncidentEntity>>

    @Query("SELECT * FROM security_incidents WHERE incidentId = :id")
    suspend fun getIncidentById(id: String): SecurityIncidentEntity?

    @Query("""
        SELECT * FROM security_incidents 
        WHERE incidentType = :type 
        AND timestamp > :sinceTimestamp 
        ORDER BY timestamp DESC LIMIT 1
    """)
    suspend fun getMostRecentIncidentOfType(type: String, sinceTimestamp: Long): SecurityIncidentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: SecurityIncidentEntity)

    @Update
    suspend fun updateIncident(incident: SecurityIncidentEntity)

    @Query("UPDATE security_incidents SET userAcknowledged = 1 WHERE incidentId = :id")
    suspend fun acknowledgeIncident(id: String)

    @Query("UPDATE security_incidents SET guardianNotificationStatus = :status WHERE incidentId = :id")
    suspend fun updateNotificationStatus(id: String, status: String)

    @Query("UPDATE security_incidents SET incidentStatus = :status WHERE incidentId = :id")
    suspend fun updateIncidentStatus(id: String, status: String)

    @Query("DELETE FROM security_incidents WHERE timestamp < :cutoffTimestamp")
    suspend fun deleteOldIncidents(cutoffTimestamp: Long)

    @Query("SELECT COUNT(*) FROM security_incidents WHERE userAcknowledged = 0")
    fun getUnacknowledgedCount(): Flow<Int>
}
