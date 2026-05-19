package com.example.gramakhata.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gramakhata.data.Customer
import com.example.gramakhata.data.GramaKhataRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

import com.example.gramakhata.data.CustomerWithBalance as DataCustomerWithBalance

@OptIn(ExperimentalCoroutinesApi::class)
class RemindersViewModel(private val repository: GramaKhataRepository) : ViewModel() {
    
    val overdueCustomers: StateFlow<List<DataCustomerWithBalance>> = repository.allCustomersWithBalance
        .map { list -> list.filter { it.balance > 0 } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalOverdue: StateFlow<Double> = overdueCustomers
        .map { list -> list.sumOf { it.balance } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
}

class RemindersViewModelFactory(private val repository: GramaKhataRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RemindersViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RemindersViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
