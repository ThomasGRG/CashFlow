package jp.ikigai.cash.flow.ui.components.popups

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.BoxMultiple9
import compose.icons.tablericons.CurrencyDollar
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.entity.Item
import jp.ikigai.cash.flow.data.entity.TransactionItem
import jp.ikigai.cash.flow.data.enums.ItemUnit
import jp.ikigai.cash.flow.ui.components.buttons.ToggleButton
import jp.ikigai.cash.flow.ui.components.buttons.ToggleRow
import jp.ikigai.cash.flow.ui.components.common.RoundedCornerOutlinedTextField
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SelectItemPopup(
    index: Int,
    templateMode: Boolean = false,
    selectedTransactionItem: TransactionItem,
    items: List<Item>,
    addItem: (TransactionItem) -> Unit,
    dismiss: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val scope = rememberCoroutineScope()

    val listState = rememberLazyListState()

    val lazyRowState = rememberLazyListState()

    LaunchedEffect(Unit) {
        listState.scrollToItem(index)
    }

    val pagerState = rememberPagerState(
        pageCount = { 2 },
        initialPage = 0
    )

    var selectedItem by remember {
        mutableStateOf(selectedTransactionItem.item)
    }

    var selectedItemUnit by remember {
        mutableStateOf(selectedTransactionItem.unit)
    }

    var price by remember {
        mutableStateOf(selectedTransactionItem.price)
    }

    var displayPrice by remember {
        mutableStateOf(selectedTransactionItem.price.toString())
    }

    var quantity by remember {
        mutableStateOf(selectedTransactionItem.quantity)
    }

    var displayQuantity by remember {
        mutableStateOf(selectedTransactionItem.quantity.toString())
    }

    val itemUnits by remember {
        mutableStateOf(
            ItemUnit.values()
                .filter { itemUnit -> itemUnit.id != 8 }
        )
    }

    LaunchedEffect(key1 = pagerState) {
        snapshotFlow { pagerState.currentPage }.collectLatest { page ->
            if (page == 1) {
                lazyRowState.animateScrollToItem(
                    itemUnits.indexOfFirst { it.id == selectedItemUnit.id }.coerceAtLeast(0)
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .animateContentSize()
                .fillMaxWidth(),
            userScrollEnabled = false,
            verticalAlignment = Alignment.Bottom
        ) {
            when (it) {
                0 -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .heightIn(max = 250.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            items(
                                items = items,
                                key = { item -> "item-${item.uuid}" }
                            ) { item ->
                                ToggleRow(
                                    identifier = item.uuid,
                                    label = item.name,
                                    selected = selectedItem?.uuid == item.uuid,
                                    onClick = {
                                        scope.launch {
                                            selectedItem = item
                                            price = item.lastKnownPrice
                                            displayPrice = item.lastKnownPrice.toString()
                                            pagerState.animateScrollToPage(1)
                                        }
                                    }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            OutlinedButton(
                                onClick = {
                                    dismiss()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(35)
                            ) {
                                Text(text = stringResource(id = R.string.cancel_button_label))
                            }
                        }
                    }
                }

                1 -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LazyRow(
                            state = lazyRowState,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            userScrollEnabled = true,
                        ) {
                            items(
                                items = itemUnits,
                                key = { itemUnit -> "${itemUnit.id}" }
                            ) { itemUnit ->
                                Row(
                                    modifier = Modifier.padding(start = 3.dp, end = 3.dp)
                                ) {
                                    ToggleButton(
                                        label = stringResource(id = itemUnit.code),
                                        selected = selectedItemUnit.id == itemUnit.id,
                                        toggle = {
                                            selectedItemUnit =
                                                if (selectedItemUnit != itemUnit) {
                                                    itemUnit
                                                } else {
                                                    ItemUnit.PIECE
                                                }
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        RoundedCornerOutlinedTextField(
                            value = displayQuantity,
                            onValueChange = { value ->
                                displayQuantity = value
                                quantity = value.toDoubleOrNull() ?: if (templateMode) 1.0 else 0.0
                            },
                            enabled = true,
                            label = stringResource(id = R.string.quantity_field_label),
                            placeHolder = stringResource(id = R.string.quantity_placeholder_label),
                            backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                                3.dp
                            ),
                            icon = TablerIcons.BoxMultiple9,
                            iconDescription = "quantity icon",
                            onDone = {
                                keyboardController?.hide()
                            }
                        )
                        if (!templateMode) {
                            Spacer(modifier = Modifier.height(8.dp))
                            RoundedCornerOutlinedTextField(
                                value = displayPrice,
                                onValueChange = { value ->
                                    displayPrice = value
                                    price = value.toDoubleOrNull() ?: 0.0
                                },
                                enabled = true,
                                label = stringResource(id = R.string.price_field_label),
                                placeHolder = stringResource(id = R.string.price_placeholder_label),
                                backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                                    3.dp
                                ),
                                icon = TablerIcons.CurrencyDollar,
                                iconDescription = "price icon",
                                onDone = {
                                    keyboardController?.hide()
                                }
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        pagerState.scrollToPage(0)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(35)
                            ) {
                                Text(text = stringResource(id = R.string.back_button_label))
                            }
                            FilledTonalButton(
                                enabled = templateMode || (quantity > 0 && price > 0),
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    dismiss()
                                    addItem(
                                        TransactionItem(
                                            item = selectedItem,
                                            unit = selectedItemUnit,
                                            quantity = if (templateMode) quantity.coerceAtLeast(1.0) else 0.0,
                                            price = price
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(35)
                            ) {
                                Text(text = stringResource(id = R.string.add_button_label))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun SelectItemPopupPreview() {
    SelectItemPopup(
        index = 1,
        templateMode = false,
        selectedTransactionItem = TransactionItem().apply {
            item = Item().apply {
                uuid = "asd"
                name = "Shopping"
                lastUsedCurrency = "INR"
                lastKnownPrice = 95.03
                lastUsedUnit = ItemUnit.KILOGRAM
            }
            unit = ItemUnit.KILOGRAM
            price = 234.00
            quantity = 1.0
        },
        items = listOf(
            Item().apply {
                uuid = "asd"
                name = "Shopping"
                lastUsedCurrency = "INR"
                lastKnownPrice = 95.03
                lastUsedUnit = ItemUnit.KILOGRAM
            },
            Item().apply {
                uuid = "asdfrg"
                name = "Transportation"
                lastUsedCurrency = "INR"
                lastKnownPrice = 95.03
                lastUsedUnit = ItemUnit.KILOGRAM
            },
            Item().apply {
                uuid = "iurwuef"
                name = "Personal Care"
                lastUsedCurrency = "INR"
                lastKnownPrice = 95.03
                lastUsedUnit = ItemUnit.KILOGRAM
            },
            Item().apply {
                uuid = "iurwueadfegf"
                name = "Food & Drinks"
                lastUsedCurrency = "INR"
                lastKnownPrice = 95.03
                lastUsedUnit = ItemUnit.KILOGRAM
            }
        ),
        addItem = {},
        dismiss = {}
    )
}