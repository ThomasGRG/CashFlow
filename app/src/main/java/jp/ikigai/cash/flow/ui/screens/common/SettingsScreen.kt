package jp.ikigai.cash.flow.ui.screens.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import jp.ikigai.cash.flow.BuildConfig
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.data.enums.SheetType
import jp.ikigai.cash.flow.data.preferences.CashFlowPreferencesDataStore
import jp.ikigai.cash.flow.ui.components.bottombars.ThreeSlotBottomAppBar
import jp.ikigai.cash.flow.ui.components.bottomsheets.RestoreSortConfigSheet
import jp.ikigai.cash.flow.ui.components.common.OneHandModeScaffold
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferencesDataStore: CashFlowPreferencesDataStore,
    navigateBack: () -> Unit,
    navigateToImportScreen: () -> Unit,
    navigateToExportScreen: () -> Unit,
    navigateToAuditLogsScreen: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val scope = rememberCoroutineScope()

    val buildVersionName by remember {
        mutableStateOf(
            BuildConfig.VERSION_NAME
        )
    }

    val buildVersionCode by remember {
        mutableIntStateOf(
            BuildConfig.VERSION_CODE
        )
    }

    var showToastBar by remember { mutableStateOf(false) }

    var currentEvent: Event? by remember {
        mutableStateOf(null)
    }

    LaunchedEffect(showToastBar) {
        if (showToastBar) {
            delay(2000)
            showToastBar = false
        }
    }

    var sheetType by remember {
        mutableStateOf(SheetType.NONE)
    }

    OneHandModeScaffold(
        sheetState = sheetState,
        showToastBar = showToastBar,
        toastBarText = currentEvent?.let {
            stringResource(id = it.message)
        } ?: "",
        onDismissToastBar = {
            showToastBar = false
        },
        showBottomSheet = sheetType != SheetType.NONE,
        bottomSheetContent = {
            RestoreSortConfigSheet(
                restore = { selectedScreens ->
                    scope.launch {
                        preferencesDataStore.restoreDefaults(selectedScreens)
                        currentEvent = Event.RestoreSortSuccess
                        showToastBar = true
                    }
                },
                dismiss = {
                    scope
                        .launch { sheetState.hide() }
                        .invokeOnCompletion { sheetType = SheetType.NONE }
                }
            )
        },
        onDismissSheet = {
            sheetType = SheetType.NONE
        },
        topBar = { scrollBehavior, expandedHeight ->
            LargeTopAppBar(
                title = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(text = stringResource(R.string.settings_label))
                        Text(
                            text = "v$buildVersionName ($buildVersionCode)",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.alpha(0.8f)
                        )
                    }
                },
                expandedHeight = expandedHeight,
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            ThreeSlotBottomAppBar(
                navigateBack = navigateBack,
                enabled = true
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                ),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.auditing_settings_group_label),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, top = 10.dp),
                textAlign = TextAlign.Start,
            )
            Column(
                modifier = Modifier
                    .clickable {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        navigateToAuditLogsScreen()
                    }
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                Text(
                    text = stringResource(id = R.string.audit_logs_setting_label),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(id = R.string.audit_logs_setting_description),
                    modifier = Modifier.alpha(0.8f),
                    fontSize = 14.sp
                )
            }
            Text(
                text = stringResource(id = R.string.backup_restore_settings_group_label),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp),
                textAlign = TextAlign.Start,
            )
            Column(
                modifier = Modifier
                    .clickable {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        navigateToImportScreen()
                    }
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                Text(
                    text = stringResource(id = R.string.import_button_label),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(id = R.string.import_transactions_label),
                    modifier = Modifier.alpha(0.8f),
                    fontSize = 14.sp
                )
            }
            Column(
                modifier = Modifier
                    .clickable {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        navigateToExportScreen()
                    }
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                Text(
                    text = stringResource(id = R.string.export_button_label),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(id = R.string.export_transactions_label),
                    modifier = Modifier.alpha(0.8f),
                    fontSize = 14.sp
                )
            }
            Text(
                text = stringResource(id = R.string.sort_settings_group_label),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp),
                textAlign = TextAlign.Start,
            )
            Column(
                modifier = Modifier
                    .clickable {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        sheetType = SheetType.RESTORE_SORT
                    }
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                Text(
                    text = stringResource(id = R.string.restore_sort_setting_label),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(id = R.string.restore_sort_setting_description),
                    modifier = Modifier.alpha(0.8f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Preview
@Composable
fun SettingsScreenPreview() {
    val context = LocalContext.current

    SettingsScreen(
        preferencesDataStore = CashFlowPreferencesDataStore(context),
        navigateBack = {},
        navigateToImportScreen = {},
        navigateToExportScreen = {},
        navigateToAuditLogsScreen = {},
    )
}

fun NavGraphBuilder.settingsScreen(navController: NavController) {
    composable(
        route = Routes.Settings.route
    ) {
        val preferencesDataStore = koinInject<CashFlowPreferencesDataStore>()

        SettingsScreen(
            preferencesDataStore = preferencesDataStore,
            navigateBack = {
                navController.popBackStack()
            },
            navigateToImportScreen = {
                navController.navigate(Routes.ImportBackup.route) {
                    launchSingleTop = true
                }
            },
            navigateToExportScreen = {
                navController.navigate(Routes.ExportTransactions.route) {
                    launchSingleTop = true
                }
            },
            navigateToAuditLogsScreen = {
                navController.navigate(Routes.AuditLogs.route) {
                    launchSingleTop = true
                }
            },
        )
    }
}