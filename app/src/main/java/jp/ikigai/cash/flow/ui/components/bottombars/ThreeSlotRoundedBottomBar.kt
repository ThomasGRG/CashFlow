package jp.ikigai.cash.flow.ui.components.bottombars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ThreeSlotRoundedBottomBar(
    navigateBack: () -> Unit,
    floatingButtonIcon: (@Composable () -> Unit)? = null,
    floatingButtonAction: (() -> Unit)? = null,
    extraButtonIcon: (@Composable () -> Unit)? = null,
    extraButtonAction: (() -> Unit)? = null,
) {
    val haptics = LocalHapticFeedback.current

    RoundedBottomBar {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        navigateBack()
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "navigate back"
                    )
                }
            }
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (floatingButtonAction != null && floatingButtonIcon != null) {
                    FloatingActionButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            floatingButtonAction()
                        }
                    ) {
                        floatingButtonIcon()
                    }
                }
            }
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (extraButtonAction != null && extraButtonIcon != null) {
                    IconButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            extraButtonAction()
                        }
                    ) {
                        extraButtonIcon()
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ThreeSlotRoundedBottomBarPreview() {
    Column {
        ThreeSlotRoundedBottomBar(
            navigateBack = {},
            floatingButtonIcon = {
                Icon(imageVector = Icons.Filled.Add, contentDescription = Icons.Filled.Add.name)
            },
            floatingButtonAction = {},
            extraButtonIcon = {
                Icon(imageVector = Icons.Filled.Delete, contentDescription = Icons.Filled.Delete.name)
            },
            extraButtonAction = {}
        )
        ThreeSlotRoundedBottomBar(
            navigateBack = {},
            floatingButtonIcon = {
                Icon(imageVector = Icons.Filled.Add, contentDescription = Icons.Filled.Add.name)
            },
            floatingButtonAction = {},
            extraButtonIcon = null,
            extraButtonAction = null
        )
        ThreeSlotRoundedBottomBar(
            navigateBack = {},
            floatingButtonIcon = null,
            floatingButtonAction = null,
            extraButtonIcon = null,
            extraButtonAction = null
        )
    }
}