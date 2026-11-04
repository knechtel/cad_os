package com.example.cad_delta.ui.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.cad_delta.CustomerWithDevices
import java.io.File

object ExportUtils {

    fun generateJsonString(data: List<CustomerWithDevices>): String {
        val sb = StringBuilder()
        sb.append("[\n")
        for ((cIndex, item) in data.withIndex()) {
            val cust = item.customer
            val devices = item.devices
            val totalValue = devices.sumOf { it.price }

            sb.append("  {\n")
            sb.append("    \"id\": ${cust.id},\n")
            sb.append("    \"name\": \"${escapeJson(cust.name)}\",\n")
            sb.append("    \"phone\": \"${escapeJson(cust.phone)}\",\n")
            sb.append("    \"cpf\": \"${escapeJson(cust.cpf)}\",\n")
            sb.append("    \"address\": \"${escapeJson(cust.address)}\",\n")
            sb.append("    \"devicesCount\": ${devices.size},\n")
            sb.append("    \"totalValue\": $totalValue,\n")
            sb.append("    \"devices\": [\n")

            for ((dIndex, dev) in devices.withIndex()) {
                val deliveredAtVal = if (dev.deliveredAt != null) dev.deliveredAt.toString() else "null"
                val deliveredAtStr = if (dev.deliveredAt != null) "\"${escapeJson(DateUtils.formatDate(dev.deliveredAt))}\"" else "null"

                sb.append("      {\n")
                sb.append("        \"id\": ${dev.id},\n")
                sb.append("        \"customerId\": ${dev.customerId},\n")
                sb.append("        \"model\": \"${escapeJson(dev.model)}\",\n")
                sb.append("        \"brand\": \"${escapeJson(dev.brand)}\",\n")
                sb.append("        \"serialNumber\": \"${escapeJson(dev.serialNumber)}\",\n")
                sb.append("        \"description\": \"${escapeJson(dev.description)}\",\n")
                sb.append("        \"price\": ${dev.price},\n")
                sb.append("        \"createdAt\": ${dev.createdAt},\n")
                sb.append("        \"formattedCreatedAt\": \"${escapeJson(DateUtils.formatDate(dev.createdAt))}\",\n")
                sb.append("        \"isDelivered\": ${dev.isDelivered},\n")
                sb.append("        \"deliveredAt\": $deliveredAtVal,\n")
                sb.append("        \"formattedDeliveredAt\": $deliveredAtStr\n")
                sb.append("      }")
                if (dIndex < devices.size - 1) sb.append(",")
                sb.append("\n")
            }

            sb.append("    ]\n")
            sb.append("  }")
            if (cIndex < data.size - 1) sb.append(",")
            sb.append("\n")
        }
        sb.append("]")
        return sb.toString()
    }

    fun generateJsonFile(context: Context, data: List<CustomerWithDevices>): Uri {
        val jsonString = generateJsonString(data)
        val exportsDir = File(context.cacheDir, "exports").apply { if (!exists()) mkdirs() }
        val file = File(exportsDir, "export_clientes_dispositivos.json")
        file.writeText(jsonString, Charsets.UTF_8)
        val authority = "${context.packageName}.fileprovider"
        return FileProvider.getUriForFile(context, authority, file)
    }

    fun generateCsvStrings(data: List<CustomerWithDevices>): Pair<String, String> {
        val customersCsv = StringBuilder()
        customersCsv.append("\"ID OS\";\"Nome\";\"Telefone\";\"CPF\";\"Endereço\";\"Qtd Dispositivos\";\"Valor Total (R$)\"\n")

        for (item in data) {
            val cust = item.customer
            val totalVal = CurrencyUtils.formatBrl(item.devices.sumOf { it.price })
            customersCsv.append(
                "\"${cust.id}\";\"${escapeCsv(cust.name)}\";\"${escapeCsv(cust.phone)}\";\"${escapeCsv(cust.cpf)}\";\"${escapeCsv(cust.address)}\";\"${item.devices.size}\";\"${escapeCsv(totalVal)}\"\n"
            )
        }

        val devicesCsv = StringBuilder()
        devicesCsv.append("\"ID Dispositivo\";\"ID OS Cliente\";\"Nome Cliente\";\"Modelo\";\"Marca\";\"Nº Série\";\"Descrição\";\"Preço (R$)\";\"Data Entrada\";\"Status\";\"Data Saída\"\n")

        for (item in data) {
            val cust = item.customer
            for (dev in item.devices) {
                val entrada = DateUtils.formatDate(dev.createdAt)
                val status = if (dev.isDelivered) "Entregue" else "Pendente"
                val saida = if (dev.isDelivered && dev.deliveredAt != null) DateUtils.formatDate(dev.deliveredAt) else "-"
                val priceFormatted = CurrencyUtils.formatBrl(dev.price)

                devicesCsv.append(
                    "\"${dev.id}\";\"${cust.id}\";\"${escapeCsv(cust.name)}\";\"${escapeCsv(dev.model)}\";\"${escapeCsv(dev.brand)}\";\"${escapeCsv(dev.serialNumber)}\";\"${escapeCsv(dev.description)}\";\"${escapeCsv(priceFormatted)}\";\"${escapeCsv(entrada)}\";\"${escapeCsv(status)}\";\"${escapeCsv(saida)}\"\n"
                )
            }
        }

        return Pair(customersCsv.toString(), devicesCsv.toString())
    }

    fun generateCsvFiles(context: Context, data: List<CustomerWithDevices>): List<Uri> {
        val (customersCsv, devicesCsv) = generateCsvStrings(data)
        val exportsDir = File(context.cacheDir, "exports").apply { if (!exists()) mkdirs() }

        val customersFile = File(exportsDir, "clientes.csv").apply {
            writeText(customersCsv, Charsets.UTF_8)
        }
        val devicesFile = File(exportsDir, "dispositivos.csv").apply {
            writeText(devicesCsv, Charsets.UTF_8)
        }

        val authority = "${context.packageName}.fileprovider"
        return listOf(
            FileProvider.getUriForFile(context, authority, customersFile),
            FileProvider.getUriForFile(context, authority, devicesFile)
        )
    }

    private fun escapeCsv(value: String): String {
        return value.replace("\"", "\"\"")
    }

    private fun escapeJson(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\b", "\\b")
            .replace("\u000C", "\\f")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}
