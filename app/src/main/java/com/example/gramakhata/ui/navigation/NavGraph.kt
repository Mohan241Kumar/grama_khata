package com.example.gramakhata.ui.navigation
import androidx.compose.ui.Modifier

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.compose.material3.Text
import com.example.gramakhata.ui.screens.DashboardScreen
import com.example.gramakhata.ui.screens.CustomerDetailsScreen
import com.example.gramakhata.ui.screens.AddTransactionScreen
import com.example.gramakhata.ui.screens.AddCustomerScreen
import com.example.gramakhata.ui.screens.SettingsScreen
import com.example.gramakhata.ui.screens.LanguageSelectionScreen
import com.example.gramakhata.ui.screens.HelpSupportScreen
import com.example.gramakhata.ui.screens.AboutScreen
import com.example.gramakhata.ui.screens.AppPreferencesScreen
import com.example.gramakhata.ui.screens.ReportsScreen
import com.example.gramakhata.ui.screens.RemindersScreen

@Composable
fun GramaKhataNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController)
        }
        composable(
            route = Screen.CustomerDetails.route,
            arguments = listOf(navArgument("customerId") { type = NavType.IntType })
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getInt("customerId") ?: 0
            CustomerDetailsScreen(navController, customerId)
        }
        composable(
            route = Screen.AddTransaction.route,
            arguments = listOf(
                navArgument("customerId") { type = NavType.IntType },
                navArgument("type") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getInt("customerId") ?: 0
            val type = backStackEntry.arguments?.getString("type") ?: "GIVE"
            AddTransactionScreen(navController, customerId, type)
        }
        composable(Screen.AddCustomer.route) {
            AddCustomerScreen(navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController)
        }
        composable(Screen.LanguageSelection.route) {
            LanguageSelectionScreen(navController)
        }
        composable(Screen.HelpSupport.route) {
            HelpSupportScreen(navController)
        }
        composable(Screen.About.route) {
            AboutScreen(navController)
        }
        composable(Screen.AppPreferences.route) {
            AppPreferencesScreen(navController)
        }
        composable(Screen.Reports.route) {
            ReportsScreen(navController)
        }
        composable(Screen.Reminders.route) {
            RemindersScreen(navController)
        }
    }
}
