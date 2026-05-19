package com.example.gramakhata.data

import androidx.room.Embedded

data class CustomerWithBalance(
    @Embedded val customer: Customer,
    val balance: Double,
    val lastTransactionTimestamp: Long?
)
