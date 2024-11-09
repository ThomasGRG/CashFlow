package jp.ikigai.cash.flow.ui.components.common

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.PositionalThreshold
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.pullToRefreshIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowDown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneHandModeIndicator(
    state: PullToRefreshState,
    isRefreshing: Boolean,
    modifier: Modifier = Modifier,
    containerColor: Color = PullToRefreshDefaults.containerColor,
    color: Color = PullToRefreshDefaults.indicatorColor,
    threshold: Dp = PositionalThreshold,
) {
    Box(
        modifier = modifier
            .pullToRefreshIndicator(
                state = state,
                isRefreshing = isRefreshing,
                containerColor = containerColor,
                threshold = threshold,
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = TablerIcons.ArrowDown,
            contentDescription = TablerIcons.ArrowDown.name,
            tint = color
        )
    }
}