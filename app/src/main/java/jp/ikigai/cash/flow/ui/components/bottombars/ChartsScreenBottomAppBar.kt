package jp.ikigai.cash.flow.ui.components.bottombars

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.ChartBar
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.enums.ChartType
import jp.ikigai.cash.flow.ui.components.buttons.CustomOutlinedButton

@Composable
fun ChartsScreenBottomAppBar(
    enabled: Boolean,
    selectedChartType: ChartType,
    startYearMonthString: String,
    endYearMonthString: String,
    yearMonthRangeStringRes: Int,
    selectedCurrency: String,
    selectedAccount: String,
    selectedCategory: String,
    selectedCounterParty: String,
    selectedMethod: String,
    navigateBack: () -> Unit,
    onSelectChartClick: () -> Unit,
    onSelectCurrencyClick: () -> Unit,
    onSelectAccountClick: () -> Unit,
    onSelectCategoryClick: () -> Unit,
    onSelectCounterPartyClick: () -> Unit,
    onSelectMethodClick: () -> Unit,
    onSelectYearMonthClick: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    val accountVisible by remember(key1 = selectedChartType) {
        mutableStateOf(selectedChartType == ChartType.ACCOUNT_TRENDS_LINE_CHART)
    }

    val categoryVisible by remember(key1 = selectedChartType) {
        mutableStateOf(selectedChartType == ChartType.CATEGORY_TRENDS_LINE_CHART)
    }

    val counterPartyVisible by remember(key1 = selectedChartType) {
        mutableStateOf(selectedChartType == ChartType.COUNTERPARTY_TRENDS_LINE_CHART)
    }

    val methodVisible by remember(key1 = selectedChartType) {
        mutableStateOf(selectedChartType == ChartType.METHOD_TRENDS_LINE_CHART)
    }

    Column {
        CustomOutlinedButton(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(horizontal = 10.dp),
            enabled = enabled,
            value = stringResource(id = selectedChartType.label),
            label = stringResource(id = R.string.chart_type_field_label),
            placeHolder = "",
            leadingIcon = TablerIcons.ChartBar,
            onClick = onSelectChartClick
        )
        Row(
            modifier = Modifier
                .padding(top = 6.dp, start = 10.dp, end = 10.dp, bottom = 6.dp)
                .horizontalScroll(
                    rememberScrollState()
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSelectYearMonthClick()
                },
                enabled = enabled,
                contentPadding = PaddingValues(0.dp),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = stringResource(
                        id = yearMonthRangeStringRes,
                        startYearMonthString,
                        endYearMonthString
                    ),
                    modifier = Modifier.padding(10.dp),
                )
            }
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSelectCurrencyClick()
                },
                enabled = enabled,
                contentPadding = PaddingValues(0.dp),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = stringResource(
                        id = R.string.selected_currency_chip_label,
                        selectedCurrency
                    ),
                    modifier = Modifier.padding(10.dp),
                )
            }
            if (accountVisible) {
                FilledTonalButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSelectAccountClick()
                    },
                    enabled = enabled,
                    contentPadding = PaddingValues(0.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = stringResource(
                            id = R.string.selected_account_filter_chip_label,
                            selectedAccount
                        ),
                        modifier = Modifier.padding(10.dp),
                    )
                }
            }
            if (categoryVisible) {
                FilledTonalButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSelectCategoryClick()
                    },
                    enabled = enabled,
                    contentPadding = PaddingValues(0.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = stringResource(
                            id = R.string.selected_category_filter_chip_label,
                            selectedCategory
                        ),
                        modifier = Modifier.padding(10.dp),
                    )
                }
            }
            if (counterPartyVisible) {
                FilledTonalButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSelectCounterPartyClick()
                    },
                    enabled = enabled,
                    contentPadding = PaddingValues(0.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = stringResource(
                            id = R.string.selected_counter_party_filter_chip_label,
                            selectedCounterParty
                        ),
                        modifier = Modifier.padding(10.dp),
                    )
                }
            }
            if (methodVisible) {
                FilledTonalButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSelectMethodClick()
                    },
                    enabled = enabled,
                    contentPadding = PaddingValues(0.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = stringResource(
                            id = R.string.selected_method_filter_chip_label,
                            selectedMethod
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
                        .fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "navigate back"
                    )
                }
                Spacer(
                    modifier = Modifier.weight(4f)
                )
            }
        }
    }
}

@Composable
fun ChartsScreenBottomAppBar(
    title: String,
    enabled: Boolean,
    selectedChartType: ChartType,
    startYearMonthString: String,
    endYearMonthString: String,
    yearMonthRangeStringRes: Int,
    selectedCurrency: String,
    selectedAccount: String,
    selectedCategory: String,
    selectedCounterParty: String,
    selectedMethod: String,
    navigateBack: () -> Unit,
    onSelectChartClick: () -> Unit,
    onSelectCurrencyClick: () -> Unit,
    onSelectAccountClick: () -> Unit,
    onSelectCategoryClick: () -> Unit,
    onSelectCounterPartyClick: () -> Unit,
    onSelectMethodClick: () -> Unit,
    onSelectYearMonthClick: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    val accountVisible by remember(key1 = selectedChartType) {
        mutableStateOf(selectedChartType == ChartType.ACCOUNT_TRENDS_LINE_CHART)
    }

    val categoryVisible by remember(key1 = selectedChartType) {
        mutableStateOf(selectedChartType == ChartType.CATEGORY_TRENDS_LINE_CHART)
    }

    val counterPartyVisible by remember(key1 = selectedChartType) {
        mutableStateOf(selectedChartType == ChartType.COUNTERPARTY_TRENDS_LINE_CHART)
    }

    val methodVisible by remember(key1 = selectedChartType) {
        mutableStateOf(selectedChartType == ChartType.METHOD_TRENDS_LINE_CHART)
    }

    Column {
        CustomOutlinedButton(
            modifier = Modifier.height(IntrinsicSize.Min),
            enabled = enabled,
            value = stringResource(id = selectedChartType.label),
            label = stringResource(id = R.string.chart_type_field_label),
            placeHolder = "",
            leadingIcon = TablerIcons.ChartBar,
            onClick = onSelectChartClick
        )
        Row(
            modifier = Modifier
                .padding(top = 6.dp, bottom = 6.dp)
                .horizontalScroll(
                    rememberScrollState()
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSelectYearMonthClick()
                },
                enabled = enabled,
                contentPadding = PaddingValues(0.dp),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = stringResource(
                        id = yearMonthRangeStringRes,
                        startYearMonthString,
                        endYearMonthString
                    ),
                    modifier = Modifier.padding(10.dp),
                )
            }
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSelectCurrencyClick()
                },
                enabled = enabled,
                contentPadding = PaddingValues(0.dp),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = stringResource(
                        id = R.string.selected_currency_chip_label,
                        selectedCurrency
                    ),
                    modifier = Modifier.padding(10.dp),
                )
            }
            if (accountVisible) {
                FilledTonalButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSelectAccountClick()
                    },
                    enabled = enabled,
                    contentPadding = PaddingValues(0.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = stringResource(
                            id = R.string.selected_account_filter_chip_label,
                            selectedAccount
                        ),
                        modifier = Modifier.padding(10.dp),
                    )
                }
            }
            if (categoryVisible) {
                FilledTonalButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSelectCategoryClick()
                    },
                    enabled = enabled,
                    contentPadding = PaddingValues(0.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = stringResource(
                            id = R.string.selected_category_filter_chip_label,
                            selectedCategory
                        ),
                        modifier = Modifier.padding(10.dp),
                    )
                }
            }
            if (counterPartyVisible) {
                FilledTonalButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSelectCounterPartyClick()
                    },
                    enabled = enabled,
                    contentPadding = PaddingValues(0.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = stringResource(
                            id = R.string.selected_counter_party_filter_chip_label,
                            selectedCounterParty
                        ),
                        modifier = Modifier.padding(10.dp),
                    )
                }
            }
            if (methodVisible) {
                FilledTonalButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSelectMethodClick()
                    },
                    enabled = enabled,
                    contentPadding = PaddingValues(0.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = stringResource(
                            id = R.string.selected_method_filter_chip_label,
                            selectedMethod
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
                .padding(all = 10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = stringResource(id = R.string.charts_subtext_label),
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
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
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
            }
        }
    }
}