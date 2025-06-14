package jp.ikigai.cash.flow.ui.components.cards

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import jp.ikigai.cash.flow.data.dto.TemplateWithChips
import jp.ikigai.cash.flow.ui.components.common.CustomChip

@Composable
fun TransactionTemplateCard(
    templateWithChips: TemplateWithChips,
    modifier: Modifier = Modifier,
    onClick: (Long) -> Unit
) {
    val haptics = LocalHapticFeedback.current

    ElevatedCard(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick(templateWithChips.id)
        },
        modifier = modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = templateWithChips.annotatedName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
            )
            if (templateWithChips.amount.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = templateWithChips.typeIcon,
                        contentDescription = "type icon",
                        tint = templateWithChips.typeIconColor,
                        modifier = Modifier.size(30.dp)
                    )
                    Text(
                        text = templateWithChips.amount,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.Start),
                verticalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.Top)
            ) {
                templateWithChips.chips.forEach {
                    CustomChip(
                        icon = it.icon,
                        label = stringResource(id = it.resId, it.value)
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionTemplateCard(
    checked: Boolean,
    enabled: Boolean,
    templateWithChips: TemplateWithChips,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val alpha by remember(key1 = enabled) {
        mutableFloatStateOf(if (enabled) 1f else 0.38f)
    }

    val animatedIconColor by animateColorAsState(
        targetValue = if (enabled) {
            templateWithChips.typeIconColor
        } else {
            MaterialTheme.colorScheme.onBackground.copy(0.38f)
        },
        label = "animated icon color"
    )

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                enabled = enabled
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = templateWithChips.annotatedName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .alpha(alpha)
                )
                Checkbox(
                    checked = checked,
                    onCheckedChange = null,
                    enabled = enabled,
                    modifier = Modifier.padding(start = 10.dp, top = 5.dp)
                )
            }
            if (templateWithChips.amount.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = templateWithChips.typeIcon,
                        contentDescription = "type icon",
                        tint = animatedIconColor,
                        modifier = Modifier.size(30.dp)
                    )
                    Text(
                        text = templateWithChips.amount,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.alpha(alpha)
                    )
                }
            }
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.Start),
                verticalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.Top)
            ) {
                templateWithChips.chips.forEach {
                    CustomChip(
                        enabled = enabled,
                        icon = it.icon,
                        label = stringResource(id = it.resId, it.value)
                    )
                }
            }
        }
    }
}