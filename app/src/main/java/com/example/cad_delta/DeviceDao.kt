package com.example.cad_delta

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: Device): Long

    @Update
    suspend fun updateDevice(device: Device): Int

    @Delete
    suspend fun deleteDevice(device: Device): Int

    @Query("SELECT * FROM devices")
    fun getAllDevices(): Flow<List<Device>>

    @Query("SELECT * FROM devices WHERE id = :id")
    fun getDeviceById(id: Long): Flow<Device?>

    @Query("SELECT * FROM devices WHERE customerId = :customerId")
    fun getDevicesByCustomerId(customerId: Long): Flow<List<Device>>
}
