package jp.ikigai.cash.flow.ui.components.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import jp.ikigai.cash.flow.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmDeleteSheet(
    message: String,
    dismiss: () -> Unit,
    delete: () -> Unit,
    sheetState: SheetState
) {
    val haptics = LocalHapticFeedback.current

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
            dismiss()
        },
        shape = RoundedCornerShape(10)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = message, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    dismiss()
                },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(35)
            ) {
                Text(text = stringResource(id = R.string.cancel_button_label))
            }
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    delete()
                    dismiss()
                },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(35)
            ) {
                Text(text = stringResource(id = R.string.delete_button_label))
            }
        }
    }
}