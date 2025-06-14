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
import androidx.compose.runtime.mutableIntStateOf
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
import compose.icons.TablerIcons
import compose.icons.tablericons.SortAscending
import compose.icons.tablericons.SortDescending
import io.objectbox.Property
import io.objectbox.query.QueryBuilder
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.data.enums.PopupType
import jp.ikigai.cash.flow.data.store.entity.CounterParty
import jp.ikigai.cash.flow.data.store.entity.CounterParty_
import jp.ikigai.cash.flow.ui.components.bottombars.ListingScreenRoundedBottomBar
import jp.ikigai.cash.flow.ui.components.cards.InfoCard
import jp.ikigai.cash.flow.ui.components.common.OneHandModeScaffold
import jp.ikigai.cash.flow.ui.components.common.OneHandModeSpacer
import jp.ikigai.cash.flow.ui.components.common.SearchBox
import jp.ikigai.cash.flow.ui.components.popups.SortOptionsPopup
import jp.ikigai.cash.flow.ui.screenStates.common.SortOptionsState
import jp.ikigai.cash.flow.ui.screenStates.listing.CounterPartyScreenState
import jp.ikigai.cash.flow.ui.viewmodels.listing.CounterPartyScreenViewModel
import jp.ikigai.cash.flow.utils.animatedComposable
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterPartyScreen(
    navigateBack: () -> Unit,
    addNewCounterParty: () -> Unit,
    editCounterParty: (Long) -> Unit,
    searchState: String,
    setSearchText: (String) -> Unit,
    sortOptionsState: SortOptionsState<CounterParty>,
    setSortOptions: (Property<CounterParty>, Int) -> Unit,
    setLocale: (Locale?) -> Unit,
    state: CounterPartyScreenState
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

    val count by remember(key1 = state.count) {
        mutableIntStateOf(state.count)
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

    var popupType by remember {
        mutableStateOf(PopupType.NONE)
    }

    val sortOptions = mapOf(
        stringResource(id = R.string.name_field_label) to CounterParty_.name,
        stringResource(id = R.string.frequency_label) to CounterParty_.frequency,
        stringResource(id = R.string.last_used_label) to CounterParty_.lastUsed
    )

    val sortField by remember(key1 = sortOptionsState.sortField) {
        mutableStateOf(sortOptionsState.sortField)
    }

    val sortFlags by remember(key1 = sortOptionsState.sortFlags) {
        mutableIntStateOf(sortOptionsState.sortFlags)
    }

    val sortIcon by remember(key1 = sortOptionsState.sortFlags, key2 = state.count) {
        mutableStateOf(
            if (state.count > 0) {
                if (sortOptionsState.sortFlags == QueryBuilder.DESCENDING) {
                    TablerIcons.SortDescending
                } else {
                    TablerIcons.SortAscending
                }
            } else null
        )
    }

    val showEmptyPlaceholder by remember(key1 = state.counterParties) {
        mutableStateOf(state.counterParties.isEmpty())
    }

    OneHandModeScaffold(
        loading = loading,
        showToastBar = false,
        toastBarText = "",
        onDismissToastBar = {},
        showBottomPopup = popupType != PopupType.NONE,
        bottomPopupContent = { hidePopup ->
            when (popupType) {
                PopupType.SORT -> {
                    SortOptionsPopup(
                        selectedField = sortField,
                        selectedDirection = sortFlags,
                        options = sortOptions,
                        sort = setSortOptions,
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
        emptyPlaceholderText = if (count == 0) {
            stringResource(id = R.string.counter_parties_screen_empty_placeholder_label)
        } else {
            stringResource(id = R.string.choose_icon_screen_empty_placeholder_label, searchText)
        },
        topBar = {
            TopAppBar(
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
                addClick = addNewCounterParty,
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
                    items = counterParties,
                    key = { counterParty -> counterParty.id }
                ) { counterParty ->
                    InfoCard(
                        modifier = Modifier.animateItem(),
                        data = counterParty,
                        onClick = { id ->
                            resetOneHandMode()
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
        addNewCounterParty = {},
        editCounterParty = {},
        searchState = "",
        setSearchText = {},
        sortOptionsState = SortOptionsState(sortField = CounterParty_.lastUsed),
        setSortOptions = { _, _ -> },
        setLocale = {},
        state = CounterPartyScreenState()
    )
}

fun NavGraphBuilder.counterPartyScreen(navController: NavController) {
    animatedComposable(
        Routes.CounterParties.route
    ) {
        val viewModel: CounterPartyScreenViewModel = koinViewModel()
        val state by viewModel.state.collectAsState()
        val searchState by viewModel.searchState.collectAsState()
        val sortOptionsState by viewModel.sortOptionsState.collectAsState()

        CounterPartyScreen(
            navigateBack = {
                navController.popBackStack()
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
            sortOptionsState = sortOptionsState,
            setSortOptions = viewModel::setSortOptions,
            setLocale = viewModel::setLocale,
            state = state
        )
    }
}