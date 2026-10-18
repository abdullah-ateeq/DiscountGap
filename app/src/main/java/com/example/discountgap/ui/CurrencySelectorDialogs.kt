package com.example.discountgap.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.discountgap.domain.CurrencyRepository
import com.example.discountgap.model.CurrencyOption

@Composable
fun CurrencyPickerDialog(
    selectedCurrency: CurrencyOption,
    onCurrencySelected: (CurrencyOption) -> Unit,
    onOtherSelected: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Currency",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(CurrencyRepository.supportedCurrencies, key = { it.code }) { option ->
                    val isSelected = selectedCurrency.code == option.code &&
                        selectedCurrency.symbol == option.symbol &&
                        selectedCurrency.decimalPlaces == option.decimalPlaces

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onCurrencySelected(option)
                            }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${option.code} (${option.symbol})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${option.name} • ${option.decimalPlaces} decimal places",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        RadioButton(
                            selected = isSelected,
                            onClick = { onCurrencySelected(option) }
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                }

                item {
                    val isCustomSelected = CurrencyRepository.supportedCurrencies.none {
                        it.code == selectedCurrency.code &&
                            it.symbol == selectedCurrency.symbol &&
                            it.decimalPlaces == selectedCurrency.decimalPlaces
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOtherSelected() }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Other",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Custom currency symbol & 0–4 decimal places",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        RadioButton(
                            selected = isCustomSelected,
                            onClick = { onOtherSelected() }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun CustomCurrencyDialog(
    initialCurrency: CurrencyOption,
    onConfirm: (code: String, symbol: String, name: String, decimalPlaces: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val isBuiltIn = CurrencyRepository.supportedCurrencies.any { it.code == initialCurrency.code }
    var code by rememberSaveable {
        mutableStateOf(if (isBuiltIn) "OTHER" else initialCurrency.code)
    }
    var symbol by rememberSaveable {
        mutableStateOf(if (isBuiltIn) "¤" else initialCurrency.symbol)
    }
    var name by rememberSaveable {
        mutableStateOf(if (isBuiltIn) "Custom Currency" else initialCurrency.name)
    }
    var decimalPlaces by rememberSaveable {
        mutableIntStateOf(initialCurrency.decimalPlaces.coerceIn(0, 4))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Custom Currency (Other)",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { if (it.length <= 6) code = it.uppercase() },
                    label = { Text("Currency Code (e.g. KWD, CHF)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = symbol,
                    onValueChange = { if (it.length <= 6) symbol = it },
                    label = { Text("Symbol (e.g. KD, CHF, ¤)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 32) name = it },
                    label = { Text("Currency Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Column {
                    Text(
                        text = "Decimal Places (0–4)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        (0..4).forEach { decimals ->
                            FilterChip(
                                selected = decimalPlaces == decimals,
                                onClick = { decimalPlaces = decimals },
                                label = { Text(decimals.toString()) }
                            )
                        }
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        confirmButton = {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                TextButton(
                    onClick = {
                        onConfirm(
                            code.trim().ifEmpty { "OTHER" },
                            symbol.trim().ifEmpty { "¤" },
                            name.trim().ifEmpty { "Custom Currency" },
                            decimalPlaces.coerceIn(0, 4)
                        )
                    }
                ) {
                    Text(
                        text = "Apply",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    )
}
