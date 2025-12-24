package jp.ikigai.cash.flow.ui.components.bottomsheets

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import jp.ikigai.cash.flow.R
import java.time.Month
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MonthRangePickerSheet(
    start: YearMonth? = null,
    end: YearMonth? = null,
    filter: (YearMonth, YearMonth) -> Unit,
    reset: () -> Unit,
    dismiss: () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val haptics = LocalHapticFeedback.current

    val locale by remember(key1 = configuration) {
        mutableStateOf(configuration.locales[0])
    }

    val years by remember {
        mutableStateOf(
            (1900..2100).toList()
        )
    }

    val currentYearMonth by remember {
        mutableStateOf(YearMonth.now(ZoneId.systemDefault()))
    }

    var startYearMonth: YearMonth? by remember(key1 = start) {
        mutableStateOf(start)
    }

    var endYearMonth: YearMonth? by remember(key1 = end) {
        mutableStateOf(end)
    }

    val startYearMonthDisplayString by remember(key1 = startYearMonth) {
        mutableStateOf(
            startYearMonth?.let {
                "${it.month.getDisplayName(TextStyle.SHORT, locale)}, ${it.year}"
            }
        )
    }

    val startYearMonthStringPlaceholder = stringResource(R.string.start_month_placeholder_label)

    val endYearMonthDisplayString by remember(key1 = endYearMonth) {
        mutableStateOf(
            endYearMonth?.let {
                "${it.month.getDisplayName(TextStyle.SHORT, locale)}, ${it.year}"
            }
        )
    }

    val endYearMonthStringPlaceholder = stringResource(R.string.end_month_placeholder_label)

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        val year = startYearMonth?.year ?: (currentYearMonth.year - 1)
        listState.scrollToItem(
            years.indexOf(year).coerceAtLeast(0)
        )
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${startYearMonthDisplayString ?: startYearMonthStringPlaceholder} - ${endYearMonthDisplayString ?: endYearMonthStringPlaceholder}",
                style = MaterialTheme.typography.headlineSmall
            )
        }
        HorizontalDivider()
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
        )
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxHeight(0.7f)
                .padding(horizontal = 12.dp)
        ) {
            items(
                items = years,
                key = { year -> year }
            ) { year ->
                Column {
                    Text(
                        text = "$year",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 12.dp, bottom = 6.dp, top = 6.dp)
                    )

                    val rangeSelectionInfo by remember(startYearMonth, endYearMonth) {
                        mutableStateOf(
                            SelectedRangeInfo.calculateRangeInfo(
                                year = year,
                                startMonth = startYearMonth,
                                endMonth = endYearMonth
                            )
                        )
                    }

                    Year(
                        locale = locale,
                        year = year,
                        onMonthSelectionChange = { yearMonth ->
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (startYearMonth != null && endYearMonth != null) {
                                startYearMonth = yearMonth
                                endYearMonth = null
                            } else if (startYearMonth == null || yearMonth < startYearMonth) {
                                startYearMonth = yearMonth
                            } else {
                                endYearMonth = yearMonth
                            }
                        },
                        currentYearMonth = currentYearMonth,
                        startYearMonth = startYearMonth,
                        endYearMonth = endYearMonth,
                        maxSelectableYearMonth = currentYearMonth,
                        rangeSelectionInfo = rangeSelectionInfo
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    dismiss()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(35)
            ) {
                Text(text = stringResource(id = R.string.cancel_button_label))
            }
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    reset()
                    dismiss()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(35)
            ) {
                Text(text = stringResource(id = R.string.reset_button_label))
            }
            FilledTonalButton(
                enabled = startYearMonth != null && endYearMonth != null,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    dismiss()
                    filter(startYearMonth!!, endYearMonth!!)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(35)
            ) {
                Text(text = stringResource(id = R.string.filter_button_label))
            }
        }
    }
}

@Composable
fun MonthRangePickerLandscapeSheet(
    start: YearMonth? = null,
    end: YearMonth? = null,
    filter: (YearMonth, YearMonth) -> Unit,
    reset: () -> Unit,
    dismiss: () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val haptics = LocalHapticFeedback.current

    val locale by remember(key1 = configuration) {
        mutableStateOf(configuration.locales[0])
    }

    val years by remember {
        mutableStateOf(
            (1900..2100).toList()
        )
    }

    val currentYearMonth by remember {
        mutableStateOf(YearMonth.now(ZoneId.systemDefault()))
    }

    var startYearMonth: YearMonth? by remember(key1 = start) {
        mutableStateOf(start)
    }

    var endYearMonth: YearMonth? by remember(key1 = end) {
        mutableStateOf(end)
    }

    val startYearMonthDisplayString by remember(key1 = startYearMonth) {
        mutableStateOf(
            startYearMonth?.let {
                "${it.month.getDisplayName(TextStyle.SHORT, locale)}, ${it.year}"
            }
        )
    }

    val startYearMonthStringPlaceholder = stringResource(R.string.start_month_placeholder_label)

    val endYearMonthDisplayString by remember(key1 = endYearMonth) {
        mutableStateOf(
            endYearMonth?.let {
                "${it.month.getDisplayName(TextStyle.SHORT, locale)}, ${it.year}"
            }
        )
    }

    val endYearMonthStringPlaceholder = stringResource(R.string.end_month_placeholder_label)

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        val year = startYearMonth?.year ?: (currentYearMonth.year - 1)
        listState.scrollToItem(
            index = years.indexOf(year).coerceAtLeast(0),
            scrollOffset = 250,
        )
    }
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(start = 10.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.Start,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${startYearMonthDisplayString ?: startYearMonthStringPlaceholder} - ${endYearMonthDisplayString ?: endYearMonthStringPlaceholder}",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            FilledTonalButton(
                enabled = startYearMonth != null && endYearMonth != null,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    dismiss()
                    filter(startYearMonth!!, endYearMonth!!)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(35)
            ) {
                Text(text = stringResource(id = R.string.filter_button_label))
            }
            Spacer(modifier = Modifier.height(10.dp))
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    reset()
                    dismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(35)
            ) {
                Text(text = stringResource(id = R.string.reset_button_label))
            }
            Spacer(modifier = Modifier.height(10.dp))
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    dismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(35)
            ) {
                Text(text = stringResource(id = R.string.cancel_button_label))
            }
        }
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
                .padding(start = 10.dp, bottom = 10.dp, end = 10.dp, top = 8.dp),
        ) {
            items(
                items = years,
                key = { year -> year }
            ) { year ->
                Column {
                    Text(
                        text = "$year",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 12.dp, bottom = 6.dp, top = 6.dp)
                    )

                    val rangeSelectionInfo by remember(startYearMonth, endYearMonth) {
                        mutableStateOf(
                            SelectedRangeInfo.calculateRangeInfo(
                                year = year,
                                startMonth = startYearMonth,
                                endMonth = endYearMonth
                            )
                        )
                    }

                    Year(
                        locale = locale,
                        year = year,
                        onMonthSelectionChange = { yearMonth ->
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (startYearMonth != null && endYearMonth != null) {
                                startYearMonth = yearMonth
                                endYearMonth = null
                            } else if (startYearMonth == null || yearMonth < startYearMonth) {
                                startYearMonth = yearMonth
                            } else {
                                endYearMonth = yearMonth
                            }
                        },
                        currentYearMonth = currentYearMonth,
                        startYearMonth = startYearMonth,
                        endYearMonth = endYearMonth,
                        maxSelectableYearMonth = currentYearMonth,
                        rangeSelectionInfo = rangeSelectionInfo
                    )
                }
            }
        }
    }
}

@Composable
private fun Year(
    locale: Locale,
    year: Int,
    onMonthSelectionChange: (yearMonth: YearMonth) -> Unit,
    currentYearMonth: YearMonth,
    startYearMonth: YearMonth?,
    endYearMonth: YearMonth?,
    maxSelectableYearMonth: YearMonth,
    rangeSelectionInfo: SelectedRangeInfo?
) {
    val rangeSelectionActiveIndicatorContainerColor = MaterialTheme.colorScheme.secondaryContainer

    val rangeSelectionDrawModifier =
        if (rangeSelectionInfo != null) {
            Modifier.drawWithContent {
                drawRangeBackground(rangeSelectionInfo, rangeSelectionActiveIndicatorContainerColor)
                drawContent()
            }
        } else {
            Modifier
        }

    Column(
        modifier = Modifier.then(rangeSelectionDrawModifier),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .requiredHeight(34.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (monthValue in 1 until 5) {
                MonthChip(
                    locale = locale,
                    monthValue = monthValue,
                    year = year,
                    currentYearMonth = currentYearMonth,
                    startYearMonth = startYearMonth,
                    endYearMonth = endYearMonth,
                    maxSelectableYearMonth = maxSelectableYearMonth,
                    onClick = { month ->
                        onMonthSelectionChange(YearMonth.of(year, month))
                    }
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .requiredHeight(34.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (monthValue in 5 until 9) {
                MonthChip(
                    locale = locale,
                    monthValue = monthValue,
                    year = year,
                    currentYearMonth = currentYearMonth,
                    startYearMonth = startYearMonth,
                    endYearMonth = endYearMonth,
                    maxSelectableYearMonth = maxSelectableYearMonth,
                    onClick = { month ->
                        onMonthSelectionChange(YearMonth.of(year, month))
                    }
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .requiredHeight(34.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (monthValue in 9 until 13) {
                MonthChip(
                    locale = locale,
                    monthValue = monthValue,
                    year = year,
                    currentYearMonth = currentYearMonth,
                    startYearMonth = startYearMonth,
                    endYearMonth = endYearMonth,
                    maxSelectableYearMonth = maxSelectableYearMonth,
                    onClick = { month ->
                        onMonthSelectionChange(YearMonth.of(year, month))
                    }
                )
            }
        }
    }
}

@Composable
fun MonthChip(
    locale: Locale,
    monthValue: Int,
    year: Int,
    currentYearMonth: YearMonth,
    startYearMonth: YearMonth?,
    endYearMonth: YearMonth?,
    maxSelectableYearMonth: YearMonth,
    onClick: (monthValue: Int) -> Unit,
) {
    val month by remember(key1 = monthValue) {
        mutableStateOf(
            Month.of(monthValue).getDisplayName(TextStyle.SHORT, locale)
        )
    }

    val selected by remember(year, startYearMonth, endYearMonth, monthValue) {
        mutableStateOf(
            (year == startYearMonth?.year && monthValue == startYearMonth.monthValue) || (year == endYearMonth?.year && monthValue == endYearMonth.monthValue)
        )
    }

    val enabled by remember(key1 = year, key2 = maxSelectableYearMonth, key3 = monthValue) {
        mutableStateOf(
            year < maxSelectableYearMonth.year || (year == maxSelectableYearMonth.year && monthValue <= maxSelectableYearMonth.monthValue)
        )
    }

    val isCurrentYearMonth by remember(key1 = year, key2 = currentYearMonth, key3 = monthValue) {
        mutableStateOf(
            year == currentYearMonth.year && monthValue == currentYearMonth.monthValue
        )
    }

    val inRange by remember(year, startYearMonth, endYearMonth, monthValue) {
        mutableStateOf(
            startYearMonth != null && endYearMonth != null && year >= startYearMonth.year && year <= endYearMonth.year && monthValue >= startYearMonth.monthValue && monthValue <= endYearMonth.monthValue
        )
    }

    Surface(
        selected = selected,
        enabled = enabled,
        onClick = {
            onClick(monthValue)
        },
        modifier = Modifier.semantics {
            role = Role.Button
        },
        shape = RoundedCornerShape(50),
        color = monthContainerColor(selected, enabled).value,
        contentColor = monthContentColor(isCurrentYearMonth, selected, inRange, enabled).value,
        border = if (isCurrentYearMonth && !selected) {
            BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.primary
            )
        } else null
    ) {
        Box(
            modifier = Modifier.requiredSize(78.dp, 34.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = month)
        }
    }
}

@Composable
private fun monthContainerColor(
    selected: Boolean,
    enabled: Boolean
): State<Color> {
    val target = if (enabled) {
        if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            Color.Transparent
        }
    } else {
        Color.Transparent
    }

    return animateColorAsState(target, label = "monthContainerColor")
}

@Composable
private fun monthContentColor(
    currentMonth: Boolean,
    selected: Boolean,
    inRange: Boolean,
    enabled: Boolean
): State<Color> {
    val target = when {
        selected && enabled -> MaterialTheme.colorScheme.onPrimary
        selected && !enabled -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f)
        inRange && enabled -> MaterialTheme.colorScheme.onSecondaryContainer
        inRange && !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        currentMonth -> MaterialTheme.colorScheme.primary
        enabled -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
    }

    return animateColorAsState(target, label = "monthContentColor")
}

private class SelectedRangeInfo(
    val gridStartCoordinates: IntOffset,
    val gridEndCoordinates: IntOffset,
    val isSelectionStart: Boolean,
    val isSelectionEnd: Boolean
) {
    companion object {
        fun calculateRangeInfo(
            year: Int,
            startMonth: YearMonth?,
            endMonth: YearMonth?
        ): SelectedRangeInfo? {
            if (
                startMonth == null || endMonth == null || startMonth.year > year || endMonth.year < year
            ) {
                return null
            }

            val isSelectionStart =
                startMonth.year == year && startMonth.monthValue >= Month.JANUARY.value
            val isSelectionEnd =
                endMonth.year == year && endMonth.monthValue <= Month.DECEMBER.value
            val startGridItemOffset = if (isSelectionStart) {
                startMonth.monthValue - 1
            } else {
                0
            }
            val endGridItemOffset = if (isSelectionEnd) {
                endMonth.monthValue - 1
            } else {
                11
            }

            // Calculate the selected coordinates within the grid.
            val gridStartCoordinates = IntOffset(
                x = startGridItemOffset % 4,
                y = startGridItemOffset / 4
            )
            val gridEndCoordinates = IntOffset(
                x = endGridItemOffset % 4,
                y = endGridItemOffset / 4
            )
            return SelectedRangeInfo(
                gridStartCoordinates,
                gridEndCoordinates,
                isSelectionStart,
                isSelectionEnd
            )
        }
    }
}

private fun ContentDrawScope.drawRangeBackground(
    selectedRangeInfo: SelectedRangeInfo,
    color: Color
) {
    val colCount = 4
    val monthContainerWidth = 78.dp.toPx()
    val monthContainerHeight = 34.dp.toPx()
    val verticalSpaceBetweenItems = 10.dp.toPx()
    val horizontalSpaceBetweenItems =
        (this.size.width - (colCount * monthContainerWidth)) / colCount

    val (x1, y1) = selectedRangeInfo.gridStartCoordinates
    val (x2, y2) = selectedRangeInfo.gridEndCoordinates
    // The endX and startX are offset to include only half the item's width when dealing with first
    // and last items in the selection in order to keep the selection edges rounded.
    var startX =
        (x1 * monthContainerWidth) +
                (if (selectedRangeInfo.isSelectionStart) monthContainerWidth / 2 else 0f) +
                (x1 * horizontalSpaceBetweenItems)
    val startY = y1 * (monthContainerHeight + verticalSpaceBetweenItems)
    var endX =
        x2 * (monthContainerWidth + horizontalSpaceBetweenItems) +
                (if (selectedRangeInfo.isSelectionEnd) monthContainerWidth / 2
                else monthContainerWidth) +
                horizontalSpaceBetweenItems
    val endY = y2 * (monthContainerHeight + verticalSpaceBetweenItems)

    val isRtl = layoutDirection == LayoutDirection.Rtl
    // Adjust the start and end in case the layout is RTL.
    if (isRtl) {
        startX = this.size.width - startX
        endX = this.size.width - endX
    }

    // Draw the first row background
    drawRect(
        color = color,
        topLeft = Offset(startX, startY),
        size =
        Size(
            width =
            when {
                y1 == y2 -> endX - startX
                isRtl -> -startX
                else -> this.size.width - startX
            },
            height = monthContainerHeight
        )
    )

    if (y1 != y2) {
        for (y in y2 - y1 - 1 downTo 1) {
            // Draw background behind the rows in between.
            drawRect(
                color = color,
                topLeft = Offset(
                    0f,
                    startY + (y * monthContainerHeight) + verticalSpaceBetweenItems
                ),
                size = Size(width = this.size.width, height = monthContainerHeight)
            )
        }
        // Draw the last row selection background
        val topLeftX = if (layoutDirection == LayoutDirection.Ltr) 0f else this.size.width
        drawRect(
            color = color,
            topLeft = Offset(topLeftX, endY),
            size =
            Size(
                width = if (isRtl) endX - this.size.width else endX,
                height = monthContainerHeight
            )
        )
    }
}