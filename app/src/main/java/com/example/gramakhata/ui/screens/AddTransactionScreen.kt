package com.example.gramakhata.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gramakhata.GramaKhataApplication
import com.example.gramakhata.R
import com.example.gramakhata.data.TransactionType
import com.example.gramakhata.ui.theme.DueRed
import com.example.gramakhata.ui.theme.PaymentGreen
import com.example.gramakhata.ui.viewmodel.AddTransactionViewModel
import com.example.gramakhata.ui.viewmodel.AddTransactionViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    navController: NavController,
    customerId: Int,
    initialType: String,
    viewModel: AddTransactionViewModel = viewModel(
        factory = AddTransactionViewModelFactory(
            (navController.context.applicationContext as GramaKhataApplication).repository,
            customerId,
            try { TransactionType.valueOf(initialType) } catch (e: Exception) { TransactionType.GIVE }
        )
    )
) {
    val customer by viewModel.customer.collectAsStateWithLifecycle()
    val currentBalance by viewModel.currentBalance.collectAsStateWithLifecycle()
    
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val todayLabel = stringResource(R.string.today)
    val yesterdayLabel = stringResource(R.string.yesterday)
    var selectedDate by remember { mutableStateOf(todayLabel) }
    var selectedTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.new_entry), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Customer Header
            Text(stringResource(R.string.entry_for), style = MaterialTheme.typography.labelSmall)
            Text(
                customer?.name ?: stringResource(R.string.loading),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                stringResource(
                    R.string.current_balance_label,
                    currentBalance.toInt(),
                    if (currentBalance >= 0) stringResource(R.string.due) else stringResource(R.string.advance)
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = if (currentBalance >= 0) DueRed else PaymentGreen
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Amount Input
            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.all { char -> char.isDigit() }) amount = it },
                label = { Text("${stringResource(R.string.amount)} (₹)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Date Quick Select
            val selectDateLabel = stringResource(R.string.select_date)
            val isCustomDate = selectedDate != todayLabel && selectedDate != yesterdayLabel
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DateChip(todayLabel, selectedDate == todayLabel) { 
                    selectedDate = todayLabel 
                    selectedTimestamp = System.currentTimeMillis()
                }
                DateChip(yesterdayLabel, selectedDate == yesterdayLabel) { 
                    selectedDate = yesterdayLabel 
                    selectedTimestamp = System.currentTimeMillis() - (24 * 60 * 60 * 1000) // Yesterday
                }
                DateChip(
                    label = if (isCustomDate) selectedDate else selectDateLabel,
                    selected = isCustomDate,
                    icon = true
                ) { 
                    showDatePicker = true 
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Note Input
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(stringResource(R.string.note)) },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(32.dp))

            // SAVE Button
            Button(
                onClick = {
                    if (amount.isNotBlank()) {
                        scope.launch {
                            val type = try {
                                TransactionType.valueOf(initialType.uppercase())
                            } catch (e: Exception) {
                                TransactionType.GIVE
                            }
                            viewModel.addTransaction(
                                amount = amount.toDouble(),
                                note = note,
                                type = type,
                                timestamp = selectedTimestamp
                            )
                            navController.navigateUp()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (initialType.uppercase() == "GIVE") DueRed else PaymentGreen
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(stringResource(R.string.save_entry), fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formatter = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                        selectedDate = formatter.format(java.util.Date(millis))
                        selectedTimestamp = millis
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun DateChip(label: String, selected: Boolean, icon: Boolean = false, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.small,
        border = if (!selected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
