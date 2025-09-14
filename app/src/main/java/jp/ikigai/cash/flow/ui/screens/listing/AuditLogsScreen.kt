package jp.ikigai.cash.flow.ui.screens.listing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.data.dto.audit.AuditLogListItem
import jp.ikigai.cash.flow.data.enums.SheetType
import jp.ikigai.cash.flow.data.enums.SortDirection
import jp.ikigai.cash.flow.ui.components.bottombars.AuditLogsScreenBottomAppBar
import jp.ikigai.cash.flow.ui.components.bottomsheets.DateRangePickerSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.ViewAuditLogSheet
import jp.ikigai.cash.flow.ui.components.cards.AuditLogCard
import jp.ikigai.cash.flow.ui.components.common.AuditLogGroupHeader
import jp.ikigai.cash.flow.ui.components.common.OneHandModeScaffold
import jp.ikigai.cash.flow.ui.components.common.OneHandModeSpacer
import jp.ikigai.cash.flow.ui.screenStates.common.SortConfigState
import jp.ikigai.cash.flow.ui.screenStates.listing.audit.AuditLogsScreenFiltersState
import jp.ikigai.cash.flow.ui.screenStates.listing.audit.AuditLogsScreenState
import jp.ikigai.cash.flow.ui.viewmodels.listing.AuditLogsScreenViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.ZonedDateTime
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditLogsScreen(
    navigateBack: () -> Unit,
    setStartDateAndEndDate: (ZonedDateTime?, ZonedDateTime?) -> Unit,
    setSortDirection: (SortDirection) -> Unit,
    setLocale: (Locale?) -> Unit,
    state: AuditLogsScreenState,
    filtersState: AuditLogsScreenFiltersState,
    sortConfigState: SortConfigState
) {
    val configuration = LocalConfiguration.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val locale by remember(key1 = configuration) {
        mutableStateOf(
            ConfigurationCompat.getLocales(configuration).get(0)
        )
    }

    LaunchedEffect(key1 = locale) {
        setLocale(locale)
    }

    var sheetType by remember {
        mutableStateOf(SheetType.NONE)
    }

    val loading by remember(key1 = state.loading) {
        mutableStateOf(state.loading)
    }

    val auditLogs by remember(key1 = state.auditLogs) {
        mutableStateOf(state.auditLogs)
    }

    val startDate by remember(key1 = filtersState.startDate) {
        mutableStateOf(filtersState.startDate)
    }

    val startDateString by remember(key1 = filtersState.startDateString) {
        mutableStateOf(filtersState.startDateString)
    }

    val endDate by remember(key1 = filtersState.endDate) {
        mutableStateOf(filtersState.endDate)
    }

    val endDateString by remember(key1 = filtersState.endDateString) {
        mutableStateOf(filtersState.endDateString)
    }

    val dateRangeStringRes by remember(key1 = filtersState.dateRangeStringRes) {
        mutableIntStateOf(filtersState.dateRangeStringRes)
    }

    val sortDirection by remember(key1 = sortConfigState.sortDirection) {
        mutableStateOf(sortConfigState.sortDirection)
    }

    val showEmptyPlaceholder by remember(key1 = state.auditLogs) {
        mutableStateOf(state.auditLogs.isEmpty())
    }

    var selectedAuditLog: AuditLogListItem? by remember {
        mutableStateOf(null)
    }

    OneHandModeScaffold(
        loading = loading,
        showToastBar = false,
        toastBarText = "",
        onDismissToastBar = {},
        sheetState = sheetState,
        showBottomSheet = sheetType != SheetType.NONE,
        bottomSheetContent = {
            when (sheetType) {
                SheetType.DATE_RANGE -> {
                    DateRangePickerSheet(
                        startDate = startDate,
                        endDate = endDate,
                        filter = setStartDateAndEndDate,
                        reset = {
                            setStartDateAndEndDate(null, null)
                        },
                        dismiss = {
                            scope
                                .launch { sheetState.hide() }
                                .invokeOnCompletion { sheetType = SheetType.NONE }
                        }
                    )
                }

                SheetType.AUDIT_LOG -> {
                    when (val log = selectedAuditLog) {
                        is AuditLogListItem.TransactionLog -> {
                            ViewAuditLogSheet(auditLogDetails = log)
                        }

                        is AuditLogListItem.AccountLog -> {
                            ViewAuditLogSheet(auditLogDetails = log)
                        }

                        null -> {}
                    }
                }

                else -> {}
            }
        },
        onDismissSheet = {
            sheetType = SheetType.NONE
        },
        showEmptyPlaceholder = showEmptyPlaceholder,
        emptyPlaceholderText = stringResource(id = R.string.audit_logs_screen_empty_placeholder_label),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = stringResource(id = R.string.audit_log_label))
                        Text(
                            text = stringResource(
                                id = dateRangeStringRes,
                                startDateString,
                                endDateString
                            ),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.alpha(0.8f)
                        )
                    }
                }
            )
        },
        bottomBar = {
            AuditLogsScreenBottomAppBar(
                navigateBack = navigateBack,
                sortDirection = sortDirection,
                onSortClick = {
                    if (sortDirection == SortDirection.DESC) {
                        setSortDirection(SortDirection.ASC)
                    } else {
                        setSortDirection(SortDirection.DESC)
                    }
                },
                onCalendarClick = {
                    sheetType = SheetType.DATE_RANGE
                }
            )
        },
    ) { oneHandModeBoxHeight, resetOneHandMode ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item(
                key = "one-hand-mode-expand-row",
                contentType = "row"
            ) {
                OneHandModeSpacer(oneHandModeBoxHeight = oneHandModeBoxHeight)
            }
            auditLogs.forEach { entry ->
                stickyHeader {
                    AuditLogGroupHeader(date = entry.key)
                }
                items(
                    items = entry.value,
                    key = { auditLogListItem ->
                        when (auditLogListItem) {
                            is AuditLogListItem.AccountLog -> {
                                auditLogListItem.logId
                            }

                            is AuditLogListItem.TransactionLog -> {
                                auditLogListItem.logId
                            }
                        }
                    }
                ) { auditLogListItem ->
                    when (auditLogListItem) {
                        is AuditLogListItem.AccountLog -> {
                            AuditLogCard(
                                modifier = Modifier.animateItem(),
                                auditLogDetails = auditLogListItem,
                                onClick = {
                                    resetOneHandMode()
                                    selectedAuditLog = it
                                    sheetType = SheetType.AUDIT_LOG
                                }
                            )
                        }

                        is AuditLogListItem.TransactionLog -> {
                            AuditLogCard(
                                modifier = Modifier.animateItem(),
                                auditLogDetails = auditLogListItem,
                                onClick = {
                                    resetOneHandMode()
                                    selectedAuditLog = it
                                    sheetType = SheetType.AUDIT_LOG
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun AuditLogsScreenPreview() {
    AuditLogsScreen(
        navigateBack = {},
        setStartDateAndEndDate = { _, _ -> },
        setSortDirection = {},
        setLocale = {},
        state = AuditLogsScreenState(),
        filtersState = AuditLogsScreenFiltersState(),
        sortConfigState = SortConfigState(sortField = "dateTime")
    )
}

fun NavGraphBuilder.auditLogsScreen(navController: NavController) {
    composable(
        route = Routes.AuditLogs.route
    ) {
        val viewModel: AuditLogsScreenViewModel = koinViewModel()
        val state by viewModel.state.collectAsState()
        val filtersState by viewModel.filtersState.collectAsState()
        val sortConfigState by viewModel.sortConfigState.collectAsState()

        AuditLogsScreen(
            navigateBack = {
                navController.popBackStack()
            },
            setStartDateAndEndDate = viewModel::setStartDateAndEndDate,
            setSortDirection = viewModel::setSortDirection,
            setLocale = viewModel::setLocale,
            state = state,
            filtersState = filtersState,
            sortConfigState = sortConfigState
        )
    }
}