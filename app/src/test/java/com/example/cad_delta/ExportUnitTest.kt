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

    @Test
    fun testJsonImportExactSpecification() {
        val sampleJson = """
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
                    "deliveredAt": 1794101982229,
                    "criado_em": "2026-11-16T20:05:21.855962-03:00",
                    "atualizado_em": "2026-11-16T20:05:21.855978-03:00"
                  }
                ],
                "total_aparelhos": 1,
                "valor_total": "188.00",
                "criado_em": "2026-11-16T20:05:21.848851-03:00",
                "atualizado_em": "2026-11-16T20:05:21.848896-03:00"
              }
            ]
        """.trimIndent()

        val parsedList = ExportUtils.parseJsonImport(sampleJson)
        assertEquals(1, parsedList.size)

        val item = parsedList[0]
        assertEquals("Maiquel Knechtel", item.customer.name)
        assertEquals("51993501980", item.customer.phone)
        assertEquals("01637948042", item.customer.cpf)
        assertEquals("coca Barcelos", item.customer.address)

        assertEquals(1, item.devices.size)
        val device = item.devices[0]
        assertEquals("model 2", device.model)
        assertEquals("Lenovo", device.brand)
        assertEquals("654676", device.serialNumber)
        assertEquals("não liga", device.description)
        assertEquals(188.0, device.price, 0.001)
        assertEquals(1793734167443L, device.createdAt)
        assertEquals(1794101982229L, device.deliveredAt)
        assertTrue(device.isDelivered)
    }

    @Test
    fun testJsonImportSingleObjectRoot() {
        val sampleJsonObject = """
            {
              "id": 5,
              "nome": "Carlos Andrade",
              "telefone": "51988887777",
              "cpf": "12345678900",
              "endereco": "Rua X, 100",
              "aparelhos": [
                {
                  "modelo": "Galaxy Tab",
                  "marca": "Samsung",
                  "numero_serial": "SN999",
                  "descricao": "Troca de bateria",
                  "preco": 350.50,
                  "createdAt": 1700000000000,
                  "deliveredAt": null
                }
              ]
            }
        """.trimIndent()

        val parsedList = ExportUtils.parseJsonImport(sampleJsonObject)
        assertEquals(1, parsedList.size)

        val item = parsedList[0]
        assertEquals("Carlos Andrade", item.customer.name)
        assertEquals(1, item.devices.size)

        val device = item.devices[0]
        assertEquals("Galaxy Tab", device.model)
        assertEquals(350.50, device.price, 0.001)
        assertEquals(null, device.deliveredAt)
        org.junit.Assert.assertFalse(device.isDelivered)
    }

    @Test
    fun testJsonImportExportedFormatFallback() {
        val cust = Customer(10L, "Ana Souza", "11977776666", "11122233344", "Av Central 500")
        val dev = Device(20L, 10L, "MacBook Pro", "Apple", "MAC123", "Limpeza interna", 450.0, isDelivered = false)

        val exportedJson = ExportUtils.generateJsonString(listOf(CustomerWithDevices(cust, listOf(dev))))
        val parsedList = ExportUtils.parseJsonImport(exportedJson)

        assertEquals(1, parsedList.size)
        val item = parsedList[0]
        assertEquals("Ana Souza", item.customer.name)
        assertEquals(1, item.devices.size)
        assertEquals("MacBook Pro", item.devices[0].model)
        assertEquals(450.0, item.devices[0].price, 0.001)
    }

    @Test
    fun testJsonImportResultsWrapperFormat() {
        val resultsWrapperJson = """
            {
              "results": [
                {
                  "id": 1,
                  "nome": "Maiquel Knechtel",
                  "telefone": "51993501980",
                  "cpf": "01637948042",
                  "endereco": "coca Barcelos",
                  "criado_em": "2026-11-16T20:05:21.848851-03:00",
                  "atualizado_em": "2026-11-16T20:05:21.848896-03:00",
                  "total_aparelhos": 1,
                  "valor_total": "188.00",
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
                      "deliveredAt": 1794101982229,
                      "criado_em": "2026-11-16T20:05:21.855962-03:00",
                      "atualizado_em": "2026-11-16T20:05:21.855978-03:00"
                    }
                  ]
                },
                {
                  "id": 2,
                  "nome": "Marcos Benhur",
                  "telefone": "51988887777",
                  "cpf": "09876543210",
                  "endereco": "Av. Ipiranga, 500",
                  "criado_em": "2026-11-16T20:05:21.848851-03:00",
                  "atualizado_em": "2026-11-16T20:05:21.848896-03:00",
                  "total_aparelhos": 1,
                  "valor_total": "250.00",
                  "aparelhos": [
                    {
                      "id": 2,
                      "cliente": 2,
                      "modelo": "iPhone XR",
                      "marca": "Apple",
                      "numero_serial": "SN777888",
                      "descricao": "Troca de tela",
                      "preco": 250.00,
                      "createdAt": 1793734167555,
                      "deliveredAt": null
                    }
                  ]
                },
                {
                  "id": 3,
                  "nome": "Juliana Paes",
                  "telefone": "51977776666",
                  "cpf": "11122233344",
                  "endereco": "Rua das Flores, 12",
                  "total_aparelhos": 2,
                  "valor_total": "400.00",
                  "aparelhos": [
                    {
                      "id": 3,
                      "cliente": 3,
                      "modelo": "Galaxy S20",
                      "marca": "Samsung",
                      "numero_serial": "SAM123456",
                      "descricao": "Conector de carga",
                      "preco": "150.00",
                      "createdAt": 1793734100000,
                      "deliveredAt": 1794101000000
                    },
                    {
                      "id": 4,
                      "cliente": 3,
                      "modelo": "PlayStation 4",
                      "marca": "Sony",
                      "numero_serial": "PS4999",
                      "descricao": "Limpeza e pasta térmica",
                      "preco": "250.00",
                      "createdAt": 1793734200000,
                      "deliveredAt": null
                    }
                  ]
                },
                {
                  "id": 4,
                  "nome": "Fernando Alonso",
                  "telefone": "51966665555",
                  "cpf": "55566677788",
                  "endereco": "Rua Pit Lane, 1",
                  "total_aparelhos": 0,
                  "valor_total": "0.00",
                  "aparelhos": []
                }
              ]
            }
        """.trimIndent()

        val parsedList = ExportUtils.parseJsonImport(resultsWrapperJson)
        assertEquals(4, parsedList.size)

        // Customer 1
        val c1 = parsedList[0]
        assertEquals("Maiquel Knechtel", c1.customer.name)
        assertEquals("51993501980", c1.customer.phone)
        assertEquals("01637948042", c1.customer.cpf)
        assertEquals("coca Barcelos", c1.customer.address)
        assertEquals(1, c1.devices.size)
        val d1 = c1.devices[0]
        assertEquals("model 2", d1.model)
        assertEquals("Lenovo", d1.brand)
        assertEquals("654676", d1.serialNumber)
        assertEquals("não liga", d1.description)
        assertEquals(188.0, d1.price, 0.001)
        assertEquals(1793734167443L, d1.createdAt)
        assertEquals(1794101982229L, d1.deliveredAt)
        assertTrue(d1.isDelivered)

        // Customer 2
        val c2 = parsedList[1]
        assertEquals("Marcos Benhur", c2.customer.name)
        assertEquals("51988887777", c2.customer.phone)
        assertEquals("09876543210", c2.customer.cpf)
        assertEquals("Av. Ipiranga, 500", c2.customer.address)
        assertEquals(1, c2.devices.size)
        val d2 = c2.devices[0]
        assertEquals("iPhone XR", d2.model)
        assertEquals("Apple", d2.brand)
        assertEquals("SN777888", d2.serialNumber)
        assertEquals("Troca de tela", d2.description)
        assertEquals(250.0, d2.price, 0.001)
        assertEquals(1793734167555L, d2.createdAt)
        assertEquals(null, d2.deliveredAt)
        org.junit.Assert.assertFalse(d2.isDelivered)

        // Customer 3
        val c3 = parsedList[2]
        assertEquals("Juliana Paes", c3.customer.name)
        assertEquals("51977776666", c3.customer.phone)
        assertEquals("11122233344", c3.customer.cpf)
        assertEquals("Rua das Flores, 12", c3.customer.address)
        assertEquals(2, c3.devices.size)

        val d3_1 = c3.devices[0]
        assertEquals("Galaxy S20", d3_1.model)
        assertEquals("Samsung", d3_1.brand)
        assertEquals("SAM123456", d3_1.serialNumber)
        assertEquals("Conector de carga", d3_1.description)
        assertEquals(150.0, d3_1.price, 0.001)
        assertEquals(1793734100000L, d3_1.createdAt)
        assertEquals(1794101000000L, d3_1.deliveredAt)
        assertTrue(d3_1.isDelivered)

        val d3_2 = c3.devices[1]
        assertEquals("PlayStation 4", d3_2.model)
        assertEquals("Sony", d3_2.brand)
        assertEquals("PS4999", d3_2.serialNumber)
        assertEquals("Limpeza e pasta térmica", d3_2.description)
        assertEquals(250.0, d3_2.price, 0.001)
        assertEquals(1793734200000L, d3_2.createdAt)
        assertEquals(null, d3_2.deliveredAt)
        org.junit.Assert.assertFalse(d3_2.isDelivered)

        // Customer 4
        val c4 = parsedList[3]
        assertEquals("Fernando Alonso", c4.customer.name)
        assertEquals("51966665555", c4.customer.phone)
        assertEquals("55566677788", c4.customer.cpf)
        assertEquals("Rua Pit Lane, 1", c4.customer.address)
        assertEquals(0, c4.devices.size)
    }

    @Test
    fun testJsonImportMultipleObjectsWithoutBrackets() {
        val multipleObjectsJson = """
            {
              "id": 1,
              "nome": "Maiquel Knechtel",
              "telefone": "51993501980",
              "cpf": "01637948042",
              "endereco": "coca Barcelos",
              "criado_em": "2026-11-16T20:05:21.848851-03:00",
              "atualizado_em": "2026-11-16T20:05:21.848896-03:00",
              "total_aparelhos": 1,
              "valor_total": "188.00",
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
                  "deliveredAt": 1794101982229,
                  "criado_em": "2026-11-16T20:05:21.855962-03:00",
                  "atualizado_em": "2026-11-16T20:05:21.855978-03:00"
                }
              ]
            },
            {
              "id": 2,
              "nome": "Marcos Benhur",
              "telefone": "51988887777",
              "cpf": "09876543210",
              "endereco": "Av. Ipiranga, 500",
              "criado_em": "2026-11-16T20:05:21.848851-03:00",
              "atualizado_em": "2026-11-16T20:05:21.848896-03:00",
              "total_aparelhos": 1,
              "valor_total": "250.00",
              "aparelhos": [
                {
                  "id": 2,
                  "cliente": 2,
                  "modelo": "iPhone XR",
                  "marca": "Apple",
                  "numero_serial": "SN777888",
                  "descricao": "Troca de tela",
                  "preco": 250.00,
                  "createdAt": 1793734167555,
                  "deliveredAt": null
                }
              ]
            },
            {
              "id": 3,
              "nome": "Juliana Paes",
              "telefone": "51977776666",
              "cpf": "11122233344",
              "endereco": "Rua das Flores, 12",
              "total_aparelhos": 2,
              "valor_total": "400.00",
              "aparelhos": [
                {
                  "id": 3,
                  "cliente": 3,
                  "modelo": "Galaxy S20",
                  "marca": "Samsung",
                  "numero_serial": "SAM123456",
                  "descricao": "Conector de carga",
                  "preco": "150.00",
                  "createdAt": 1793734100000,
                  "deliveredAt": 1794101000000
                },
                {
                  "id": 4,
                  "cliente": 3,
                  "modelo": "PlayStation 4",
                  "marca": "Sony",
                  "numero_serial": "PS4999",
                  "descricao": "Limpeza e pasta térmica",
                  "preco": "250.00",
                  "createdAt": 1793734200000,
                  "deliveredAt": null
                }
              ]
            },
            {
              "id": 4,
              "nome": "Fernando Alonso",
              "telefone": "51966665555",
              "cpf": "55566677788",
              "endereco": "Rua Pit Lane, 1",
              "total_aparelhos": 0,
              "valor_total": "0.00",
              "aparelhos": []
            }
        """.trimIndent()

        val parsedList = ExportUtils.parseJsonImport(multipleObjectsJson)
        assertEquals(4, parsedList.size)

        // Customer 1
        val c1 = parsedList[0]
        assertEquals("Maiquel Knechtel", c1.customer.name)
        assertEquals("51993501980", c1.customer.phone)
        assertEquals("01637948042", c1.customer.cpf)
        assertEquals("coca Barcelos", c1.customer.address)
        assertEquals(1, c1.devices.size)
        val d1 = c1.devices[0]
        assertEquals("model 2", d1.model)
        assertEquals("Lenovo", d1.brand)
        assertEquals("654676", d1.serialNumber)
        assertEquals("não liga", d1.description)
        assertEquals(188.0, d1.price, 0.001)
        assertEquals(1793734167443L, d1.createdAt)
        assertEquals(1794101982229L, d1.deliveredAt)
        assertTrue(d1.isDelivered)

        // Customer 2
        val c2 = parsedList[1]
        assertEquals("Marcos Benhur", c2.customer.name)
        assertEquals("51988887777", c2.customer.phone)
        assertEquals("09876543210", c2.customer.cpf)
        assertEquals("Av. Ipiranga, 500", c2.customer.address)
        assertEquals(1, c2.devices.size)
        val d2 = c2.devices[0]
        assertEquals("iPhone XR", d2.model)
        assertEquals("Apple", d2.brand)
        assertEquals("SN777888", d2.serialNumber)
        assertEquals("Troca de tela", d2.description)
        assertEquals(250.0, d2.price, 0.001)
        assertEquals(1793734167555L, d2.createdAt)
        assertEquals(null, d2.deliveredAt)
        org.junit.Assert.assertFalse(d2.isDelivered)

        // Customer 3
        val c3 = parsedList[2]
        assertEquals("Juliana Paes", c3.customer.name)
        assertEquals("51977776666", c3.customer.phone)
        assertEquals("11122233344", c3.customer.cpf)
        assertEquals("Rua das Flores, 12", c3.customer.address)
        assertEquals(2, c3.devices.size)

        val d3_1 = c3.devices[0]
        assertEquals("Galaxy S20", d3_1.model)
        assertEquals("Samsung", d3_1.brand)
        assertEquals("SAM123456", d3_1.serialNumber)
        assertEquals("Conector de carga", d3_1.description)
        assertEquals(150.0, d3_1.price, 0.001)
        assertEquals(1793734100000L, d3_1.createdAt)
        assertEquals(1794101000000L, d3_1.deliveredAt)
        assertTrue(d3_1.isDelivered)

        val d3_2 = c3.devices[1]
        assertEquals("PlayStation 4", d3_2.model)
        assertEquals("Sony", d3_2.brand)
        assertEquals("PS4999", d3_2.serialNumber)
        assertEquals("Limpeza e pasta térmica", d3_2.description)
        assertEquals(250.0, d3_2.price, 0.001)
        assertEquals(1793734200000L, d3_2.createdAt)
        assertEquals(null, d3_2.deliveredAt)
        org.junit.Assert.assertFalse(d3_2.isDelivered)

        // Customer 4
        val c4 = parsedList[3]
        assertEquals("Fernando Alonso", c4.customer.name)
        assertEquals("51966665555", c4.customer.phone)
        assertEquals("55566677788", c4.customer.cpf)
        assertEquals("Rua Pit Lane, 1", c4.customer.address)
        assertEquals(0, c4.devices.size)
    }
}

