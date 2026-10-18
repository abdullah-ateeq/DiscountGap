package com.example.discountgap.domain

import com.example.discountgap.model.CurrencyOption
import com.example.discountgap.model.Money

object DiscountGapValidator {

    const val ERROR_NEGATIVE_ORIGINAL = "Original price cannot be negative."
    const val ERROR_NEGATIVE_DISCOUNTED = "Discounted price cannot be negative."
    const val ERROR_DISCOUNTED_ABOVE_ORIGINAL = "Discounted price cannot be greater than the original price."
    const val ERROR_CURRENCY_PRECISION = "Review the prices for the selected currency."
    const val ERROR_OVERFLOW = "Amount is too large."

    const val MIN_CUSTOM_DECIMALS = 0
    const val MAX_CUSTOM_DECIMALS = 4

    sealed interface ValidationResult {
        data class Valid(
            val original: Money,
            val discounted: Money
        ) : ValidationResult

        data class Invalid(
            val message: String
        ) : ValidationResult
    }

    fun isValidCurrency(currency: CurrencyOption): Boolean {
        return currency.code.isNotBlank() &&
            currency.symbol.isNotBlank() &&
            currency.decimalPlaces in MIN_CUSTOM_DECIMALS..MAX_CUSTOM_DECIMALS
    }

    fun validateMinorUnits(
        originalAmountMinorUnits: Long,
        discountedAmountMinorUnits: Long,
        currency: CurrencyOption
    ): ValidationResult {
        if (!isValidCurrency(currency)) {
            return ValidationResult.Invalid(ERROR_CURRENCY_PRECISION)
        }
        if (originalAmountMinorUnits < 0L) {
            return ValidationResult.Invalid(ERROR_NEGATIVE_ORIGINAL)
        }
        if (discountedAmountMinorUnits < 0L) {
            return ValidationResult.Invalid(ERROR_NEGATIVE_DISCOUNTED)
        }
        if (discountedAmountMinorUnits > originalAmountMinorUnits) {
            return ValidationResult.Invalid(ERROR_DISCOUNTED_ABOVE_ORIGINAL)
        }
        return ValidationResult.Valid(
            original = Money(originalAmountMinorUnits, currency),
            discounted = Money(discountedAmountMinorUnits, currency)
        )
    }

    fun validateInputs(
        originalInput: String,
        discountedInput: String,
        currency: CurrencyOption
    ): ValidationResult {
        if (!isValidCurrency(currency)) {
            return ValidationResult.Invalid(ERROR_CURRENCY_PRECISION)
        }

        val originalParsed = MoneyParser.parse(
            rawInput = originalInput,
            currency = currency,
            isOriginalField = true
        )
        val discountedParsed = MoneyParser.parse(
            rawInput = discountedInput,
            currency = currency,
            isOriginalField = false
        )

        // Check currency precision errors first when switching currencies
        if (originalParsed is MoneyParser.ParseResult.Error &&
            originalParsed.reason == MoneyParser.ParseErrorReason.PRECISION_EXCEEDED
        ) {
            return ValidationResult.Invalid(ERROR_CURRENCY_PRECISION)
        }
        if (discountedParsed is MoneyParser.ParseResult.Error &&
            discountedParsed.reason == MoneyParser.ParseErrorReason.PRECISION_EXCEEDED
        ) {
            return ValidationResult.Invalid(ERROR_CURRENCY_PRECISION)
        }

        // Check negative errors
        if (originalParsed is MoneyParser.ParseResult.Error &&
            originalParsed.reason == MoneyParser.ParseErrorReason.NEGATIVE
        ) {
            return ValidationResult.Invalid(ERROR_NEGATIVE_ORIGINAL)
        }
        if (discountedParsed is MoneyParser.ParseResult.Error &&
            discountedParsed.reason == MoneyParser.ParseErrorReason.NEGATIVE
        ) {
            return ValidationResult.Invalid(ERROR_NEGATIVE_DISCOUNTED)
        }

        // Check overflow errors
        if (originalParsed is MoneyParser.ParseResult.Error &&
            originalParsed.reason == MoneyParser.ParseErrorReason.OVERFLOW
        ) {
            return ValidationResult.Invalid(ERROR_OVERFLOW)
        }
        if (discountedParsed is MoneyParser.ParseResult.Error &&
            discountedParsed.reason == MoneyParser.ParseErrorReason.OVERFLOW
        ) {
            return ValidationResult.Invalid(ERROR_OVERFLOW)
        }

        if (originalParsed is MoneyParser.ParseResult.Error) {
            return ValidationResult.Invalid(originalParsed.message)
        }
        if (discountedParsed is MoneyParser.ParseResult.Error) {
            return ValidationResult.Invalid(discountedParsed.message)
        }

        val originalMoney = (originalParsed as MoneyParser.ParseResult.Success).money
        val discountedMoney = (discountedParsed as MoneyParser.ParseResult.Success).money

        return validateMinorUnits(
            originalAmountMinorUnits = originalMoney.minorUnits,
            discountedAmountMinorUnits = discountedMoney.minorUnits,
            currency = currency
        )
    }

    fun hasCompatiblePrecision(rawInput: String, currency: CurrencyOption): Boolean {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) return true
        val parseResult = MoneyParser.parse(trimmed, currency)
        return !(parseResult is MoneyParser.ParseResult.Error &&
            parseResult.reason == MoneyParser.ParseErrorReason.PRECISION_EXCEEDED)
    }
}
