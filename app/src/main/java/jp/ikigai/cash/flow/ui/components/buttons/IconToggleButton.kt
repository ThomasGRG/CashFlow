package jp.ikigai.cash.flow.ui.components.buttons

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowsSort

@Composable
fun IconToggleButton(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    toggle: () -> Unit
) {
    val color by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
        label = "toggle_button_color"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .selectable(
                enabled = true,
                selected = selected,
                onClick = toggle
            )
            .background(color = color)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = icon.name
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Preview
@Composable
fun IconToggleButtonPreview() {
    Column {
        IconToggleButton(label = "INR", icon = TablerIcons.ArrowsSort, selected = true, toggle = {})
        IconToggleButton(
            label = "INR",
            icon = TablerIcons.ArrowsSort,
            selected = false,
            toggle = {})
    }
}