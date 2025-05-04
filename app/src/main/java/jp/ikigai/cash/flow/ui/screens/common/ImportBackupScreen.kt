package jp.ikigai.cash.flow.ui.screens.common

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import io.realm.kotlin.query.Sort
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.data.dto.TransactionWithIcons
import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.data.entity.CounterParty
import jp.ikigai.cash.flow.data.entity.Method
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.data.enums.PopupType
import jp.ikigai.cash.flow.ui.components.bottombars.ImportBackupScreenRoundedBottomBar
import jp.ikigai.cash.flow.ui.components.cards.MapCategoryCard
import jp.ikigai.cash.flow.ui.components.cards.MapCounterPartyCard
import jp.ikigai.cash.flow.ui.components.cards.MapMethodCard
import jp.ikigai.cash.flow.ui.components.cards.MapSourceCard
import jp.ikigai.cash.flow.ui.components.cards.TransactionCard
import jp.ikigai.cash.flow.ui.components.cards.TransactionTemplateCard
import jp.ikigai.cash.flow.ui.components.common.OneHandModeScaffold
import jp.ikigai.cash.flow.ui.components.common.OneHandModeSpacer
import jp.ikigai.cash.flow.ui.components.common.TransactionGroupHeader
import jp.ikigai.cash.flow.ui.components.common.WaitDialog
import jp.ikigai.cash.flow.ui.components.popups.AmountFilterPopup
import jp.ikigai.cash.flow.ui.components.popups.ConfirmNavigationPopup
import jp.ikigai.cash.flow.ui.components.popups.DateRangePickerPopup
import jp.ikigai.cash.flow.ui.components.popups.FilterCurrencyPopup
import jp.ikigai.cash.flow.ui.components.popups.FilterTransactionTypePopup
import jp.ikigai.cash.flow.ui.components.popups.ReviewDetailsPopup
import jp.ikigai.cash.flow.ui.components.popups.SelectCategoryPopup
import jp.ikigai.cash.flow.ui.components.popups.SelectCounterPartyPopup
import jp.ikigai.cash.flow.ui.components.popups.SelectMethodPopup
import jp.ikigai.cash.flow.ui.components.popups.SelectSourcePopup
import jp.ikigai.cash.flow.ui.screenStates.common.restore.ImportBackupScreenEnabledState
import jp.ikigai.cash.flow.ui.screenStates.common.restore.ImportBackupScreenMainState
import jp.ikigai.cash.flow.ui.screenStates.common.restore.ImportBackupScreenSelectionState
import jp.ikigai.cash.flow.ui.viewmodels.common.ImportBackupScreenViewModel
import jp.ikigai.cash.flow.utils.animatedComposable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.io.InputStream
import java.time.LocalDate
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ImportBackupScreen(
    navigateBack: () -> Unit,
    setLocale: (Locale?) -> Unit,
    loadFile: (InputStream?) -> Unit,
    import: () -> Unit,
    toggleSelection: (Boolean, Map<LocalDate, List<TransactionWithIcons>>) -> Unit,
    toggleTransactionSelected: (String) -> Unit,
    toggleLocalDateSelected: (Boolean, List<TransactionWithIcons>) -> Unit,
    setSearchText: (String) -> Unit,
    setSelectedCurrencies: (Map<String, Boolean>) -> Unit,
    setStartDateAndEndDate: (LocalDate?, LocalDate?) -> Unit,
    setSelectedTransactionTypes: (List<Int>) -> Unit,
    setSortDirection: (Sort) -> Unit,
    filterByAmount: (Double, Double) -> Unit,
    toggleTransactionTemplateSelected: (String) -> Unit,
    setCategoryMapping: (String, Category) -> Unit,
    toggleCategorySelected: (String) -> Unit,
    setCounterPartyMapping: (String, CounterParty) -> Unit,
    toggleCounterPartySelected: (String) -> Unit,
    setMethodMapping: (String, Method) -> Unit,
    toggleMethodSelected: (String) -> Unit,
    setSourceMapping: (String, Source) -> Unit,
    toggleSourceSelected: (String) -> Unit,
    toggleRestoreBalance: (String) -> Unit,
    checkPageValidity: (Int) -> Unit,
    events: Flow<Event?>,
    mainState: ImportBackupScreenMainState,
    enabledState: ImportBackupScreenEnabledState,
    selectionState: ImportBackupScreenSelectionState,
    selectedTransactionsState: Set<String>
) {
    val haptics = LocalHapticFeedback.current
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val keyboardController = LocalSoftwareKeyboardController.current

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

    val scrollScope = rememberCoroutineScope()

    val enabled by remember(key1 = mainState.enabled) {
        mutableStateOf(mainState.enabled)
    }

    val loading by remember(key1 = mainState.loading) {
        mutableStateOf(mainState.loading)
    }

    val importOngoing by remember(key1 = mainState.importOngoing) {
        mutableStateOf(mainState.importOngoing)
    }

    var popupType by remember {
        mutableStateOf(PopupType.NONE)
    }

    val dataLoadComplete by remember(key1 = mainState.dataLoadComplete) {
        mutableStateOf(mainState.dataLoadComplete)
    }

    val searchText by remember(key1 = mainState.searchText) {
        mutableStateOf(mainState.searchText)
    }

    val currencies by remember(key1 = mainState.currencies) {
        mutableStateOf(mainState.currencies)
    }

    val selectedCurrencies by remember(key1 = mainState.selectedCurrencies) {
        mutableStateOf(mainState.selectedCurrencies)
    }

    val selectedCurrencyCount by remember(key1 = mainState.selectedCurrencyCount) {
        mutableStateOf(mainState.selectedCurrencyCount)
    }

    val startDate by remember(key1 = mainState.startDate) {
        mutableStateOf(mainState.startDate)
    }

    val startDateString by remember(key1 = mainState.startDateString) {
        mutableStateOf(mainState.startDateString)
    }

    val endDate by remember(key1 = mainState.endDate) {
        mutableStateOf(mainState.endDate)
    }

    val endDateString by remember(key1 = mainState.endDateString) {
        mutableStateOf(mainState.endDateString)
    }

    val dateRangeStringRes by remember(key1 = mainState.dateRangeStringRes) {
        mutableIntStateOf(mainState.dateRangeStringRes)
    }

    val sortDirection by remember(key1 = mainState.sortDirection) {
        mutableStateOf(mainState.sortDirection)
    }

    val transactions by remember(
        key1 = mainState.filteredTransactions,
        key2 = mainState.transactionsHashCode
    ) {
        mutableStateOf(mainState.filteredTransactions)
    }

    val enabledTempTransactions by remember(key1 = enabledState.enabledTempTransactions) {
        mutableStateOf(enabledState.enabledTempTransactions)
    }

    val selectedTransactions by remember(key1 = selectedTransactionsState) {
        mutableStateOf(selectedTransactionsState)
    }

    val selectedTransactionsCount by remember(key1 = selectionState.selectedTransactionsCount) {
        mutableStateOf(selectionState.selectedTransactionsCount)
    }

    val allSelected by remember(key1 = selectionState.allSelected) {
        mutableStateOf(selectionState.allSelected)
    }

    val enabledLocalDates by remember(key1 = enabledState.enabledLocalDates) {
        mutableStateOf(enabledState.enabledLocalDates)
    }

    val selectedLocalDates by remember(key1 = selectionState.selectedLocalDates) {
        mutableStateOf(selectionState.selectedLocalDates)
    }

    //region Category State Variables

    val tempCategories by remember(key1 = mainState.tempCategories) {
        mutableStateOf(mainState.tempCategories)
    }

    val selectedTempCategories by remember(key1 = mainState.selectedTempCategories) {
        mutableStateOf(mainState.selectedTempCategories)
    }

    val selectedTempCategoryCount by remember(key1 = mainState.selectedTempCategoryCount) {
        mutableStateOf(mainState.selectedTempCategoryCount)
    }

    var selectedTempCategoryUUID by remember {
        mutableStateOf("")
    }

    val conflictingTempCategories by remember(key1 = mainState.conflictingTempCategories) {
        mutableStateOf(mainState.conflictingTempCategories)
    }

    val dbCategories by remember(key1 = mainState.dbCategories) {
        mutableStateOf(mainState.dbCategories)
    }

    val categoryMappings by remember(key1 = mainState.categoryMappings) {
        mutableStateOf(mainState.categoryMappings)
    }

    //endregion

    //region CounterParty State Variables

    val tempCounterParties by remember(key1 = mainState.tempCounterParties) {
        mutableStateOf(mainState.tempCounterParties)
    }

    val selectedTempCounterParties by remember(key1 = mainState.selectedTempCounterParties) {
        mutableStateOf(mainState.selectedTempCounterParties)
    }

    val selectedTempCounterPartyCount by remember(key1 = mainState.selectedTempCounterPartyCount) {
        mutableStateOf(mainState.selectedTempCounterPartyCount)
    }

    var selectedTempCounterPartyUUID by remember {
        mutableStateOf("")
    }

    val conflictingTempCounterParties by remember(key1 = mainState.conflictingTempCounterParties) {
        mutableStateOf(mainState.conflictingTempCounterParties)
    }

    val dbCounterParties by remember(key1 = mainState.dbCounterParties) {
        mutableStateOf(mainState.dbCounterParties)
    }

    val counterPartyMappings by remember(key1 = mainState.counterPartyMappings) {
        mutableStateOf(mainState.counterPartyMappings)
    }

    //endregion

    //region Method State Variables

    val tempMethods by remember(key1 = mainState.tempMethods) {
        mutableStateOf(mainState.tempMethods)
    }

    val selectedTempMethods by remember(key1 = mainState.selectedTempMethods) {
        mutableStateOf(mainState.selectedTempMethods)
    }

    val selectedTempMethodCount by remember(key1 = mainState.selectedTempMethodCount) {
        mutableStateOf(mainState.selectedTempMethodCount)
    }

    var selectedTempMethodUUID by remember {
        mutableStateOf("")
    }

    val conflictingTempMethods by remember(key1 = mainState.conflictingTempMethods) {
        mutableStateOf(mainState.conflictingTempMethods)
    }

    val dbMethods by remember(key1 = mainState.dbMethods) {
        mutableStateOf(mainState.dbMethods)
    }

    val methodMappings by remember(key1 = mainState.methodMappings) {
        mutableStateOf(mainState.methodMappings)
    }

    //endregion

    //region Source State Variables

    val tempSources by remember(key1 = mainState.tempSources) {
        mutableStateOf(mainState.tempSources)
    }

    val selectedTempSources by remember(key1 = mainState.selectedTempSources) {
        mutableStateOf(mainState.selectedTempSources)
    }

    val selectedTempSourceCount by remember(key1 = mainState.selectedTempSourceCount) {
        mutableStateOf(mainState.selectedTempSourceCount)
    }

    var selectedTempSourceUUID by remember {
        mutableStateOf("")
    }

    val restoreBalanceSources by remember(key1 = mainState.restoreBalanceSources) {
        mutableStateOf(mainState.restoreBalanceSources)
    }

    val conflictingTempSources by remember(key1 = mainState.conflictingTempSources) {
        mutableStateOf(mainState.conflictingTempSources)
    }

    val currencySourceMap by remember(key1 = mainState.currencySourceMap) {
        mutableStateOf(mainState.currencySourceMap)
    }

    val sourceMappings by remember(key1 = mainState.sourceMappings) {
        mutableStateOf(mainState.sourceMappings)
    }

    //endregion

    //region Template State Variables

    val tempTransactionTemplatesWithIcons by remember(key1 = mainState.tempTransactionTemplatesWithIcons) {
        mutableStateOf(mainState.tempTransactionTemplatesWithIcons)
    }

    val selectedTempTransactionTemplates by remember(key1 = mainState.selectedTempTransactionTemplates) {
        mutableStateOf(mainState.selectedTempTransactionTemplates)
    }

    val selectedTempTransactionTemplateCount by remember(key1 = selectionState.selectedTempTransactionTemplateCount) {
        mutableStateOf(selectionState.selectedTempTransactionTemplateCount)
    }

    val enabledTempTransactionTemplates by remember(key1 = enabledState.enabledTempTransactionTemplates) {
        mutableStateOf(enabledState.enabledTempTransactionTemplates)
    }

    //endregion

    val selectedTransactionTypes by remember(key1 = mainState.selectedTransactionTypes) {
        mutableStateOf(mainState.selectedTransactionTypes)
    }

    val filterAmountMin by remember(key1 = mainState.filterAmountMin) {
        mutableDoubleStateOf(mainState.filterAmountMin)
    }

    val filterAmountMax by remember(key1 = mainState.filterAmountMax) {
        mutableDoubleStateOf(mainState.filterAmountMax)
    }

    val filterAmountRange by remember(key1 = mainState.filterAmountRange) {
        mutableStateOf(mainState.filterAmountRange)
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
                scrollScope.launch {
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
            scrollScope.launch {
                pagerState.animateScrollToPage(pagerState.currentPage - 1)
            }
        } else if (dataLoadComplete) {
            popupType = PopupType.CONFIRM_NAVIGATION
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

    if (importOngoing) {
        WaitDialog()
    }

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
        showBottomPopup = popupType != PopupType.NONE,
        bottomPopupContent = { hidePopup ->
            when (popupType) {
                PopupType.REVIEW -> {
                    ReviewDetailsPopup(
                        selectedTempCategoryCount = selectedTempCategoryCount,
                        selectedTempCounterPartyCount = selectedTempCounterPartyCount,
                        selectedTempMethodCount = selectedTempMethodCount,
                        selectedTempSourceCount = selectedTempSourceCount,
                        selectedTempTransactionTemplateCount = selectedTempTransactionTemplateCount,
                        selectedTransactionsCount = selectedTransactionsCount,
                        dismiss = hidePopup,
                        complete = import
                    )
                }

                PopupType.CONFIRM_NAVIGATION -> {
                    ConfirmNavigationPopup(
                        message = stringResource(id = R.string.navigation_confirmation_label),
                        dismiss = hidePopup,
                        navigate = navigateBack
                    )
                }

                PopupType.DATE_RANGE -> {
                    DateRangePickerPopup(
                        startDate = startDate,
                        endDate = endDate,
                        filter = setStartDateAndEndDate,
                        reset = {
                            setStartDateAndEndDate(null, null)
                        },
                        dismiss = hidePopup
                    )
                }

                PopupType.CURRENCY -> {
                    FilterCurrencyPopup(
                        selectedCurrencyMap = selectedCurrencies,
                        currencies = currencies,
                        filter = setSelectedCurrencies,
                        dismiss = hidePopup
                    )
                }

                PopupType.AMOUNT -> {
                    AmountFilterPopup(
                        minAmount = filterAmountMin,
                        maxAmount = filterAmountMax,
                        filter = filterByAmount,
                        dismiss = hidePopup
                    )
                }

                PopupType.CATEGORY -> {
                    SelectCategoryPopup(
                        index = dbCategories.indexOfFirst { it.uuid == categoryMappings[selectedTempCategoryUUID]?.uuid }
                            .coerceAtLeast(0),
                        selectedCategoryUUID = categoryMappings[selectedTempCategoryUUID]?.uuid
                            ?: "",
                        categories = dbCategories,
                        setSelectedCategory = {
                            setCategoryMapping(selectedTempCategoryUUID, it)
                        },
                        dismiss = hidePopup
                    )
                }

                PopupType.COUNTERPARTY -> {
                    SelectCounterPartyPopup(
                        index = dbCounterParties.indexOfFirst { it.uuid == counterPartyMappings[selectedTempCounterPartyUUID]?.uuid }
                            .coerceAtLeast(0),
                        selectedCounterPartyUUID = counterPartyMappings[selectedTempCounterPartyUUID]?.uuid
                            ?: "",
                        counterParties = dbCounterParties,
                        setSelectedCounterParty = {
                            setCounterPartyMapping(selectedTempCounterPartyUUID, it)
                        },
                        dismiss = hidePopup
                    )
                }

                PopupType.METHOD -> {
                    SelectMethodPopup(
                        index = dbMethods.indexOfFirst { it.uuid == methodMappings[selectedTempMethodUUID]?.uuid }
                            .coerceAtLeast(0),
                        selectedMethodUUID = methodMappings[selectedTempMethodUUID]?.uuid ?: "",
                        methods = dbMethods,
                        setSelectedMethod = {
                            setMethodMapping(selectedTempMethodUUID, it)
                        },
                        dismiss = hidePopup
                    )
                }

                PopupType.SOURCE -> {
                    SelectSourcePopup(
                        index = currencySourceMap[sourceMappings[selectedTempSourceUUID]?.currency]
                            ?.indexOfFirst { it.uuid == sourceMappings[selectedTempSourceUUID]?.uuid }
                            ?.coerceAtLeast(0) ?: 0,
                        selectedSourceUUID = sourceMappings[selectedTempSourceUUID]?.uuid ?: "",
                        sources = currencySourceMap[sourceMappings[selectedTempSourceUUID]?.currency]
                            ?: emptyList(),
                        setSelectedSource = {
                            setSourceMapping(selectedTempSourceUUID, it)
                        },
                        dismiss = hidePopup
                    )
                }

                PopupType.TYPE -> {
                    FilterTransactionTypePopup(
                        selectedTransactionTypes = selectedTransactionTypes,
                        filter = setSelectedTransactionTypes,
                        dismiss = hidePopup
                    )
                }

                else -> {}
            }
        },
        onDismissPopup = {
            popupType = PopupType.NONE
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
                                    Text(text = stringResource(id = R.string.map_sources_label))
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
                            scrollScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        } else if (dataLoadComplete) {
                            popupType = PopupType.CONFIRM_NAVIGATION
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
                    if (sortDirection == Sort.DESCENDING) {
                        setSortDirection(Sort.ASCENDING)
                    } else {
                        setSortDirection(Sort.DESCENDING)
                    }
                },
                onFilterByAmountClick = {
                    popupType = PopupType.AMOUNT
                },
                onFilterByTypeClick = {
                    popupType = PopupType.TYPE
                },
                onFilterByCurrencyClick = {
                    popupType = PopupType.CURRENCY
                },
                onSearchClick = {
                    if (isFocused) {
                        keyboardController?.show()
                    } else {
                        focusRequester.requestFocus()
                    }
                },
                onToggleSelectClick = {
                    toggleSelection(allSelected, transactions)
                },
                onCalendarClick = {
                    popupType = PopupType.DATE_RANGE
                },
                actionButtonClick = {
                    if (dataLoadComplete && selectedTransactionsCount.isNotEmpty()) {
                        popupType = PopupType.REVIEW
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
                                key = { tempCategory -> tempCategory.uuid }
                            ) { tempCategory ->
                                MapCategoryCard(
                                    tempCategory = tempCategory,
                                    mappedCategory = categoryMappings[tempCategory.uuid]
                                        ?: Category(),
                                    modifier = Modifier.animateItem(),
                                    selected = selectedTempCategories[tempCategory.uuid] == true,
                                    conflicting = conflictingTempCategories.contains(tempCategory.uuid),
                                    selectCategory = {
                                        selectedTempCategoryUUID = tempCategory.uuid
                                        popupType = PopupType.CATEGORY
                                    },
                                    toggleSelected = {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        toggleCategorySelected(tempCategory.uuid)
                                    },
                                    clearSelectedCategory = {
                                        setCategoryMapping(tempCategory.uuid, Category())
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
                                key = { tempCounterParty -> tempCounterParty.uuid }
                            ) { tempCounterParty ->
                                MapCounterPartyCard(
                                    tempCounterParty = tempCounterParty,
                                    mappedCounterParty = counterPartyMappings[tempCounterParty.uuid]
                                        ?: CounterParty(),
                                    modifier = Modifier.animateItem(),
                                    selected = selectedTempCounterParties[tempCounterParty.uuid] == true,
                                    conflicting = conflictingTempCounterParties.contains(
                                        tempCounterParty.uuid
                                    ),
                                    selectCounterParty = {
                                        selectedTempCounterPartyUUID = tempCounterParty.uuid
                                        popupType = PopupType.COUNTERPARTY
                                    },
                                    toggleSelected = {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        toggleCounterPartySelected(tempCounterParty.uuid)
                                    },
                                    clearSelectedCounterParty = {
                                        setCounterPartyMapping(
                                            tempCounterParty.uuid,
                                            CounterParty()
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
                                key = { tempMethod -> tempMethod.uuid }
                            ) { tempMethod ->
                                MapMethodCard(
                                    tempMethod = tempMethod,
                                    mappedMethod = methodMappings[tempMethod.uuid] ?: Method(),
                                    modifier = Modifier.animateItem(),
                                    selected = selectedTempMethods[tempMethod.uuid] == true,
                                    conflicting = conflictingTempMethods.contains(tempMethod.uuid),
                                    selectMethod = {
                                        selectedTempMethodUUID = tempMethod.uuid
                                        popupType = PopupType.METHOD
                                    },
                                    toggleSelected = {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        toggleMethodSelected(tempMethod.uuid)
                                    },
                                    clearSelectedMethod = {
                                        setMethodMapping(tempMethod.uuid, Method())
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
                                items = tempSources,
                                key = { tempSource -> tempSource.uuid }
                            ) { tempSource ->
                                MapSourceCard(
                                    tempSource = tempSource,
                                    mappedSource = sourceMappings[tempSource.uuid] ?: Source(),
                                    modifier = Modifier.animateItem(),
                                    selected = selectedTempSources[tempSource.uuid] == true,
                                    restoreBalance = restoreBalanceSources.contains(tempSource.uuid),
                                    conflicting = conflictingTempSources.contains(tempSource.uuid),
                                    selectSource = {
                                        selectedTempSourceUUID = tempSource.uuid
                                        popupType = PopupType.SOURCE
                                    },
                                    toggleSelected = {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        toggleSourceSelected(tempSource.uuid)
                                    },
                                    toggleRestoreBalance = {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        toggleRestoreBalance(tempSource.uuid)
                                    },
                                    clearSelectedSource = {
                                        setSourceMapping(tempSource.uuid, Source())
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
                                    key = { transactionTemplateWithIcons -> transactionTemplateWithIcons.uuid }
                                ) { transactionTemplateWithIcons ->
                                    TransactionTemplateCard(
                                        checked = enabledTempTransactionTemplates.contains(
                                            transactionTemplateWithIcons.uuid
                                        ) && selectedTempTransactionTemplates.contains(
                                            transactionTemplateWithIcons.uuid
                                        ),
                                        enabled = enabledTempTransactionTemplates.contains(
                                            transactionTemplateWithIcons.uuid
                                        ),
                                        modifier = Modifier.animateItem(),
                                        transactionTemplateWithIcons = transactionTemplateWithIcons,
                                        onClick = {
                                            toggleTransactionTemplateSelected(
                                                transactionTemplateWithIcons.uuid
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    5 -> {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        top = 5.dp,
                                        start = 10.dp,
                                        end = 10.dp,
                                        bottom = 10.dp
                                    ),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                OutlinedTextField(
                                    value = searchText,
                                    onValueChange = setSearchText,
                                    enabled = enabled,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(focusRequester = focusRequester),
                                    label = {
                                        Text(text = stringResource(id = R.string.search_field_label))
                                    },
                                    trailingIcon = {
                                        AnimatedVisibility(
                                            visible = searchText.isNotEmpty(),
                                            enter = scaleIn() + fadeIn(),
                                            exit = scaleOut() + fadeOut()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Clear,
                                                contentDescription = "clear field",
                                                modifier = Modifier
                                                    .clickable(
                                                        enabled = enabled,
                                                        onClick = {
                                                            setSearchText("")
                                                        }
                                                    )
                                            )
                                        }
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            keyboardController?.hide()
                                        }
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    interactionSource = interactionSource
                                )
                            }
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
                                transactions.forEach {
                                    stickyHeader {
                                        TransactionGroupHeader(
                                            date = it.key,
                                            selected = selectedLocalDates.contains(it.key),
                                            enabled = enabled && enabledLocalDates.contains(
                                                it.key
                                            ),
                                            onClick = {
                                                resetOneHandMode()
                                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                toggleLocalDateSelected(
                                                    selectedLocalDates.contains(it.key),
                                                    transactions[it.key] ?: emptyList()
                                                )
                                            }
                                        )
                                    }
                                    items(
                                        items = it.value,
                                        key = { transactionWithIcons -> transactionWithIcons.uuid }
                                    ) { transactionWithIcons ->
                                        TransactionCard(
                                            checked = enabledTempTransactions.contains(
                                                transactionWithIcons.uuid
                                            ) && selectedTransactions.contains(transactionWithIcons.uuid),
                                            enabled = enabled && enabledTempTransactions.contains(
                                                transactionWithIcons.uuid
                                            ),
                                            transactionWithIcons = transactionWithIcons,
                                            onClick = {
                                                resetOneHandMode()
                                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                toggleTransactionSelected(transactionWithIcons.uuid)
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
        toggleSelection = { _, _ -> },
        toggleTransactionSelected = {},
        toggleLocalDateSelected = { _, _ -> },
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
        setSourceMapping = { _, _ -> },
        toggleSourceSelected = {},
        toggleRestoreBalance = {},
        checkPageValidity = {},
        events = emptyList<Event>().asFlow(),
        mainState = ImportBackupScreenMainState(),
        enabledState = ImportBackupScreenEnabledState(),
        selectionState = ImportBackupScreenSelectionState(),
        selectedTransactionsState = emptySet()
    )
}

fun NavGraphBuilder.importBackupScreen(navController: NavController) {
    animatedComposable(
        route = Routes.ImportBackup.route
    ) {
        val viewModel: ImportBackupScreenViewModel = koinViewModel()
        val mainState by viewModel.mainState.collectAsState()
        val enabledState by viewModel.enabledState.collectAsState()
        val selectionState by viewModel.selectionState.collectAsState()
        val selectedTransactionsState by viewModel.selectedTransactionsState.collectAsState()

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
            setSourceMapping = viewModel::setSelectedSource,
            toggleSourceSelected = viewModel::toggleSourceSelected,
            toggleRestoreBalance = viewModel::toggleRestoreBalance,
            checkPageValidity = viewModel::checkPageValidity,
            events = viewModel.event,
            mainState = mainState,
            enabledState = enabledState,
            selectionState = selectionState,
            selectedTransactionsState = selectedTransactionsState
        )
    }
}