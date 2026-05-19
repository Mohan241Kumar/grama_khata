package com.example.gramakhata.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gramakhata.GramaKhataApplication
import com.example.gramakhata.R
import com.example.gramakhata.data.Customer
import com.example.gramakhata.data.CustomerWithBalance
import com.example.gramakhata.ui.components.CustomerListItem
import com.example.gramakhata.ui.components.SummaryCard
import com.example.gramakhata.ui.navigation.Screen
import com.example.gramakhata.ui.viewmodel.DashboardViewModel
import com.example.gramakhata.ui.viewmodel.DashboardViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModelFactory(
            (navController.context.applicationContext as GramaKhataApplication).repository
        )
    )
) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val totalGive by viewModel.totalGive.collectAsStateWithLifecycle()
    val totalGet by viewModel.totalGet.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var customerToDelete by remember { mutableStateOf<Customer?>(null) }

    val filteredCustomers = customers.filter {
        it.customer.name.contains(searchQuery, ignoreCase = true)
    }

    if (customerToDelete != null) {
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            title = { Text(stringResource(R.string.delete_customer_title)) },
            text = { Text(stringResource(R.string.delete_customer_msg, customerToDelete?.name ?: "")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        customerToDelete?.let { viewModel.deleteCustomer(it) }
                        customerToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { customerToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.dashboard_title), 
                        style = MaterialTheme.typography.headlineMedium, 
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ) 
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Reports.route) }) {
                        Icon(Icons.Default.TrendingUp, contentDescription = stringResource(R.string.business_insights), tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.AddCustomer.route) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.add_customer), fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            SummaryCard(youGive = totalGive, youGet = totalGet)
            
            // Search & Filter Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.search_hint), style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .clickable { /* Filter Logic */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = stringResource(R.string.search_hint), tint = MaterialTheme.colorScheme.primary)
                }
            }
            
            // Section Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.customers_count, filteredCustomers.size),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.business_insights),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { navController.navigate(Screen.Reports.route) }
                )
            }
            
            if (filteredCustomers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(bottom = 80.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (searchQuery.isEmpty()) stringResource(R.string.no_customers_yet) else stringResource(R.string.no_matching_customers),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (searchQuery.isEmpty()) {
                            TextButton(onClick = { navController.navigate(Screen.AddCustomer.route) }) {
                                Text(stringResource(R.string.add_first_customer))
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredCustomers, key = { it.customer.id }) { customerWithBalance ->
                        val customer = customerWithBalance.customer
                        val lastTxDate = if (customerWithBalance.lastTransactionTimestamp != null) {
                            val formatter = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault())
                            "Last entry ${formatter.format(java.util.Date(customerWithBalance.lastTransactionTimestamp))}"
                        } else {
                            stringResource(R.string.no_transactions)
                        }
                        
                        CustomerListItem(
                            name = customer.name,
                            lastTransaction = lastTxDate,
                            balance = customerWithBalance.balance,
                            profileImageUri = customer.profileImageUri,
                            onClick = { navController.navigate(Screen.CustomerDetails.createRoute(customer.id)) },
                            onLongClick = { customerToDelete = customer }
                        )
                    }
                }
            }
        }
    }
}
