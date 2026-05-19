package com.example.gramakhata.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.gramakhata.GramaKhataApplication
import com.example.gramakhata.R
import com.example.gramakhata.ui.components.ActionButtons
import com.example.gramakhata.ui.components.TransactionListItem
import com.example.gramakhata.ui.navigation.Screen
import com.example.gramakhata.ui.theme.DueRed
import com.example.gramakhata.ui.theme.PaymentGreen
import com.example.gramakhata.ui.viewmodel.CustomerDetailsViewModel
import com.example.gramakhata.ui.viewmodel.CustomerDetailsViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailsScreen(
    navController: NavController,
    customerId: Int,
    viewModel: CustomerDetailsViewModel = viewModel(
        factory = CustomerDetailsViewModelFactory(
            (navController.context.applicationContext as GramaKhataApplication).repository,
            customerId
        )
    )
) {
    val customer by viewModel.customer.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val balance by viewModel.balance.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Profile Image in App Bar
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            if (customer?.profileImageUri != null) {
                                AsyncImage(
                                    model = customer?.profileImageUri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = customer?.name?.firstOrNull()?.toString() ?: "?",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column {
                            Text(customer?.name ?: stringResource(R.string.loading), style = MaterialTheme.typography.titleMedium)
                            Text(customer?.phoneNumber ?: "", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* More options */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 2.dp) {
                ActionButtons(
                    onGiveClick = { navController.navigate(Screen.AddTransaction.createRoute(customerId, "GIVE")) },
                    onTakeClick = { navController.navigate(Screen.AddTransaction.createRoute(customerId, "TAKE")) }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Balance Summary Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (balance >= 0) DueRed.copy(alpha = 0.1f) else PaymentGreen.copy(alpha = 0.1f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (balance >= 0) stringResource(R.string.total_due) else stringResource(R.string.total_advance),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (balance >= 0) DueRed else PaymentGreen
                        )
                        Text(
                            text = "₹${"%.0f".format(kotlin.math.abs(balance))}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (balance >= 0) DueRed else PaymentGreen
                        )
                    }
                    
                    if (balance > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                val message = context.getString(R.string.sms_reminder_msg, customer?.name ?: "", balance.toInt())
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("sms:${customer?.phoneNumber}")
                                    putExtra("sms_body", message)
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DueRed),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DueRed),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                              )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.send_reminder), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            Text(
                text = stringResource(R.string.transaction_history),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(transactions) { transaction ->
                    TransactionListItem(
                        amount = transaction.amount,
                        type = transaction.type,
                        note = transaction.note,
                        timestamp = transaction.timestamp
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}
