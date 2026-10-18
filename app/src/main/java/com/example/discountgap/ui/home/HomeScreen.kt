package com.example.discountgap.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.discountgap.domain.CurrencyRepository
import com.example.discountgap.domain.DiscountGapFormatter
import com.example.discountgap.model.CurrencyOption
import com.example.discountgap.model.DiscountGapHistory
import com.example.discountgap.model.DiscountGapResult
import com.example.discountgap.model.DiscountGapUiState
import com.example.discountgap.ui.CurrencyPickerDialog
import com.example.discountgap.ui.CustomCurrencyDialog
import com.example.discountgap.ui.DiscountGapViewModel
import com.example.discountgap.util.ClipboardShareHelper
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    uiState: DiscountGapUiState,
    onOriginalPriceChanged: (String) -> Unit,
    onDiscountedPriceChanged: (String) -> Unit,
    onCurrencySelected: (CurrencyOption) -> Unit,
    onCustomCurrencyCreated: (String, String, String, Int) -> Unit,
    onQuickOriginalPreset: (Long) -> Unit,
    onQuickDiscountPreset: (Int) -> Unit,
    onComparePrices: () -> Unit,
    onSaveResult: () -> Boolean,
    onReset: () -> Unit,
    onGetCopyText: () -> String?,
    onGetShareText: () -> String?,
    onRestoreHistoryItem: (DiscountGapHistory) -> Unit,
    onViewAllHistory: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val haptics = LocalHapticFeedback.current
    val hapticsEnabled = uiState.settings.hapticFeedback
    val animationsEnabled = uiState.settings.animationsEnabled

    var showCurrencyPicker by rememberSaveable { mutableStateOf(false) }
    var showCustomCurrencyDialog by rememberSaveable { mutableStateOf(false) }

    if (showCurrencyPicker) {
        CurrencyPickerDialog(
            selectedCurrency = uiState.currency,
            onCurrencySelected = { selected ->
                ClipboardShareHelper.performHapticIfEnabled(hapticsEnabled, haptics)
                showCurrencyPicker = false
                onCurrencySelected(selected)
            },
            onOtherSelected = {
                ClipboardShareHelper.performHapticIfEnabled(hapticsEnabled, haptics)
                showCurrencyPicker = false
                showCustomCurrencyDialog = true
            },
            onDismiss = { showCurrencyPicker = false }
        )
    }

    if (showCustomCurrencyDialog) {
        CustomCurrencyDialog(
            initialCurrency = uiState.currency,
            onConfirm = { code, symbol, name, decimals ->
                ClipboardShareHelper.performHapticIfEnabled(hapticsEnabled, haptics)
                showCustomCurrencyDialog = false
                onCustomCurrencyCreated(code, symbol, name, decimals)
            },
            onDismiss = { showCustomCurrencyDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "DiscountGap",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Compare original and discounted prices.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Price Input Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Original Price Row + Currency Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ORIGINAL PRICE",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    FilledTonalButton(
                        onClick = {
                            ClipboardShareHelper.performHapticIfEnabled(hapticsEnabled, haptics)
                            showCurrencyPicker = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.semantics {
                            contentDescription = "Selected currency ${uiState.currency.code}. Tap to change currency."
                        }
                    ) {
                        Text(
                            text = "Currency: ${uiState.currency.code} (${uiState.currency.symbol})",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                OutlinedTextField(
                    value = uiState.originalAmountInput,
                    onValueChange = onOriginalPriceChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "Original price, ${uiState.originalAmountInput} ${uiState.currency.name}."
                        },
                    prefix = {
                        Text(
                            text = "${uiState.currency.symbol} ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    placeholder = {
                        Text(if (uiState.currency.decimalPlaces == 0) "100" else "100.00")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    textStyle = MaterialTheme.typography.titleLarge,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (uiState.currency.decimalPlaces == 0) {
                            KeyboardType.Number
                        } else {
                            KeyboardType.Decimal
                        },
                        imeAction = ImeAction.Next
                    )
                )

                // Quick Original Price Presets
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Quick Original Price Presets",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val numberFormatter = NumberFormat.getIntegerInstance(Locale.US)
                        DiscountGapViewModel.QUICK_ORIGINAL_PRESETS.forEach { presetMajor ->
                            val label = numberFormatter.format(presetMajor)
                            AssistChip(
                                onClick = {
                                    ClipboardShareHelper.performHapticIfEnabled(hapticsEnabled, haptics)
                                    onQuickOriginalPreset(presetMajor)
                                },
                                label = {
                                    Text(
                                        text = "${uiState.currency.symbol}$label",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                },
                                modifier = Modifier.semantics {
                                    contentDescription = "Set original price to $label ${uiState.currency.code}"
                                }
                            )
                        }
                    }
                }

                // Visual Downward Arrow
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "↓",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Discounted Price Field
                Text(
                    text = "DISCOUNTED PRICE",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = uiState.discountedAmountInput,
                    onValueChange = onDiscountedPriceChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "Discounted price, ${uiState.discountedAmountInput} ${uiState.currency.name}."
                        },
                    prefix = {
                        Text(
                            text = "${uiState.currency.symbol} ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    placeholder = {
                        Text(if (uiState.currency.decimalPlaces == 0) "75" else "75.00")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    textStyle = MaterialTheme.typography.titleLarge,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (uiState.currency.decimalPlaces == 0) {
                            KeyboardType.Number
                        } else {
                            KeyboardType.Decimal
                        },
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            onComparePrices()
                        }
                    )
                )

                // Quick Discount Presets
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Quick Discount Presets",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        DiscountGapViewModel.QUICK_DISCOUNT_PRESETS.forEach { percent ->
                            FilterChip(
                                selected = uiState.selectedPresetPercent == percent,
                                onClick = {
                                    ClipboardShareHelper.performHapticIfEnabled(hapticsEnabled, haptics)
                                    onQuickDiscountPreset(percent)
                                },
                                label = {
                                    Text(
                                        text = "${percent}% OFF",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                },
                                modifier = Modifier.semantics {
                                    contentDescription = "Apply $percent percent discount preset"
                                }
                            )
                        }
                    }
                }

                // Primary Action Button: COMPARE PRICES
                Button(
                    onClick = {
                        ClipboardShareHelper.performHapticIfEnabled(hapticsEnabled, haptics)
                        focusManager.clearFocus()
                        onComparePrices()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .semantics {
                            contentDescription = "Compare prices."
                        },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "COMPARE PRICES",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Error Banner (when validation fails)
        if (uiState.errorMessage != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Error: ${uiState.errorMessage}"
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = uiState.errorMessage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Result Section
        val currentResult = uiState.result
        if (currentResult != null) {
            MainResultCard(
                result = currentResult,
                animationsEnabled = animationsEnabled
            )

            PriceComparisonCard(
                result = currentResult
            )

            DiscountBarCard(
                result = currentResult,
                animationsEnabled = animationsEnabled
            )

            // Result Action Buttons: SAVE, COPY, SHARE, RESET
            ResultActionsRow(
                onSave = {
                    ClipboardShareHelper.performHapticIfEnabled(hapticsEnabled, haptics)
                    if (onSaveResult()) {
                        onShowMessage("Comparison saved to history.")
                    }
                },
                onCopy = {
                    ClipboardShareHelper.performHapticIfEnabled(hapticsEnabled, haptics)
                    val copyText = onGetCopyText()
                    if (copyText != null) {
                        ClipboardShareHelper.copyToClipboard(context, copyText)
                        onShowMessage("Copied result to clipboard.")
                    }
                },
                onShare = {
                    ClipboardShareHelper.performHapticIfEnabled(hapticsEnabled, haptics)
                    val shareText = onGetShareText()
                    if (shareText != null) {
                        ClipboardShareHelper.shareText(context, shareText)
                    }
                },
                onReset = {
                    ClipboardShareHelper.performHapticIfEnabled(hapticsEnabled, haptics)
                    onReset()
                    onShowMessage("Restored default comparison.")
                }
            )
        }

        // Recent Comparisons Quick Preview on Home
        if (uiState.history.isNotEmpty()) {
            RecentComparisonsSection(
                history = uiState.history.take(3),
                onSelectHistory = { item ->
                    ClipboardShareHelper.performHapticIfEnabled(hapticsEnabled, haptics)
                    onRestoreHistoryItem(item)
                },
                onViewAll = {
                    ClipboardShareHelper.performHapticIfEnabled(hapticsEnabled, haptics)
                    onViewAllHistory()
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun MainResultCard(
    result: DiscountGapResult,
    animationsEnabled: Boolean
) {
    val savingsText = DiscountGapFormatter.formatMoney(result.savingsMinorUnits, result.currency)
    val originalText = DiscountGapFormatter.formatMoney(result.originalAmountMinorUnits, result.currency)
    val discountedText = DiscountGapFormatter.formatMoney(result.discountedAmountMinorUnits, result.currency)
    val discountBadgeText = DiscountGapFormatter.formatDiscountBadge(result)
    val remainingText = DiscountGapFormatter.formatRemainingPercentage(result)

    val savingsAccessibility = "You save $savingsText."
    val discountAccessibility = DiscountGapFormatter.formatAccessibilityDiscount(result)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "YOU SAVE",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )

            if (animationsEnabled) {
                AnimatedContent(
                    targetState = savingsText,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(200))
                    },
                    label = "savingsAnimation"
                ) { animatedSavings ->
                    Text(
                        text = animatedSavings,
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.semantics {
                            contentDescription = savingsAccessibility
                        }
                    )
                }
            } else {
                Text(
                    text = savingsText,
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics {
                        contentDescription = savingsAccessibility
                    }
                )
            }

            // Priority 2: Discount Percentage Badge
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.semantics {
                    contentDescription = discountAccessibility
                }
            ) {
                Text(
                    text = discountBadgeText,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            if (result.isZeroOriginal) {
                Text(
                    text = "Discount percentage unavailable",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            if (result.isNoPriceDifference) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "NO PRICE DIFFERENCE",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.16f)
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Original, Discounted, Remaining Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryMetricColumn(
                    label = "Original",
                    value = originalText,
                    alignment = Alignment.Start
                )
                SummaryMetricColumn(
                    label = "Discounted",
                    value = discountedText,
                    alignment = Alignment.CenterHorizontally
                )
                SummaryMetricColumn(
                    label = "Remaining",
                    value = remainingText,
                    alignment = Alignment.End
                )
            }
        }
    }
}

@Composable
private fun SummaryMetricColumn(
    label: String,
    value: String,
    alignment: Alignment.Horizontal
) {
    Column(horizontalAlignment = alignment) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PriceComparisonCard(
    result: DiscountGapResult
) {
    val originalText = DiscountGapFormatter.formatMoney(result.originalAmountMinorUnits, result.currency)
    val savingsText = DiscountGapFormatter.formatMoney(result.savingsMinorUnits, result.currency)
    val discountedText = DiscountGapFormatter.formatMoney(result.discountedAmountMinorUnits, result.currency)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "PRICE COMPARISON",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = originalText,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "ORIGINAL",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "↓",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "-$savingsText",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "SAVINGS",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Text(
                text = "↓",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = discountedText,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "DISCOUNTED",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DiscountBarCard(
    result: DiscountGapResult,
    animationsEnabled: Boolean
) {
    val discountLabel = DiscountGapFormatter.formatDiscountBadge(result)
    val remainingPercentageText = DiscountGapFormatter.formatRemainingPercentage(result)
    val targetDiscountFraction = DiscountGapFormatter.getDiscountBarFraction(result)

    val animatedDiscountFraction by animateFloatAsState(
        targetValue = targetDiscountFraction,
        animationSpec = tween(durationMillis = if (animationsEnabled) 300 else 0),
        label = "discountBarFraction"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = discountLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (result.isPercentageAvailable) {
                        "$remainingPercentageText OF ORIGINAL PRICE REMAINS"
                    } else {
                        "PERCENTAGE UNAVAILABLE"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Horizontal visualization bar: Discount vs Remaining
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = animatedDiscountFraction)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Original: 100%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Discount: ${DiscountGapFormatter.formatPercentage(result.discountPercentageBasisPoints)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Remaining: $remainingPercentageText",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResultActionsRow(
    onSave: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onReset: () -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 2
    ) {
        FilledTonalButton(
            onClick = onSave,
            modifier = Modifier
                .weight(1f)
                .semantics { contentDescription = "Save comparison to history" },
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.BookmarkBorder, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("SAVE", fontWeight = FontWeight.Bold)
        }

        FilledTonalButton(
            onClick = onCopy,
            modifier = Modifier
                .weight(1f)
                .semantics { contentDescription = "Copy result to clipboard" },
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.ContentCopy, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("COPY", fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
            onClick = onShare,
            modifier = Modifier
                .weight(1f)
                .semantics { contentDescription = "Share comparison result" },
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("SHARE", fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
            onClick = onReset,
            modifier = Modifier
                .weight(1f)
                .semantics { contentDescription = "Reset comparison to defaults" },
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("RESET", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RecentComparisonsSection(
    history: List<DiscountGapHistory>,
    onSelectHistory: (DiscountGapHistory) -> Unit,
    onViewAll: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT COMPARISONS",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(onClick = onViewAll) {
                    Text("View All")
                }
            }

            history.forEach { item ->
                val currency = CurrencyRepository.findByCode(item.currencyCode)
                    ?: CurrencyRepository.createCustomCurrency(
                        code = item.currencyCode,
                        symbol = item.currencySymbol,
                        decimalPlaces = 2
                    )
                val origStr = DiscountGapFormatter.formatMoney(item.originalAmountMinorUnits, currency)
                val discStr = DiscountGapFormatter.formatMoney(item.discountedAmountMinorUnits, currency)
                val saveStr = DiscountGapFormatter.formatMoney(item.savingsMinorUnits, currency)
                val pctStr = DiscountGapFormatter.formatPercentage(item.discountPercentageBasisPoints)

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectHistory(item) },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "$origStr → $discStr",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Save: $saveStr",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = if (item.discountPercentageBasisPoints >= 0L) "$pctStr OFF" else "—",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
