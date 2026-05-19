package com.example.gramakhata.util

import android.content.Context
import com.example.gramakhata.data.TransactionWithCustomer
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ExcelExporter {
    
    /**
     * Exports transactions to a CSV file (Excel compatible).
     * Uses the ="value" trick to force Excel to treat phone numbers as text.
     */
    fun exportTransactions(context: Context, transactions: List<TransactionWithCustomer>): File? {
        val csvData = StringBuilder()
        
        // Add header row
        csvData.append("Date,Time,Customer Name,Phone Number,Type,Amount,Note\n")
        
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        
        // Add data rows
        for (transaction in transactions) {
            val dateObj = Date(transaction.transaction.timestamp)
            val date = dateFormat.format(dateObj)
            val time = timeFormat.format(dateObj)
            
            val name = escapeCsv(transaction.customerName)
            
            // Force phone number to be text using Excel formula notation to avoid scientific notation
            val phone = "=\"${transaction.phoneNumber}\""
            
            val type = transaction.transaction.type.name
            val amount = transaction.transaction.amount.toString()
            val note = escapeCsv(transaction.transaction.note)
            
            csvData.append("$date,$time,$name,$phone,$type,$amount,$note\n")
        }
        
        // Save to file
        return try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }
            
            val fileName = "GramaKhata_Export_${System.currentTimeMillis()}.csv"
            val file = File(exportDir, fileName)
            FileOutputStream(file).use { out ->
                out.write(csvData.toString().toByteArray())
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }
}
