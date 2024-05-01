package jp.ikigai.cash.flow.ui.components.dialogs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ResetIconDialog(
    dismiss: () -> Unit,
    reset: () -> Unit,
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
                    reset()
                }
            ) {
                Text(text = "Reset")
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    dismiss()
                }
            ) {
                Text(text = "Cancel")
            }
        },
        text = {
            Text(text = "Are you sure you want to reset the icon to default?")
        },
        icon = {
            Icon(imageVector = Icons.Outlined.Info, contentDescription = "reset")
        }
    )
}

@Preview
@Composable
fun ResetIconDialogPreview() {
    ResetIconDialog(
        dismiss = {},
        reset = {}
    )
}