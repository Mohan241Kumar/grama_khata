package com.example.gramakhata.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class GramaKhataRepository(private val dao: GramaKhataDao) {
    val allCustomers: Flow<List<Customer>> = dao.getAllCustomers()
    val allCustomersWithBalance: Flow<List<CustomerWithBalance>> = dao.getAllCustomersWithBalance()
    val totalGive: Flow<Double?> = dao.getTotalGive()
    val totalGet: Flow<Double?> = dao.getTotalGet()
    val allTransactionsWithCustomerName: Flow<List<TransactionWithCustomer>> = dao.getAllTransactionsWithCustomerName()
    
    fun getPeriodGiveSum(startTime: Long): Flow<Double?> = dao.getPeriodGiveSum(startTime)
    fun getPeriodTakeSum(startTime: Long): Flow<Double?> = dao.getPeriodTakeSum(startTime)
    fun getTotalGiveVolume(): Flow<Double?> = dao.getTotalGiveVolume()
    fun getTotalTakeVolume(): Flow<Double?> = dao.getTotalTakeVolume()

    fun getTransactionsForCustomer(customerId: Int): Flow<List<Transaction>> =
        dao.getTransactionsForCustomer(customerId)

    fun getCustomerBalance(customerId: Int): Flow<Double?> =
        dao.getCustomerBalance(customerId)

    suspend fun addCustomer(customer: Customer) {
        withContext(Dispatchers.IO) {
            dao.insertCustomer(customer)
        }
    }

    suspend fun addTransaction(transaction: Transaction) {
        withContext(Dispatchers.IO) {
            dao.insertTransaction(transaction)
        }
    }

    suspend fun deleteCustomer(customer: Customer) {
        withContext(Dispatchers.IO) {
            dao.deleteCustomer(customer)
        }
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        withContext(Dispatchers.IO) {
            dao.deleteTransaction(transaction)
        }
    }
}
