package jp.ikigai.cash.flow.ui.components.bottomsheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.utils.getEndOfDay
import jp.ikigai.cash.flow.utils.getMonthEndDate
import jp.ikigai.cash.flow.utils.getMonthStartDate
import jp.ikigai.cash.flow.utils.getStartOfDay
import jp.ikigai.cash.flow.utils.toEpochMilli
import jp.ikigai.cash.flow.utils.toUTCZonedDateTime
import java.time.ZonedDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerSheet(
    startDate: ZonedDateTime,
    endDate: ZonedDateTime,
    filter: (ZonedDateTime, ZonedDateTime) -> Unit,
    dismiss: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = startDate.toEpochMilli(),
        initialSelectedEndDateMillis = endDate.toEpochMilli()
    )

    Column(
        modifier = Modifier.padding(bottom = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DateRangePicker(
            state = dateRangePickerState,
            modifier = Modifier
                .fillMaxHeight(0.68f),
            showModeToggle = false,
            colors = DatePickerDefaults.colors(
                containerColor = BottomSheetDefaults.ContainerColor
            )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
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
            Spacer(modifier = Modifier.width(10.dp))
            FilledTonalButton(
                enabled = dateRangePickerState.selectedStartDateMillis != null && dateRangePickerState.selectedEndDateMillis != null,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    dismiss()
                    filter(
                        (dateRangePickerState.selectedStartDateMillis!!).toUTCZonedDateTime()
                            .getStartOfDay(),
                        (dateRangePickerState.selectedEndDateMillis!!).toUTCZonedDateTime()
                            .getEndOfDay()
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(35),
            ) {
                Text(text = stringResource(id = R.string.filter_button_label))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerSheet(
    startDate: ZonedDateTime?,
    endDate: ZonedDateTime?,
    filter: (ZonedDateTime, ZonedDateTime) -> Unit,
    reset: () -> Unit,
    dismiss: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = startDate?.toEpochMilli(),
        initialSelectedEndDateMillis = endDate?.toEpochMilli()
    )

    Column(
        modifier = Modifier.padding(bottom = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DateRangePicker(
            state = dateRangePickerState,
            modifier = Modifier
                .fillMaxHeight(0.68f),
            showModeToggle = false,
            colors = DatePickerDefaults.colors(
                containerColor = BottomSheetDefaults.ContainerColor
            )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
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
                enabled = dateRangePickerState.selectedStartDateMillis != null && dateRangePickerState.selectedEndDateMillis != null,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    dismiss()
                    filter(
                        (dateRangePickerState.selectedStartDateMillis!!).toUTCZonedDateTime()
                            .getStartOfDay(),
                        (dateRangePickerState.selectedEndDateMillis!!).toUTCZonedDateTime()
                            .getEndOfDay()
                    )
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

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun DateRangePickerSheetPreview() {
    DateRangePickerSheet(
        startDate = getMonthStartDate(),
        endDate = getMonthEndDate(),
        filter = { _, _ -> },
        dismiss = {})
}