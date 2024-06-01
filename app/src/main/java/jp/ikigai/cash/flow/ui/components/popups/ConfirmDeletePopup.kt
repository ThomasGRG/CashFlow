package jp.ikigai.cash.flow.ui.components.popups

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.InfoCircle
import jp.ikigai.cash.flow.R

@Composable
fun ConfirmDeletePopup(
    message: String,
    dismiss: () -> Unit,
    delete: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = TablerIcons.InfoCircle,
            contentDescription = "caution icon",
            tint = MaterialTheme.colorScheme.error
        )
        Text(text = message, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
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
                    dismiss()
                    delete()
                },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(35)
            ) {
                Text(text = stringResource(id = R.string.delete_button_label))
            }
        }
    }
}

@Preview
@Composable
fun ConfirmDeletePopupPreview() {
    ConfirmDeletePopup(
        message = stringResource(id = R.string.delete_transaction_confirmation_label),
        dismiss = {},
        delete = {}
    )
}