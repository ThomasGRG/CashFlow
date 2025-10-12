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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.ChartPie
import compose.icons.tablericons.SortDescending
import jp.ikigai.cash.flow.R

@Composable
fun ListingScreenBottomAppBar(
    navigateBack: () -> Unit,
    addClick: () -> Unit,
    sortClick: () -> Unit,
    sortIcon: ImageVector = TablerIcons.SortDescending,
    searchClick: () -> Unit,
    graphClick: () -> Unit,
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
                        contentDescription = "navigate back"
                    )
                }
            }
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        sortClick()
                    }
                ) {
                    Icon(
                        imageVector = sortIcon,
                        contentDescription = "sort"
                    )
                }
            }
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingActionButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        addClick()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "add"
                    )
                }
            }
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        searchClick()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "search"
                    )
                }
            }
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        graphClick()
                    }
                ) {
                    Icon(
                        imageVector = TablerIcons.ChartPie,
                        contentDescription = "graph"
                    )
                }
            }
        }
    }
}

@Composable
fun ListingScreenBottomAppBar(
    title: String,
    subTitle: String,
    navigateBack: () -> Unit,
    addClick: () -> Unit,
    sortClick: () -> Unit,
    sortIcon: ImageVector = TablerIcons.SortDescending,
    graphClick: () -> Unit,
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
            IconButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    sortClick()
                }
            ) {
                Icon(
                    imageVector = sortIcon,
                    contentDescription = "sort"
                )
            }
            IconButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    graphClick()
                }
            ) {
                Icon(
                    imageVector = TablerIcons.ChartPie,
                    contentDescription = "graph"
                )
            }
            FloatingActionButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    addClick()
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "add"
                )
            }
        }
    }
}

@Preview
@Composable
fun ListingScreenBottomAppBarPreview() {
    Column {
        ListingScreenBottomAppBar(
            navigateBack = {},
            sortClick = {},
            sortIcon = TablerIcons.SortDescending,
            addClick = {},
            searchClick = {},
            graphClick = {}
        )
        ListingScreenBottomAppBar(
            title = stringResource(id = R.string.categories_label),
            subTitle = stringResource(id = R.string.category_count_label, "23"),
            navigateBack = {},
            sortClick = {},
            sortIcon = TablerIcons.SortDescending,
            addClick = {},
            graphClick = {}
        )
    }
}