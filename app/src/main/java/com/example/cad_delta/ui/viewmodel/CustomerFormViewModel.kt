package com.example.cad_delta.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cad_delta.AppRepository
import com.example.cad_delta.Customer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class CustomerFormState(
    val id: Long = 0,
    val name: String = "",
    val phone: String = "",
    val cpf: String = "",
    val address: String = "",
    val nameError: String? = null,
    val phoneError: String? = null,
    val cpfError: String? = null,
    val addressError: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false
)

class CustomerFormViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerFormState())
    val uiState: StateFlow<CustomerFormState> = _uiState.asStateFlow()

    fun loadCustomer(customerId: Long) {
        if (customerId <= 0) {
            _uiState.value = CustomerFormState()
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val customer = repository.getCustomerById(customerId).firstOrNull()
            if (customer != null) {
                _uiState.value = CustomerFormState(
                    id = customer.id,
                    name = customer.name,
                    phone = customer.phone,
                    cpf = customer.cpf,
                    address = customer.address,
                    isLoading = false
                )
            } else {
                _uiState.value = CustomerFormState(isLoading = false)
            }
        }
    }

    fun onNameChange(value: String) {
        _uiState.value = _uiState.value.copy(
            name = value,
            nameError = if (value.isNotBlank()) null else _uiState.value.nameError
        )
    }

    fun onPhoneChange(value: String) {
        _uiState.value = _uiState.value.copy(
            phone = value,
            phoneError = if (value.isNotBlank()) null else _uiState.value.phoneError
        )
    }

    fun onCpfChange(value: String) {
        _uiState.value = _uiState.value.copy(
            cpf = value,
            cpfError = if (value.isNotBlank()) null else _uiState.value.cpfError
        )
    }

    fun onAddressChange(value: String) {
        _uiState.value = _uiState.value.copy(
            address = value,
            addressError = if (value.isNotBlank()) null else _uiState.value.addressError
        )
    }

    fun saveCustomer(onSuccess: () -> Unit) {
        val currentState = _uiState.value

        var hasError = false
        val nameErr = if (currentState.name.isBlank()) {
            hasError = true
            "Nome é obrigatório"
        } else null

        val phoneErr = if (currentState.phone.isBlank()) {
            hasError = true
            "Telefone é obrigatório"
        } else null

        val cpfErr = if (currentState.cpf.isBlank()) {
            hasError = true
            "CPF é obrigatório"
        } else null

        val addressErr = if (currentState.address.isBlank()) {
            hasError = true
            "Endereço é obrigatório"
        } else null

        if (hasError) {
            _uiState.value = currentState.copy(
                nameError = nameErr,
                phoneError = phoneErr,
                cpfError = cpfErr,
                addressError = addressErr
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isSaving = true)
            val customer = Customer(
                id = currentState.id,
                name = currentState.name.trim(),
                phone = currentState.phone.trim(),
                cpf = currentState.cpf.trim(),
                address = currentState.address.trim()
            )
            if (customer.id <= 0) {
                repository.insertCustomer(customer)
            } else {
                repository.updateCustomer(customer)
            }
            _uiState.value = _uiState.value.copy(isSaving = false)
            onSuccess()
        }
    }
}
