package jp.ikigai.cash.flow.ui.screens.listing

import android.icu.util.Currency
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import io.objectbox.query.QueryBuilder
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.data.enums.PopupType
import jp.ikigai.cash.flow.data.store.entity.Transaction
import jp.ikigai.cash.flow.data.store.entity.Transaction_
import jp.ikigai.cash.flow.ui.components.bottombars.TransactionScreenRoundedBottomBar
import jp.ikigai.cash.flow.ui.components.cards.TransactionCard
import jp.ikigai.cash.flow.ui.components.common.BottomPopup
import jp.ikigai.cash.flow.ui.components.common.ToastBar
import jp.ikigai.cash.flow.ui.components.common.TotalTransactionInfo
import jp.ikigai.cash.flow.ui.components.common.TransactionGroupHeader
import jp.ikigai.cash.flow.ui.components.popups.AmountFilterPopup
import jp.ikigai.cash.flow.ui.components.popups.CloneTransactionPopup
import jp.ikigai.cash.flow.ui.components.popups.CurrencyPopup
import jp.ikigai.cash.flow.ui.components.popups.DateRangePickerPopup
import jp.ikigai.cash.flow.ui.components.popups.FilterAccountPopup
import jp.ikigai.cash.flow.ui.components.popups.FilterCategoryPopup
import jp.ikigai.cash.flow.ui.components.popups.FilterCounterPartyPopup
import jp.ikigai.cash.flow.ui.components.popups.FilterMethodPopup
import jp.ikigai.cash.flow.ui.components.popups.FilterTransactionTypePopup
import jp.ikigai.cash.flow.ui.components.popups.MoreOptionsPopup
import jp.ikigai.cash.flow.ui.components.popups.SelectTemplatePopup
import jp.ikigai.cash.flow.ui.screenStates.common.SortOptionsState
import jp.ikigai.cash.flow.ui.screenStates.listing.transactions.FiltersState
import jp.ikigai.cash.flow.ui.screenStates.listing.transactions.TransactionsScreenState
import jp.ikigai.cash.flow.ui.viewmodels.listing.TransactionsScreenViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import java.time.ZonedDateTime
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransactionsScreen(
    canAddTransaction: () -> Boolean,
    addTransaction: (Long) -> Unit,
    editTransaction: (Long) -> Unit,
    searchState: String,
    setSearchText: (String) -> Unit,
    setLocale: (Locale?) -> Unit,
    setCurrency: (String) -> Unit,
    setStartDateAndEndDate: (ZonedDateTime, ZonedDateTime) -> Unit,
    setSelectedAccounts: (Map<Long, Boolean>) -> Unit,
    setSelectedCategories: (Map<Long, Boolean>) -> Unit,
    setSelectedCounterParties: (Map<Long, Boolean>) -> Unit,
    setSelectedMethods: (Map<Long, Boolean>) -> Unit,
    setSelectedTransactionTypes: (List<Int>) -> Unit,
    setSortFlags: (Int) -> Unit,
    filterByAmount: (Double, Double) -> Unit,
    cloneTransaction: (Long, Boolean) -> Unit,
    navigateToCategoriesScreen: () -> Unit,
    navigateToCounterPartyScreen: () -> Unit,
    navigateToMethodsScreen: () -> Unit,
    navigateToTemplatesScreen: () -> Unit,
    navigateToSourcesScreen: () -> Unit,
    navigateToSettingsScreen: () -> Unit,
    openGithubPage: () -> Unit,
    events: Flow<Event>,
    state: TransactionsScreenState,
    filtersState: FiltersState,
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

    val transactions by remember(key1 = state.transactions, key2 = state.transactionsHashCode) {
        mutableStateOf(state.transactions)
    }

    val templates by remember(key1 = state.templates) {
        mutableStateOf(state.templates)
    }

    val currencies by remember(key1 = state.currencies) {
        mutableStateOf(state.currencies)
    }

    val balance by remember(key1 = state.balance) {
        mutableStateOf(state.balance)
    }

    val selectedCurrency by remember(key1 = filtersState.selectedCurrency) {
        mutableStateOf(filtersState.selectedCurrency)
    }

    val selectedCurrencySymbol by remember(key1 = filtersState.selectedCurrency) {
        mutableStateOf(Currency.getInstance(filtersState.selectedCurrency).symbol)
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

    val totalExpense by remember(key1 = state.expense) {
        mutableStateOf(state.expense)
    }

    val expenseTransactionsCount by remember(key1 = state.expenseTransactionsCount) {
        mutableStateOf(state.expenseTransactionsCount)
    }

    val totalIncome by remember(key1 = state.income) {
        mutableStateOf(state.income)
    }

    val incomeTransactionsCount by remember(key1 = state.incomeTransactionsCount) {
        mutableStateOf(state.incomeTransactionsCount)
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

    val methods by remember(key1 = state.methods) {
        mutableStateOf(state.methods)
    }

    val selectedMethods by remember(key1 = filtersState.selectedMethods) {
        mutableStateOf(filtersState.selectedMethods)
    }

    val selectedMethodCount by remember(key1 = filtersState.selectedMethodCount) {
        mutableStateOf(filtersState.selectedMethodCount)
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

    var selectedTransactionId by remember {
        mutableLongStateOf(0L)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .animateContentSize()
                .navigationBarsPadding()
                .imePadding()
                .fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(text = stringResource(id = R.string.transactions_label))
                            Text(
                                text = stringResource(
                                    id = R.string.date_range_label,
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
                TransactionScreenRoundedBottomBar(
                    selectedCurrencySymbol = selectedCurrencySymbol,
                    filterAmount = filterAmountRange,
                    sortFlags = sortFlags,
                    selectedAccountCount = selectedAccountCount,
                    selectedCategoryCount = selectedCategoryCount,
                    selectedCounterPartyCount = selectedCounterPartyCount,
                    counterPartyFilterVisible = counterParties.isNotEmpty(),
                    selectedMethodCount = selectedMethodCount,
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
                    onFilterByCategoryClick = {
                        popupType = PopupType.CATEGORY
                    },
                    onFilterByCounterPartyClick = {
                        popupType = PopupType.COUNTERPARTY
                    },
                    onFilterByMethodClick = {
                        popupType = PopupType.METHOD
                    },
                    onFilterBySourceClick = {
                        popupType = PopupType.ACCOUNT
                    },
                    onCurrencyClick = {
                        popupType = PopupType.CURRENCY
                    },
                    onCalendarClick = {
                        popupType = PopupType.DATE_RANGE
                    },
                    addTransaction = {
                        if (canAddTransaction()) {
                            if (templates.isEmpty()) {
                                addTransaction(0L)
                            } else {
                                popupType = PopupType.TEMPLATES
                            }
                        }
                    },
                    onSearchClick = {
                        if (!(transactions.isEmpty() && searchText.isEmpty())) {
                            if (isFocused) {
                                keyboardController?.show()
                            } else {
                                focusRequester.requestFocus()
                            }
                        }
                    },
                    onMoreClick = {
                        popupType = PopupType.MORE_OPTIONS
                    }
                )
            }
        ) { contentPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {
                Column {
                    AnimatedVisibility(visible = !(transactions.isEmpty() && searchText.isEmpty())) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 5.dp, start = 10.dp, end = 10.dp, bottom = 10.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            OutlinedTextField(
                                value = searchText,
                                onValueChange = setSearchText,
                                enabled = true,
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
                    }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 10.dp, end = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (searchText.isBlank()) {
                            item(
                                key = "totalBalance"
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
                                    Text(
                                        text = balance,
                                        style = MaterialTheme.typography.displaySmall
                                    )
                                }
                            }
                            item(
                                key = "infoRow"
                            ) {
                                TotalTransactionInfo(
                                    expenses = totalExpense,
                                    expensesCount = expenseTransactionsCount,
                                    income = totalIncome,
                                    incomeCount = incomeTransactionsCount
                                )
                            }
                        }
                        transactions.forEach {
                            stickyHeader {
                                TransactionGroupHeader(
                                    date = it.key,
                                    amount = it.value.totalAmount
                                )
                            }
                            items(
                                items = it.value.transactions,
                                key = { transactionWithChips -> transactionWithChips.id }
                            ) { transactionWithChips ->
                                TransactionCard(
                                    transactionWithChips = transactionWithChips,
                                    onClick = { id ->
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        editTransaction(id)
                                    },
                                    onLongClick = { id ->
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedTransactionId = id
                                        popupType = PopupType.CLONE_TRANSACTION
                                    },
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }
                    }
                }
                if (loading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                    )
                } else if (searchText.isEmpty() && transactions.isEmpty()) {
                    Text(
                        text = stringResource(id = R.string.transactions_screen_empty_placeholder_label),
                        modifier = Modifier.align(
                            Alignment.Center
                        )
                    )
                }
                AnimatedVisibility(
                    visible = showToastBar,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut(),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    ToastBar(
                        message = currentEvent?.let { stringResource(id = it.message) } ?: "",
                        onDismiss = {
                            showToastBar = false
                        }
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = popupType != PopupType.NONE,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.imePadding()
        ) {
            BottomPopup(
                dismiss = {
                    popupType = PopupType.NONE
                }
            ) { hidePopup ->
                when (popupType) {
                    PopupType.CURRENCY -> {
                        CurrencyPopup(
                            index = currencies.indexOfFirst { it.currency.currencyCode == selectedCurrency },
                            selectedCurrency = selectedCurrency,
                            setSelectedCurrency = { currency ->
                                setCurrency(currency)
                            },
                            currencies = currencies,
                            dismiss = hidePopup
                        )
                    }

                    PopupType.DATE_RANGE -> {
                        DateRangePickerPopup(
                            startDate = startDate,
                            endDate = endDate,
                            filter = setStartDateAndEndDate,
                            dismiss = hidePopup
                        )
                    }

                    PopupType.MORE_OPTIONS -> {
                        MoreOptionsPopup(
                            navigateToCategoriesScreen = navigateToCategoriesScreen,
                            navigateToCounterPartyScreen = navigateToCounterPartyScreen,
                            navigateToMethodsScreen = navigateToMethodsScreen,
                            navigateToSourcesScreen = navigateToSourcesScreen,
                            navigateToTemplatesScreen = navigateToTemplatesScreen,
                            navigateToSettingsScreen = navigateToSettingsScreen,
                            openGithubReleasesPage = openGithubPage,
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
                            selectedCategoryMap = selectedCategories,
                            filter = setSelectedCategories,
                            categories = categories,
                            dismiss = hidePopup
                        )
                    }

                    PopupType.COUNTERPARTY -> {
                        FilterCounterPartyPopup(
                            selectedCounterPartyMap = selectedCounterParties,
                            filter = setSelectedCounterParties,
                            counterParties = counterParties,
                            dismiss = hidePopup
                        )
                    }

                    PopupType.METHOD -> {
                        FilterMethodPopup(
                            selectedMethodsMap = selectedMethods,
                            filter = setSelectedMethods,
                            methods = methods,
                            dismiss = hidePopup
                        )
                    }

                    PopupType.ACCOUNT -> {
                        FilterAccountPopup(
                            selectedAccountsMap = selectedAccounts,
                            filter = setSelectedAccounts,
                            accounts = accounts,
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

                    PopupType.TEMPLATES -> {
                        SelectTemplatePopup(
                            templates = templates,
                            addNewTransaction = addTransaction,
                            dismiss = hidePopup
                        )
                    }

                    PopupType.CLONE_TRANSACTION -> {
                        CloneTransactionPopup(
                            cloneTransaction = { setCurrentDateTime ->
                                cloneTransaction(selectedTransactionId, setCurrentDateTime)
                            },
                            dismiss = hidePopup
                        )
                    }

                    else -> {}
                }
            }
        }
    }
}

@Preview
@Composable
fun TransactionsScreenPreview() {
    TransactionsScreen(
        canAddTransaction = { true },
        addTransaction = {},
        editTransaction = {},
        searchState = "",
        setSearchText = {},
        setLocale = {},
        setCurrency = {},
        setStartDateAndEndDate = { _, _ -> },
        setSelectedAccounts = {},
        setSelectedCategories = {},
        setSelectedCounterParties = {},
        setSelectedMethods = {},
        setSelectedTransactionTypes = {},
        setSortFlags = {},
        filterByAmount = { _, _ -> },
        cloneTransaction = { _, _ -> },
        navigateToCategoriesScreen = {},
        navigateToCounterPartyScreen = {},
        navigateToMethodsScreen = {},
        navigateToTemplatesScreen = {},
        navigateToSourcesScreen = {},
        navigateToSettingsScreen = {},
        openGithubPage = {},
        events = emptyList<Event>().asFlow(),
        state = TransactionsScreenState(),
        filtersState = FiltersState(),
        sortOptionsState = SortOptionsState(sortField = Transaction_.time)
    )
}

fun NavGraphBuilder.transactionsScreen(navController: NavController) {
    composable(
        Routes.Transactions.route,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Companion.Left,
                animationSpec = tween(Constants.TWEEN_DURATION)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Companion.Left,
                animationSpec = tween(Constants.TWEEN_DURATION)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Companion.Right,
                animationSpec = tween(Constants.TWEEN_DURATION)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Companion.Right,
                animationSpec = tween(Constants.TWEEN_DURATION)
            )
        }
    ) {
        val uriHandler = LocalUriHandler.current
        val viewModel: TransactionsScreenViewModel = koinViewModel()
        val state by viewModel.state.collectAsState()
        val searchState by viewModel.searchState.collectAsState()
        val filtersState by viewModel.filtersState.collectAsState()
        val sortOptionsState by viewModel.sortOptionsState.collectAsState()

        TransactionsScreen(
            canAddTransaction = viewModel::canAddTransaction,
            addTransaction = { templateId ->
                navController.navigate(Routes.UpsertTransaction.getRoute(templateId = templateId)) {
                    launchSingleTop = true
                }
            },
            editTransaction = { id ->
                navController.navigate(Routes.UpsertTransaction.getRoute(id)) {
                    launchSingleTop = true
                }
            },
            searchState = searchState,
            setSearchText = viewModel::setSearchText,
            setLocale = viewModel::setLocale,
            setCurrency = viewModel::setCurrency,
            setStartDateAndEndDate = viewModel::setStartDateAndEndDate,
            setSelectedAccounts = viewModel::setSelectedAccounts,
            setSelectedCategories = viewModel::setSelectedCategories,
            setSelectedCounterParties = viewModel::setSelectedCounterParties,
            setSelectedMethods = viewModel::setSelectedMethods,
            setSelectedTransactionTypes = viewModel::setSelectedTransactionTypes,
            setSortFlags = viewModel::setSortFlags,
            filterByAmount = viewModel::setFilterAmounts,
            cloneTransaction = viewModel::cloneTransaction,
            navigateToMethodsScreen = {
                navController.navigate(Routes.Methods.route) {
                    launchSingleTop = true
                }
            },
            navigateToSourcesScreen = {
                navController.navigate(Routes.Accounts.route) {
                    launchSingleTop = true
                }
            },
            navigateToCategoriesScreen = {
                navController.navigate(Routes.Categories.route) {
                    launchSingleTop = true
                }
            },
            navigateToCounterPartyScreen = {
                navController.navigate(Routes.CounterParties.route) {
                    launchSingleTop = true
                }
            },
            navigateToTemplatesScreen = {
                navController.navigate(Routes.Templates.route) {
                    launchSingleTop = true
                }
            },
            navigateToSettingsScreen = {
                navController.navigate(Routes.Settings.route) {
                    launchSingleTop = true
                }
            },
            openGithubPage = {
                uriHandler.openUri("https://github.com/ThomasGRG/CashFlow/releases")
            },
            events = viewModel.event,
            state = state,
            filtersState = filtersState,
            sortOptionsState = sortOptionsState
        )
    }
}