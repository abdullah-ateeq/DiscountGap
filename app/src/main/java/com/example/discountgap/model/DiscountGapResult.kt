package com.example.discountgap.model

data class DiscountGapResult(
    val originalAmountMinorUnits: Long,
    val discountedAmountMinorUnits: Long,
    val savingsMinorUnits: Long,
    val discountPercentageBasisPoints: Long,
    val remainingPercentageBasisPoints: Long,
    val currency: CurrencyOption
) {
    val isZeroOriginal: Boolean
        get() = originalAmountMinorUnits == 0L

    val isPercentageAvailable: Boolean
        get() = originalAmountMinorUnits > 0L && discountPercentageBasisPoints >= 0L

    val isNoPriceDifference: Boolean
        get() = originalAmountMinorUnits == discountedAmountMinorUnits

    companion object {
        const val UNDEFINED_BASIS_POINTS = -1L
        const val FULL_PERCENTAGE_BASIS_POINTS = 10_000L
    }
}
