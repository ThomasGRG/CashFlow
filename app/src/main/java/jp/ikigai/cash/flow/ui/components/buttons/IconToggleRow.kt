package jp.ikigai.cash.flow.ui.components.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Archive

@Composable
fun IconToggleRow(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember {
        MutableInteractionSource()
    }
    val color =
        if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .selectable(
                interactionSource = interactionSource,
                indication = rememberRipple(),
                enabled = true,
                selected = selected,
                onClick = onClick
            )
            .background(color = color)
            .padding(10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = icon.name
        )
        Spacer(
            modifier = Modifier.width(6.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Preview
@Composable
fun IconToggleRowPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        IconToggleRow(label = "Category", icon = TablerIcons.Archive, selected = true, onClick = {})
        IconToggleRow(
            label = "Category",
            icon = TablerIcons.Archive,
            selected = false,
            onClick = {})
    }
}