package jp.ikigai.cash.flow.ui.components.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.ikigai.cash.flow.R
import java.time.ZonedDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerBottomSheet(
    time: ZonedDateTime,
    updateTime: (ZonedDateTime) -> Unit,
    dismiss: () -> Unit,
    sheetState: SheetState,
) {
    val timePickerState = rememberTimePickerState(
        is24Hour = false,
        initialHour = time.hour,
        initialMinute = time.minute
    )

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
            dismiss()
        },
        shape = RoundedCornerShape(10),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TimePicker(
                state = timePickerState
            )
        }
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
                    updateTime(time.withHour(timePickerState.hour).withMinute(timePickerState.minute))
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
fun TimePickerBottomSheetPreview() {
    TimePickerBottomSheet(
        time = ZonedDateTime.now(),
        updateTime = {},
        dismiss = {},
        sheetState = rememberModalBottomSheetState()
    )
}