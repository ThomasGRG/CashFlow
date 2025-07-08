package jp.ikigai.cash.flow.ui.screens.listing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import compose.icons.TablerIcons
import compose.icons.tablericons.SortAscending
import compose.icons.tablericons.SortDescending
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.data.enums.PopupType
import jp.ikigai.cash.flow.data.enums.SortDirection
import jp.ikigai.cash.flow.ui.components.bottombars.ListingScreenRoundedBottomBar
import jp.ikigai.cash.flow.ui.components.cards.TransactionTemplateCard
import jp.ikigai.cash.flow.ui.components.common.OneHandModeScaffold
import jp.ikigai.cash.flow.ui.components.common.OneHandModeSpacer
import jp.ikigai.cash.flow.ui.components.common.SearchBox
import jp.ikigai.cash.flow.ui.components.popups.SortConfigPopup
import jp.ikigai.cash.flow.ui.screenStates.common.SortConfigState
import jp.ikigai.cash.flow.ui.screenStates.listing.TransactionTemplateScreenState
import jp.ikigai.cash.flow.ui.viewmodels.listing.TransactionTemplateScreenViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionTemplateScreen(
    navigateBack: () -> Unit,
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

    var popupType by remember {
        mutableStateOf(PopupType.NONE)
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

    OneHandModeScaffold(
        loading = loading,
        showToastBar = showToastBar,
        toastBarText = currentEvent?.let {
            stringResource(id = it.message)
        } ?: "",
        onDismissToastBar = {
            showToastBar = false
        },
        showBottomPopup = popupType != PopupType.NONE,
        bottomPopupContent = { hidePopup ->
            when (popupType) {
                PopupType.SORT -> {
                    SortConfigPopup(
                        selectedField = sortField,
                        selectedDirection = sortDirection,
                        fields = sortFields,
                        sort = setSortConfig,
                        dismiss = hidePopup
                    )
                }

                else -> {}
            }
        },
        onDismissPopup = {
            popupType = PopupType.NONE
        },
        showEmptyPlaceholder = showEmptyPlaceholder,
        emptyPlaceholderText = if (count == 0L) {
            stringResource(id = R.string.templates_screen_empty_placeholder_label)
        } else {
            stringResource(id = R.string.choose_icon_screen_empty_placeholder_label, searchText)
        },
        topBar = {
            TopAppBar(
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
                }
            )
        },
        bottomBar = {
            ListingScreenRoundedBottomBar(
                navigateBack = navigateBack,
                sortClick = if (count > 0) {
                    {
                        popupType = PopupType.SORT
                    }
                } else null,
                sortIcon = sortIcon,
                addClick = addNewTransactionTemplate,
                searchClick = if (count > 0) {
                    {
                        if (isFocused) {
                            keyboardController?.show()
                        } else {
                            focusRequester.requestFocus()
                        }
                    }
                } else null,
                graphClick = if (count > 0) {
                    {}
                } else null
            )
        }
    ) { oneHandModeBoxHeight, resetOneHandMode ->
        Column {
            AnimatedVisibility(visible = count > 0) {
                SearchBox(
                    modifier = Modifier
                        .padding(top = 5.dp, start = 10.dp, end = 10.dp, bottom = 10.dp),
                    searchText = searchText,
                    setSearchText = setSearchText,
                    focusRequester = focusRequester,
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
                items(
                    items = templates,
                    key = { template -> template.id }
                ) { transactionTemplate ->
                    TransactionTemplateCard(
                        modifier = Modifier.animateItem(),
                        templateWithChips = transactionTemplate,
                        onClick = { id ->
                            resetOneHandMode()
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