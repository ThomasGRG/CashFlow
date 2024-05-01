package jp.ikigai.cash.flow.ui.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import compose.icons.AllIcons
import compose.icons.TablerIcons
import compose.icons.tablericons.AlertOctagon

@Composable
fun IconDetailsDialog(
    dismiss: () -> Unit,
    iconName: String
) {
    val haptics = LocalHapticFeedback.current

    AlertDialog(
        onDismissRequest = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            dismiss()
        },
        confirmButton = {
            Button(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    dismiss()
                }
            ) {
                Text(text = "Close")
            }
        },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = iconName
                )
            }
        },
        icon = {
            Icon(
                imageVector = TablerIcons.AllIcons.find { it.name == iconName }
                    ?: TablerIcons.AlertOctagon,
                contentDescription = iconName,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    )
}

@Preview
@Composable
fun IconDetailsDialogPreview() {
    IconDetailsDialog(
        dismiss = {},
        iconName = "CalendarTime"
    )
}