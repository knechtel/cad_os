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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cad_delta.Customer
import com.example.cad_delta.CustomerWithDevices
import com.example.cad_delta.Device
import com.example.cad_delta.ui.theme.Cad_deltaTheme
import com.example.cad_delta.ui.utils.CurrencyUtils
import com.example.cad_delta.ui.viewmodel.MainViewModel

@Composable
fun CustomerListScreen(
    viewModel: MainViewModel,
    onNavigateToAddCustomer: () -> Unit,
    onNavigateToCustomerDetail: (Long) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val customersWithDevices by viewModel.customersWithDevices.collectAsState()
    val customerToDelete by viewModel.customerToDelete.collectAsState()

    CustomerListContent(
        searchQuery = searchQuery,
        customersWithDevices = customersWithDevices,
        customerToDelete = customerToDelete,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onAddCustomerClick = onNavigateToAddCustomer,
        onCustomerClick = { onNavigateToCustomerDetail(it.customer.id) },
        onDeleteCustomerClick = viewModel::onDeleteCustomerClick,
        onConfirmDeleteCustomer = viewModel::onConfirmDeleteCustomer,
        onDismissDeleteDialog = viewModel::onDismissDeleteCustomerDialog
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListContent(
    searchQuery: String,
    customersWithDevices: List<CustomerWithDevices>,
    customerToDelete: Customer?,
    onSearchQueryChange: (String) -> Unit,
    onAddCustomerClick: () -> Unit,
    onCustomerClick: (CustomerWithDevices) -> Unit,
    onDeleteCustomerClick: (Customer) -> Unit,
    onConfirmDeleteCustomer: () -> Unit,
    onDismissDeleteDialog: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Lista de Clientes", fontWeight = FontWeight.Bold) },
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
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                placeholder = { Text("Buscar por OS, nome, telefone ou CPF...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpar")
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            if (customersWithDevices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isBlank()) "Nenhum cliente cadastrado." else "Nenhum cliente encontrado para \"$searchQuery\".",
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
            customersWithDevices = listOf(
                CustomerWithDevices(
                    customer = Customer(1, "João Silva", "(11) 98765-4321", "123.456.789-00", "Rua Flores, 100"),
                    devices = listOf(
                        Device(1, 1, "iPhone 13", "Apple", "SN12345", "Troca de Tela", 1200.0),
                        Device(2, 1, "Galaxy S22", "Samsung", "SN67890", "Bateria", 450.0)
                    )
                )
            ),
            customerToDelete = null,
            onSearchQueryChange = {},
            onAddCustomerClick = {},
            onCustomerClick = {},
            onDeleteCustomerClick = {},
            onConfirmDeleteCustomer = {},
            onDismissDeleteDialog = {}
        )
    }
}
