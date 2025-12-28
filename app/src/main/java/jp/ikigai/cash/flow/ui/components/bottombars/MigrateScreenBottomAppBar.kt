package jp.ikigai.cash.flow.ui.components.bottombars

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.CalendarEvent
import compose.icons.tablericons.Replace
import compose.icons.tablericons.Search
import compose.icons.tablericons.SortAscending
import compose.icons.tablericons.SortDescending
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.enums.SheetType
import jp.ikigai.cash.flow.data.enums.SortDirection
import jp.ikigai.cash.flow.ui.components.common.AnimatedToggleSelectIcon
import jp.ikigai.cash.flow.ui.components.common.CustomFloatingActionButton
import jp.ikigai.cash.flow.ui.components.common.FilterContainer

@Composable
fun MigrateScreenBottomAppBar(
    navigateBack: () -> Unit,
    enabled: Boolean,
    migrateEnabled: Boolean,
    allSelected: Boolean,
    sortDirection: SortDirection,
    filterAmount: String,
    selectedAccountCount: String,
    selectedCategoryCount: String?,
    selectedCounterPartyCount: String?,
    selectedCurrencyCount: String,
    selectedMethodCount: String?,
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
            if (selectedCategoryCount != null) {
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
            }
            if (selectedCounterPartyCount != null) {
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
            if (selectedMethodCount != null) {
                FilledTonalButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        setSheetType(SheetType.METHOD)
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        navigateBack()
                    },
                    modifier = Modifier.defaultMinSize(minHeight = 70.dp, minWidth = 70.dp),
                ) {
                    Icon(
                        imageVector = TablerIcons.ArrowLeft,
                        contentDescription = "navigate back"
                    )
                }
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        setSheetType(SheetType.DATE_RANGE)
                    },
                    enabled = enabled,
                    modifier = Modifier.defaultMinSize(minHeight = 70.dp, minWidth = 70.dp),
                ) {
                    Icon(
                        imageVector = TablerIcons.CalendarEvent,
                        contentDescription = "select time period"
                    )
                }
                CustomFloatingActionButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        setSheetType(SheetType.CATEGORY)
                    },
                    enabled = migrateEnabled
                ) {
                    Icon(
                        imageVector = TablerIcons.Replace,
                        contentDescription = "migrate transactions"
                    )
                }
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSearchClick()
                    },
                    enabled = enabled,
                    modifier = Modifier.defaultMinSize(minHeight = 70.dp, minWidth = 70.dp),
                ) {
                    Icon(
                        imageVector = TablerIcons.Search,
                        contentDescription = "search"
                    )
                }
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleSelectClick()
                    },
                    enabled = enabled,
                    modifier = Modifier.defaultMinSize(minHeight = 70.dp, minWidth = 70.dp),
                ) {
                    AnimatedToggleSelectIcon(deselectVisible = allSelected)
                }
            }
        }
    }
}

@Composable
fun MigrateScreenBottomAppBar(
    navigateBack: () -> Unit,
    title: String,
    subTitle: String,
    enabled: Boolean,
    migrateEnabled: Boolean,
    allSelected: Boolean,
    sortDirection: SortDirection,
    filterAmount: String,
    selectedAccountCount: String,
    selectedCategoryCount: String?,
    selectedCounterPartyCount: String?,
    selectedCurrencyCount: String,
    selectedMethodCount: String?,
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
            if (selectedCategoryCount != null) {
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
            }
            if (selectedCounterPartyCount != null) {
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
            if (selectedMethodCount != null) {
                FilledTonalButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        setSheetType(SheetType.METHOD)
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
                    modifier = Modifier.defaultMinSize(minHeight = 70.dp, minWidth = 70.dp),
                ) {
                    Icon(
                        imageVector = TablerIcons.ArrowLeft,
                        contentDescription = "navigate back"
                    )
                }
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        setSheetType(SheetType.DATE_RANGE)
                    },
                    enabled = enabled,
                    modifier = Modifier.defaultMinSize(minHeight = 70.dp, minWidth = 70.dp),
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
                    modifier = Modifier.defaultMinSize(minHeight = 70.dp, minWidth = 70.dp),
                ) {
                    AnimatedToggleSelectIcon(deselectVisible = allSelected)
                }
                CustomFloatingActionButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (selectedCategoryCount == null) {
                            setSheetType(SheetType.CATEGORY)
                        } else if (selectedCounterPartyCount == null) {
                            setSheetType(SheetType.COUNTERPARTY)
                        } else {
                            setSheetType(SheetType.METHOD)
                        }
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