package com.example.discountgap

import com.example.discountgap.data.InMemoryPreferencesDataSource
import com.example.discountgap.domain.CurrencyRepository
import com.example.discountgap.domain.DiscountGapCalculator
import com.example.discountgap.domain.DiscountGapFormatter
import com.example.discountgap.domain.DiscountGapValidator
import com.example.discountgap.domain.MoneyFormatter
import com.example.discountgap.domain.MoneyParser
import com.example.discountgap.domain.PercentageFormatter
import com.example.discountgap.model.AppThemeMode
import com.example.discountgap.model.CalculationState
import com.example.discountgap.model.DiscountGapResult
import com.example.discountgap.repository.DiscountGapHistoryRepository
import com.example.discountgap.repository.SettingsRepository
import com.example.discountgap.ui.DiscountGapViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DiscountGapUnitTest {

    // 1. Equal prices (Critical Test — No Discount)
    @Test
    fun test01_equalPrices_zeroDiscount() {
        val result = DiscountGapCalculator.calculate(
            originalAmountMinorUnits = 10_000L,
            discountedAmountMinorUnits = 10_000L,
            currency = CurrencyRepository.USD
        )
        assertEquals(0L, result.savingsMinorUnits)
        assertEquals(0L, result.discountPercentageBasisPoints)
        assertEquals(10_000L, result.remainingPercentageBasisPoints)
        assertEquals("$0.00", DiscountGapFormatter.formatMoney(result.savingsMinorUnits, result.currency))
        assertEquals("0%", DiscountGapFormatter.formatPercentage(result.discountPercentageBasisPoints))
        assertEquals("100%", DiscountGapFormatter.formatPercentage(result.remainingPercentageBasisPoints))
        assertTrue(result.isNoPriceDifference)
    }

    // 2. 5% discount
    @Test
    fun test02_fivePercentDiscount() {
        val result = DiscountGapCalculator.calculate(
            originalAmountMinorUnits = 10_000L,
            discountedAmountMinorUnits = 9_500L,
            currency = CurrencyRepository.USD
        )
        assertEquals(500L, result.savingsMinorUnits)
        assertEquals(500L, result.discountPercentageBasisPoints)
        assertEquals(9_500L, result.remainingPercentageBasisPoints)
        assertEquals("5%", DiscountGapFormatter.formatPercentage(result.discountPercentageBasisPoints))
    }

    // 3. 10% discount (Critical Test — 10% Discount)
    @Test
    fun test03_tenPercentDiscount() {
        val result = DiscountGapCalculator.calculate(
            originalAmountMinorUnits = 10_000L,
            discountedAmountMinorUnits = 9_000L,
            currency = CurrencyRepository.USD
        )
        assertEquals(1_000L, result.savingsMinorUnits)
        assertEquals(1_000L, result.discountPercentageBasisPoints)
        assertEquals(9_000L, result.remainingPercentageBasisPoints)
        assertEquals("$10.00", DiscountGapFormatter.formatMoney(result.savingsMinorUnits, result.currency))
        assertEquals("10%", DiscountGapFormatter.formatPercentage(result.discountPercentageBasisPoints))
    }

    // 4. 15% discount
    @Test
    fun test04_fifteenPercentDiscount() {
        val result = DiscountGapCalculator.calculate(
            originalAmountMinorUnits = 20_000L,
            discountedAmountMinorUnits = 17_000L,
            currency = CurrencyRepository.USD
        )
        assertEquals(3_000L, result.savingsMinorUnits)
        assertEquals(1_500L, result.discountPercentageBasisPoints)
        assertEquals(8_500L, result.remainingPercentageBasisPoints)
        assertEquals("15%", DiscountGapFormatter.formatPercentage(result.discountPercentageBasisPoints))
    }

    // 5. 20% discount
    @Test
    fun test05_twentyPercentDiscount() {
        val result = DiscountGapCalculator.calculate(
            originalAmountMinorUnits = 10_000L,
            discountedAmountMinorUnits = 8_000L,
            currency = CurrencyRepository.USD
        )
        assertEquals(2_000L, result.savingsMinorUnits)
        assertEquals(2_000L, result.discountPercentageBasisPoints)
        assertEquals(8_000L, result.remainingPercentageBasisPoints)
        assertEquals("20%", DiscountGapFormatter.formatPercentage(result.discountPercentageBasisPoints))
    }

    // 6. 25% discount (Critical Test — Basic Comparison)
    @Test
    fun test06_twentyFivePercentDiscount_basicComparison() {
        val result = DiscountGapCalculator.calculate(
            originalAmountMinorUnits = 10_000L,
            discountedAmountMinorUnits = 7_500L,
            currency = CurrencyRepository.USD
        )
        assertEquals(2_500L, result.savingsMinorUnits)
        assertEquals(2_500L, result.discountPercentageBasisPoints)
        assertEquals(7_500L, result.remainingPercentageBasisPoints)
        assertEquals("$25.00", DiscountGapFormatter.formatMoney(result.savingsMinorUnits, result.currency))
        assertEquals("25%", DiscountGapFormatter.formatPercentage(result.discountPercentageBasisPoints))
        assertEquals("75%", DiscountGapFormatter.formatPercentage(result.remainingPercentageBasisPoints))
    }

    // 7. 50% discount
    @Test
    fun test07_fiftyPercentDiscount() {
        val result = DiscountGapCalculator.calculate(
            originalAmountMinorUnits = 10_000L,
            discountedAmountMinorUnits = 5_000L,
            currency = CurrencyRepository.USD
        )
        assertEquals(5_000L, result.savingsMinorUnits)
        assertEquals(5_000L, result.discountPercentageBasisPoints)
        assertEquals(5_000L, result.remainingPercentageBasisPoints)
        assertEquals("50%", DiscountGapFormatter.formatPercentage(result.discountPercentageBasisPoints))
    }

    // 8. 100% discount (Critical Test — Full Discount)
    @Test
    fun test08_oneHundredPercentDiscount_fullDiscount() {
        val result = DiscountGapCalculator.calculate(
            originalAmountMinorUnits = 10_000L,
            discountedAmountMinorUnits = 0L,
            currency = CurrencyRepository.USD
        )
        assertEquals(10_000L, result.savingsMinorUnits)
        assertEquals(10_000L, result.discountPercentageBasisPoints)
        assertEquals(0L, result.remainingPercentageBasisPoints)
        assertEquals("$100.00", DiscountGapFormatter.formatMoney(result.savingsMinorUnits, result.currency))
        assertEquals("100%", DiscountGapFormatter.formatPercentage(result.discountPercentageBasisPoints))
        assertEquals("0%", DiscountGapFormatter.formatPercentage(result.remainingPercentageBasisPoints))
    }

    // 9. Decimal percentage (Critical Test — Decimal Prices)
    @Test
    fun test09_decimalPercentageAndPrices() {
        // $99.99 -> $74.99 => Savings $25.00, 2500.250025... basis points -> 2500 (25.00% / 25%)
        val result1 = DiscountGapCalculator.calculate(
            originalAmountMinorUnits = 9_999L,
            discountedAmountMinorUnits = 7_499L,
            currency = CurrencyRepository.USD
        )
        assertEquals(2_500L, result1.savingsMinorUnits)
        assertEquals("$25.00", DiscountGapFormatter.formatMoney(result1.savingsMinorUnits, result1.currency))
        assertEquals(2_500L, result1.discountPercentageBasisPoints)
        assertEquals("25.00%", PercentageFormatter.formatPercentageTwoDecimals(result1.discountPercentageBasisPoints))

        // $30.00 -> $20.00 => Savings $10.00, 33.33%
        val result2 = DiscountGapCalculator.calculate(
            originalAmountMinorUnits = 3_000L,
            discountedAmountMinorUnits = 2_000L,
            currency = CurrencyRepository.USD
        )
        assertEquals(1_000L, result2.savingsMinorUnits)
        assertEquals(3_333L, result2.discountPercentageBasisPoints)
        assertEquals("33.33%", PercentageFormatter.formatPercentage(result2.discountPercentageBasisPoints))
    }

    // 10. Zero original price (Critical Test — Zero Original)
    @Test
    fun test10_zeroOriginalPrice() {
        val result = DiscountGapCalculator.calculate(
            originalAmountMinorUnits = 0L,
            discountedAmountMinorUnits = 0L,
            currency = CurrencyRepository.USD
        )
        assertEquals(0L, result.savingsMinorUnits)
        assertEquals(DiscountGapResult.UNDEFINED_BASIS_POINTS, result.discountPercentageBasisPoints)
        assertEquals(DiscountGapResult.UNDEFINED_BASIS_POINTS, result.remainingPercentageBasisPoints)
        assertFalse(result.isPercentageAvailable)
        assertTrue(result.isZeroOriginal)
        assertTrue(result.isNoPriceDifference)
        assertEquals("$0.00", DiscountGapFormatter.formatMoney(result.savingsMinorUnits, result.currency))
        assertEquals("—", DiscountGapFormatter.formatPercentage(result.discountPercentageBasisPoints))
        assertEquals("—", DiscountGapFormatter.formatDiscountBadge(result))
    }

    // 11. Zero discounted price
    @Test
    fun test11_zeroDiscountedPrice() {
        val result = DiscountGapCalculator.calculate(
            originalAmountMinorUnits = 4_999L,
            discountedAmountMinorUnits = 0L,
            currency = CurrencyRepository.USD
        )
        assertEquals(4_999L, result.savingsMinorUnits)
        assertEquals(10_000L, result.discountPercentageBasisPoints)
        assertEquals(0L, result.remainingPercentageBasisPoints)
    }

    // 12. Negative original price
    @Test
    fun test12_negativeOriginalPrice() {
        val validation = DiscountGapValidator.validateInputs(
            originalInput = "-50.00",
            discountedInput = "25.00",
            currency = CurrencyRepository.USD
        )
        assertTrue(validation is DiscountGapValidator.ValidationResult.Invalid)
        assertEquals(
            "Original price cannot be negative.",
            (validation as DiscountGapValidator.ValidationResult.Invalid).message
        )
    }

    // 13. Negative discounted price
    @Test
    fun test13_negativeDiscountedPrice() {
        val validation = DiscountGapValidator.validateInputs(
            originalInput = "100.00",
            discountedInput = "-10.00",
            currency = CurrencyRepository.USD
        )
        assertTrue(validation is DiscountGapValidator.ValidationResult.Invalid)
        assertEquals(
            "Discounted price cannot be negative.",
            (validation as DiscountGapValidator.ValidationResult.Invalid).message
        )
    }

    // 14. Discounted price above original (Critical Test — Invalid Price Relationship)
    @Test
    fun test14_discountedPriceAboveOriginal() {
        val validation = DiscountGapValidator.validateInputs(
            originalInput = "100",
            discountedInput = "120",
            currency = CurrencyRepository.USD
        )
        assertTrue(validation is DiscountGapValidator.ValidationResult.Invalid)
        assertEquals(
            "Discounted price cannot be greater than the original price.",
            (validation as DiscountGapValidator.ValidationResult.Invalid).message
        )
    }

    // 15. Savings calculation
    @Test
    fun test15_savingsCalculation() {
        val result = DiscountGapCalculator.calculate(
            originalAmountMinorUnits = 20_000L,
            discountedAmountMinorUnits = 15_000L,
            currency = CurrencyRepository.USD
        )
        assertEquals(5_000L, result.savingsMinorUnits)
        assertEquals("$50.00", DiscountGapFormatter.formatMoney(result.savingsMinorUnits, result.currency))
    }

    // 16. Discount percentage
    @Test
    fun test16_discountPercentageCalculation() {
        val result = DiscountGapCalculator.calculate(
            originalAmountMinorUnits = 8_000L,
            discountedAmountMinorUnits = 7_000L,
            currency = CurrencyRepository.USD
        )
        // Savings = 1000, 1000 / 8000 = 12.5% = 1250 basis points
        assertEquals(1_250L, result.discountPercentageBasisPoints)
        assertEquals("12.5%", DiscountGapFormatter.formatPercentage(result.discountPercentageBasisPoints))
    }

    // 17. Remaining percentage
    @Test
    fun test17_remainingPercentageCalculation() {
        val result = DiscountGapCalculator.calculate(
            originalAmountMinorUnits = 8_000L,
            discountedAmountMinorUnits = 7_000L,
            currency = CurrencyRepository.USD
        )
        assertEquals(8_750L, result.remainingPercentageBasisPoints)
        assertEquals("87.5%", DiscountGapFormatter.formatPercentage(result.remainingPercentageBasisPoints))
    }

    // 18. USD
    @Test
    fun test18_currencyUSD() {
        val usd = CurrencyRepository.USD
        assertEquals(2, usd.decimalPlaces)
        assertEquals("$1,250.00", MoneyFormatter.format(125_000L, usd))
    }

    // 19. EUR
    @Test
    fun test19_currencyEUR() {
        val eur = CurrencyRepository.EUR
        assertEquals(2, eur.decimalPlaces)
        assertEquals("€1,250.00", MoneyFormatter.format(125_000L, eur))
    }

    // 20. GBP
    @Test
    fun test20_currencyGBP() {
        val gbp = CurrencyRepository.GBP
        assertEquals(2, gbp.decimalPlaces)
        assertEquals("£1,250.00", MoneyFormatter.format(125_000L, gbp))
    }

    // 21. PKR (Critical Test — PKR)
    @Test
    fun test21_currencyPKR() {
        val pkr = CurrencyRepository.PKR
        val result = DiscountGapCalculator.calculate(
            originalAmountMinorUnits = 100_000L,
            discountedAmountMinorUnits = 75_000L,
            currency = pkr
        )
        assertEquals("Rs. 1,000.00", MoneyFormatter.format(result.originalAmountMinorUnits, pkr))
        assertEquals("Rs. 750.00", MoneyFormatter.format(result.discountedAmountMinorUnits, pkr))
        assertEquals("Rs. 250.00", MoneyFormatter.format(result.savingsMinorUnits, pkr))
        assertEquals("25%", PercentageFormatter.formatPercentage(result.discountPercentageBasisPoints))
    }

    // 22. AED
    @Test
    fun test22_currencyAED() {
        val aed = CurrencyRepository.AED
        assertEquals(2, aed.decimalPlaces)
        assertEquals("AED 500.00", MoneyFormatter.format(50_000L, aed))
    }

    // 23. SAR
    @Test
    fun test23_currencySAR() {
        val sar = CurrencyRepository.SAR
        assertEquals(2, sar.decimalPlaces)
        assertEquals("SAR 500.00", MoneyFormatter.format(50_000L, sar))
    }

    // 24. CAD
    @Test
    fun test24_currencyCAD() {
        val cad = CurrencyRepository.CAD
        assertEquals(2, cad.decimalPlaces)
        assertEquals("CA$250.00", MoneyFormatter.format(25_000L, cad))
    }

    // 25. AUD
    @Test
    fun test25_currencyAUD() {
        val aud = CurrencyRepository.AUD
        assertEquals(2, aud.decimalPlaces)
        assertEquals("A$250.00", MoneyFormatter.format(25_000L, aud))
    }

    // 26. INR
    @Test
    fun test26_currencyINR() {
        val inr = CurrencyRepository.INR
        assertEquals(2, inr.decimalPlaces)
        assertEquals("₹1,500.00", MoneyFormatter.format(150_000L, inr))
    }

    // 27. JPY (Critical Test — JPY)
    @Test
    fun test27_currencyJPY() {
        val jpy = CurrencyRepository.JPY
        assertEquals(0, jpy.decimalPlaces)
        val result = DiscountGapCalculator.calculate(
            originalAmountMinorUnits = 1_000L,
            discountedAmountMinorUnits = 750L,
            currency = jpy
        )
        assertEquals("¥1,000", MoneyFormatter.format(result.originalAmountMinorUnits, jpy))
        assertEquals("¥750", MoneyFormatter.format(result.discountedAmountMinorUnits, jpy))
        assertEquals("¥250", MoneyFormatter.format(result.savingsMinorUnits, jpy))
        assertEquals("25%", PercentageFormatter.formatPercentage(result.discountPercentageBasisPoints))
    }

    // 28. Zero-decimal currency
    @Test
    fun test28_zeroDecimalCurrencyValidation() {
        val jpy = CurrencyRepository.JPY
        val valid = MoneyParser.parse("1250", jpy)
        assertTrue(valid is MoneyParser.ParseResult.Success)
        assertEquals(1_250L, (valid as MoneyParser.ParseResult.Success).money.minorUnits)

        val invalidDecimal = MoneyParser.parse("1250.50", jpy)
        assertTrue(invalidDecimal is MoneyParser.ParseResult.Error)
        assertEquals(
            "Review the prices for the selected currency.",
            (invalidDecimal as MoneyParser.ParseResult.Error).message
        )
    }

    // 29. Two-decimal currency
    @Test
    fun test29_twoDecimalCurrencyValidation() {
        val cny = CurrencyRepository.CNY
        val valid = MoneyParser.parse("1250.55", cny)
        assertTrue(valid is MoneyParser.ParseResult.Success)
        assertEquals(125_055L, (valid as MoneyParser.ParseResult.Success).money.minorUnits)

        val invalidThreeDecimals = MoneyParser.parse("1250.555", cny)
        assertTrue(invalidThreeDecimals is MoneyParser.ParseResult.Error)
        assertEquals(
            "Review the prices for the selected currency.",
            (invalidThreeDecimals as MoneyParser.ParseResult.Error).message
        )
    }

    // 30. Custom currency (0-4 decimal places)
    @Test
    fun test30_customCurrencyThreeAndFourDecimals() {
        val custom3 = CurrencyRepository.createCustomCurrency("KWD", "KD", "Kuwaiti Dinar", 3)
        assertEquals(3, custom3.decimalPlaces)
        val parsed3 = MoneyParser.parse("10.500", custom3)
        assertTrue(parsed3 is MoneyParser.ParseResult.Success)
        assertEquals(10_500L, (parsed3 as MoneyParser.ParseResult.Success).money.minorUnits)
        assertEquals("KD 10.500", MoneyFormatter.format(10_500L, custom3))

        val custom4 = CurrencyRepository.createCustomCurrency("UNIT", "U$", "Unit Token", 4)
        assertEquals(4, custom4.decimalPlaces)
        val parsed4 = MoneyParser.parse("1.2345", custom4)
        assertTrue(parsed4 is MoneyParser.ParseResult.Success)
        assertEquals(12_345L, (parsed4 as MoneyParser.ParseResult.Success).money.minorUnits)
        assertEquals("U$1.2345", MoneyFormatter.format(12_345L, custom4))
    }

    // 31. Money parsing
    @Test
    fun test31_moneyParsing() {
        val usd = CurrencyRepository.USD
        assertEquals(10_000L, MoneyParser.parseToMinorUnitsOrNull("100", usd))
        assertEquals(10_000L, MoneyParser.parseToMinorUnitsOrNull("100.00", usd))
        assertEquals(10_050L, MoneyParser.parseToMinorUnitsOrNull("100.5", usd))
        assertEquals(125_075L, MoneyParser.parseToMinorUnitsOrNull("$1,250.75", usd))
        assertNull(MoneyParser.parseToMinorUnitsOrNull("abc", usd))
        assertNull(MoneyParser.parseToMinorUnitsOrNull("", usd))
    }

    // 32. Money formatting
    @Test
    fun test32_moneyFormatting() {
        assertEquals("$1,250.00", MoneyFormatter.format(125_000L, CurrencyRepository.USD))
        assertEquals("Rs. 1,250.00", MoneyFormatter.format(125_000L, CurrencyRepository.PKR))
        assertEquals("¥1,250", MoneyFormatter.format(1_250L, CurrencyRepository.JPY))
    }

    // 33. Percentage formatting
    @Test
    fun test33_percentageFormatting() {
        assertEquals("5%", PercentageFormatter.formatPercentage(500L))
        assertEquals("12.5%", PercentageFormatter.formatPercentage(1_250L))
        assertEquals("25%", PercentageFormatter.formatPercentage(2_500L))
        assertEquals("33.33%", PercentageFormatter.formatPercentage(3_333L))
        assertEquals("100%", PercentageFormatter.formatPercentage(10_000L))
        assertEquals("—", PercentageFormatter.formatPercentage(-1L))
    }

    // 34. Percentage rounding (ROUND HALF UP)
    @Test
    fun test34_percentageRoundingHalfUp() {
        // 1 / 6 = 16.6666...% -> 1667 basis points (16.67%)
        val bp1 = DiscountGapCalculator.calculateBasisPointsRoundHalfUp(1L, 6L)
        assertEquals(1_667L, bp1)
        assertEquals("16.67%", PercentageFormatter.formatPercentage(bp1))

        // Exact half: 1 / 20000 of 10000 basis points = 0.5 basis points -> rounds UP to 1 basis point (0.01%)
        val bpHalf = DiscountGapCalculator.calculateBasisPointsRoundHalfUp(1L, 20_000L)
        assertEquals(1L, bpHalf)
        assertEquals("0.01%", PercentageFormatter.formatPercentage(bpHalf))
    }

    // 35. Large amounts (Critical Test — Large Value)
    @Test
    fun test35_largeAmounts() {
        val original = 900_000_000_000_00L // $900,000,000,000.00
        val discounted = 675_000_000_000_00L // $675,000,000,000.00
        val result = DiscountGapCalculator.calculate(original, discounted, CurrencyRepository.USD)
        assertEquals(225_000_000_000_00L, result.savingsMinorUnits)
        assertEquals(2_500L, result.discountPercentageBasisPoints)
        assertEquals(7_500L, result.remainingPercentageBasisPoints)
    }

    // 36. BigInteger fallback (Critical Test — Overflow intermediate multiplication)
    @Test
    fun test36_bigIntegerFallbackOnHugeValues() {
        // Choose original near Long.MAX_VALUE where savings * 10000 would overflow 64-bit Long
        val hugeOriginal = Long.MAX_VALUE - 7L
        val hugeDiscounted = hugeOriginal / 2L
        val result = DiscountGapCalculator.calculate(
            originalAmountMinorUnits = hugeOriginal,
            discountedAmountMinorUnits = hugeDiscounted,
            currency = CurrencyRepository.JPY
        )
        assertEquals(hugeOriginal - hugeDiscounted, result.savingsMinorUnits)
        assertEquals(5_000L, result.discountPercentageBasisPoints)
        assertEquals(5_000L, result.remainingPercentageBasisPoints)
        assertEquals(hugeOriginal, result.discountedAmountMinorUnits + result.savingsMinorUnits)
    }

    // 37. Overflow protection
    @Test
    fun test37_overflowProtectionInParser() {
        val tooLargeInput = "9999999999999999999999999999.99"
        val parseResult = MoneyParser.parse(tooLargeInput, CurrencyRepository.USD)
        assertTrue(parseResult is MoneyParser.ParseResult.Error)
        assertEquals(
            "Amount is too large.",
            (parseResult as MoneyParser.ParseResult.Error).message
        )
    }

    // 38. Core savings invariant (Critical Test — Savings Invariant)
    @Test
    fun test38_coreSavingsInvariant() {
        val testPairs = listOf(
            0L to 0L,
            100L to 100L,
            100L to 0L,
            9_999L to 7_499L,
            123_456_789L to 98_765_432L,
            Long.MAX_VALUE to (Long.MAX_VALUE / 3L)
        )
        for ((orig, disc) in testPairs) {
            val result = DiscountGapCalculator.calculate(orig, disc, CurrencyRepository.USD)
            assertEquals(
                orig,
                result.discountedAmountMinorUnits + result.savingsMinorUnits
            )
        }
    }

    // 39. Percentage consistency & determinism (Critical Test — Consistency & Deterministic)
    @Test
    fun test39_percentageConsistencyAndDeterminism() {
        val testPairs = listOf(
            10_000L to 7_500L,
            9_999L to 7_499L,
            3_000L to 2_000L,
            7_777L to 1_234L,
            20_000L to 19_999L
        )
        for ((orig, disc) in testPairs) {
            val first = DiscountGapCalculator.calculate(orig, disc, CurrencyRepository.USD)
            val second = DiscountGapCalculator.calculate(orig, disc, CurrencyRepository.USD)
            assertEquals(first, second)
            assertEquals(
                10_000L,
                first.discountPercentageBasisPoints + first.remainingPercentageBasisPoints
            )
        }
    }

    // 40. History save
    @Test
    fun test40_historySave() {
        val repo = DiscountGapHistoryRepository(InMemoryPreferencesDataSource())
        val result = DiscountGapCalculator.calculate(10_000L, 7_500L, CurrencyRepository.USD)
        val saved = repo.saveCalculation(result)
        assertEquals(1, saved.size)
        assertEquals(10_000L, saved.first().originalAmountMinorUnits)
        assertEquals(7_500L, saved.first().discountedAmountMinorUnits)
        assertEquals(2_500L, saved.first().savingsMinorUnits)
        assertEquals(2_500L, saved.first().discountPercentageBasisPoints)
    }

    // 41. History load
    @Test
    fun test41_historyLoad() {
        val dataSource = InMemoryPreferencesDataSource()
        val repo1 = DiscountGapHistoryRepository(dataSource)
        repo1.saveCalculation(DiscountGapCalculator.calculate(20_000L, 15_000L, CurrencyRepository.EUR))

        val repo2 = DiscountGapHistoryRepository(dataSource)
        val loaded = repo2.loadHistory()
        assertEquals(1, loaded.size)
        assertEquals("EUR", loaded.first().currencyCode)
        assertEquals(5_000L, loaded.first().savingsMinorUnits)
    }

    // 42. History limit (max 10, adding 11th removes oldest)
    @Test
    fun test42_historyLimitTenEntries() {
        val repo = DiscountGapHistoryRepository(InMemoryPreferencesDataSource())
        for (i in 1..11) {
            val orig = i * 1_000L
            val disc = i * 800L
            repo.saveCalculation(
                DiscountGapCalculator.calculate(orig, disc, CurrencyRepository.USD),
                timestampMillis = i.toLong()
            )
        }
        val history = repo.loadHistory()
        assertEquals(10, history.size)
        // Newest (11th) is at index 0, oldest remaining is 2nd calculation (2000L)
        assertEquals(11_000L, history.first().originalAmountMinorUnits)
        assertEquals(2_000L, history.last().originalAmountMinorUnits)
    }

    // 43. History deletion
    @Test
    fun test43_historyDeleteIndividual() {
        val repo = DiscountGapHistoryRepository(InMemoryPreferencesDataSource())
        repo.saveCalculation(DiscountGapCalculator.calculate(10_000L, 8_000L, CurrencyRepository.USD))
        val afterSecond = repo.saveCalculation(DiscountGapCalculator.calculate(20_000L, 10_000L, CurrencyRepository.USD))
        assertEquals(2, afterSecond.size)

        val idToDelete = afterSecond.first().id
        val remaining = repo.deleteCalculation(idToDelete)
        assertEquals(1, remaining.size)
        assertEquals(10_000L, remaining.first().originalAmountMinorUnits)
    }

    // 44. History clear
    @Test
    fun test44_historyClear() {
        val repo = DiscountGapHistoryRepository(InMemoryPreferencesDataSource())
        repo.saveCalculation(DiscountGapCalculator.calculate(10_000L, 7_500L, CurrencyRepository.USD))
        repo.saveCalculation(DiscountGapCalculator.calculate(5_000L, 2_500L, CurrencyRepository.USD))
        val cleared = repo.clearHistory()
        assertTrue(cleared.isEmpty())
        assertTrue(repo.loadHistory().isEmpty())
    }

    // 45. History restore
    @Test
    fun test45_historyRestoreInViewModel() {
        val viewModel = DiscountGapViewModel(
            historyRepository = DiscountGapHistoryRepository(InMemoryPreferencesDataSource()),
            settingsRepository = SettingsRepository(InMemoryPreferencesDataSource())
        )
        viewModel.onCurrencySelected(CurrencyRepository.PKR)
        viewModel.onOriginalPriceChanged("2000.00")
        viewModel.onDiscountedPriceChanged("1500.00")
        viewModel.saveToHistory()

        val savedItem = viewModel.uiState.value.history.first()
        viewModel.reset()
        assertEquals("USD", viewModel.uiState.value.currency.code)

        viewModel.restoreFromHistory(savedItem)
        val restoredState = viewModel.uiState.value
        assertEquals("PKR", restoredState.currency.code)
        assertEquals("2000.00", restoredState.originalAmountInput)
        assertEquals("1500.00", restoredState.discountedAmountInput)
        assertNotNull(restoredState.result)
        assertEquals(50_000L, restoredState.result?.savingsMinorUnits)
    }

    // 46. Settings persistence
    @Test
    fun test46_settingsPersistence() {
        val dataSource = InMemoryPreferencesDataSource()
        val settingsRepo1 = SettingsRepository(dataSource)
        settingsRepo1.setThemeMode(AppThemeMode.DARK)
        settingsRepo1.setDefaultCurrency(CurrencyRepository.PKR)
        settingsRepo1.setDynamicColor(false)

        val settingsRepo2 = SettingsRepository(dataSource)
        val loaded = settingsRepo2.loadSettings()
        assertEquals(AppThemeMode.DARK, loaded.themeMode)
        assertEquals("PKR", loaded.defaultCurrency.code)
        assertFalse(loaded.dynamicColor)
    }

    // 47. Last-comparison persistence
    @Test
    fun test47_lastComparisonPersistence() {
        val dataSource = InMemoryPreferencesDataSource()
        val vm1 = DiscountGapViewModel(
            historyRepository = DiscountGapHistoryRepository(dataSource),
            settingsRepository = SettingsRepository(dataSource)
        )
        vm1.onCurrencySelected(CurrencyRepository.EUR)
        vm1.onOriginalPriceChanged("250.00")
        vm1.onDiscountedPriceChanged("200.00")

        // Reopen with new ViewModel sharing same persistence store
        val vm2 = DiscountGapViewModel(
            historyRepository = DiscountGapHistoryRepository(dataSource),
            settingsRepository = SettingsRepository(dataSource)
        )
        assertEquals("EUR", vm2.uiState.value.currency.code)
        assertEquals("250.00", vm2.uiState.value.originalAmountInput)
        assertEquals("200.00", vm2.uiState.value.discountedAmountInput)
        assertEquals(5_000L, vm2.uiState.value.result?.savingsMinorUnits)
    }

    // 48. Reset
    @Test
    fun test48_resetRestoresDefaultsWithoutClearingHistory() {
        val viewModel = DiscountGapViewModel(
            historyRepository = DiscountGapHistoryRepository(InMemoryPreferencesDataSource()),
            settingsRepository = SettingsRepository(InMemoryPreferencesDataSource())
        )
        viewModel.onCurrencySelected(CurrencyRepository.GBP)
        viewModel.onOriginalPriceChanged("500.00")
        viewModel.onDiscountedPriceChanged("300.00")
        viewModel.saveToHistory()
        assertEquals(1, viewModel.uiState.value.history.size)

        viewModel.reset()
        val state = viewModel.uiState.value
        assertEquals("100.00", state.originalAmountInput)
        assertEquals("75.00", state.discountedAmountInput)
        assertEquals("USD", state.currency.code)
        assertEquals(2_500L, state.result?.savingsMinorUnits)
        assertEquals(1, state.history.size)
    }

    // 49. Copy formatting
    @Test
    fun test49_copyFormatting() {
        val result = DiscountGapCalculator.calculate(10_000L, 7_500L, CurrencyRepository.USD)
        val expected = """
            DiscountGap Result

            Original Price: $100.00
            Discounted Price: $75.00

            You Save: $25.00
            Discount: 25% OFF
            Remaining: 75%
        """.trimIndent()
        assertEquals(expected, DiscountGapFormatter.formatCopyText(result))
    }

    // 50. Share formatting
    @Test
    fun test50_shareFormatting() {
        val result = DiscountGapCalculator.calculate(10_000L, 7_500L, CurrencyRepository.USD)
        val expected = """
            DiscountGap

            Original: $100.00
            Discounted: $75.00

            You Save: $25.00
            Discount: 25% OFF
        """.trimIndent()
        assertEquals(expected, DiscountGapFormatter.formatShareText(result))
    }

    // 51. Quick price presets (10, 50, 100, 500, 1000)
    @Test
    fun test51_quickOriginalPricePresets() {
        val viewModel = DiscountGapViewModel(
            historyRepository = DiscountGapHistoryRepository(InMemoryPreferencesDataSource()),
            settingsRepository = SettingsRepository(InMemoryPreferencesDataSource())
        )
        viewModel.onQuickOriginalPricePreset(500L)
        assertEquals("500.00", viewModel.uiState.value.originalAmountInput)
        assertEquals(42_500L, viewModel.uiState.value.result?.savingsMinorUnits)
    }

    // 52. Discount percentage presets (including $99.99 with 20% OFF -> $79.99, Savings $20.00)
    @Test
    fun test52_discountPercentagePresets() {
        val viewModel = DiscountGapViewModel(
            historyRepository = DiscountGapHistoryRepository(InMemoryPreferencesDataSource()),
            settingsRepository = SettingsRepository(InMemoryPreferencesDataSource())
        )
        viewModel.onOriginalPriceChanged("99.99")
        viewModel.onQuickDiscountPreset(20)

        val state = viewModel.uiState.value
        assertEquals("79.99", state.discountedAmountInput)
        assertNotNull(state.result)
        assertEquals(2_000L, state.result?.savingsMinorUnits)
        assertEquals(2_000L, state.result?.discountPercentageBasisPoints)
    }

    // 53. Currency switching (no currency conversion; precision validation)
    @Test
    fun test53_currencySwitchingValidation() {
        val viewModel = DiscountGapViewModel(
            historyRepository = DiscountGapHistoryRepository(InMemoryPreferencesDataSource()),
            settingsRepository = SettingsRepository(InMemoryPreferencesDataSource())
        )
        // "100.00" has decimals which JPY (0 decimal places) does not support
        viewModel.onCurrencySelected(CurrencyRepository.JPY)
        assertNull(viewModel.uiState.value.result)
        assertEquals(
            "Review the prices for the selected currency.",
            viewModel.uiState.value.errorMessage
        )

        // When entering whole numbers for JPY, it interprets 100 as ¥100 and 75 as ¥75
        viewModel.onOriginalPriceChanged("100")
        viewModel.onDiscountedPriceChanged("75")
        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals(25L, viewModel.uiState.value.result?.savingsMinorUnits)
        assertEquals("¥25", DiscountGapFormatter.formatMoney(25L, CurrencyRepository.JPY))
    }

    // 54. Rapid calculation & double-save protection
    @Test
    fun test54_rapidCalculationAndDoubleSaveProtection() {
        val viewModel = DiscountGapViewModel(
            historyRepository = DiscountGapHistoryRepository(InMemoryPreferencesDataSource()),
            settingsRepository = SettingsRepository(InMemoryPreferencesDataSource())
        )
        // Double save of identical result should not create duplicate consecutive entries
        viewModel.saveToHistory()
        viewModel.saveToHistory()
        assertEquals(1, viewModel.uiState.value.history.size)

        // Rapid input changes ending in invalid price must clear stale result immediately
        viewModel.onOriginalPriceChanged("200.00")
        viewModel.onDiscountedPriceChanged("150.00")
        assertNotNull(viewModel.uiState.value.result)

        viewModel.onDiscountedPriceChanged("250.00")
        assertNull(viewModel.uiState.value.result)
        assertTrue(viewModel.uiState.value.calculationState is CalculationState.Error)
        assertEquals(
            "Discounted price cannot be greater than the original price.",
            viewModel.uiState.value.errorMessage
        )
    }
}
