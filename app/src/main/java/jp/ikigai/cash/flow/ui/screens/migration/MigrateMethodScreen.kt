package jp.ikigai.cash.flow.ui.screens.migration

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import jp.ikigai.cash.flow.MethodWithTransactionMetadata
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.data.enums.SheetType
import jp.ikigai.cash.flow.data.enums.SortDirection
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.ui.components.bottombars.MigrateMethodScreenBottomAppBar
import jp.ikigai.cash.flow.ui.components.bottomsheets.AmountFilterSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.DateRangePickerSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.FilterAccountSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.FilterCategorySheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.FilterCounterPartySheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.FilterCurrencySheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.FilterTransactionTypeSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.MigrateMethodSheet
import jp.ikigai.cash.flow.ui.components.cards.TransactionCard
import jp.ikigai.cash.flow.ui.components.common.LandscapeScaffold
import jp.ikigai.cash.flow.ui.components.common.OneHandModeScaffold
import jp.ikigai.cash.flow.ui.components.common.SearchBox
import jp.ikigai.cash.flow.ui.components.common.TransactionGroupHeader
import jp.ikigai.cash.flow.ui.screenStates.common.SortConfigState
import jp.ikigai.cash.flow.ui.screenStates.common.TransactionFilters
import jp.ikigai.cash.flow.ui.screenStates.migration.MigrateMethodScreenState
import jp.ikigai.cash.flow.ui.viewmodels.migration.MigrateMethodScreenViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MigrateMethodScreen(
    navigateBack: () -> Unit,
    migrate: (MethodWithTransactionMetadata) -> Unit,
    toggleSelection: () -> Unit,
    toggleTransactionSelected: (Long) -> Unit,
    toggleLocalDateSelected: (LocalDate) -> Unit,
    searchState: String,
    setSearchText: (String) -> Unit,
    setLocale: (Locale?) -> Unit,
    setSelectedCurrencies: (Set<String>) -> Unit,
    setStartDateAndEndDate: (ZonedDateTime?, ZonedDateTime?) -> Unit,
    setSelectedAccounts: (Set<Long>) -> Unit,
    setSelectedCategories: (Set<Long>) -> Unit,
    setSelectedCounterParties: (Set<Long>, Boolean) -> Unit,
    setSelectedTransactionTypes: (List<TransactionType>) -> Unit,
    setSortDirection: (SortDirection) -> Unit,
    filterByAmount: (Double, Double) -> Unit,
    events: Flow<Event>,
    state: MigrateMethodScreenState,
    filtersState: TransactionFilters,
    sortConfigState: SortConfigState
) {
    val configuration = LocalConfiguration.current
    val haptics = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val focusRequester = remember {
        FocusRequester()
    }

    val interactionSource = remember {
        MutableInteractionSource()
    }

    val isFocused by interactionSource.collectIsFocusedAsState()

    val locale by remember(key1 = configuration) {
        mutableStateOf(
            ConfigurationCompat.getLocales(configuration).get(0)
        )
    }

    LaunchedEffect(key1 = locale) {
        setLocale(locale)
    }

    var showToastBar by remember { mutableStateOf(false) }

    var currentEvent: Event? by remember {
        mutableStateOf(null)
    }

    LaunchedEffect(Unit) {
        events.collectLatest { event ->
            showToastBar = false
            currentEvent = event
            showToastBar = true
        }
    }

    LaunchedEffect(showToastBar) {
        if (showToastBar) {
            delay(2000)
            showToastBar = false
            if (currentEvent == Event.MigrationSuccess) {
                navigateBack()
            }
        }
    }

    var sheetType by remember {
        mutableStateOf(SheetType.NONE)
    }

    val searchText by remember(key1 = searchState) {
        mutableStateOf(searchState)
    }

    val loading by remember(key1 = state.loading) {
        mutableStateOf(state.loading)
    }

    val enabled by remember(key1 = state.enabled) {
        mutableStateOf(state.enabled)
    }

    val transactions by remember(
        key1 = state.filteredTransactions,
        key2 = state.transactionsHashCode
    ) {
        mutableStateOf(state.filteredTransactions)
    }

    val selectedTransactions by remember(key1 = state.selectedTransactions) {
        mutableStateOf(state.selectedTransactions)
    }

    val selectedTransactionCount by remember(key1 = state.selectedTransactionCount) {
        mutableIntStateOf(state.selectedTransactionCount)
    }

    val selectedLocalDates by remember(key1 = state.selectedLocalDates) {
        mutableStateOf(state.selectedLocalDates)
    }

    val allSelected by remember(key1 = state.allSelected) {
        mutableStateOf(state.allSelected)
    }

    val currencies by remember(key1 = state.currencies) {
        mutableStateOf(state.currencies)
    }

    val selectedCurrencies by remember(key1 = filtersState.selectedCurrencies) {
        mutableStateOf(filtersState.selectedCurrencies)
    }

    val selectedCurrencyCount by remember(key1 = filtersState.selectedCurrencyCount) {
        mutableStateOf(filtersState.selectedCurrencyCount)
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

    val accounts by remember(key1 = state.accounts) {
        mutableStateOf(state.accounts)
    }

    val selectedAccounts by remember(key1 = filtersState.selectedAccounts) {
        mutableStateOf(filtersState.selectedAccounts)
    }

    val selectedAccountCount by remember(key1 = filtersState.selectedAccountCount) {
        mutableStateOf(filtersState.selectedAccountCount)
    }

    val categories by remember(key1 = state.categories) {
        mutableStateOf(state.categories)
    }

    val selectedCategories by remember(key1 = filtersState.selectedCategories) {
        mutableStateOf(filtersState.selectedCategories)
    }

    val selectedCategoryCount by remember(key1 = filtersState.selectedCategoryCount) {
        mutableStateOf(filtersState.selectedCategoryCount)
    }

    val counterParties by remember(key1 = state.counterParties) {
        mutableStateOf(state.counterParties)
    }

    val selectedCounterParties by remember(key1 = filtersState.selectedCounterParties) {
        mutableStateOf(filtersState.selectedCounterParties)
    }

    val selectedCounterPartyCount by remember(key1 = filtersState.selectedCounterPartyCount) {
        mutableStateOf(filtersState.selectedCounterPartyCount)
    }

    val includeNoCounterPartyTransactions by remember(key1 = filtersState.includeNoCounterPartyTransactions) {
        mutableStateOf(filtersState.includeNoCounterPartyTransactions)
    }

    val methods by remember(key1 = state.methods) {
        mutableStateOf(state.methods)
    }

    val selectedTransactionTypes by remember(key1 = filtersState.selectedTransactionTypes) {
        mutableStateOf(filtersState.selectedTransactionTypes)
    }

    val filterAmountMin by remember(key1 = filtersState.filterAmountMin) {
        mutableDoubleStateOf(filtersState.filterAmountMin)
    }

    val filterAmountMax by remember(key1 = filtersState.filterAmountMax) {
        mutableDoubleStateOf(filtersState.filterAmountMax)
    }

    val filterAmountRange by remember(key1 = filtersState.filterAmountRange) {
        mutableStateOf(filtersState.filterAmountRange)
    }

    val sortDirection by remember(key1 = sortConfigState.sortDirection) {
        mutableStateOf(sortConfigState.sortDirection)
    }

    val showEmptyPlaceholder by remember(key1 = state.filteredTransactions) {
        mutableStateOf(state.filteredTransactions.isEmpty())
    }

    val migrateEnabled by remember(
        key1 = state.selectedTransactionCount,
        key2 = state.enabled
    ) {
        mutableStateOf(state.enabled && state.selectedTransactionCount > 0)
    }

    if (windowSizeClass.windowHeightSizeClass == WindowHeightSizeClass.COMPACT && windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.MEDIUM) {
        LandscapeScaffold(
            loading = loading,
            showToastBar = showToastBar,
            toastBarText = currentEvent?.let {
                stringResource(id = it.message)
            } ?: "",
            onDismissToastBar = {
                showToastBar = false
                if (currentEvent == Event.MigrationSuccess) {
                    navigateBack()
                }
            },
            showEmptyPlaceholder = showEmptyPlaceholder,
            emptyPlaceholderText = if (searchText.isNotBlank()) {
                stringResource(id = R.string.choose_icon_screen_empty_placeholder_label, searchText)
            } else {
                stringResource(id = R.string.no_results_found_filters_placeholder_label)
            },
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

                    SheetType.CURRENCY -> {
                        FilterCurrencySheet(
                            selectedCurrencyCodes = selectedCurrencies,
                            currencies = currencies,
                            filter = setSelectedCurrencies,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion { sheetType = SheetType.NONE }
                            }
                        )
                    }

                    SheetType.AMOUNT -> {
                        AmountFilterSheet(
                            minAmount = filterAmountMin,
                            maxAmount = filterAmountMax,
                            filter = filterByAmount,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion { sheetType = SheetType.NONE }
                            }
                        )
                    }

                    SheetType.CATEGORY -> {
                        FilterCategorySheet(
                            selectedCategoryIds = selectedCategories,
                            categories = categories,
                            filter = setSelectedCategories,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion { sheetType = SheetType.NONE }
                            }
                        )
                    }

                    SheetType.COUNTERPARTY -> {
                        FilterCounterPartySheet(
                            selectedCounterPartyIds = selectedCounterParties,
                            includeNoCounterPartyTransactions = includeNoCounterPartyTransactions,
                            counterParties = counterParties,
                            filter = setSelectedCounterParties,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion { sheetType = SheetType.NONE }
                            }
                        )
                    }

                    SheetType.METHOD -> {
                        MigrateMethodSheet(
                            migrateCount = selectedTransactionCount,
                            selectMethod = migrate,
                            methods = methods,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion { sheetType = SheetType.NONE }
                            }
                        )
                    }

                    SheetType.ACCOUNT -> {
                        FilterAccountSheet(
                            selectedAccountIds = selectedAccounts,
                            accounts = accounts,
                            filter = setSelectedAccounts,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion { sheetType = SheetType.NONE }
                            }
                        )
                    }

                    SheetType.TYPE -> {
                        FilterTransactionTypeSheet(
                            selectedTransactionTypes = selectedTransactionTypes,
                            filter = setSelectedTransactionTypes,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion { sheetType = SheetType.NONE }
                            }
                        )
                    }

                    else -> {}
                }
            },
            onDismissSheet = {
                sheetType = SheetType.NONE
            },
            firstColContent = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 10.dp, end = 10.dp, bottom = 10.dp, top = 2.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    SearchBox(
                        searchText = searchText,
                        setSearchText = setSearchText,
                        enabled = enabled,
                        focusRequester = focusRequester,
                        interactionSource = interactionSource
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    MigrateMethodScreenBottomAppBar(
                        title = stringResource(id = R.string.migrate_transactions_label),
                        subTitle = stringResource(
                            id = dateRangeStringRes,
                            startDateString,
                            endDateString
                        ),
                        navigateBack = navigateBack,
                        enabled = enabled,
                        migrateEnabled = migrateEnabled,
                        allSelected = allSelected,
                        sortDirection = sortDirection,
                        filterAmount = filterAmountRange,
                        selectedCurrencyCount = selectedCurrencyCount,
                        selectedAccountCount = selectedAccountCount,
                        selectedCategoryCount = selectedCategoryCount,
                        selectedCounterPartyCount = selectedCounterPartyCount,
                        counterPartyFilterVisible = counterParties.isNotEmpty(),
                        selectedTransactionTypeCount = selectedTransactionTypes.size,
                        onSortClick = {
                            if (sortDirection == SortDirection.DESC) {
                                setSortDirection(SortDirection.ASC)
                            } else {
                                setSortDirection(SortDirection.DESC)
                            }
                        },
                        onToggleSelectClick = toggleSelection,
                        setSheetType = {
                            sheetType = it
                        },
                    )
                }
            },
            secondColContent = {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    transactions.forEach { entry ->
                        stickyHeader {
                            TransactionGroupHeader(
                                date = entry.key,
                                selected = selectedLocalDates.contains(entry.key),
                                enabled = enabled,
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    toggleLocalDateSelected(entry.key)
                                }
                            )
                        }
                        items(
                            items = entry.value,
                            key = { transactionWithChips -> transactionWithChips.id }
                        ) { transactionWithChips ->
                            TransactionCard(
                                checked = selectedTransactions.contains(transactionWithChips.id),
                                enabled = enabled,
                                transactionWithChips = transactionWithChips,
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    toggleTransactionSelected(transactionWithChips.id)
                                },
                                onLongClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        )
    } else {
        OneHandModeScaffold(
            loading = loading,
            showToastBar = showToastBar,
            toastBarText = currentEvent?.let {
                stringResource(id = it.message)
            } ?: "",
            onDismissToastBar = {
                showToastBar = false
                if (currentEvent == Event.MigrationSuccess) {
                    navigateBack()
                }
            },
            showEmptyPlaceholder = showEmptyPlaceholder,
            emptyPlaceholderText = if (searchText.isNotBlank()) {
                stringResource(id = R.string.choose_icon_screen_empty_placeholder_label, searchText)
            } else {
                stringResource(id = R.string.no_results_found_filters_placeholder_label)
            },
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

                    SheetType.CURRENCY -> {
                        FilterCurrencySheet(
                            selectedCurrencyCodes = selectedCurrencies,
                            currencies = currencies,
                            filter = setSelectedCurrencies,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion { sheetType = SheetType.NONE }
                            }
                        )
                    }

                    SheetType.AMOUNT -> {
                        AmountFilterSheet(
                            minAmount = filterAmountMin,
                            maxAmount = filterAmountMax,
                            filter = filterByAmount,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion { sheetType = SheetType.NONE }
                            }
                        )
                    }

                    SheetType.CATEGORY -> {
                        FilterCategorySheet(
                            selectedCategoryIds = selectedCategories,
                            categories = categories,
                            filter = setSelectedCategories,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion { sheetType = SheetType.NONE }
                            }
                        )
                    }

                    SheetType.COUNTERPARTY -> {
                        FilterCounterPartySheet(
                            selectedCounterPartyIds = selectedCounterParties,
                            includeNoCounterPartyTransactions = includeNoCounterPartyTransactions,
                            counterParties = counterParties,
                            filter = setSelectedCounterParties,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion { sheetType = SheetType.NONE }
                            }
                        )
                    }

                    SheetType.METHOD -> {
                        MigrateMethodSheet(
                            migrateCount = selectedTransactionCount,
                            selectMethod = migrate,
                            methods = methods,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion { sheetType = SheetType.NONE }
                            }
                        )
                    }

                    SheetType.ACCOUNT -> {
                        FilterAccountSheet(
                            selectedAccountIds = selectedAccounts,
                            accounts = accounts,
                            filter = setSelectedAccounts,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion { sheetType = SheetType.NONE }
                            }
                        )
                    }

                    SheetType.TYPE -> {
                        FilterTransactionTypeSheet(
                            selectedTransactionTypes = selectedTransactionTypes,
                            filter = setSelectedTransactionTypes,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion { sheetType = SheetType.NONE }
                            }
                        )
                    }

                    else -> {}
                }
            },
            onDismissSheet = {
                sheetType = SheetType.NONE
            },
            topBar = { scrollBehavior, expandedHeight ->
                LargeTopAppBar(
                    title = {
                        Column {
                            Text(text = stringResource(id = R.string.migrate_transactions_label))
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
                    },
                    expandedHeight = expandedHeight,
                    scrollBehavior = scrollBehavior,
                )
            },
            bottomBar = {
                MigrateMethodScreenBottomAppBar(
                    navigateBack = navigateBack,
                    enabled = enabled,
                    migrateEnabled = migrateEnabled,
                    allSelected = allSelected,
                    sortDirection = sortDirection,
                    filterAmount = filterAmountRange,
                    selectedCurrencyCount = selectedCurrencyCount,
                    selectedAccountCount = selectedAccountCount,
                    selectedCategoryCount = selectedCategoryCount,
                    selectedCounterPartyCount = selectedCounterPartyCount,
                    counterPartyFilterVisible = counterParties.isNotEmpty(),
                    selectedTransactionTypeCount = selectedTransactionTypes.size,
                    onSortClick = {
                        if (sortDirection == SortDirection.DESC) {
                            setSortDirection(SortDirection.ASC)
                        } else {
                            setSortDirection(SortDirection.DESC)
                        }
                    },
                    onSearchClick = {
                        if (isFocused) {
                            keyboardController?.show()
                        } else {
                            focusRequester.requestFocus()
                        }
                    },
                    onToggleSelectClick = toggleSelection,
                    setSheetType = {
                        sheetType = it
                    },
                )
            }
        ) {
            Column {
                SearchBox(
                    modifier = Modifier
                        .padding(top = 5.dp, start = 10.dp, end = 10.dp, bottom = 10.dp),
                    searchText = searchText,
                    setSearchText = setSearchText,
                    enabled = enabled,
                    focusRequester = focusRequester,
                    interactionSource = interactionSource
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 10.dp, end = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    transactions.forEach { entry ->
                        stickyHeader {
                            TransactionGroupHeader(
                                date = entry.key,
                                selected = selectedLocalDates.contains(entry.key),
                                enabled = enabled,
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    toggleLocalDateSelected(entry.key)
                                }
                            )
                        }
                        items(
                            items = entry.value,
                            key = { transactionWithChips -> transactionWithChips.id }
                        ) { transactionWithChips ->
                            TransactionCard(
                                checked = selectedTransactions.contains(transactionWithChips.id),
                                enabled = enabled,
                                transactionWithChips = transactionWithChips,
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    toggleTransactionSelected(transactionWithChips.id)
                                },
                                onLongClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                modifier = Modifier.animateItem()
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
fun MigrateMethodScreenPreview() {
    MigrateMethodScreen(
        navigateBack = {},
        migrate = {},
        toggleSelection = {},
        toggleTransactionSelected = {},
        toggleLocalDateSelected = {},
        searchState = "",
        setSearchText = {},
        setLocale = {},
        setSelectedCurrencies = {},
        setStartDateAndEndDate = { _, _ -> },
        setSelectedAccounts = {},
        setSelectedCategories = {},
        setSelectedCounterParties = { _, _ -> },
        setSelectedTransactionTypes = {},
        setSortDirection = {},
        filterByAmount = { _, _ -> },
        events = emptyList<Event>().asFlow(),
        state = MigrateMethodScreenState(),
        filtersState = TransactionFilters(),
        sortConfigState = SortConfigState(sortField = "transactionDateTime")
    )
}

fun NavGraphBuilder.migrateMethodScreen(navController: NavController) {
    composable(
        route = Routes.MigrateMethod.route,
        arguments = listOf(
            navArgument("id") {
                type = NavType.LongType
            }
        )
    ) {
        val viewModel: MigrateMethodScreenViewModel = koinViewModel()
        val state by viewModel.state.collectAsState()
        val searchState by viewModel.searchState.collectAsState()
        val filtersState by viewModel.filtersState.collectAsState()
        val sortConfigState by viewModel.sortConfigState.collectAsState()

        MigrateMethodScreen(
            navigateBack = {
                navController.popBackStack()
            },
            migrate = viewModel::migrateTransactions,
            toggleSelection = viewModel::toggleSelection,
            toggleTransactionSelected = viewModel::toggleTransactionSelected,
            toggleLocalDateSelected = viewModel::toggleLocalDateSelected,
            searchState = searchState,
            setSearchText = viewModel::setSearchText,
            setLocale = viewModel::setLocale,
            setSelectedCurrencies = viewModel::setSelectedCurrencies,
            setStartDateAndEndDate = viewModel::setStartDateAndEndDate,
            setSelectedAccounts = viewModel::setSelectedAccounts,
            setSelectedCategories = viewModel::setSelectedCategories,
            setSelectedCounterParties = viewModel::setSelectedCounterParties,
            setSelectedTransactionTypes = viewModel::setSelectedTransactionTypes,
            setSortDirection = viewModel::setSortDirection,
            filterByAmount = viewModel::setFilterAmounts,
            events = viewModel.event,
            state = state,
            filtersState = filtersState,
            sortConfigState = sortConfigState
        )
    }
}