package com.example.discountgap.repository

import android.content.SharedPreferences
import com.example.discountgap.data.InMemoryPreferencesDataSource
import com.example.discountgap.data.PreferencesDataSource
import com.example.discountgap.data.SharedPreferencesDataSource
import com.example.discountgap.domain.CurrencyRepository
import com.example.discountgap.model.CurrencyOption
import com.example.discountgap.model.DiscountGapHistory
import com.example.discountgap.model.DiscountGapResult
import java.util.UUID

class DiscountGapHistoryRepository(
    private val dataSource: PreferencesDataSource = InMemoryPreferencesDataSource()
) {
    constructor(sharedPreferences: SharedPreferences) : this(
        SharedPreferencesDataSource(sharedPreferences)
    )

    companion object {
        const val MAX_HISTORY_ITEMS = 10
        private const val KEY_HISTORY_ENTRIES = "discount_gap_history_entries_v1"
        private const val KEY_CURRENCY_DECIMALS_MAP = "discount_gap_currency_decimals_v1"
        private const val ENTRY_DELIMITER = "\n"
        private const val FIELD_DELIMITER = "\t"
    }

    @Synchronized
    fun loadHistory(): List<DiscountGapHistory> {
        val raw = dataSource.getString(KEY_HISTORY_ENTRIES, null) ?: return emptyList()
        if (raw.isBlank()) return emptyList()

        return raw.split(ENTRY_DELIMITER)
            .mapNotNull { line -> deserializeEntry(line) }
            .take(MAX_HISTORY_ITEMS)
    }

    @Synchronized
    fun saveCalculation(
        result: DiscountGapResult,
        timestampMillis: Long = System.currentTimeMillis()
    ): List<DiscountGapHistory> {
        val current = loadHistory().toMutableList()

        // Rapid double-save protection: if the most recent item has identical values, do not duplicate
        val topItem = current.firstOrNull()
        if (topItem != null &&
            topItem.originalAmountMinorUnits == result.originalAmountMinorUnits &&
            topItem.discountedAmountMinorUnits == result.discountedAmountMinorUnits &&
            topItem.savingsMinorUnits == result.savingsMinorUnits &&
            topItem.discountPercentageBasisPoints == result.discountPercentageBasisPoints &&
            topItem.currencyCode == result.currency.code &&
            topItem.currencySymbol == result.currency.symbol
        ) {
            return current
        }

        saveCurrencyMetadata(result.currency)

        val newEntry = DiscountGapHistory(
            id = UUID.randomUUID().toString(),
            originalAmountMinorUnits = result.originalAmountMinorUnits,
            discountedAmountMinorUnits = result.discountedAmountMinorUnits,
            savingsMinorUnits = result.savingsMinorUnits,
            discountPercentageBasisPoints = result.discountPercentageBasisPoints,
            currencyCode = result.currency.code,
            currencySymbol = result.currency.symbol,
            calculatedAt = timestampMillis
        )

        current.add(0, newEntry)
        val trimmed = current.take(MAX_HISTORY_ITEMS)
        persistHistory(trimmed)
        return trimmed
    }

    @Synchronized
    fun saveHistoryEntry(entry: DiscountGapHistory): List<DiscountGapHistory> {
        val current = loadHistory().toMutableList()
        val topItem = current.firstOrNull()
        if (topItem != null &&
            topItem.originalAmountMinorUnits == entry.originalAmountMinorUnits &&
            topItem.discountedAmountMinorUnits == entry.discountedAmountMinorUnits &&
            topItem.savingsMinorUnits == entry.savingsMinorUnits &&
            topItem.discountPercentageBasisPoints == entry.discountPercentageBasisPoints &&
            topItem.currencyCode == entry.currencyCode &&
            topItem.currencySymbol == entry.currencySymbol
        ) {
            return current
        }
        current.add(0, entry)
        val trimmed = current.take(MAX_HISTORY_ITEMS)
        persistHistory(trimmed)
        return trimmed
    }

    @Synchronized
    fun deleteCalculation(id: String): List<DiscountGapHistory> {
        val updated = loadHistory().filterNot { it.id == id }
        persistHistory(updated)
        return updated
    }

    @Synchronized
    fun clearHistory(): List<DiscountGapHistory> {
        dataSource.remove(KEY_HISTORY_ENTRIES)
        return emptyList()
    }

    fun resolveCurrencyForHistory(history: DiscountGapHistory): CurrencyOption {
        val builtIn = CurrencyRepository.findByCode(history.currencyCode)
        if (builtIn != null && builtIn.symbol == history.currencySymbol) {
            return builtIn
        }
        val storedDecimals = loadCurrencyDecimals(history.currencyCode)
            ?: builtIn?.decimalPlaces
            ?: 2
        return CurrencyRepository.createCustomCurrency(
            code = history.currencyCode,
            symbol = history.currencySymbol,
            name = builtIn?.name ?: "Custom (${history.currencyCode})",
            decimalPlaces = storedDecimals
        )
    }

    private fun saveCurrencyMetadata(currency: CurrencyOption) {
        val currentMap = loadCurrencyDecimalsMap().toMutableMap()
        currentMap[currency.code] = currency.decimalPlaces
        val serialized = currentMap.entries.joinToString(";") { "${it.key}:${it.value}" }
        dataSource.putString(KEY_CURRENCY_DECIMALS_MAP, serialized)
    }

    private fun loadCurrencyDecimals(code: String): Int? {
        return loadCurrencyDecimalsMap()[code]
    }

    private fun loadCurrencyDecimalsMap(): Map<String, Int> {
        val raw = dataSource.getString(KEY_CURRENCY_DECIMALS_MAP, null) ?: return emptyMap()
        if (raw.isBlank()) return emptyMap()
        return raw.split(";").mapNotNull { pair ->
            val parts = pair.split(":")
            if (parts.size == 2) {
                val decimals = parts[1].toIntOrNull()
                if (decimals != null) parts[0] to decimals else null
            } else {
                null
            }
        }.toMap()
    }

    private fun persistHistory(items: List<DiscountGapHistory>) {
        if (items.isEmpty()) {
            dataSource.remove(KEY_HISTORY_ENTRIES)
            return
        }
        val serialized = items.joinToString(ENTRY_DELIMITER) { serializeEntry(it) }
        dataSource.putString(KEY_HISTORY_ENTRIES, serialized)
    }

    private fun serializeEntry(entry: DiscountGapHistory): String {
        val safeCode = entry.currencyCode.replace(FIELD_DELIMITER, " ").replace(ENTRY_DELIMITER, " ")
        val safeSymbol = entry.currencySymbol.replace(FIELD_DELIMITER, " ").replace(ENTRY_DELIMITER, " ")
        return listOf(
            entry.id,
            entry.originalAmountMinorUnits.toString(),
            entry.discountedAmountMinorUnits.toString(),
            entry.savingsMinorUnits.toString(),
            entry.discountPercentageBasisPoints.toString(),
            safeCode,
            safeSymbol,
            entry.calculatedAt.toString()
        ).joinToString(FIELD_DELIMITER)
    }

    private fun deserializeEntry(line: String): DiscountGapHistory? {
        val parts = line.split(FIELD_DELIMITER)
        if (parts.size < 8) return null
        return try {
            DiscountGapHistory(
                id = parts[0],
                originalAmountMinorUnits = parts[1].toLong(),
                discountedAmountMinorUnits = parts[2].toLong(),
                savingsMinorUnits = parts[3].toLong(),
                discountPercentageBasisPoints = parts[4].toLong(),
                currencyCode = parts[5],
                currencySymbol = parts[6],
                calculatedAt = parts[7].toLong()
            )
        } catch (e: NumberFormatException) {
            null
        }
    }
}
