package com.example.gramakhata.data

import androidx.room.Embedded

data class TransactionWithCustomer(
    @Embedded val transaction: Transaction,
    val customerName: String,
    val phoneNumber: String
)
