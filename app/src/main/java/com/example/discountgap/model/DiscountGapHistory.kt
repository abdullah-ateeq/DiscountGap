package com.example.discountgap.model

data class DiscountGapHistory(
    val id: String,
    val originalAmountMinorUnits: Long,
    val discountedAmountMinorUnits: Long,
    val savingsMinorUnits: Long,
    val discountPercentageBasisPoints: Long,
    val currencyCode: String,
    val currencySymbol: String,
    val calculatedAt: Long
)
