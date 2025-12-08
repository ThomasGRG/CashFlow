package jp.ikigai.cash.flow.ui.components.bottombars

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.enums.SheetType
import jp.ikigai.cash.flow.data.enums.SortDirection
import jp.ikigai.cash.flow.ui.components.common.AnimatedToggleSelectIcon
import jp.ikigai.cash.flow.ui.components.common.CustomFloatingActionButton
import jp.ikigai.cash.flow.ui.components.common.FilterContainer

@Composable
fun MigrateMethodScreenBottomAppBar(
    navigateBack: () -> Unit,
    enabled: Boolean,
    migrateEnabled: Boolean,
    allSelected: Boolean,
    sortDirection: SortDirection,
    filterAmount: String,
    selectedCurrencyCount: String,
    selectedAccountCount: String,
    selectedCategoryCount: String,
    selectedCounterPartyCount: String,
    counterPartyFilterVisible: Boolean,
    selectedTransactionTypeCount: Int,
    onSortClick: () -> Unit,
    onSearchClick: () -> Unit,
    onToggleSelectClick: () -> Unit,
    setSheetType: (SheetType) -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    Column {
        FilterContainer(
            padding = PaddingValues(
                top = 6.dp,
                bottom = 10.dp
            )
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
                    setSheetType(SheetType.TYPE)
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
                    setSheetType(SheetType.AMOUNT)
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
                    setSheetType(SheetType.CURRENCY)
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
                    setSheetType(SheetType.CATEGORY)
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
            if (counterPartyFilterVisible) {
                FilledTonalButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        setSheetType(SheetType.COUNTERPARTY)
                    },
                    enabled = enabled,
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
                    setSheetType(SheetType.ACCOUNT)
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
                        setSheetType(SheetType.DATE_RANGE)
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
                            setSheetType(SheetType.METHOD)
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

@Composable
fun MigrateMethodScreenBottomAppBar(
    title: String,
    subTitle: String,
    navigateBack: () -> Unit,
    enabled: Boolean,
    migrateEnabled: Boolean,
    allSelected: Boolean,
    sortDirection: SortDirection,
    filterAmount: String,
    selectedCurrencyCount: String,
    selectedAccountCount: String,
    selectedCategoryCount: String,
    selectedCounterPartyCount: String,
    counterPartyFilterVisible: Boolean,
    selectedTransactionTypeCount: Int,
    onSortClick: () -> Unit,
    onToggleSelectClick: () -> Unit,
    setSheetType: (SheetType) -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    Column {
        FilterContainer(
            modifier = Modifier.padding(bottom = 10.dp),
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
                    setSheetType(SheetType.TYPE)
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
                    setSheetType(SheetType.AMOUNT)
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
                    setSheetType(SheetType.CURRENCY)
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
                    setSheetType(SheetType.CATEGORY)
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
            if (counterPartyFilterVisible) {
                FilledTonalButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        setSheetType(SheetType.COUNTERPARTY)
                    },
                    enabled = enabled,
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
                    setSheetType(SheetType.ACCOUNT)
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
                        navigateBack()
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "navigate back"
                    )
                }
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        setSheetType(SheetType.DATE_RANGE)
                    },
                    enabled = enabled,
                ) {
                    Icon(
                        imageVector = TablerIcons.CalendarEvent,
                        contentDescription = "select time period"
                    )
                }
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleSelectClick()
                    },
                    enabled = enabled,
                ) {
                    AnimatedToggleSelectIcon(deselectVisible = allSelected)
                }
                CustomFloatingActionButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        setSheetType(SheetType.METHOD)
                    },
                    enabled = migrateEnabled,
                ) {
                    Icon(
                        imageVector = TablerIcons.Replace,
                        contentDescription = "migrate transactions"
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun MigrateMethodScreenBottomAppBarPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) {
        MigrateMethodScreenBottomAppBar(
            navigateBack = {},
            enabled = true,
            migrateEnabled = true,
            allSelected = true,
            sortDirection = SortDirection.DESC,
            filterAmount = "3000+",
            selectedCurrencyCount = "1",
            selectedAccountCount = "1",
            selectedCategoryCount = "1",
            selectedCounterPartyCount = "1",
            counterPartyFilterVisible = true,
            selectedTransactionTypeCount = 2,
            onSortClick = {},
            onSearchClick = {},
            onToggleSelectClick = {},
            setSheetType = {},
        )
        MigrateMethodScreenBottomAppBar(
            navigateBack = {},
            enabled = false,
            migrateEnabled = false,
            allSelected = false,
            sortDirection = SortDirection.DESC,
            filterAmount = "3000+",
            selectedCurrencyCount = "1",
            selectedAccountCount = "1",
            selectedCategoryCount = "1",
            selectedCounterPartyCount = "1",
            counterPartyFilterVisible = true,
            selectedTransactionTypeCount = 2,
            onSortClick = {},
            onSearchClick = {},
            onToggleSelectClick = {},
            setSheetType = {},
        )
    }
}