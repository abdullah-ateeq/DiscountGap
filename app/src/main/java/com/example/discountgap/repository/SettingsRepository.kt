package com.example.discountgap.repository

import android.content.SharedPreferences
import com.example.discountgap.data.InMemoryPreferencesDataSource
import com.example.discountgap.data.PreferencesDataSource
import com.example.discountgap.data.SharedPreferencesDataSource
import com.example.discountgap.domain.CurrencyRepository
import com.example.discountgap.model.AppSettings
import com.example.discountgap.model.AppThemeMode
import com.example.discountgap.model.CurrencyOption

class SettingsRepository(
    private val dataSource: PreferencesDataSource = InMemoryPreferencesDataSource()
) {
    constructor(sharedPreferences: SharedPreferences) : this(
        SharedPreferencesDataSource(sharedPreferences)
    )

    data class SavedComparisonState(
        val originalInput: String,
        val discountedInput: String,
        val currency: CurrencyOption
    )

    companion object {
        const val DEFAULT_ORIGINAL_INPUT = "100.00"
        const val DEFAULT_DISCOUNTED_INPUT = "75.00"

        private const val KEY_THEME_MODE = "settings_theme_mode"
        private const val KEY_DYNAMIC_COLOR = "settings_dynamic_color"
        private const val KEY_HAPTIC_FEEDBACK = "settings_haptic_feedback"
        private const val KEY_ANIMATIONS_ENABLED = "settings_animations_enabled"
        private const val KEY_DEFAULT_CURRENCY_CODE = "settings_default_currency_code"
        private const val KEY_DEFAULT_CURRENCY_SYMBOL = "settings_default_currency_symbol"
        private const val KEY_DEFAULT_CURRENCY_NAME = "settings_default_currency_name"
        private const val KEY_DEFAULT_CURRENCY_DECIMALS = "settings_default_currency_decimals"
        private const val KEY_CONFIRM_HISTORY_DELETE = "settings_confirm_history_delete"
        private const val KEY_PRESERVE_LAST_COMPARISON = "settings_preserve_last_comparison"

        private const val KEY_LAST_ORIGINAL_INPUT = "last_original_input"
        private const val KEY_LAST_DISCOUNTED_INPUT = "last_discounted_input"
        private const val KEY_LAST_CURRENCY_CODE = "last_currency_code"
        private const val KEY_LAST_CURRENCY_SYMBOL = "last_currency_symbol"
        private const val KEY_LAST_CURRENCY_NAME = "last_currency_name"
        private const val KEY_LAST_CURRENCY_DECIMALS = "last_currency_decimals"
    }

    fun loadSettings(): AppSettings {
        val themeName = dataSource.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name)
        val themeMode = AppThemeMode.entries.firstOrNull { it.name == themeName } ?: AppThemeMode.SYSTEM
        val dynamicColor = dataSource.getBoolean(KEY_DYNAMIC_COLOR, true)
        val hapticFeedback = dataSource.getBoolean(KEY_HAPTIC_FEEDBACK, true)
        val animationsEnabled = dataSource.getBoolean(KEY_ANIMATIONS_ENABLED, true)
        val confirmHistoryDelete = dataSource.getBoolean(KEY_CONFIRM_HISTORY_DELETE, true)
        val preserveLastComparison = dataSource.getBoolean(KEY_PRESERVE_LAST_COMPARISON, true)

        val defaultCurrency = loadCurrency(
            codeKey = KEY_DEFAULT_CURRENCY_CODE,
            symbolKey = KEY_DEFAULT_CURRENCY_SYMBOL,
            nameKey = KEY_DEFAULT_CURRENCY_NAME,
            decimalsKey = KEY_DEFAULT_CURRENCY_DECIMALS,
            fallback = CurrencyRepository.USD
        )

        return AppSettings(
            themeMode = themeMode,
            dynamicColor = dynamicColor,
            hapticFeedback = hapticFeedback,
            animationsEnabled = animationsEnabled,
            defaultCurrency = defaultCurrency,
            confirmHistoryDelete = confirmHistoryDelete,
            preserveLastComparison = preserveLastComparison
        )
    }

    fun saveSettings(settings: AppSettings) {
        dataSource.putString(KEY_THEME_MODE, settings.themeMode.name)
        dataSource.putBoolean(KEY_DYNAMIC_COLOR, settings.dynamicColor)
        dataSource.putBoolean(KEY_HAPTIC_FEEDBACK, settings.hapticFeedback)
        dataSource.putBoolean(KEY_ANIMATIONS_ENABLED, settings.animationsEnabled)
        dataSource.putBoolean(KEY_CONFIRM_HISTORY_DELETE, settings.confirmHistoryDelete)
        dataSource.putBoolean(KEY_PRESERVE_LAST_COMPARISON, settings.preserveLastComparison)

        saveCurrency(
            currency = settings.defaultCurrency,
            codeKey = KEY_DEFAULT_CURRENCY_CODE,
            symbolKey = KEY_DEFAULT_CURRENCY_SYMBOL,
            nameKey = KEY_DEFAULT_CURRENCY_NAME,
            decimalsKey = KEY_DEFAULT_CURRENCY_DECIMALS
        )
    }

    fun setThemeMode(themeMode: AppThemeMode): AppSettings {
        val updated = loadSettings().copy(themeMode = themeMode)
        saveSettings(updated)
        return updated
    }

    fun setDynamicColor(enabled: Boolean): AppSettings {
        val updated = loadSettings().copy(dynamicColor = enabled)
        saveSettings(updated)
        return updated
    }

    fun setHapticFeedback(enabled: Boolean): AppSettings {
        val updated = loadSettings().copy(hapticFeedback = enabled)
        saveSettings(updated)
        return updated
    }

    fun setAnimationsEnabled(enabled: Boolean): AppSettings {
        val updated = loadSettings().copy(animationsEnabled = enabled)
        saveSettings(updated)
        return updated
    }

    fun setDefaultCurrency(currency: CurrencyOption): AppSettings {
        val updated = loadSettings().copy(defaultCurrency = currency)
        saveSettings(updated)
        return updated
    }

    fun setConfirmHistoryDelete(enabled: Boolean): AppSettings {
        val updated = loadSettings().copy(confirmHistoryDelete = enabled)
        saveSettings(updated)
        return updated
    }

    fun setPreserveLastComparison(enabled: Boolean): AppSettings {
        val updated = loadSettings().copy(preserveLastComparison = enabled)
        saveSettings(updated)
        return updated
    }

    fun resetSettings(): AppSettings {
        val defaults = AppSettings()
        saveSettings(defaults)
        return defaults
    }

    fun saveLastComparison(
        originalInput: String,
        discountedInput: String,
        currency: CurrencyOption
    ) {
        dataSource.putString(KEY_LAST_ORIGINAL_INPUT, originalInput)
        dataSource.putString(KEY_LAST_DISCOUNTED_INPUT, discountedInput)
        saveCurrency(
            currency = currency,
            codeKey = KEY_LAST_CURRENCY_CODE,
            symbolKey = KEY_LAST_CURRENCY_SYMBOL,
            nameKey = KEY_LAST_CURRENCY_NAME,
            decimalsKey = KEY_LAST_CURRENCY_DECIMALS
        )
    }

    fun loadLastComparison(): SavedComparisonState? {
        val original = dataSource.getString(KEY_LAST_ORIGINAL_INPUT, null) ?: return null
        val discounted = dataSource.getString(KEY_LAST_DISCOUNTED_INPUT, null) ?: return null
        val settings = loadSettings()
        val currency = loadCurrency(
            codeKey = KEY_LAST_CURRENCY_CODE,
            symbolKey = KEY_LAST_CURRENCY_SYMBOL,
            nameKey = KEY_LAST_CURRENCY_NAME,
            decimalsKey = KEY_LAST_CURRENCY_DECIMALS,
            fallback = settings.defaultCurrency
        )
        return SavedComparisonState(
            originalInput = original,
            discountedInput = discounted,
            currency = currency
        )
    }

    private fun saveCurrency(
        currency: CurrencyOption,
        codeKey: String,
        symbolKey: String,
        nameKey: String,
        decimalsKey: String
    ) {
        dataSource.putString(codeKey, currency.code)
        dataSource.putString(symbolKey, currency.symbol)
        dataSource.putString(nameKey, currency.name)
        dataSource.putInt(decimalsKey, currency.decimalPlaces)
    }

    private fun loadCurrency(
        codeKey: String,
        symbolKey: String,
        nameKey: String,
        decimalsKey: String,
        fallback: CurrencyOption
    ): CurrencyOption {
        val code = dataSource.getString(codeKey, null) ?: return fallback
        val builtIn = CurrencyRepository.findByCode(code)
        val symbol = dataSource.getString(symbolKey, builtIn?.symbol ?: fallback.symbol)
            ?: (builtIn?.symbol ?: fallback.symbol)
        val name = dataSource.getString(nameKey, builtIn?.name ?: fallback.name)
            ?: (builtIn?.name ?: fallback.name)
        val decimals = dataSource.getInt(decimalsKey, builtIn?.decimalPlaces ?: fallback.decimalPlaces)

        if (builtIn != null &&
            builtIn.symbol == symbol &&
            builtIn.decimalPlaces == decimals
        ) {
            return builtIn
        }
        return CurrencyRepository.createCustomCurrency(
            code = code,
            symbol = symbol,
            name = name,
            decimalPlaces = decimals
        )
    }
}
