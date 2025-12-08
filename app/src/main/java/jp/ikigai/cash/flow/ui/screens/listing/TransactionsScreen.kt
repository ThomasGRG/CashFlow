package jp.ikigai.cash.flow.ui.screens.listing

import android.icu.util.Currency
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.data.TransactionHeader
import jp.ikigai.cash.flow.data.enums.SheetType
import jp.ikigai.cash.flow.data.enums.SortDirection
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.ui.components.bottombars.TransactionScreenBottomAppBar
import jp.ikigai.cash.flow.ui.components.bottomsheets.AmountFilterSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.CloneTransactionSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.CurrencySheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.DateRangePickerSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.FilterAccountSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.FilterCategorySheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.FilterCounterPartySheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.FilterMethodSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.FilterTransactionTypeSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.MoreOptionsSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.SelectTemplateSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.SortConfigSheet
import jp.ikigai.cash.flow.ui.components.cards.TransactionCard
import jp.ikigai.cash.flow.ui.components.common.BalanceCard
import jp.ikigai.cash.flow.ui.components.common.LandscapeScaffold
import jp.ikigai.cash.flow.ui.components.common.SearchBox
import jp.ikigai.cash.flow.ui.components.common.ToastBar
import jp.ikigai.cash.flow.ui.components.common.TotalTransactionInfo
import jp.ikigai.cash.flow.ui.components.common.TransactionGroupHeader
import jp.ikigai.cash.flow.ui.screenStates.common.SortConfigState
import jp.ikigai.cash.flow.ui.screenStates.listing.transactions.FiltersState
import jp.ikigai.cash.flow.ui.screenStates.listing.transactions.TransactionsScreenState
import jp.ikigai.cash.flow.ui.viewmodels.listing.TransactionsScreenViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.ZonedDateTime
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
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
    setSelectedCounterParties: (Map<Long, Boolean>, Boolean) -> Unit,
    setSelectedMethods: (Map<Long, Boolean>) -> Unit,
    setSelectedTransactionTypes: (List<TransactionType>) -> Unit,
    setSortConfig: (String, SortDirection) -> Unit,
    filterByAmount: (Double, Double) -> Unit,
    cloneTransaction: (Long, Boolean) -> Unit,
    navigateToAccountsScreen: () -> Unit,
    navigateToCategoriesScreen: () -> Unit,
    navigateToCounterPartyScreen: () -> Unit,
    navigateToMethodsScreen: () -> Unit,
    navigateToTemplatesScreen: () -> Unit,
    navigateToSettingsScreen: () -> Unit,
    openGithubPage: () -> Unit,
    events: Flow<Event>,
    state: TransactionsScreenState,
    filtersState: FiltersState,
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

    val includeNoCounterPartyTransactions by remember(key1 = filtersState.includeNoCounterPartyTransactions) {
        mutableStateOf(filtersState.includeNoCounterPartyTransactions)
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

    val sortFields = mapOf(
        stringResource(id = R.string.amount_label) to "transactionAmount",
        stringResource(id = R.string.time_field_label) to "transactionDateTime",
    )

    val sortField by remember(key1 = sortConfigState.sortField) {
        mutableStateOf(sortConfigState.sortField)
    }

    val sortedBy by remember(key1 = sortField) {
        mutableStateOf(
            sortFields.entries.find { it.value == sortField }?.key
        )
    }

    val sortDirection by remember(key1 = sortConfigState.sortDirection) {
        mutableStateOf(sortConfigState.sortDirection)
    }

    var selectedTransactionId by remember {
        mutableLongStateOf(0L)
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
            },
            showEmptyPlaceholder = searchText.isEmpty() && transactions.isEmpty(),
            emptyPlaceholderText = stringResource(id = R.string.transactions_screen_empty_placeholder_label),
            sheetState = sheetState,
            showBottomSheet = sheetType != SheetType.NONE,
            bottomSheetContent = {
                when (sheetType) {
                    SheetType.CURRENCY -> {
                        CurrencySheet(
                            index = currencies.indexOfFirst { it.currency.currencyCode == selectedCurrency },
                            selectedCurrency = selectedCurrency,
                            setSelectedCurrency = { currency ->
                                setCurrency(currency)
                            },
                            currencies = currencies,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion {
                                        sheetType = SheetType.NONE
                                    }
                            }
                        )
                    }

                    SheetType.DATE_RANGE -> {
                        DateRangePickerSheet(
                            startDate = startDate,
                            endDate = endDate,
                            filter = setStartDateAndEndDate,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion {
                                        sheetType = SheetType.NONE
                                    }
                            }
                        )
                    }

                    SheetType.MORE_OPTIONS -> {
                        MoreOptionsSheet(
                            navigateToCategoriesScreen = navigateToCategoriesScreen,
                            navigateToCounterPartyScreen = navigateToCounterPartyScreen,
                            navigateToMethodsScreen = navigateToMethodsScreen,
                            navigateToSourcesScreen = navigateToAccountsScreen,
                            navigateToTemplatesScreen = navigateToTemplatesScreen,
                            navigateToSettingsScreen = navigateToSettingsScreen,
                            openGithubReleasesPage = openGithubPage,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion {
                                        sheetType = SheetType.NONE
                                    }
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
                                    .invokeOnCompletion {
                                        sheetType = SheetType.NONE
                                    }
                            }
                        )
                    }

                    SheetType.CATEGORY -> {
                        FilterCategorySheet(
                            selectedCategoryMap = selectedCategories,
                            filter = setSelectedCategories,
                            categories = categories,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion {
                                        sheetType = SheetType.NONE
                                    }
                            }
                        )
                    }

                    SheetType.COUNTERPARTY -> {
                        FilterCounterPartySheet(
                            selectedCounterPartyMap = selectedCounterParties,
                            includeNoCounterPartyTransactions = includeNoCounterPartyTransactions,
                            counterParties = counterParties,
                            filter = setSelectedCounterParties,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion {
                                        sheetType = SheetType.NONE
                                    }
                            }
                        )
                    }

                    SheetType.METHOD -> {
                        FilterMethodSheet(
                            selectedMethodsMap = selectedMethods,
                            filter = setSelectedMethods,
                            methods = methods,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion {
                                        sheetType = SheetType.NONE
                                    }
                            }
                        )
                    }

                    SheetType.ACCOUNT -> {
                        FilterAccountSheet(
                            selectedAccountsMap = selectedAccounts,
                            filter = setSelectedAccounts,
                            accounts = accounts,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion {
                                        sheetType = SheetType.NONE
                                    }
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
                                    .invokeOnCompletion {
                                        sheetType = SheetType.NONE
                                    }
                            }
                        )
                    }

                    SheetType.TEMPLATES -> {
                        SelectTemplateSheet(
                            templates = templates,
                            addNewTransaction = addTransaction,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion {
                                        sheetType = SheetType.NONE
                                    }
                            }
                        )
                    }

                    SheetType.CLONE_TRANSACTION -> {
                        CloneTransactionSheet(
                            cloneTransaction = { setCurrentDateTime ->
                                cloneTransaction(selectedTransactionId, setCurrentDateTime)
                            },
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion {
                                        sheetType = SheetType.NONE
                                    }
                            }
                        )
                    }

                    SheetType.SORT -> {
                        SortConfigSheet(
                            selectedField = sortField,
                            selectedDirection = sortDirection,
                            fields = sortFields,
                            sort = setSortConfig,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion {
                                        sheetType = SheetType.NONE
                                    }
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
                    AnimatedVisibility(visible = !(transactions.isEmpty() && searchText.isEmpty())) {
                        SearchBox(
                            modifier = Modifier.padding(bottom = 10.dp),
                            searchText = searchText,
                            setSearchText = setSearchText,
                            focusRequester = focusRequester,
                            interactionSource = interactionSource
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TransactionScreenBottomAppBar(
                        title = stringResource(id = R.string.transactions_label),
                        subTitle = stringResource(
                            id = R.string.date_range_label,
                            startDateString,
                            endDateString
                        ),
                        selectedCurrencySymbol = selectedCurrencySymbol,
                        filterAmount = filterAmountRange,
                        sortField = sortedBy,
                        sortDirection = sortDirection,
                        selectedAccountCount = selectedAccountCount,
                        selectedCategoryCount = selectedCategoryCount,
                        selectedCounterPartyCount = selectedCounterPartyCount,
                        counterPartyFilterVisible = counterParties.isNotEmpty(),
                        selectedMethodCount = selectedMethodCount,
                        selectedTransactionTypeCount = selectedTransactionTypes.size,
                        addTransaction = {
                            if (canAddTransaction()) {
                                if (templates.isEmpty()) {
                                    addTransaction(0L)
                                } else {
                                    sheetType = SheetType.TEMPLATES
                                }
                            }
                        },
                        setSheetType = {
                            sheetType = it
                        },
                    )
                }
            },
            secondColContent = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (searchText.isBlank()) {
                        item(
                            key = "totalBalance"
                        ) {
                            BalanceCard(
                                modifier = Modifier.padding(top = 8.dp),
                                balance = balance
                            )
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
                    transactions.forEach { entry ->
                        when (val header = entry.key) {
                            is TransactionHeader.DateHeader -> {
                                stickyHeader(
                                    key = header.date,
                                    contentType = "sticky_date_header"
                                ) {
                                    TransactionGroupHeader(
                                        date = header.date,
                                        amount = header.formattedAmount
                                    )
                                }
                            }

                            is TransactionHeader.AmountHeader -> {
                                stickyHeader(
                                    key = header.formattedAmountRange,
                                    contentType = "sticky_amount_header"
                                ) {
                                    TransactionGroupHeader(
                                        amountRange = header.formattedAmountRange
                                    )
                                }
                            }
                        }
                        items(
                            items = entry.value,
                            key = { transactionWithChips -> transactionWithChips.id },
                            contentType = { "transaction_item" }
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
                                    sheetType = SheetType.CLONE_TRANSACTION
                                },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        )
    } else {
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
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                )
            },
            bottomBar = {
                TransactionScreenBottomAppBar(
                    selectedCurrencySymbol = selectedCurrencySymbol,
                    filterAmount = filterAmountRange,
                    sortField = sortedBy,
                    sortDirection = sortDirection,
                    selectedAccountCount = selectedAccountCount,
                    selectedCategoryCount = selectedCategoryCount,
                    selectedCounterPartyCount = selectedCounterPartyCount,
                    counterPartyFilterVisible = counterParties.isNotEmpty(),
                    selectedMethodCount = selectedMethodCount,
                    selectedTransactionTypeCount = selectedTransactionTypes.size,
                    addTransaction = {
                        if (canAddTransaction()) {
                            if (templates.isEmpty()) {
                                addTransaction(0L)
                            } else {
                                sheetType = SheetType.TEMPLATES
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
                    setSheetType = {
                        sheetType = it
                    },
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
                        SearchBox(
                            modifier = Modifier
                                .padding(top = 5.dp, start = 10.dp, end = 10.dp),
                            searchText = searchText,
                            setSearchText = setSearchText,
                            focusRequester = focusRequester,
                            interactionSource = interactionSource
                        )
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
                                BalanceCard(balance = balance)
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
                        transactions.forEach { entry ->
                            when (val header = entry.key) {
                                is TransactionHeader.DateHeader -> {
                                    stickyHeader(
                                        key = header.date,
                                        contentType = "sticky_date_header"
                                    ) {
                                        TransactionGroupHeader(
                                            date = header.date,
                                            amount = header.formattedAmount
                                        )
                                    }
                                }

                                is TransactionHeader.AmountHeader -> {
                                    stickyHeader(
                                        key = header.formattedAmountRange,
                                        contentType = "sticky_amount_header"
                                    ) {
                                        TransactionGroupHeader(
                                            amountRange = header.formattedAmountRange
                                        )
                                    }
                                }
                            }
                            items(
                                items = entry.value,
                                key = { transactionWithChips -> transactionWithChips.id },
                                contentType = { "transaction_item" }
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
                                        sheetType = SheetType.CLONE_TRANSACTION
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
            if (sheetType != SheetType.NONE) {
                ModalBottomSheet(
                    onDismissRequest = {
                        sheetType = SheetType.NONE
                    },
                    sheetState = sheetState,
//                sheetGesturesEnabled: Boolean = false, TODO()
                ) {
                    when (sheetType) {
                        SheetType.CURRENCY -> {
                            CurrencySheet(
                                index = currencies.indexOfFirst { it.currency.currencyCode == selectedCurrency },
                                selectedCurrency = selectedCurrency,
                                setSelectedCurrency = { currency ->
                                    setCurrency(currency)
                                },
                                currencies = currencies,
                                dismiss = {
                                    scope
                                        .launch { sheetState.hide() }
                                        .invokeOnCompletion {
                                            sheetType = SheetType.NONE
                                        }
                                }
                            )
                        }

                        SheetType.DATE_RANGE -> {
                            DateRangePickerSheet(
                                startDate = startDate,
                                endDate = endDate,
                                filter = setStartDateAndEndDate,
                                dismiss = {
                                    scope
                                        .launch { sheetState.hide() }
                                        .invokeOnCompletion {
                                            sheetType = SheetType.NONE
                                        }
                                }
                            )
                        }

                        SheetType.MORE_OPTIONS -> {
                            MoreOptionsSheet(
                                navigateToCategoriesScreen = navigateToCategoriesScreen,
                                navigateToCounterPartyScreen = navigateToCounterPartyScreen,
                                navigateToMethodsScreen = navigateToMethodsScreen,
                                navigateToSourcesScreen = navigateToAccountsScreen,
                                navigateToTemplatesScreen = navigateToTemplatesScreen,
                                navigateToSettingsScreen = navigateToSettingsScreen,
                                openGithubReleasesPage = openGithubPage,
                                dismiss = {
                                    scope
                                        .launch { sheetState.hide() }
                                        .invokeOnCompletion {
                                            sheetType = SheetType.NONE
                                        }
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
                                        .invokeOnCompletion {
                                            sheetType = SheetType.NONE
                                        }
                                }
                            )
                        }

                        SheetType.CATEGORY -> {
                            FilterCategorySheet(
                                selectedCategoryMap = selectedCategories,
                                filter = setSelectedCategories,
                                categories = categories,
                                dismiss = {
                                    scope
                                        .launch { sheetState.hide() }
                                        .invokeOnCompletion {
                                            sheetType = SheetType.NONE
                                        }
                                }
                            )
                        }

                        SheetType.COUNTERPARTY -> {
                            FilterCounterPartySheet(
                                selectedCounterPartyMap = selectedCounterParties,
                                includeNoCounterPartyTransactions = includeNoCounterPartyTransactions,
                                counterParties = counterParties,
                                filter = setSelectedCounterParties,
                                dismiss = {
                                    scope
                                        .launch { sheetState.hide() }
                                        .invokeOnCompletion {
                                            sheetType = SheetType.NONE
                                        }
                                }
                            )
                        }

                        SheetType.METHOD -> {
                            FilterMethodSheet(
                                selectedMethodsMap = selectedMethods,
                                filter = setSelectedMethods,
                                methods = methods,
                                dismiss = {
                                    scope
                                        .launch { sheetState.hide() }
                                        .invokeOnCompletion {
                                            sheetType = SheetType.NONE
                                        }
                                }
                            )
                        }

                        SheetType.ACCOUNT -> {
                            FilterAccountSheet(
                                selectedAccountsMap = selectedAccounts,
                                filter = setSelectedAccounts,
                                accounts = accounts,
                                dismiss = {
                                    scope
                                        .launch { sheetState.hide() }
                                        .invokeOnCompletion {
                                            sheetType = SheetType.NONE
                                        }
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
                                        .invokeOnCompletion {
                                            sheetType = SheetType.NONE
                                        }
                                }
                            )
                        }

                        SheetType.TEMPLATES -> {
                            SelectTemplateSheet(
                                templates = templates,
                                addNewTransaction = addTransaction,
                                dismiss = {
                                    scope
                                        .launch { sheetState.hide() }
                                        .invokeOnCompletion {
                                            sheetType = SheetType.NONE
                                        }
                                }
                            )
                        }

                        SheetType.CLONE_TRANSACTION -> {
                            CloneTransactionSheet(
                                cloneTransaction = { setCurrentDateTime ->
                                    cloneTransaction(selectedTransactionId, setCurrentDateTime)
                                },
                                dismiss = {
                                    scope
                                        .launch { sheetState.hide() }
                                        .invokeOnCompletion {
                                            sheetType = SheetType.NONE
                                        }
                                }
                            )
                        }

                        SheetType.SORT -> {
                            SortConfigSheet(
                                selectedField = sortField,
                                selectedDirection = sortDirection,
                                fields = sortFields,
                                sort = setSortConfig,
                                dismiss = {
                                    scope
                                        .launch { sheetState.hide() }
                                        .invokeOnCompletion {
                                            sheetType = SheetType.NONE
                                        }
                                }
                            )
                        }

                        else -> {}
                    }
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
        setSelectedCounterParties = { _, _ -> },
        setSelectedMethods = {},
        setSelectedTransactionTypes = {},
        setSortConfig = { _, _ -> },
        filterByAmount = { _, _ -> },
        cloneTransaction = { _, _ -> },
        navigateToAccountsScreen = {},
        navigateToCategoriesScreen = {},
        navigateToCounterPartyScreen = {},
        navigateToMethodsScreen = {},
        navigateToTemplatesScreen = {},
        navigateToSettingsScreen = {},
        openGithubPage = {},
        events = emptyList<Event>().asFlow(),
        state = TransactionsScreenState(),
        filtersState = FiltersState(),
        sortConfigState = SortConfigState(sortField = "transactionDateTime")
    )
}

fun NavGraphBuilder.transactionsScreen(navController: NavController) {
    composable(
        Routes.Transactions.route
    ) {
        val uriHandler = LocalUriHandler.current
        val viewModel: TransactionsScreenViewModel = koinViewModel()
        val state by viewModel.state.collectAsState()
        val searchState by viewModel.searchState.collectAsState()
        val filtersState by viewModel.filtersState.collectAsState()
        val sortConfigState by viewModel.sortConfigState.collectAsState()

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
            setSortConfig = viewModel::setSortConfig,
            filterByAmount = viewModel::setFilterAmounts,
            cloneTransaction = viewModel::cloneTransaction,
            navigateToMethodsScreen = {
                navController.navigate(Routes.Methods.route) {
                    launchSingleTop = true
                }
            },
            navigateToAccountsScreen = {
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
            sortConfigState = sortConfigState
        )
    }
}