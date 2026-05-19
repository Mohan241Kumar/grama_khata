package com.example.gramakhata.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GramaKhataDao {
    // Customers
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("""
        SELECT c.*, 
               (SELECT SUM(CASE WHEN type = 'GIVE' THEN amount ELSE -amount END) FROM transactions WHERE customerId = c.id) as balance,
               (SELECT MAX(timestamp) FROM transactions WHERE customerId = c.id) as lastTransactionTimestamp
        FROM customers c
        ORDER BY name ASC
    """)
    fun getAllCustomersWithBalance(): Flow<List<CustomerWithBalance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCustomer(customer: Customer): Long

    @Delete
    fun deleteCustomer(customer: Customer)

    // Transactions
    @Query("SELECT * FROM transactions WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getTransactionsForCustomer(customerId: Int): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTransaction(transaction: Transaction)

    @Delete
    fun deleteTransaction(transaction: Transaction)

    // Combined Data (simplified for now)
    @Query("SELECT SUM(CASE WHEN type = 'GIVE' THEN amount ELSE -amount END) FROM transactions WHERE customerId = :customerId")
    fun getCustomerBalance(customerId: Int): Flow<Double?>

    @Query("""
        SELECT SUM(balance) FROM (
            SELECT SUM(CASE WHEN type = 'TAKE' THEN amount ELSE -amount END) as balance 
            FROM transactions GROUP BY customerId HAVING balance > 0
        )
    """)
    fun getTotalGive(): Flow<Double?>

    @Query("""
        SELECT SUM(balance) FROM (
            SELECT SUM(CASE WHEN type = 'GIVE' THEN amount ELSE -amount END) as balance 
            FROM transactions GROUP BY customerId HAVING balance > 0
        )
    """)
    fun getTotalGet(): Flow<Double?>
    @Query("""
        SELECT t.*, c.name as customerName, c.phoneNumber as phoneNumber 
        FROM transactions t 
        JOIN customers c ON t.customerId = c.id 
        ORDER BY t.timestamp DESC
    """)
    fun getAllTransactionsWithCustomerName(): Flow<List<TransactionWithCustomer>>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'GIVE' AND timestamp >= :startTime")
    fun getPeriodGiveSum(startTime: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'TAKE' AND timestamp >= :startTime")
    fun getPeriodTakeSum(startTime: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'GIVE'")
    fun getTotalGiveVolume(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'TAKE'")
    fun getTotalTakeVolume(): Flow<Double?>
}
