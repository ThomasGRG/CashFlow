package jp.ikigai.cash.flow.ui.components.sheets

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.BoxMultiple9
import compose.icons.tablericons.CurrencyDollar
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.entity.Item
import jp.ikigai.cash.flow.data.entity.TransactionItem
import jp.ikigai.cash.flow.data.enums.ItemUnit
import jp.ikigai.cash.flow.ui.components.buttons.ToggleButton
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
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
                .fillMaxWidth()
                .heightIn(0.dp, maxHeight.dp),
            contentPadding = PaddingValues(10.dp),
            userScrollEnabled = false,
            verticalAlignment = Alignment.Bottom
        ) {
            when (it) {
                0 -> {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(
                                state = scrollState
                            ),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        items.forEach { item ->
                            Row(
                                modifier = Modifier.padding(start = 4.dp, end = 4.dp)
                            ) {
                                ToggleButton(
                                    label = item.name,
                                    selected = selectedItem?.uuid == item.uuid,
                                    toggle = {
                                        scope.launch {
                                            selectedItem = item
                                            pagerState.animateScrollToPage(1)
                                        }
                                    }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
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
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(0.dp, maxHeight.dp)
                            .verticalScroll(
                                state = scrollState
                            ),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        ItemUnit.values().filter { itemUnit -> itemUnit.id < 8 }
                            .forEach { itemUnit ->
                                Row(
                                    modifier = Modifier.padding(start = 4.dp, end = 4.dp)
                                ) {
                                    ToggleButton(
                                        label = itemUnit.code,
                                        selected = selectedItemUnit.id == itemUnit.id,
                                        toggle = {
                                            selectedItemUnit = if (selectedItemUnit != itemUnit) {
                                                itemUnit
                                            } else {
                                                ItemUnit.PIECE
                                            }
                                        }
                                    )
                                }
                            }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 4.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = displayQuantity,
                                onValueChange = { value ->
                                    displayQuantity = value
                                    val newQuantity = value.toDoubleOrNull()
                                    quantity = newQuantity ?: 0.0
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.None,
                                    autoCorrect = false,
                                    keyboardType = KeyboardType.Number,
                                ),
                                leadingIcon = {
                                    Icon(
                                        imageVector = TablerIcons.BoxMultiple9,
                                        contentDescription = "quantity icon",
                                    )
                                },
                                label = {
                                    Text(text = stringResource(id = R.string.quantity_field_label))
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                            )
                            if (!templateMode) {
                                OutlinedTextField(
                                    value = displayPrice,
                                    onValueChange = { value ->
                                        displayPrice = value
                                        val newPrice = value.toDoubleOrNull()
                                        price = newPrice ?: 0.0
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.None,
                                        autoCorrect = false,
                                        keyboardType = KeyboardType.Number,
                                    ),
                                    leadingIcon = {
                                        Icon(
                                            imageVector = TablerIcons.CurrencyDollar,
                                            contentDescription = "price icon",
                                        )
                                    },
                                    label = {
                                        Text(text = stringResource(id = R.string.price_field_label))
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(0)
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