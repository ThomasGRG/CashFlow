package jp.ikigai.cash.flow.ui.components.bottombars

import android.icu.util.Currency
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.CalendarEvent
import compose.icons.tablericons.DotsVertical
import compose.icons.tablericons.Plus
import compose.icons.tablericons.Search
import compose.icons.tablericons.SortAscending
import compose.icons.tablericons.SortDescending
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.enums.SheetType
import jp.ikigai.cash.flow.data.enums.SortDirection
import jp.ikigai.cash.flow.ui.components.common.FilterContainer

@Composable
fun TransactionScreenBottomAppBar(
    selectedCurrencySymbol: String,
    sortField: String?,
    sortDirection: SortDirection,
    filterAmount: String,
    selectedAccountCount: String,
    selectedCategoryCount: String,
    selectedCounterPartyCount: String,
    counterPartyFilterVisible: Boolean,
    selectedMethodCount: String,
    selectedTransactionTypeCount: Int,
    addTransaction: () -> Unit,
    onSearchClick: () -> Unit,
    setSheetType: (SheetType) -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    val sortIcon by remember(key1 = sortDirection) {
        mutableStateOf(
            if (sortDirection == SortDirection.DESC) {
                TablerIcons.SortDescending
            } else {
                TablerIcons.SortAscending
            }
        )
    }

    val sortedBy by remember(key1 = sortField) {
        mutableStateOf(sortField ?: "")
    }

    Column {
        FilterContainer(
            padding = PaddingValues(
                top = 6.dp,
                bottom = 10.dp,
            )
        ) {
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    setSheetType(SheetType.SORT)
                },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                shape = MaterialTheme.shapes.small
            ) {
                Icon(
                    imageVector = sortIcon,
                    contentDescription = "sort direction icon"
                )
                Text(
                    text = stringResource(id = R.string.sort_by_chip_label, sortedBy),
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    setSheetType(SheetType.TYPE)
                },
                contentPadding = PaddingValues(0.dp),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = stringResource(
                        id = R.string.transaction_type_filter_chip_label,
                        selectedTransactionTypeCount
                    ),
                    modifier = Modifier.padding(10.dp),
                )
            }
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    setSheetType(SheetType.AMOUNT)
                },
                contentPadding = PaddingValues(0.dp),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = stringResource(id = R.string.amount_filter_chip_label, filterAmount),
                    modifier = Modifier.padding(10.dp),
                )
            }
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    setSheetType(SheetType.CATEGORY)
                },
                contentPadding = PaddingValues(0.dp),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = stringResource(
                        id = R.string.category_filter_chip_label,
                        selectedCategoryCount
                    ),
                    modifier = Modifier.padding(10.dp),
                )
            }
            if (counterPartyFilterVisible) {
                FilledTonalButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        setSheetType(SheetType.COUNTERPARTY)
                    },
                    contentPadding = PaddingValues(0.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = stringResource(
                            id = R.string.counter_party_filter_chip_label,
                            selectedCounterPartyCount
                        ),
                        modifier = Modifier.padding(10.dp),
                    )
                }
            }
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    setSheetType(SheetType.METHOD)
                },
                contentPadding = PaddingValues(0.dp),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = stringResource(
                        id = R.string.method_filter_chip_label,
                        selectedMethodCount
                    ),
                    modifier = Modifier.padding(10.dp),
                )
            }
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    setSheetType(SheetType.ACCOUNT)
                },
                contentPadding = PaddingValues(0.dp),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = stringResource(
                        id = R.string.account_filter_chip_label,
                        selectedAccountCount
                    ),
                    modifier = Modifier.padding(10.dp),
                )
            }
        }
        BottomAppBar(
            contentPadding = PaddingValues(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        setSheetType(SheetType.CURRENCY)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    Text(text = selectedCurrencySymbol, fontSize = TextUnit(22f, TextUnitType.Sp))
                }
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        setSheetType(SheetType.DATE_RANGE)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    Icon(
                        imageVector = TablerIcons.CalendarEvent,
                        contentDescription = "select time period"
                    )
                }
                Row(
                    modifier = Modifier
                        .weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    FloatingActionButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            addTransaction()
                        }
                    ) {
                        Icon(
                            imageVector = TablerIcons.Plus,
                            contentDescription = "add new transaction"
                        )
                    }
                }
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSearchClick()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    Icon(
                        imageVector = TablerIcons.Search,
                        contentDescription = "search"
                    )
                }
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        setSheetType(SheetType.MORE_OPTIONS)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    Icon(
                        imageVector = TablerIcons.DotsVertical,
                        contentDescription = "more"
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionScreenBottomAppBar(
    title: String,
    subTitle: String,
    selectedCurrencySymbol: String,
    sortField: String?,
    sortDirection: SortDirection,
    filterAmount: String,
    selectedAccountCount: String,
    selectedCategoryCount: String,
    selectedCounterPartyCount: String,
    counterPartyFilterVisible: Boolean,
    selectedMethodCount: String,
    selectedTransactionTypeCount: Int,
    addTransaction: () -> Unit,
    setSheetType: (SheetType) -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    val sortIcon by remember(key1 = sortDirection) {
        mutableStateOf(
            if (sortDirection == SortDirection.DESC) {
                TablerIcons.SortDescending
            } else {
                TablerIcons.SortAscending
            }
        )
    }

    val sortedBy by remember(key1 = sortField) {
        mutableStateOf(sortField ?: "")
    }

    Column {
        FilterContainer(
            modifier = Modifier.padding(
                top = 10.dp,
                bottom = 10.dp
            ),
        ) {
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    setSheetType(SheetType.SORT)
                },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                shape = MaterialTheme.shapes.small
            ) {
                Icon(
                    imageVector = sortIcon,
                    contentDescription = "sort direction icon"
                )
                Text(
                    text = stringResource(id = R.string.sort_by_chip_label, sortedBy),
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    setSheetType(SheetType.TYPE)
                },
                contentPadding = PaddingValues(0.dp),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = stringResource(
                        id = R.string.transaction_type_filter_chip_label,
                        selectedTransactionTypeCount
                    ),
                    modifier = Modifier.padding(10.dp),
                )
            }
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    setSheetType(SheetType.AMOUNT)
                },
                contentPadding = PaddingValues(0.dp),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = stringResource(id = R.string.amount_filter_chip_label, filterAmount),
                    modifier = Modifier.padding(10.dp),
                )
            }
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    setSheetType(SheetType.CATEGORY)
                },
                contentPadding = PaddingValues(0.dp),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = stringResource(
                        id = R.string.category_filter_chip_label,
                        selectedCategoryCount
                    ),
                    modifier = Modifier.padding(10.dp),
                )
            }
            if (counterPartyFilterVisible) {
                FilledTonalButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        setSheetType(SheetType.COUNTERPARTY)
                    },
                    contentPadding = PaddingValues(0.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = stringResource(
                            id = R.string.counter_party_filter_chip_label,
                            selectedCounterPartyCount
                        ),
                        modifier = Modifier.padding(10.dp),
                    )
                }
            }
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    setSheetType(SheetType.METHOD)
                },
                contentPadding = PaddingValues(0.dp),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = stringResource(
                        id = R.string.method_filter_chip_label,
                        selectedMethodCount
                    ),
                    modifier = Modifier.padding(10.dp),
                )
            }
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    setSheetType(SheetType.ACCOUNT)
                },
                contentPadding = PaddingValues(0.dp),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = stringResource(
                        id = R.string.account_filter_chip_label,
                        selectedAccountCount
                    ),
                    modifier = Modifier.padding(10.dp),
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(20.dp)
                )
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(all = 10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = subTitle,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.alpha(0.8f)
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 66.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        setSheetType(SheetType.MORE_OPTIONS)
                    },
                ) {
                    Icon(
                        imageVector = TablerIcons.DotsVertical,
                        contentDescription = "more"
                    )
                }
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        setSheetType(SheetType.DATE_RANGE)
                    },
                ) {
                    Icon(
                        imageVector = TablerIcons.CalendarEvent,
                        contentDescription = "select time period"
                    )
                }
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        setSheetType(SheetType.CURRENCY)
                    },
                ) {
                    Text(
                        text = selectedCurrencySymbol,
                        fontSize = TextUnit(22f, TextUnitType.Sp)
                    )
                }
                FloatingActionButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        addTransaction()
                    }
                ) {
                    Icon(
                        imageVector = TablerIcons.Plus,
                        contentDescription = "add new transaction"
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun TransactionScreenBottomAppBarPreview() {
    TransactionScreenBottomAppBar(
        selectedCurrencySymbol = Currency.getInstance("INR").symbol,
        sortField = "transactionDateTime",
        sortDirection = SortDirection.DESC,
        filterAmount = "3000+",
        counterPartyFilterVisible = true,
        selectedAccountCount = "1",
        selectedCategoryCount = "1",
        selectedCounterPartyCount = "1",
        selectedMethodCount = "1",
        selectedTransactionTypeCount = 2,
        addTransaction = {},
        onSearchClick = {},
        setSheetType = {},
    )
}