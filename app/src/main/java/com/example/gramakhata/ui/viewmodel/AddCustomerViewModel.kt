package com.example.gramakhata.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gramakhata.data.Customer
import com.example.gramakhata.data.GramaKhataRepository
import kotlinx.coroutines.launch

class AddCustomerViewModel(private val repository: GramaKhataRepository) : ViewModel() {
    fun addCustomer(name: String, phoneNumber: String, profileImageUri: String? = null) {
        viewModelScope.launch {
            repository.addCustomer(
                Customer(
                    name = name,
                    phoneNumber = phoneNumber,
                    profileImageUri = profileImageUri
                )
            )
        }
    }
}

class AddCustomerViewModelFactory(private val repository: GramaKhataRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddCustomerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddCustomerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
