package com.example.gramakhata

import android.app.Application
import com.example.gramakhata.data.GramaKhataDatabase
import com.example.gramakhata.data.GramaKhataRepository

class GramaKhataApplication : Application() {
    val database by lazy { GramaKhataDatabase.getDatabase(this) }
    val repository by lazy { GramaKhataRepository(database.dao()) }
}
