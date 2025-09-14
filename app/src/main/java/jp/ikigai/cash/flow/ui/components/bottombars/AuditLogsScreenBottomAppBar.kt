package jp.ikigai.cash.flow.ui.components.bottombars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.CalendarEvent
import compose.icons.tablericons.SortAscending
import compose.icons.tablericons.SortDescending
import jp.ikigai.cash.flow.data.enums.SortDirection

@Composable
fun AuditLogsScreenBottomAppBar(
    navigateBack: () -> Unit,
    sortDirection: SortDirection,
    onSortClick: () -> Unit,
    onCalendarClick: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    BottomAppBar(
        modifier = Modifier.padding(top = 10.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        navigateBack()
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "navigate back"
                    )
                }
            }
            Row(
                modifier = Modifier.weight(3f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCalendarClick()
                    }
                ) {
                    Icon(
                        imageVector = TablerIcons.CalendarEvent,
                        contentDescription = "select time period"
                    )
                }
            }
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSortClick()
                    }
                ) {
                    Icon(
                        imageVector = if (sortDirection == SortDirection.DESC) {
                            TablerIcons.SortDescending
                        } else {
                            TablerIcons.SortAscending
                        },
                        contentDescription = "sort direction icon"
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun AuditLogsScreenBottomAppBarPreview() {
    AuditLogsScreenBottomAppBar(
        navigateBack = {},
        sortDirection = SortDirection.DESC,
        onSortClick = {},
        onCalendarClick = {}
    )
}