package com.example.discountgap.domain

import com.example.discountgap.model.CurrencyOption
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object MoneyFormatter {

    private val US_SYMBOLS = DecimalFormatSymbols(Locale.US)

    fun format(
        minorUnits: Long,
        currency: CurrencyOption
    ): String {
        val isNegative = minorUnits < 0L
        val absValue = BigDecimal.valueOf(minorUnits).abs().movePointLeft(currency.decimalPlaces)

        val pattern = if (currency.decimalPlaces > 0) {
            "#,##0." + "0".repeat(currency.decimalPlaces)
        } else {
            "#,##0"
        }

        val decimalFormat = DecimalFormat(pattern, US_SYMBOLS).apply {
            isGroupingUsed = true
        }
        val formattedNumber = decimalFormat.format(absValue)
        val formattedPrefix = formatSymbolPrefix(currency.symbol)
        return if (isNegative) {
            "-$formattedPrefix$formattedNumber"
        } else {
            "$formattedPrefix$formattedNumber"
        }
    }

    fun formatOriginalPrice(
        minorUnits: Long,
        currency: CurrencyOption
    ): String = format(minorUnits, currency)

    fun formatDiscountedPrice(
        minorUnits: Long,
        currency: CurrencyOption
    ): String = format(minorUnits, currency)

    fun formatSavings(
        minorUnits: Long,
        currency: CurrencyOption
    ): String = format(minorUnits, currency)

    /**
     * Formats minor units as an editable plain decimal number without symbol or thousands separators
     * (e.g., 10000 in USD -> "100.00", 1000 in JPY -> "1000").
     */
    fun formatPlainInput(
        minorUnits: Long,
        currency: CurrencyOption
    ): String {
        val value = BigDecimal.valueOf(minorUnits).movePointLeft(currency.decimalPlaces)
        return value.toPlainString()
    }

    /**
     * Adds a space after alphabetic/abbreviated currency symbols like "Rs.", "AED", "SAR",
     * while keeping symbols like "$", "€", "£", "¥", "₹", "CA$", "A$" directly adjacent.
     */
    private fun formatSymbolPrefix(rawSymbol: String): String {
        val trimmed = rawSymbol.trim()
        if (trimmed.isEmpty()) return ""
        val lastChar = trimmed.last()
        val needsSpace = lastChar == '.' || lastChar.isLetter()
        return if (needsSpace) "$trimmed " else trimmed
    }
}
