package com.example.cad_delta.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cad_delta.ui.theme.Cad_deltaTheme
import com.example.cad_delta.ui.viewmodel.DeviceFormState
import com.example.cad_delta.ui.viewmodel.DeviceFormViewModel

@Composable
fun AddEditDeviceScreen(
    customerId: Long,
    deviceId: Long,
    viewModel: DeviceFormViewModel,
    onNavigateBack: () -> Unit
) {
    LaunchedEffect(customerId, deviceId) {
        viewModel.loadDevice(customerId, deviceId)
    }

    val uiState by viewModel.uiState.collectAsState()

    AddEditDeviceContent(
        deviceId = deviceId,
        uiState = uiState,
        onModelChange = viewModel::onModelChange,
        onBrandChange = viewModel::onBrandChange,
        onSerialNumberChange = viewModel::onSerialNumberChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onPriceChange = viewModel::onPriceChange,
        onSaveClick = { viewModel.saveDevice(customerId, onSuccess = onNavigateBack) },
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDeviceContent(
    deviceId: Long,
    uiState: DeviceFormState,
    onModelChange: (String) -> Unit,
    onBrandChange: (String) -> Unit,
    onSerialNumberChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val title = if (deviceId <= 0) "Novo Dispositivo" else "Editar Dispositivo"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Preencha as informações do dispositivo:",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = uiState.model,
                    onValueChange = onModelChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Modelo *") },
                    placeholder = { Text("Ex: iPhone 13, Galaxy S22") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Smartphone, contentDescription = null)
                    },
                    isError = uiState.modelError != null,
                    supportingText = uiState.modelError?.let { { Text(it) } },
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.brand,
                    onValueChange = onBrandChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Marca *") },
                    placeholder = { Text("Ex: Apple, Samsung, Motorola") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Devices, contentDescription = null)
                    },
                    isError = uiState.brandError != null,
                    supportingText = uiState.brandError?.let { { Text(it) } },
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.serialNumber,
                    onValueChange = onSerialNumberChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Número de Série (Serial) *") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.QrCode, contentDescription = null)
                    },
                    isError = uiState.serialError != null,
                    supportingText = uiState.serialError?.let { { Text(it) } },
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = onDescriptionChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Descrição / Defeito / Observações") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Description, contentDescription = null)
                    },
                    minLines = 3,
                    maxLines = 5
                )

                OutlinedTextField(
                    value = uiState.price,
                    onValueChange = onPriceChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Preço (R$) *") },
                    placeholder = { Text("0.00") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.AttachMoney, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = uiState.priceError != null,
                    supportingText = uiState.priceError?.let { { Text(it) } },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onSaveClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !uiState.isSaving
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = if (deviceId <= 0) "Salvar Dispositivo" else "Atualizar Dispositivo",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddEditDeviceContentPreview() {
    Cad_deltaTheme {
        AddEditDeviceContent(
            deviceId = -1L,
            uiState = DeviceFormState(
                model = "iPhone 13",
                brand = "Apple",
                serialNumber = "SN987654321",
                description = "Troca de tela e bateria",
                price = "1200.00"
            ),
            onModelChange = {},
            onBrandChange = {},
            onSerialNumberChange = {},
            onDescriptionChange = {},
            onPriceChange = {},
            onSaveClick = {},
            onNavigateBack = {}
        )
    }
}
