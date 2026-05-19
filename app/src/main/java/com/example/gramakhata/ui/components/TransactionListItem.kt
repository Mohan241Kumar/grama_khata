package com.example.gramakhata.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gramakhata.data.TransactionType
import com.example.gramakhata.ui.theme.DueRed
import com.example.gramakhata.ui.theme.PaymentGreen
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionListItem(
    amount: Double,
    type: TransactionType,
    note: String,
    timestamp: Long
) {
    val date = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(timestamp))
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (note.isNotEmpty()) note else "Transaction",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Text(
            text = "₹${"%.0f".format(amount)}",
            style = MaterialTheme.typography.titleLarge,
            color = if (type == TransactionType.GIVE) DueRed else PaymentGreen,
            fontWeight = FontWeight.Bold
        )
    }
}
