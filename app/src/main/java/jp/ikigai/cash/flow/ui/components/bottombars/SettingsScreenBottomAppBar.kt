package jp.ikigai.cash.flow.ui.components.bottombars

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft

@Composable
fun SettingsScreenBottomAppBar(
    navigateBack: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    BottomAppBar(
        modifier = Modifier.padding(top = 10.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        IconButton(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                navigateBack()
            },
            modifier = Modifier.defaultMinSize(minHeight = 70.dp, minWidth = 70.dp),
        ) {
            Icon(
                imageVector = TablerIcons.ArrowLeft,
                contentDescription = "navigate back"
            )
        }
    }
}

@Composable
fun SettingsScreenBottomAppBar(
    title: String,
    subTitle: String,
    navigateBack: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(20.dp)
            )
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(all = 10.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = subTitle,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.alpha(0.8f)
        )
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 10.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 66.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            IconButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    navigateBack()
                },
                modifier = Modifier.defaultMinSize(minHeight = 70.dp, minWidth = 70.dp),
            ) {
                Icon(
                    imageVector = TablerIcons.ArrowLeft,
                    contentDescription = "navigate back"
                )
            }
        }
    }
}