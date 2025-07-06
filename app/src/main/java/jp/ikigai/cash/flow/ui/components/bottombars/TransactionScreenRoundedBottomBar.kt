package jp.ikigai.cash.flow.ui.components.bottombars

import android.icu.util.Currency
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.CalendarEvent
import compose.icons.tablericons.SortAscending
import compose.icons.tablericons.SortDescending
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.enums.SortDirection

@Composable
fun TransactionScreenRoundedBottomBar(
    selectedCurrencySymbol: String,
    sortDirection: SortDirection,
    filterAmount: String,
    selectedAccountCount: String,
    selectedCategoryCount: String,
    selectedCounterPartyCount: String,
    counterPartyFilterVisible: Boolean,
    selectedMethodCount: String,
    selectedTransactionTypeCount: Int,
    onSortClick: () -> Unit,
    onFilterByAmountClick: () -> Unit,
    onFilterByTypeClick: () -> Unit,
    onFilterByCategoryClick: () -> Unit,
    onFilterByCounterPartyClick: () -> Unit,
    onFilterByMethodClick: () -> Unit,
    onFilterBySourceClick: () -> Unit,
    onCurrencyClick: () -> Unit,
    onCalendarClick: () -> Unit,
    addTransaction: () -> Unit,
    onSearchClick: () -> Unit,
    onMoreClick: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    Column {
        Row(
            modifier = Modifier
                .padding(top = 6.dp, start = 10.dp, end = 10.dp, bottom = 0.dp)
                .horizontalScroll(
                    rememberScrollState()
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSortClick()
                },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                shape = MaterialTheme.shapes.small
            ) {
                Icon(
                    imageVector = if (sortDirection == SortDirection.DESC) {
                        TablerIcons.SortDescending
                    } else {
                        TablerIcons.SortAscending
                    },
                    contentDescription = "sort direction icon"
                )
                Text(
                    text = stringResource(id = R.string.sort_by_time_chip_label),
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onFilterByTypeClick()
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
                    onFilterByAmountClick()
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
                    onFilterByCategoryClick()
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
                        onFilterByCounterPartyClick()
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
                    onFilterByMethodClick()
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
                    onFilterBySourceClick()
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
        RoundedBottomBar {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCurrencyClick()
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
                        onCalendarClick()
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
                            imageVector = Icons.Filled.Add,
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
                    Icon(imageVector = Icons.Outlined.Search, contentDescription = "search")
                }
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onMoreClick()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "more")
                }
            }
        }
    }
}

@Preview
@Composable
fun TransactionScreenRoundedBottomBarPreview() {
    TransactionScreenRoundedBottomBar(
        selectedCurrencySymbol = Currency.getInstance("INR").symbol,
        sortDirection = SortDirection.DESC,
        filterAmount = "3000+",
        counterPartyFilterVisible = true,
        selectedAccountCount = "1",
        selectedCategoryCount = "1",
        selectedCounterPartyCount = "1",
        selectedMethodCount = "1",
        selectedTransactionTypeCount = 2,
        onSortClick = {},
        onFilterByAmountClick = {},
        onFilterByTypeClick = {},
        onFilterByCategoryClick = {},
        onFilterByCounterPartyClick = {},
        onFilterByMethodClick = {},
        onFilterBySourceClick = {},
        onCurrencyClick = {},
        onCalendarClick = {},
        addTransaction = {},
        onSearchClick = {},
        onMoreClick = {},
    )
}