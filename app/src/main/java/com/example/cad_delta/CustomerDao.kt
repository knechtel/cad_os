package com.example.cad_delta

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Update
    suspend fun updateCustomer(customer: Customer): Int

    @Delete
    suspend fun deleteCustomer(customer: Customer): Int

    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id")
    fun getCustomerById(id: Long): Flow<Customer?>

    @Transaction
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getCustomersWithDevices(): Flow<List<CustomerWithDevices>>

    @Transaction
    @Query("SELECT * FROM customers WHERE id = :id")
    fun getCustomerWithDevicesById(id: Long): Flow<CustomerWithDevices?>
}
