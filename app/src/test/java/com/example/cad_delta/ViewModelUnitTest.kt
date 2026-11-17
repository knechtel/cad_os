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

    @Test
    fun testGetCurrentDateString() {
        val currentDate = com.example.cad_delta.ui.utils.DateUtils.getCurrentDateString()
        assertNotNull(currentDate)
        org.junit.Assert.assertTrue(currentDate.matches(Regex("\\d{2}/\\d{2}/\\d{4}")))
    }

    @Test
    fun testDailyRevenueStateManagement() {
        val fakeRepo = createFakeRepository()
        val viewModel = com.example.cad_delta.ui.viewmodel.MainViewModel(fakeRepo)

        val todayStr = com.example.cad_delta.ui.utils.DateUtils.getCurrentDateString()
        assertEquals(todayStr, viewModel.revenueDate.value)

        viewModel.onRevenueDateChange("15/05/2025")
        assertEquals("15/05/2025", viewModel.revenueDate.value)

        viewModel.clearRevenueDate()
        assertEquals(todayStr, viewModel.revenueDate.value)
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun testDailyRevenueCalculationLogic() = kotlinx.coroutines.test.runTest {
        val timestamp1 = 1700000000000L
        val dateStr1 = com.example.cad_delta.ui.utils.DateUtils.formatDateOnly(timestamp1)

        val timestamp2 = 1600000000000L
        val dateStr2 = com.example.cad_delta.ui.utils.DateUtils.formatDateOnly(timestamp2)

        val devicesMap = mapOf(
            1L to listOf(
                Device(10L, 1L, "Phone A1", "Brand", "SN1", "Desc", 150.0, isDelivered = true, deliveredAt = timestamp1),
                Device(11L, 1L, "Phone A2", "Brand", "SN2", "Desc", 250.0, isDelivered = true, deliveredAt = timestamp1)
            ),
            42L to listOf(
                Device(20L, 42L, "Phone B1", "Brand", "SN3", "Desc", 500.0, isDelivered = true, deliveredAt = timestamp2),
                Device(21L, 42L, "Phone B2", "Brand", "SN4", "Desc", 300.0, isDelivered = false, deliveredAt = null)
            )
        )

        val fakeRepo = createFakeRepositoryWithDevices(devicesMap)
        val viewModel = com.example.cad_delta.ui.viewmodel.MainViewModel(fakeRepo)

        // Select dateStr1
        viewModel.onRevenueDateChange(dateStr1)
        val customersDate1 = viewModel.dailyRevenueCustomers.first { list -> list.isNotEmpty() }
        assertEquals(1, customersDate1.size)
        assertEquals(1L, customersDate1.first().customer.id)
        assertEquals(2, customersDate1.first().devices.size)

        val totalRev1 = viewModel.totalDailyRevenue.first { it > 0.0 }
        assertEquals(400.0, totalRev1, 0.001)

        // Select dateStr2
        viewModel.onRevenueDateChange(dateStr2)
        val customersDate2 = viewModel.dailyRevenueCustomers.first { list -> list.isNotEmpty() && list.first().customer.id == 42L }
        assertEquals(1, customersDate2.size)
        assertEquals(42L, customersDate2.first().customer.id)
        assertEquals(1, customersDate2.first().devices.size)

        val totalRev2 = viewModel.totalDailyRevenue.first { it == 500.0 }
        assertEquals(500.0, totalRev2, 0.001)

        // Select non-existent date
        viewModel.onRevenueDateChange("01/01/1990")
        val customersEmpty = viewModel.dailyRevenueCustomers.first { list -> list.isEmpty() }
        assertEquals(0, customersEmpty.size)
    }

    @Test
    fun testIsCustomerLockedHelper() {
        val customerNoDevices = CustomerWithDevices(
            customer = Customer(1L, "Zero Devices", "000", "000", "Addr 0"),
            devices = emptyList()
        )
        org.junit.Assert.assertFalse(isCustomerLocked(customerNoDevices))
        org.junit.Assert.assertFalse(customerNoDevices.isLocked)
        org.junit.Assert.assertFalse(isCustomerLocked(emptyList<Device>()))
        org.junit.Assert.assertFalse(isCustomerLocked(null as CustomerWithDevices?))

        val unlockedCustomer = CustomerWithDevices(
            customer = Customer(1L, "Alice", "111", "000", "Addr 1"),
            devices = listOf(
                Device(10L, 1L, "Phone A", "Brand", "SN1", "Desc", 100.0, isDelivered = false, deliveredAt = null)
            )
        )
        org.junit.Assert.assertFalse(isCustomerLocked(unlockedCustomer))
        org.junit.Assert.assertFalse(unlockedCustomer.isLocked)
        org.junit.Assert.assertFalse(isCustomerLocked(unlockedCustomer.devices))

        val lockedCustomerByDelivered = CustomerWithDevices(
            customer = Customer(2L, "Bob", "222", "111", "Addr 2"),
            devices = listOf(
                Device(20L, 2L, "Phone B", "Brand", "SN2", "Desc", 200.0, isDelivered = true, deliveredAt = System.currentTimeMillis())
            )
        )
        org.junit.Assert.assertTrue(isCustomerLocked(lockedCustomerByDelivered))
        org.junit.Assert.assertTrue(lockedCustomerByDelivered.isLocked)
        org.junit.Assert.assertTrue(isCustomerLocked(lockedCustomerByDelivered.devices))

        val lockedCustomerByDeliveredAt = CustomerWithDevices(
            customer = Customer(3L, "Charlie", "333", "222", "Addr 3"),
            devices = listOf(
                Device(30L, 3L, "Phone C", "Brand", "SN3", "Desc", 300.0, isDelivered = false, deliveredAt = System.currentTimeMillis())
            )
        )
        org.junit.Assert.assertTrue(isCustomerLocked(lockedCustomerByDeliveredAt))
        org.junit.Assert.assertTrue(lockedCustomerByDeliveredAt.isLocked)
        org.junit.Assert.assertTrue(isCustomerLocked(lockedCustomerByDeliveredAt.devices))
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun testCustomerFormViewModelLocking() = kotlinx.coroutines.test.runTest {
        var updateCalled = false
        val lockedCustomerWithDevices = CustomerWithDevices(
            customer = Customer(1L, "Alice", "111", "000", "Addr 1"),
            devices = listOf(
                Device(10L, 1L, "Phone A", "Brand", "SN1", "Desc", 100.0, isDelivered = true, deliveredAt = 1000L)
            )
        )

        val fakeCustomerDao = object : CustomerDao {
            override suspend fun insertCustomer(customer: Customer) = 1L
            override suspend fun updateCustomer(customer: Customer): Int {
                updateCalled = true
                return 1
            }
            override suspend fun deleteCustomer(customer: Customer) = 1
            override fun getAllCustomers() = kotlinx.coroutines.flow.flowOf(emptyList<Customer>())
            override fun getCustomerById(id: Long) = kotlinx.coroutines.flow.flowOf(lockedCustomerWithDevices.customer)
            override fun getCustomersWithDevices() = kotlinx.coroutines.flow.flowOf(listOf(lockedCustomerWithDevices))
            override fun getCustomerWithDevicesById(id: Long) = kotlinx.coroutines.flow.flowOf(lockedCustomerWithDevices)
        }
        val fakeDeviceDao = object : DeviceDao {
            override suspend fun insertDevice(device: Device) = 1L
            override suspend fun updateDevice(device: Device) = 1
            override suspend fun deleteDevice(device: Device) = 1
            override fun getAllDevices() = kotlinx.coroutines.flow.flowOf(emptyList<Device>())
            override fun getDeviceById(id: Long) = kotlinx.coroutines.flow.flowOf(null)
            override fun getDevicesByCustomerId(customerId: Long) = kotlinx.coroutines.flow.flowOf(emptyList<Device>())
        }
        val repo = AppRepository(fakeCustomerDao, fakeDeviceDao)
        val viewModel = com.example.cad_delta.ui.viewmodel.CustomerFormViewModel(repo)

        viewModel.loadCustomer(1L)
        val state = viewModel.uiState.first { it.id == 1L }
        org.junit.Assert.assertTrue(state.isLocked)

        var successCallback = false
        viewModel.saveCustomer { successCallback = true }
        org.junit.Assert.assertFalse(updateCalled)
        org.junit.Assert.assertFalse(successCallback)
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun testDeviceFormViewModelLocking() = kotlinx.coroutines.test.runTest {
        var insertDeviceCalled = false
        var updateDeviceCalled = false
        val lockedCustomerWithDevices = CustomerWithDevices(
            customer = Customer(1L, "Alice", "111", "000", "Addr 1"),
            devices = listOf(
                Device(10L, 1L, "Phone A", "Brand", "SN1", "Desc", 100.0, isDelivered = true, deliveredAt = 1000L)
            )
        )

        val fakeCustomerDao = object : CustomerDao {
            override suspend fun insertCustomer(customer: Customer) = 1L
            override suspend fun updateCustomer(customer: Customer) = 1
            override suspend fun deleteCustomer(customer: Customer) = 1
            override fun getAllCustomers() = kotlinx.coroutines.flow.flowOf(emptyList<Customer>())
            override fun getCustomerById(id: Long) = kotlinx.coroutines.flow.flowOf(lockedCustomerWithDevices.customer)
            override fun getCustomersWithDevices() = kotlinx.coroutines.flow.flowOf(listOf(lockedCustomerWithDevices))
            override fun getCustomerWithDevicesById(id: Long) = kotlinx.coroutines.flow.flowOf(lockedCustomerWithDevices)
        }
        val fakeDeviceDao = object : DeviceDao {
            override suspend fun insertDevice(device: Device): Long {
                insertDeviceCalled = true
                return 1L
            }
            override suspend fun updateDevice(device: Device): Int {
                updateDeviceCalled = true
                return 1
            }
            override suspend fun deleteDevice(device: Device) = 1
            override fun getAllDevices() = kotlinx.coroutines.flow.flowOf(emptyList<Device>())
            override fun getDeviceById(id: Long) = kotlinx.coroutines.flow.flowOf(lockedCustomerWithDevices.devices.first())
            override fun getDevicesByCustomerId(customerId: Long) = kotlinx.coroutines.flow.flowOf(lockedCustomerWithDevices.devices)
        }
        val repo = AppRepository(fakeCustomerDao, fakeDeviceDao)
        val viewModel = com.example.cad_delta.ui.viewmodel.DeviceFormViewModel(repo)

        viewModel.loadDevice(1L, -1L)
        val state = viewModel.uiState.first { it.customerId == 1L }
        org.junit.Assert.assertTrue(state.isLocked)

        viewModel.onModelChange("New Phone")
        viewModel.onBrandChange("Brand")
        viewModel.onSerialNumberChange("SN999")
        viewModel.onPriceChange("100")

        var successCallback = false
        viewModel.saveDevice(1L) { successCallback = true }
        org.junit.Assert.assertFalse(insertDeviceCalled)
        org.junit.Assert.assertFalse(updateDeviceCalled)
        org.junit.Assert.assertFalse(successCallback)
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun testCustomerFormViewModelUnlockedEditing() = kotlinx.coroutines.test.runTest {
        var updateCalled = false
        val unlockedCustomerWithDevices = CustomerWithDevices(
            customer = Customer(1L, "Alice", "111", "000", "Addr 1"),
            devices = listOf(
                Device(10L, 1L, "Phone A", "Brand", "SN1", "Desc", 100.0, isDelivered = false, deliveredAt = null)
            )
        )

        val fakeCustomerDao = object : CustomerDao {
            override suspend fun insertCustomer(customer: Customer) = 1L
            override suspend fun updateCustomer(customer: Customer): Int {
                updateCalled = true
                return 1
            }
            override suspend fun deleteCustomer(customer: Customer) = 1
            override fun getAllCustomers() = kotlinx.coroutines.flow.flowOf(emptyList<Customer>())
            override fun getCustomerById(id: Long) = kotlinx.coroutines.flow.flowOf(unlockedCustomerWithDevices.customer)
            override fun getCustomersWithDevices() = kotlinx.coroutines.flow.flowOf(listOf(unlockedCustomerWithDevices))
            override fun getCustomerWithDevicesById(id: Long) = kotlinx.coroutines.flow.flowOf(unlockedCustomerWithDevices)
        }
        val fakeDeviceDao = object : DeviceDao {
            override suspend fun insertDevice(device: Device) = 1L
            override suspend fun updateDevice(device: Device) = 1
            override suspend fun deleteDevice(device: Device) = 1
            override fun getAllDevices() = kotlinx.coroutines.flow.flowOf(emptyList<Device>())
            override fun getDeviceById(id: Long) = kotlinx.coroutines.flow.flowOf(null)
            override fun getDevicesByCustomerId(customerId: Long) = kotlinx.coroutines.flow.flowOf(emptyList<Device>())
        }
        val repo = AppRepository(fakeCustomerDao, fakeDeviceDao)
        val viewModel = com.example.cad_delta.ui.viewmodel.CustomerFormViewModel(repo)

        viewModel.loadCustomer(1L)
        val state = viewModel.uiState.first { it.id == 1L }
        org.junit.Assert.assertFalse(state.isLocked)

        viewModel.onNameChange("Alice Updated")
        viewModel.onPhoneChange("999")
        viewModel.onCpfChange("111")
        viewModel.onAddressChange("Addr 2")

        var successCallback = false
        viewModel.saveCustomer { successCallback = true }
        org.junit.Assert.assertTrue(updateCalled)
        org.junit.Assert.assertTrue(successCallback)
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun testDeviceFormViewModelUnlockedEditing() = kotlinx.coroutines.test.runTest {
        var updateDeviceCalled = false
        val unlockedCustomerWithDevices = CustomerWithDevices(
            customer = Customer(1L, "Alice", "111", "000", "Addr 1"),
            devices = listOf(
                Device(10L, 1L, "Phone A", "Brand", "SN1", "Desc", 100.0, isDelivered = false, deliveredAt = null)
            )
        )

        val fakeCustomerDao = object : CustomerDao {
            override suspend fun insertCustomer(customer: Customer) = 1L
            override suspend fun updateCustomer(customer: Customer) = 1
            override suspend fun deleteCustomer(customer: Customer) = 1
            override fun getAllCustomers() = kotlinx.coroutines.flow.flowOf(emptyList<Customer>())
            override fun getCustomerById(id: Long) = kotlinx.coroutines.flow.flowOf(unlockedCustomerWithDevices.customer)
            override fun getCustomersWithDevices() = kotlinx.coroutines.flow.flowOf(listOf(unlockedCustomerWithDevices))
            override fun getCustomerWithDevicesById(id: Long) = kotlinx.coroutines.flow.flowOf(unlockedCustomerWithDevices)
        }
        val fakeDeviceDao = object : DeviceDao {
            override suspend fun insertDevice(device: Device) = 1L
            override suspend fun updateDevice(device: Device): Int {
                updateDeviceCalled = true
                return 1
            }
            override suspend fun deleteDevice(device: Device) = 1
            override fun getAllDevices() = kotlinx.coroutines.flow.flowOf(emptyList<Device>())
            override fun getDeviceById(id: Long) = kotlinx.coroutines.flow.flowOf(unlockedCustomerWithDevices.devices.first())
            override fun getDevicesByCustomerId(customerId: Long) = kotlinx.coroutines.flow.flowOf(unlockedCustomerWithDevices.devices)
        }
        val repo = AppRepository(fakeCustomerDao, fakeDeviceDao)
        val viewModel = com.example.cad_delta.ui.viewmodel.DeviceFormViewModel(repo)

        viewModel.loadDevice(1L, 10L)
        val state = viewModel.uiState.first { it.id == 10L }
        org.junit.Assert.assertFalse(state.isLocked)
        org.junit.Assert.assertFalse(state.isAlreadyDeliveredOnLoad)

        viewModel.onModelChange("Updated Phone A")
        viewModel.onBrandChange("New Brand")
        viewModel.onSerialNumberChange("SN100")
        viewModel.onPriceChange("250.00")

        var successCallback = false
        viewModel.saveDevice(1L) { successCallback = true }
        org.junit.Assert.assertTrue(updateDeviceCalled)
        org.junit.Assert.assertTrue(successCallback)
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun testDeviceFormViewModelCheckboxLocking() = kotlinx.coroutines.test.runTest {
        val repo = createFakeRepository()
        val viewModel = com.example.cad_delta.ui.viewmodel.DeviceFormViewModel(repo)

        // Initially false
        org.junit.Assert.assertFalse(viewModel.uiState.value.isDelivered)

        // Mark as delivered
        viewModel.onIsDeliveredChange(true)
        org.junit.Assert.assertTrue(viewModel.uiState.value.isDelivered)
        org.junit.Assert.assertNotNull(viewModel.uiState.value.deliveredAt)

        // Attempting to uncheck should be ignored
        viewModel.onIsDeliveredChange(false)
        org.junit.Assert.assertTrue(viewModel.uiState.value.isDelivered)
        org.junit.Assert.assertNotNull(viewModel.uiState.value.deliveredAt)
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun testMainViewModelToggleDeviceDeliveredUncheckingPrevented() = kotlinx.coroutines.test.runTest {
        var updateCalled = false
        val fakeCustomerDao = object : CustomerDao {
            override suspend fun insertCustomer(customer: Customer) = 1L
            override suspend fun updateCustomer(customer: Customer) = 1
            override suspend fun deleteCustomer(customer: Customer) = 1
            override fun getAllCustomers() = kotlinx.coroutines.flow.flowOf(emptyList<Customer>())
            override fun getCustomerById(id: Long) = kotlinx.coroutines.flow.flowOf(null)
            override fun getCustomersWithDevices() = kotlinx.coroutines.flow.flowOf(emptyList<CustomerWithDevices>())
            override fun getCustomerWithDevicesById(id: Long) = kotlinx.coroutines.flow.flowOf(null)
        }
        val fakeDeviceDao = object : DeviceDao {
            override suspend fun insertDevice(device: Device) = 1L
            override suspend fun updateDevice(device: Device): Int {
                updateCalled = true
                return 1
            }
            override suspend fun deleteDevice(device: Device) = 1
            override fun getAllDevices() = kotlinx.coroutines.flow.flowOf(emptyList<Device>())
            override fun getDeviceById(id: Long) = kotlinx.coroutines.flow.flowOf(null)
            override fun getDevicesByCustomerId(customerId: Long) = kotlinx.coroutines.flow.flowOf(emptyList<Device>())
        }
        val repo = AppRepository(fakeCustomerDao, fakeDeviceDao)
        val viewModel = com.example.cad_delta.ui.viewmodel.MainViewModel(repo)

        val deliveredDevice = Device(
            id = 1L,
            customerId = 1L,
            model = "Pixel 7",
            brand = "Google",
            serialNumber = "SN123",
            description = "Test",
            price = 999.0,
            isDelivered = true,
            deliveredAt = 1000L
        )

        viewModel.onToggleDeviceDelivered(deliveredDevice, false)
        org.junit.Assert.assertFalse(updateCalled)
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun testImportDataFromJsonInViewModel() = kotlinx.coroutines.test.runTest {
        val insertedCustomers = mutableListOf<Customer>()
        val insertedDevices = mutableListOf<Device>()

        val fakeCustomerDao = object : CustomerDao {
            private var currentId = 0L
            override suspend fun insertCustomer(customer: Customer): Long {
                currentId++
                insertedCustomers.add(customer.copy(id = currentId))
                return currentId
            }
            override suspend fun updateCustomer(customer: Customer) = 1
            override suspend fun deleteCustomer(customer: Customer) = 1
            override fun getAllCustomers() = kotlinx.coroutines.flow.flowOf(emptyList<Customer>())
            override fun getCustomerById(id: Long) = kotlinx.coroutines.flow.flowOf(null)
            override fun getCustomersWithDevices() = kotlinx.coroutines.flow.flowOf(emptyList<CustomerWithDevices>())
            override fun getCustomerWithDevicesById(id: Long) = kotlinx.coroutines.flow.flowOf(null)
        }
        val fakeDeviceDao = object : DeviceDao {
            private var currentId = 0L
            override suspend fun insertDevice(device: Device): Long {
                currentId++
                insertedDevices.add(device.copy(id = currentId))
                return currentId
            }
            override suspend fun updateDevice(device: Device) = 1
            override suspend fun deleteDevice(device: Device) = 1
            override fun getAllDevices() = kotlinx.coroutines.flow.flowOf(emptyList<Device>())
            override fun getDeviceById(id: Long) = kotlinx.coroutines.flow.flowOf(null)
            override fun getDevicesByCustomerId(customerId: Long) = kotlinx.coroutines.flow.flowOf(emptyList<Device>())
        }

        val repository = AppRepository(fakeCustomerDao, fakeDeviceDao)
        val viewModel = com.example.cad_delta.ui.viewmodel.MainViewModel(repository)

        val jsonInput = """
            [
              {
                "id": 1,
                "nome": "Maiquel Knechtel",
                "telefone": "51993501980",
                "cpf": "01637948042",
                "endereco": "coca Barcelos",
                "aparelhos": [
                  {
                    "id": 1,
                    "cliente": 1,
                    "modelo": "model 2",
                    "marca": "Lenovo",
                    "numero_serial": "654676",
                    "descricao": "não liga",
                    "preco": "188.00",
                    "createdAt": 1793734167443,
                    "deliveredAt": 1794101982229
                  }
                ]
              }
            ]
        """.trimIndent()

        viewModel.importDataFromJson(jsonInput)

        testScheduler.advanceUntilIdle()

        assertEquals(1, insertedCustomers.size)
        assertEquals("Maiquel Knechtel", insertedCustomers[0].name)
        assertEquals(1L, insertedCustomers[0].id)

        assertEquals(1, insertedDevices.size)
        assertEquals(1L, insertedDevices[0].customerId)
        assertEquals("model 2", insertedDevices[0].model)
        assertEquals(188.0, insertedDevices[0].price, 0.001)

        val msg = viewModel.exportMessage.value
        assertNotNull(msg)
        org.junit.Assert.assertTrue(msg!!.contains("Importação concluída: 1 cliente(s) e 1 aparelho(s) importados!"))
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
