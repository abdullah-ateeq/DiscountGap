package com.example.discountgap.domain

import com.example.discountgap.model.CurrencyOption
import com.example.discountgap.model.DiscountGapResult
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

object DiscountGapCalculator {

    const val BASIS_POINTS_SCALE = 10_000L
    private val BASIS_POINTS_BIG_INT = BigInteger.valueOf(BASIS_POINTS_SCALE)
    private val HUNDRED_BIG_INT = BigInteger.valueOf(100L)
    private val TWO_BIG_INT = BigInteger.valueOf(2L)
    private val MAX_LONG_BIG_INT = BigInteger.valueOf(Long.MAX_VALUE)

    // Safe limit for Long multiplication before needing BigInteger
    private const val SAFE_MULTIPLY_LIMIT = Long.MAX_VALUE / BASIS_POINTS_SCALE

    fun calculate(
        originalAmountMinorUnits: Long,
        discountedAmountMinorUnits: Long,
        currency: CurrencyOption
    ): DiscountGapResult {
        require(DiscountGapValidator.isValidCurrency(currency)) {
            DiscountGapValidator.ERROR_CURRENCY_PRECISION
        }
        require(originalAmountMinorUnits >= 0L) {
            DiscountGapValidator.ERROR_NEGATIVE_ORIGINAL
        }
        require(discountedAmountMinorUnits >= 0L) {
            DiscountGapValidator.ERROR_NEGATIVE_DISCOUNTED
        }
        require(discountedAmountMinorUnits <= originalAmountMinorUnits) {
            DiscountGapValidator.ERROR_DISCOUNTED_ABOVE_ORIGINAL
        }

        val savingsMinorUnits = Math.subtractExact(originalAmountMinorUnits, discountedAmountMinorUnits)

        // Guarantee core invariant: original == discounted + savings
        check(originalAmountMinorUnits == Math.addExact(discountedAmountMinorUnits, savingsMinorUnits)) {
            "Core savings invariant violated"
        }

        if (originalAmountMinorUnits == 0L) {
            return DiscountGapResult(
                originalAmountMinorUnits = 0L,
                discountedAmountMinorUnits = 0L,
                savingsMinorUnits = 0L,
                discountPercentageBasisPoints = DiscountGapResult.UNDEFINED_BASIS_POINTS,
                remainingPercentageBasisPoints = DiscountGapResult.UNDEFINED_BASIS_POINTS,
                currency = currency
            )
        }

        val discountBasisPoints = calculateBasisPointsRoundHalfUp(
            numerator = savingsMinorUnits,
            denominator = originalAmountMinorUnits
        )

        // Ensure mathematical consistency: discountPercentage + remainingPercentage == 10000 basis points (100%)
        val remainingBasisPoints = Math.subtractExact(BASIS_POINTS_SCALE, discountBasisPoints)

        return DiscountGapResult(
            originalAmountMinorUnits = originalAmountMinorUnits,
            discountedAmountMinorUnits = discountedAmountMinorUnits,
            savingsMinorUnits = savingsMinorUnits,
            discountPercentageBasisPoints = discountBasisPoints,
            remainingPercentageBasisPoints = remainingBasisPoints,
            currency = currency
        )
    }

    /**
     * Calculates (numerator * 10000) / denominator rounded using ROUND HALF UP.
     * Uses primitive Long when safe, and automatically falls back to BigInteger
     * when intermediate multiplication could overflow Long.
     */
    fun calculateBasisPointsRoundHalfUp(
        numerator: Long,
        denominator: Long
    ): Long {
        require(numerator >= 0L) { "Numerator cannot be negative" }
        require(denominator > 0L) { "Denominator must be positive" }

        if (numerator == 0L) return 0L
        if (numerator == denominator) return BASIS_POINTS_SCALE

        // Fast overflow-safe Long path when numerator <= Long.MAX_VALUE / 10000 and denominator <= Long.MAX_VALUE / 2
        if (numerator <= SAFE_MULTIPLY_LIMIT && denominator <= Long.MAX_VALUE / 2L) {
            val scaled = Math.multiplyExact(numerator, BASIS_POINTS_SCALE)
            val quotient = scaled / denominator
            val remainder = scaled % denominator
            val doubledRemainder = Math.multiplyExact(remainder, 2L)
            return if (doubledRemainder >= denominator) {
                Math.addExact(quotient, 1L)
            } else {
                quotient
            }
        }

        // BigInteger fallback for huge values to prevent overflow
        val numBig = BigInteger.valueOf(numerator)
        val denBig = BigInteger.valueOf(denominator)
        val scaledBig = numBig.multiply(BASIS_POINTS_BIG_INT)
        val divRem = scaledBig.divideAndRemainder(denBig)
        val quotientBig = divRem[0]
        val remainderBig = divRem[1]
        val roundedBig = if (remainderBig.multiply(TWO_BIG_INT) >= denBig) {
            quotientBig.add(BigInteger.ONE)
        } else {
            quotientBig
        }
        if (roundedBig > MAX_LONG_BIG_INT) {
            throw ArithmeticException(DiscountGapValidator.ERROR_OVERFLOW)
        }
        return roundedBig.longValueExact()
    }

    /**
     * Calculates discounted price minor units from an original price and a preset discount percentage P (0..100):
     * discounted = O * (100 - P) / 100
     * Rounded to the currency's minor unit using ROUND HALF UP.
     */
    fun calculateDiscountedMinorUnitsFromPreset(
        originalAmountMinorUnits: Long,
        discountPercent: Int
    ): Long {
        require(originalAmountMinorUnits >= 0L) {
            DiscountGapValidator.ERROR_NEGATIVE_ORIGINAL
        }
        require(discountPercent in 0..100) {
            "Discount preset must be between 0 and 100"
        }

        if (originalAmountMinorUnits == 0L || discountPercent == 100) return 0L
        if (discountPercent == 0) return originalAmountMinorUnits

        val remainingPercent = 100L - discountPercent.toLong()
        val origBig = BigInteger.valueOf(originalAmountMinorUnits)
        val productBig = origBig.multiply(BigInteger.valueOf(remainingPercent))
        val divRem = productBig.divideAndRemainder(HUNDRED_BIG_INT)
        val quotient = divRem[0]
        val remainder = divRem[1]
        val rounded = if (remainder.multiply(TWO_BIG_INT) >= HUNDRED_BIG_INT) {
            quotient.add(BigInteger.ONE)
        } else {
            quotient
        }
        return rounded.longValueExact()
    }

    /**
     * Converts a whole-unit preset (e.g., 10, 50, 100, 500, 1000) into minor units for the given currency.
     */
    fun majorUnitsToMinorUnits(
        majorUnits: Long,
        currency: CurrencyOption
    ): Long {
        require(majorUnits >= 0L) { DiscountGapValidator.ERROR_NEGATIVE_ORIGINAL }
        require(DiscountGapValidator.isValidCurrency(currency)) {
            DiscountGapValidator.ERROR_CURRENCY_PRECISION
        }
        val scale = BigDecimal.TEN.pow(currency.decimalPlaces).toBigInteger()
        val minorBig = BigInteger.valueOf(majorUnits).multiply(scale)
        if (minorBig > MAX_LONG_BIG_INT) {
            throw ArithmeticException(DiscountGapValidator.ERROR_OVERFLOW)
        }
        return minorBig.longValueExact()
    }
}
