package jp.ikigai.cash.flow.ui.components.bottombars

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.CalendarEvent
import compose.icons.tablericons.Replace
import compose.icons.tablericons.SortAscending
import compose.icons.tablericons.SortDescending
import io.objectbox.query.QueryBuilder
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.ui.components.common.AnimatedToggleSelectIcon
import jp.ikigai.cash.flow.ui.components.common.CustomFloatingActionButton

@Composable
fun MigrateCounterPartyScreenRoundedBottomBar(
    navigateBack: () -> Unit,
    enabled: Boolean,
    migrateEnabled: Boolean,
    allSelected: Boolean,
    sortFlags: Int,
    filterAmount: String,
    selectedCurrencyCount: String,
    selectedAccountCount: String,
    selectedCategoryCount: String,
    selectedMethodCount: String,
    selectedTransactionTypeCount: Int,
    onSortClick: () -> Unit,
    onFilterByAmountClick: () -> Unit,
    onFilterByTypeClick: () -> Unit,
    onFilterByCurrencyClick: () -> Unit,
    onFilterByCategoryClick: () -> Unit,
    onFilterByMethodClick: () -> Unit,
    onFilterBySourceClick: () -> Unit,
    onCalendarClick: () -> Unit,
    migrateTransactions: () -> Unit,
    onSearchClick: () -> Unit,
    onToggleSelectClick: () -> Unit,
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
                enabled = enabled,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                shape = MaterialTheme.shapes.small
            ) {
                Icon(
                    imageVector = if (sortFlags == QueryBuilder.DESCENDING) {
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
                enabled = enabled,
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
                enabled = enabled,
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
                    onFilterByCurrencyClick()
                },
                enabled = enabled,
                contentPadding = PaddingValues(0.dp),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = stringResource(
                        id = R.string.currency_filter_chip_label,
                        selectedCurrencyCount
                    ),
                    modifier = Modifier.padding(10.dp),
                )
            }
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onFilterByCategoryClick()
                },
                enabled = enabled,
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
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onFilterByMethodClick()
                },
                enabled = enabled,
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
                enabled = enabled,
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
                        navigateBack()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "navigate back"
                    )
                }
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCalendarClick()
                    },
                    enabled = enabled,
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
                    CustomFloatingActionButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            migrateTransactions()
                        },
                        enabled = migrateEnabled
                    ) {
                        Icon(
                            imageVector = TablerIcons.Replace,
                            contentDescription = "migrate transactions"
                        )
                    }
                }
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSearchClick()
                    },
                    enabled = enabled,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    Icon(imageVector = Icons.Outlined.Search, contentDescription = "search")
                }
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleSelectClick()
                    },
                    enabled = enabled,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    AnimatedToggleSelectIcon(deselectVisible = allSelected)
                }
            }
        }
    }
}

@Preview
@Composable
fun MigrateCounterPartyScreenRoundedBottomBarPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) {
        MigrateCounterPartyScreenRoundedBottomBar(
            navigateBack = {},
            enabled = true,
            migrateEnabled = true,
            allSelected = true,
            sortFlags = QueryBuilder.DESCENDING,
            filterAmount = "3000+",
            selectedCurrencyCount = "1",
            selectedAccountCount = "1",
            selectedCategoryCount = "1",
            selectedMethodCount = "1",
            selectedTransactionTypeCount = 2,
            onSortClick = {},
            onFilterByAmountClick = {},
            onFilterByTypeClick = {},
            onFilterByCurrencyClick = {},
            onFilterByCategoryClick = {},
            onFilterByMethodClick = {},
            onFilterBySourceClick = {},
            onCalendarClick = {},
            migrateTransactions = {},
            onSearchClick = {},
            onToggleSelectClick = {},
        )
        MigrateCounterPartyScreenRoundedBottomBar(
            navigateBack = {},
            enabled = false,
            migrateEnabled = false,
            allSelected = false,
            sortFlags = QueryBuilder.DESCENDING,
            filterAmount = "3000+",
            selectedCurrencyCount = "1",
            selectedAccountCount = "1",
            selectedCategoryCount = "1",
            selectedMethodCount = "1",
            selectedTransactionTypeCount = 2,
            onSortClick = {},
            onFilterByAmountClick = {},
            onFilterByTypeClick = {},
            onFilterByCurrencyClick = {},
            onFilterByCategoryClick = {},
            onFilterByMethodClick = {},
            onFilterBySourceClick = {},
            onCalendarClick = {},
            migrateTransactions = {},
            onSearchClick = {},
            onToggleSelectClick = {},
        )
    }
}