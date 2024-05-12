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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.CalendarTime

@Composable
fun IconDetailsDialog(
    dismiss: () -> Unit,
    icon: ImageVector?
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
            icon?.let {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = icon.name
                    )
                }
            }
        },
        icon = {
            icon?.let {
                Icon(
                    imageVector = icon,
                    contentDescription = icon.name,
                    modifier = Modifier.size(120.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    )
}

@Preview
@Composable
fun IconDetailsDialogPreview() {
    IconDetailsDialog(
        dismiss = {},
        icon = TablerIcons.CalendarTime
    )
}