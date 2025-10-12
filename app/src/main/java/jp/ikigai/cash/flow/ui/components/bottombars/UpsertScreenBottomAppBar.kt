package jp.ikigai.cash.flow.ui.components.bottombars

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.DeviceFloppy
import compose.icons.tablericons.Replace
import jp.ikigai.cash.flow.ui.components.common.CustomFloatingActionButton

@Composable
fun UpsertScreenBottomAppBar(
    enabled: Boolean,
    navigateBack: () -> Unit,
    saveClick: () -> Unit,
    deleteClick: (() -> Unit)? = null,
) {
    val haptics = LocalHapticFeedback.current

    BottomAppBar(
        modifier = Modifier.padding(top = 10.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
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
                        contentDescription = "navigate back",
                    )
                }
            }
            Row(
                modifier = Modifier.weight(3f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CustomFloatingActionButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        saveClick()
                    },
                    enabled = enabled
                ) {
                    Icon(
                        imageVector = TablerIcons.DeviceFloppy,
                        contentDescription = TablerIcons.DeviceFloppy.name,
                    )
                }
            }
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                deleteClick?.let {
                    IconButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            deleteClick()
                        },
                        enabled = enabled
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = Icons.Outlined.Delete.name,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UpsertScreenBottomAppBar(
    title: String,
    enabled: Boolean,
    navigateBack: () -> Unit,
    saveClick: () -> Unit,
    deleteClick: (() -> Unit)? = null,
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
            modifier = Modifier.padding(vertical = 10.dp)
        )
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 10.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
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
            deleteClick?.let {
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        deleteClick()
                    },
                    enabled = enabled
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = Icons.Outlined.Delete.name,
                    )
                }
            }
            CustomFloatingActionButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    saveClick()
                },
                enabled = enabled
            ) {
                Icon(
                    imageVector = TablerIcons.DeviceFloppy,
                    contentDescription = TablerIcons.DeviceFloppy.name,
                )
            }
        }
    }
}

@Composable
fun UpsertScreenBottomAppBar(
    title: String,
    enabled: Boolean,
    navigateBack: () -> Unit,
    saveClick: () -> Unit,
    deleteClick: (() -> Unit)? = null,
    migrateClick: (() -> Unit)? = null,
    transactionCount: String,
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
            modifier = Modifier.padding(vertical = 10.dp)
        )
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 10.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
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
            migrateClick?.let {
                BadgedBox(
                    badge = {
                        Badge(
                            content = {
                                Text(text = transactionCount)
                            },
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    },
                    content = {
                        IconButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                migrateClick()
                            },
                            enabled = enabled
                        ) {
                            Icon(
                                imageVector = TablerIcons.Replace,
                                contentDescription = TablerIcons.Replace.name,
                            )
                        }
                    }
                )
            }
            deleteClick?.let {
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        deleteClick()
                    },
                    enabled = enabled
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = Icons.Outlined.Delete.name,
                    )
                }
            }
            CustomFloatingActionButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    saveClick()
                },
                enabled = enabled
            ) {
                Icon(
                    imageVector = TablerIcons.DeviceFloppy,
                    contentDescription = TablerIcons.DeviceFloppy.name,
                )
            }
        }
    }
}

@Preview
@Composable
fun UpsertScreenBottomAppBarPreview() {
    Column {
        UpsertScreenBottomAppBar(
            enabled = true,
            navigateBack = {},
            saveClick = {},
            deleteClick = {},
        )
        UpsertScreenBottomAppBar(
            enabled = false,
            navigateBack = {},
            saveClick = {},
            deleteClick = null,
        )
    }
}