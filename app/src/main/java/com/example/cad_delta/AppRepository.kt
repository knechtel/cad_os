package com.example.cad_delta

import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val customerDao: CustomerDao,
    private val deviceDao: DeviceDao
) {
    // Customer operations
    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()

    fun getCustomerById(id: Long): Flow<Customer?> = customerDao.getCustomerById(id)

    suspend fun insertCustomer(customer: Customer): Long = customerDao.insertCustomer(customer)

    suspend fun updateCustomer(customer: Customer): Int = customerDao.updateCustomer(customer)

    suspend fun deleteCustomer(customer: Customer): Int = customerDao.deleteCustomer(customer)

    // Device operations
    val allDevices: Flow<List<Device>> = deviceDao.getAllDevices()

    fun getDeviceById(id: Long): Flow<Device?> = deviceDao.getDeviceById(id)

    fun getDevicesByCustomerId(customerId: Long): Flow<List<Device>> =
        deviceDao.getDevicesByCustomerId(customerId)

    suspend fun insertDevice(device: Device): Long = deviceDao.insertDevice(device)

    suspend fun updateDevice(device: Device): Int = deviceDao.updateDevice(device)

    suspend fun deleteDevice(device: Device): Int = deviceDao.deleteDevice(device)

    // Customer with Devices relation operations
    val customersWithDevices: Flow<List<CustomerWithDevices>> =
        customerDao.getCustomersWithDevices()

    fun getCustomerWithDevicesById(id: Long): Flow<CustomerWithDevices?> =
        customerDao.getCustomerWithDevicesById(id)
}
