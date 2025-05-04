package jp.ikigai.cash.flow.ui.components.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.ChartLine
import jp.ikigai.cash.flow.R

@Composable
fun CustomChip(
    enabled: Boolean = true,
    icon: ImageVector,
    label: String
) {
    val animatedBackgroundColor by animateColorAsState(
        targetValue = if (enabled) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        label = "animated background color"
    )

    val animatedContentColor by animateColorAsState(
        targetValue = if (enabled) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        label = "animated background color"
    )

    val animatedBackgroundOpacity by animateFloatAsState(
        targetValue = if (enabled) {
            1f
        } else {
            0.12f
        },
        label = "animated background opacity"
    )

    val animatedContentOpacity by animateFloatAsState(
        targetValue = if (enabled) {
            1f
        } else {
            0.38f
        },
        label = "animated content opacity"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(animatedBackgroundColor.copy(animatedBackgroundOpacity))
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = icon.name,
            modifier = Modifier.padding(end = 6.dp),
            tint = animatedContentColor.copy(animatedContentOpacity)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = animatedContentColor.copy(animatedContentOpacity)
        )
    }
}

@Preview
@Composable
fun CustomChipPreview() {
    Column(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CustomChip(
            icon = TablerIcons.ChartLine,
            label = stringResource(R.string.frequency_label)
        )
        CustomChip(
            enabled = false,
            icon = TablerIcons.ChartLine,
            label = stringResource(R.string.frequency_label)
        )
    }
}