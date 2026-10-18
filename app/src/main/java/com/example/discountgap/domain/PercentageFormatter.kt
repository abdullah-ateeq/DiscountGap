package com.example.discountgap.domain

import com.example.discountgap.model.DiscountGapResult

object PercentageFormatter {

    /**
     * Formats basis points (where 10000 = 100%) to a percentage string with up to 2 decimal places,
     * omitting unnecessary trailing zeros.
     * Examples:
     * 500 -> "5%"
     * 1250 -> "12.5%"
     * 2500 -> "25%"
     * 3333 -> "33.33%"
     * 10000 -> "100%"
     * -1 -> "—"
     */
    fun formatPercentage(
        basisPoints: Long
    ): String {
        if (basisPoints < 0L || basisPoints == DiscountGapResult.UNDEFINED_BASIS_POINTS) {
            return "—"
        }

        val wholePercent = basisPoints / 100L
        val fractionalBasisPoints = basisPoints % 100L

        return when {
            fractionalBasisPoints == 0L -> "${wholePercent}%"
            fractionalBasisPoints % 10L == 0L -> "${wholePercent}.${fractionalBasisPoints / 10L}%"
            else -> "${wholePercent}.${fractionalBasisPoints.toString().padStart(2, '0')}%"
        }
    }

    /**
     * Formats basis points with a fixed 2 decimal places (e.g. 2500 -> "25.00%").
     */
    fun formatPercentageTwoDecimals(
        basisPoints: Long
    ): String {
        if (basisPoints < 0L || basisPoints == DiscountGapResult.UNDEFINED_BASIS_POINTS) {
            return "—"
        }
        val wholePercent = basisPoints / 100L
        val fractionalBasisPoints = basisPoints % 100L
        return "${wholePercent}.${fractionalBasisPoints.toString().padStart(2, '0')}%"
    }
}
