package jp.ikigai.cash.flow.ui.components.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.utils.toLocalMilli
import jp.ikigai.cash.flow.utils.toUTCZonedDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerBottomSheet(
    date: ZonedDateTime,
    selectableDates: SelectableDates = DatePickerDefaults.AllDates,
    setDate: (ZonedDateTime) -> Unit,
    dismiss: () -> Unit,
    sheetState: SheetState,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = date.toLocalMilli(),
        selectableDates = selectableDates
    )

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
            dismiss()
        },
        shape = RoundedCornerShape(10)
    ) {
        DatePicker(
            state = datePickerState,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(
                onClick = {
                    dismiss()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = stringResource(id = R.string.cancel_button_label))
            }
            TextButton(
                onClick = {
                    setDate(datePickerState.selectedDateMillis!!.toUTCZonedDateTime())
                    dismiss()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = stringResource(id = R.string.set_button_label))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun DatePickerBottomSheetPreview() {
    val now = ZonedDateTime.now(ZoneId.of("UTC"))
    DatePickerBottomSheet(
        date = now,
        selectableDates = DatePickerDefaults.AllDates,
        setDate = {},
        dismiss = {},
        sheetState = rememberModalBottomSheetState()
    )
}