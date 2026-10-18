package com.example.discountgap.domain

import com.example.discountgap.model.CurrencyOption
import com.example.discountgap.model.Money
import java.math.BigInteger

object MoneyParser {

    sealed interface ParseResult {
        data class Success(val money: Money) : ParseResult
        data class Error(val reason: ParseErrorReason, val message: String) : ParseResult
    }

    enum class ParseErrorReason {
        EMPTY,
        NEGATIVE,
        INVALID_FORMAT,
        PRECISION_EXCEEDED,
        OVERFLOW
    }

    private val VALID_NUMBER_REGEX = Regex("""^\d+(?:\.\d*)?$|^\.\d+$""")
    private val MAX_LONG_BIG_INT = BigInteger.valueOf(Long.MAX_VALUE)

    fun parse(
        rawInput: String,
        currency: CurrencyOption,
        isOriginalField: Boolean = true
    ): ParseResult {
        if (!DiscountGapValidator.isValidCurrency(currency)) {
            return ParseResult.Error(
                ParseErrorReason.PRECISION_EXCEEDED,
                DiscountGapValidator.ERROR_CURRENCY_PRECISION
            )
        }

        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) {
            return ParseResult.Error(
                ParseErrorReason.EMPTY,
                if (isOriginalField) "Enter an original price." else "Enter a discounted price."
            )
        }

        // Strip currency symbol if present at start, and strip grouping commas/spaces
        val withoutSymbol = if (currency.symbol.isNotEmpty() && trimmed.startsWith(currency.symbol)) {
            trimmed.removePrefix(currency.symbol).trim()
        } else if (trimmed.startsWith("$") || trimmed.startsWith("€") || trimmed.startsWith("£") || trimmed.startsWith("¥") || trimmed.startsWith("₹")) {
            trimmed.drop(1).trim()
        } else if (trimmed.startsWith("-") && trimmed.length > 1) {
            trimmed
        } else {
            trimmed
        }

        val cleaned = withoutSymbol.replace(",", "").replace(" ", "")
        if (cleaned.isEmpty()) {
            return ParseResult.Error(
                ParseErrorReason.EMPTY,
                if (isOriginalField) "Enter an original price." else "Enter a discounted price."
            )
        }

        if (cleaned.startsWith("-")) {
            val numberPart = cleaned.removePrefix("-")
            if (numberPart.isNotEmpty() && VALID_NUMBER_REGEX.matches(numberPart)) {
                return ParseResult.Error(
                    ParseErrorReason.NEGATIVE,
                    if (isOriginalField) {
                        DiscountGapValidator.ERROR_NEGATIVE_ORIGINAL
                    } else {
                        DiscountGapValidator.ERROR_NEGATIVE_DISCOUNTED
                    }
                )
            }
            return ParseResult.Error(
                ParseErrorReason.NEGATIVE,
                if (isOriginalField) {
                    DiscountGapValidator.ERROR_NEGATIVE_ORIGINAL
                } else {
                    DiscountGapValidator.ERROR_NEGATIVE_DISCOUNTED
                }
            )
        }

        val normalized = if (cleaned.startsWith("+")) cleaned.removePrefix("+") else cleaned
        if (!VALID_NUMBER_REGEX.matches(normalized)) {
            return ParseResult.Error(
                ParseErrorReason.INVALID_FORMAT,
                if (isOriginalField) "Enter a valid original price." else "Enter a valid discounted price."
            )
        }

        val parts = normalized.split(".")
        val wholePartStr = parts.getOrNull(0)?.ifEmpty { "0" } ?: "0"
        val decimalPartStr = parts.getOrNull(1) ?: ""

        // If the input contains a decimal point when currency has 0 decimals, or has more decimal digits than currency supports
        if (parts.size > 1 && currency.decimalPlaces == 0) {
            // Even if someone entered "100.5" or "100.00" in a 0-decimal currency like JPY, check precision
            return ParseResult.Error(
                ParseErrorReason.PRECISION_EXCEEDED,
                DiscountGapValidator.ERROR_CURRENCY_PRECISION
            )
        }

        if (decimalPartStr.length > currency.decimalPlaces) {
            return ParseResult.Error(
                ParseErrorReason.PRECISION_EXCEEDED,
                DiscountGapValidator.ERROR_CURRENCY_PRECISION
            )
        }

        val paddedDecimals = decimalPartStr.padEnd(currency.decimalPlaces, '0')
        return try {
            val scaleFactor = BigInteger.TEN.pow(currency.decimalPlaces)
            val wholeBigInt = BigInteger(wholePartStr)
            val fracBigInt = if (paddedDecimals.isEmpty()) BigInteger.ZERO else BigInteger(paddedDecimals)
            val totalMinorBigInt = wholeBigInt.multiply(scaleFactor).add(fracBigInt)

            if (totalMinorBigInt > MAX_LONG_BIG_INT || totalMinorBigInt < BigInteger.ZERO) {
                ParseResult.Error(
                    ParseErrorReason.OVERFLOW,
                    DiscountGapValidator.ERROR_OVERFLOW
                )
            } else {
                val minorUnits = totalMinorBigInt.longValueExact()
                ParseResult.Success(Money(minorUnits = minorUnits, currency = currency))
            }
        } catch (e: NumberFormatException) {
            ParseResult.Error(
                ParseErrorReason.INVALID_FORMAT,
                if (isOriginalField) "Enter a valid original price." else "Enter a valid discounted price."
            )
        } catch (e: ArithmeticException) {
            ParseResult.Error(
                ParseErrorReason.OVERFLOW,
                DiscountGapValidator.ERROR_OVERFLOW
            )
        }
    }

    fun parseToMinorUnitsOrNull(rawInput: String, currency: CurrencyOption): Long? {
        return when (val result = parse(rawInput, currency)) {
            is ParseResult.Success -> result.money.minorUnits
            is ParseResult.Error -> null
        }
    }
}
