package com.example.cad_delta.ui.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.cad_delta.CustomerWithDevices
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    fun generateCustomerReportPdf(
        context: Context,
        customerWithDevices: CustomerWithDevices
    ): Uri {
        val pdfDocument = PdfDocument()
        try {
            val customer = customerWithDevices.customer
            val devices = customerWithDevices.devices

            val pageWidth = 595
            val pageHeight = 842
            var pageNumber = 1

            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            val leftMargin = 36f
            val rightMargin = 559f
            val bottomLimit = 770f

            // Paints
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#1565C0")
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#555555")
                textSize = 9.5f
                typeface = Typeface.DEFAULT
            }

            val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#CCCCCC")
                strokeWidth = 1f
            }

            val sectionTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#1565C0")
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#333333")
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#222222")
                textSize = 10f
                typeface = Typeface.DEFAULT
            }

            val cardBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#F4F6F9")
                style = Paint.Style.FILL
            }

            val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#D0D7DE")
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }

            val tableHeaderBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#1565C0")
                style = Paint.Style.FILL
            }

            val tableHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val tableRowBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FAFAFA")
                style = Paint.Style.FILL
            }

            val tableCellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#333333")
                textSize = 9.5f
                typeface = Typeface.DEFAULT
            }

            val tableBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#E0E0E0")
                style = Paint.Style.STROKE
                strokeWidth = 0.8f
            }

            val summaryBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#E3F2FD")
                style = Paint.Style.FILL
            }

            val summaryTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#0D47A1")
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val footerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#888888")
                textSize = 8.5f
                typeface = Typeface.DEFAULT
            }

            var y = 105f

            // --- Header ---
            // Logo oficial obtido das diretrizes de marca da Delta:
            // https://news.delta.com/delta-air-lines-logos-and-brand-guidelines
            val deltaLogo = BitmapFactory.decodeResource(context.resources, com.example.cad_delta.R.drawable.delta_logo)
            val logoBounds = RectF(leftMargin, 29f, leftMargin + 112f, 47f)
            canvas.drawBitmap(deltaLogo, null, logoBounds, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
            canvas.drawText("Rua Fernandes Bastos, 1978", leftMargin + 128f, 35f, subtitlePaint)
            canvas.drawText("Telefone: 51-9350-1980", leftMargin + 128f, 50f, subtitlePaint)
            canvas.drawText("Relatório de Cliente e Aparelhos", leftMargin, 76f, titlePaint)
            val currentDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            val dateText = "Data: $currentDate"
            val dateWidth = subtitlePaint.measureText(dateText)
            canvas.drawText(dateText, rightMargin - dateWidth, 76f, subtitlePaint)

            canvas.drawLine(leftMargin, 86f, rightMargin, 86f, linePaint)

            // --- Section 1: Customer Details ---
            canvas.drawText("1. Dados do Cliente - Nº da OS: #${customer.id}", leftMargin, y, sectionTitlePaint)
            y += 12f

            val cardTop = y
            val cardHeight = if (customer.address.isNotBlank()) 75f else 60f
            val cardRect = RectF(leftMargin, cardTop, rightMargin, cardTop + cardHeight)
            canvas.drawRoundRect(cardRect, 6f, 6f, cardBgPaint)
            canvas.drawRoundRect(cardRect, 6f, 6f, cardBorderPaint)

            var custY = cardTop + 20f
            canvas.drawText("Nº da OS:", leftMargin + 12f, custY, labelPaint)
            canvas.drawText("OS #${customer.id}", leftMargin + 65f, custY, valuePaint)

            canvas.drawText("Nome:", leftMargin + 180f, custY, labelPaint)
            canvas.drawText(truncateText(customer.name, valuePaint, 240f), leftMargin + 220f, custY, valuePaint)

            custY += 18f
            canvas.drawText("CPF:", leftMargin + 12f, custY, labelPaint)
            canvas.drawText(customer.cpf, leftMargin + 45f, custY, valuePaint)

            canvas.drawText("Telefone:", leftMargin + 180f, custY, labelPaint)
            canvas.drawText(customer.phone, leftMargin + 235f, custY, valuePaint)

            if (customer.address.isNotBlank()) {
                custY += 18f
                canvas.drawText("Endereço:", leftMargin + 12f, custY, labelPaint)
                canvas.drawText(truncateText(customer.address, valuePaint, 430f), leftMargin + 70f, custY, valuePaint)
            }

            y += cardHeight + 25f

            // --- Section 2: Devices Table ---
            canvas.drawText("2. Aparelhos / Dispositivos (${devices.size})", leftMargin, y, sectionTitlePaint)
            y += 12f

            fun drawTableHeader(c: Canvas, topY: Float) {
                val headerRect = RectF(leftMargin, topY, rightMargin, topY + 22f)
                c.drawRect(headerRect, tableHeaderBgPaint)
                val headerTextY = topY + 15f
                c.drawText("Modelo", leftMargin + 4f, headerTextY, tableHeaderPaint)
                c.drawText("Marca", leftMargin + 72f, headerTextY, tableHeaderPaint)
                c.drawText("Nº Série", leftMargin + 124f, headerTextY, tableHeaderPaint)
                c.drawText("Entrada", leftMargin + 178f, headerTextY, tableHeaderPaint)
                c.drawText("Data Saída", leftMargin + 258f, headerTextY, tableHeaderPaint)
                c.drawText("Descrição", leftMargin + 346f, headerTextY, tableHeaderPaint)
                val priceHeader = "Preço (R$)"
                val priceWidth = tableHeaderPaint.measureText(priceHeader)
                c.drawText(priceHeader, rightMargin - 4f - priceWidth, headerTextY, tableHeaderPaint)
            }

            drawTableHeader(canvas, y)
            y += 22f

            if (devices.isEmpty()) {
                val emptyRect = RectF(leftMargin, y, rightMargin, y + 30f)
                canvas.drawRect(emptyRect, tableRowBgPaint)
                canvas.drawRect(emptyRect, tableBorderPaint)
                canvas.drawText("Nenhum dispositivo cadastrado para este cliente.", leftMargin + 12f, y + 19f, tableCellPaint)
                y += 30f
            } else {
                for ((index, device) in devices.withIndex()) {
                    val rowHeight = 22f
                    if (y + rowHeight > bottomLimit) {
                        drawFooter(canvas, pageNumber, footerTextPaint, leftMargin, rightMargin)
                        pdfDocument.finishPage(page)
                        pageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        y = 45f
                        canvas.drawText("Relatório de Cliente - Continuação", leftMargin, y, titlePaint)
                        y += 15f
                        canvas.drawLine(leftMargin, y, rightMargin, y, linePaint)
                        y += 20f
                        drawTableHeader(canvas, y)
                        y += 22f
                    }

                    val rowRect = RectF(leftMargin, y, rightMargin, y + rowHeight)
                    if (index % 2 == 1) {
                        canvas.drawRect(rowRect, tableRowBgPaint)
                    }
                    canvas.drawRect(rowRect, tableBorderPaint)

                    val cellTextY = y + 15f
                    canvas.drawText(truncateText(device.model, tableCellPaint, 64f), leftMargin + 4f, cellTextY, tableCellPaint)
                    canvas.drawText(truncateText(device.brand, tableCellPaint, 48f), leftMargin + 72f, cellTextY, tableCellPaint)
                    canvas.drawText(truncateText(device.serialNumber, tableCellPaint, 50f), leftMargin + 124f, cellTextY, tableCellPaint)

                    val entradaStr = if (device.createdAt > 0L) DateUtils.formatDate(device.createdAt) else "-"
                    canvas.drawText(truncateText(entradaStr, tableCellPaint, 76f), leftMargin + 178f, cellTextY, tableCellPaint)

                    val saidaStr = if (device.isDelivered && device.deliveredAt != null && device.deliveredAt > 0L) {
                        DateUtils.formatDate(device.deliveredAt)
                    } else {
                        "Pendente"
                    }
                    canvas.drawText(truncateText(saidaStr, tableCellPaint, 84f), leftMargin + 258f, cellTextY, tableCellPaint)

                    canvas.drawText(truncateText(device.description, tableCellPaint, 96f), leftMargin + 346f, cellTextY, tableCellPaint)

                    val formattedPrice = CurrencyUtils.formatBrl(device.price)
                    val priceW = tableCellPaint.measureText(formattedPrice)
                    canvas.drawText(formattedPrice, rightMargin - 4f - priceW, cellTextY, tableCellPaint)

                    y += rowHeight
                }
            }

            y += 20f

            // --- Footer / Summary Box ---
            if (y + 50f > bottomLimit) {
                drawFooter(canvas, pageNumber, footerTextPaint, leftMargin, rightMargin)
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 45f
            }

            val totalDevices = devices.size
            val totalPrice = devices.sumOf { it.price }
            val summaryRect = RectF(leftMargin, y, rightMargin, y + 45f)
            canvas.drawRoundRect(summaryRect, 6f, 6f, summaryBgPaint)
            val summaryBorderPaint = Paint(cardBorderPaint).apply {
                color = Color.parseColor("#90CAF9")
            }
            canvas.drawRoundRect(summaryRect, 6f, 6f, summaryBorderPaint)

            val sumY = y + 26f
            canvas.drawText("Total de Aparelhos: $totalDevices", leftMargin + 16f, sumY, summaryTextPaint)
            val totalFormatted = "Valor Total: ${CurrencyUtils.formatBrl(totalPrice)}"
            val totalW = summaryTextPaint.measureText(totalFormatted)
            canvas.drawText(totalFormatted, rightMargin - 16f - totalW, sumY, summaryTextPaint)

            drawFooter(canvas, pageNumber, footerTextPaint, leftMargin, rightMargin)

            pdfDocument.finishPage(page)

            val reportsDir = File(context.cacheDir, "reports")
            if (!reportsDir.exists()) {
                reportsDir.mkdirs()
            }
            val pdfFile = File(reportsDir, "relatorio_cliente_${customer.id}.pdf")
            pdfFile.outputStream().use { out ->
                pdfDocument.writeTo(out)
            }

            val authority = "${context.packageName}.fileprovider"
            return FileProvider.getUriForFile(context, authority, pdfFile)
        } finally {
            pdfDocument.close()
        }
    }

    fun generateGeneralReportPdf(
        context: Context,
        customersWithDevices: List<CustomerWithDevices>
    ): Uri {
        val pdfDocument = PdfDocument()
        try {
            val pageWidth = 595
            val pageHeight = 842
            var pageNumber = 1

            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            val leftMargin = 36f
            val rightMargin = 559f
            val bottomLimit = 770f

            // Paints
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#1565C0")
                textSize = 17f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#555555")
                textSize = 9.5f
                typeface = Typeface.DEFAULT
            }

            val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#CCCCCC")
                strokeWidth = 1f
            }

            val sectionTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#1565C0")
                textSize = 11.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#222222")
                textSize = 9.5f
                typeface = Typeface.DEFAULT
            }

            val cardBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#F4F6F9")
                style = Paint.Style.FILL
            }

            val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#D0D7DE")
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }

            val tableHeaderBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#1565C0")
                style = Paint.Style.FILL
            }

            val tableHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val tableRowBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FAFAFA")
                style = Paint.Style.FILL
            }

            val tableCellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#333333")
                textSize = 9f
                typeface = Typeface.DEFAULT
            }

            val tableBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#E0E0E0")
                style = Paint.Style.STROKE
                strokeWidth = 0.8f
            }

            val summaryBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#E3F2FD")
                style = Paint.Style.FILL
            }

            val summaryTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#0D47A1")
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val footerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#888888")
                textSize = 8.5f
                typeface = Typeface.DEFAULT
            }

            var y = 105f

            // --- Header ---
            val deltaLogo = BitmapFactory.decodeResource(context.resources, com.example.cad_delta.R.drawable.delta_logo)
            val logoBounds = RectF(leftMargin, 29f, leftMargin + 112f, 47f)
            canvas.drawBitmap(deltaLogo, null, logoBounds, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
            canvas.drawText("Rua Fernandes Bastos, 1978", leftMargin + 128f, 35f, subtitlePaint)
            canvas.drawText("Telefone: 51-9350-1980", leftMargin + 128f, 50f, subtitlePaint)
            canvas.drawText("Relatório Geral de Clientes e Aparelhos", leftMargin, 76f, titlePaint)
            val currentDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            val dateText = "Data: $currentDate"
            val dateWidth = subtitlePaint.measureText(dateText)
            canvas.drawText(dateText, rightMargin - dateWidth, 76f, subtitlePaint)

            canvas.drawLine(leftMargin, 86f, rightMargin, 86f, linePaint)

            // --- Consolidated Summary Box ---
            val totalCustomers = customersWithDevices.size
            val allDevices = customersWithDevices.flatMap { it.devices }
            val totalDevices = allDevices.size
            val deliveredDevices = allDevices.count { it.isDelivered }
            val pendingDevices = allDevices.count { !it.isDelivered }
            val totalPrice = allDevices.sumOf { it.price }

            val summaryHeight = 40f
            val summaryRect = RectF(leftMargin, y, rightMargin, y + summaryHeight)
            canvas.drawRoundRect(summaryRect, 6f, 6f, summaryBgPaint)
            val summaryBorderPaint = Paint(cardBorderPaint).apply {
                color = Color.parseColor("#90CAF9")
            }
            canvas.drawRoundRect(summaryRect, 6f, 6f, summaryBorderPaint)

            val sumY = y + 16f
            canvas.drawText("Total Clientes: $totalCustomers | Total Aparelhos: $totalDevices ($deliveredDevices Entregues / $pendingDevices Pendentes)", leftMargin + 12f, sumY, summaryTextPaint)
            val totalFormatted = "Valor Geral: ${CurrencyUtils.formatBrl(totalPrice)}"
            val totalW = summaryTextPaint.measureText(totalFormatted)
            canvas.drawText(totalFormatted, rightMargin - 12f - totalW, sumY + 14f, summaryTextPaint)

            y += summaryHeight + 16f

            fun drawTableHeader(c: Canvas, topY: Float) {
                val headerRect = RectF(leftMargin, topY, rightMargin, topY + 22f)
                c.drawRect(headerRect, tableHeaderBgPaint)
                val headerTextY = topY + 15f
                c.drawText("Modelo", leftMargin + 4f, headerTextY, tableHeaderPaint)
                c.drawText("Marca", leftMargin + 72f, headerTextY, tableHeaderPaint)
                c.drawText("Nº Série", leftMargin + 124f, headerTextY, tableHeaderPaint)
                c.drawText("Entrada", leftMargin + 178f, headerTextY, tableHeaderPaint)
                c.drawText("Status / Saída", leftMargin + 258f, headerTextY, tableHeaderPaint)
                c.drawText("Descrição", leftMargin + 346f, headerTextY, tableHeaderPaint)
                val priceHeader = "Preço (R$)"
                val priceWidth = tableHeaderPaint.measureText(priceHeader)
                c.drawText(priceHeader, rightMargin - 4f - priceWidth, headerTextY, tableHeaderPaint)
            }

            if (customersWithDevices.isEmpty()) {
                canvas.drawText("Nenhum cliente cadastrado no sistema.", leftMargin, y, sectionTitlePaint)
            } else {
                for (item in customersWithDevices) {
                    val customer = item.customer
                    val devices = item.devices

                    if (y + 90f > bottomLimit) {
                        drawFooter(canvas, pageNumber, footerTextPaint, leftMargin, rightMargin)
                        pdfDocument.finishPage(page)
                        pageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        y = 45f
                    }

                    // Customer Subheader Card
                    canvas.drawText("Cliente OS #${customer.id}: ${customer.name}", leftMargin, y, sectionTitlePaint)
                    y += 10f

                    val cardTop = y
                    val cardHeight = 36f
                    val cardRect = RectF(leftMargin, cardTop, rightMargin, cardTop + cardHeight)
                    canvas.drawRoundRect(cardRect, 4f, 4f, cardBgPaint)
                    canvas.drawRoundRect(cardRect, 4f, 4f, cardBorderPaint)

                    val custY = cardTop + 15f
                    canvas.drawText("CPF: ${customer.cpf}", leftMargin + 10f, custY, valuePaint)
                    canvas.drawText("Tel: ${customer.phone}", leftMargin + 150f, custY, valuePaint)
                    if (customer.address.isNotBlank()) {
                        canvas.drawText("Endereço: ${truncateText(customer.address, valuePaint, 220f)}", leftMargin + 270f, custY, valuePaint)
                    }

                    y += cardHeight + 8f

                    drawTableHeader(canvas, y)
                    y += 22f

                    if (devices.isEmpty()) {
                        val emptyRect = RectF(leftMargin, y, rightMargin, y + 20f)
                        canvas.drawRect(emptyRect, tableRowBgPaint)
                        canvas.drawRect(emptyRect, tableBorderPaint)
                        canvas.drawText("Nenhum aparelho cadastrado para este cliente.", leftMargin + 8f, y + 14f, tableCellPaint)
                        y += 20f
                    } else {
                        for ((index, device) in devices.withIndex()) {
                            val rowHeight = 22f
                            if (y + rowHeight > bottomLimit) {
                                drawFooter(canvas, pageNumber, footerTextPaint, leftMargin, rightMargin)
                                pdfDocument.finishPage(page)
                                pageNumber++
                                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                                page = pdfDocument.startPage(pageInfo)
                                canvas = page.canvas
                                y = 45f
                                canvas.drawText("Relatório Geral - Continuação (${customer.name})", leftMargin, y, titlePaint)
                                y += 15f
                                canvas.drawLine(leftMargin, y, rightMargin, y, linePaint)
                                y += 20f
                                drawTableHeader(canvas, y)
                                y += 22f
                            }

                            val rowRect = RectF(leftMargin, y, rightMargin, y + rowHeight)
                            if (index % 2 == 1) {
                                canvas.drawRect(rowRect, tableRowBgPaint)
                            }
                            canvas.drawRect(rowRect, tableBorderPaint)

                            val cellTextY = y + 15f
                            canvas.drawText(truncateText(device.model, tableCellPaint, 64f), leftMargin + 4f, cellTextY, tableCellPaint)
                            canvas.drawText(truncateText(device.brand, tableCellPaint, 48f), leftMargin + 72f, cellTextY, tableCellPaint)
                            canvas.drawText(truncateText(device.serialNumber, tableCellPaint, 50f), leftMargin + 124f, cellTextY, tableCellPaint)

                            val entradaStr = if (device.createdAt > 0L) DateUtils.formatDate(device.createdAt) else "-"
                            canvas.drawText(truncateText(entradaStr, tableCellPaint, 76f), leftMargin + 178f, cellTextY, tableCellPaint)

                            val statusStr = if (device.isDelivered && device.deliveredAt != null && device.deliveredAt > 0L) {
                                "Entregue (${DateUtils.formatDateOnly(device.deliveredAt)})"
                            } else {
                                "Pendente"
                            }
                            canvas.drawText(truncateText(statusStr, tableCellPaint, 84f), leftMargin + 258f, cellTextY, tableCellPaint)

                            canvas.drawText(truncateText(device.description, tableCellPaint, 96f), leftMargin + 346f, cellTextY, tableCellPaint)

                            val formattedPrice = CurrencyUtils.formatBrl(device.price)
                            val priceW = tableCellPaint.measureText(formattedPrice)
                            canvas.drawText(formattedPrice, rightMargin - 4f - priceW, cellTextY, tableCellPaint)

                            y += rowHeight
                        }
                    }

                    y += 14f
                }
            }

            drawFooter(canvas, pageNumber, footerTextPaint, leftMargin, rightMargin)
            pdfDocument.finishPage(page)

            val exportsDir = File(context.cacheDir, "exports")
            if (!exportsDir.exists()) {
                exportsDir.mkdirs()
            }
            val pdfFile = File(exportsDir, "relatorio_geral_cad_delta.pdf")
            pdfFile.outputStream().use { out ->
                pdfDocument.writeTo(out)
            }

            val authority = "${context.packageName}.fileprovider"
            return FileProvider.getUriForFile(context, authority, pdfFile)
        } finally {
            pdfDocument.close()
        }
    }

    private fun drawFooter(
        canvas: Canvas,
        pageNumber: Int,
        paint: Paint,
        leftMargin: Float,
        rightMargin: Float
    ) {
        val footerY = 815f
        canvas.drawLine(leftMargin, footerY - 10f, rightMargin, footerY - 10f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E0E0E0")
            strokeWidth = 0.5f
        })
        canvas.drawText("Gerado por Cad Delta", leftMargin, footerY, paint)
        val pageText = "Página $pageNumber"
        val pageW = paint.measureText(pageText)
        canvas.drawText(pageText, rightMargin - pageW, footerY, paint)
    }

    private fun truncateText(text: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        var len = text.length
        while (len > 0) {
            val truncated = text.substring(0, len) + "..."
            if (paint.measureText(truncated) <= maxWidth) return truncated
            len--
        }
        return text
    }
}
