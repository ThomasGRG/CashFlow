package jp.ikigai.cash.flow.ui.components.buttons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ToggleButton(
    label: String,
    selected: Boolean,
    toggle: () -> Unit
) {
    val colors =
        if (selected) ButtonDefaults.filledTonalButtonColors() else ButtonDefaults.outlinedButtonColors()
    val border = if (!selected) ButtonDefaults.outlinedButtonBorder else null

    FilledTonalButton(
        onClick = toggle,
        colors = colors,
        border = border
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Preview
@Composable
fun ToggleButtonPreview() {
    Column {
        ToggleButton(
            label = "Transportation",
            selected = true,
            toggle = {}
        )
        ToggleButton(
            label = "Transportation",
            selected = false,
            toggle = {}
        )
    }
}