package com.example.cad_delta

import com.example.cad_delta.ui.utils.CurrencyUtils
import com.example.cad_delta.ui.viewmodel.CustomerFormState
import com.example.cad_delta.ui.viewmodel.DeviceFormState
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
}
