package com.example.discountgap.domain

import com.example.discountgap.model.CurrencyOption
import com.example.discountgap.model.DiscountGapResult

object DiscountGapFormatter {

    fun formatMoney(
        minorUnits: Long,
        currency: CurrencyOption
    ): String = MoneyFormatter.format(minorUnits, currency)

    fun formatPercentage(
        basisPoints: Long
    ): String = PercentageFormatter.formatPercentage(basisPoints)

    fun formatDiscountBadge(result: DiscountGapResult): String {
        return if (result.isPercentageAvailable) {
            "${formatPercentage(result.discountPercentageBasisPoints)} OFF"
        } else {
            "—"
        }
    }

    fun formatRemainingPercentage(result: DiscountGapResult): String {
        return if (result.isPercentageAvailable) {
            formatPercentage(result.remainingPercentageBasisPoints)
        } else {
            "—"
        }
    }

    fun formatCopyText(
        result: DiscountGapResult
    ): String {
        val originalFormatted = formatMoney(result.originalAmountMinorUnits, result.currency)
        val discountedFormatted = formatMoney(result.discountedAmountMinorUnits, result.currency)
        val savingsFormatted = formatMoney(result.savingsMinorUnits, result.currency)
        val discountLine = formatDiscountBadge(result)
        val remainingLine = formatRemainingPercentage(result)

        return buildString {
            appendLine("DiscountGap Result")
            appendLine()
            appendLine("Original Price: $originalFormatted")
            appendLine("Discounted Price: $discountedFormatted")
            appendLine()
            appendLine("You Save: $savingsFormatted")
            appendLine("Discount: $discountLine")
            append("Remaining: $remainingLine")
        }
    }

    fun formatShareText(
        result: DiscountGapResult
    ): String {
        val originalFormatted = formatMoney(result.originalAmountMinorUnits, result.currency)
        val discountedFormatted = formatMoney(result.discountedAmountMinorUnits, result.currency)
        val savingsFormatted = formatMoney(result.savingsMinorUnits, result.currency)
        val discountLine = formatDiscountBadge(result)

        return buildString {
            appendLine("DiscountGap")
            appendLine()
            appendLine("Original: $originalFormatted")
            appendLine("Discounted: $discountedFormatted")
            appendLine()
            appendLine("You Save: $savingsFormatted")
            append("Discount: $discountLine")
        }
    }

    fun formatAccessibilityMoney(
        label: String,
        minorUnits: Long,
        currency: CurrencyOption
    ): String {
        val formatted = formatMoney(minorUnits, currency)
        return "$label, $formatted (${currency.name})."
    }

    fun formatAccessibilityDiscount(result: DiscountGapResult): String {
        return if (result.isPercentageAvailable) {
            "${formatPercentage(result.discountPercentageBasisPoints)} discount."
        } else {
            "Discount percentage unavailable."
        }
    }

    fun getDiscountBarFraction(result: DiscountGapResult): Float {
        if (!result.isPercentageAvailable) return 0f
        return (result.discountPercentageBasisPoints.toFloat() /
            DiscountGapResult.FULL_PERCENTAGE_BASIS_POINTS.toFloat()).coerceIn(0f, 1f)
    }

    fun getRemainingBarFraction(result: DiscountGapResult): Float {
        if (!result.isPercentageAvailable) return 0f
        return (result.remainingPercentageBasisPoints.toFloat() /
            DiscountGapResult.FULL_PERCENTAGE_BASIS_POINTS.toFloat()).coerceIn(0f, 1f)
    }
}
