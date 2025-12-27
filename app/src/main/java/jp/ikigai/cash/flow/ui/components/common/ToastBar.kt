package jp.ikigai.cash.flow.ui.components.common

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import compose.icons.TablerIcons
import compose.icons.tablericons.X

@Composable
fun ToastBar(
    message: String,
    onDismiss: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    Snackbar(
        content = {
            Text(
                text = message
            )
        },
        dismissAction = {
            IconButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDismiss()
                },
                content = {
                    Icon(
                        imageVector = TablerIcons.X,
                        contentDescription = "dismiss",
                    )
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dismissActionContentColor = MaterialTheme.colorScheme.onSurface
    )
}

@Preview
@Composable
fun ToastBarPreview() {
    ToastBar(
        message = "Test",
        onDismiss = {}
    )
}