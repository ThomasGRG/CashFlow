/*
CashFlow - Expense Tracker
Copyright (C) 2025 ThomasGRG

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package jp.ikigai.cash.flow.ui.screens.listing

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
import androidx.window.core.layout.WindowSizeClass
import compose.icons.TablerIcons
import compose.icons.tablericons.SortAscending
import compose.icons.tablericons.SortDescending
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.data.enums.ChartType
import jp.ikigai.cash.flow.data.enums.SheetType
import jp.ikigai.cash.flow.data.enums.SortDirection
import jp.ikigai.cash.flow.ui.components.bottombars.ListingScreenBottomAppBar
import jp.ikigai.cash.flow.ui.components.bottomsheets.SortConfigLandscapeSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.SortConfigSheet
import jp.ikigai.cash.flow.ui.components.cards.TransactionTemplateCard
import jp.ikigai.cash.flow.ui.components.common.LandscapeScaffold
import jp.ikigai.cash.flow.ui.components.common.OneHandModeScaffold
import jp.ikigai.cash.flow.ui.components.common.SearchBox
import jp.ikigai.cash.flow.ui.screenStates.common.SortConfigState
import jp.ikigai.cash.flow.ui.screenStates.listing.TransactionTemplateScreenState
import jp.ikigai.cash.flow.ui.viewmodels.listing.TransactionTemplateScreenViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionTemplateScreen(
    navigateBack: () -> Unit,
    viewCharts: () -> Unit,
    addNewTransactionTemplate: () -> Unit,
    editTransactionTemplate: (Long) -> Unit,
    searchState: String,
    setSearchText: (String) -> Unit,
    sortConfigState: SortConfigState,
    setSortConfig: (String, SortDirection) -> Unit,
    setLocale: (Locale?) -> Unit,
    events: Flow<Event>,
    state: TransactionTemplateScreenState
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

    var sheetType by remember {
        mutableStateOf(SheetType.NONE)
    }

    val sortFields = mapOf(
        stringResource(id = R.string.name_field_label) to "templateName",
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

    val templates by remember(key1 = state.templates) {
        mutableStateOf(state.templates)
    }

    val showEmptyPlaceholder by remember(key1 = state.templates) {
        mutableStateOf(state.templates.isEmpty())
    }

    if (
        windowSizeClass.isWidthAtLeastBreakpoint(
            WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND
        )
    ) {
        LandscapeScaffold(
            loading = loading,
            sheetState = sheetState,
            showBottomSheet = sheetType != SheetType.NONE,
            bottomSheetContent = {
                SortConfigLandscapeSheet(
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
                stringResource(id = R.string.templates_screen_empty_placeholder_label)
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
                    SearchBox(
                        modifier = Modifier.background(MaterialTheme.colorScheme.background),
                        searchText = searchText,
                        setSearchText = setSearchText,
                        focusRequester = focusRequester,
                        interactionSource = interactionSource
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    ListingScreenBottomAppBar(
                        title = stringResource(id = R.string.templates_label),
                        subTitle = stringResource(
                            id = R.string.templates_count_label,
                            countString
                        ),
                        navigateBack = navigateBack,
                        sortClick = {
                            sheetType = SheetType.SORT
                        },
                        sortIcon = sortIcon,
                        addClick = addNewTransactionTemplate,
                        chartClick = viewCharts,
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
                        items = templates,
                        key = { template -> template.id }
                    ) { transactionTemplate ->
                        TransactionTemplateCard(
                            modifier = Modifier.animateItem(),
                            templateWithChips = transactionTemplate,
                            onClick = { id ->
                                editTransactionTemplate(id)
                            }
                        )
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
            },
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
                stringResource(id = R.string.templates_screen_empty_placeholder_label)
            } else {
                stringResource(id = R.string.no_results_found_search_placeholder_label, searchText)
            },
            topBar = { scrollBehavior, expandedHeight ->
                LargeTopAppBar(
                    title = {
                        Column {
                            Text(text = stringResource(id = R.string.templates_label))
                            Text(
                                text = stringResource(
                                    id = R.string.templates_count_label,
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
                        sheetType = SheetType.SORT
                    },
                    sortIcon = sortIcon,
                    addClick = addNewTransactionTemplate,
                    searchClick = {
                        if (isFocused) {
                            keyboardController?.show()
                        } else {
                            focusRequester.requestFocus()
                        }
                    },
                    chartClick = viewCharts,
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
                items(
                    items = templates,
                    key = { template -> template.id }
                ) { transactionTemplate ->
                    TransactionTemplateCard(
                        modifier = Modifier.animateItem(),
                        templateWithChips = transactionTemplate,
                        onClick = { id ->
                            editTransactionTemplate(id)
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun TransactionTemplateScreenPreview() {
    TransactionTemplateScreen(
        navigateBack = {},
        viewCharts = {},
        addNewTransactionTemplate = {},
        editTransactionTemplate = {},
        searchState = "",
        setSearchText = {},
        sortConfigState = SortConfigState(sortField = "transactionCount"),
        setSortConfig = { _, _ -> },
        setLocale = {},
        events = emptyList<Event>().asFlow(),
        state = TransactionTemplateScreenState()
    )
}

fun NavGraphBuilder.transactionTemplateScreen(navController: NavController) {
    composable(
        route = Routes.Templates.route
    ) {
        val viewModel: TransactionTemplateScreenViewModel = koinViewModel()
        val state by viewModel.state.collectAsState()
        val searchState by viewModel.searchState.collectAsState()
        val sortConfigState by viewModel.sortConfigState.collectAsState()

        TransactionTemplateScreen(
            navigateBack = {
                navController.popBackStack()
            },
            viewCharts = {
                navController.navigate(
                    Routes.Insights.getRoute(ChartType.TEMPLATE_TRANSACTION_COUNT_BAR_CHART)
                ) {
                    launchSingleTop = true
                }
            },
            addNewTransactionTemplate = {
                if (viewModel.canAddTransaction()) {
                    navController.navigate(Routes.UpsertTemplate.getRoute()) {
                        launchSingleTop = true
                    }
                }
            },
            editTransactionTemplate = { id ->
                navController.navigate(Routes.UpsertTemplate.getRoute(id)) {
                    launchSingleTop = true
                }
            },
            searchState = searchState,
            setSearchText = viewModel::setSearchText,
            sortConfigState = sortConfigState,
            setSortConfig = viewModel::setSortConfig,
            setLocale = viewModel::setLocale,
            events = viewModel.event,
            state = state
        )
    }
}