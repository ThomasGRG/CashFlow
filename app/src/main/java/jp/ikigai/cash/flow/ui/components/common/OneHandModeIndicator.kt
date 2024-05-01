package jp.ikigai.cash.flow.ui.components.common

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowDown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneHandModeIndicator(
    state: PullToRefreshState,
) {
    val targetAlpha by remember {
        derivedStateOf { if (state.isRefreshing) 1f else state.progress.coerceAtMost(1f) }
    }
    val alphaState by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "icon alpha animation"
    )
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Icon(
            imageVector = TablerIcons.ArrowDown,
            contentDescription = TablerIcons.ArrowDown.name,
            modifier = Modifier.alpha(alphaState)
        )
    }
}