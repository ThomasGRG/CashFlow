package jp.ikigai.cash.flow.ui.components.bottombars

import android.icu.util.Currency
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import compose.icons.TablerIcons
import compose.icons.tablericons.CalendarEvent
import compose.icons.tablericons.Filter

@Composable
fun TransactionScreenRoundedBottomBar(
    selectedCurrencySymbol: String,
    onCurrencyClick: () -> Unit,
    onCalendarClick: () -> Unit,
    addTransaction: () -> Unit,
    onFilterClick: () -> Unit,
    onMoreClick: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current

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
                    onFilterClick()
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                Icon(imageVector = TablerIcons.Filter, contentDescription = "filter")
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

@Preview
@Composable
fun TransactionScreenRoundedBottomBarPreview() {
    TransactionScreenRoundedBottomBar(
        selectedCurrencySymbol = Currency.getInstance("INR").symbol,
        onCurrencyClick = {},
        onCalendarClick = {},
        addTransaction = {},
        onFilterClick = {},
        onMoreClick = {},
    )
}