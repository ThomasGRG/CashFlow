package jp.ikigai.cash.flow.ui.components.bottombars

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import compose.icons.tablericons.ArrowNarrowDown
import compose.icons.tablericons.ArrowNarrowUp
import compose.icons.tablericons.CalendarEvent
import compose.icons.tablericons.DatabaseImport
import compose.icons.tablericons.FileImport
import io.realm.kotlin.query.Sort
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.ui.components.common.AnimatedToggleSelectIcon
import jp.ikigai.cash.flow.ui.components.common.CustomFloatingActionButton

@Composable
fun ImportBackupScreenRoundedBottomBar(
    loading: Boolean,
    enabled: Boolean,
    navigateBack: () -> Unit,
    navigateToNext: () -> Unit,
    isSelectTransactionsScreen: Boolean,
    importEnabled: Boolean,
    dataLoaded: Boolean,
    allSelected: Boolean,
    sortDirection: Sort,
    filterAmount: String,
    selectedCurrencyCount: String,
    selectedTransactionTypeCount: Int,
    onSortClick: () -> Unit,
    onFilterByAmountClick: () -> Unit,
    onFilterByTypeClick: () -> Unit,
    onFilterByCurrencyClick: () -> Unit,
    onSearchClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onToggleSelectClick: () -> Unit,
    actionButtonClick: () -> Unit
) {
    val haptics = LocalHapticFeedback.current

    Column {
        AnimatedVisibility(
            visible = dataLoaded && isSelectTransactionsScreen,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
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
                    shape = MaterialTheme.shapes.small,
                    enabled = enabled
                ) {
                    Icon(
                        imageVector = if (sortDirection == Sort.DESCENDING) {
                            TablerIcons.ArrowNarrowDown
                        } else {
                            TablerIcons.ArrowNarrowUp
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
                    shape = MaterialTheme.shapes.small,
                    enabled = enabled
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
                    shape = MaterialTheme.shapes.small,
                    enabled = enabled
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
                    contentPadding = PaddingValues(0.dp),
                    shape = MaterialTheme.shapes.small,
                    enabled = enabled
                ) {
                    Text(
                        text = stringResource(
                            id = R.string.currency_filter_chip_label,
                            selectedCurrencyCount
                        ),
                        modifier = Modifier.padding(10.dp),
                    )
                }
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
                        .fillMaxSize(),
                    enabled = !loading
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "navigate back"
                    )
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    AnimatedVisibility(
                        visible = dataLoaded && isSelectTransactionsScreen,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        IconButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onCalendarClick()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize(),
                            enabled = enabled
                        ) {
                            Icon(
                                imageVector = TablerIcons.CalendarEvent,
                                contentDescription = "select time period"
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    AnimatedVisibility(
                        visible = dataLoaded && isSelectTransactionsScreen,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        CustomFloatingActionButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                actionButtonClick()
                            },
                            enabled = enabled && importEnabled
                        ) {
                            Icon(
                                imageVector = TablerIcons.DatabaseImport,
                                contentDescription = "import data"
                            )
                        }
                    }
                    AnimatedVisibility(
                        visible = !dataLoaded,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        CustomFloatingActionButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                actionButtonClick()
                            },
                            enabled = true
                        ) {
                            Icon(
                                imageVector = TablerIcons.FileImport,
                                contentDescription = "select backup file"
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    AnimatedVisibility(
                        visible = dataLoaded && isSelectTransactionsScreen,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        IconButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSearchClick()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize(),
                            enabled = enabled
                        ) {
                            Icon(imageVector = Icons.Outlined.Search, contentDescription = "search")
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    AnimatedVisibility(
                        visible = dataLoaded && !isSelectTransactionsScreen,
                        enter = fadeIn() + expandHorizontally(),
                        exit = fadeOut() + shrinkHorizontally()
                    ) {
                        IconButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                navigateToNext()
                            },
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "navigate to next screen"
                            )
                        }
                    }
                    AnimatedVisibility(
                        visible = dataLoaded && isSelectTransactionsScreen,
                        enter = fadeIn() + expandHorizontally(expandFrom = Alignment.Start),
                        exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.Start)
                    ) {
                        IconButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onToggleSelectClick()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize(),
                            enabled = enabled
                        ) {
                            AnimatedToggleSelectIcon(deselectVisible = allSelected)
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ImportBackupScreenRoundedBottomBarPreview() {
    Column {
        ImportBackupScreenRoundedBottomBar(
            enabled = true,
            loading = false,
            navigateBack = {},
            navigateToNext = {},
            isSelectTransactionsScreen = false,
            allSelected = true,
            importEnabled = true,
            dataLoaded = false,
            sortDirection = Sort.DESCENDING,
            selectedCurrencyCount = "2",
            selectedTransactionTypeCount = 2,
            filterAmount = "3000+",
            onSortClick = {},
            onFilterByAmountClick = {},
            onFilterByTypeClick = {},
            onFilterByCurrencyClick = {},
            onSearchClick = {},
            onCalendarClick = {},
            onToggleSelectClick = {},
            actionButtonClick = {}
        )
        ImportBackupScreenRoundedBottomBar(
            enabled = false,
            loading = true,
            navigateBack = {},
            navigateToNext = {},
            isSelectTransactionsScreen = true,
            allSelected = false,
            importEnabled = true,
            dataLoaded = true,
            sortDirection = Sort.DESCENDING,
            selectedCurrencyCount = "2",
            selectedTransactionTypeCount = 2,
            filterAmount = "3000+",
            onSortClick = {},
            onFilterByAmountClick = {},
            onFilterByTypeClick = {},
            onFilterByCurrencyClick = {},
            onSearchClick = {},
            onCalendarClick = {},
            onToggleSelectClick = {},
            actionButtonClick = {}
        )
    }
}