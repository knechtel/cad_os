package com.example.cad_delta

import com.example.cad_delta.ui.utils.ExportUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportUnitTest {

    @Test
    fun testJsonExportFormat() {
        val customer1 = Customer(1L, "Maria Silva", "11999998888", "123.456.789-00", "Rua A, 123")
        val device1 = Device(101L, 1L, "iPhone 13", "Apple", "SN001", "Tela trincada", 1500.0)
        val device2 = Device(102L, 1L, "Galaxy S21", "Samsung", "SN002", "Bateria", 800.0, isDelivered = true, deliveredAt = 1700000000000L)

        val customer2 = Customer(2L, "João Souza", "11988887777", "987.654.321-11", "Av. B, 456")

        val data = listOf(
            CustomerWithDevices(customer1, listOf(device1, device2)),
            CustomerWithDevices(customer2, emptyList())
        )

        val jsonString = ExportUtils.generateJsonString(data)
        assertTrue(jsonString.isNotBlank())
        assertTrue(jsonString.startsWith("["))
        assertTrue(jsonString.endsWith("]"))
        assertTrue(jsonString.contains("\"name\": \"Maria Silva\""))
        assertTrue(jsonString.contains("\"devicesCount\": 2"))
        assertTrue(jsonString.contains("\"totalValue\": 2300.0"))
        assertTrue(jsonString.contains("\"model\": \"iPhone 13\""))
        assertTrue(jsonString.contains("\"brand\": \"Apple\""))
        assertTrue(jsonString.contains("\"isDelivered\": false"))
        assertTrue(jsonString.contains("\"isDelivered\": true"))
        assertTrue(jsonString.contains("\"deliveredAt\": 1700000000000"))
    }

    @Test
    fun testCsvExportFormat() {
        val customer = Customer(1L, "Maria \"Lúcia\" Silva", "11999998888", "123.456.789-00", "Rua A, 123")
        val device = Device(101L, 1L, "iPhone 13", "Apple", "SN001", "Tela trincada", 1500.0)

        val data = listOf(
            CustomerWithDevices(customer, listOf(device))
        )

        val (customersCsv, devicesCsv) = ExportUtils.generateCsvStrings(data)

        assertTrue(customersCsv.contains("\"ID OS\";\"Nome\";\"Telefone\";\"CPF\";\"Endereço\";\"Qtd Dispositivos\";\"Valor Total (R$)\""))
        assertTrue(customersCsv.contains("\"1\";\"Maria \"\"Lúcia\"\" Silva\";\"11999998888\";\"123.456.789-00\";\"Rua A, 123\";\"1\""))

        assertTrue(devicesCsv.contains("\"ID Dispositivo\";\"ID OS Cliente\";\"Nome Cliente\";\"Modelo\";\"Marca\";\"Nº Série\";\"Descrição\";\"Preço (R$)\";\"Data Entrada\";\"Status\";\"Data Saída\""))
        assertTrue(devicesCsv.contains("\"101\";\"1\";\"Maria \"\"Lúcia\"\" Silva\";\"iPhone 13\";\"Apple\";\"SN001\";\"Tela trincada\""))
        assertTrue(devicesCsv.contains("Pendente"))
    }

    @Test
    fun testSummaryStatsCalculation() {
        val cust1 = Customer(1L, "A", "111", "222", "Address A")
        val cust2 = Customer(2L, "B", "333", "444", "Address B")

        val dev1 = Device(1L, 1L, "M1", "B1", "SN1", "D1", 100.0, isDelivered = false)
        val dev2 = Device(2L, 1L, "M2", "B2", "SN2", "D2", 200.0, isDelivered = true, deliveredAt = 1000L)
        val dev3 = Device(3L, 2L, "M3", "B3", "SN3", "D3", 300.0, isDelivered = false)

        val data = listOf(
            CustomerWithDevices(cust1, listOf(dev1, dev2)),
            CustomerWithDevices(cust2, listOf(dev3))
        )

        val totalCustomers = data.size
        val allDevices = data.flatMap { it.devices }
        val totalDevices = allDevices.size
        val totalValue = allDevices.sumOf { it.price }
        val deliveredCount = allDevices.count { it.isDelivered }
        val pendingCount = allDevices.count { !it.isDelivered }

        assertEquals(2, totalCustomers)
        assertEquals(3, totalDevices)
        assertEquals(600.0, totalValue, 0.001)
        assertEquals(1, deliveredCount)
        assertEquals(2, pendingCount)
    }
}
