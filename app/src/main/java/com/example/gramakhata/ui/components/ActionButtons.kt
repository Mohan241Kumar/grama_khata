package com.example.gramakhata.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.gramakhata.ui.theme.DueRed
import com.example.gramakhata.ui.theme.PaymentGreen

@Composable
fun ActionButtons(
    onGiveClick: () -> Unit,
    onTakeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onGiveClick,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = DueRed),
            shape = MaterialTheme.shapes.small
        ) {
            Text("YOU GIVE ₹", color = Color.White)
        }
        
        Button(
            onClick = onTakeClick,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = PaymentGreen),
            shape = MaterialTheme.shapes.small
        ) {
            Text("YOU GET ₹", color = Color.White)
        }
    }
}
