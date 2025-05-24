package jp.ikigai.cash.flow.ui.screens.migration

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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import io.objectbox.query.QueryBuilder
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.data.enums.PopupType
import jp.ikigai.cash.flow.data.store.entity.Method
import jp.ikigai.cash.flow.data.store.entity.Transaction
import jp.ikigai.cash.flow.data.store.entity.Transaction_
import jp.ikigai.cash.flow.ui.components.bottombars.MigrateMethodScreenRoundedBottomBar
import jp.ikigai.cash.flow.ui.components.cards.TransactionCard
import jp.ikigai.cash.flow.ui.components.common.OneHandModeScaffold
import jp.ikigai.cash.flow.ui.components.common.OneHandModeSpacer
import jp.ikigai.cash.flow.ui.components.common.TransactionGroupHeader
import jp.ikigai.cash.flow.ui.components.popups.AmountFilterPopup
import jp.ikigai.cash.flow.ui.components.popups.DateRangePickerPopup
import jp.ikigai.cash.flow.ui.components.popups.FilterAccountPopup
import jp.ikigai.cash.flow.ui.components.popups.FilterCategoryPopup
import jp.ikigai.cash.flow.ui.components.popups.FilterCounterPartyPopup
import jp.ikigai.cash.flow.ui.components.popups.FilterCurrencyPopup
import jp.ikigai.cash.flow.ui.components.popups.FilterTransactionTypePopup
import jp.ikigai.cash.flow.ui.components.popups.MigrateMethodPopup
import jp.ikigai.cash.flow.ui.screenStates.common.SortOptionsState
import jp.ikigai.cash.flow.ui.screenStates.common.TransactionFilters
import jp.ikigai.cash.flow.ui.screenStates.migration.MigrateMethodScreenState
import jp.ikigai.cash.flow.ui.viewmodels.migration.MigrateMethodScreenViewModel
import jp.ikigai.cash.flow.utils.animatedComposable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MigrateMethodScreen(
    navigateBack: () -> Unit,
    migrate: (Method) -> Unit,
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
    setSelectedTransactionTypes: (List<Int>) -> Unit,
    setSortFlags: (Int) -> Unit,
    filterByAmount: (Double, Double) -> Unit,
    events: Flow<Event>,
    state: MigrateMethodScreenState,
    filtersState: TransactionFilters,
    sortOptionsState: SortOptionsState<Transaction>
) {
    val configuration = LocalConfiguration.current
    val haptics = LocalHapticFeedback.current
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

    var popupType by remember {
        mutableStateOf(PopupType.NONE)
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

    val sortFlags by remember(key1 = sortOptionsState.sortFlags) {
        mutableIntStateOf(sortOptionsState.sortFlags)
    }

    val migrateEnabled by remember(
        key1 = state.selectedTransactionCount,
        key2 = state.enabled
    ) {
        mutableStateOf(state.enabled && state.selectedTransactionCount > 0)
    }

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
        showEmptyPlaceholder = false,
        emptyPlaceholderText = "",
        showBottomPopup = popupType != PopupType.NONE,
        bottomPopupContent = { hidePopup ->
            when (popupType) {
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
                        selectedCurrencyCodes = selectedCurrencies,
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
                    FilterCategoryPopup(
                        selectedCategoryIds = selectedCategories,
                        categories = categories,
                        filter = setSelectedCategories,
                        dismiss = hidePopup
                    )
                }

                PopupType.COUNTERPARTY -> {
                    FilterCounterPartyPopup(
                        selectedCounterPartyIds = selectedCounterParties,
                        includeNoCounterPartyTransactions = includeNoCounterPartyTransactions,
                        counterParties = counterParties,
                        filter = setSelectedCounterParties,
                        dismiss = hidePopup
                    )
                }

                PopupType.METHOD -> {
                    MigrateMethodPopup(
                        migrateCount = selectedTransactionCount,
                        selectMethod = migrate,
                        methods = methods,
                        dismiss = hidePopup
                    )
                }

                PopupType.ACCOUNT -> {
                    FilterAccountPopup(
                        selectedAccountIds = selectedAccounts,
                        accounts = accounts,
                        filter = setSelectedAccounts,
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
                }
            )
        },
        bottomBar = {
            MigrateMethodScreenRoundedBottomBar(
                navigateBack = navigateBack,
                enabled = enabled,
                migrateEnabled = migrateEnabled,
                allSelected = allSelected,
                sortFlags = sortFlags,
                filterAmount = filterAmountRange,
                selectedCurrencyCount = selectedCurrencyCount,
                selectedAccountCount = selectedAccountCount,
                selectedCategoryCount = selectedCategoryCount,
                selectedCounterPartyCount = selectedCounterPartyCount,
                counterPartyFilterVisible = counterParties.isNotEmpty(),
                selectedTransactionTypeCount = selectedTransactionTypes.size,
                onSortClick = {
                    if (sortFlags == QueryBuilder.DESCENDING) {
                        setSortFlags(0)
                    } else {
                        setSortFlags(QueryBuilder.DESCENDING)
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
                onFilterByCategoryClick = {
                    popupType = PopupType.CATEGORY
                },
                onFilterByCounterPartyClick = {
                    popupType = PopupType.COUNTERPARTY
                },
                onFilterBySourceClick = {
                    popupType = PopupType.ACCOUNT
                },
                onCalendarClick = {
                    popupType = PopupType.DATE_RANGE
                },
                migrateTransactions = {
                    popupType = PopupType.METHOD
                },
                onSearchClick = {
                    if (isFocused) {
                        keyboardController?.show()
                    } else {
                        focusRequester.requestFocus()
                    }
                },
                onToggleSelectClick = toggleSelection
            )
        }
    ) { oneHandModeBoxHeight, resetOneHandMode ->
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp, start = 10.dp, end = 10.dp, bottom = 10.dp),
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
                                        enabled = true,
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
                            enabled = enabled,
                            onClick = {
                                resetOneHandMode()
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                toggleLocalDateSelected(it.key)
                            }
                        )
                    }
                    items(
                        items = it.value,
                        key = { transactionWithChips -> transactionWithChips.id }
                    ) { transactionWithChips ->
                        TransactionCard(
                            checked = selectedTransactions.contains(transactionWithChips.id),
                            enabled = enabled,
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
        setSortFlags = {},
        filterByAmount = { _, _ -> },
        events = emptyList<Event>().asFlow(),
        state = MigrateMethodScreenState(),
        filtersState = TransactionFilters(),
        sortOptionsState = SortOptionsState(sortField = Transaction_.time)
    )
}

fun NavGraphBuilder.migrateMethodScreen(navController: NavController) {
    animatedComposable(
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
        val sortOptionsState by viewModel.sortOptionsState.collectAsState()

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
            setSortFlags = viewModel::setSortFlags,
            filterByAmount = viewModel::setFilterAmounts,
            events = viewModel.event,
            state = state,
            filtersState = filtersState,
            sortOptionsState = sortOptionsState
        )
    }
}