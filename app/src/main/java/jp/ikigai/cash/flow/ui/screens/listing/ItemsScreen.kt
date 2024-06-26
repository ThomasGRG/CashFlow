package jp.ikigai.cash.flow.ui.screens.listing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.data.entity.Item
import jp.ikigai.cash.flow.data.enums.PopupType
import jp.ikigai.cash.flow.ui.components.bottombars.ThreeSlotRoundedBottomBar
import jp.ikigai.cash.flow.ui.components.cards.ItemCard
import jp.ikigai.cash.flow.ui.components.common.OneHandModeScaffold
import jp.ikigai.cash.flow.ui.components.common.OneHandModeSpacer
import jp.ikigai.cash.flow.ui.components.popups.UpsertItemPopup
import jp.ikigai.cash.flow.ui.screenStates.listing.ItemsScreenState
import jp.ikigai.cash.flow.ui.viewmodels.listing.ItemsScreenViewModel
import jp.ikigai.cash.flow.utils.animatedComposable
import jp.ikigai.cash.flow.utils.getNumberFormatter
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsScreen(
    navigateBack: () -> Unit,
    upsertItem: (String) -> Unit,
    editItem: (Item) -> Unit,
    state: ItemsScreenState
) {
    val numberFormatter by remember {
        mutableStateOf(getNumberFormatter())
    }

    val loading by remember(key1 = state.loading) {
        mutableStateOf(state.loading)
    }

    val showEmptyPlaceholder by remember(key1 = state.items) {
        mutableStateOf(state.items.isEmpty())
    }

    val items by remember(key1 = state.items) {
        mutableStateOf(state.items.map { it.name })
    }

    val itemCount by remember(key1 = state.items) {
        mutableStateOf(
            numberFormatter
                .format(state.items.size)
                .toString()
        )
    }

    val selectedItem by remember(key1 = state.selectedItem) {
        mutableStateOf(state.selectedItem)
    }

    val enabled by remember(key1 = state.enabled) {
        mutableStateOf(state.enabled)
    }

    var popupType by remember {
        mutableStateOf(PopupType.NONE)
    }

    OneHandModeScaffold(
        loading = loading,
        showToastBar = false,
        toastBarText = "",
        onDismissToastBar = {},
        showBottomPopup = popupType == PopupType.ITEMS,
        bottomPopupContent = { hidePopup ->
            UpsertItemPopup(
                name = selectedItem.name,
                items = items,
                enabled = enabled,
                save = {
                    upsertItem(it)
                },
                dismiss = {
                    hidePopup()
                    popupType = PopupType.NONE
                }
            )
        },
        onDismissPopup = {
            popupType = PopupType.NONE
        },
        showEmptyPlaceholder = showEmptyPlaceholder,
        emptyPlaceholderText = stringResource(id = R.string.items_screen_empty_placeholder_label),
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier.padding(5.dp)
                    ) {
                        Text(text = stringResource(id = R.string.items_label))
                        Text(
                            text = stringResource(id = R.string.items_count_label, itemCount),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.alpha(0.8f)
                        )
                    }
                }
            )
        },
        bottomBar = {
            ThreeSlotRoundedBottomBar(
                navigateBack = navigateBack,
                floatingButtonAction = {
                    editItem(Item())
                    popupType = PopupType.ITEMS
                },
                floatingButtonIcon = {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = Icons.Filled.Add.name)
                },
            )
        }
    ) { oneHandModeBoxHeight, resetOneHandMode ->
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
                items = state.items,
                key = { itemDetails -> itemDetails.uuid }
            ) { itemDetails ->
                ItemCard(
                    title = itemDetails.name,
                    frequency = numberFormatter.format(itemDetails.frequency).toString(),
                    pricePerUnit = if (itemDetails.lastKnownPrice > 0) {
                        stringResource(
                            id = R.string.price_per_unit_label,
                            numberFormatter.format(itemDetails.lastKnownPrice).toString(),
                            itemDetails.lastUsedCurrency,
                            stringResource(id = itemDetails.lastUsedUnit.code)
                        )
                    } else "",
                    onClick = {
                        resetOneHandMode()
                        editItem(itemDetails)
                        popupType = PopupType.ITEMS
                    },
                )
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
        upsertItem = {},
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
            editItem = { item ->
                viewModel.editItem(item)
            },
            state = state
        )
    }
}