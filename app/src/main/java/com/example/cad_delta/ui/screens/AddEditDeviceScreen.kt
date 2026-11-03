package com.example.cad_delta.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import com.example.cad_delta.ui.utils.DateUtils
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
        onIsDeliveredChange = viewModel::onIsDeliveredChange,
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
    onIsDeliveredChange: (Boolean) -> Unit,
    onSaveClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val title = if (deviceId <= 0) "Novo Dispositivo" else "Editar Dispositivo"
    val isEditable = !uiState.isAlreadyDeliveredOnLoad

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
                if (uiState.isAlreadyDeliveredOnLoad || uiState.isDelivered) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (uiState.isAlreadyDeliveredOnLoad) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (uiState.isAlreadyDeliveredOnLoad) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = if (uiState.isAlreadyDeliveredOnLoad) {
                                    "Este aparelho já foi entregue em ${DateUtils.formatDate(uiState.deliveredAt)}. Aparelhos entregues não podem ser editados."
                                } else {
                                    "Aparelho marcado como Entregue (Saída: ${DateUtils.formatDate(uiState.deliveredAt)})."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (uiState.isAlreadyDeliveredOnLoad) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

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
                    enabled = isEditable && !uiState.isDelivered,
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
                    enabled = isEditable && !uiState.isDelivered,
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
                    enabled = isEditable && !uiState.isDelivered,
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
                    enabled = isEditable && !uiState.isDelivered,
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
                    enabled = isEditable && !uiState.isDelivered,
                    singleLine = true
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = isEditable) {
                            onIsDeliveredChange(!uiState.isDelivered)
                        }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = uiState.isDelivered,
                        onCheckedChange = onIsDeliveredChange,
                        enabled = isEditable
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Aparelho Entregue",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.isDelivered && uiState.deliveredAt != null) {
                            Text(
                                text = "Data de Saída: ${DateUtils.formatDate(uiState.deliveredAt)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onSaveClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = isEditable && !uiState.isSaving
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
            onIsDeliveredChange = {},
            onSaveClick = {},
            onNavigateBack = {}
        )
    }
}
