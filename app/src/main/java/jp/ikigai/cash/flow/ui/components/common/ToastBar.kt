/*
CashFlow - Expense Tracker
Copyright (C) 2025 ThomasGRG

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

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