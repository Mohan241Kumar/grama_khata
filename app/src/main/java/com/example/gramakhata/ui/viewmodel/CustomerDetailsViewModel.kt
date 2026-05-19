package com.example.gramakhata.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gramakhata.data.Customer
import com.example.gramakhata.data.GramaKhataRepository
import com.example.gramakhata.data.Transaction
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CustomerDetailsViewModel(
    private val repository: GramaKhataRepository,
    private val customerId: Int
) : ViewModel() {
    
    val customer: StateFlow<Customer?> = repository.allCustomers
        .map { customers -> customers.find { it.id == customerId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val transactions: StateFlow<List<Transaction>> = repository.getTransactionsForCustomer(customerId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val balance: StateFlow<Double> = repository.getCustomerBalance(customerId)
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
}

class CustomerDetailsViewModelFactory(
    private val repository: GramaKhataRepository,
    private val customerId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CustomerDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CustomerDetailsViewModel(repository, customerId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
