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

package jp.ikigai.cash.flow.ui.components.bottomsheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.ikigai.cash.flow.R

@Composable
fun TransactionActionsSheet(
    cloneTransaction: (Boolean) -> Unit,
    createTemplate: () -> Unit,
    dismiss: () -> Unit
) {
    val haptics = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .padding(start = 24.dp, end = 24.dp, bottom = 14.dp)
            .wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilledTonalButton(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                dismiss()
                cloneTransaction(true)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(35)
        ) {
            Text(text = stringResource(id = R.string.clone_transaction_current_date_button_label))
        }
        FilledTonalButton(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                dismiss()
                cloneTransaction(false)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(35)
        ) {
            Text(text = stringResource(id = R.string.clone_transaction_button_label))
        }
        FilledTonalButton(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                dismiss()
                createTemplate()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(35)
        ) {
            Text(text = stringResource(id = R.string.create_template_from_transaction_button_label))
        }
        FilledTonalButton(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                dismiss()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(35)
        ) {
            Text(text = stringResource(id = R.string.cancel_button_label))
        }
    }
}

@Preview
@Composable
fun TransactionActionsSheetPreview() {
    TransactionActionsSheet(
        cloneTransaction = {},
        createTemplate = {},
        dismiss = {}
    )
}