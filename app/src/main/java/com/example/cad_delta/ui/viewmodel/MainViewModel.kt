package com.example.cad_delta.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cad_delta.AppRepository
import com.example.cad_delta.Customer
import com.example.cad_delta.CustomerWithDevices
import com.example.cad_delta.Device
import com.example.cad_delta.ui.utils.PdfGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class MainViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _customerToDelete = MutableStateFlow<Customer?>(null)
    val customerToDelete: StateFlow<Customer?> = _customerToDelete.asStateFlow()

    private val _deviceToDelete = MutableStateFlow<Device?>(null)
    val deviceToDelete: StateFlow<Device?> = _deviceToDelete.asStateFlow()

    private val _pdfMessage = MutableStateFlow<String?>(null)
    val pdfMessage: StateFlow<String?> = _pdfMessage.asStateFlow()

    val customersWithDevices: StateFlow<List<CustomerWithDevices>> = combine(
        repository.customersWithDevices,
        _searchQuery
    ) { list, query ->
        if (query.isBlank()) {
            list
        } else {
            val q = query.trim().lowercase(Locale.getDefault())
            val digitsOnly = q.removePrefix("os").removePrefix("#").trim()
            list.filter { item ->
                item.customer.name.lowercase(Locale.getDefault()).contains(q) ||
                        item.customer.phone.lowercase(Locale.getDefault()).contains(q) ||
                        item.customer.cpf.lowercase(Locale.getDefault()).contains(q) ||
                        item.customer.address.lowercase(Locale.getDefault()).contains(q) ||
                        item.customer.id.toString() == q ||
                        ("os #${item.customer.id}".contains(q)) ||
                        ("os:${item.customer.id}".contains(q)) ||
                        (digitsOnly.isNotEmpty() && item.customer.id.toString() == digitsOnly)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onDeleteCustomerClick(customer: Customer) {
        _customerToDelete.value = customer
    }

    fun onDismissDeleteCustomerDialog() {
        _customerToDelete.value = null
    }

    fun onConfirmDeleteCustomer() {
        val customer = _customerToDelete.value ?: return
        viewModelScope.launch {
            repository.deleteCustomer(customer)
            _customerToDelete.value = null
        }
    }

    fun onDeleteDeviceClick(device: Device) {
        _deviceToDelete.value = device
    }

    fun onDismissDeleteDeviceDialog() {
        _deviceToDelete.value = null
    }

    fun onConfirmDeleteDevice() {
        val device = _deviceToDelete.value ?: return
        viewModelScope.launch {
            repository.deleteDevice(device)
            _deviceToDelete.value = null
        }
    }

    fun getCustomerWithDevicesById(customerId: Long): StateFlow<CustomerWithDevices?> {
        return repository.getCustomerWithDevicesById(customerId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    }

    fun generatePdf(customerWithDevices: CustomerWithDevices?) {
        if (customerWithDevices == null) return
        viewModelScope.launch {
            val count = customerWithDevices.devices.size
            val total = customerWithDevices.devices.sumOf { it.price }
            _pdfMessage.value = "PDF gerado para ${customerWithDevices.customer.name} ($count dispositivos, Total: R$ ${String.format(Locale.getDefault(), "%.2f", total)})"
        }
    }

    fun generateAndSharePdf(context: Context, customerWithDevices: CustomerWithDevices?) {
        if (customerWithDevices == null) return
        viewModelScope.launch {
            try {
                val uri = PdfGenerator.generateCustomerReportPdf(context, customerWithDevices)
                sharePdf(context, uri, customerWithDevices.customer.name)
                _pdfMessage.value = "PDF gerado com sucesso para ${customerWithDevices.customer.name}!"
            } catch (e: Exception) {
                _pdfMessage.value = "Erro ao gerar PDF: ${e.message}"
            }
        }
    }

    fun sharePdf(context: Context, uri: Uri, customerName: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Relatório de Cliente - $customerName")
            putExtra(Intent.EXTRA_TEXT, "Segue em anexo o relatório do cliente $customerName.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Compartilhar Relatório PDF")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun clearPdfMessage() {
        _pdfMessage.value = null
    }
}
