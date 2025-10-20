package jp.ikigai.cash.flow.ui.screens.common

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import jp.ikigai.cash.flow.AccountWithTransactionMetadata
import jp.ikigai.cash.flow.CategoryWithTransactionMetadata
import jp.ikigai.cash.flow.CounterPartyWithTransactionMetadata
import jp.ikigai.cash.flow.MethodWithTransactionMetadata
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.data.dto.TransactionWithChips
import jp.ikigai.cash.flow.data.enums.SheetType
import jp.ikigai.cash.flow.data.enums.SortDirection
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.ui.components.bottombars.ImportBackupScreenBottomAppBar
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
import jp.ikigai.cash.flow.ui.components.common.LandscapeScaffold
import jp.ikigai.cash.flow.ui.components.common.OneHandModeScaffold
import jp.ikigai.cash.flow.ui.components.common.SearchBox
import jp.ikigai.cash.flow.ui.screenStates.common.SortConfigState
import jp.ikigai.cash.flow.ui.screenStates.common.restore.ImportBackupScreenFiltersState
import jp.ikigai.cash.flow.ui.screenStates.common.restore.ImportBackupScreenPrimaryState
import jp.ikigai.cash.flow.ui.screenStates.common.restore.ImportBackupScreenSecondaryState
import jp.ikigai.cash.flow.ui.screens.common.restore.ImportScreenAnimatedTitleContent
import jp.ikigai.cash.flow.ui.screens.common.restore.ImportScreenDefaultContent
import jp.ikigai.cash.flow.ui.screens.common.restore.ImportScreenMapAccountsContent
import jp.ikigai.cash.flow.ui.screens.common.restore.ImportScreenMapCategoriesContent
import jp.ikigai.cash.flow.ui.screens.common.restore.ImportScreenMapCounterPartiesContent
import jp.ikigai.cash.flow.ui.screens.common.restore.ImportScreenMapMethodsContent
import jp.ikigai.cash.flow.ui.screens.common.restore.ImportScreenSelectTemplatesContent
import jp.ikigai.cash.flow.ui.screens.common.restore.ImportScreenSelectTransactionsContent
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
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
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

    val hasDbAccounts by remember(key1 = primaryState.currencyAccountMap) {
        mutableStateOf(
            primaryState.currencyAccountMap.values.any { it.isNotEmpty() }
        )
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

    val pagerState = rememberPagerState(
        pageCount = { 6 }
    )

    val headers by remember {
        mutableStateOf(
            listOf(
                R.string.map_categories_label,
                R.string.map_counter_parties_label,
                R.string.map_methods_label,
                R.string.map_accounts_label,
                R.string.select_templates_label,
                R.string.select_transactions_label,
            )
        )
    }

    val subHeaders by remember(
        selectedTempCategoryCount,
        selectedTempCounterPartyCount,
        selectedTempMethodCount,
        selectedTempAccountCount,
        selectedTempTransactionTemplateCount,
    ) {
        mutableStateOf(
            listOf(
                selectedTempCategoryCount,
                selectedTempCounterPartyCount,
                selectedTempMethodCount,
                selectedTempAccountCount,
                selectedTempTransactionTemplateCount,
            )
        )
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

    BackHandler(
        enabled = enabled && dataLoadComplete
    ) {
        if (pagerState.currentPage > 0) {
            scope.launch {
                pagerState.animateScrollToPage(pagerState.currentPage - 1)
            }
        } else {
            sheetType = SheetType.CONFIRM_NAVIGATION
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

    if (windowSizeClass.windowHeightSizeClass == WindowHeightSizeClass.COMPACT && windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.MEDIUM) {
        LandscapeScaffold(
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
                            selectedAccountId = accountMappings[selectedTempAccountId]?.accountId
                                ?: 0L,
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
            firstColContent = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 10.dp, end = 10.dp, bottom = 10.dp, top = 2.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AnimatedVisibility(
                        visible = pagerState.currentPage == 5,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        SearchBox(
                            modifier = Modifier.padding(bottom = 10.dp),
                            searchText = searchText,
                            setSearchText = setSearchText,
                            enabled = enabled,
                            focusRequester = focusRequester,
                            interactionSource = interactionSource
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    ImportBackupScreenBottomAppBar(
                        currentPage = pagerState.currentPage,
                        headers = headers,
                        subHeaders = subHeaders,
                        dateRangeStringRes = dateRangeStringRes,
                        startDateString = startDateString,
                        endDateString = endDateString,
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
            },
            secondColContent = {
                if (!dataLoadComplete) {
                    ImportScreenDefaultContent()
                } else {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = false,
                    ) { pageNumber ->
                        when (pageNumber) {
                            0 -> {
                                ImportScreenMapCategoriesContent(
                                    dbCategories = dbCategories,
                                    tempCategories = tempCategories,
                                    categoryMappings = categoryMappings,
                                    selectedTempCategories = selectedTempCategories,
                                    conflictingTempCategories = conflictingTempCategories,
                                    selectCategory = { tempCategoryId ->
                                        selectedTempCategoryId = tempCategoryId
                                        sheetType = SheetType.CATEGORY
                                    },
                                    toggleSelected = toggleCategorySelected,
                                    setCategoryMapping = setCategoryMapping,
                                )
                            }

                            1 -> {
                                ImportScreenMapCounterPartiesContent(
                                    dbCounterParties = dbCounterParties,
                                    tempCounterParties = tempCounterParties,
                                    counterPartyMappings = counterPartyMappings,
                                    selectedTempCounterParties = selectedTempCounterParties,
                                    conflictingTempCounterParties = conflictingTempCounterParties,
                                    selectCounterParty = { tempCounterPartyId ->
                                        selectedTempCounterPartyId = tempCounterPartyId
                                        sheetType = SheetType.COUNTERPARTY
                                    },
                                    toggleSelected = toggleCounterPartySelected,
                                    setCounterPartyMapping = setCounterPartyMapping,
                                )
                            }

                            2 -> {
                                ImportScreenMapMethodsContent(
                                    dbMethods = dbMethods,
                                    tempMethods = tempMethods,
                                    methodMappings = methodMappings,
                                    selectedTempMethods = selectedTempMethods,
                                    conflictingTempMethods = conflictingTempMethods,
                                    selectMethod = { tempMethodId ->
                                        selectedTempMethodId = tempMethodId
                                        sheetType = SheetType.METHOD
                                    },
                                    toggleSelected = toggleMethodSelected,
                                    setMethodMapping = setMethodMapping,
                                )
                            }

                            3 -> {
                                ImportScreenMapAccountsContent(
                                    hasDbAccounts = hasDbAccounts,
                                    tempAccounts = tempAccounts,
                                    accountMappings = accountMappings,
                                    selectedTempAccounts = selectedTempAccounts,
                                    restoreBalanceAccounts = restoreBalanceAccounts,
                                    conflictingTempAccounts = conflictingTempAccounts,
                                    selectAccount = { tempAccountId, tempAccountCurrency ->
                                        selectedTempAccountId = tempAccountId
                                        selectedTempAccountCurrency = tempAccountCurrency
                                        sheetType = SheetType.ACCOUNT
                                    },
                                    toggleAccountSelected = toggleAccountSelected,
                                    toggleRestoreBalance = toggleRestoreBalance,
                                    setAccountMapping = setAccountMapping,
                                )
                            }

                            4 -> {
                                ImportScreenSelectTemplatesContent(
                                    tempTransactionTemplatesWithIcons = tempTransactionTemplatesWithIcons,
                                    enabledTempTransactionTemplates = enabledTempTransactionTemplates,
                                    selectedTempTransactionTemplates = selectedTempTransactionTemplates,
                                    toggleTransactionTemplateSelected = toggleTransactionTemplateSelected,
                                )
                            }

                            5 -> {
                                ImportScreenSelectTransactionsContent(
                                    loading = loading,
                                    searchText = searchText,
                                    enabled = enabled,
                                    transactions = transactions,
                                    selectedLocalDates = selectedLocalDates,
                                    enabledLocalDates = enabledLocalDates,
                                    enabledTempTransactions = enabledTempTransactions,
                                    selectedTransactions = selectedTransactions,
                                    toggleLocalDateSelected = toggleLocalDateSelected,
                                    toggleTransactionSelected = toggleTransactionSelected,
                                )
                            }
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
                            selectedAccountId = accountMappings[selectedTempAccountId]?.accountId
                                ?: 0L,
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
            topBar = { scrollBehavior, expandedHeight ->
                LargeTopAppBar(
                    title = {
                        if (!dataLoadComplete) {
                            Column {
                                Text(
                                    text = stringResource(R.string.import_transactions_label),
                                )
                                Text(
                                    text = stringResource(R.string.select_backup_label),
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.alpha(0.8f),
                                )
                            }
                        } else {
                            ImportScreenAnimatedTitleContent(
                                currentPage = pagerState.currentPage,
                                headers = headers,
                                subHeaders = subHeaders,
                                dateRangeStringRes = dateRangeStringRes,
                                startDateString = startDateString,
                                endDateString = endDateString,
                            )
                        }
                    },
                    expandedHeight = expandedHeight,
                    scrollBehavior = scrollBehavior,
                )
            },
            bottomBar = {
                ImportBackupScreenBottomAppBar(
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
        ) {
            if (!dataLoadComplete) {
                ImportScreenDefaultContent()
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = false,
                    contentPadding = PaddingValues(vertical = 10.dp),
                ) { pageNumber ->
                    when (pageNumber) {
                        0 -> {
                            ImportScreenMapCategoriesContent(
                                dbCategories = dbCategories,
                                tempCategories = tempCategories,
                                categoryMappings = categoryMappings,
                                selectedTempCategories = selectedTempCategories,
                                conflictingTempCategories = conflictingTempCategories,
                                selectCategory = { tempCategoryId ->
                                    selectedTempCategoryId = tempCategoryId
                                    sheetType = SheetType.CATEGORY
                                },
                                toggleSelected = toggleCategorySelected,
                                setCategoryMapping = setCategoryMapping,
                                contentPadding = PaddingValues(start = 10.dp, end = 10.dp),
                            )
                        }

                        1 -> {
                            ImportScreenMapCounterPartiesContent(
                                dbCounterParties = dbCounterParties,
                                tempCounterParties = tempCounterParties,
                                counterPartyMappings = counterPartyMappings,
                                selectedTempCounterParties = selectedTempCounterParties,
                                conflictingTempCounterParties = conflictingTempCounterParties,
                                selectCounterParty = { tempCounterPartyId ->
                                    selectedTempCounterPartyId = tempCounterPartyId
                                    sheetType = SheetType.COUNTERPARTY
                                },
                                toggleSelected = toggleCounterPartySelected,
                                setCounterPartyMapping = setCounterPartyMapping,
                                contentPadding = PaddingValues(start = 10.dp, end = 10.dp),
                            )
                        }

                        2 -> {
                            ImportScreenMapMethodsContent(
                                dbMethods = dbMethods,
                                tempMethods = tempMethods,
                                methodMappings = methodMappings,
                                selectedTempMethods = selectedTempMethods,
                                conflictingTempMethods = conflictingTempMethods,
                                selectMethod = { tempMethodId ->
                                    selectedTempMethodId = tempMethodId
                                    sheetType = SheetType.METHOD
                                },
                                toggleSelected = toggleMethodSelected,
                                setMethodMapping = setMethodMapping,
                                contentPadding = PaddingValues(start = 10.dp, end = 10.dp),
                            )
                        }

                        3 -> {
                            ImportScreenMapAccountsContent(
                                hasDbAccounts = hasDbAccounts,
                                tempAccounts = tempAccounts,
                                accountMappings = accountMappings,
                                selectedTempAccounts = selectedTempAccounts,
                                restoreBalanceAccounts = restoreBalanceAccounts,
                                conflictingTempAccounts = conflictingTempAccounts,
                                selectAccount = { tempAccountId, tempAccountCurrency ->
                                    selectedTempAccountId = tempAccountId
                                    selectedTempAccountCurrency = tempAccountCurrency
                                    sheetType = SheetType.ACCOUNT
                                },
                                toggleAccountSelected = toggleAccountSelected,
                                toggleRestoreBalance = toggleRestoreBalance,
                                setAccountMapping = setAccountMapping,
                                contentPadding = PaddingValues(start = 10.dp, end = 10.dp),
                            )
                        }

                        4 -> {
                            ImportScreenSelectTemplatesContent(
                                tempTransactionTemplatesWithIcons = tempTransactionTemplatesWithIcons,
                                enabledTempTransactionTemplates = enabledTempTransactionTemplates,
                                selectedTempTransactionTemplates = selectedTempTransactionTemplates,
                                toggleTransactionTemplateSelected = toggleTransactionTemplateSelected,
                                contentPadding = PaddingValues(start = 10.dp, end = 10.dp),
                            )
                        }

                        5 -> {
                            ImportScreenSelectTransactionsContent(
                                loading = loading,
                                searchText = searchText,
                                setSearchText = setSearchText,
                                enabled = enabled,
                                focusRequester = focusRequester,
                                interactionSource = interactionSource,
                                transactions = transactions,
                                selectedLocalDates = selectedLocalDates,
                                enabledLocalDates = enabledLocalDates,
                                enabledTempTransactions = enabledTempTransactions,
                                selectedTransactions = selectedTransactions,
                                toggleLocalDateSelected = toggleLocalDateSelected,
                                toggleTransactionSelected = toggleTransactionSelected,
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