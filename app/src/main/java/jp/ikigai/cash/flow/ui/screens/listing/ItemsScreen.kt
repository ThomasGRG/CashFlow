package jp.ikigai.cash.flow.ui.screens.listing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import compose.icons.TablerIcons
import compose.icons.tablericons.SortAscending
import compose.icons.tablericons.SortDescending
import io.realm.kotlin.query.Sort
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.data.dto.ItemListingDTO
import jp.ikigai.cash.flow.data.enums.PopupType
import jp.ikigai.cash.flow.ui.components.bottombars.ListingScreenRoundedBottomBar
import jp.ikigai.cash.flow.ui.components.cards.ItemCard
import jp.ikigai.cash.flow.ui.components.common.OneHandModeScaffold
import jp.ikigai.cash.flow.ui.components.common.OneHandModeSpacer
import jp.ikigai.cash.flow.ui.components.popups.SortOptionsPopup
import jp.ikigai.cash.flow.ui.components.popups.UpsertItemPopup
import jp.ikigai.cash.flow.ui.screenStates.listing.ItemsScreenState
import jp.ikigai.cash.flow.ui.viewmodels.listing.ItemsScreenViewModel
import jp.ikigai.cash.flow.utils.animatedComposable
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsScreen(
    navigateBack: () -> Unit,
    upsertItem: (String, ItemListingDTO?) -> Unit,
    editItem: (ItemListingDTO?) -> Unit,
    setSearchText: (String) -> Unit,
    setSortInfo: (String, Sort) -> Unit,
    setLocale: (Locale?) -> Unit,
    state: ItemsScreenState
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
        mutableLongStateOf(state.count)
    }

    val countString by remember(key1 = state.countString) {
        mutableStateOf(state.countString)
    }

    val searchText by remember(key1 = state.searchText) {
        mutableStateOf(state.searchText)
    }

    val loading by remember(key1 = state.loading) {
        mutableStateOf(state.loading)
    }

    val items by remember(key1 = state.items) {
        mutableStateOf(state.items)
    }

    var popupType by remember {
        mutableStateOf(PopupType.NONE)
    }

    val sortOptions = mapOf(
        stringResource(id = R.string.name_field_label) to "name",
        stringResource(id = R.string.price_field_label) to "lastKnownPrice",
        stringResource(id = R.string.frequency_label) to "frequency",
        stringResource(id = R.string.last_used_label) to "lastUsed"
    )

    val sortOption by remember(key1 = state.sortField) {
        mutableStateOf(state.sortField)
    }

    val sortDirection by remember(key1 = state.sortDirection) {
        mutableStateOf(state.sortDirection)
    }

    val sortIcon by remember(key1 = state.sortDirection, key2 = state.count) {
        mutableStateOf(
            if (state.count > 0) {
                if (state.sortDirection == Sort.DESCENDING) {
                    TablerIcons.SortDescending
                } else {
                    TablerIcons.SortAscending
                }
            } else null
        )
    }

    val showEmptyPlaceholder by remember(key1 = state.items) {
        mutableStateOf(state.items.isEmpty())
    }

    val itemNames by remember(key1 = state.itemNames) {
        mutableStateOf(state.itemNames)
    }

    val selectedItem by remember(key1 = state.selectedItem) {
        mutableStateOf(state.selectedItem)
    }

    val enabled by remember(key1 = state.enabled) {
        mutableStateOf(state.enabled)
    }

    OneHandModeScaffold(
        loading = loading,
        showToastBar = false,
        toastBarText = "",
        onDismissToastBar = {},
        showBottomPopup = popupType != PopupType.NONE,
        bottomPopupContent = { hidePopup ->
            when (popupType) {
                PopupType.UPSERT_ITEM -> {
                    UpsertItemPopup(
                        name = selectedItem?.annotatedName?.text ?: "",
                        items = itemNames,
                        enabled = enabled,
                        save = { newName ->
                            upsertItem(newName, selectedItem)
                        },
                        dismiss = {
                            hidePopup()
                            popupType = PopupType.NONE
                        }
                    )
                }

                PopupType.SORT -> {
                    SortOptionsPopup(
                        selectedOption = sortOption,
                        selectedDirection = sortDirection,
                        options = sortOptions,
                        sort = setSortInfo,
                        dismiss = {
                            hidePopup()
                            popupType = PopupType.NONE
                        }
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
            stringResource(id = R.string.items_screen_empty_placeholder_label)
        } else {
            stringResource(id = R.string.choose_icon_screen_empty_placeholder_label, searchText)
        },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = stringResource(id = R.string.items_label))
                        Text(
                            text = stringResource(id = R.string.items_count_label, countString),
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
                addClick = {
                    editItem(null)
                    popupType = PopupType.UPSERT_ITEM
                },
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
                    items = items,
                    key = { item -> item.uuid }
                ) { item ->
                    ItemCard(
                        modifier = Modifier.animateItem(),
                        data = item,
                        onClick = { itemDTO ->
                            resetOneHandMode()
                            editItem(itemDTO)
                            popupType = PopupType.UPSERT_ITEM
                        },
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ItemsScreenPreview() {
    ItemsScreen(
        navigateBack = {},
        editItem = {},
        upsertItem = { _, _ -> },
        setSearchText = {},
        setSortInfo = { _, _ -> },
        setLocale = {},
        state = ItemsScreenState()
    )
}

fun NavGraphBuilder.itemScreen(navController: NavController) {
    animatedComposable(
        Routes.Items.route
    ) {
        val viewModel: ItemsScreenViewModel = koinViewModel()
        val state by viewModel.state.collectAsState()

        ItemsScreen(
            navigateBack = {
                navController.popBackStack()
            },
            upsertItem = viewModel::upsertItem,
            editItem = viewModel::editItem,
            setSearchText = viewModel::setSearchText,
            setSortInfo = viewModel::setSortInfo,
            setLocale = viewModel::setLocale,
            state = state
        )
    }
}