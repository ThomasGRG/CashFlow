package jp.ikigai.cash.flow.ui.screens.listing

import android.icu.util.Currency
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.data.dto.Filters
import jp.ikigai.cash.flow.data.enums.SheetType
import jp.ikigai.cash.flow.data.screenStates.listing.TransactionsScreenState
import jp.ikigai.cash.flow.ui.components.bottombars.TransactionScreenRoundedBottomBar
import jp.ikigai.cash.flow.ui.components.buttons.ToggleRow
import jp.ikigai.cash.flow.ui.components.cards.TransactionCard
import jp.ikigai.cash.flow.ui.components.common.ToastBar
import jp.ikigai.cash.flow.ui.components.common.TotalTransactionInfo
import jp.ikigai.cash.flow.ui.components.common.TransactionGroupHeader
import jp.ikigai.cash.flow.ui.components.sheets.CommonSelectionSheet
import jp.ikigai.cash.flow.ui.components.sheets.DateRangePickerBottomSheet
import jp.ikigai.cash.flow.ui.components.sheets.FilterSheet
import jp.ikigai.cash.flow.ui.components.sheets.MoreBottomSheet
import jp.ikigai.cash.flow.ui.viewmodels.listing.TransactionsScreenViewModel
import jp.ikigai.cash.flow.utils.getNumberFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransactionsScreen(
    addTransaction: () -> Unit,
    editTransaction: (String) -> Unit,
    setFilters: (Filters) -> Unit,
    setCurrency: (String) -> Unit,
    setStartDateAndEndDate: (LocalDate, LocalDate) -> Unit,
    navigateToCategoriesScreen: () -> Unit,
    navigateToCounterPartyScreen: () -> Unit,
    navigateToMethodsScreen: () -> Unit,
    navigateToItemsScreen: () -> Unit,
    navigateToSourcesScreen: () -> Unit,
    openGithubPage: () -> Unit,
    events: Flow<Event>,
    state: TransactionsScreenState,
) {
    val configuration = LocalConfiguration.current

    var screenHeight by remember {
        mutableIntStateOf(configuration.screenHeightDp)
    }
    val sheetMaxHeight = remember(key1 = screenHeight) {
        screenHeight * 0.4
    }

    LaunchedEffect(configuration) {
        snapshotFlow { configuration.screenHeightDp }
            .collectLatest { screenHeight = it }
    }

    val scope = rememberCoroutineScope()

    var toastBarString by remember { mutableStateOf("") }

    var showToastBar by remember { mutableStateOf(false) }

    var currentEvent: Event? by remember {
        mutableStateOf(null)
    }

    LaunchedEffect(Unit) {
        events.collectLatest { event ->
            showToastBar = false
            currentEvent = event
            toastBarString = when (event) {
                Event.SourceCategoryMethodRequired -> "At least one category, method and source are required to proceed."
                Event.SourceCategoryRequired -> "At least one category and source are required to proceed."
                Event.SourceMethodRequired -> "At least one method and source are required to proceed."
                Event.CategoryMethodRequired -> "At least one category and method are required to proceed."
                Event.CategoryRequired -> "At least one category is required to proceed."
                Event.MethodRequired -> "At least one method is required to proceed."
                Event.SourceRequired -> "At least one source is required to proceed."
                else -> ""
            }
            showToastBar = true
        }
    }

    LaunchedEffect(showToastBar) {
        if (showToastBar) {
            delay(2000)
            showToastBar = false
        }
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    var sheetType by remember {
        mutableStateOf(SheetType.NONE)
    }

    val numberFormatter by remember {
        mutableStateOf(getNumberFormatter())
    }

    val loading by remember(key1 = state.loading) {
        mutableStateOf(state.loading)
    }

    val currencies by remember(key1 = state.currencies) {
        mutableStateOf(state.currencies)
    }

    val balance by remember(key1 = state.balance) {
        mutableStateOf(state.balance)
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

    when (sheetType) {
        SheetType.TEMPLATES -> {}

        SheetType.FILTER -> {
            FilterSheet(
                filters = filters,
                setFilters = setFilters,
                dismiss = {
                    scope.launch {
                        sheetState.hide()
                        sheetType = SheetType.NONE
                    }
                },
                maxHeight = sheetMaxHeight,
                sheetState = sheetState
            )
        }

        SheetType.CURRENCY -> {
            CommonSelectionSheet(
                index = currencies.indexOfFirst { it.currencyCode == selectedCurrency },
                dismiss = {
                    scope.launch {
                        sheetState.hide()
                        sheetType = SheetType.NONE
                    }
                },
                maxHeight = sheetMaxHeight,
                sheetState = sheetState
            ) {
                items(
                    items = currencies,
                    key = { currency -> "currency-${currency.currencyCode}" }
                ) { currency ->
                    ToggleRow(
                        identifier = currency.currencyCode,
                        label = "${currency.displayName} (${currency.currencyCode})",
                        selected = currency.currencyCode == selectedCurrency,
                        onClick = { currencyCode ->
                            scope.launch {
                                sheetState.hide()
                                sheetType = SheetType.NONE
                                setCurrency(currencyCode)
                            }
                        }
                    )
                }
            }
        }

        SheetType.DATE_RANGE -> {
            DateRangePickerBottomSheet(
                startDate = startDate,
                endDate = endDate,
                filter = setStartDateAndEndDate,
                dismiss = {
                    scope.launch {
                        sheetState.hide()
                        sheetType = SheetType.NONE
                    }
                },
                sheetState = sheetState
            )
        }

        SheetType.MORE_OPTIONS -> {
            MoreBottomSheet(
                navigateToCategoriesScreen = navigateToCategoriesScreen,
                navigateToCounterPartyScreen = navigateToCounterPartyScreen,
                navigateToMethodsScreen = navigateToMethodsScreen,
                navigateToSourcesScreen = navigateToSourcesScreen,
                navigateToItemsScreen = navigateToItemsScreen,
                openGithubReleasesPage = openGithubPage,
                dismiss = {
                    sheetType = SheetType.NONE
                },
                sheetState = sheetState
            )
        }

        else -> {}
    }

    Scaffold(
        modifier = Modifier
            .navigationBarsPadding()
            .imePadding()
            .fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier.padding(5.dp)
                    ) {
                        Text(text = "Transactions")
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
                    sheetType = SheetType.CURRENCY
                },
                onCalendarClick = {
                    sheetType = SheetType.DATE_RANGE
                },
                addTransaction = addTransaction,
                onFilterClick = {
                    sheetType = SheetType.FILTER
                },
                onMoreClick = {
                    sheetType = SheetType.MORE_OPTIONS
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
                        expensesCount = numberFormatter.format(expenseTransactionsCount).toString(),
                        income = numberFormatter.format(totalIncome).toString(),
                        incomeCount = numberFormatter.format(incomeTransactionsCount).toString()
                    )
                }
                state.transactions.forEach {
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
                            amount = numberFormatter.format(transactionWithIcons.amount).toString(),
                            onClick = {
                                editTransaction(transactionWithIcons.uuid)
                            },
                            onLongClick = {},
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                }
            }
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.transactions.isEmpty()) {
                Text(
                    text = "You have not added any transactions.", modifier = Modifier.align(
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
                    message = toastBarString,
                    onDismiss = {
                        showToastBar = false
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun TransactionsScreenPreview() {

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
            addTransaction = {
                if (viewModel.canAddTransaction()) {
                    navController.navigate(Routes.UpsertTransaction.getRoute()) {
                        launchSingleTop = true
                    }
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
            navigateToItemsScreen = {
                navController.navigate(Routes.Items.route) {
                    launchSingleTop = true
                }
            },
            openGithubPage = {
                uriHandler.openUri("https://github.com/ThomasGRG/Expense-Tracker/releases")
            },
            events = viewModel.event,
            state = state
        )
    }
}