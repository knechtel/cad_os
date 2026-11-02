package com.example.cad_delta.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cad_delta.Customer
import com.example.cad_delta.CustomerWithDevices
import com.example.cad_delta.Device
import com.example.cad_delta.ui.theme.Cad_deltaTheme
import com.example.cad_delta.ui.utils.CurrencyUtils
import com.example.cad_delta.ui.utils.DateUtils
import com.example.cad_delta.ui.viewmodel.MainViewModel

@Composable
fun CustomerDetailScreen(
    customerId: Long,
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEditCustomer: (Long) -> Unit,
    onNavigateToAddDevice: (Long) -> Unit,
    onNavigateToEditDevice: (Long, Long) -> Unit
) {
    val context = LocalContext.current
    val customerWithDevicesState = remember(customerId) {
        viewModel.getCustomerWithDevicesById(customerId)
    }
    val customerWithDevices by customerWithDevicesState.collectAsState()
    val deviceToDelete by viewModel.deviceToDelete.collectAsState()
    val pdfMessage by viewModel.pdfMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(pdfMessage) {
        pdfMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearPdfMessage()
        }
    }

    CustomerDetailContent(
        customerWithDevices = customerWithDevices,
        deviceToDelete = deviceToDelete,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onEditCustomerClick = { onNavigateToEditCustomer(customerId) },
        onAddDeviceClick = { onNavigateToAddDevice(customerId) },
        onEditDeviceClick = { deviceId -> onNavigateToEditDevice(customerId, deviceId) },
        onDeleteDeviceClick = viewModel::onDeleteDeviceClick,
        onConfirmDeleteDevice = viewModel::onConfirmDeleteDevice,
        onDismissDeleteDeviceDialog = viewModel::onDismissDeleteDeviceDialog,
        onGeneratePdfClick = { viewModel.generateAndSharePdf(context, customerWithDevices) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailContent(
    customerWithDevices: CustomerWithDevices?,
    deviceToDelete: Device?,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onEditCustomerClick: () -> Unit,
    onAddDeviceClick: () -> Unit,
    onEditDeviceClick: (Long) -> Unit,
    onDeleteDeviceClick: (Device) -> Unit,
    onConfirmDeleteDevice: () -> Unit,
    onDismissDeleteDeviceDialog: () -> Unit,
    onGeneratePdfClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = customerWithDevices?.customer?.let { "OS #${it.id} - ${it.name}" } ?: "Detalhes do Cliente",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onGeneratePdfClick) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Gerar PDF"
                        )
                    }
                    IconButton(onClick = onEditCustomerClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar Cliente"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddDeviceClick,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Novo Dispositivo") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (customerWithDevices == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Carregando dados do cliente...",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            val customer = customerWithDevices.customer
            val devices = customerWithDevices.devices
            val totalValue = devices.sumOf { it.price }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    CustomerHeaderCard(
                        customer = customer,
                        onEditClick = onEditCustomerClick,
                        onPdfClick = onGeneratePdfClick
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Devices,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "Dispositivos (${devices.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Total: ${CurrencyUtils.formatBrl(totalValue)}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                if (devices.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Nenhum dispositivo cadastrado para este cliente.\nClique em \"+ Novo Dispositivo\" para adicionar.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(devices, key = { it.id }) { device ->
                        DeviceItemCard(
                            device = device,
                            onEditClick = { onEditDeviceClick(device.id) },
                            onDeleteClick = { onDeleteDeviceClick(device) }
                        )
                    }
                }
            }
        }

        if (deviceToDelete != null) {
            AlertDialog(
                onDismissRequest = onDismissDeleteDeviceDialog,
                title = { Text("Excluir Dispositivo") },
                text = {
                    Text("Tem certeza que deseja excluir o dispositivo \"${deviceToDelete.model} - ${deviceToDelete.brand}\"?")
                },
                confirmButton = {
                    TextButton(onClick = onConfirmDeleteDevice) {
                        Text("Excluir", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissDeleteDeviceDialog) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun CustomerHeaderCard(
    customer: Customer,
    onEditClick: () -> Unit,
    onPdfClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Column {
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Text(
                                text = "OS #${customer.id}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = customer.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Text(
                    text = customer.phone,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Default.Badge,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Text(
                    text = customer.cpf,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            if (customer.address.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = customer.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onPdfClick,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text("Gerar PDF")
                }
                Button(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text("Editar Dados")
                }
            }
        }
    }
}

@Composable
fun DeviceItemCard(
    device: Device,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Smartphone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "${device.model} (${device.brand})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar Dispositivo",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Excluir Dispositivo",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Text(
                    text = "S/N: ${device.serialNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            if (device.createdAt > 0L) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = "Cadastrado em: ${DateUtils.formatDate(device.createdAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            if (device.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = device.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = CurrencyUtils.formatBrl(device.price),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CustomerDetailContentPreview() {
    Cad_deltaTheme {
        CustomerDetailContent(
            customerWithDevices = CustomerWithDevices(
                customer = Customer(1, "João Silva", "(11) 98765-4321", "123.456.789-00", "Rua Flores, 100"),
                devices = listOf(
                    Device(1, 1, "iPhone 13", "Apple", "SN12345", "Troca de Tela", 1200.0),
                    Device(2, 1, "Galaxy S22", "Samsung", "SN67890", "Bateria", 450.0)
                )
            ),
            deviceToDelete = null,
            snackbarHostState = remember { SnackbarHostState() },
            onNavigateBack = {},
            onEditCustomerClick = {},
            onAddDeviceClick = {},
            onEditDeviceClick = {},
            onDeleteDeviceClick = {},
            onConfirmDeleteDevice = {},
            onDismissDeleteDeviceDialog = {},
            onGeneratePdfClick = {}
        )
    }
}
