package com.example.cad_delta

import com.example.cad_delta.ui.utils.CurrencyUtils
import com.example.cad_delta.ui.viewmodel.CustomerFormState
import com.example.cad_delta.ui.viewmodel.DeviceFormState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ViewModelUnitTest {

    @Test
    fun testCurrencyUtilsFormat() {
        val formattedZero = CurrencyUtils.formatBrl(0.0)
        assertNotNull(formattedZero)
        val formattedValue = CurrencyUtils.formatBrl(1250.50)
        assertNotNull(formattedValue)
    }

    @Test
    fun testCustomerFormStateValidation() {
        val initialState = CustomerFormState()
        assertEquals("", initialState.name)
        assertEquals("", initialState.phone)
        assertEquals("", initialState.cpf)
        assertEquals("", initialState.address)
        assertNull(initialState.nameError)
    }

    @Test
    fun testDeviceFormStateValidation() {
        val initialState = DeviceFormState()
        assertEquals("", initialState.model)
        assertEquals("", initialState.brand)
        assertEquals("", initialState.serialNumber)
        assertEquals("", initialState.price)
        assertNull(initialState.priceError)
    }

    @Test
    fun testPdfReportDataCalculation() {
        val customer = Customer(1L, "Maria Silva", "11999998888", "12345678900", "Rua A, 123")
        val devices = listOf(
            Device(1L, 1L, "iPhone 13", "Apple", "SN001", "Tela trincada", 1500.0),
            Device(2L, 1L, "Galaxy S21", "Samsung", "SN002", "Bateria ruim", 800.0)
        )
        val customerWithDevices = CustomerWithDevices(customer, devices)

        assertEquals(2, customerWithDevices.devices.size)
        assertEquals(2300.0, customerWithDevices.devices.sumOf { it.price }, 0.001)
    }

    @Test
    fun testCustomerIdAsOsNumber() {
        val customer = Customer(42L, "João Souza", "11988887777", "98765432100", "Av. Brasil, 500")
        val osNumber = "OS #${customer.id}"
        assertEquals("OS #42", osNumber)
    }

    @Test
    fun testDateUtilsFormatting() {
        val timestamp = 1700000000000L
        val formatted = com.example.cad_delta.ui.utils.DateUtils.formatDate(timestamp)
        assertNotNull(formatted)
        org.junit.Assert.assertTrue(formatted.contains("/"))
        org.junit.Assert.assertTrue(formatted.contains(":"))
    }

    @Test
    fun testDateUtilsFormattingWithNullAndZero() {
        val formattedNull = com.example.cad_delta.ui.utils.DateUtils.formatDate(null)
        assertEquals("-", formattedNull)
        val formattedZero = com.example.cad_delta.ui.utils.DateUtils.formatDate(0L)
        assertEquals("-", formattedZero)
        val formattedNegative = com.example.cad_delta.ui.utils.DateUtils.formatDate(-100L)
        assertEquals("-", formattedNegative)
    }

    @Test
    fun testDeviceCreatedAtDefaultAndPreservation() {
        val startTime = System.currentTimeMillis()
        val device = Device(
            id = 0L,
            customerId = 1L,
            model = "Pixel 7",
            brand = "Google",
            serialNumber = "SN123",
            description = "Test",
            price = 999.0
        )
        org.junit.Assert.assertTrue(device.createdAt >= startTime)
        org.junit.Assert.assertFalse(device.isDelivered)
        assertNull(device.deliveredAt)

        val fixedTimestamp = 1600000000000L
        val oldDevice = device.copy(id = 1L, createdAt = fixedTimestamp)
        assertEquals(fixedTimestamp, oldDevice.createdAt)

        val updatedDevice = oldDevice.copy(model = "Pixel 7 Pro", isDelivered = true, deliveredAt = System.currentTimeMillis())
        assertEquals(fixedTimestamp, updatedDevice.createdAt)
        org.junit.Assert.assertTrue(updatedDevice.isDelivered)
        assertNotNull(updatedDevice.deliveredAt)
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @org.junit.Before
    fun setUp() {
        kotlinx.coroutines.Dispatchers.setMain(kotlinx.coroutines.test.UnconfinedTestDispatcher())
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @org.junit.After
    fun tearDown() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    @Test
    fun testOsFilterStateManagement() {
        val fakeRepo = createFakeRepository()
        val viewModel = com.example.cad_delta.ui.viewmodel.MainViewModel(fakeRepo)

        assertEquals("", viewModel.osFilter.value)
        assertEquals("", viewModel.searchQuery.value)
        assertEquals("", viewModel.dateFilter.value)
        assertEquals("", viewModel.deliveryDateFilter.value)

        viewModel.onOsFilterChange("42")
        assertEquals("42", viewModel.osFilter.value)

        viewModel.onDateFilterChange("25/12/2024")
        assertEquals("25/12/2024", viewModel.dateFilter.value)

        viewModel.onDeliveryDateFilterChange("26/12/2024")
        assertEquals("26/12/2024", viewModel.deliveryDateFilter.value)

        viewModel.onSearchQueryChange("Maria")
        assertEquals("Maria", viewModel.searchQuery.value)

        viewModel.clearOsFilter()
        assertEquals("", viewModel.osFilter.value)
        assertEquals("25/12/2024", viewModel.dateFilter.value)
        assertEquals("26/12/2024", viewModel.deliveryDateFilter.value)
        assertEquals("Maria", viewModel.searchQuery.value)

        viewModel.clearDateFilter()
        assertEquals("", viewModel.dateFilter.value)

        viewModel.clearDeliveryDateFilter()
        assertEquals("", viewModel.deliveryDateFilter.value)

        viewModel.onOsFilterChange("1")
        viewModel.onDateFilterChange("10/10/2024")
        viewModel.onDeliveryDateFilterChange("15/10/2024")
        viewModel.clearAllFilters()
        assertEquals("", viewModel.osFilter.value)
        assertEquals("", viewModel.searchQuery.value)
        assertEquals("", viewModel.dateFilter.value)
        assertEquals("", viewModel.deliveryDateFilter.value)
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun testOsFilteringLogic() = kotlinx.coroutines.test.runTest {
        val fakeRepo = createFakeRepository()
        val viewModel = com.example.cad_delta.ui.viewmodel.MainViewModel(fakeRepo)

        // Initial emission should contain all 3 customers
        val initialList = viewModel.customersWithDevices.first { it.isNotEmpty() }
        assertEquals(3, initialList.size)

        // Filter by exact ID 42
        viewModel.onOsFilterChange("42")
        val filteredList42 = viewModel.customersWithDevices.first { list -> list.any { it.customer.id == 42L } }
        assertEquals(1, filteredList42.size)
        assertEquals(42L, filteredList42.first().customer.id)

        // Filter by "OS #1"
        viewModel.onOsFilterChange("OS #1")
        val filteredList1 = viewModel.customersWithDevices.first { list -> list.size == 2 }
        assertEquals(2, filteredList1.size)

        // Clear OS filter
        viewModel.clearOsFilter()
        val resetList = viewModel.customersWithDevices.first { list -> list.size == 3 }
        assertEquals(3, resetList.size)
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun testDateFilteringLogic() = kotlinx.coroutines.test.runTest {
        val timestamp1 = 1700000000000L // e.g. 14/11/2023 depending on timezone
        val dateStr1 = com.example.cad_delta.ui.utils.DateUtils.formatDateOnly(timestamp1)

        val timestamp2 = 1600000000000L // e.g. 13/09/2020 depending on timezone
        val dateStr2 = com.example.cad_delta.ui.utils.DateUtils.formatDateOnly(timestamp2)

        val devicesMap = mapOf(
            1L to listOf(Device(10L, 1L, "Phone A", "Brand", "SN1", "Desc", 100.0, createdAt = timestamp1)),
            42L to listOf(Device(20L, 42L, "Phone B", "Brand", "SN2", "Desc", 200.0, createdAt = timestamp2))
        )

        val fakeRepo = createFakeRepositoryWithDevices(devicesMap)
        val viewModel = com.example.cad_delta.ui.viewmodel.MainViewModel(fakeRepo)

        // Initial emission should contain all 3 customers
        val initialList = viewModel.customersWithDevices.first { it.isNotEmpty() }
        assertEquals(3, initialList.size)

        // Filter by date matching customer 1 (Alice)
        viewModel.onDateFilterChange(dateStr1)
        val filteredListDate1 = viewModel.customersWithDevices.first { list -> list.size == 1 && list.first().customer.id == 1L }
        assertEquals(1, filteredListDate1.size)
        assertEquals(1L, filteredListDate1.first().customer.id)

        // Filter by date matching customer 42 (Bob)
        viewModel.onDateFilterChange(dateStr2)
        val filteredListDate2 = viewModel.customersWithDevices.first { list -> list.size == 1 && list.first().customer.id == 42L }
        assertEquals(1, filteredListDate2.size)
        assertEquals(42L, filteredListDate2.first().customer.id)

        // Clear date filter
        viewModel.clearDateFilter()
        val resetList = viewModel.customersWithDevices.first { list -> list.size == 3 }
        assertEquals(3, resetList.size)
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun testDeliveryDateFilteringLogic() = kotlinx.coroutines.test.runTest {
        val timestamp1 = 1700000000000L
        val deliveryDateStr1 = com.example.cad_delta.ui.utils.DateUtils.formatDateOnly(timestamp1)

        val timestamp2 = 1600000000000L
        val deliveryDateStr2 = com.example.cad_delta.ui.utils.DateUtils.formatDateOnly(timestamp2)

        val devicesMap = mapOf(
            1L to listOf(Device(10L, 1L, "Phone A", "Brand", "SN1", "Desc", 100.0, isDelivered = true, deliveredAt = timestamp1)),
            42L to listOf(Device(20L, 42L, "Phone B", "Brand", "SN2", "Desc", 200.0, isDelivered = false, deliveredAt = null)),
            100L to listOf(Device(30L, 100L, "Phone C", "Brand", "SN3", "Desc", 300.0, isDelivered = true, deliveredAt = timestamp2))
        )

        val fakeRepo = createFakeRepositoryWithDevices(devicesMap)
        val viewModel = com.example.cad_delta.ui.viewmodel.MainViewModel(fakeRepo)

        val initialList = viewModel.customersWithDevices.first { it.isNotEmpty() }
        assertEquals(3, initialList.size)

        // Filter by delivery date matching customer 1 (Alice)
        viewModel.onDeliveryDateFilterChange(deliveryDateStr1)
        val filteredList1 = viewModel.customersWithDevices.first { list -> list.size == 1 && list.first().customer.id == 1L }
        assertEquals(1, filteredList1.size)
        assertEquals(1L, filteredList1.first().customer.id)

        // Filter by delivery date matching customer 100 (Charlie)
        viewModel.onDeliveryDateFilterChange(deliveryDateStr2)
        val filteredList2 = viewModel.customersWithDevices.first { list -> list.size == 1 && list.first().customer.id == 100L }
        assertEquals(1, filteredList2.size)
        assertEquals(100L, filteredList2.first().customer.id)

        // Clear delivery date filter
        viewModel.clearDeliveryDateFilter()
        val resetList = viewModel.customersWithDevices.first { list -> list.size == 3 }
        assertEquals(3, resetList.size)
    }

    private fun createFakeRepositoryWithDevices(devicesMap: Map<Long, List<Device>>): AppRepository {
        val fakeCustomerDao = object : CustomerDao {
            override suspend fun insertCustomer(customer: Customer) = 1L
            override suspend fun updateCustomer(customer: Customer) = 1
            override suspend fun deleteCustomer(customer: Customer) = 1
            override fun getAllCustomers() = kotlinx.coroutines.flow.flowOf(emptyList<Customer>())
            override fun getCustomerById(id: Long) = kotlinx.coroutines.flow.flowOf(null)
            override fun getCustomersWithDevices() = kotlinx.coroutines.flow.flowOf(
                listOf(
                    CustomerWithDevices(Customer(1L, "Alice", "111", "000", "Addr 1"), devicesMap[1L] ?: emptyList()),
                    CustomerWithDevices(Customer(42L, "Bob", "222", "111", "Addr 2"), devicesMap[42L] ?: emptyList()),
                    CustomerWithDevices(Customer(100L, "Charlie", "333", "222", "Addr 3"), devicesMap[100L] ?: emptyList())
                )
            )
            override fun getCustomerWithDevicesById(id: Long) = kotlinx.coroutines.flow.flowOf(null)
        }
        val fakeDeviceDao = object : DeviceDao {
            override suspend fun insertDevice(device: Device) = 1L
            override suspend fun updateDevice(device: Device) = 1
            override suspend fun deleteDevice(device: Device) = 1
            override fun getAllDevices() = kotlinx.coroutines.flow.flowOf(emptyList<Device>())
            override fun getDeviceById(id: Long) = kotlinx.coroutines.flow.flowOf(null)
            override fun getDevicesByCustomerId(customerId: Long) = kotlinx.coroutines.flow.flowOf(emptyList<Device>())
        }
        return AppRepository(fakeCustomerDao, fakeDeviceDao)
    }

    private fun createFakeRepository(): AppRepository {
        val fakeCustomerDao = object : CustomerDao {
            override suspend fun insertCustomer(customer: Customer) = 1L
            override suspend fun updateCustomer(customer: Customer) = 1
            override suspend fun deleteCustomer(customer: Customer) = 1
            override fun getAllCustomers() = kotlinx.coroutines.flow.flowOf(emptyList<Customer>())
            override fun getCustomerById(id: Long) = kotlinx.coroutines.flow.flowOf(null)
            override fun getCustomersWithDevices() = kotlinx.coroutines.flow.flowOf(
                listOf(
                    CustomerWithDevices(Customer(1L, "Alice", "111", "000", "Addr 1"), emptyList()),
                    CustomerWithDevices(Customer(42L, "Bob", "222", "111", "Addr 2"), emptyList()),
                    CustomerWithDevices(Customer(100L, "Charlie", "333", "222", "Addr 3"), emptyList())
                )
            )
            override fun getCustomerWithDevicesById(id: Long) = kotlinx.coroutines.flow.flowOf(null)
        }
        val fakeDeviceDao = object : DeviceDao {
            override suspend fun insertDevice(device: Device) = 1L
            override suspend fun updateDevice(device: Device) = 1
            override suspend fun deleteDevice(device: Device) = 1
            override fun getAllDevices() = kotlinx.coroutines.flow.flowOf(emptyList<Device>())
            override fun getDeviceById(id: Long) = kotlinx.coroutines.flow.flowOf(null)
            override fun getDevicesByCustomerId(customerId: Long) = kotlinx.coroutines.flow.flowOf(emptyList<Device>())
        }
        return AppRepository(fakeCustomerDao, fakeDeviceDao)
    }
}
