package jp.ikigai.cash.flow.ui.screens.common

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import jp.ikigai.cash.flow.AccountWithTransactionMetadata
import jp.ikigai.cash.flow.CategoryWithTransactionMetadata
import jp.ikigai.cash.flow.CounterPartyWithTransactionMetadata
import jp.ikigai.cash.flow.MethodWithTransactionMetadata
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.data.dto.TransactionWithChips
import jp.ikigai.cash.flow.data.enums.SheetType
import jp.ikigai.cash.flow.data.enums.SortDirection
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.ui.components.bottombars.ImportBackupScreenRoundedBottomBar
import jp.ikigai.cash.flow.ui.components.bottomsheets.AmountFilterSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.ConfirmNavigationSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.DateRangePickerSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.FilterCurrencySheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.FilterTransactionTypeSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.ReviewDetailsSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.SelectAccountSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.SelectCategorySheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.SelectCounterPartySheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.SelectMethodSheet
import jp.ikigai.cash.flow.ui.components.cards.MapAccountCard
import jp.ikigai.cash.flow.ui.components.cards.MapCategoryCard
import jp.ikigai.cash.flow.ui.components.cards.MapCounterPartyCard
import jp.ikigai.cash.flow.ui.components.cards.MapMethodCard
import jp.ikigai.cash.flow.ui.components.cards.TransactionCard
import jp.ikigai.cash.flow.ui.components.cards.TransactionTemplateCard
import jp.ikigai.cash.flow.ui.components.common.OneHandModeScaffold
import jp.ikigai.cash.flow.ui.components.common.OneHandModeSpacer
import jp.ikigai.cash.flow.ui.components.common.SearchBox
import jp.ikigai.cash.flow.ui.components.common.TransactionGroupHeader
import jp.ikigai.cash.flow.ui.screenStates.common.SortConfigState
import jp.ikigai.cash.flow.ui.screenStates.common.restore.ImportBackupScreenFiltersState
import jp.ikigai.cash.flow.ui.screenStates.common.restore.ImportBackupScreenPrimaryState
import jp.ikigai.cash.flow.ui.screenStates.common.restore.ImportBackupScreenSecondaryState
import jp.ikigai.cash.flow.ui.viewmodels.common.ImportBackupScreenViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.io.InputStream
import java.time.ZonedDateTime
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportBackupScreen(
    navigateBack: () -> Unit,
    setLocale: (Locale?) -> Unit,
    loadFile: (InputStream?) -> Unit,
    import: () -> Unit,
    toggleSelection: (Boolean) -> Unit,
    toggleTransactionSelected: (Long) -> Unit,
    toggleLocalDateSelected: (Boolean, List<TransactionWithChips>) -> Unit,
    searchState: String,
    setSearchText: (String) -> Unit,
    setSelectedCurrencies: (Set<String>) -> Unit,
    setStartDateAndEndDate: (ZonedDateTime?, ZonedDateTime?) -> Unit,
    setSelectedTransactionTypes: (List<TransactionType>) -> Unit,
    setSortDirection: (SortDirection) -> Unit,
    filterByAmount: (Double, Double) -> Unit,
    toggleTransactionTemplateSelected: (Long) -> Unit,
    setCategoryMapping: (Long, CategoryWithTransactionMetadata) -> Unit,
    toggleCategorySelected: (Long) -> Unit,
    setCounterPartyMapping: (Long, CounterPartyWithTransactionMetadata) -> Unit,
    toggleCounterPartySelected: (Long) -> Unit,
    setMethodMapping: (Long, MethodWithTransactionMetadata) -> Unit,
    toggleMethodSelected: (Long) -> Unit,
    setAccountMapping: (Long, AccountWithTransactionMetadata) -> Unit,
    toggleAccountSelected: (Long) -> Unit,
    toggleRestoreBalance: (Long) -> Unit,
    checkPageValidity: (Int) -> Unit,
    events: Flow<Event?>,
    primaryState: ImportBackupScreenPrimaryState,
    secondaryState: ImportBackupScreenSecondaryState,
    filtersState: ImportBackupScreenFiltersState,
    sortConfigState: SortConfigState
) {
    val haptics = LocalHapticFeedback.current
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val keyboardController = LocalSoftwareKeyboardController.current

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

    val enabled by remember(key1 = primaryState.enabled) {
        mutableStateOf(primaryState.enabled)
    }

    val loading by remember(key1 = primaryState.loading) {
        mutableStateOf(primaryState.loading)
    }

    var sheetType by remember {
        mutableStateOf(SheetType.NONE)
    }

    val dataLoadComplete by remember(key1 = primaryState.dataLoadComplete) {
        mutableStateOf(primaryState.dataLoadComplete)
    }

    val searchText by remember(key1 = searchState) {
        mutableStateOf(searchState)
    }

    val currencies by remember(key1 = primaryState.currencies) {
        mutableStateOf(primaryState.currencies)
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

    val sortDirection by remember(key1 = sortConfigState.sortDirection) {
        mutableStateOf(sortConfigState.sortDirection)
    }

    val transactions by remember(
        key1 = primaryState.filteredTransactions,
        key2 = primaryState.transactionsHashCode
    ) {
        mutableStateOf(primaryState.filteredTransactions)
    }

    val enabledTempTransactions by remember(key1 = secondaryState.enabledTempTransactions) {
        mutableStateOf(secondaryState.enabledTempTransactions)
    }

    val selectedTransactions by remember(key1 = primaryState.selectedTempTransactions) {
        mutableStateOf(primaryState.selectedTempTransactions)
    }

    val selectedTransactionsCount by remember(key1 = secondaryState.selectedTransactionsCount) {
        mutableStateOf(secondaryState.selectedTransactionsCount)
    }

    val allSelected by remember(key1 = secondaryState.allSelected) {
        mutableStateOf(secondaryState.allSelected)
    }

    val enabledLocalDates by remember(key1 = secondaryState.enabledLocalDates) {
        mutableStateOf(secondaryState.enabledLocalDates)
    }

    val selectedLocalDates by remember(key1 = secondaryState.selectedLocalDates) {
        mutableStateOf(secondaryState.selectedLocalDates)
    }

    //region Category State Variables

    val tempCategories by remember(key1 = primaryState.tempCategories) {
        mutableStateOf(primaryState.tempCategories)
    }

    val selectedTempCategories by remember(key1 = primaryState.selectedTempCategories) {
        mutableStateOf(primaryState.selectedTempCategories)
    }

    val selectedTempCategoryCount by remember(key1 = primaryState.selectedTempCategoryCount) {
        mutableStateOf(primaryState.selectedTempCategoryCount)
    }

    var selectedTempCategoryId by remember {
        mutableLongStateOf(0L)
    }

    val conflictingTempCategories by remember(key1 = primaryState.conflictingTempCategories) {
        mutableStateOf(primaryState.conflictingTempCategories)
    }

    val dbCategories by remember(key1 = primaryState.dbCategories) {
        mutableStateOf(primaryState.dbCategories)
    }

    val categoryMappings by remember(key1 = primaryState.categoryMappings) {
        mutableStateOf(primaryState.categoryMappings)
    }

    //endregion

    //region CounterParty State Variables

    val tempCounterParties by remember(key1 = primaryState.tempCounterParties) {
        mutableStateOf(primaryState.tempCounterParties)
    }

    val selectedTempCounterParties by remember(key1 = primaryState.selectedTempCounterParties) {
        mutableStateOf(primaryState.selectedTempCounterParties)
    }

    val selectedTempCounterPartyCount by remember(key1 = primaryState.selectedTempCounterPartyCount) {
        mutableStateOf(primaryState.selectedTempCounterPartyCount)
    }

    var selectedTempCounterPartyId by remember {
        mutableLongStateOf(0L)
    }

    val conflictingTempCounterParties by remember(key1 = primaryState.conflictingTempCounterParties) {
        mutableStateOf(primaryState.conflictingTempCounterParties)
    }

    val dbCounterParties by remember(key1 = primaryState.dbCounterParties) {
        mutableStateOf(primaryState.dbCounterParties)
    }

    val counterPartyMappings by remember(key1 = primaryState.counterPartyMappings) {
        mutableStateOf(primaryState.counterPartyMappings)
    }

    //endregion

    //region Method State Variables

    val tempMethods by remember(key1 = primaryState.tempMethods) {
        mutableStateOf(primaryState.tempMethods)
    }

    val selectedTempMethods by remember(key1 = primaryState.selectedTempMethods) {
        mutableStateOf(primaryState.selectedTempMethods)
    }

    val selectedTempMethodCount by remember(key1 = primaryState.selectedTempMethodCount) {
        mutableStateOf(primaryState.selectedTempMethodCount)
    }

    var selectedTempMethodId by remember {
        mutableLongStateOf(0L)
    }

    val conflictingTempMethods by remember(key1 = primaryState.conflictingTempMethods) {
        mutableStateOf(primaryState.conflictingTempMethods)
    }

    val dbMethods by remember(key1 = primaryState.dbMethods) {
        mutableStateOf(primaryState.dbMethods)
    }

    val methodMappings by remember(key1 = primaryState.methodMappings) {
        mutableStateOf(primaryState.methodMappings)
    }

    //endregion

    //region Account State Variables

    val tempAccounts by remember(key1 = primaryState.tempAccounts) {
        mutableStateOf(primaryState.tempAccounts)
    }

    val selectedTempAccounts by remember(key1 = primaryState.selectedTempAccounts) {
        mutableStateOf(primaryState.selectedTempAccounts)
    }

    val selectedTempAccountCount by remember(key1 = primaryState.selectedTempAccountCount) {
        mutableStateOf(primaryState.selectedTempAccountCount)
    }

    var selectedTempAccountId by remember {
        mutableLongStateOf(0L)
    }

    var selectedTempAccountCurrency by remember {
        mutableStateOf("")
    }

    val restoreBalanceAccounts by remember(key1 = primaryState.restoreBalanceAccounts) {
        mutableStateOf(primaryState.restoreBalanceAccounts)
    }

    val conflictingTempAccounts by remember(key1 = primaryState.conflictingTempAccounts) {
        mutableStateOf(primaryState.conflictingTempAccounts)
    }

    val currencyAccountMap by remember(key1 = primaryState.currencyAccountMap) {
        mutableStateOf(primaryState.currencyAccountMap)
    }

    val accountMappings by remember(key1 = primaryState.accountMappings) {
        mutableStateOf(primaryState.accountMappings)
    }

    //endregion

    //region Template State Variables

    val tempTransactionTemplatesWithIcons by remember(key1 = primaryState.tempTransactionTemplatesWithIcons) {
        mutableStateOf(primaryState.tempTransactionTemplatesWithIcons)
    }

    val selectedTempTransactionTemplates by remember(key1 = primaryState.selectedTempTransactionTemplates) {
        mutableStateOf(primaryState.selectedTempTransactionTemplates)
    }

    val selectedTempTransactionTemplateCount by remember(key1 = secondaryState.selectedTempTransactionTemplateCount) {
        mutableStateOf(secondaryState.selectedTempTransactionTemplateCount)
    }

    val enabledTempTransactionTemplates by remember(key1 = secondaryState.enabledTempTransactionTemplates) {
        mutableStateOf(secondaryState.enabledTempTransactionTemplates)
    }

    //endregion

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

    val headerPagerState = rememberPagerState(
        pageCount = { 6 }
    )

    val pagerState = rememberPagerState(
        pageCount = { 6 }
    )

    LaunchedEffect(key1 = pagerState) {
        snapshotFlow { pagerState.currentPage }.collectLatest { page ->
            headerPagerState.animateScrollToPage(page)
        }
    }

    var showToastBar by remember { mutableStateOf(false) }

    var currentEvent: Event? by remember {
        mutableStateOf(null)
    }

    LaunchedEffect(Unit) {
        events.collectLatest { event ->
            if (event != null) {
                showToastBar = false
                currentEvent = event
                showToastBar = true
            } else {
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            }
        }
    }

    LaunchedEffect(showToastBar) {
        if (showToastBar) {
            delay(2000)
            showToastBar = false
            if (currentEvent == Event.ImportSuccess) {
                navigateBack()
            }
        }
    }

    BackHandler(enabled = enabled) {
        if (pagerState.currentPage > 0) {
            scope.launch {
                pagerState.animateScrollToPage(pagerState.currentPage - 1)
            }
        } else if (dataLoadComplete) {
            sheetType = SheetType.CONFIRM_NAVIGATION
        } else {
            navigateBack()
        }
    }

    val importFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                context.contentResolver.openInputStream(uri).use { inputStream ->
                    loadFile(inputStream)
                }
            }
        }
    )

    OneHandModeScaffold(
        loading = loading,
        showToastBar = showToastBar,
        toastBarText = currentEvent?.let {
            stringResource(id = it.message)
        } ?: "",
        onDismissToastBar = {
            showToastBar = false
            if (currentEvent == Event.ImportSuccess) {
                navigateBack()
            }
        },
        showEmptyPlaceholder = false,
        emptyPlaceholderText = "",
        sheetState = sheetState,
        showBottomSheet = sheetType != SheetType.NONE,
        bottomSheetContent = {
            when (sheetType) {
                SheetType.REVIEW -> {
                    ReviewDetailsSheet(
                        selectedTempCategoryCount = selectedTempCategoryCount,
                        selectedTempCounterPartyCount = selectedTempCounterPartyCount,
                        selectedTempMethodCount = selectedTempMethodCount,
                        selectedTempAccountCount = selectedTempAccountCount,
                        selectedTempTransactionTemplateCount = selectedTempTransactionTemplateCount,
                        selectedTransactionsCount = selectedTransactionsCount,
                        complete = import,
                        dismiss = {
                            scope
                                .launch { sheetState.hide() }
                                .invokeOnCompletion { sheetType = SheetType.NONE }
                        }
                    )
                }

                SheetType.CONFIRM_NAVIGATION -> {
                    ConfirmNavigationSheet(
                        message = stringResource(id = R.string.navigation_confirmation_label),
                        navigate = navigateBack,
                        dismiss = {
                            scope
                                .launch { sheetState.hide() }
                                .invokeOnCompletion { sheetType = SheetType.NONE }
                        }
                    )
                }

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
                    SelectCategorySheet(
                        index = dbCategories
                            .indexOfFirst { it.categoryId == categoryMappings[selectedTempCategoryId]?.categoryId }
                            .coerceAtLeast(0),
                        selectedCategoryId = categoryMappings[selectedTempCategoryId]?.categoryId
                            ?: 0L,
                        categories = dbCategories,
                        setSelectedCategory = {
                            setCategoryMapping(selectedTempCategoryId, it)
                        },
                        dismiss = {
                            scope
                                .launch { sheetState.hide() }
                                .invokeOnCompletion { sheetType = SheetType.NONE }
                        }
                    )
                }

                SheetType.COUNTERPARTY -> {
                    SelectCounterPartySheet(
                        index = dbCounterParties
                            .indexOfFirst { it.counterPartyId == counterPartyMappings[selectedTempCounterPartyId]?.counterPartyId }
                            .coerceAtLeast(0),
                        selectedCounterPartyId = counterPartyMappings[selectedTempCounterPartyId]?.counterPartyId
                            ?: 0L,
                        counterParties = dbCounterParties,
                        setSelectedCounterParty = {
                            setCounterPartyMapping(selectedTempCounterPartyId, it)
                        },
                        dismiss = {
                            scope
                                .launch { sheetState.hide() }
                                .invokeOnCompletion { sheetType = SheetType.NONE }
                        }
                    )
                }

                SheetType.METHOD -> {
                    SelectMethodSheet(
                        index = dbMethods
                            .indexOfFirst { it.methodId == methodMappings[selectedTempMethodId]?.methodId }
                            .coerceAtLeast(0),
                        selectedMethodId = methodMappings[selectedTempMethodId]?.methodId ?: 0L,
                        methods = dbMethods,
                        setSelectedMethod = {
                            setMethodMapping(selectedTempMethodId, it)
                        },
                        dismiss = {
                            scope
                                .launch { sheetState.hide() }
                                .invokeOnCompletion { sheetType = SheetType.NONE }
                        }
                    )
                }

                SheetType.ACCOUNT -> {
                    SelectAccountSheet(
                        index = currencyAccountMap[selectedTempAccountCurrency]
                            ?.indexOfFirst { it.accountId == accountMappings[selectedTempAccountId]?.accountId }
                            ?.coerceAtLeast(0) ?: 0,
                        selectedAccountId = accountMappings[selectedTempAccountId]?.accountId ?: 0L,
                        accounts = currencyAccountMap[selectedTempAccountCurrency]
                            ?: emptyList(),
                        setSelectedAccount = {
                            setAccountMapping(selectedTempAccountId, it)
                        },
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
        topBar = {
            TopAppBar(
                title = {
                    if (!dataLoadComplete) {
                        Text(text = stringResource(R.string.select_backup_label))
                    } else {
                        HorizontalPager(
                            state = headerPagerState,
                            modifier = Modifier.fillMaxSize(),
                            userScrollEnabled = false,
                        ) { pageNumber ->
                            when (pageNumber) {
                                0 -> {
                                    Text(text = stringResource(id = R.string.map_categories_label))
                                }

                                1 -> {
                                    Text(text = stringResource(id = R.string.map_counter_parties_label))
                                }

                                2 -> {
                                    Text(text = stringResource(id = R.string.map_methods_label))
                                }

                                3 -> {
                                    Text(text = stringResource(id = R.string.map_accounts_label))
                                }

                                4 -> {
                                    Text(text = stringResource(id = R.string.select_templates_label))
                                }

                                5 -> {
                                    Column {
                                        Text(text = stringResource(id = R.string.select_transactions_label))
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
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            ImportBackupScreenRoundedBottomBar(
                enabled = enabled,
                navigateBack = {
                    if (enabled) {
                        if (pagerState.currentPage > 0) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        } else if (dataLoadComplete) {
                            sheetType = SheetType.CONFIRM_NAVIGATION
                        }
                    } else {
                        navigateBack()
                    }
                },
                navigateToNext = {
                    if (pagerState.currentPage < 5) {
                        checkPageValidity(pagerState.currentPage)
                    }
                },
                isSelectTransactionsScreen = pagerState.currentPage == 5,
                allSelected = allSelected,
                importEnabled = selectedTransactionsCount.isNotEmpty(),
                dataLoaded = dataLoadComplete,
                sortDirection = sortDirection,
                filterAmount = filterAmountRange,
                selectedCurrencyCount = selectedCurrencyCount,
                selectedTransactionTypeCount = selectedTransactionTypes.size,
                onSortClick = {
                    if (sortDirection == SortDirection.DESC) {
                        setSortDirection(SortDirection.ASC)
                    } else {
                        setSortDirection(SortDirection.DESC)
                    }
                },
                onFilterByAmountClick = {
                    sheetType = SheetType.AMOUNT
                },
                onFilterByTypeClick = {
                    sheetType = SheetType.TYPE
                },
                onFilterByCurrencyClick = {
                    sheetType = SheetType.CURRENCY
                },
                onSearchClick = {
                    if (isFocused) {
                        keyboardController?.show()
                    } else {
                        focusRequester.requestFocus()
                    }
                },
                onToggleSelectClick = {
                    toggleSelection(allSelected)
                },
                onCalendarClick = {
                    sheetType = SheetType.DATE_RANGE
                },
                actionButtonClick = {
                    if (dataLoadComplete && selectedTransactionsCount.isNotEmpty()) {
                        sheetType = SheetType.REVIEW
                    } else {
                        importFilePicker.launch("application/json")
                    }
                }
            )
        }
    ) { oneHandModeBoxHeight, resetOneHandMode ->
        if (!dataLoadComplete) {
            Text(
                text = stringResource(R.string.select_backup_continue_label),
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = false
            ) { pageNumber ->
                when (pageNumber) {
                    0 -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 10.dp, end = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            item(
                                key = "one-hand-mode-expand-row",
                                contentType = "row"
                            ) {
                                OneHandModeSpacer(oneHandModeBoxHeight = oneHandModeBoxHeight)
                            }
                            items(
                                items = tempCategories,
                                key = { tempCategory -> tempCategory.tempCategoryId }
                            ) { tempCategory ->
                                MapCategoryCard(
                                    tempCategory = tempCategory,
                                    mappedCategory = categoryMappings[
                                        tempCategory.tempCategoryId
                                    ] ?: CategoryWithTransactionMetadata(
                                        categoryId = 0L,
                                        categoryName = "",
                                        icon = Constants.DEFAULT_CATEGORY_ICON,
                                        transactionCount = 0L,
                                        lastUsed = null
                                    ),
                                    modifier = Modifier.animateItem(),
                                    selected = selectedTempCategories.contains(tempCategory.tempCategoryId),
                                    conflicting = conflictingTempCategories.contains(tempCategory.tempCategoryId),
                                    selectCategory = {
                                        selectedTempCategoryId = tempCategory.tempCategoryId
                                        sheetType = SheetType.CATEGORY
                                    },
                                    toggleSelected = {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        toggleCategorySelected(tempCategory.tempCategoryId)
                                    },
                                    clearSelectedCategory = {
                                        setCategoryMapping(
                                            tempCategory.tempCategoryId,
                                            CategoryWithTransactionMetadata(
                                                categoryId = 0L,
                                                categoryName = "",
                                                icon = Constants.DEFAULT_CATEGORY_ICON,
                                                transactionCount = 0L,
                                                lastUsed = null
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }

                    1 -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 10.dp, end = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            item(
                                key = "one-hand-mode-expand-row",
                                contentType = "row"
                            ) {
                                OneHandModeSpacer(oneHandModeBoxHeight = oneHandModeBoxHeight)
                            }
                            items(
                                items = tempCounterParties,
                                key = { tempCounterParty -> tempCounterParty.tempCounterPartyId }
                            ) { tempCounterParty ->
                                MapCounterPartyCard(
                                    tempCounterParty = tempCounterParty,
                                    mappedCounterParty = counterPartyMappings[
                                        tempCounterParty.tempCounterPartyId
                                    ] ?: CounterPartyWithTransactionMetadata(
                                        counterPartyId = 0L,
                                        counterPartyName = "",
                                        transactionCount = 0L,
                                        lastUsed = null
                                    ),
                                    modifier = Modifier.animateItem(),
                                    selected = selectedTempCounterParties.contains(
                                        tempCounterParty.tempCounterPartyId
                                    ),
                                    conflicting = conflictingTempCounterParties.contains(
                                        tempCounterParty.tempCounterPartyId
                                    ),
                                    selectCounterParty = {
                                        selectedTempCounterPartyId =
                                            tempCounterParty.tempCounterPartyId
                                        sheetType = SheetType.COUNTERPARTY
                                    },
                                    toggleSelected = {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        toggleCounterPartySelected(tempCounterParty.tempCounterPartyId)
                                    },
                                    clearSelectedCounterParty = {
                                        setCounterPartyMapping(
                                            tempCounterParty.tempCounterPartyId,
                                            CounterPartyWithTransactionMetadata(
                                                counterPartyId = 0L,
                                                counterPartyName = "",
                                                transactionCount = 0L,
                                                lastUsed = null
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }

                    2 -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 10.dp, end = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            item(
                                key = "one-hand-mode-expand-row",
                                contentType = "row"
                            ) {
                                OneHandModeSpacer(oneHandModeBoxHeight = oneHandModeBoxHeight)
                            }
                            items(
                                items = tempMethods,
                                key = { tempMethod -> tempMethod.tempMethodId }
                            ) { tempMethod ->
                                MapMethodCard(
                                    tempMethod = tempMethod,
                                    mappedMethod = methodMappings[
                                        tempMethod.tempMethodId
                                    ] ?: MethodWithTransactionMetadata(
                                        methodId = 0L,
                                        methodName = "",
                                        transactionCount = 0L,
                                        lastUsed = null
                                    ),
                                    modifier = Modifier.animateItem(),
                                    selected = selectedTempMethods.contains(
                                        tempMethod.tempMethodId
                                    ),
                                    conflicting = conflictingTempMethods.contains(
                                        tempMethod.tempMethodId
                                    ),
                                    selectMethod = {
                                        selectedTempMethodId = tempMethod.tempMethodId
                                        sheetType = SheetType.METHOD
                                    },
                                    toggleSelected = {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        toggleMethodSelected(tempMethod.tempMethodId)
                                    },
                                    clearSelectedMethod = {
                                        setMethodMapping(
                                            tempMethod.tempMethodId,
                                            MethodWithTransactionMetadata(
                                                methodId = 0L,
                                                methodName = "",
                                                transactionCount = 0L,
                                                lastUsed = null
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }

                    3 -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 10.dp, end = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            item(
                                key = "one-hand-mode-expand-row",
                                contentType = "row"
                            ) {
                                OneHandModeSpacer(oneHandModeBoxHeight = oneHandModeBoxHeight)
                            }
                            items(
                                items = tempAccounts,
                                key = { tempAccount -> tempAccount.tempAccountId }
                            ) { tempAccount ->
                                MapAccountCard(
                                    tempAccount = tempAccount,
                                    mappedAccount = accountMappings[
                                        tempAccount.tempAccountId
                                    ] ?: AccountWithTransactionMetadata(
                                        accountId = 0L,
                                        accountName = "",
                                        currency = "INR",
                                        balance = 0.0,
                                        formattedBalance = "",
                                        transactionCount = 0L,
                                        lastUsed = null
                                    ),
                                    modifier = Modifier.animateItem(),
                                    selected = selectedTempAccounts.contains(
                                        tempAccount.tempAccountId
                                    ),
                                    restoreBalance = restoreBalanceAccounts.contains(
                                        tempAccount.tempAccountId
                                    ),
                                    conflicting = conflictingTempAccounts.contains(
                                        tempAccount.tempAccountId
                                    ),
                                    selectSource = {
                                        selectedTempAccountId = tempAccount.tempAccountId
                                        selectedTempAccountCurrency =
                                            tempAccount.tempAccountCurrency
                                        sheetType = SheetType.ACCOUNT
                                    },
                                    toggleSelected = {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        toggleAccountSelected(tempAccount.tempAccountId)
                                    },
                                    toggleRestoreBalance = {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        toggleRestoreBalance(tempAccount.tempAccountId)
                                    },
                                    clearSelectedSource = {
                                        setAccountMapping(
                                            tempAccount.tempAccountId,
                                            AccountWithTransactionMetadata(
                                                accountId = 0L,
                                                accountName = "",
                                                currency = "INR",
                                                balance = 0.0,
                                                formattedBalance = "",
                                                transactionCount = 0L,
                                                lastUsed = null
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }

                    4 -> {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            AnimatedVisibility(
                                visible = tempTransactionTemplatesWithIcons.isEmpty(),
                                modifier = Modifier.align(alignment = Alignment.Center)
                            ) {
                                Text(text = stringResource(id = R.string.no_templates_to_import_placeholder_label))
                            }
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 10.dp, end = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                item(
                                    key = "one-hand-mode-expand-row",
                                    contentType = "row"
                                ) {
                                    OneHandModeSpacer(oneHandModeBoxHeight = oneHandModeBoxHeight)
                                }
                                items(
                                    items = tempTransactionTemplatesWithIcons,
                                    key = { templateWithChips -> templateWithChips.id }
                                ) { templateWithChips ->
                                    TransactionTemplateCard(
                                        checked = enabledTempTransactionTemplates.contains(
                                            templateWithChips.id
                                        ) && selectedTempTransactionTemplates.contains(
                                            templateWithChips.id
                                        ),
                                        enabled = enabledTempTransactionTemplates.contains(
                                            templateWithChips.id
                                        ),
                                        modifier = Modifier.animateItem(),
                                        templateWithChips = templateWithChips,
                                        onClick = {
                                            toggleTransactionTemplateSelected(
                                                templateWithChips.id
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    5 -> {
                        Column {
                            SearchBox(
                                modifier = Modifier
                                    .padding(
                                        top = 5.dp,
                                        start = 10.dp,
                                        end = 10.dp,
                                        bottom = 10.dp
                                    ),
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
                                item(
                                    key = "one-hand-mode-expand-row",
                                    contentType = "row"
                                ) {
                                    OneHandModeSpacer(oneHandModeBoxHeight = oneHandModeBoxHeight)
                                }
                                transactions.forEach { entry ->
                                    stickyHeader {
                                        TransactionGroupHeader(
                                            date = entry.key,
                                            selected = selectedLocalDates.contains(entry.key),
                                            enabled = enabled && enabledLocalDates.contains(
                                                entry.key
                                            ),
                                            onClick = {
                                                resetOneHandMode()
                                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                toggleLocalDateSelected(
                                                    selectedLocalDates.contains(entry.key),
                                                    transactions[entry.key] ?: emptyList()
                                                )
                                            }
                                        )
                                    }
                                    items(
                                        items = entry.value,
                                        key = { transactionWithChips -> transactionWithChips.id }
                                    ) { transactionWithChips ->
                                        TransactionCard(
                                            checked = enabledTempTransactions.contains(
                                                transactionWithChips.id
                                            ) && selectedTransactions.contains(transactionWithChips.id),
                                            enabled = enabled && enabledTempTransactions.contains(
                                                transactionWithChips.id
                                            ),
                                            transactionWithChips = transactionWithChips,
                                            onClick = {
                                                resetOneHandMode()
                                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                toggleTransactionSelected(transactionWithChips.id)
                                            },
                                            onLongClick = {
                                                resetOneHandMode()
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
        }
    }
}

@Preview
@Composable
fun ImportBackupScreenPreview() {
    ImportBackupScreen(
        navigateBack = {},
        setLocale = {},
        loadFile = {},
        import = {},
        toggleSelection = {},
        toggleTransactionSelected = {},
        toggleLocalDateSelected = { _, _ -> },
        searchState = "",
        setSearchText = {},
        setSelectedCurrencies = {},
        setStartDateAndEndDate = { _, _ -> },
        setSelectedTransactionTypes = {},
        setSortDirection = {},
        filterByAmount = { _, _ -> },
        toggleTransactionTemplateSelected = {},
        setCategoryMapping = { _, _ -> },
        toggleCategorySelected = {},
        setCounterPartyMapping = { _, _ -> },
        toggleCounterPartySelected = {},
        setMethodMapping = { _, _ -> },
        toggleMethodSelected = {},
        setAccountMapping = { _, _ -> },
        toggleAccountSelected = {},
        toggleRestoreBalance = {},
        checkPageValidity = {},
        events = emptyList<Event>().asFlow(),
        primaryState = ImportBackupScreenPrimaryState(),
        secondaryState = ImportBackupScreenSecondaryState(),
        filtersState = ImportBackupScreenFiltersState(),
        sortConfigState = SortConfigState(sortField = "tempTransactionDateTime")
    )
}

fun NavGraphBuilder.importBackupScreen(navController: NavController) {
    composable(
        route = Routes.ImportBackup.route
    ) {
        val viewModel: ImportBackupScreenViewModel = koinViewModel()
        val mainState by viewModel.primaryState.collectAsState()
        val secondaryState by viewModel.secondaryState.collectAsState()
        val searchState by viewModel.searchState.collectAsState()
        val filtersState by viewModel.filtersState.collectAsState()
        val sortConfigState by viewModel.sortConfigState.collectAsState()

        ImportBackupScreen(
            navigateBack = {
                navController.popBackStack()
            },
            setLocale = viewModel::setLocale,
            loadFile = viewModel::loadFile,
            import = viewModel::importData,
            toggleSelection = viewModel::toggleSelection,
            toggleTransactionSelected = viewModel::toggleTransactionSelected,
            toggleLocalDateSelected = viewModel::toggleLocalDateSelected,
            searchState = searchState,
            setSearchText = viewModel::setSearchText,
            setSelectedCurrencies = viewModel::setSelectedCurrencies,
            setStartDateAndEndDate = viewModel::setStartDateAndEndDate,
            setSelectedTransactionTypes = viewModel::setSelectedTransactionTypes,
            setSortDirection = viewModel::setSortDirection,
            filterByAmount = viewModel::setFilterAmounts,
            toggleTransactionTemplateSelected = viewModel::toggleTransactionTemplateSelected,
            setCategoryMapping = viewModel::setCategoryMapping,
            toggleCategorySelected = viewModel::toggleCategorySelected,
            setCounterPartyMapping = viewModel::setSelectedCounterParty,
            toggleCounterPartySelected = viewModel::toggleCounterPartySelected,
            setMethodMapping = viewModel::setSelectedMethod,
            toggleMethodSelected = viewModel::toggleMethodSelected,
            setAccountMapping = viewModel::setSelectedAccount,
            toggleAccountSelected = viewModel::toggleAccountSelected,
            toggleRestoreBalance = viewModel::toggleRestoreBalance,
            checkPageValidity = viewModel::checkPageValidity,
            events = viewModel.event,
            primaryState = mainState,
            secondaryState = secondaryState,
            filtersState = filtersState,
            sortConfigState = sortConfigState
        )
    }
}