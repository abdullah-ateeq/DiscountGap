package com.example.discountgap.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.discountgap.domain.CurrencyRepository
import com.example.discountgap.domain.DiscountGapFormatter
import com.example.discountgap.model.DiscountGapHistory
import com.example.discountgap.util.ClipboardShareHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    history: List<DiscountGapHistory>,
    confirmDeleteEnabled: Boolean,
    hapticsEnabled: Boolean,
    onRestoreHistoryItem: (DiscountGapHistory) -> Unit,
    onDeleteHistoryItem: (String) -> Unit,
    onClearAllHistory: () -> Unit,
    onConfirmationDialogVisibilityChanged: (Boolean) -> Unit
) {
    val haptics = LocalHapticFeedback.current
    var pendingDeleteItemId by rememberSaveable { mutableStateOf<String?>(null) }
    var showClearAllDialog by rememberSaveable { mutableStateOf(false) }

    val anyConfirmationActive = pendingDeleteItemId != null || showClearAllDialog
    LaunchedEffect(anyConfirmationActive) {
        onConfirmationDialogVisibilityChanged(anyConfirmationActive)
    }
    DisposableEffect(Unit) {
        onDispose {
            onConfirmationDialogVisibilityChanged(false)
        }
    }

    if (pendingDeleteItemId != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteItemId = null },
            title = { Text("Delete Comparison?") },
            text = { Text("Remove this comparison from your recent history?") },
            dismissButton = {
                TextButton(onClick = { pendingDeleteItemId = null }) {
                    Text("Cancel")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = pendingDeleteItemId
                        pendingDeleteItemId = null
                        if (id != null) {
                            ClipboardShareHelper.performHapticIfEnabled(hapticsEnabled, haptics)
                            onDeleteHistoryItem(id)
                        }
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("Clear History") },
            text = { Text("Clear all recent price comparisons?") },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearAllDialog = false
                        ClipboardShareHelper.performHapticIfEnabled(hapticsEnabled, haptics)
                        onClearAllHistory()
                    }
                ) {
                    Text(
                        text = "Clear",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "RECENT COMPARISONS",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Tap any item to restore and recalculate (up to 10 saved).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (history.isNotEmpty()) {
                OutlinedButton(
                    onClick = {
                        ClipboardShareHelper.performHapticIfEnabled(hapticsEnabled, haptics)
                        if (confirmDeleteEnabled) {
                            showClearAllDialog = true
                        } else {
                            onClearAllHistory()
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Clear")
                }
            }
        }

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "No saved comparisons yet",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Compare two prices on the Home tab and tap SAVE to keep up to 10 recent comparisons here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(history, key = { it.id }) { item ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart ||
                                value == SwipeToDismissBoxValue.StartToEnd
                            ) {
                                ClipboardShareHelper.performHapticIfEnabled(hapticsEnabled, haptics)
                                if (confirmDeleteEnabled) {
                                    pendingDeleteItemId = item.id
                                    false
                                } else {
                                    onDeleteHistoryItem(item.id)
                                    true
                                }
                            } else {
                                false
                            }
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {}
                    ) {
                        HistoryEntryCard(
                            item = item,
                            onClick = {
                                ClipboardShareHelper.performHapticIfEnabled(hapticsEnabled, haptics)
                                onRestoreHistoryItem(item)
                            },
                            onDeleteClick = {
                                ClipboardShareHelper.performHapticIfEnabled(hapticsEnabled, haptics)
                                if (confirmDeleteEnabled) {
                                    pendingDeleteItemId = item.id
                                } else {
                                    onDeleteHistoryItem(item.id)
                                }
                            }
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun HistoryEntryCard(
    item: DiscountGapHistory,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val currency = remember(item.currencyCode, item.currencySymbol) {
        CurrencyRepository.findByCode(item.currencyCode)
            ?: CurrencyRepository.createCustomCurrency(
                code = item.currencyCode,
                symbol = item.currencySymbol,
                decimalPlaces = 2
            )
    }

    val originalText = DiscountGapFormatter.formatMoney(item.originalAmountMinorUnits, currency)
    val discountedText = DiscountGapFormatter.formatMoney(item.discountedAmountMinorUnits, currency)
    val savingsText = DiscountGapFormatter.formatMoney(item.savingsMinorUnits, currency)
    val pctText = DiscountGapFormatter.formatPercentage(item.discountPercentageBasisPoints)
    val discountLabel = if (item.discountPercentageBasisPoints >= 0L) "$pctText OFF" else "—"

    val timestampFormat = remember {
        SimpleDateFormat("MMM d, h:mm a", Locale.US)
    }
    val dateString = remember(item.calculatedAt) {
        timestampFormat.format(Date(item.calculatedAt))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .semantics {
                contentDescription = "Original $originalText, Discounted $discountedText, $discountLabel, Save $savingsText. Tap to restore."
            },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "$originalText → $discountedText",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = discountLabel,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Save: $savingsText",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = "${item.currencyCode} • $dateString",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.semantics {
                    contentDescription = "Delete history item"
                }
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
