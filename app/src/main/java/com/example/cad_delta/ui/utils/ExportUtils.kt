package com.example.cad_delta.ui.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.cad_delta.Customer
import com.example.cad_delta.CustomerWithDevices
import com.example.cad_delta.Device
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.io.File

object ExportUtils {

    fun parseJsonImport(jsonString: String): List<CustomerWithDevices> {
        val trimmed = jsonString.trim()
        if (trimmed.isEmpty()) return emptyList()

        val jsonArray: JSONArray = try {
            if (trimmed.startsWith("[")) {
                JSONArray(trimmed)
            } else if (trimmed.startsWith("{")) {
                val tokener = JSONTokener(trimmed)
                val firstVal = tokener.nextValue()
                val nextChar = if (tokener.more()) tokener.nextClean() else '\u0000'
                if (nextChar == '\u0000' && firstVal is JSONObject) {
                    if (firstVal.has("results") && !firstVal.isNull("results") && firstVal.optJSONArray("results") != null) {
                        firstVal.getJSONArray("results")
                    } else {
                        JSONArray().apply { put(firstVal) }
                    }
                } else {
                    JSONArray(wrapInArray(trimmed))
                }
            } else {
                JSONArray(wrapInArray(trimmed))
            }
        } catch (_: Exception) {
            try {
                JSONArray(wrapInArray(trimmed))
            } catch (e2: Exception) {
                throw IllegalArgumentException("Formato JSON inválido. O conteúdo deve ser um objeto ou um array.", e2)
            }
        }

        val result = mutableListOf<CustomerWithDevices>()

        for (i in 0 until jsonArray.length()) {
            val custObj = jsonArray.optJSONObject(i) ?: continue

            val name = getStringField(custObj, "nome", "name")
            val phone = getStringField(custObj, "telefone", "phone")
            val cpf = getStringField(custObj, "cpf")
            val address = getStringField(custObj, "endereco", "address")

            val customer = Customer(
                id = 0,
                name = name,
                phone = phone,
                cpf = cpf,
                address = address
            )

            val devicesArray = custObj.optJSONArray("aparelhos") ?: custObj.optJSONArray("devices")
            val devices = mutableListOf<Device>()

            if (devicesArray != null) {
                for (j in 0 until devicesArray.length()) {
                    val devObj = devicesArray.optJSONObject(j) ?: continue

                    val model = getStringField(devObj, "modelo", "model")
                    val brand = getStringField(devObj, "marca", "brand")
                    val serialNumber = getStringField(devObj, "numero_serial", "serialNumber")
                    val description = getStringField(devObj, "descricao", "description")
                    val price = parsePrice(devObj, "preco", "price")
                    val createdAt = parseLong(devObj, "createdAt", "created_at")
                        .takeIf { it != null && it > 0L } ?: System.currentTimeMillis()

                    val deliveredAtVal = parseLong(devObj, "deliveredAt", "delivered_at", "entregue_em")
                    val (isDelivered, finalDeliveredAt) = if (deliveredAtVal != null && deliveredAtVal > 0L) {
                        Pair(true, deliveredAtVal)
                    } else {
                        val isDeliv = devObj.optBoolean("isDelivered", false)
                        Pair(isDeliv, if (isDeliv && deliveredAtVal != null && deliveredAtVal > 0L) deliveredAtVal else null)
                    }

                    val device = Device(
                        id = 0,
                        customerId = 0,
                        model = model,
                        brand = brand,
                        serialNumber = serialNumber,
                        description = description,
                        price = price,
                        createdAt = createdAt,
                        isDelivered = isDelivered,
                        deliveredAt = finalDeliveredAt
                    )
                    devices.add(device)
                }
            }

            result.add(CustomerWithDevices(customer, devices))
        }

        return result
    }

    private fun getStringField(obj: JSONObject, vararg keys: String): String {
        for (key in keys) {
            if (obj.has(key) && !obj.isNull(key)) {
                val str = obj.optString(key, "")
                if (str.isNotBlank() && str != "null") {
                    return str
                }
            }
        }
        return ""
    }

    private fun parsePrice(obj: JSONObject, vararg keys: String): Double {
        for (key in keys) {
            if (obj.has(key) && !obj.isNull(key)) {
                val raw = obj.opt(key)
                if (raw is Number) {
                    return raw.toDouble()
                } else if (raw is String) {
                    val clean = raw.replace("R$", "").replace(" ", "").replace(",", ".")
                    val parsed = clean.toDoubleOrNull()
                    if (parsed != null) return parsed
                }
            }
        }
        return 0.0
    }

    private fun parseLong(obj: JSONObject, vararg keys: String): Long? {
        for (key in keys) {
            if (obj.has(key) && !obj.isNull(key)) {
                val raw = obj.opt(key)
                if (raw is Number) {
                    return raw.toLong()
                } else if (raw is String) {
                    val parsed = raw.toLongOrNull()
                    if (parsed != null) return parsed
                }
            }
        }
        return null
    }

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

    private fun wrapInArray(s: String): String {
        var str = s.trim()
        if (str.endsWith(",")) {
            str = str.dropLast(1).trim()
        }
        return "[$str]"
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

