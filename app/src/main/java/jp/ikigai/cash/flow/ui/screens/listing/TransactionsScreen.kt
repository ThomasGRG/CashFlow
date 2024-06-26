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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.data.dto.Filters
import jp.ikigai.cash.flow.data.enums.PopupType
import jp.ikigai.cash.flow.ui.components.bottombars.TransactionScreenRoundedBottomBar
import jp.ikigai.cash.flow.ui.components.cards.TransactionCard
import jp.ikigai.cash.flow.ui.components.common.BottomPopup
import jp.ikigai.cash.flow.ui.components.common.ToastBar
import jp.ikigai.cash.flow.ui.components.common.TotalTransactionInfo
import jp.ikigai.cash.flow.ui.components.common.TransactionGroupHeader
import jp.ikigai.cash.flow.ui.components.popups.CloneTransactionPopup
import jp.ikigai.cash.flow.ui.components.popups.CurrencyPopup
import jp.ikigai.cash.flow.ui.components.popups.DateRangePickerPopup
import jp.ikigai.cash.flow.ui.components.popups.FilterPopup
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransactionsScreen(
    canAddTransaction: () -> Boolean,
    addTransaction: (String) -> Unit,
    editTransaction: (String) -> Unit,
    setFilters: (Filters) -> Unit,
    setCurrency: (String) -> Unit,
    setStartDateAndEndDate: (LocalDate, LocalDate) -> Unit,
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
    val haptics = LocalHapticFeedback.current

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

    val filters by remember(key1 = state.filters) {
        mutableStateOf(state.filters)
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
                        Column(
                            modifier = Modifier.padding(5.dp)
                        ) {
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
                    onFilterClick = {
                        popupType = PopupType.FILTER
                    },
                    onMoreClick = {
                        popupType = PopupType.MORE_OPTIONS
                    },
                )
            }
        ) { contentPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item(
                        key = "totalBalance"
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
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
                            incomeCount = numberFormatter.format(incomeTransactionsCount).toString()
                        )
                    }
                    transactions.forEach {
                        stickyHeader {
                            TransactionGroupHeader(
                                date = it.key,
                                amount = numberFormatter.format(it.value.totalAmount).toString(),
                                currency = selectedCurrency
                            )
                        }
                        items(
                            items = it.value.transactions,
                            key = { transactionWithIcons -> transactionWithIcons.uuid }
                        ) { transactionWithIcons ->
                            TransactionCard(
                                transactionWithIcons = transactionWithIcons,
                                amount = numberFormatter.format(transactionWithIcons.amount)
                                    .toString(),
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    editTransaction(transactionWithIcons.uuid)
                                },
                                onLongClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedTransactionUUID = transactionWithIcons.uuid
                                    popupType = PopupType.CLONE_TRANSACTION
                                },
                                modifier = Modifier.animateItemPlacement()
                            )
                        }
                    }
                }
                if (loading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                    )
                } else if (transactions.isEmpty()) {
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
                            index = currencies.indexOfFirst { it.currencyCode == selectedCurrency },
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

                    PopupType.FILTER -> {
                        FilterPopup(
                            filters = filters,
                            setFilters = setFilters,
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
        setFilters = {},
        setCurrency = {},
        setStartDateAndEndDate = { _, _ -> },
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
            setFilters = viewModel::setFilters,
            setCurrency = viewModel::setCurrency,
            setStartDateAndEndDate = viewModel::setStartDateAndEndDate,
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