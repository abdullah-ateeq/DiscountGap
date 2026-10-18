package com.example.discountgap.domain

import com.example.discountgap.model.CurrencyOption

object CurrencyRepository {

    val USD = CurrencyOption(code = "USD", symbol = "$", name = "US Dollar", decimalPlaces = 2)
    val EUR = CurrencyOption(code = "EUR", symbol = "€", name = "Euro", decimalPlaces = 2)
    val GBP = CurrencyOption(code = "GBP", symbol = "£", name = "British Pound", decimalPlaces = 2)
    val PKR = CurrencyOption(code = "PKR", symbol = "Rs.", name = "Pakistani Rupee", decimalPlaces = 2)
    val AED = CurrencyOption(code = "AED", symbol = "AED", name = "UAE Dirham", decimalPlaces = 2)
    val SAR = CurrencyOption(code = "SAR", symbol = "SAR", name = "Saudi Riyal", decimalPlaces = 2)
    val CAD = CurrencyOption(code = "CAD", symbol = "CA$", name = "Canadian Dollar", decimalPlaces = 2)
    val AUD = CurrencyOption(code = "AUD", symbol = "A$", name = "Australian Dollar", decimalPlaces = 2)
    val INR = CurrencyOption(code = "INR", symbol = "₹", name = "Indian Rupee", decimalPlaces = 2)
    val JPY = CurrencyOption(code = "JPY", symbol = "¥", name = "Japanese Yen", decimalPlaces = 0)
    val CNY = CurrencyOption(code = "CNY", symbol = "¥", name = "Chinese Yuan", decimalPlaces = 2)

    val supportedCurrencies: List<CurrencyOption> = listOf(
        USD,
        EUR,
        GBP,
        PKR,
        AED,
        SAR,
        CAD,
        AUD,
        INR,
        JPY,
        CNY
    )

    val defaultCurrency: CurrencyOption = USD

    fun findByCode(code: String): CurrencyOption? {
        return supportedCurrencies.firstOrNull { it.code.equals(code.trim(), ignoreCase = true) }
    }

    fun createCustomCurrency(
        code: String,
        symbol: String,
        name: String = "Custom Currency",
        decimalPlaces: Int
    ): CurrencyOption {
        val normalizedDecimals = decimalPlaces.coerceIn(0, 4)
        val normalizedCode = code.trim().uppercase().ifEmpty { "OTHER" }
        val normalizedSymbol = symbol.trim().ifEmpty { normalizedCode }
        val normalizedName = name.trim().ifEmpty { "Custom ($normalizedCode)" }
        return CurrencyOption(
            code = normalizedCode,
            symbol = normalizedSymbol,
            name = normalizedName,
            decimalPlaces = normalizedDecimals
        )
    }
}
