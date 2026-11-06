package com.chs.yourwol.data.db

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Query("SELECT * FROM devices ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE id = :id")
    suspend fun getById(id: Long): DeviceEntity?

    @Upsert
    suspend fun upsert(entity: DeviceEntity): Long

    @Delete
    suspend fun delete(id: Long)

    @Query("UPDATE devices SET hostKeyFingerprint = :fingerprint WHERE id = :id")
    suspend fun updateHostKey(id: Long, fingerprint: String?)
}
