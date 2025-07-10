package jp.ikigai.cash.flow.ui.components.bottomsheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.CalendarTime

@Composable
fun IconDetailsSheet(
    dismiss: () -> Unit,
    icon: ImageVector?,
) {
    val haptics = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .padding(start = 24.dp, end = 24.dp, bottom = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        icon?.let {
            Icon(
                imageVector = icon,
                contentDescription = icon.name,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = icon.name,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium,
            )
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
            Text(text = "Close")
        }
    }
}

@Preview
@Composable
fun IconDetailsSheetPreview() {
    IconDetailsSheet(
        dismiss = {},
        icon = TablerIcons.CalendarTime
    )
}