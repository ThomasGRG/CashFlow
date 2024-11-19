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
import io.realm.kotlin.query.Sort
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.data.enums.PopupType
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
import jp.ikigai.cash.flow.ui.components.popups.FilterCategoryPopup
import jp.ikigai.cash.flow.ui.components.popups.FilterCounterPartyPopup
import jp.ikigai.cash.flow.ui.components.popups.FilterItemsPopup
import jp.ikigai.cash.flow.ui.components.popups.FilterMethodPopup
import jp.ikigai.cash.flow.ui.components.popups.FilterSourcePopup
import jp.ikigai.cash.flow.ui.components.popups.FilterTransactionTypePopup
import jp.ikigai.cash.flow.ui.components.popups.MoreOptionsPopup
import jp.ikigai.cash.flow.ui.components.popups.SelectTemplatePopup
import jp.ikigai.cash.flow.ui.screenStates.listing.TransactionsScreenState
import jp.ikigai.cash.flow.ui.viewmodels.listing.TransactionsScreenViewModel
import jp.ikigai.cash.flow.utils.getNumberFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransactionsScreen(
    canAddTransaction: () -> Boolean,
    addTransaction: (String) -> Unit,
    editTransaction: (String) -> Unit,
    setSearchText: (String) -> Unit,
    setLocale: (Locale?) -> Unit,
    setCurrency: (String) -> Unit,
    setStartDateAndEndDate: (LocalDate, LocalDate) -> Unit,
    setSelectedCategories: (Map<String, Boolean>) -> Unit,
    setSelectedCounterParties: (Boolean, Map<String, Boolean>) -> Unit,
    setSelectedMethods: (Map<String, Boolean>) -> Unit,
    setSelectedSources: (Map<String, Boolean>) -> Unit,
    setSelectedItems: (Boolean, Map<String, Boolean>) -> Unit,
    setSelectedTransactionTypes: (List<Int>) -> Unit,
    setSortDirection: (Sort) -> Unit,
    filterByAmount: (Double, Double) -> Unit,
    cloneTransaction: (String, Boolean) -> Unit,
    navigateToCategoriesScreen: () -> Unit,
    navigateToCounterPartyScreen: () -> Unit,
    navigateToMethodsScreen: () -> Unit,
    navigateToTemplatesScreen: () -> Unit,
    navigateToItemsScreen: () -> Unit,
    navigateToSourcesScreen: () -> Unit,
    openGithubPage: () -> Unit,
    events: Flow<Event>,
    state: TransactionsScreenState,
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

    val numberFormatter by remember {
        mutableStateOf(getNumberFormatter())
    }

    val searchText by remember(key1 = state.searchText) {
        mutableStateOf(state.searchText)
    }

    val loading by remember(key1 = state.loading) {
        mutableStateOf(state.loading)
    }

    val transactions by remember(key1 = state.transactions) {
        mutableStateOf(state.transactions)
    }

    val templates by remember(key1 = state.templates) {
        mutableStateOf(state.templates)
    }

    val currencies by remember(key1 = state.currencies) {
        mutableStateOf(state.currencies)
    }

    val balance by remember(key1 = state.balance) {
        mutableStateOf(numberFormatter.format(state.balance).toString())
    }

    val selectedCurrency by remember(key1 = state.selectedCurrency) {
        mutableStateOf(state.selectedCurrency)
    }

    val selectedCurrencySymbol by remember(key1 = state.selectedCurrency) {
        mutableStateOf(Currency.getInstance(state.selectedCurrency).symbol)
    }

    val startDate by remember(key1 = state.startDate) {
        mutableStateOf(state.startDate)
    }

    val startDateString by remember(key1 = state.startDateString) {
        mutableStateOf(state.startDateString)
    }

    val endDate by remember(key1 = state.endDate) {
        mutableStateOf(state.endDate)
    }

    val endDateString by remember(key1 = state.endDateString) {
        mutableStateOf(state.endDateString)
    }

    val totalExpense by remember(key1 = state.expense) {
        mutableDoubleStateOf(state.expense)
    }

    val expenseTransactionsCount by remember(key1 = state.expenseTransactionsCount) {
        mutableIntStateOf(state.expenseTransactionsCount)
    }

    val totalIncome by remember(key1 = state.income) {
        mutableDoubleStateOf(state.income)
    }

    val incomeTransactionsCount by remember(key1 = state.incomeTransactionsCount) {
        mutableIntStateOf(state.incomeTransactionsCount)
    }

    val categories by remember(key1 = state.categories) {
        mutableStateOf(state.categories)
    }

    val selectedCategories by remember(key1 = state.selectedCategories) {
        mutableStateOf(state.selectedCategories)
    }

    val selectedCategoryCount by remember(key1 = state.selectedCategoryCount) {
        mutableIntStateOf(state.selectedCategoryCount)
    }

    val counterParties by remember(key1 = state.counterParties) {
        mutableStateOf(state.counterParties)
    }

    val includeNoCounterPartyTransactions by remember(key1 = state.includeNoCounterPartyTransactions) {
        mutableStateOf(state.includeNoCounterPartyTransactions)
    }

    val selectedCounterParties by remember(key1 = state.selectedCounterParties) {
        mutableStateOf(state.selectedCounterParties)
    }

    val selectedCounterPartyCount by remember(key1 = state.selectedCounterPartyCount) {
        mutableIntStateOf(state.selectedCounterPartyCount)
    }

    val methods by remember(key1 = state.methods) {
        mutableStateOf(state.methods)
    }

    val selectedMethods by remember(key1 = state.selectedMethods) {
        mutableStateOf(state.selectedMethods)
    }

    val selectedMethodCount by remember(key1 = state.selectedMethodCount) {
        mutableIntStateOf(state.selectedMethodCount)
    }

    val sources by remember(key1 = state.sources) {
        mutableStateOf(state.sources)
    }

    val selectedSources by remember(key1 = state.selectedSources) {
        mutableStateOf(state.selectedSources)
    }

    val selectedSourceCount by remember(key1 = state.selectedSourceCount) {
        mutableIntStateOf(state.selectedSourceCount)
    }

    val items by remember(key1 = state.items) {
        mutableStateOf(state.items)
    }

    val includeNoItemTransactions by remember(key1 = state.includeNoItemTransactions) {
        mutableStateOf(state.includeNoItemTransactions)
    }

    val selectedItems by remember(key1 = state.selectedItems) {
        mutableStateOf(state.selectedItems)
    }

    val selectedItemCount by remember(key1 = state.selectedItemCount) {
        mutableIntStateOf(state.selectedItemCount)
    }

    val selectedTransactionTypes by remember(key1 = state.selectedTransactionTypes) {
        mutableStateOf(state.selectedTransactionTypes)
    }

    val filterAmountMin by remember(key1 = state.filterAmountMin) {
        mutableDoubleStateOf(state.filterAmountMin)
    }

    val filterAmountMax by remember(key1 = state.filterAmountMax) {
        mutableDoubleStateOf(state.filterAmountMax)
    }

    val filterAmountRange by remember(key1 = state.filterAmountRange) {
        mutableStateOf(state.filterAmountRange)
    }

    val sortDirection by remember(key1 = state.sortDirection) {
        mutableStateOf(state.sortDirection)
    }

    var selectedTransactionUUID by remember {
        mutableStateOf("")
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
                                text = "$startDateString to $endDateString",
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
                    sortDirection = sortDirection,
                    selectedCategoryCount = selectedCategoryCount,
                    selectedCounterPartyCount = selectedCounterPartyCount,
                    selectedMethodCount = selectedMethodCount,
                    selectedSourceCount = selectedSourceCount,
                    selectedItemCount = selectedItemCount,
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
                        popupType = PopupType.SOURCE
                    },
                    onFilterByItemClick = {
                        popupType = PopupType.FILTER_ITEMS
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
                                addTransaction("")
                            } else {
                                popupType = PopupType.TEMPLATES
                            }
                        }
                    },
                    onSearchClick = {
                        if (isFocused) {
                            keyboardController?.show()
                        } else {
                            focusRequester.requestFocus()
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
                                        text = "$balance $selectedCurrency",
                                        style = MaterialTheme.typography.displaySmall
                                    )
                                }
                            }
                            item(
                                key = "infoRow"
                            ) {
                                TotalTransactionInfo(
                                    currency = selectedCurrency,
                                    expenses = numberFormatter.format(totalExpense).toString(),
                                    expensesCount = numberFormatter.format(expenseTransactionsCount)
                                        .toString(),
                                    income = numberFormatter.format(totalIncome).toString(),
                                    incomeCount = numberFormatter.format(incomeTransactionsCount)
                                        .toString()
                                )
                            }
                        }
                        transactions.forEach {
                            stickyHeader {
                                TransactionGroupHeader(
                                    date = it.key,
                                    amount = numberFormatter.format(it.value.totalAmount)
                                        .toString(),
                                    currency = selectedCurrency
                                )
                            }
                            items(
                                items = it.value.transactions,
                                key = { transactionWithIcons -> transactionWithIcons.uuid }
                            ) { transactionWithIcons ->
                                TransactionCard(
                                    transactionWithIcons = transactionWithIcons,
                                    onClick = {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        editTransaction(transactionWithIcons.uuid)
                                    },
                                    onLongClick = {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedTransactionUUID = transactionWithIcons.uuid
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
                            dismiss = {
                                hidePopup()
                                popupType = PopupType.NONE
                            }
                        )
                    }

                    PopupType.DATE_RANGE -> {
                        DateRangePickerPopup(
                            startDate = startDate,
                            endDate = endDate,
                            filter = setStartDateAndEndDate,
                            dismiss = {
                                hidePopup()
                                popupType = PopupType.NONE
                            }
                        )
                    }

                    PopupType.MORE_OPTIONS -> {
                        MoreOptionsPopup(
                            navigateToCategoriesScreen = navigateToCategoriesScreen,
                            navigateToCounterPartyScreen = navigateToCounterPartyScreen,
                            navigateToMethodsScreen = navigateToMethodsScreen,
                            navigateToSourcesScreen = navigateToSourcesScreen,
                            navigateToTemplatesScreen = navigateToTemplatesScreen,
                            navigateToItemsScreen = navigateToItemsScreen,
                            openGithubReleasesPage = openGithubPage,
                            dismiss = {
                                hidePopup()
                                popupType = PopupType.NONE
                            }
                        )
                    }

                    PopupType.AMOUNT -> {
                        AmountFilterPopup(
                            minAmount = filterAmountMin,
                            maxAmount = filterAmountMax,
                            filter = filterByAmount,
                            dismiss = {
                                hidePopup()
                                popupType = PopupType.NONE
                            }
                        )
                    }

                    PopupType.CATEGORY -> {
                        FilterCategoryPopup(
                            selectedCategoryMap = selectedCategories,
                            filter = setSelectedCategories,
                            categories = categories,
                            dismiss = {
                                hidePopup()
                                popupType = PopupType.NONE
                            }
                        )
                    }

                    PopupType.COUNTERPARTY -> {
                        FilterCounterPartyPopup(
                            includeTransactionsWithNoCounterParty = includeNoCounterPartyTransactions,
                            selectedCounterPartyMap = selectedCounterParties,
                            filter = setSelectedCounterParties,
                            counterParties = counterParties,
                            dismiss = {
                                hidePopup()
                                popupType = PopupType.NONE
                            }
                        )
                    }

                    PopupType.METHOD -> {
                        FilterMethodPopup(
                            selectedMethodsMap = selectedMethods,
                            filter = setSelectedMethods,
                            methods = methods,
                            dismiss = {
                                hidePopup()
                                popupType = PopupType.NONE
                            }
                        )
                    }

                    PopupType.SOURCE -> {
                        FilterSourcePopup(
                            selectedSourcesMap = selectedSources,
                            filter = setSelectedSources,
                            sources = sources,
                            dismiss = {
                                hidePopup()
                                popupType = PopupType.NONE
                            }
                        )
                    }

                    PopupType.FILTER_ITEMS -> {
                        FilterItemsPopup(
                            includeTransactionsWithNoItems = includeNoItemTransactions,
                            selectedItemsMap = selectedItems,
                            filterItems = setSelectedItems,
                            items = items,
                            dismiss = {
                                hidePopup()
                                popupType = PopupType.NONE
                            }
                        )
                    }

                    PopupType.TYPE -> {
                        FilterTransactionTypePopup(
                            selectedTransactionTypes = selectedTransactionTypes,
                            filter = setSelectedTransactionTypes,
                            dismiss = {
                                hidePopup()
                                popupType = PopupType.NONE
                            }
                        )
                    }

                    PopupType.TEMPLATES -> {
                        SelectTemplatePopup(
                            templates = templates,
                            addNewTransaction = addTransaction,
                            dismiss = {
                                hidePopup()
                                popupType = PopupType.NONE
                            }
                        )
                    }

                    PopupType.CLONE_TRANSACTION -> {
                        CloneTransactionPopup(
                            cloneTransaction = { setCurrentDateTime ->
                                cloneTransaction(selectedTransactionUUID, setCurrentDateTime)
                            },
                            dismiss = {
                                hidePopup()
                                popupType = PopupType.NONE
                            }
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
        setSearchText = {},
        setLocale = {},
        setCurrency = {},
        setStartDateAndEndDate = { _, _ -> },
        setSelectedCategories = {},
        setSelectedCounterParties = { _, _ -> },
        setSelectedMethods = {},
        setSelectedSources = {},
        setSelectedItems = { _, _ -> },
        setSelectedTransactionTypes = {},
        setSortDirection = {},
        filterByAmount = { _, _ -> },
        cloneTransaction = { _, _ -> },
        navigateToCategoriesScreen = {},
        navigateToCounterPartyScreen = {},
        navigateToMethodsScreen = {},
        navigateToTemplatesScreen = {},
        navigateToItemsScreen = {},
        navigateToSourcesScreen = {},
        openGithubPage = {},
        events = emptyList<Event>().asFlow(),
        state = TransactionsScreenState()
    )
}

fun NavGraphBuilder.transactionsScreen(navController: NavController) {
    composable(
        Routes.Transactions.route,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Companion.Left,
                animationSpec = tween(Constants.tweenDuration)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Companion.Left,
                animationSpec = tween(Constants.tweenDuration)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Companion.Right,
                animationSpec = tween(Constants.tweenDuration)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Companion.Right,
                animationSpec = tween(Constants.tweenDuration)
            )
        }
    ) {
        val uriHandler = LocalUriHandler.current
        val viewModel: TransactionsScreenViewModel = koinViewModel()
        val state by viewModel.state.collectAsState()

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
            setSearchText = viewModel::setSearchText,
            setLocale = viewModel::setLocale,
            setCurrency = viewModel::setCurrency,
            setStartDateAndEndDate = viewModel::setStartDateAndEndDate,
            setSelectedCategories = viewModel::setSelectedCategories,
            setSelectedCounterParties = viewModel::setSelectedCounterParties,
            setSelectedMethods = viewModel::setSelectedMethods,
            setSelectedSources = viewModel::setSelectedSources,
            setSelectedItems = viewModel::setSelectedItems,
            setSelectedTransactionTypes = viewModel::setSelectedTransactionTypes,
            setSortDirection = viewModel::setSortDirection,
            filterByAmount = viewModel::setFilterAmounts,
            cloneTransaction = viewModel::cloneTransaction,
            navigateToMethodsScreen = {
                navController.navigate(Routes.Methods.route) {
                    launchSingleTop = true
                }
            },
            navigateToSourcesScreen = {
                navController.navigate(Routes.Sources.route) {
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
            navigateToItemsScreen = {
                navController.navigate(Routes.Items.route) {
                    launchSingleTop = true
                }
            },
            openGithubPage = {
                uriHandler.openUri("https://github.com/ThomasGRG/CashFlow/releases")
            },
            events = viewModel.event,
            state = state
        )
    }
}