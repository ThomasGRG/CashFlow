package jp.ikigai.cash.flow.ui.components.buttons

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

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
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium
        )
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