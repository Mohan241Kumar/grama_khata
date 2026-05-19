package com.example.gramakhata

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gramakhata.ui.screens.MainScreen
import com.example.gramakhata.ui.theme.GramaKhataTheme
import com.example.gramakhata.ui.viewmodel.ThemeViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val isDarkMode by themeViewModel.isDarkMode.collectAsStateWithLifecycle()
            
            GramaKhataTheme(darkTheme = isDarkMode) {
                MainScreen()
            }
        }
    }
}