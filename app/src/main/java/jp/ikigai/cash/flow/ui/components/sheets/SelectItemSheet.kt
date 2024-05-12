package jp.ikigai.cash.flow.ui.components.sheets

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
@Composable
fun SelectItemSheet(
    templateMode: Boolean = false,
    selectedTransactionItem: TransactionItem,
    items: List<Item>,
    addItem: (TransactionItem) -> Unit,
    dismiss: () -> Unit,
    maxHeight: Double,
    sheetState: SheetState,
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
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

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
            dismiss()
        },
        shape = RoundedCornerShape(10)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .animateContentSize()
                .fillMaxWidth()
                .heightIn(0.dp, maxHeight.dp),
            userScrollEnabled = false,
            verticalAlignment = Alignment.Bottom
        ) {
            when (it) {
                0 -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 10.dp, end = 10.dp),
                    ) {
                        LazyColumn(
                            modifier = Modifier.padding(bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            items(
                                items = items,
                                key = { item -> item.uuid }
                            ) { item ->
                                ToggleRow(
                                    identifier = item.uuid,
                                    label = item.name,
                                    selected = selectedItem?.uuid == item.uuid,
                                    onClick = {
                                        scope.launch {
                                            selectedItem = item
                                            pagerState.scrollToPage(1)
                                        }
                                    }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            TextButton(
                                onClick = {
                                    dismiss()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = stringResource(id = R.string.cancel_button_label))
                            }
                        }
                    }
                }

                1 -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 10.dp, end = 10.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 4.dp, end = 4.dp, bottom = 80.dp)
                                .verticalScroll(scrollState),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LazyHorizontalGrid(
                                rows = GridCells.Fixed(1),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.heightIn(max = 50.dp)
                            ) {
                                items(
                                    items = ItemUnit.values()
                                        .filter { itemUnit -> itemUnit.id < 8 },
                                    key = { itemUnit -> "${itemUnit.id}" }
                                ) { itemUnit ->
                                    ToggleButton(
                                        label = itemUnit.code,
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
                            RoundedCornerOutlinedTextField(
                                value = displayQuantity,
                                onValueChange = { value ->
                                    displayQuantity = value
                                    quantity = value.toDoubleOrNull() ?: 0.0
                                },
                                enabled = true,
                                label = stringResource(id = R.string.quantity_field_label),
                                placeHolder = stringResource(id = R.string.quantity_placeholder_label),
                                backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                                    1.0.dp
                                ),
                                icon = TablerIcons.BoxMultiple9,
                                iconDescription = "quantity icon",
                                onDone = {}
                            )
                            if (!templateMode) {
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
                                        1.0.dp
                                    ),
                                    icon = TablerIcons.CurrencyDollar,
                                    iconDescription = "price icon",
                                    onDone = {}
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        pagerState.scrollToPage(0)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = stringResource(id = R.string.back_button_label))
                            }
                            TextButton(
                                onClick = {
                                    addItem(
                                        TransactionItem(
                                            item = selectedItem,
                                            unit = selectedItemUnit,
                                            quantity = quantity,
                                            price = price
                                        )
                                    )
                                    dismiss()
                                },
                                modifier = Modifier.weight(1f),
                                enabled = templateMode || (quantity > 0 && price > 0)
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