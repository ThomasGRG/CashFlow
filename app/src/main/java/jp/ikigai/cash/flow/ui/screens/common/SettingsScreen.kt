package jp.ikigai.cash.flow.ui.screens.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.ui.components.bottombars.ThreeSlotRoundedBottomBar
import jp.ikigai.cash.flow.ui.components.common.OneHandModeScaffold
import jp.ikigai.cash.flow.utils.animatedComposable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navigateBack: () -> Unit,
    navigateToExportScreen: () -> Unit,
    navigateToImportScreen: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    OneHandModeScaffold(
        loading = false,
        emptyPlaceholderText = "",
        showEmptyPlaceholder = false,
        showToastBar = false,
        toastBarText = "",
        onDismissToastBar = {},
        showBottomPopup = false,
        bottomPopupContent = {},
        onDismissPopup = {},
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.settings_label))
                }
            )
        },
        bottomBar = {
            ThreeSlotRoundedBottomBar(
                navigateBack = navigateBack,
                enabled = true
            )
        }
    ) { _, _ ->
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    navigateToImportScreen()
                },
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp, top = 10.dp)
                    .height(50.dp)
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.small
            ) {
                Text(text = stringResource(R.string.import_transactions_label))
            }
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    navigateToExportScreen()
                },
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp, top = 10.dp)
                    .height(50.dp)
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.small
            ) {
                Text(text = stringResource(R.string.export_transactions_label))
            }
        }
    }
}

@Preview
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(
        navigateBack = {},
        navigateToExportScreen = {},
        navigateToImportScreen = {},
    )
}

fun NavGraphBuilder.settingsScreen(navController: NavController) {
    animatedComposable(
        route = Routes.Settings.route
    ) {
        SettingsScreen(
            navigateBack = {
                navController.popBackStack()
            },
            navigateToExportScreen = {
                navController.navigate(Routes.ExportTransactions.route) {
                    launchSingleTop = true
                }
            },
            navigateToImportScreen = {
                navController.navigate(Routes.ImportBackup.route) {
                    launchSingleTop = true
                }
            },
        )
    }
}