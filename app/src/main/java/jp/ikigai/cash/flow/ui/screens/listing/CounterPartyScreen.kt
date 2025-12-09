package jp.ikigai.cash.flow.ui.screens.listing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import compose.icons.TablerIcons
import compose.icons.tablericons.SortAscending
import compose.icons.tablericons.SortDescending
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.data.enums.ChartType
import jp.ikigai.cash.flow.data.enums.SheetType
import jp.ikigai.cash.flow.data.enums.SortDirection
import jp.ikigai.cash.flow.ui.components.bottombars.ListingScreenBottomAppBar
import jp.ikigai.cash.flow.ui.components.bottomsheets.SortConfigSheet
import jp.ikigai.cash.flow.ui.components.cards.InfoCard
import jp.ikigai.cash.flow.ui.components.common.LandscapeScaffold
import jp.ikigai.cash.flow.ui.components.common.OneHandModeScaffold
import jp.ikigai.cash.flow.ui.components.common.SearchBox
import jp.ikigai.cash.flow.ui.screenStates.common.SortConfigState
import jp.ikigai.cash.flow.ui.screenStates.listing.CounterPartyScreenState
import jp.ikigai.cash.flow.ui.viewmodels.listing.CounterPartyScreenViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterPartyScreen(
    navigateBack: () -> Unit,
    viewCharts: () -> Unit,
    addNewCounterParty: () -> Unit,
    editCounterParty: (Long) -> Unit,
    searchState: String,
    setSearchText: (String) -> Unit,
    sortConfigState: SortConfigState,
    setSortConfig: (String, SortDirection) -> Unit,
    setLocale: (Locale?) -> Unit,
    state: CounterPartyScreenState
) {
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

    val count by remember(key1 = state.count) {
        mutableLongStateOf(state.count)
    }

    val countString by remember(key1 = state.countString) {
        mutableStateOf(state.countString)
    }

    val searchText by remember(key1 = searchState) {
        mutableStateOf(searchState)
    }

    val loading by remember(key1 = state.loading) {
        mutableStateOf(state.loading)
    }

    val counterParties by remember(key1 = state.counterParties) {
        mutableStateOf(state.counterParties)
    }

    var sheetType by remember {
        mutableStateOf(SheetType.NONE)
    }

    val sortFields = mapOf(
        stringResource(id = R.string.name_field_label) to "counterPartyName",
        stringResource(id = R.string.frequency_label) to "transactionCount",
        stringResource(id = R.string.last_used_label) to "lastUsed"
    )

    val sortField by remember(key1 = sortConfigState.sortField) {
        mutableStateOf(sortConfigState.sortField)
    }

    val sortDirection by remember(key1 = sortConfigState.sortDirection) {
        mutableStateOf(sortConfigState.sortDirection)
    }

    val sortIcon by remember(key1 = sortConfigState.sortDirection) {
        mutableStateOf(
            if (sortConfigState.sortDirection == SortDirection.DESC) {
                TablerIcons.SortDescending
            } else {
                TablerIcons.SortAscending
            }
        )
    }

    val showEmptyPlaceholder by remember(key1 = state.counterParties) {
        mutableStateOf(state.counterParties.isEmpty())
    }

    if (windowSizeClass.windowHeightSizeClass == WindowHeightSizeClass.COMPACT && windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.MEDIUM) {
        LandscapeScaffold(
            loading = loading,
            sheetState = sheetState,
            showBottomSheet = sheetType != SheetType.NONE,
            bottomSheetContent = {
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
            },
            onDismissSheet = {
                sheetType = SheetType.NONE
            },
            showEmptyPlaceholder = showEmptyPlaceholder,
            emptyPlaceholderText = if (count == 0L) {
                stringResource(id = R.string.counter_parties_screen_empty_placeholder_label)
            } else {
                stringResource(id = R.string.no_results_found_search_placeholder_label, searchText)
            },
            firstColContent = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 10.dp, end = 10.dp, bottom = 10.dp, top = 2.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AnimatedVisibility(
                        visible = count > 0,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        SearchBox(
                            modifier = Modifier.background(MaterialTheme.colorScheme.background),
                            searchText = searchText,
                            setSearchText = setSearchText,
                            focusRequester = focusRequester,
                            interactionSource = interactionSource
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    ListingScreenBottomAppBar(
                        title = stringResource(id = R.string.counter_parties_label),
                        subTitle = stringResource(
                            id = R.string.counter_party_count_label,
                            countString
                        ),
                        navigateBack = navigateBack,
                        sortClick = {
                            if (count > 0) {
                                sheetType = SheetType.SORT
                            }
                        },
                        sortIcon = sortIcon,
                        addClick = addNewCounterParty,
                        chartClick = {
                            if (count > 0) {
                                viewCharts()
                            }
                        },
                    )
                }
            },
            secondColContent = {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    items(
                        items = counterParties,
                        key = { counterParty -> counterParty.id }
                    ) { counterParty ->
                        InfoCard(
                            modifier = Modifier.animateItem(),
                            data = counterParty,
                            onClick = { id ->
                                editCounterParty(id)
                            }
                        )
                    }
                }
            }
        )
    } else {
        OneHandModeScaffold(
            loading = loading,
            sheetState = sheetState,
            showBottomSheet = sheetType != SheetType.NONE,
            bottomSheetContent = {
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
            },
            onDismissSheet = {
                sheetType = SheetType.NONE
            },
            showEmptyPlaceholder = showEmptyPlaceholder,
            emptyPlaceholderText = if (count == 0L) {
                stringResource(id = R.string.counter_parties_screen_empty_placeholder_label)
            } else {
                stringResource(id = R.string.no_results_found_search_placeholder_label, searchText)
            },
            topBar = { scrollBehavior, expandedHeight ->
                LargeTopAppBar(
                    title = {
                        Column {
                            Text(text = stringResource(id = R.string.counter_parties_label))
                            Text(
                                text = stringResource(
                                    id = R.string.counter_party_count_label,
                                    countString
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
                ListingScreenBottomAppBar(
                    navigateBack = navigateBack,
                    sortClick = {
                        if (count > 0) {
                            sheetType = SheetType.SORT
                        }
                    },
                    sortIcon = sortIcon,
                    addClick = addNewCounterParty,
                    searchClick = {
                        if (count > 0) {
                            if (isFocused) {
                                keyboardController?.show()
                            } else {
                                focusRequester.requestFocus()
                            }
                        }
                    },
                    chartClick = {
                        if (count > 0) {
                            viewCharts()
                        }
                    }
                )
            }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 10.dp, end = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                stickyHeader(
                    key = "search",
                    contentType = "search_box"
                ) {
                    AnimatedVisibility(
                        visible = count > 0,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        SearchBox(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.background)
                                .padding(top = 5.dp, bottom = 8.dp),
                            searchText = searchText,
                            setSearchText = setSearchText,
                            focusRequester = focusRequester,
                            interactionSource = interactionSource
                        )
                    }
                }
                items(
                    items = counterParties,
                    key = { counterParty -> counterParty.id }
                ) { counterParty ->
                    InfoCard(
                        modifier = Modifier.animateItem(),
                        data = counterParty,
                        onClick = { id ->
                            editCounterParty(id)
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun CounterPartyScreenPreview() {
    CounterPartyScreen(
        navigateBack = {},
        viewCharts = {},
        addNewCounterParty = {},
        editCounterParty = {},
        searchState = "",
        setSearchText = {},
        sortConfigState = SortConfigState(sortField = "frequency"),
        setSortConfig = { _, _ -> },
        setLocale = {},
        state = CounterPartyScreenState()
    )
}

fun NavGraphBuilder.counterPartyScreen(navController: NavController) {
    composable(
        route = Routes.CounterParties.route
    ) {
        val viewModel: CounterPartyScreenViewModel = koinViewModel()
        val state by viewModel.state.collectAsState()
        val searchState by viewModel.searchState.collectAsState()
        val sortConfigState by viewModel.sortConfigState.collectAsState()

        CounterPartyScreen(
            navigateBack = {
                navController.popBackStack()
            },
            viewCharts = {
                navController.navigate(
                    Routes.Insights.getRoute(ChartType.COUNTERPARTY_DEBIT_CREDIT_BAR_CHART)
                ) {
                    launchSingleTop = true
                }
            },
            addNewCounterParty = {
                navController.navigate(Routes.UpsertCounterParty.getRoute()) {
                    launchSingleTop = true
                }
            },
            editCounterParty = { id ->
                navController.navigate(Routes.UpsertCounterParty.getRoute(id)) {
                    launchSingleTop = true
                }
            },
            searchState = searchState,
            setSearchText = viewModel::setSearchText,
            sortConfigState = sortConfigState,
            setSortConfig = viewModel::setSortConfig,
            setLocale = viewModel::setLocale,
            state = state
        )
    }
}