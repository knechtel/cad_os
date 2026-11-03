package com.example.cad_delta.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cad_delta.Customer
import com.example.cad_delta.CustomerWithDevices
import com.example.cad_delta.Device
import com.example.cad_delta.ui.theme.Cad_deltaTheme
import com.example.cad_delta.ui.utils.CurrencyUtils
import com.example.cad_delta.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun CustomerListScreen(
    viewModel: MainViewModel,
    onNavigateToAddCustomer: () -> Unit,
    onNavigateToCustomerDetail: (Long) -> Unit,
    onNavigateToDeliveryList: () -> Unit = {},
    deliveryOnly: Boolean = false
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val osFilter by viewModel.osFilter.collectAsState()
    val dateFilter by viewModel.dateFilter.collectAsState()
    val deliveryDateFilter by viewModel.deliveryDateFilter.collectAsState()
    val customersWithDevices by viewModel.customersWithDevices.collectAsState()
    val customerToDelete by viewModel.customerToDelete.collectAsState()

    CustomerListContent(
        searchQuery = searchQuery,
        osFilter = osFilter,
        dateFilter = dateFilter,
        deliveryDateFilter = deliveryDateFilter,
        customersWithDevices = customersWithDevices,
        customerToDelete = customerToDelete,
        deliveryOnly = deliveryOnly,
        onNavigateToDeliveryList = onNavigateToDeliveryList,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onOsFilterChange = viewModel::onOsFilterChange,
        onClearOsFilter = viewModel::clearOsFilter,
        onDateFilterChange = viewModel::onDateFilterChange,
        onClearDateFilter = viewModel::clearDateFilter,
        onDeliveryDateFilterChange = viewModel::onDeliveryDateFilterChange,
        onClearDeliveryDateFilter = viewModel::clearDeliveryDateFilter,
        onClearAllFilters = viewModel::clearAllFilters,
        onAddCustomerClick = onNavigateToAddCustomer,
        onCustomerClick = { onNavigateToCustomerDetail(it.customer.id) },
        onDeleteCustomerClick = viewModel::onDeleteCustomerClick,
        onConfirmDeleteCustomer = viewModel::onConfirmDeleteCustomer,
        onDismissDeleteDialog = viewModel::onDismissDeleteCustomerDialog
    )
}

@Composable
fun DeliveredCustomerListScreen(
    viewModel: MainViewModel,
    onNavigateToAddCustomer: () -> Unit,
    onNavigateToCustomerDetail: (Long) -> Unit,
    onNavigateToEntryList: () -> Unit
) {
    CustomerListScreen(
        viewModel = viewModel,
        onNavigateToAddCustomer = onNavigateToAddCustomer,
        onNavigateToCustomerDetail = onNavigateToCustomerDetail,
        onNavigateToDeliveryList = onNavigateToEntryList,
        deliveryOnly = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListContent(
    searchQuery: String,
    osFilter: String,
    dateFilter: String,
    deliveryDateFilter: String,
    customersWithDevices: List<CustomerWithDevices>,
    customerToDelete: Customer?,
    deliveryOnly: Boolean,
    onNavigateToDeliveryList: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onOsFilterChange: (String) -> Unit,
    onClearOsFilter: () -> Unit,
    onDateFilterChange: (String) -> Unit,
    onClearDateFilter: () -> Unit,
    onDeliveryDateFilterChange: (String) -> Unit,
    onClearDeliveryDateFilter: () -> Unit,
    onClearAllFilters: () -> Unit,
    onAddCustomerClick: () -> Unit,
    onCustomerClick: (CustomerWithDevices) -> Unit,
    onDeleteCustomerClick: (Customer) -> Unit,
    onConfirmDeleteCustomer: () -> Unit,
    onDismissDeleteDialog: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeliveryDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }
                            onDateFilterChange(formatter.format(Date(selectedMillis)))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showDeliveryDatePicker) {
        val deliveryDatePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDeliveryDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = deliveryDatePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }
                            onDeliveryDateFilterChange(formatter.format(Date(selectedMillis)))
                        }
                        showDeliveryDatePicker = false
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeliveryDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = deliveryDatePickerState)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (deliveryOnly) "Clientes Entregues" else "Lista de Clientes", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(onClick = onNavigateToDeliveryList) {
                        Text(if (deliveryOnly) "Entrada e ID" else "Entregues")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCustomerClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Adicionar Cliente")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Main Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                placeholder = { Text("Buscar por nome, telefone ou CPF...") },
                label = { Text("Buscar Cliente") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpar busca")
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            // Dedicated Filters (Filter Chips)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!deliveryOnly) FilterChip(
                    selected = osFilter.isNotEmpty(),
                    onClick = {
                        if (osFilter.isNotEmpty()) {
                            onClearOsFilter()
                        }
                    },
                    label = {
                        Text(if (osFilter.isEmpty()) "Filtro por ID" else "ID #$osFilter")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.ConfirmationNumber,
                            contentDescription = "Filtro por ID",
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    },
                    trailingIcon = {
                        if (osFilter.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpar filtro por ID",
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    }
                )

                if (!deliveryOnly) FilterChip(
                    selected = dateFilter.isNotEmpty(),
                    onClick = {
                        if (dateFilter.isNotEmpty()) {
                            onClearDateFilter()
                        } else {
                            showDatePicker = true
                        }
                    },
                    label = {
                        Text(if (dateFilter.isEmpty()) "Filtro por Data de Entrada" else "Entrada: $dateFilter")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Filtro por Data de Entrada",
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    },
                    trailingIcon = {
                        if (dateFilter.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpar filtro de data",
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    }
                )

                if (deliveryOnly) FilterChip(
                    selected = deliveryDateFilter.isNotEmpty(),
                    onClick = {
                        if (deliveryDateFilter.isNotEmpty()) {
                            onClearDeliveryDateFilter()
                        } else {
                            showDeliveryDatePicker = true
                        }
                    },
                    label = {
                        Text(if (deliveryDateFilter.isEmpty()) "Filtro por Data Entregue" else "Entregue: $deliveryDateFilter")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.EventAvailable,
                            contentDescription = "Filtro por Data Entregue",
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    },
                    trailingIcon = {
                        if (deliveryDateFilter.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpar filtro de data entregue",
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    }
                )
            }

            // Dedicated Filter Inputs (Numeric OS Field, Entry Date Field & Delivery Date Field)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!deliveryOnly) OutlinedTextField(
                    value = osFilter,
                    onValueChange = onOsFilterChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ex: 1, 42") },
                    label = { Text("ID do Cliente") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Tag, contentDescription = "ID do Cliente")
                    },
                    trailingIcon = {
                        if (osFilter.isNotEmpty()) {
                            IconButton(onClick = onClearOsFilter) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpar ID")
                            }
                        }
                    },
                    shape = MaterialTheme.shapes.medium
                )

                if (!deliveryOnly) OutlinedTextField(
                    value = dateFilter,
                    onValueChange = onDateFilterChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("dd/mm/aaaa") },
                    label = { Text("Data Entrada") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(imageVector = Icons.Default.CalendarToday, contentDescription = "Abrir Calendário Data Entrada")
                        }
                    },
                    trailingIcon = {
                        if (dateFilter.isNotEmpty()) {
                            IconButton(onClick = onClearDateFilter) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpar Data Entrada")
                            }
                        }
                    },
                    shape = MaterialTheme.shapes.medium
                )

                if (deliveryOnly) OutlinedTextField(
                    value = deliveryDateFilter,
                    onValueChange = onDeliveryDateFilterChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("dd/mm/aaaa") },
                    label = { Text("Data Entregue") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = {
                        IconButton(onClick = { showDeliveryDatePicker = true }) {
                            Icon(imageVector = Icons.Default.EventAvailable, contentDescription = "Abrir Calendário Data Entregue")
                        }
                    },
                    trailingIcon = {
                        if (deliveryDateFilter.isNotEmpty()) {
                            IconButton(onClick = onClearDeliveryDateFilter) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpar Data Entregue")
                            }
                        }
                    },
                    shape = MaterialTheme.shapes.medium
                )
            }

            // Active Filters Banner & Clear Option
            if (searchQuery.isNotEmpty() || osFilter.isNotEmpty() || dateFilter.isNotEmpty() || deliveryDateFilter.isNotEmpty()) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(18.dp)
                                    .padding(end = 4.dp)
                            )
                            Text(
                                text = buildString {
                                    append("Filtros: ")
                                    val parts = mutableListOf<String>()
                                    if (osFilter.isNotEmpty()) parts.add("ID #$osFilter")
                                    if (dateFilter.isNotEmpty()) parts.add("Entrada: $dateFilter")
                                    if (deliveryDateFilter.isNotEmpty()) parts.add("Entregue: $deliveryDateFilter")
                                    if (searchQuery.isNotEmpty()) parts.add("\"$searchQuery\"")
                                    append(parts.joinToString(" • "))
                                },
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        TextButton(
                            onClick = onClearAllFilters,
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = "Limpar tudo",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            if (customersWithDevices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val emptyMessage = when {
                        osFilter.isNotBlank() || dateFilter.isNotBlank() || deliveryDateFilter.isNotBlank() || searchQuery.isNotBlank() -> {
                            val details = mutableListOf<String>()
                            if (osFilter.isNotBlank()) details.add("ID #$osFilter")
                            if (dateFilter.isNotBlank()) details.add("Data Entrada $dateFilter")
                            if (deliveryDateFilter.isNotBlank()) details.add("Data Entregue $deliveryDateFilter")
                            if (searchQuery.isNotBlank()) details.add("busca \"$searchQuery\"")
                            "Nenhum cliente/dispositivo encontrado para ${details.joinToString(" • ")}."
                        }
                        else -> "Nenhum cliente cadastrado."
                    }
                    Text(
                        text = emptyMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(customersWithDevices, key = { it.customer.id }) { item ->
                        CustomerItemCard(
                            item = item,
                            onClick = { onCustomerClick(item) },
                            onDeleteClick = { onDeleteCustomerClick(item.customer) }
                        )
                    }
                }
            }
        }

        if (customerToDelete != null) {
            AlertDialog(
                onDismissRequest = onDismissDeleteDialog,
                title = { Text("Excluir Cliente") },
                text = {
                    Text("Tem certeza que deseja excluir o cliente \"${customerToDelete.name}\"? Todos os dispositivos vinculados também serão excluídos.")
                },
                confirmButton = {
                    TextButton(onClick = onConfirmDeleteCustomer) {
                        Text("Excluir", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissDeleteDialog) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun CustomerItemCard(
    item: CustomerWithDevices,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val totalDevices = item.devices.size
    val totalPriceSum = item.devices.sumOf { it.price }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Column {
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Text(
                                text = "OS #${item.customer.id}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.customer.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir Cliente",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Text(
                    text = item.customer.phone,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Default.Badge,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Text(
                    text = item.customer.cpf,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (item.customer.address.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = item.customer.address,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Devices,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = "$totalDevices dispositivo(s)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = "Total: ${CurrencyUtils.formatBrl(totalPriceSum)}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CustomerListContentPreview() {
    Cad_deltaTheme {
        CustomerListContent(
            searchQuery = "",
            osFilter = "42",
            dateFilter = "25/12/2024",
            deliveryDateFilter = "",
            customersWithDevices = listOf(
                CustomerWithDevices(
                    customer = Customer(42, "João Silva", "(11) 98765-4321", "123.456.789-00", "Rua Flores, 100"),
                    devices = listOf(
                        Device(1, 42, "iPhone 13", "Apple", "SN12345", "Troca de Tela", 1200.0),
                        Device(2, 42, "Galaxy S22", "Samsung", "SN67890", "Bateria", 450.0)
                    )
                )
            ),
            customerToDelete = null,
            deliveryOnly = false,
            onNavigateToDeliveryList = {},
            onSearchQueryChange = {},
            onOsFilterChange = {},
            onClearOsFilter = {},
            onDateFilterChange = {},
            onClearDateFilter = {},
            onDeliveryDateFilterChange = {},
            onClearDeliveryDateFilter = {},
            onClearAllFilters = {},
            onAddCustomerClick = {},
            onCustomerClick = {},
            onDeleteCustomerClick = {},
            onConfirmDeleteCustomer = {},
            onDismissDeleteDialog = {}
        )
    }
}
