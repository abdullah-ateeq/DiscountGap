package com.example.discountgap.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.discountgap.ui.about.AboutScreen
import com.example.discountgap.ui.history.HistoryScreen
import com.example.discountgap.ui.home.HomeScreen
import com.example.discountgap.ui.settings.SettingsScreen
import com.example.discountgap.ui.theme.DiscountGapTheme
import com.example.discountgap.util.ClipboardShareHelper
import kotlinx.coroutines.launch

enum class DiscountGapDestination(
    val route: String,
    val label: String,
    val icon: ImageVector? = null
) {
    SPLASH("splash", "Splash", null),
    HOME("home", "Home", Icons.Default.Calculate),
    HISTORY("history", "History", Icons.Default.History),
    SETTINGS("settings", "Settings", Icons.Default.Settings),
    ABOUT("about", "About", Icons.Default.Info)
}

private val BOTTOM_NAV_DESTINATIONS = listOf(
    DiscountGapDestination.HOME,
    DiscountGapDestination.HISTORY,
    DiscountGapDestination.SETTINGS,
    DiscountGapDestination.ABOUT
)

@Composable
fun DiscountGapApp(
    viewModel: DiscountGapViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current

    var isConfirmationDialogVisible by rememberSaveable { mutableStateOf(false) }

    val showBottomBar = currentRoute != null &&
        currentRoute != DiscountGapDestination.SPLASH.route &&
        !isConfirmationDialogVisible

    DiscountGapTheme(
        themeMode = uiState.settings.themeMode,
        dynamicColor = uiState.settings.dynamicColor
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        BOTTOM_NAV_DESTINATIONS.forEach { destination ->
                            val selected = currentRoute == destination.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (!selected) {
                                        ClipboardShareHelper.performHapticIfEnabled(
                                            uiState.settings.hapticFeedback,
                                            haptics
                                        )
                                        navController.navigate(destination.route) {
                                            popUpTo(DiscountGapDestination.HOME.route) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    destination.icon?.let { icon ->
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = destination.label
                                        )
                                    }
                                },
                                label = { Text(destination.label) }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = DiscountGapDestination.SPLASH.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                composable(DiscountGapDestination.SPLASH.route) {
                    SplashScreen(
                        animationsEnabled = uiState.settings.animationsEnabled,
                        onSplashFinished = {
                            navController.navigate(DiscountGapDestination.HOME.route) {
                                popUpTo(DiscountGapDestination.SPLASH.route) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(DiscountGapDestination.HOME.route) {
                    HomeScreen(
                        uiState = uiState,
                        onOriginalPriceChanged = viewModel::onOriginalPriceChanged,
                        onDiscountedPriceChanged = viewModel::onDiscountedPriceChanged,
                        onCurrencySelected = viewModel::onCurrencySelected,
                        onCustomCurrencyCreated = viewModel::onCustomCurrencyCreated,
                        onQuickOriginalPreset = viewModel::onQuickOriginalPricePreset,
                        onQuickDiscountPreset = viewModel::onQuickDiscountPreset,
                        onComparePrices = viewModel::comparePrices,
                        onSaveResult = viewModel::saveToHistory,
                        onReset = viewModel::reset,
                        onGetCopyText = viewModel::getCopyText,
                        onGetShareText = viewModel::getShareText,
                        onRestoreHistoryItem = { item ->
                            viewModel.restoreFromHistory(item)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Restored comparison from history.")
                            }
                        },
                        onViewAllHistory = {
                            navController.navigate(DiscountGapDestination.HISTORY.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onShowMessage = { message ->
                            coroutineScope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar(message)
                            }
                        }
                    )
                }

                composable(DiscountGapDestination.HISTORY.route) {
                    HistoryScreen(
                        history = uiState.history,
                        confirmDeleteEnabled = uiState.settings.confirmHistoryDelete,
                        hapticsEnabled = uiState.settings.hapticFeedback,
                        onRestoreHistoryItem = { item ->
                            viewModel.restoreFromHistory(item)
                            navController.navigate(DiscountGapDestination.HOME.route) {
                                popUpTo(DiscountGapDestination.HOME.route) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                            coroutineScope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar("Restored comparison.")
                            }
                        },
                        onDeleteHistoryItem = viewModel::deleteHistoryItem,
                        onClearAllHistory = viewModel::clearHistory,
                        onConfirmationDialogVisibilityChanged = { visible ->
                            isConfirmationDialogVisible = visible
                        }
                    )
                }

                composable(DiscountGapDestination.SETTINGS.route) {
                    SettingsScreen(
                        settings = uiState.settings,
                        onThemeModeChanged = viewModel::updateThemeMode,
                        onDynamicColorChanged = viewModel::updateDynamicColor,
                        onHapticFeedbackChanged = viewModel::updateHapticFeedback,
                        onAnimationsChanged = viewModel::updateAnimationsEnabled,
                        onDefaultCurrencyChanged = viewModel::updateDefaultCurrency,
                        onConfirmHistoryDeleteChanged = viewModel::updateConfirmHistoryDelete,
                        onPreserveLastComparisonChanged = viewModel::updatePreserveLastComparison,
                        onClearHistory = viewModel::clearHistory,
                        onResetSettings = viewModel::resetSettings,
                        onConfirmationDialogVisibilityChanged = { visible ->
                            isConfirmationDialogVisible = visible
                        },
                        onShowMessage = { message ->
                            coroutineScope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar(message)
                            }
                        }
                    )
                }

                composable(DiscountGapDestination.ABOUT.route) {
                    AboutScreen()
                }
            }
        }
    }
}
