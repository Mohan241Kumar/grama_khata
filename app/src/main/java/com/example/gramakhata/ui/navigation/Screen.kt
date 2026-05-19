package com.example.gramakhata.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object CustomerDetails : Screen("customer_details/{customerId}") {
        fun createRoute(customerId: Int) = "customer_details/$customerId"
    }
    object AddTransaction : Screen("add_transaction/{customerId}/{type}") {
        fun createRoute(customerId: Int, type: String) = "add_transaction/$customerId/$type"
    }
    object AddCustomer : Screen("add_customer")
    object Settings : Screen("settings")
    object LanguageSelection : Screen("language_selection")
    object HelpSupport : Screen("help_support")
    object About : Screen("about")
    object AppPreferences : Screen("app_preferences")
    object Reports : Screen("reports")
    object Reminders : Screen("reminders")
}
