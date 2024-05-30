package jp.ikigai.cash.flow.ui.components.popups

import androidx.compose.foundation.background
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
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.utils.getStartOfDayInEpochMilli
import jp.ikigai.cash.flow.utils.toZonedDateTime
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerPopup(
    startDate: LocalDate,
    endDate: LocalDate,
    filter: (LocalDate, LocalDate) -> Unit,
    dismiss: () -> Unit,
) {
    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = startDate.getStartOfDayInEpochMilli(),
        initialSelectedEndDateMillis = endDate.getStartOfDayInEpochMilli()
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            .padding(top = 24.dp, bottom = 15.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DateRangePicker(
            state = dateRangePickerState,
            modifier = Modifier
                .fillMaxHeight(0.68f)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            OutlinedButton(
                onClick = {
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
                    dismiss()
                    filter(
                        (dateRangePickerState.selectedStartDateMillis!!).toZonedDateTime()
                            .toLocalDate(),
                        (dateRangePickerState.selectedEndDateMillis!!).toZonedDateTime()
                            .toLocalDate()
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

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun DateRangePickerPopupPreview() {
    DateRangePickerPopup(
        startDate = YearMonth.now().atDay(1),
        endDate = YearMonth.now().atEndOfMonth(),
        filter = { _, _ -> },
        dismiss = {})
}