package com.example.gramakhata.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gramakhata.data.GramaKhataRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.first

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import java.util.Calendar

enum class ReportPeriod {
    DAILY, WEEKLY, MONTHLY, ALL
}

class ReportsViewModel(private val repository: GramaKhataRepository) : ViewModel() {
    
    private val _selectedPeriod = MutableStateFlow(ReportPeriod.ALL)
    val selectedPeriod: StateFlow<ReportPeriod> = _selectedPeriod

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val periodStartTime = _selectedPeriod.map { period ->
        val calendar = Calendar.getInstance()
        when (period) {
            ReportPeriod.DAILY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            ReportPeriod.WEEKLY -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            ReportPeriod.MONTHLY -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            ReportPeriod.ALL -> 0L
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val totalGive: StateFlow<Double> = periodStartTime.flatMapLatest { startTime ->
        if (startTime == 0L) repository.getTotalGiveVolume() else repository.getPeriodGiveSum(startTime)
    }.map { it ?: 0.0 }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val totalGet: StateFlow<Double> = periodStartTime.flatMapLatest { startTime ->
        if (startTime == 0L) repository.getTotalTakeVolume() else repository.getPeriodTakeSum(startTime)
    }.map { it ?: 0.0 }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val overallGive: StateFlow<Double> = repository.totalGive
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val overallGet: StateFlow<Double> = repository.totalGet
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun onPeriodSelected(period: ReportPeriod) {
        _selectedPeriod.value = period
    }

    private val _exportStatus = kotlinx.coroutines.flow.MutableStateFlow<ExportStatus>(ExportStatus.Idle)
    val exportStatus: StateFlow<ExportStatus> = _exportStatus

    fun exportToExcel(context: android.content.Context) {
        viewModelScope.launch {
            _exportStatus.value = ExportStatus.Loading
            
            // Fetch transactions directly from repository to ensure we get the latest data
            val transactions = repository.allTransactionsWithCustomerName.first()
            
            if (transactions.isEmpty()) {
                _exportStatus.value = ExportStatus.Error("No transactions found to export")
                return@launch
            }

            val file = com.example.gramakhata.util.ExcelExporter.exportTransactions(context, transactions)
            if (file != null) {
                _exportStatus.value = ExportStatus.Success(file)
            } else {
                _exportStatus.value = ExportStatus.Error("Failed to generate Excel file")
            }
        }
    }

    fun resetExportStatus() {
        _exportStatus.value = ExportStatus.Idle
    }
}

sealed class ExportStatus {
    object Idle : ExportStatus()
    object Loading : ExportStatus()
    data class Success(val file: java.io.File) : ExportStatus()
    data class Error(val message: String) : ExportStatus()
}

class ReportsViewModelFactory(private val repository: GramaKhataRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReportsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
