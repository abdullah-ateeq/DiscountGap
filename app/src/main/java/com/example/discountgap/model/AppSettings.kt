package com.example.discountgap.model

enum class AppThemeMode(val displayName: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

data class AppSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val hapticFeedback: Boolean = true,
    val animationsEnabled: Boolean = true,
    val defaultCurrency: CurrencyOption = CurrencyOption(
        code = "USD",
        symbol = "$",
        name = "US Dollar",
        decimalPlaces = 2
    ),
    val confirmHistoryDelete: Boolean = true,
    val preserveLastComparison: Boolean = true
)
