package com.example.discountgap.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.discountgap.domain.CurrencyRepository
import com.example.discountgap.domain.DiscountGapCalculator
import com.example.discountgap.domain.DiscountGapFormatter
import com.example.discountgap.domain.DiscountGapValidator
import com.example.discountgap.domain.MoneyFormatter
import com.example.discountgap.domain.MoneyParser
import com.example.discountgap.model.AppSettings
import com.example.discountgap.model.AppThemeMode
import com.example.discountgap.model.CalculationState
import com.example.discountgap.model.CurrencyOption
import com.example.discountgap.model.DiscountGapHistory
import com.example.discountgap.model.DiscountGapUiState
import com.example.discountgap.repository.DiscountGapHistoryRepository
import com.example.discountgap.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DiscountGapViewModel(
    private val historyRepository: DiscountGapHistoryRepository = DiscountGapHistoryRepository(),
    private val settingsRepository: SettingsRepository = SettingsRepository()
) : ViewModel() {

    companion object {
        val QUICK_ORIGINAL_PRESETS = listOf(10L, 50L, 100L, 500L, 1000L)
        val QUICK_DISCOUNT_PRESETS = listOf(5, 10, 15, 20, 25, 50)
        private const val DEFAULT_ORIGINAL_MAJOR = 100L
        private const val DEFAULT_DISCOUNTED_MAJOR = 75L

        fun provideFactory(context: Context): ViewModelProvider.Factory {
            val appContext = context.applicationContext
            val prefs = appContext.getSharedPreferences("discount_gap_prefs", Context.MODE_PRIVATE)
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DiscountGapViewModel(
                        historyRepository = DiscountGapHistoryRepository(prefs),
                        settingsRepository = SettingsRepository(prefs)
                    ) as T
                }
            }
        }
    }

    private val _uiState: MutableStateFlow<DiscountGapUiState>
    val uiState: StateFlow<DiscountGapUiState>

    init {
        val loadedSettings = settingsRepository.loadSettings()
        val loadedHistory = historyRepository.loadHistory()
        val savedComparison = if (loadedSettings.preserveLastComparison) {
            settingsRepository.loadLastComparison()
        } else {
            null
        }

        val initialCurrency = savedComparison?.currency ?: loadedSettings.defaultCurrency
        val defaultOrigMinor = DiscountGapCalculator.majorUnitsToMinorUnits(DEFAULT_ORIGINAL_MAJOR, initialCurrency)
        val defaultDiscMinor = DiscountGapCalculator.majorUnitsToMinorUnits(DEFAULT_DISCOUNTED_MAJOR, initialCurrency)

        val initialOriginalInput = savedComparison?.originalInput
            ?: MoneyFormatter.formatPlainInput(defaultOrigMinor, initialCurrency)
        val initialDiscountedInput = savedComparison?.discountedInput
            ?: MoneyFormatter.formatPlainInput(defaultDiscMinor, initialCurrency)

        val initialState = evaluateState(
            originalInput = initialOriginalInput,
            discountedInput = initialDiscountedInput,
            currency = initialCurrency,
            history = loadedHistory,
            settings = loadedSettings,
            selectedPresetPercent = null
        )

        _uiState = MutableStateFlow(initialState)
        uiState = _uiState.asStateFlow()
    }

    fun onOriginalPriceChanged(newInput: String) {
        updateComparisonInputs(
            originalInput = newInput,
            discountedInput = _uiState.value.discountedAmountInput,
            currency = _uiState.value.currency,
            selectedPresetPercent = null
        )
    }

    fun onDiscountedPriceChanged(newInput: String) {
        updateComparisonInputs(
            originalInput = _uiState.value.originalAmountInput,
            discountedInput = newInput,
            currency = _uiState.value.currency,
            selectedPresetPercent = null
        )
    }

    fun onCurrencySelected(newCurrency: CurrencyOption) {
        updateComparisonInputs(
            originalInput = _uiState.value.originalAmountInput,
            discountedInput = _uiState.value.discountedAmountInput,
            currency = newCurrency,
            selectedPresetPercent = _uiState.value.selectedPresetPercent
        )
    }

    fun onCustomCurrencyCreated(
        code: String,
        symbol: String,
        name: String,
        decimalPlaces: Int
    ) {
        val custom = CurrencyRepository.createCustomCurrency(
            code = code,
            symbol = symbol,
            name = name,
            decimalPlaces = decimalPlaces
        )
        onCurrencySelected(custom)
    }

    fun onQuickOriginalPricePreset(majorAmount: Long) {
        val currentCurrency = _uiState.value.currency
        val minorUnits = try {
            DiscountGapCalculator.majorUnitsToMinorUnits(majorAmount, currentCurrency)
        } catch (e: IllegalArgumentException) {
            return
        } catch (e: ArithmeticException) {
            return
        }
        val formattedInput = MoneyFormatter.formatPlainInput(minorUnits, currentCurrency)
        updateComparisonInputs(
            originalInput = formattedInput,
            discountedInput = _uiState.value.discountedAmountInput,
            currency = currentCurrency,
            selectedPresetPercent = null
        )
    }

    fun onQuickDiscountPreset(discountPercent: Int) {
        val currentState = _uiState.value
        val currency = currentState.currency

        val parsedOriginal = MoneyParser.parse(
            rawInput = currentState.originalAmountInput,
            currency = currency,
            isOriginalField = true
        )

        when (parsedOriginal) {
            is MoneyParser.ParseResult.Success -> {
                val discountedMinorUnits = DiscountGapCalculator.calculateDiscountedMinorUnitsFromPreset(
                    originalAmountMinorUnits = parsedOriginal.money.minorUnits,
                    discountPercent = discountPercent
                )
                val formattedDiscounted = MoneyFormatter.formatPlainInput(discountedMinorUnits, currency)
                updateComparisonInputs(
                    originalInput = currentState.originalAmountInput,
                    discountedInput = formattedDiscounted,
                    currency = currency,
                    selectedPresetPercent = discountPercent
                )
            }
            is MoneyParser.ParseResult.Error -> {
                _uiState.update { state ->
                    state.copy(
                        result = null,
                        errorMessage = parsedOriginal.message,
                        isCalculating = false,
                        calculationState = CalculationState.Error(parsedOriginal.message),
                        selectedPresetPercent = null
                    )
                }
            }
        }
    }

    fun comparePrices() {
        val current = _uiState.value
        updateComparisonInputs(
            originalInput = current.originalAmountInput,
            discountedInput = current.discountedAmountInput,
            currency = current.currency,
            selectedPresetPercent = current.selectedPresetPercent
        )
    }

    fun saveToHistory(): Boolean {
        val currentResult = _uiState.value.result ?: return false
        val updatedHistory = historyRepository.saveCalculation(currentResult)
        _uiState.update { state ->
            state.copy(history = updatedHistory)
        }
        return true
    }

    fun deleteHistoryItem(id: String) {
        val updatedHistory = historyRepository.deleteCalculation(id)
        _uiState.update { state ->
            state.copy(history = updatedHistory)
        }
    }

    fun clearHistory() {
        val updatedHistory = historyRepository.clearHistory()
        _uiState.update { state ->
            state.copy(history = updatedHistory)
        }
    }

    fun restoreFromHistory(item: DiscountGapHistory) {
        val restoredCurrency = historyRepository.resolveCurrencyForHistory(item)
        val restoredOriginal = MoneyFormatter.formatPlainInput(
            minorUnits = item.originalAmountMinorUnits,
            currency = restoredCurrency
        )
        val restoredDiscounted = MoneyFormatter.formatPlainInput(
            minorUnits = item.discountedAmountMinorUnits,
            currency = restoredCurrency
        )
        updateComparisonInputs(
            originalInput = restoredOriginal,
            discountedInput = restoredDiscounted,
            currency = restoredCurrency,
            selectedPresetPercent = null
        )
    }

    fun reset() {
        val resetCurrency = CurrencyRepository.USD
        val origMinor = DiscountGapCalculator.majorUnitsToMinorUnits(DEFAULT_ORIGINAL_MAJOR, resetCurrency)
        val discMinor = DiscountGapCalculator.majorUnitsToMinorUnits(DEFAULT_DISCOUNTED_MAJOR, resetCurrency)
        val origInput = MoneyFormatter.formatPlainInput(origMinor, resetCurrency)
        val discInput = MoneyFormatter.formatPlainInput(discMinor, resetCurrency)

        updateComparisonInputs(
            originalInput = origInput,
            discountedInput = discInput,
            currency = resetCurrency,
            selectedPresetPercent = null
        )
    }

    fun getCopyText(): String? {
        val result = _uiState.value.result ?: return null
        return DiscountGapFormatter.formatCopyText(result)
    }

    fun getShareText(): String? {
        val result = _uiState.value.result ?: return null
        return DiscountGapFormatter.formatShareText(result)
    }

    // Settings updates
    fun updateThemeMode(themeMode: AppThemeMode) {
        val updated = settingsRepository.setThemeMode(themeMode)
        _uiState.update { it.copy(settings = updated) }
    }

    fun updateDynamicColor(enabled: Boolean) {
        val updated = settingsRepository.setDynamicColor(enabled)
        _uiState.update { it.copy(settings = updated) }
    }

    fun updateHapticFeedback(enabled: Boolean) {
        val updated = settingsRepository.setHapticFeedback(enabled)
        _uiState.update { it.copy(settings = updated) }
    }

    fun updateAnimationsEnabled(enabled: Boolean) {
        val updated = settingsRepository.setAnimationsEnabled(enabled)
        _uiState.update { it.copy(settings = updated) }
    }

    fun updateDefaultCurrency(currency: CurrencyOption) {
        val updated = settingsRepository.setDefaultCurrency(currency)
        _uiState.update { it.copy(settings = updated) }
        onCurrencySelected(currency)
    }

    fun updateConfirmHistoryDelete(enabled: Boolean) {
        val updated = settingsRepository.setConfirmHistoryDelete(enabled)
        _uiState.update { it.copy(settings = updated) }
    }

    fun updatePreserveLastComparison(enabled: Boolean) {
        val updated = settingsRepository.setPreserveLastComparison(enabled)
        _uiState.update { it.copy(settings = updated) }
        if (enabled) {
            val current = _uiState.value
            settingsRepository.saveLastComparison(
                originalInput = current.originalAmountInput,
                discountedInput = current.discountedAmountInput,
                currency = current.currency
            )
        }
    }

    fun resetSettings() {
        val defaults = settingsRepository.resetSettings()
        _uiState.update { it.copy(settings = defaults) }
    }

    private fun updateComparisonInputs(
        originalInput: String,
        discountedInput: String,
        currency: CurrencyOption,
        selectedPresetPercent: Int?
    ) {
        val currentSettings = _uiState.value.settings
        val currentHistory = _uiState.value.history

        val nextState = evaluateState(
            originalInput = originalInput,
            discountedInput = discountedInput,
            currency = currency,
            history = currentHistory,
            settings = currentSettings,
            selectedPresetPercent = selectedPresetPercent
        )

        _uiState.value = nextState

        if (currentSettings.preserveLastComparison) {
            settingsRepository.saveLastComparison(
                originalInput = originalInput,
                discountedInput = discountedInput,
                currency = currency
            )
        }
    }

    private fun evaluateState(
        originalInput: String,
        discountedInput: String,
        currency: CurrencyOption,
        history: List<DiscountGapHistory>,
        settings: AppSettings,
        selectedPresetPercent: Int?
    ): DiscountGapUiState {
        // When both inputs are completely empty, stay in Idle state without error
        if (originalInput.isBlank() && discountedInput.isBlank()) {
            return DiscountGapUiState(
                originalAmountInput = originalInput,
                discountedAmountInput = discountedInput,
                currency = currency,
                result = null,
                history = history,
                errorMessage = null,
                isCalculating = false,
                calculationState = CalculationState.Idle,
                settings = settings,
                selectedPresetPercent = null
            )
        }

        return when (
            val validation = DiscountGapValidator.validateInputs(
                originalInput = originalInput,
                discountedInput = discountedInput,
                currency = currency
            )
        ) {
            is DiscountGapValidator.ValidationResult.Valid -> {
                try {
                    val result = DiscountGapCalculator.calculate(
                        originalAmountMinorUnits = validation.original.minorUnits,
                        discountedAmountMinorUnits = validation.discounted.minorUnits,
                        currency = currency
                    )
                    DiscountGapUiState(
                        originalAmountInput = originalInput,
                        discountedAmountInput = discountedInput,
                        currency = currency,
                        result = result,
                        history = history,
                        errorMessage = null,
                        isCalculating = false,
                        calculationState = CalculationState.Calculated,
                        settings = settings,
                        selectedPresetPercent = selectedPresetPercent
                    )
                } catch (e: IllegalArgumentException) {
                    val msg = e.message ?: DiscountGapValidator.ERROR_CURRENCY_PRECISION
                    DiscountGapUiState(
                        originalAmountInput = originalInput,
                        discountedAmountInput = discountedInput,
                        currency = currency,
                        result = null,
                        history = history,
                        errorMessage = msg,
                        isCalculating = false,
                        calculationState = CalculationState.Error(msg),
                        settings = settings,
                        selectedPresetPercent = null
                    )
                } catch (e: ArithmeticException) {
                    val msg = DiscountGapValidator.ERROR_OVERFLOW
                    DiscountGapUiState(
                        originalAmountInput = originalInput,
                        discountedAmountInput = discountedInput,
                        currency = currency,
                        result = null,
                        history = history,
                        errorMessage = msg,
                        isCalculating = false,
                        calculationState = CalculationState.Error(msg),
                        settings = settings,
                        selectedPresetPercent = null
                    )
                }
            }
            is DiscountGapValidator.ValidationResult.Invalid -> {
                DiscountGapUiState(
                    originalAmountInput = originalInput,
                    discountedAmountInput = discountedInput,
                    currency = currency,
                    result = null,
                    history = history,
                    errorMessage = validation.message,
                    isCalculating = false,
                    calculationState = CalculationState.Error(validation.message),
                    settings = settings,
                    selectedPresetPercent = null
                )
            }
        }
    }
}
