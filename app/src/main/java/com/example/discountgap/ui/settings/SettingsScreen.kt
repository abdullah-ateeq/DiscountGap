package com.example.discountgap.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.discountgap.domain.CurrencyRepository
import com.example.discountgap.model.AppSettings
import com.example.discountgap.model.AppThemeMode
import com.example.discountgap.model.CurrencyOption
import com.example.discountgap.ui.CurrencyPickerDialog
import com.example.discountgap.ui.CustomCurrencyDialog
import com.example.discountgap.util.ClipboardShareHelper

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onThemeModeChanged: (AppThemeMode) -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit,
    onHapticFeedbackChanged: (Boolean) -> Unit,
    onAnimationsChanged: (Boolean) -> Unit,
    onDefaultCurrencyChanged: (CurrencyOption) -> Unit,
    onConfirmHistoryDeleteChanged: (Boolean) -> Unit,
    onPreserveLastComparisonChanged: (Boolean) -> Unit,
    onClearHistory: () -> Unit,
    onResetSettings: () -> Unit,
    onConfirmationDialogVisibilityChanged: (Boolean) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val hapticsEnabled = settings.hapticFeedback

    var showCurrencyPicker by rememberSaveable { mutableStateOf(false) }
    var showCustomCurrencyDialog by rememberSaveable { mutableStateOf(false) }
    var showClearHistoryDialog by rememberSaveable { mutableStateOf(false) }
    var showResetSettingsDialog by rememberSaveable { mutableStateOf(false) }

    val anyConfirmationActive = showClearHistoryDialog || showResetSettingsDialog
    LaunchedEffect(anyConfirmationActive) {
        onConfirmationDialogVisibilityChanged(anyConfirmationActive)
    }
    DisposableEffect(Unit) {
        onDispose {
            onConfirmationDialogVisibilityChanged(false)
        }
    }

    if (showCurrencyPicker) {
        CurrencyPickerDialog(
            selectedCurrency = settings.defaultCurrency,
            onCurrencySelected = { selected ->
                ClipboardShareHelper.performHapticIfEnabled(hapticsEnabled, haptics)
                showCurrencyPicker = false
                onDefaultCurrencyChanged(selected)
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
            initialCurrency = settings.defaultCurrency,
            onConfirm = { code, symbol, name, decimals ->
                ClipboardShareHelper.performHapticIfEnabled(hapticsEnabled, haptics)
                showCustomCurrencyDialog = false
                val custom = CurrencyRepository.createCustomCurrency(
                    code = code,
                    symbol = symbol,
                    name = name,
                    decimalPlaces = decimals
                )
                onDefaultCurrencyChanged(custom)
            },
            onDismiss = { showCustomCurrencyDialog = false }
        )
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear History") },
            text = { Text("Clear all recent price comparisons?") },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearHistoryDialog = false
                        ClipboardShareHelper.performHapticIfEnabled(hapticsEnabled, haptics)
                        onClearHistory()
                        onShowMessage("Cleared all recent price comparisons.")
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

    if (showResetSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showResetSettingsDialog = false },
            title = { Text("Reset Settings") },
            text = { Text("Restore all settings to their default values?") },
            dismissButton = {
                TextButton(onClick = { showResetSettingsDialog = false }) {
                    Text("Cancel")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetSettingsDialog = false
                        ClipboardShareHelper.performHapticIfEnabled(hapticsEnabled, haptics)
                        onResetSettings()
                        onShowMessage("Settings restored to defaults.")
                    }
                ) {
                    Text(
                        text = "Reset",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Customize theme, currency defaults, and behavior.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Appearance Card
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "APPEARANCE",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Theme",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppThemeMode.entries.forEach { mode ->
                            FilterChip(
                                selected = settings.themeMode == mode,
                                onClick = {
                                    ClipboardShareHelper.performHapticIfEnabled(hapticsEnabled, haptics)
                                    onThemeModeChanged(mode)
                                },
                                label = { Text(mode.displayName) }
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))

                SettingSwitchRow(
                    title = "Dynamic Color",
                    subtitle = "Use Material You wallpaper colors on Android 12+",
                    checked = settings.dynamicColor,
                    onCheckedChange = {
                        ClipboardShareHelper.performHapticIfEnabled(hapticsEnabled, haptics)
                        onDynamicColorChanged(it)
                    }
                )
            }
        }

        // Preferences Card
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "PREFERENCES",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Default Currency",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${settings.defaultCurrency.name} (${settings.defaultCurrency.decimalPlaces} decimals)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FilledTonalButton(
                        onClick = {
                            ClipboardShareHelper.performHapticIfEnabled(hapticsEnabled, haptics)
                            showCurrencyPicker = true
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("${settings.defaultCurrency.code} (${settings.defaultCurrency.symbol})")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))

                SettingSwitchRow(
                    title = "Haptic Feedback",
                    subtitle = "Subtle tactile response when tapping presets and actions",
                    checked = settings.hapticFeedback,
                    onCheckedChange = {
                        onHapticFeedbackChanged(it)
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))

                SettingSwitchRow(
                    title = "Animations",
                    subtitle = "Smooth transitions for savings and discount bar",
                    checked = settings.animationsEnabled,
                    onCheckedChange = {
                        ClipboardShareHelper.performHapticIfEnabled(hapticsEnabled, haptics)
                        onAnimationsChanged(it)
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))

                SettingSwitchRow(
                    title = "Confirm History Delete",
                    subtitle = "Ask before deleting recent price comparisons",
                    checked = settings.confirmHistoryDelete,
                    onCheckedChange = {
                        ClipboardShareHelper.performHapticIfEnabled(hapticsEnabled, haptics)
                        onConfirmHistoryDeleteChanged(it)
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))

                SettingSwitchRow(
                    title = "Preserve Last Comparison",
                    subtitle = "Restore your last entered original, discounted price, and currency when opening the app",
                    checked = settings.preserveLastComparison,
                    onCheckedChange = {
                        ClipboardShareHelper.performHapticIfEnabled(hapticsEnabled, haptics)
                        onPreserveLastComparisonChanged(it)
                    }
                )
            }
        }

        // Data & Reset Card
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "DATA & DEFAULTS",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedButton(
                    onClick = {
                        ClipboardShareHelper.performHapticIfEnabled(hapticsEnabled, haptics)
                        showClearHistoryDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Clear History",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedButton(
                    onClick = {
                        ClipboardShareHelper.performHapticIfEnabled(hapticsEnabled, haptics)
                        showResetSettingsDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Reset Settings",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
