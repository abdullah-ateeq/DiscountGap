package com.example.discountgap.model

data class DiscountGapUiState(
    val originalAmountInput: String,
    val discountedAmountInput: String,
    val currency: CurrencyOption,
    val result: DiscountGapResult?,
    val history: List<DiscountGapHistory>,
    val errorMessage: String?,
    val isCalculating: Boolean,
    val calculationState: CalculationState = when {
        errorMessage != null -> CalculationState.Error(errorMessage)
        result != null -> CalculationState.Calculated
        else -> CalculationState.Idle
    },
    val settings: AppSettings = AppSettings(),
    val selectedPresetPercent: Int? = null
)
