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
}
