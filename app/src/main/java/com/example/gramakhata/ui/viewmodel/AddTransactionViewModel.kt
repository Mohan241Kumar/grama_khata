package com.example.gramakhata.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gramakhata.data.Customer
import com.example.gramakhata.data.GramaKhataRepository
import com.example.gramakhata.data.Transaction
import com.example.gramakhata.data.TransactionType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AddTransactionViewModel(
    private val repository: GramaKhataRepository,
    private val customerId: Int,
    val initialType: TransactionType
) : ViewModel() {

    val customer: StateFlow<Customer?> = repository.allCustomers
        .map { customers -> customers.find { it.id == customerId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentBalance: StateFlow<Double> = repository.getCustomerBalance(customerId)
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun addTransaction(amount: Double, note: String, type: TransactionType, timestamp: Long) {
        viewModelScope.launch {
            repository.addTransaction(
                Transaction(
                    customerId = customerId,
                    amount = amount,
                    type = type,
                    note = note,
                    timestamp = timestamp
                )
            )
        }
    }
}

class AddTransactionViewModelFactory(
    private val repository: GramaKhataRepository,
    private val customerId: Int,
    private val initialType: TransactionType
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddTransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddTransactionViewModel(repository, customerId, initialType) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
