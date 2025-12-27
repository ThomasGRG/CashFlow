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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.ArrowRight
import compose.icons.tablericons.CalendarEvent
import compose.icons.tablericons.DatabaseImport
import compose.icons.tablericons.FileImport
import compose.icons.tablericons.Search
import compose.icons.tablericons.SortAscending
import compose.icons.tablericons.SortDescending
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.enums.SheetType
import jp.ikigai.cash.flow.data.enums.SortDirection
import jp.ikigai.cash.flow.ui.components.common.AnimatedToggleSelectIcon
import jp.ikigai.cash.flow.ui.components.common.CustomFloatingActionButton
import jp.ikigai.cash.flow.ui.components.common.FilterContainer
import jp.ikigai.cash.flow.ui.screens.common.restore.ImportScreenAnimatedTitleContent

@Composable
fun ImportBackupScreenBottomAppBar(
    enabled: Boolean,
    navigateBack: () -> Unit,
    navigateToNext: () -> Unit,
    isSelectTransactionsScreen: Boolean,
    importEnabled: Boolean,
    dataLoaded: Boolean,
    allSelected: Boolean,
    sortDirection: SortDirection,
    filterAmount: String,
    selectedCurrencyCount: String,
    selectedTransactionTypeCount: Int,
    onSortClick: () -> Unit,
    onSearchClick: () -> Unit,
    onToggleSelectClick: () -> Unit,
    actionButtonClick: () -> Unit,
    setSheetType: (SheetType) -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    Column {
        AnimatedVisibility(
            visible = dataLoaded && isSelectTransactionsScreen,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
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
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    shape = MaterialTheme.shapes.small,
                    enabled = enabled
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
                        setSheetType(SheetType.AMOUNT)
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
                        setSheetType(SheetType.CURRENCY)
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
                        .fillMaxSize(),
                ) {
                    Icon(
                        imageVector = TablerIcons.ArrowLeft,
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
                                setSheetType(SheetType.DATE_RANGE)
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
                            Icon(
                                imageVector = TablerIcons.Search,
                                contentDescription = "search"
                            )
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
                                imageVector = TablerIcons.ArrowRight,
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

@Composable
fun ImportBackupScreenBottomAppBar(
    currentPage: Int,
    headers: List<Int>,
    subHeaders: List<String>,
    dateRangeStringRes: Int,
    startDateString: String,
    endDateString: String,
    enabled: Boolean,
    navigateBack: () -> Unit,
    navigateToNext: () -> Unit,
    isSelectTransactionsScreen: Boolean,
    importEnabled: Boolean,
    dataLoaded: Boolean,
    allSelected: Boolean,
    sortDirection: SortDirection,
    filterAmount: String,
    selectedCurrencyCount: String,
    selectedTransactionTypeCount: Int,
    onSortClick: () -> Unit,
    onToggleSelectClick: () -> Unit,
    actionButtonClick: () -> Unit,
    setSheetType: (SheetType) -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    Column {
        AnimatedVisibility(
            visible = dataLoaded && isSelectTransactionsScreen,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            FilterContainer(
                modifier = Modifier.padding(bottom = 10.dp),
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
                        setSheetType(SheetType.AMOUNT)
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
                        setSheetType(SheetType.CURRENCY)
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(20.dp)
                )
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(all = 10.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            if (!dataLoaded) {
                Text(
                    text = stringResource(R.string.import_transactions_label),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = stringResource(R.string.select_backup_label),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.alpha(0.8f),
                )
            } else {
                ImportScreenAnimatedTitleContent(
                    currentPage = currentPage,
                    headers = headers,
                    subHeaders = subHeaders,
                    dateRangeStringRes = dateRangeStringRes,
                    startDateString = startDateString,
                    endDateString = endDateString,
                    titleStyle = MaterialTheme.typography.titleLarge,
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(66.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                ) {
                    IconButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            navigateBack()
                        },
                    ) {
                        Icon(
                            imageVector = TablerIcons.ArrowLeft,
                            contentDescription = "navigate back"
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .weight(1.5f)
                        .fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    AnimatedVisibility(
                        visible = dataLoaded && isSelectTransactionsScreen,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        IconButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                setSheetType(SheetType.DATE_RANGE)
                            },
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
                        .weight(1.5f)
                        .fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    AnimatedVisibility(
                        visible = dataLoaded && isSelectTransactionsScreen,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut(),
                    ) {
                        IconButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onToggleSelectClick()
                            },
                            enabled = enabled
                        ) {
                            AnimatedToggleSelectIcon(deselectVisible = allSelected)
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                ) {
                    AnimatedVisibility(
                        visible = dataLoaded && !isSelectTransactionsScreen,
                        enter = fadeIn() + expandHorizontally(),
                        exit = fadeOut() + shrinkHorizontally(),
                    ) {
                        IconButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                navigateToNext()
                            },
                        ) {
                            Icon(
                                imageVector = TablerIcons.ArrowRight,
                                contentDescription = "navigate to next screen"
                            )
                        }
                    }
                    AnimatedVisibility(
                        visible = dataLoaded && isSelectTransactionsScreen,
                        enter = fadeIn() + expandHorizontally(expandFrom = Alignment.Start),
                        exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.Start),
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
                        enter = fadeIn() + expandHorizontally(),
                        exit = fadeOut() + shrinkHorizontally(),
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
            }
        }
    }
}

@Preview
@Composable
fun ImportBackupScreenBottomAppBarPreview() {
    Column {
        ImportBackupScreenBottomAppBar(
            enabled = true,
            navigateBack = {},
            navigateToNext = {},
            isSelectTransactionsScreen = false,
            allSelected = true,
            importEnabled = true,
            dataLoaded = false,
            sortDirection = SortDirection.DESC,
            selectedCurrencyCount = "2",
            selectedTransactionTypeCount = 2,
            filterAmount = "3000+",
            onSortClick = {},
            onSearchClick = {},
            onToggleSelectClick = {},
            actionButtonClick = {},
            setSheetType = {},
        )
        ImportBackupScreenBottomAppBar(
            enabled = false,
            navigateBack = {},
            navigateToNext = {},
            isSelectTransactionsScreen = true,
            allSelected = false,
            importEnabled = true,
            dataLoaded = true,
            sortDirection = SortDirection.DESC,
            selectedCurrencyCount = "2",
            selectedTransactionTypeCount = 2,
            filterAmount = "3000+",
            onSortClick = {},
            onSearchClick = {},
            onToggleSelectClick = {},
            actionButtonClick = {},
            setSheetType = {},
        )
    }
}