package com.example.cad_delta.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cad_delta.AppRepository
import com.example.cad_delta.Device
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class DeviceFormState(
    val id: Long = 0,
    val customerId: Long = 0,
    val model: String = "",
    val brand: String = "",
    val serialNumber: String = "",
    val description: String = "",
    val price: String = "",
    val createdAt: Long = 0L,
    val modelError: String? = null,
    val brandError: String? = null,
    val serialError: String? = null,
    val priceError: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false
)

class DeviceFormViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeviceFormState())
    val uiState: StateFlow<DeviceFormState> = _uiState.asStateFlow()

    fun loadDevice(customerId: Long, deviceId: Long) {
        if (deviceId <= 0) {
            _uiState.value = DeviceFormState(customerId = customerId)
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, customerId = customerId)
            val device = repository.getDeviceById(deviceId).firstOrNull()
            if (device != null) {
                _uiState.value = DeviceFormState(
                    id = device.id,
                    customerId = device.customerId,
                    model = device.model,
                    brand = device.brand,
                    serialNumber = device.serialNumber,
                    description = device.description,
                    price = device.price.toString(),
                    createdAt = device.createdAt,
                    isLoading = false
                )
            } else {
                _uiState.value = DeviceFormState(customerId = customerId, isLoading = false)
            }
        }
    }

    fun onModelChange(value: String) {
        _uiState.value = _uiState.value.copy(
            model = value,
            modelError = if (value.isNotBlank()) null else _uiState.value.modelError
        )
    }

    fun onBrandChange(value: String) {
        _uiState.value = _uiState.value.copy(
            brand = value,
            brandError = if (value.isNotBlank()) null else _uiState.value.brandError
        )
    }

    fun onSerialNumberChange(value: String) {
        _uiState.value = _uiState.value.copy(
            serialNumber = value,
            serialError = if (value.isNotBlank()) null else _uiState.value.serialError
        )
    }

    fun onDescriptionChange(value: String) {
        _uiState.value = _uiState.value.copy(description = value)
    }

    fun onPriceChange(value: String) {
        _uiState.value = _uiState.value.copy(
            price = value,
            priceError = if (value.isNotBlank()) null else _uiState.value.priceError
        )
    }

    fun saveDevice(customerId: Long, onSuccess: () -> Unit) {
        val currentState = _uiState.value

        var hasError = false
        val modelErr = if (currentState.model.isBlank()) {
            hasError = true
            "Modelo é obrigatório"
        } else null

        val brandErr = if (currentState.brand.isBlank()) {
            hasError = true
            "Marca é obrigatória"
        } else null

        val serialErr = if (currentState.serialNumber.isBlank()) {
            hasError = true
            "Número de série é obrigatório"
        } else null

        val parsedPrice = currentState.price.replace(",", ".").toDoubleOrNull()
        val priceErr = if (currentState.price.isBlank()) {
            hasError = true
            "Preço é obrigatório"
        } else if (parsedPrice == null || parsedPrice < 0) {
            hasError = true
            "Informe um valor numérico válido"
        } else null

        if (hasError) {
            _uiState.value = currentState.copy(
                modelError = modelErr,
                brandError = brandErr,
                serialError = serialErr,
                priceError = priceErr
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isSaving = true)
            val targetCreatedAt = if (currentState.id <= 0) {
                System.currentTimeMillis()
            } else {
                if (currentState.createdAt > 0L) currentState.createdAt else System.currentTimeMillis()
            }
            val device = Device(
                id = currentState.id,
                customerId = if (currentState.customerId > 0) currentState.customerId else customerId,
                model = currentState.model.trim(),
                brand = currentState.brand.trim(),
                serialNumber = currentState.serialNumber.trim(),
                description = currentState.description.trim(),
                price = parsedPrice ?: 0.0,
                createdAt = targetCreatedAt
            )
            if (device.id <= 0) {
                repository.insertDevice(device)
            } else {
                repository.updateDevice(device)
            }
            _uiState.value = _uiState.value.copy(isSaving = false)
            onSuccess()
        }
    }
}
